package com.pdrops.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import com.pdrops.data.UpgradeSavesManager;
import com.pdrops.upgrades.IUpgradeGetter;
import com.pdrops.upgrades.PlayerUpgradeContainer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftServer.class)
public abstract class ServerMixin implements IUpgradeGetter {
    @Shadow
    public abstract ServerWorld getOverworld();

    @Unique
    public PlayerUpgradeContainer upgradeContainer = new PlayerUpgradeContainer(MinecraftServer.class.cast(this));



    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/server/MinecraftServer;refreshSpawnPoint()V"), method = "tickWorlds")
    private void tick(CallbackInfo info) {
        upgradeContainer.tickServer(this.getOverworld());
    }

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/server/world/ServerWorld;tick(Ljava/util/function/BooleanSupplier;)V"), method = "tickWorlds")
    private void tickingWorld(CallbackInfo info, @Local ServerWorld serverWorld) {
        if (!serverWorld.equals(this.getOverworld())) {
            UpgradeSavesManager.setDeathFaceTimer(serverWorld, UpgradeSavesManager.getDeathFaceTimer(this.getOverworld()));
        }
    }


    @Override
    public PlayerUpgradeContainer getContainer() {
        return upgradeContainer;
    }
}
