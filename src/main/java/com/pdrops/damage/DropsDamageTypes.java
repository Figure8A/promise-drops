package com.pdrops.damage;

import com.pdrops.PromiseDrops;
import net.minecraft.entity.damage.DamageType;
import net.minecraft.registry.Registerable;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;

public class DropsDamageTypes {
    public static final RegistryKey<DamageType> DEATH_FACE = registerDamageType("death_face");



    public static void bootstrap(Registerable<DamageType> damageTypeRegisterable) {
        damageTypeRegisterable.register(DEATH_FACE, new DamageType("death_face", 0.0f));
    }

    private static RegistryKey<DamageType> registerDamageType(String name) {
        return RegistryKey.of(RegistryKeys.DAMAGE_TYPE, PromiseDrops.of(name));
    }
}
