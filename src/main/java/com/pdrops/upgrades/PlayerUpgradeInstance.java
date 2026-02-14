package com.pdrops.upgrades;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.pdrops.PromiseDrops;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.math.MathHelper;

public class PlayerUpgradeInstance {
    public static final Codec<PlayerUpgradeInstance> CODEC = RecordCodecBuilder.create(instance -> instance.group(PlayerUpgrade.ENTRY_CODEC.fieldOf("upgrade").forGetter(PlayerUpgradeInstance::getUpgrade), Codec.INT.fieldOf("level").forGetter(PlayerUpgradeInstance::getLevel)).apply(instance, PlayerUpgradeInstance::new));
    public static final Codec<PlayerUpgradeInstance> CODEC_WITH_TIME = RecordCodecBuilder.create(instance -> instance.group(PlayerUpgrade.ENTRY_CODEC.fieldOf("upgrade").forGetter(PlayerUpgradeInstance::getUpgrade), Codec.INT.fieldOf("level").forGetter(PlayerUpgradeInstance::getLevel), Codec.INT.fieldOf("ambientTicks").forGetter(PlayerUpgradeInstance::getAmbientTicks)).apply(instance, PlayerUpgradeInstance::new));
    public static final PacketCodec<RegistryByteBuf, PlayerUpgradeInstance> PACKET_CODEC = PacketCodec.tuple(PlayerUpgrade.PACKET_CODEC, PlayerUpgradeInstance::getUpgrade, PacketCodecs.INTEGER, PlayerUpgradeInstance::getLevel, PlayerUpgradeInstance::new);
    public static final PacketCodec<RegistryByteBuf, PlayerUpgradeInstance> PACKET_CODEC_WITH_TIME = PacketCodec.tuple(PlayerUpgrade.PACKET_CODEC, PlayerUpgradeInstance::getUpgrade, PacketCodecs.INTEGER, PlayerUpgradeInstance::getLevel, PacketCodecs.INTEGER, PlayerUpgradeInstance::getAmbientTicks, PlayerUpgradeInstance::new);
    public RegistryEntry<PlayerUpgrade> upgrade;
    public int level;
    public int ambientTicks;
    public int removeTicks = 0, lastRemoveTicks = 0, maxRemoveTicks = 0, xOffset = 0;
    public boolean playKillSound = false;

    public PlayerUpgradeInstance(RegistryEntry<PlayerUpgrade> upgrade, int level) {
        this.upgrade = upgrade;
        this.level = level;
    }

    public PlayerUpgradeInstance(RegistryEntry<PlayerUpgrade> upgrade, int level, int ambientTicks) {
        this(upgrade, level);
        this.ambientTicks = ambientTicks;
    }

    public PlayerUpgradeInstance(RegistryEntry.Reference<PlayerUpgrade> upgrade) {
        this(upgrade, 1);
    }

    public boolean mergeUpgrade(PlayerUpgradeInstance playerUpgradeInstance) {
        int level = this.getLevel();
        if (this.getUpgrade().matches(playerUpgradeInstance.getUpgrade())) {
            int getClamped =  Math.max(playerUpgradeInstance.getLevel() + this.getLevel(), this.getMaxLevel());
            if (this.getMaxLevel() >= getClamped) {
                this.addLevel(playerUpgradeInstance.getLevel());
                PromiseDrops.LOGGER.info("Added level {}", this.getLevel());
                return true;
            }
        }
        return false;
    }

    public void addLevel(int level) {
        this.level += level;
    }

    public void tick() {
        ambientTicks++;
        this.lastRemoveTicks = this.removeTicks;
        if (shouldTickRemove()) {
            if ((this.getMaxRemoveTicks() / 4) == this.getRemoveTicks()) {
                this.setPlayKillSound(true);
            }

            this.removeTicks++;
        }
    }



    public RegistryEntry<PlayerUpgrade> getUpgrade() {
        return upgrade;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public double modifyAttribute(PlayerEntity player, RegistryEntry<EntityAttribute> attribute, double base) {
        PlayerUpgrade upgrade1 = this.getUpgrade().value();
        if (upgrade1.modifiesAttribute() && upgrade1.getAttribute().get().matches(attribute)) {
            base += this.getUpgradeBonus();
        }
        if (upgrade1.hasExtraAttribute() && upgrade1.getExtraAttribute().get().matches(attribute)) {
            base += this.getExtraUpgradeBonus();
        }
        return base;
    }

    private double getExtraUpgradeBonus() {
        return this.getUpgrade().value().getExtraBase() * this.getLevel();
    }
    public int getUpgradeBonusCeil() {
        return MathHelper.ceil(this.getUpgrade().value().getBase()) * this.getLevel();
    }

    public double getUpgradeBonus() {
        return this.getUpgrade().value().getBase() * this.getLevel();
    }

    public int getMaxLevel() {
        return this.getUpgrade().value().getMaxLevel();
    }

    @Override
    public boolean equals(Object o) {
        return this == o || o instanceof PlayerUpgradeInstance upgradeInstance && upgradeInstance.getUpgrade().equals(this.getUpgrade()) && this.getLevel() == upgradeInstance.getLevel();
    }

    @Override
    public String toString() {
        return new StringBuilder("value=[").append(this.getUpgrade().getKey()).append("]").append(", level=[").append(this.getLevel()).append("]").toString();
    }

    public boolean isAtMaxLevel() {
        return this.getLevel() >= this.getMaxLevel();
    }
    public boolean isAtMaxLevel(int levelOverride) {
        return levelOverride >= this.getMaxLevel();
    }

    public int getAmbientTicks() {
        return ambientTicks;
    }

    public void setAmbientTicks(int ambientTicks) {
        this.ambientTicks = ambientTicks;
    }

    public int getLastRemoveTicks() {
        return lastRemoveTicks;
    }

    public int getMaxRemoveTicks() {
        return maxRemoveTicks;
    }

    public int getRemoveTicks() {
        return removeTicks;
    }

    public void setMaxRemoveTicks(int maxRemoveTicks) {
        this.maxRemoveTicks = maxRemoveTicks;
    }

    public float getLerpRemoveTicks(float delta) {
        return getDeltaRemoveTicks(delta) / (float) (getMaxRemoveTicks());
    }

    public float getDeltaRemoveTicks(float delta) {
        float lerp = MathHelper.lerp(delta, (float) this.getLastRemoveTicks(), (float) this.getRemoveTicks());
        return lerp;
    }
    public boolean shouldTickRemove() {
        return this.getRemoveTicks() != this.getMaxRemoveTicks();
    }

    public boolean shouldRemove() {
        return this.getMaxRemoveTicks() != 0 && !shouldTickRemove();
    }

    public void setXOffset(int xOffset) {
        this.xOffset = xOffset;
    }

    public int getXOffset() {
        return xOffset;
    }

    public boolean shouldPlayKillSound() {
        return playKillSound;
    }

    public void setPlayKillSound(boolean playKillSound) {
        this.playKillSound = playKillSound;
    }

    public PlayerUpgradeInstance maxLevel() {
        this.setLevel(this.getMaxLevel());
        return this;
    }

    public String getUpgradeTranslationName() {
        return this.getUpgrade().value().getTranslationKey();
    }
}
