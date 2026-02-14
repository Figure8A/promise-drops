package com.pdrops.mixin;

import com.pdrops.upgrades.IUpgradeGetter;
import com.pdrops.upgrades.PlayerUpgradeContainer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(ServerWorld.class)
public abstract class ServerWorldMixin implements IUpgradeGetter {


    @Shadow public abstract MinecraftServer getServer();

    @Override
    public PlayerUpgradeContainer getContainer() {
        return ((IUpgradeGetter) this.getServer()).getContainer();
    }
}