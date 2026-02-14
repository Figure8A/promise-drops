package com.pdrops.mixin.client;

import com.pdrops.upgrades.IUpgradeGetter;
import com.pdrops.upgrades.PlayerUpgradeContainer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientWorld.class)
public class ClientWorldMixin implements IUpgradeGetter {


    @Shadow @Final private MinecraftClient client;

    @Override
    public PlayerUpgradeContainer getContainer() {
        return ((IUpgradeGetter) this.client).getContainer();
    }
}
