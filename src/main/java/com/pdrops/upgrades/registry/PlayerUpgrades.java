package com.pdrops.upgrades.registry;

import com.pdrops.PromiseDrops;
import com.pdrops.upgrades.PlayerUpgrade;
import net.minecraft.entity.ExperienceOrbEntity;
import net.minecraft.entity.attribute.ClampedEntityAttribute;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;

public class PlayerUpgrades {

    public static final RegistryEntry<PlayerUpgrade> ARMOR = register(
            "armor", new PlayerUpgrade(6, EntityAttributes.ARMOR, "upgrade.name.armor").setBase(1.25)

    );
    public static final RegistryEntry<PlayerUpgrade> ATTACK_DAMAGE = register(
            "attack_damage", new PlayerUpgrade(7, EntityAttributes.ATTACK_DAMAGE, "upgrade.name.attack_damage").setBase(1)
    );
    public static final RegistryEntry<PlayerUpgrade> ATTACK_SPEED = register(
            "attack_speed", new PlayerUpgrade(10, EntityAttributes.ATTACK_SPEED, "upgrade.name.attack_speed").setBase(0.85)
    );
    public static final RegistryEntry<PlayerUpgrade> INTERACTION_RANGE = register(
            "interaction_range", new PlayerUpgrade(8, EntityAttributes.BLOCK_INTERACTION_RANGE, "upgrade.name.interaction_range").addExtraAttribute(EntityAttributes.ENTITY_INTERACTION_RANGE).setBase(0.5).setExtraBase(0.515)
    );
    public static final RegistryEntry<PlayerUpgrade> FALL_DAMAGE_MULTIPLIER = register(
            "fall_damage_multiplier", new PlayerUpgrade(8, EntityAttributes.FALL_DAMAGE_MULTIPLIER, "upgrade.name.fall_damage_multiplier").setBase(-0.1)
    );
    public static final RegistryEntry<PlayerUpgrade> KNOCKBACK_RESISTANCE = register(
            "knockback_resistance", new PlayerUpgrade(4, EntityAttributes.KNOCKBACK_RESISTANCE, "upgrade.name.knockback_resistance").setBase(0.075)
    );
    public static final RegistryEntry<PlayerUpgrade> MINING_EFFICIENCY = register(
            "mining_efficiency", new PlayerUpgrade(10, EntityAttributes.MINING_EFFICIENCY, "upgrade.name.mining_efficiency").setBase(0.9)
    );
    public static final RegistryEntry<PlayerUpgrade> MOVEMENT_SPEED = register(
            "movement_speed", new PlayerUpgrade(10, EntityAttributes.MOVEMENT_SPEED, "upgrade.name.movement_speed").addExtraAttribute(EntityAttributes.WATER_MOVEMENT_EFFICIENCY).setBase(0.015).setExtraBase(0.0585)
    );
    public static final RegistryEntry<PlayerUpgrade> OXYGEN_BONUS = register(
            "oxygen_bonus", new PlayerUpgrade(5, EntityAttributes.OXYGEN_BONUS, "upgrade.name.oxygen_bonus").setBase(0.45)
    );
    public static final RegistryEntry<PlayerUpgrade> SNEAKING_SPEED = register(
            "sneaking_speed", new PlayerUpgrade(4, EntityAttributes.SNEAKING_SPEED, "upgrade.name.sneaking_speed").setBase(0.1)
    );
    public static final RegistryEntry<PlayerUpgrade> STEP_HEIGHT = register(
            "step_height", new PlayerUpgrade(3, EntityAttributes.STEP_HEIGHT, "upgrade.name.step_height").setBase(0.5)
    );
    public static final RegistryEntry<PlayerUpgrade> DROP_CHANCE = register(
            "drop_chance", new PlayerUpgrade(5, "upgrade.name.drop_chance").setBase(5)
    );
    public static final RegistryEntry<PlayerUpgrade> MAGNETAR = register(
            "magnetar", new PlayerUpgrade(10, "upgrade.name.magnetar").setBase(0.45)
    );
    public static final RegistryEntry<PlayerUpgrade> EXPERIENCE_MULTIPLIER = register(
            "experience_multiplier", new PlayerUpgrade(10, "upgrade.name.experience_multiplier").setBase(0.5)
    );
    public static final RegistryEntry<PlayerUpgrade> DRAG_REDUCTION = register(
            "drag_reduction", new PlayerUpgrade(10, "upgrade.name.drag_reduction").setBase(0.0075)
    );


    private static RegistryEntry<PlayerUpgrade> register(String id, PlayerUpgrade upgrade) {
        upgrade.setName(id);
        return Registry.registerReference(DropRegistries.PLAYER_UPGRADE, PromiseDrops.of(id), upgrade);
    }

    public static void initialize() {
        DropRegistries.PLAYER_UPGRADE.streamEntries().forEach(upgrade -> {
            //PromiseDrops.LOGGER.info("{}: {}", upgrade.value().getName(), upgrade.value().getMaxLevel());
        });
    }

}
