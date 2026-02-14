package com.pdrops.upgrades;

import com.mojang.serialization.Codec;
import com.pdrops.upgrades.registry.DropRegistries;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.registry.entry.RegistryEntry;

import java.util.Optional;

public class PlayerUpgrade {
    public static final Codec<RegistryEntry<PlayerUpgrade>> ENTRY_CODEC = DropRegistries.PLAYER_UPGRADE.getEntryCodec();
    public static final Codec<PlayerUpgrade> CODEC = DropRegistries.PLAYER_UPGRADE.getCodec();
    public static final PacketCodec<RegistryByteBuf, RegistryEntry<PlayerUpgrade>> PACKET_CODEC = PacketCodecs.registryEntry(DropRegistries.PLAYER_UPGRADE_KEY);
    public Optional<RegistryEntry<EntityAttribute>> attribute = Optional.empty();
    public Optional<RegistryEntry<EntityAttribute>> extraAttribute = Optional.empty();
    public String name;
    public int maxLevel;
    private double base = 0;
    private double extraBase = 0;
    private final String translationKey;
    public PlayerUpgrade(int maxLevel, String translationKey) {
        this.maxLevel = maxLevel;
        this.translationKey = translationKey;
    }

    public PlayerUpgrade(int maxLevel, RegistryEntry<EntityAttribute> attribute, String translationKey) {
        this(maxLevel, translationKey);
        this.attribute = Optional.ofNullable(attribute);
    }

    public PlayerUpgrade setBase(double base) {
        this.base = base;
        return this;
    }


    public PlayerUpgrade addExtraAttribute(RegistryEntry<EntityAttribute> attribute) {
        this.extraAttribute = Optional.ofNullable(attribute);
        return this;
    }
    public PlayerUpgrade setExtraBase(double extraBase) {
        this.extraBase = extraBase;
        return this;
    }


    public int getMaxLevel() {
        return maxLevel;
    }

    public boolean modifiesAttribute() {
        return attribute.isPresent();
    }

    public Optional<RegistryEntry<EntityAttribute>> getAttribute() {
        return attribute;
    }

    public double getBase() {
        return this.base;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }
    public boolean hasExtraAttribute() {
        return extraAttribute.isPresent();
    }

    public Optional<RegistryEntry<EntityAttribute>> getExtraAttribute() {
        return extraAttribute;
    }

    public double getExtraBase() {
        return extraBase == 0 ? this.getBase() : extraBase;
    }

    public String getTranslationKey() {
        return translationKey;
    }
}
