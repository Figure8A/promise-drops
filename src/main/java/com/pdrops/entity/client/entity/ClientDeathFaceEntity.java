package com.pdrops.entity.client.entity;


import com.pdrops.entity.DropsEntities;
import com.pdrops.entity.custom.DeathFaceEntity;
import com.pdrops.sounds.DropsSounds;
import com.pdrops.sounds.instances.DeathFaceSoundInstance;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.sound.SoundManager;
import net.minecraft.entity.*;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.util.Arm;
import net.minecraft.util.math.*;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

import static net.minecraft.server.network.ServerPlayerEntity.addEnderPearlTicket;

public class ClientDeathFaceEntity extends DeathFaceEntity {
    public DeathFaceSoundInstance deathFaceSoundInstance;
    public ClientDeathFaceEntity(EntityType<? extends LivingEntity> type, World world) {
        super(type, world);
    }


    @Override
    public void tick() {
        super.tick();
        if (this.getEntityWorld().isClient()) {
            if (this.getDeathFaceSoundInstance() == null) {
                this.deathFaceSoundInstance = new DeathFaceSoundInstance(this);
            }
            MinecraftClient client = MinecraftClient.getInstance();
            SoundManager soundManager = client.getSoundManager();
            if (!soundManager.isPlaying(this.getDeathFaceSoundInstance())) {
                soundManager.play(this.getDeathFaceSoundInstance());

            }
            client.getMusicTracker().stop();
        }
    }


    public DeathFaceSoundInstance getDeathFaceSoundInstance() {
        return deathFaceSoundInstance;
    }
}
