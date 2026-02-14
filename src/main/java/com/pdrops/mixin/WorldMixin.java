package com.pdrops.mixin;
import com.pdrops.upgrades.IUpgradeGetter;
import com.pdrops.upgrades.PlayerUpgradeContainer;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(World.class)
public class WorldMixin implements IUpgradeGetter {

    @Override
    public PlayerUpgradeContainer getContainer() {
        return null;
    }

}