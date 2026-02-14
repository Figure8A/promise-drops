package com.pdrops.networking.registry;

import com.pdrops.networking.DropsSyncUpgradesS2CPacket;
import com.pdrops.networking.DropsSyncUpgradesTimeS2CPacket;
import com.pdrops.networking.DropsUpdateS2CPacket;
import com.pdrops.networking.OnDropClientC2SPacket;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

public class DropsNetworking {

    public static final byte ADD_UPGRADE_GUI_HEALTH = 0;
    public static final byte ADD_UPGRADE_GUI_FOOD = 1;
    public static final byte ADD_UPGRADE_SKILL_POINT = 2;
    public static final byte ADD_UPGRADE_SKILL_POINTS = 3;

    public static void registerC2SDropPayloads() {
        PayloadTypeRegistry.playC2S().register(OnDropClientC2SPacket.ID, OnDropClientC2SPacket.CODEC);
    }

    public static void registerS2CDropPayloads() {
        PayloadTypeRegistry.playS2C().register(DropsUpdateS2CPacket.ID, DropsUpdateS2CPacket.CODEC);
        PayloadTypeRegistry.playS2C().register(DropsSyncUpgradesTimeS2CPacket.ID, DropsSyncUpgradesTimeS2CPacket.CODEC);
        PayloadTypeRegistry.playS2C().register(DropsSyncUpgradesS2CPacket.ID, DropsSyncUpgradesS2CPacket.CODEC);
    }

    public static void registerC2SDropPackets() {
        ServerPlayNetworking.registerGlobalReceiver(OnDropClientC2SPacket.ID, (payload, context) -> OnDropClientC2SPacket.receive(context.player().getEntityWorld().getServer(), context.player(), payload.type()));
    }

    public static void registerS2CDropPackets() {
        ClientPlayNetworking.registerGlobalReceiver(DropsUpdateS2CPacket.ID, (payload, context) -> context.client().execute(() ->  DropsUpdateS2CPacket.receive(context.client(), payload.update())));
        ClientPlayNetworking.registerGlobalReceiver(DropsSyncUpgradesTimeS2CPacket.ID, (payload, context) -> context.client().execute(() ->  DropsSyncUpgradesTimeS2CPacket.receive(context.client(), payload.upgrades(), true)));
        ClientPlayNetworking.registerGlobalReceiver(DropsSyncUpgradesS2CPacket.ID, (payload, context) -> context.client().execute(() ->  DropsSyncUpgradesTimeS2CPacket.receive(context.client(), payload.upgrades(), false)));

    }

}
