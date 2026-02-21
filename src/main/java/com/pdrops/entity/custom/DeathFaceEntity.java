package com.pdrops.entity.custom;


import com.pdrops.PromiseDrops;
import com.pdrops.damage.DropsDamageTypes;
import com.pdrops.entity.DropsEntities;
import com.pdrops.entity.client.entity.ClientDeathFaceEntity;
import com.pdrops.entity.inter.IDeathFaceGetter;
import com.pdrops.sounds.DropsSounds;
import com.pdrops.sounds.instances.DeathFaceSoundInstance;
import com.pdrops.waypoint.CustomWaypointStyles;
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
import java.util.UUID;

import static net.minecraft.server.network.ServerPlayerEntity.addEnderPearlTicket;

public class DeathFaceEntity extends LivingEntity {
    private static final TrackedData<Optional<LazyEntityReference<LivingEntity>>> TARGET = DataTracker.registerData(DeathFaceEntity.class, TrackedDataHandlerRegistry.LAZY_ENTITY_REFERENCE);
    private static final TrackedData<Boolean> CLOSE_TO_PLAYER = DataTracker.registerData(DeathFaceEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
    private static final TrackedData<Boolean> SPAWNED_FROM_JAM = DataTracker.registerData(DeathFaceEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
    public final AnimationState idleAnimationState = new AnimationState();
    public final AnimationState angerAnimationState = new AnimationState();
    private long chunkTicketExpiryTicks = 0L;

    public DeathFaceEntity(EntityType<? extends LivingEntity> type, World world) {
        super(type, world);
        initDeath();
    }
    public DeathFaceEntity(World world, Vec3d pos, boolean fromJam) {
        super(DropsEntities.DEATH_FACE, world);
        this.refreshPositionAndAngles(pos, 0f,0f);
        initDeath();
        this.setJam(fromJam);
    }

    private void initDeath() {
        this.tickTarget();
        this.noClip = true;
        this.getWaypointConfig().style = CustomWaypointStyles.DEATH_FACE;
        this.getWaypointConfig().color = Optional.of(this.getTeamColorValue());
        PromiseDrops.LOGGER.info("Initialized Death Face, found a player? {}", this.getTargetUUID().isPresent());
    }

    @Override
    public void tick() {
        this.noClip = true;
        super.tick();
        this.noClip = false;
        this.setNoGravity(true);
        if (this.age == 2) {
            this.getEntityWorld().playSoundFromEntity(null, this, DropsSounds.SFX_DEATHFACE_LAUGH,  SoundCategory.PLAYERS,1f,1f);
        }

        if (!this.getEntityWorld().isClient()) {
            if (this.getTarget() == null || !this.getTarget().isInteractable()) {
                this.setTargetUUID(Optional.empty());
            } else {
                if (this.age % 20 == 0) {
                    this.tickTarget();
                }
            }
            if (this.getHealth() < this.getMaxHealth()) {
                this.setHealth(this.getHealth() + 10);
            }
        }
        angerAnimationState.setRunning(this.isCloseToTarget(), this.age);
        idleAnimationState.setRunning(!this.isCloseToTarget(), this.age);
        if (this.shouldRemove()) {
            this.discard();
        }


        int i = ChunkSectionPos.getSectionCoordFloored(this.getEntityPos().getX());
        int j = ChunkSectionPos.getSectionCoordFloored(this.getEntityPos().getZ());
        if (this.isAlive() && !this.getEntityWorld().isClient()) {
            BlockPos blockPos = BlockPos.ofFloored(this.getEntityPos());
            if ((--this.chunkTicketExpiryTicks <= 0L || i != ChunkSectionPos.getSectionCoord(blockPos.getX()) || j != ChunkSectionPos.getSectionCoord(blockPos.getZ()))) {
                this.chunkTicketExpiryTicks = this.handleDeathFaceLoads();
            }
        }
    }

    @Override
    public boolean canUsePortals(boolean allowVehicles) {
        return false;
    }

    private void tickChasePlayer() {
        if (this.getTarget() != null) {
            ((IDeathFaceGetter) this.getTarget()).setDeathFaceEntity(this);
            Vec3d vec3d = new Vec3d(
                    this.getTarget().getX() - this.getX(), this.getTarget().getY() + this.getTarget().getStandingEyeHeight() / 2.0 - this.getY(), this.getTarget().getZ() - this.getZ()
            );

            double d = vec3d.lengthSquared();
            double e = 5.0 - Math.sqrt(d) / 1.2f;
            //PromiseDrops.LOGGER.info("d {}", this.getTarget().distanceTo(this));
            float value = 0.8849f;
            float v = this.getTarget().distanceTo(this);
            if (v < 8f) {
                e = 2.0 - Math.sqrt(d) / 5.0;
                value = 0.939f;
            }

            this.setVelocity(this.getVelocity().multiply(value)
                    .add(vec3d.normalize().multiply(e * e * 0.03)));

            float change = (float) Math.max(8d, this.getVelocity().lengthSquared());
            this.lookAtEntity(this.getTarget(), change, change);
            if (this.age < 60) {
                this.setVelocity(this.getVelocity().multiply(0.77));
            } else {
                if (v > 1000) {
                    this.discard();
                }
            }
            this.setCloseToTarget(v < 7.5f);
        } else {
            this.setCloseToTarget(false);
        }
    }

    @Override
    protected void knockback(LivingEntity target) {
        super.knockback(target);
    }

    @Override
    public void takeKnockback(double strength, double x, double z) {
        super.takeKnockback(strength, x, z);
    }

    @Override
    public void tickMovement() {
        tickChasePlayer();
        this.move(MovementType.SELF, this.getVelocity());
        this.tickBlockCollision();
    }

    @Override
    public Arm getMainArm() {
        return Arm.RIGHT;
    }

    private long handleDeathFaceLoads() {
        if (this.getEntityWorld() instanceof ServerWorld serverWorld) {
            ChunkPos chunkPos = this.getChunkPos();
            serverWorld.resetIdleTimeout();
            return addEnderPearlTicket(serverWorld, chunkPos) - 1L;
        } else {
            return 0L;
        }
    }

    public boolean shouldRemove() {
        if (this.getEntityWorld() instanceof ServerWorld serverWorld) {
            return PromiseDrops.allPlayersDead(serverWorld.getServer().getPlayerManager().getPlayerList());
        }
        return false;
    }
    public @Nullable PlayerEntity getTarget() {
       return this.getTargetUUID().isPresent() ? this.getTargetUUID().get().resolve(this.getEntityWorld()::getEntityAnyDimension, LivingEntity.class) instanceof PlayerEntity player ? player : null : null;
    }
    public void lookAtEntity(Entity targetEntity, float maxYawChange, float maxPitchChange) {
        double d = targetEntity.getX() - this.getX();
        double e = targetEntity.getZ() - this.getZ();
        double f;
        if (targetEntity instanceof LivingEntity livingEntity) {
            f = livingEntity.getEyeY() - this.getEyeY();
        } else {
            f = (targetEntity.getBoundingBox().minY + targetEntity.getBoundingBox().maxY) / 2.0 - this.getEyeY();
        }

        double g = Math.sqrt(d * d + e * e);
        float h = (float)(MathHelper.atan2(e, d) * 180.0F / (float)Math.PI) - 90.0F;
        float i = (float)(-(MathHelper.atan2(f, g) * 180.0F / (float)Math.PI));
        this.setPitch(this.changeAngle(this.getPitch(), i, maxPitchChange));
        this.setYaw(this.changeAngle(this.getYaw(), h, maxYawChange));
        this.setHeadYaw(this.getYaw());
        this.lastPitch = this.getPitch();
        this.lastYaw = this.getYaw();
    }

    private float changeAngle(float from, float to, float max) {
        float f = MathHelper.wrapDegrees(to - from);
        if (f > max) {
            f = max;
        }
        if (f < -max) {
            f = -max;
        }
        return from + f;
    }


    private void tickTarget() {
        PlayerEntity player = this.getEntityWorld().getClosestPlayer(this, 5000);
        if (player != null) {
            this.setTargetUUID(Optional.of(LazyEntityReference.of(player)));
        }
    }

    @Override
    public void onPlayerCollision(PlayerEntity player) {
        if (!this.getEntityWorld().isClient() && !player.isCreative()) {
            double x = 0.25;
            if (player.getBoundingBox().intersects(this.getBoundingBox().shrink(x, x, x)) && this.age > 150) {
                ServerWorld entityWorld = (ServerWorld) this.getEntityWorld();
                player.damage(entityWorld, entityWorld.getDamageSources().create(DropsDamageTypes.DEATH_FACE), Float.MAX_VALUE);
                this.kill(entityWorld);
                this.setTargetUUID(Optional.empty());
            }
        }
        super.onPlayerCollision(player);
    }



    @Override
    protected void initDataTracker(DataTracker.Builder builder) {
        builder.add(TARGET, Optional.empty());
        builder.add(CLOSE_TO_PLAYER, false);
        builder.add(SPAWNED_FROM_JAM, false);
        super.initDataTracker(builder);
    }

    @Override
    public boolean damage(ServerWorld world, DamageSource source, float amount) {
        if (source.isSourceCreativePlayer()) {
            this.discard();
            return true;
        }
        return super.damage(world, source, amount);
    }

    @Override
    public ItemStack getEquippedStack(EquipmentSlot slot) {
        return ItemStack.EMPTY;
    }

    @Override
    public void equipStack(EquipmentSlot slot, ItemStack stack) {
    }


    public Optional<LazyEntityReference<LivingEntity>> getTargetUUID() {
        return this.dataTracker.get(TARGET);
    }

    public void setTargetUUID(Optional<LazyEntityReference<LivingEntity>> targetUUID) {
        this.dataTracker.set(TARGET, targetUUID);
    }

    @Override
    protected void readCustomData(ReadView view) {
        super.readCustomData(view);
        LazyEntityReference<LivingEntity> lazyEntityReference = LazyEntityReference.fromDataOrPlayerName(view, "TargetDeath", this.getEntityWorld());
        if (lazyEntityReference != null) {
            this.dataTracker.set(TARGET, Optional.of(lazyEntityReference));
        } else {
            this.dataTracker.set(TARGET, Optional.empty());
        }
    }

    @Override
    protected void writeCustomData(WriteView view) {
        super.writeCustomData(view);
        if (this.getTargetUUID().isPresent()) {
            this.getTargetUUID().get().writeData(view, "TargetDeath");
        }
    }

    @Override
    public boolean isGlowing() {
        return true;
    }
    @Override
    public int getTeamColorValue() {
        return 0x9850f8;
    }
    public boolean isCloseToTarget() {
        return this.dataTracker.get(CLOSE_TO_PLAYER);
    }
    public void setCloseToTarget(boolean close) {
        this.dataTracker.set(CLOSE_TO_PLAYER, close);
    }
    public boolean shouldJam() {
        return this.dataTracker.get(SPAWNED_FROM_JAM);
    }
    public void setJam(boolean jam) {
        this.dataTracker.set(SPAWNED_FROM_JAM, jam);
    }
    public static DefaultAttributeContainer.Builder createDeathFaceAttributes() {
        return LivingEntity.createLivingAttributes()
                .add(EntityAttributes.MOVEMENT_SPEED, 0.009d)
                .add(EntityAttributes.WAYPOINT_TRANSMIT_RANGE, 6.0E7)
                .add(EntityAttributes.MAX_HEALTH, 999999999F)
                .add(EntityAttributes.KNOCKBACK_RESISTANCE, 0.6F);
    }
    public static DeathFaceEntity create(EntityType<DeathFaceEntity> entityType, World world) {
        if (world.isClient()) {
            return new ClientDeathFaceEntity(entityType, world);
        } else {
            return new DeathFaceEntity(entityType, world);
        }
    }
}
