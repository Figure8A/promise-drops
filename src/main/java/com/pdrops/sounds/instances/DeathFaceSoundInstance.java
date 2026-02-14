package com.pdrops.sounds.instances;


import com.pdrops.PromiseDrops;
import com.pdrops.entity.custom.DeathFaceEntity;
import com.pdrops.sounds.DropsSounds;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.sound.MovingSoundInstance;
import net.minecraft.client.sound.SoundInstance;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.math.MathHelper;

import java.util.UUID;

public class DeathFaceSoundInstance extends MovingSoundInstance {

    private final DeathFaceEntity deathFaceEntity;
    private float distance = 0.0F;

    public DeathFaceSoundInstance(DeathFaceEntity deathFaceEntity) {
        super(deathFaceEntity.shouldJam() ? DropsSounds.MU_DEATHJAM : DropsSounds.MU_DEATHMODE, SoundCategory.HOSTILE, SoundInstance.createRandom());
        this.deathFaceEntity = deathFaceEntity;
        this.repeat = true;
        this.repeatDelay = 0;
        this.volume = 0.0F;
        this.x = (float)deathFaceEntity.getX();
        this.y = (float)deathFaceEntity.getY();
        this.z = (float)deathFaceEntity.getZ();
    }

    @Override
    public boolean canPlay() {
        return !this.deathFaceEntity.isSilent();
    }


    @Override
    public boolean shouldAlwaysPlay() {
        return true;
    }


    @Override
    public void tick() {
        if (this.deathFaceEntity.isRemoved()) {
            this.setDone();
        } else {
            this.x = (float) this.deathFaceEntity.getX();
            this.y = (float) this.deathFaceEntity.getEyeY();
            this.z = (float) this.deathFaceEntity.getZ();
            ClientPlayerEntity clientPlayer = MinecraftClient.getInstance().player;
            if (clientPlayer != null) {
                if (deathFaceEntity.getTargetUUID().isPresent()) {
                    boolean match = deathFaceEntity.getTargetUUID().get().uuidEquals(clientPlayer);
                    float f = this.deathFaceEntity.distanceTo(clientPlayer);
                    if (match) {
                        if (f < 25f) {
                            float delta = (25f - f) / 25f;
                            this.volume = MathHelper.clampedLerp(delta, 0.05f, 1.5f);
                        }
                    } else {
                        this.volume = 1f;
                        this.x = clientPlayer.getX();
                        this.y = clientPlayer.getY();
                        this.z = clientPlayer.getZ();
                    }
                }
            }

        }
    }
}
