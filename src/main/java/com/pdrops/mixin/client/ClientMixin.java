package com.pdrops.mixin.client;

import com.pdrops.sounds.DropsSounds;
import com.pdrops.upgrades.IUpgradeGetter;
import com.pdrops.upgrades.PlayerUpgradeContainer;
import com.pdrops.util.Easings;
import com.pdrops.util.IClientData;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.ClientBossBar;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.gui.hud.PlayerListHud;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.sound.SoundManager;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.random.Random;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;
import java.util.UUID;

@Mixin(MinecraftClient.class)
public abstract class ClientMixin implements IUpgradeGetter, IClientData {

    @Shadow
    @Final
    public InGameHud inGameHud;

    @Shadow
    public abstract SoundManager getSoundManager();

    @Shadow
    @Nullable
    public ClientPlayerEntity player;
    @Shadow
    @Nullable
    public ClientWorld world;
    @Unique
    public int guiLevelTicks, prevGuiLevelTicks, lastGuiY, guiY = 4;

    @Unique
    public PlayerUpgradeContainer upgradeContainer = new PlayerUpgradeContainer();
    @Unique
    public boolean lastBossbar, lastHudVisible;

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/world/ClientWorld;tickEntities()V"), method = "tick")
    private void tickUpgrades(CallbackInfo info) {
        if (this.upgradeContainer.tickClient(this.world)) {
            float v =  (this.upgradeContainer.getUpgradeDequeueIterations() - 1) / 10f;
            float pitch = 1f + MathHelper.clamp(v, 0f, 2f);

            this.getSoundManager().play(new PositionedSoundInstance(DropsSounds.GET_UPGRADE, SoundCategory.UI, 0.25f, pitch, Random.create(0), player.getX(), player.getY(), player.getZ()));
        }
        if (!this.upgradeContainer.getUpgrades().isEmpty()) {
            for (var up : this.upgradeContainer.getUpgrades()) {
                if (up.shouldPlayKillSound()) {
                    this.getSoundManager().play(new PositionedSoundInstance(DropsSounds.SFX_KILLENEMY, SoundCategory.UI, 0.15f, 1f, Random.create(0), player.getX(), player.getY(), player.getZ()));
                    up.setPlayKillSound(false);
                }
            }
        }

        this.prevGuiLevelTicks = guiLevelTicks;
        if (guiLevelTicks < 20) {
            guiLevelTicks++;
        }
        boolean empty = ((IBossBarHudGetter) this.inGameHud.getBossBarHud()).getBossBars().isEmpty();
        if (lastBossbar != empty) {
            guiLevelTicks = 0;
            this.lastGuiY = guiY;
            this.guiY = empty ? 4 : 35;
            lastBossbar = empty;
        }
        boolean tabList = ((IPlayerListHudGetter) this.inGameHud.getPlayerListHud()).isVisible();
        if (lastHudVisible != tabList) {
            guiLevelTicks = 0;
            this.lastGuiY = guiY;

            this.guiY = tabList ? -30 : guiY;
            lastBossbar = empty;
        }
    }


    @Override
    public PlayerUpgradeContainer getContainer() {
        return upgradeContainer;
    }

    @Override
    public float getGuiLevel(float delta) {
        Map<UUID, ClientBossBar> bossBars = ((IBossBarHudGetter) this.inGameHud.getBossBarHud()).getBossBars();
        if (((IPlayerListHudGetter) this.inGameHud.getPlayerListHud()).isVisible()) {
            return -600;
        }
        if (!bossBars.isEmpty()) {
            return 4 + (bossBars.size() * 18);
        }
        return 4;
    }

    private int getLastGuiY() {
        return lastGuiY;
    }

    public int getGuiY() {
        return guiY;
    }
}
