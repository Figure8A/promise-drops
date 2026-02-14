package com.pdrops.upgrades;

import com.pdrops.PromiseDrops;
import com.pdrops.PromiseDropsClient;
import com.pdrops.data.UpgradeSavesManager;
import com.pdrops.entity.custom.DeathFaceEntity;
import com.pdrops.networking.DropsSyncUpgradesS2CPacket;
import com.pdrops.networking.DropsSyncUpgradesTimeS2CPacket;
import com.pdrops.upgrades.registry.DropRegistries;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class PlayerUpgradeContainer {
    private List<PlayerUpgradeInstance> upgrades = new ArrayList<>();
    private List<PlayerUpgradeGuiElement> upgradeGuiElements = new ArrayList<>();
    private int lastUpgradeDequeueTicks, upgradeDequeueTicks, upgradeDequeueIterations;
    private MinecraftServer server;
    private boolean client;
    private boolean dirty;
    private int lastDeathFaceSync;
    private int lastDeathFaceBarTicks, deathFaceBarTicks;
    public PlayerUpgradeContainer(MinecraftServer server) {
        super();
        this.setServer(server);
        client = false;
    }
    public PlayerUpgradeContainer() {
        this.resetUpgrades();
        client = true;
    }
    public void setPlayerUpgrade(PlayerUpgradeInstance newUpgrade) {
        setPlayerUpgrade(newUpgrade, false);
    }
    public void setPlayerUpgrade(PlayerUpgradeInstance newUpgrade, boolean syncTime) {
        if (this.hasUpgrade(newUpgrade)) {
            PlayerUpgradeInstance upgrade = this.getUpgrade(newUpgrade);
            int levelBefore = upgrade.getLevel();
            upgrade.setLevel(newUpgrade.getLevel());
            if (syncTime) {
                upgrade.setAmbientTicks(newUpgrade.getAmbientTicks());
            }
            if (upgrade.getLevel() <= 0) {
                this.getUpgrades().remove(upgrade);
            } else {
                if (levelBefore < upgrade.getLevel()) {
                    if (this.isOnClient() && !syncTime) {
                        for (int i = 0; i < upgrade.getLevel() - levelBefore; i++) {
                            addGuiElement(new PlayerUpgradeGuiElement(new PlayerUpgradeInstance(newUpgrade.getUpgrade(), levelBefore + i + 1)));
                        }
                    }
                }
            }
        } else {
            if (newUpgrade.getLevel() > 0) {
                this.getUpgrades().addFirst(newUpgrade);
                if (this.isOnClient() && !syncTime) {
                    for (int i = 0; i < newUpgrade.getLevel(); i++) {
                        addGuiElement(new PlayerUpgradeGuiElement(new PlayerUpgradeInstance(newUpgrade.getUpgrade(), i + 1)));
                    }
                }
            }
        }
        this.markDirty();
    }

    private void addLevelsBetween(PlayerUpgradeInstance newUpgrade, int level, int b) {
        int i1 = level - b;
        for (int i = 0; i < i1; i++) {
            addGuiElement(new PlayerUpgradeGuiElement(new PlayerUpgradeInstance(newUpgrade.getUpgrade(), (i1 + 1) - (newUpgrade.getLevel() - i))));
        }
    }

    public void addPlayerUpgrade(PlayerUpgradeInstance newUpgrade) {
        boolean merged = false;
        for (PlayerUpgradeInstance playerUpgrade : this.getUpgrades()) {
            if (playerUpgrade.mergeUpgrade(newUpgrade)) {
                merged = true;
                break;
            }
        }
        if (!merged) {
            this.getUpgrades().addFirst(newUpgrade);
        }
        this.markDirty();
    }

    public void mergeLevels(List<PlayerUpgradeInstance> upgrades, boolean syncTime) {
        for (PlayerUpgradeInstance upgrade : upgrades) {
            setPlayerUpgrade(upgrade, syncTime);
        }
    }

    public boolean tickClient(World world) {
        this.tick(world);
        this.lastUpgradeDequeueTicks = this.upgradeDequeueTicks;
        if (!this.getUpgradeGuiElements().isEmpty()) {
            this.upgradeDequeueTicks++;
            int upgradeDequeueTicks1 = this.getUpgradeDequeueTicks();
            if (this.getUpgradeGuiElements().size() == 1) {
                upgradeDequeueTicks1 = this.getUpgradeDequeueRate(0);
            }

            if (this.upgradeDequeueTicks >= upgradeDequeueTicks1) {
                this.getUpgradeGuiElements().removeLast();
                this.upgradeDequeueTicks = 0;
                upgradeDequeueIterations++;
                return !this.getUpgradeGuiElements().isEmpty();
            }
            if (upgradeDequeueIterations <= 1) {
                upgradeDequeueIterations = 1;
            }

            return upgradeDequeueIterations == 1 && upgradeDequeueTicks == 1;
        } else {
            upgradeDequeueIterations = 0;
        }
        this.getUpgrades().removeIf(PlayerUpgradeInstance::shouldRemove);
        return false;
    }

    public void tickServer(World world) {
        this.tick(world);
        if (this.isDirty()) {
            this.goodScrub();
            this.markClean();
        }
    }

    public void tick(World world) {
        this.getUpgrades().forEach(PlayerUpgradeInstance::tick);
        this.tickDeathFace(world);
    }


    public void goodScrub() {
        if (!this.isOnClient()) {
            this.getServer().getPlayerManager().getPlayerList().forEach(this::syncOne);
        }
    }

    public void syncOne(ServerPlayerEntity player) {
       syncOne(player, false);
    }


    public void syncOne(ServerPlayerEntity player, boolean syncTime) {
        ServerPlayNetworking.send(player, syncTime ? new DropsSyncUpgradesTimeS2CPacket(this.getUpgrades()) : new DropsSyncUpgradesS2CPacket(this.getUpgrades()));
    }


    public List<PlayerUpgradeInstance> getUpgrades() {
        return upgrades;
    }

    public boolean hasUpgrade(PlayerUpgradeInstance upgradeInstance) {
        return hasUpgrade(upgradeInstance.getUpgrade());
    }
    public boolean hasUpgrade(RegistryEntry<PlayerUpgrade> entry) {
        return this.getUpgrades().stream().map(PlayerUpgradeInstance::getUpgrade).anyMatch(entry::matches);
    }
    public PlayerUpgradeInstance getUpgrade(PlayerUpgradeInstance upgradeInstance) {
        return getUpgrade(upgradeInstance.getUpgrade());
    }
    public PlayerUpgradeInstance getUpgrade(RegistryEntry<PlayerUpgrade> upgradeRegistryEntry) {
        for (PlayerUpgradeInstance playerUpgrade : this.getUpgrades()) {
            if (playerUpgrade.getUpgrade().matches(upgradeRegistryEntry)) {
                return playerUpgrade;
            }
        }
        //lazy fallback
        return new PlayerUpgradeInstance(upgradeRegistryEntry, 0);
    }

    public boolean hasUpgradesAtMax() {
        if (this.getUpgrades().size() != DropRegistries.PLAYER_UPGRADE.size()) {
            return false;
        }
        for (PlayerUpgradeInstance playerUpgradeInstance : this.getUpgrades()) {
            if (!playerUpgradeInstance.isAtMaxLevel()) {
                return false;
            }
        }
        return true;
    }


    private boolean isOnClient() {
        return client;
    }

    public boolean isDirty() {
        return dirty;
    }

    public void markDirty() {
        this.dirty = true;
    }

    public void markClean() {
        this.dirty = false;
    }
    public void setServer(MinecraftServer server) {
        this.server = server;
    }

    public MinecraftServer getServer() {
        return server;
    }

    public void resetUpgrades() {
        upgrades = new ArrayList<>();
    }


    public boolean isUpgradeAtMaxLevel(PlayerUpgradeInstance upgrade) {
        return this.getUpgrade(upgrade).isAtMaxLevel();
    }

    public List<PlayerUpgradeGuiElement> getUpgradeGuiElements() {
        return upgradeGuiElements;
    }
    public float getLerpUpgradeDequeueTicks(float delta) {
        return getDeltaUpgradeDequeueTicks(delta) / (float) (this.getUpgradeGuiElements().size() == 1 ? this.getUpgradeDequeueRate(0) : this.getUpgradeDequeueTicks());
    }

    public float getDeltaUpgradeDequeueTicks(float delta) {
        float lerp = MathHelper.lerp(delta, (float) this.lastUpgradeDequeueTicks, (float) this.upgradeDequeueTicks);
        return Math.min(lerp, (float) upgradeDequeueTicks);
    }
    public PlayerUpgradeGuiElement getCurrentUpgradeGuiElement() {
        return this.getUpgradeGuiElements().getLast();
    }

    public int collectNewUpgradesInQueue() {
        if (!this.hasElementsInQueue()) {
            return 0;
        }
        int levelsAt1 = 0;
        for (PlayerUpgradeGuiElement guiElement : this.getUpgradeGuiElements()) {
            levelsAt1 += guiElement.mapToFirstLevel();
        }
        return levelsAt1;
    }

    public boolean hasElementsInQueue() {
        return !this.getUpgradeGuiElements().isEmpty();
    }
    public void addGuiElement(PlayerUpgradeGuiElement playerUpgradeGuiElement) {
        this.getUpgradeGuiElements().addFirst(playerUpgradeGuiElement);
    }
    public void addLastGuiElement(PlayerUpgradeGuiElement playerUpgradeGuiElement) {
        this.getUpgradeGuiElements().addLast(playerUpgradeGuiElement);
    }

    public int getUpgradeDequeueTicks() {
        return getUpgradeDequeueRate(upgradeDequeueIterations);
    }

    public int getUpgradeDequeueRate(int override) {
        int returnT = 55;
        switch (override) {
            case 1 -> returnT = 55;
            case 2 -> returnT = 50;
            case 3 ->  returnT = 43;
            case 4 ->  returnT = 33;
            case 5 ->  returnT = 24;
            case 6 ->  returnT = 13;
            case 7 ->  returnT = 6;
            case 8 ->  returnT = 3;
            case 9 -> returnT = 2;
        }
        if (override >= 10) {
            return 2;
        }
        return returnT;
    }

    public int getLowestLevelFor(PlayerUpgradeInstance entry) {
        int lowestLevel = entry.getLevel();
        if (this.hasElementsInQueue()) {
            for (PlayerUpgradeGuiElement playerUpgradeGuiElement : this.getUpgradeGuiElements()) {
                if (playerUpgradeGuiElement.getUpgradeInstance() != null && playerUpgradeGuiElement.getUpgradeInstance().getUpgrade().matches(entry.getUpgrade())) {
                    lowestLevel = Math.min(lowestLevel, playerUpgradeGuiElement.getUpgradeInstance().getLevel());
                }
            }
        }
        return lowestLevel;
    }

    public int getUpgradeDequeueIterations() {
        return upgradeDequeueIterations;
    }

    public void nukeAllUpgradesClient(World world) {
        for (PlayerUpgradeInstance playerUpgradeInstance : this.getUpgrades()) {
            playerUpgradeInstance.setMaxRemoveTicks(world.getRandom().nextBetween(80, 89));
            playerUpgradeInstance.setXOffset(world.getRandom().nextBetween(-200, 200));
        }
        this.getUpgradeGuiElements().clear();
    }

    private void tickDeathFace(World world) {
        this.lastDeathFaceBarTicks = deathFaceBarTicks;
        int deathFaceTimer = UpgradeSavesManager.getDeathFaceTimer(world);
        if (deathFaceTimer > 0) {
            UpgradeSavesManager.setDeathFaceTimer(world, --deathFaceTimer);
            if (deathFaceTimer < 20 && !world.isClient()) {
                PromiseDrops.LOGGER.info("Spawing Death Face in {} ticks, the world ticking him currently is {}", deathFaceTimer, world.getRegistryKey());
            }
            if (deathFaceTimer == 0) {
                this.spawnDeathFace(world);
            }
            if (deathFaceBarTicks < 60) {
                deathFaceBarTicks++;
            }
        } else {
            if (deathFaceBarTicks > 0) {
                deathFaceBarTicks--;
            }
        }

    }

    public void spawnDeathFace(World world) {
        if (!world.isClient()) {
            ServerWorld serverWorld = (ServerWorld) world;
            PlayerEntity player = serverWorld.getServer().getPlayerManager().getPlayerList().stream().filter(PlayerEntity::isInteractable).findFirst().orElse(null);
            if (player != null) {
                PromiseDrops.LOGGER.info("Spawing Death Face in world {}", world.getRegistryKey());
                Vec3d playerPos = player.getEntityPos();
                Direction facing = player.getHorizontalFacing();
                if (facing.getAxis().equals(Direction.Axis.Y)) {
                    facing = Direction.NORTH;
                }
                playerPos = playerPos.add(facing.getDoubleVector().getHorizontal().multiply(15));
                DeathFaceEntity deathFaceEntity = new DeathFaceEntity(player.getEntityWorld(), playerPos, PromiseDrops.isTheNja09(player));
                player.getEntityWorld().spawnEntity(deathFaceEntity);
            } else {
                PromiseDrops.LOGGER.info("Could not find a player to spawn death face, this should not happen.");
            }

        }
    }


    public void onDeathFaceUpdate(int before, int after) {
        this.lastDeathFaceSync = Math.max(this.lastDeathFaceSync, after);
        if (after == 0 || Math.abs(before - after) >= 20) {
            this.lastDeathFaceSync = 0;
        }
    }

    public int getLastDeathFaceSync() {
        return lastDeathFaceSync;
    }

    public float getLerpDeathFaceBarTicks(float delta) {
        return getDeltaDeathFaceBarTicks(delta) / 60f;
    }

    public float getDeltaDeathFaceBarTicks(float delta) {
        return MathHelper.lerp(delta, (float) this.lastDeathFaceBarTicks, (float) this.deathFaceBarTicks);
    }

    public void startSpawningDeathFace(ServerWorld world) {
        UpgradeSavesManager.setDeathFaceTimer(world, 1200);
    }
}
