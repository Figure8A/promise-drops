package com.pdrops.entity.client.model.layers;


import com.pdrops.PromiseDrops;
import net.minecraft.client.render.entity.model.EntityModelLayer;

public class PromiseDropsModelLayers {

    public static final EntityModelLayer DEATH_FACE = registerMain("death_face/death_face");


    private static EntityModelLayer registerMain(String id) {
        return register(id, "main");
    }

    private static EntityModelLayer register(String id, String layer) {
        return create(id, layer);

    }

    private static EntityModelLayer create(String id, String layer) {
        return new EntityModelLayer(PromiseDrops.of(id), layer);
    }

}
