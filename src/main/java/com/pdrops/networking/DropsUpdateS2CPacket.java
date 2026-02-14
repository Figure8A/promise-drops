package com.pdrops.networking;


import com.pdrops.PromiseDrops;
import com.pdrops.networking.registry.DropsNetworking;
import com.pdrops.upgrades.PlayerUpgradeCollectionType;
import com.pdrops.upgrades.PlayerUpgradeContainer;
import com.pdrops.upgrades.PlayerUpgradeGuiElement;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;

public record DropsUpdateS2CPacket(byte update) implements CustomPayload {
    public static final PacketCodec<RegistryByteBuf, DropsUpdateS2CPacket> CODEC = PacketCodec.tuple(PacketCodecs.BYTE, DropsUpdateS2CPacket::update, DropsUpdateS2CPacket::new);
    public static final Id<DropsUpdateS2CPacket> ID = new Id<>(PromiseDrops.of("lunacy_update"));


    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }



    @Environment(EnvType.CLIENT)
    public static void receive(MinecraftClient client, byte update) {
        PlayerUpgradeContainer container = PromiseDrops.getUpgradeContainer(client.world);
        switch (update) {
            case DropsNetworking.ADD_UPGRADE_GUI_HEALTH -> container.addLastGuiElement(new PlayerUpgradeGuiElement(PlayerUpgradeCollectionType.HEART));
            case DropsNetworking.ADD_UPGRADE_GUI_FOOD -> container.addLastGuiElement(new PlayerUpgradeGuiElement(PlayerUpgradeCollectionType.FOOD));
            case DropsNetworking.ADD_UPGRADE_SKILL_POINT -> container.addLastGuiElement(new PlayerUpgradeGuiElement(PlayerUpgradeCollectionType.SINGLE_SKILL_POINT));
            case DropsNetworking.ADD_UPGRADE_SKILL_POINTS -> container.addLastGuiElement(new PlayerUpgradeGuiElement(PlayerUpgradeCollectionType.SKILL_POINTS));
        }
    }

}
