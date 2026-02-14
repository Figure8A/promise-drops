package com.pdrops.particles.registry;

import com.pdrops.PromiseDrops;
import net.fabricmc.fabric.api.particle.v1.FabricParticleTypes;
import net.minecraft.particle.SimpleParticleType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class DropsParticles {



    public static final SimpleParticleType DROPPED_UPGRADE  =
            registerParticle("dropped_upgrade", FabricParticleTypes.simple(true));

    public static final SimpleParticleType COLLECTED_UPGRADE  =
            registerParticle("collected_upgrade", FabricParticleTypes.simple(true));

    private static SimpleParticleType registerParticle(String name, SimpleParticleType particleType) {
        return Registry.register(Registries.PARTICLE_TYPE, PromiseDrops.of(name), particleType);
    }

    public static void init() {

    }

}
