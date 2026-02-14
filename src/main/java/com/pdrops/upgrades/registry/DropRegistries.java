package com.pdrops.upgrades.registry;

import com.pdrops.upgrades.PlayerUpgrade;
import net.fabricmc.fabric.api.event.registry.DynamicRegistries;
import net.fabricmc.fabric.api.event.registry.FabricRegistryBuilder;
import net.fabricmc.fabric.api.event.registry.RegistryAttribute;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.Identifier;

public class DropRegistries {

    public static final RegistryKey<Registry<PlayerUpgrade>> PLAYER_UPGRADE_KEY = of("player_upgrade");

    public static final Registry<PlayerUpgrade> PLAYER_UPGRADE = FabricRegistryBuilder.createSimple(PLAYER_UPGRADE_KEY).attribute(RegistryAttribute.SYNCED).buildAndRegister();

    private static <T> RegistryKey<Registry<T>> of(String id) {
        return RegistryKey.ofRegistry(Identifier.of(id));
    }

    public static void registerRegistry(){
        //DynamicRegistries.registerSynced(PLAYER_UPGRADE_KEY, PlayerUpgrade.CODEC);
    }

}
