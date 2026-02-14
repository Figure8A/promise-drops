package com.pdrops.mixin;

import com.pdrops.PromiseDrops;
import com.pdrops.upgrades.registry.PlayerUpgrades;
import net.minecraft.entity.*;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ExperienceOrbEntity.class)
public abstract class ExperienceOrbEntityMixin  extends Entity {


    public ExperienceOrbEntityMixin(EntityType<?> type, World world) {
        super(type, world);
    }


    @Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/ExperienceOrbEntity;getValue()I"), method = "onPlayerCollision")
    private int addExperience(ExperienceOrbEntity instance) {
        boolean hasEx = PromiseDrops.getUpgradeContainer(this.getEntityWorld()).hasUpgrade(PlayerUpgrades.EXPERIENCE_MULTIPLIER);
        if (hasEx) {
            double upgradeBonusCeil = 1 + PromiseDrops.getUpgradeContainer(this.getEntityWorld()).getUpgrade(PlayerUpgrades.EXPERIENCE_MULTIPLIER).getUpgradeBonus();
            int i = MathHelper.ceil(instance.getValue() * upgradeBonusCeil);
            //PromiseDrops.LOGGER.info("Before {}, After, {}, bonus {}", instance.getValue(), i, upgradeBonusCeil);
            return i;
        }
        return instance.getValue();
    }
}