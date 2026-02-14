package com.pdrops;

import com.google.common.base.Suppliers;
import com.pdrops.command.PlayerUpgradeSets;
import com.pdrops.data.UpgradeSavesManager;
import com.pdrops.entity.DropsEntities;
import com.pdrops.entity.custom.DeathFaceEntity;
import com.pdrops.entity.inter.IDeathFaceGetter;
import com.pdrops.networking.DropsUpdateS2CPacket;
import com.pdrops.networking.registry.DropsNetworking;
import com.pdrops.particles.registry.DropsParticles;
import com.pdrops.sounds.DropsSounds;
import com.pdrops.upgrades.*;
import com.pdrops.upgrades.registry.DropRegistries;
import com.pdrops.upgrades.registry.PlayerUpgrades;
import com.pdrops.waypoint.CustomWaypointStyles;
import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.entity.event.v1.ServerEntityWorldChangeEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricTrackedDataRegistry;
import net.minecraft.advancement.Advancement;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.TrackedDataHandler;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Colors;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.TeleportTarget;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.util.*;
import java.util.List;

public class PromiseDrops implements ModInitializer {
	public static final String MOD_ID = "promise-drops";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
	public static final TrackedDataHandler<PlayerUpgradeInstance> UPGRADE_INSTANCE = TrackedDataHandler.create(PlayerUpgradeInstance.PACKET_CODEC);
	public static final TrackedDataHandler<PlayerUpgradeCollectionType> UPGRADE_COLLECTION_TYPE = TrackedDataHandler.create(PlayerUpgradeCollectionType.PACKET_CODEC);
	public static final RegistryKey<World> INFINITE_GARDEN = RegistryKey.of(RegistryKeys.WORLD, Identifier.of("roguecraft", "infinite_garden"));
	public static final int DARK_GREEN = Suppliers.memoize(() -> new Color(Colors.GREEN).darker().getRGB()).get();

	@Override
	public void onInitialize() {
		DropsParticles.init();
		DropsSounds.init();
		PlayerUpgrades.initialize();
		DropRegistries.registerRegistry();
		ServerPlayerEvents.JOIN.register(this::tryToSyncJoinData);
		DropsNetworking.registerC2SDropPayloads();
		DropsNetworking.registerS2CDropPayloads();
		DropsNetworking.registerC2SDropPackets();
		DropsEntities.registerDropsEntities();
		FabricTrackedDataRegistry.register(of("player_upgrades"), UPGRADE_INSTANCE);
		FabricTrackedDataRegistry.register(of("upgrade_collection_type"), UPGRADE_COLLECTION_TYPE);
		CommandRegistrationCallback.EVENT.register(PlayerUpgradeSets::register);
		UpgradeSavesManager.init();
		ServerLifecycleEvents.BEFORE_SAVE.register(UpgradeSavesManager::flushSaves);
		ServerLifecycleEvents.SERVER_STARTED.register(UpgradeSavesManager::readSaves);
		ServerEntityWorldChangeEvents.AFTER_PLAYER_CHANGE_WORLD.register(this::addRougecraftReset);
		ServerLivingEntityEvents.AFTER_DEATH.register(this::tryAddingDeathFace);
		FabricDefaultAttributeRegistry.register(DropsEntities.DEATH_FACE, DeathFaceEntity.createDeathFaceAttributes());
		CustomWaypointStyles.init();
	}

	private void tryAddingDeathFace(LivingEntity livingEntity, DamageSource damageSource) {
		if (livingEntity instanceof PlayerEntity player && !player.getEntityWorld().isClient()) {
			ServerWorld world = (ServerWorld) player.getEntityWorld();
			if (isOnlyOnePlayerPlaying(world.getServer().getPlayerManager().getPlayerList())) {
				getUpgradeContainer(world).startSpawningDeathFace(world.getServer().getOverworld());
			} else if (UpgradeSavesManager.getDeathFaceTimer(world) > 0) {
				UpgradeSavesManager.setDeathFaceTimer(world, 0);
			}
		}
	}

	public static boolean isOnlyOnePlayerPlaying(List<? extends PlayerEntity> players) {
		boolean multiplePlayers = players.size() > 1;
		long count = players.stream().filter(LivingEntity::isInteractable).count();
		//PromiseDrops.LOGGER.info("Players Alive: {}, should spawn death face? {}", count, count == 1);
		return multiplePlayers && count == 1;
	}
	public static boolean allPlayersDead(List<? extends PlayerEntity> players) {
		return players.stream().filter(LivingEntity::isInteractable).count() == 0;
	}


	private void addRougecraftReset(ServerPlayerEntity player, ServerWorld origin, ServerWorld destination) {
		PromiseDrops.LOGGER.info("World? {}", destination.getRegistryKey());
		if (isRougecraftWorld(destination)) {
			PlayerUpgradeContainer upgradeContainer = getUpgradeContainer(origin);
			upgradeContainer.resetUpgrades();
			upgradeContainer.markDirty();
			UpgradeSavesManager.setDeathFaceTimer(destination.getServer().getOverworld(), 0);
			PromiseDrops.LOGGER.info("Playing Rougecraft, flush all upgrades");
		}
		DeathFaceEntity deathFaceEntity = ((IDeathFaceGetter) player).getDeathFaceEntity();
		if (deathFaceEntity != null) {
			if (!deathFaceEntity.isRemoved()) {
				deathFaceEntity.teleport(player.getEntityWorld(), player.getX() + 10, player.getY(), player.getZ(), Set.of(), player.getYaw(), player.getPitch(), false);
				deathFaceEntity.age = 0;
			}
		}


	}

	public static boolean isRougecraftWorld(World world) {
		return world.getRegistryKey().equals(INFINITE_GARDEN);
	}

	private void tryToSyncJoinData(ServerPlayerEntity serverPlayerEntity) {
		getUpgradeContainer(serverPlayerEntity.getEntityWorld()).syncOne(serverPlayerEntity, true);
		if (isFigure(serverPlayerEntity)) {
			serverPlayerEntity.getWaypointConfig().style = CustomWaypointStyles.FIGURE;
			serverPlayerEntity.getWaypointConfig().color = Optional.of(Colors.WHITE);
		}

	}
	public static Identifier of(String id) {
		return Identifier.of(MOD_ID, id);
	}
	public static PlayerUpgradeContainer getUpgradeContainer(World world) {
		return ((IUpgradeGetter) world).getContainer();
	}
	public static void updatePlayerStatus(ServerPlayerEntity serverPlayerEntity, byte status) {
		ServerPlayNetworking.send(serverPlayerEntity, new DropsUpdateS2CPacket(status));
	}

	public static int getColorFromTier(int level) {
		return switch (level) {
            case 1 -> Colors.LIGHT_RED;
			case 2 -> Colors.YELLOW;
			case 3 -> Colors.LIGHT_YELLOW;
			case 4 -> Colors.GREEN;
			case 5 -> DARK_GREEN;
			case 6 -> Colors.BLUE;
			case 7 -> Colors.CYAN;
			case 8 -> Colors.LIGHT_PINK;
			case 9 -> Colors.PURPLE;
			case 10 -> Colors.WHITE;
            default -> Colors.ALTERNATE_WHITE;
        };
	}
	public static boolean isTheNja09(@Nullable Entity entity) {
		return entity != null && entity.getUuidAsString().equals("02e52e2d-6c47-466f-98b8-f4e0afb74801");
	}
	public static boolean isFigure(@Nullable Entity entity) {
		return entity != null && entity.getUuidAsString().equals("e0d11d90-6ebc-4125-86d2-80511459c48e");
	}
}