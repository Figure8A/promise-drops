package com.pdrops.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import com.pdrops.PromiseDrops;
import com.pdrops.entity.custom.UpgradeEntity;
import com.pdrops.particles.registry.DropsParticles;
import com.pdrops.sounds.DropsSounds;
import com.pdrops.upgrades.PlayerUpgrade;
import com.pdrops.upgrades.PlayerUpgradeCollectionType;
import com.pdrops.upgrades.PlayerUpgradeContainer;
import com.pdrops.upgrades.PlayerUpgradeInstance;
import com.pdrops.upgrades.registry.DropRegistries;
import com.pdrops.upgrades.registry.PlayerUpgrades;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.boss.WitherEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.mob.WardenEntity;
import net.minecraft.entity.passive.FishEntity;
import net.minecraft.entity.passive.IronGolemEntity;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Util;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity {

	public LivingEntityMixin(EntityType<?> type, World world) {
		super(type, world);
	}
	@Inject(method = "getAttributeValue", at = @At(value = "RETURN"), cancellable = true)
	public void invokeUpgrades(RegistryEntry<EntityAttribute> attribute, CallbackInfoReturnable<Double> cir) {
		if (LivingEntity.class.cast(this) instanceof PlayerEntity player) {
			List<PlayerUpgradeInstance> playerUpgrades = PromiseDrops.getUpgradeContainer(player.getEntityWorld()).getUpgrades();
			AtomicReference<Double> i = new AtomicReference<>(cir.getReturnValue());
			if (!playerUpgrades.isEmpty()) {
				playerUpgrades.forEach(playerUpgrade -> i.set(playerUpgrade.modifyAttribute(player, attribute, i.get())));
			}
			cir.setReturnValue(i.get());
		}
	}

	@Redirect(method = "getNextAirUnderwater", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/attribute/EntityAttributeInstance;getValue()D"))
	public double invokeWater(EntityAttributeInstance instance) {
		if (LivingEntity.class.cast(this) instanceof PlayerEntity player) {
			var playerUpgrades = PromiseDrops.getUpgradeContainer(player.getEntityWorld()).getUpgrade(PlayerUpgrades.OXYGEN_BONUS);
			if (playerUpgrades.getLevel() > 0) {
				return playerUpgrades.modifyAttribute(player, instance.getAttribute(), instance.getValue());
			}
		}
		return instance.getValue();
	}

	@Inject(method = "onDeath", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;onKilledBy(Lnet/minecraft/entity/LivingEntity;)V"))
	public void tryDropping(DamageSource damageSource, CallbackInfo ci, @Local LivingEntity livingEntity) {
		if (livingEntity instanceof PlayerEntity player && !this.getEntityWorld().isClient() && !PromiseDrops.isRougecraftWorld(getEntityWorld())) {
			var container = PromiseDrops.getUpgradeContainer(player.getEntityWorld());
			int i = this.getRandom().nextInt(100);
			PlayerUpgradeInstance upgrade = container.getUpgrade(PlayerUpgrades.DROP_CHANCE);
			int upgradeDropChance = this.getUpgradeDropChance() + ((int)upgrade.getUpgradeBonus());
			PromiseDrops.LOGGER.info("Trying to drop, {} vs {}, it is {}", i, upgradeDropChance, i < upgradeDropChance);
			if (i < upgradeDropChance) {
				RegistryEntry.Reference<PlayerUpgrade> rUpgrade = Util.getRandom(DropRegistries.PLAYER_UPGRADE.streamEntries().toList(), this.getRandom());
				UpgradeEntity upgradeEntity = UpgradeEntity.create(getEntityWorld(), this.getX(), this.getY(), this.getZ(), 1, rUpgrade);
				upgradeEntity.setVelocity(this.getVelocity().multiply(1.5f));
				if (container.getUpgrade(upgradeEntity.getUpgrade()).isAtMaxLevel()) {
					if (!this.tryReRollingUpgrade(this.getRandom(), upgradeEntity, container)) {
						upgradeEntity.setUpgradeCollectionType(PlayerUpgradeCollectionType.HEART);
					}
				}
				if (this.getEntityWorld() instanceof ServerWorld serverWorld) {
					if (PromiseDrops.isOnlyOnePlayerPlaying(serverWorld.getServer().getPlayerManager().getPlayerList())) {
						upgradeEntity.setUpgradeCollectionType(this.getRandom().nextBoolean() ? PlayerUpgradeCollectionType.SINGLE_SKILL_POINT : PlayerUpgradeCollectionType.SKILL_POINTS);
					}
				}

				this.playSound(DropsSounds.DROP_UPGRADE, 0.5f, this.random.nextFloat() * 0.2F + 0.9F);
				if (this.getEntityWorld() instanceof ServerWorld serverWorld) {
					serverWorld.spawnParticles(DropsParticles.DROPPED_UPGRADE, this.getX(), this.getBodyY(0.5), this.getZ(), 1, 0.0, 0.0, 0.0, 0.0);
				}
			}
		}
	}

	private boolean tryReRollingUpgrade(Random random, UpgradeEntity upgradeEntity, PlayerUpgradeContainer container) {
		List<RegistryEntry.Reference<PlayerUpgrade>> allUpgrades = DropRegistries.PLAYER_UPGRADE.streamEntries().toList();
		//PromiseDrops.LOGGER.info("{}, is max level!!", upgradeEntity.getUpgrade().getUpgrade());
		for (int i = 0; i < 10; i++) {
			var upgrade = Util.getRandom(allUpgrades, random);
			boolean upgradeMax = container.getUpgrade(upgrade).isAtMaxLevel();
			//PromiseDrops.LOGGER.info("Roll {}, Upgrade: {}, max level? {}",i, upgrade, upgradeMax);
			if (!upgradeMax) {
				upgradeEntity.setUpgrade(new PlayerUpgradeInstance(upgrade, upgradeEntity.getUpgrade().getLevel()));
				//PromiseDrops.LOGGER.info("Upgrade not max, ship it");
				return true;
			}
		}
		return false;
	}

	@Unique
    public int getUpgradeDropChance() {
		var living = LivingEntity.class.cast(this);
		int i = 0;
		if (living instanceof HostileEntity) {
			i = 25;
		} else if (living instanceof PassiveEntity) {
			i = 5;
		} else if (living instanceof FishEntity) {
			i = 10;
		} else if (living instanceof IronGolemEntity || living instanceof WardenEntity || living instanceof WitherEntity || living instanceof PlayerEntity) {
			i = 100;
		}
		if (living.isBaby()) {
			i += 5;
		}
		return i;
	}

}