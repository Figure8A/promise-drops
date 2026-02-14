package com.pdrops.networking;


import com.pdrops.PromiseDrops;
import com.pdrops.upgrades.PlayerUpgrade;
import com.pdrops.upgrades.PlayerUpgradeContainer;
import com.pdrops.upgrades.PlayerUpgradeInstance;
import com.pdrops.upgrades.registry.DropRegistries;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.registry.entry.RegistryEntry;

import java.util.ArrayList;
import java.util.List;

public record DropsSyncUpgradesTimeS2CPacket(List<PlayerUpgradeInstance> upgrades) implements CustomPayload {
    public static final PacketCodec<RegistryByteBuf, DropsSyncUpgradesTimeS2CPacket> CODEC = PacketCodec.tuple(PlayerUpgradeInstance.PACKET_CODEC_WITH_TIME.collect(PacketCodecs.toList()), DropsSyncUpgradesTimeS2CPacket::upgrades, DropsSyncUpgradesTimeS2CPacket::new);
    public static final Id<DropsSyncUpgradesTimeS2CPacket> ID = new Id<>(PromiseDrops.of("drops_upgrade_with_time_sync"));


    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }



    @Environment(EnvType.CLIENT)
    public static void receive(MinecraftClient client, List<PlayerUpgradeInstance> upgrades, boolean syncTime) {
        PlayerUpgradeContainer upgradeContainer = PromiseDrops.getUpgradeContainer(client.world);
        List<PlayerUpgradeInstance> upgradeList = new ArrayList<>(upgrades);
        if (upgradeList.isEmpty() && !upgradeContainer.getUpgrades().isEmpty()) {
            upgradeContainer.nukeAllUpgradesClient(client.world);
            return;
        }

        var d = upgradeList.stream().map(PlayerUpgradeInstance::getUpgrade).toList();
        for (RegistryEntry.Reference<PlayerUpgrade> upgradeRegistryEntry : DropRegistries.PLAYER_UPGRADE.streamEntries().toList()) {
            if (d.stream().noneMatch(upgradeRegistryEntry::matches)) {
                upgradeList.add(new PlayerUpgradeInstance(upgradeRegistryEntry, 0));
            }
        }
        upgradeContainer.mergeLevels(upgradeList, syncTime);
    }

}
