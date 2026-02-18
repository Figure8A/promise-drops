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
    public static RegistryKey<WaypointStyle> NJA = of("nja");
    public static RegistryKey<WaypointStyle> ODSTERS = of("odsters");
    public static RegistryKey<WaypointStyle> WEST = of("west");

    public static RegistryKey<WaypointStyle> of(String id) {
        return RegistryKey.of(REGISTRY, PromiseDrops.of(id));
    }

    public static void init() {
        PromiseDrops.UUID_WAYPOINT_MAP.put("e0d11d90-6ebc-4125-86d2-80511459c48e", FIGURE);
        PromiseDrops.UUID_WAYPOINT_MAP.put("02e52e2d-6c47-466f-98b8-f4e0afb74801", NJA);
        PromiseDrops.UUID_WAYPOINT_MAP.put("17c1e6ee-e2a4-4064-a59b-08cf5d9e96b2", WEST);
        PromiseDrops.UUID_WAYPOINT_MAP.put("b010f5a3-7934-438c-9ae2-b1dda6092d0f", ODSTERS);
    }
}
