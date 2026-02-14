package com.pdrops.networking;

import com.pdrops.PromiseDrops;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;



public record OnDropClientC2SPacket(byte type) implements CustomPayload {
    public static final PacketCodec<RegistryByteBuf, OnDropClientC2SPacket> CODEC = PacketCodec.tuple(PacketCodecs.BYTE, OnDropClientC2SPacket::type, OnDropClientC2SPacket::new);
    public static final Id<OnDropClientC2SPacket> ID = new Id<>(PromiseDrops.of("on_action_cs"));


    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }

    public static void receive(MinecraftServer server, ServerPlayerEntity player, byte type) {


    }
}
