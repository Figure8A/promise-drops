package com.pdrops.waypoint;

import com.pdrops.PromiseDrops;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.Identifier;
import net.minecraft.world.waypoint.WaypointStyle;

public class CustomWaypointStyles {

    private static final RegistryKey<? extends Registry<WaypointStyle>> REGISTRY = RegistryKey.ofRegistry(Identifier.ofVanilla("waypoint_style_asset"));
    public static RegistryKey<WaypointStyle> DEATH_FACE  = of("death_face");
    public static RegistryKey<WaypointStyle> FIGURE = of("figure");


    public static RegistryKey<WaypointStyle> of(String id) {
        return RegistryKey.of(REGISTRY, PromiseDrops.of(id));
    }

    public static void init() {

    }
}
