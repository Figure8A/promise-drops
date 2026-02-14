package com.pdrops.data;

import com.mojang.serialization.Codec;
import com.pdrops.PromiseDrops;
import com.pdrops.upgrades.PlayerUpgradeInstance;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentSyncPredicate;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;
@SuppressWarnings("UnstableApiUsage")
public class UpgradeSavesManager {
    public static final AttachmentType<List<PlayerUpgradeInstance>> SAVED_UPGRADES = AttachmentRegistry.create(
            PromiseDrops.of("saved_upgrades"), builder -> builder.initializer(ArrayList::new).persistent(PlayerUpgradeInstance.CODEC_WITH_TIME.listOf()));

    public static final AttachmentType<Integer> DEATH_FACE_TIMER = AttachmentRegistry.create(
            PromiseDrops.of("death_face_timer"), builder -> builder.initializer(() -> 0).syncWith(PacketCodecs.INTEGER, AttachmentSyncPredicate.all()).persistent(Codec.INT));



    public static List<PlayerUpgradeInstance> getUpgrades(World world) {
        return world.getAttachedOrCreate(SAVED_UPGRADES, ArrayList::new);
    }

    public static void setUpgrades(World world, List<PlayerUpgradeInstance> lawsHolder) {
        world.setAttached(SAVED_UPGRADES,  lawsHolder);
    }

    public static int getDeathFaceTimer(World world) {
        return world.getAttachedOrCreate(DEATH_FACE_TIMER);
    }

    public static void setDeathFaceTimer(World world, int deathFaceTimer) {
        world.setAttached(DEATH_FACE_TIMER, deathFaceTimer);
    }


    public static void init() {

    }

    public static void flushSaves(MinecraftServer server, boolean flush, boolean force) {
        var upgradeContainer = PromiseDrops.getUpgradeContainer(server.getOverworld());
        setUpgrades(server.getOverworld(), upgradeContainer.getUpgrades());
        PromiseDrops.LOGGER.info("Flushed Upgrades of {}", upgradeContainer);
    }

    public static void readSaves(MinecraftServer server) {
        var upgradeContainer = getUpgrades(server.getOverworld());
        PromiseDrops.getUpgradeContainer(server.getOverworld()).mergeLevels(upgradeContainer, true);
        PromiseDrops.LOGGER.info("Parsed Upgrades of {}", upgradeContainer);
    }
}
