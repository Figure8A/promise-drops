package com.pdrops.networking;


import com.pdrops.PromiseDrops;
import com.pdrops.upgrades.PlayerUpgrade;
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

public record DropsSyncUpgradesS2CPacket(List<PlayerUpgradeInstance> upgrades) implements CustomPayload {
    public static final PacketCodec<RegistryByteBuf, DropsSyncUpgradesS2CPacket> CODEC = PacketCodec.tuple(PlayerUpgradeInstance.PACKET_CODEC.collect(PacketCodecs.toList()), DropsSyncUpgradesS2CPacket::upgrades, DropsSyncUpgradesS2CPacket::new);
    public static final Id<DropsSyncUpgradesS2CPacket> ID = new Id<>(PromiseDrops.of("drops_upgrade_sync"));


    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }



}
