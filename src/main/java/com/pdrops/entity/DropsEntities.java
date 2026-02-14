package com.pdrops.entity;

import com.pdrops.PromiseDrops;
import com.pdrops.entity.custom.DeathFaceEntity;
import com.pdrops.entity.custom.UpgradeEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;

public class DropsEntities {



    public static final EntityType<UpgradeEntity> UPGRADE_ENTITY = register(
            "upgrade_entity",
            EntityType.Builder.<UpgradeEntity>create(UpgradeEntity::new, SpawnGroup.MISC)
                    .dimensions(0.5F, 0.5F)
                    .maxTrackingRange(16)
                    .trackingTickInterval(21)
                    .alwaysUpdateVelocity(true)
                    .makeFireImmune()
    );

    public static final EntityType<DeathFaceEntity> DEATH_FACE = register("death_face",
            EntityType.Builder.create(DeathFaceEntity::create, SpawnGroup.MISC)
                    .dimensions(2f, 3f)
                    .eyeHeight(0.25f)
                    .spawnableFarFromPlayer()
                    .alwaysUpdateVelocity(true));



    private static <T extends Entity> EntityType<T> register(RegistryKey<EntityType<?>> key, EntityType.Builder<T> type) {
        return Registry.register(Registries.ENTITY_TYPE, key, type.build(key));
    }

    private static RegistryKey<EntityType<?>> keyOf(String id) {
        return RegistryKey.of(RegistryKeys.ENTITY_TYPE, PromiseDrops.of(id));
    }

    private static <T extends Entity> EntityType<T> register(String id, EntityType.Builder<T> type) {
        return register(keyOf(id), type);
    }

    public static void registerDropsEntities() {
    }
}
