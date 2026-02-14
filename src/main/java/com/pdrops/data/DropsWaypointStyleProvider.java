package com.pdrops.data;

import com.pdrops.PromiseDrops;
import com.pdrops.waypoint.CustomWaypointStyles;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.minecraft.client.resource.waypoint.WaypointStyleAsset;
import net.minecraft.data.DataOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.DataWriter;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.Identifier;
import net.minecraft.world.waypoint.WaypointStyle;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;

public class DropsWaypointStyleProvider implements DataProvider {

    private final DataOutput.PathResolver pathResolver;

    public DropsWaypointStyleProvider(FabricDataOutput output) {
        this.pathResolver = output.getResolver(DataOutput.OutputType.RESOURCE_PACK, "waypoint_style");
    }

    private static void bootstrap(BiConsumer<RegistryKey<WaypointStyle>, WaypointStyleAsset> waypointStyleBiConsumer) {
        waypointStyleBiConsumer.accept(
                CustomWaypointStyles.DEATH_FACE,
                new WaypointStyleAsset(
                        6,
                        12,
                        List.of(
                                PromiseDrops.of("death_face_0"),
                                PromiseDrops.of("death_face_1"),
                                PromiseDrops.of("death_face_3"),
                                PromiseDrops.of("death_face_3"),
                                PromiseDrops.of("death_face_4")
                        )
                )
        );
    }

    @Override
    public CompletableFuture<?> run(DataWriter writer) {
        Map<RegistryKey<WaypointStyle>, WaypointStyleAsset> map = new HashMap();
        bootstrap((key, asset) -> {
            if (map.putIfAbsent(key, asset) != null) {
                throw new IllegalStateException("Tried to register waypoint style twice for id: " + key);
            }
        });
        return DataProvider.writeAllToPath(writer, WaypointStyleAsset.CODEC, this.pathResolver::resolveJson, map);
    }

    @Override
    public String getName() {
        return "Drops Waypoint Style Definitions";
    }

}
