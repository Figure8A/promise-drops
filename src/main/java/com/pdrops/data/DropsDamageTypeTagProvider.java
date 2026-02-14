package com.pdrops.data;

import com.pdrops.damage.DropsDamageTypes;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;
import net.minecraft.entity.damage.DamageType;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.tag.DamageTypeTags;

import java.util.concurrent.CompletableFuture;

public class DropsDamageTypeTagProvider extends FabricTagProvider<DamageType> {


    public DropsDamageTypeTagProvider(FabricDataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> registriesFuture) {
        super(output, RegistryKeys.DAMAGE_TYPE, registriesFuture);
    }

    @Override
    public String getName() {
        return "Drops Damage Gen";
    }

    @Override
    protected void configure(RegistryWrapper.WrapperLookup wrapperLookup) {
        this.builder(DamageTypeTags.BYPASSES_ARMOR).add(DropsDamageTypes.DEATH_FACE);
        this.builder(DamageTypeTags.BYPASSES_INVULNERABILITY).add(DropsDamageTypes.DEATH_FACE);
        this.builder(DamageTypeTags.BYPASSES_RESISTANCE).add(DropsDamageTypes.DEATH_FACE);
        this.builder(DamageTypeTags.NO_KNOCKBACK).add(DropsDamageTypes.DEATH_FACE);
    }
}
