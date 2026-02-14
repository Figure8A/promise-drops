package com.pdrops.upgrades;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.function.ValueLists;

import java.util.function.IntFunction;

public enum PlayerUpgradeCollectionType implements StringIdentifiable {
    UPGRADE("upgrade", 0),
    HEART("heart", 1),
    FOOD("food", 2),
    SINGLE_SKILL_POINT("single_skill_point", 3),
    SKILL_POINTS("skill_points", 4)
    ;

    public static final Codec<PlayerUpgradeCollectionType> CODEC = StringIdentifiable.createCodec(PlayerUpgradeCollectionType::values);
    private static final IntFunction<PlayerUpgradeCollectionType> INDEX_MAPPER = ValueLists.createIndexToValueFunction(
            PlayerUpgradeCollectionType::getIndex, values(), UPGRADE
    );
    public static final PacketCodec<ByteBuf, PlayerUpgradeCollectionType> PACKET_CODEC = PacketCodecs.indexed(INDEX_MAPPER, PlayerUpgradeCollectionType::getIndex);
    private final String id;
    private final int index;

    PlayerUpgradeCollectionType(final String id, final int index) {
        this.id = id;
        this.index = index;
    }

    public static PlayerUpgradeCollectionType byIndex(int index) {
        return INDEX_MAPPER.apply(index);
    }

    public boolean isSkill() {
        return this == PlayerUpgradeCollectionType.SKILL_POINTS || this == SINGLE_SKILL_POINT;
    }

    public int getIndex() {
        return this.index;
    }

    @Override
    public String asString() {
        return this.id;
    }


}
