package com.pdrops.entity.custom;


import com.pdrops.PromiseDrops;
import com.pdrops.entity.DropsEntities;
import com.pdrops.networking.registry.DropsNetworking;
import com.pdrops.particles.registry.DropsParticles;
import com.pdrops.upgrades.PlayerUpgrade;
import com.pdrops.upgrades.PlayerUpgradeCollectionType;
import com.pdrops.upgrades.PlayerUpgradeContainer;
import com.pdrops.upgrades.PlayerUpgradeInstance;
import com.pdrops.upgrades.registry.PlayerUpgrades;
import net.minecraft.command.permission.LeveledPermissionPredicate;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.PositionInterpolator;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.function.LazyContainer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.util.math.Easing;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class UpgradeEntity extends Entity {
    private final PositionInterpolator interpolator = new PositionInterpolator(this, 1);
    public PlayerEntity trackedPlayer;
    private static final TrackedData<Integer> HOVERING_TICKS = DataTracker.registerData(UpgradeEntity.class, TrackedDataHandlerRegistry.INTEGER);
    private static final TrackedData<PlayerUpgradeInstance> UPGRADE = DataTracker.registerData(UpgradeEntity.class, PromiseDrops.UPGRADE_INSTANCE);
    private static final TrackedData<PlayerUpgradeCollectionType> UPGRADE_COLLECTION_TYPE = DataTracker.registerData(UpgradeEntity.class, PromiseDrops.UPGRADE_COLLECTION_TYPE);
    public int lastHoveringTicks;

    public UpgradeEntity(EntityType<?> type, World world) {
        super(type, world);
    }
    public UpgradeEntity(World world, double x, double y, double z) {
        super(DropsEntities.UPGRADE_ENTITY, world);
        this.updatePosition(x, y, z);
    }
    public static UpgradeEntity create(World world, double x, double y, double z, int level, RegistryEntry<PlayerUpgrade> upgrade) {
        UpgradeEntity upgradeEntity = new UpgradeEntity(world, x, y, z);
        PlayerUpgradeInstance playerUpgradeInstance = new PlayerUpgradeInstance(upgrade, level);
        upgradeEntity.setUpgrade(playerUpgradeInstance);
        upgradeEntity.tryFindCloserPlayer();
        world.spawnEntity(upgradeEntity);
        return upgradeEntity;
    }

    public static UpgradeEntity create(World world, double x, double y, double z, RegistryEntry<PlayerUpgrade> upgrade) {
        return create(world, x, y, z, 1, upgrade);
    }


    @Override
    protected double getGravity() {
        return 0.009;
    }

    @Override
    public void tick() {
        this.interpolator.tick();
        super.tick();
        boolean bl = !this.getEntityWorld().isSpaceEmpty(this.getBoundingBox());
        if (this.isSubmergedIn(FluidTags.WATER)) {
            applyWaterMovement();
        } else if (!bl) {
            this.applyGravity();
        }

        if (this.getEntityWorld().getFluidState(this.getBlockPos()).isIn(FluidTags.LAVA)) {
            this.setVelocity((this.random.nextFloat() - this.random.nextFloat()) * 0.2F, 0.2F, (this.random.nextFloat() - this.random.nextFloat()) * 0.2F);
        }
        this.lastHoveringTicks = this.getHoveringTicks();
        this.setHoveringTicks(this.getHoveringTicks() + 1);
        if (!this.getEntityWorld().isClient()) {
            if ((this.getHoveringTicks() % 120 == 0 || this.getHoveringTicks() <= 1)) {
                this.tryFindCloserPlayer();
            }

        }
        if (this.trackedPlayer != null && (this.trackedPlayer.distanceTo(this) > 590 || this.trackedPlayer.isDead() || this.trackedPlayer.isRemoved())) {
            this.trackedPlayer = null;
            this.setVelocity(Vec3d.ZERO);
        }

        if (this.trackedPlayer != null && this.isLogicalSideForUpdatingMovement()) {
            Vec3d vec3d = new Vec3d(this.trackedPlayer.getX() - this.getX(), this.trackedPlayer.getY() + this.trackedPlayer.getStandingEyeHeight() / 2.0 - this.getY(), this.trackedPlayer.getZ() - this.getZ());
            double d = vec3d.lengthSquared();
            double e = 1.0 - Math.sqrt(d) / 18.0;
            double v = 0.05;
            float v1 = this.getHoveringTicks() / 55f;
            v += v1 * v1 * v1 * v1;
            Vec3d multiply = vec3d.multiply(e * e * v);
            Vec3d multiplied = multiply.multiply(0.1);
            Vec3d add = this.getVelocity().add(multiplied.multiply(1,0.75,1));
            int g = 25;
            if (this.getHoveringTicks() < g) {
                this.setVelocity(add);
                this.noClip = false;
            } else {
                this.noClip = true;
                float delta = getPlayerLerp(g);
                this.setPosition(this.getEntityPos().lerp(this.trackedPlayer.getEyePos(), MathHelper.clamp(delta, 0f, 1f)));
            }
        }
        if (this.trackedPlayer == null && !this.getEntityWorld().isClient() && bl) {
            boolean bl2 = !this.getEntityWorld().isSpaceEmpty(this.getBoundingBox().offset(this.getVelocity()));
            if (bl2) {
                this.pushOutOfBlocks(this.getX(), (this.getBoundingBox().minY + this.getBoundingBox().maxY) / 2.0, this.getZ());
                this.velocityDirty = true;
            }
        }

        double d = this.getVelocity().y;
        this.move(MovementType.SELF, this.getVelocity());
        this.tickBlockCollision();
        float f = 0.98F;
        if (this.isOnGround()) {
            f = this.getEntityWorld().getBlockState(this.getVelocityAffectingPos()).getBlock().getSlipperiness() * 0.98F;
        }
        this.setVelocity(this.getVelocity().multiply(f));
        if (this.groundCollision && d < -this.getFinalGravity()) {
            this.setVelocity(new Vec3d(this.getVelocity().x, -d * 0.4, this.getVelocity().z));
        }
    }

    private float getPlayerLerp(int g) {
        int n = this.getHoveringTicks() - g;
        return Easing.inCirc(n / 25f);
    }

    private void applyWaterMovement() {
        Vec3d vec3d = this.getVelocity();
        this.setVelocity(vec3d.x * 0.99F, Math.min(vec3d.y + 5.0E-4F, 0.06F), vec3d.z * 0.99F);
    }

    @Override
    public boolean canHit() {
        return true;
    }

    @Override
    public boolean isCollidable(@Nullable Entity entity) {
        return super.isCollidable(entity);
    }

    @Override
    public void onPlayerCollision(PlayerEntity player) {
        if (!player.getEntityWorld().isClient() && (player.getBoundingBox().expand(0.1).intersects(this.getBoundingBox()) || this.getPlayerLerp(25) >= 1f)) {
            PlayerUpgradeContainer upgradeContainer = PromiseDrops.getUpgradeContainer(this.getEntityWorld());
            if (this.getEntityWorld() instanceof ServerWorld serverWorld) {
                if (!upgradeContainer.isUpgradeAtMaxLevel(this.getUpgrade()) && !this.getUpgradeCollectionType().isSkill()) {
                    upgradeContainer.addPlayerUpgrade(this.getUpgrade());
                } else {
                    for (ServerPlayerEntity serverPlayer : serverWorld.getPlayers()) {
                        if (this.getUpgradeCollectionType().isSkill()) {
                            boolean justOne = serverPlayer.equals(player) && this.getUpgradeCollectionType().equals(PlayerUpgradeCollectionType.SINGLE_SKILL_POINT);
                            boolean all = this.getUpgradeCollectionType().equals(PlayerUpgradeCollectionType.SKILL_POINTS);
                            Optional<LazyContainer> func = Optional.of(new LazyContainer(PromiseDrops.of("add_extra_skill")));
                            MinecraftServer minecraftServer = serverWorld.getServer();
                            if (justOne || all) {
                                func.flatMap(function -> function.get(minecraftServer.getCommandFunctionManager())).ifPresent(function -> minecraftServer.getCommandFunctionManager().execute(function, serverPlayer.getCommandSource(serverWorld).withSilent().withPermissions(LeveledPermissionPredicate.GAMEMASTERS)));
                                PromiseDrops.updatePlayerStatus(serverPlayer, justOne ? DropsNetworking.ADD_UPGRADE_SKILL_POINT : DropsNetworking.ADD_UPGRADE_SKILL_POINTS);
                            }
                        } else {
                            if (serverPlayer.getHealth() < serverPlayer.getMaxHealth()) {
                                serverPlayer.addStatusEffect(new StatusEffectInstance(StatusEffects.INSTANT_HEALTH, 1, 2));
                                PromiseDrops.updatePlayerStatus(serverPlayer, DropsNetworking.ADD_UPGRADE_GUI_HEALTH);
                            } else {
                                serverPlayer.addStatusEffect(new StatusEffectInstance(StatusEffects.SATURATION, 10));
                                PromiseDrops.updatePlayerStatus(serverPlayer, DropsNetworking.ADD_UPGRADE_GUI_FOOD);
                            }
                        }
                    }
                }
                serverWorld.spawnParticles(DropsParticles.COLLECTED_UPGRADE, this.getX(), player.getBodyY(0.5), this.getZ(), 1, 0.0, 0.0, 0.0, 0.0);
                this.kill((ServerWorld) getEntityWorld());
                this.trackedPlayer = null;
            }
        }
        super.onPlayerCollision(player);
    }

    private void tryFindCloserPlayer() {
        PlayerEntity player = this.getEntityWorld().getClosestPlayer(this, 250);
        if (player != null) {
            this.trackedPlayer = player;
        }
    }

    @Override
    protected void initDataTracker(DataTracker.Builder builder) {
        builder.add(HOVERING_TICKS, 0);
        builder.add(UPGRADE, new PlayerUpgradeInstance(PlayerUpgrades.ARMOR, 1));
        builder.add(UPGRADE_COLLECTION_TYPE, PlayerUpgradeCollectionType.UPGRADE);
    }

    @Override
    public boolean damage(ServerWorld world, DamageSource source, float amount) {
        if (source.isSourceCreativePlayer() && this.age > 10) {
            this.kill(world);
            return true;
        }
        return false;
    }

    @Override
    protected void readCustomData(ReadView view) {
        view.read("upgrade", PlayerUpgradeInstance.CODEC).ifPresent(this::setUpgrade);
        view.read("upgradeCollectType", PlayerUpgradeCollectionType.CODEC).ifPresent(this::setUpgradeCollectionType);
    }

    @Override
    protected void writeCustomData(WriteView view) {
        view.put("upgrade", PlayerUpgradeInstance.CODEC, this.getUpgrade());
        view.put("upgradeCollectType", PlayerUpgradeCollectionType.CODEC, this.getUpgradeCollectionType());
    }
    public int getHoveringTicks() {
        return this.dataTracker.get(HOVERING_TICKS);
    }

    public void setHoveringTicks(int hoveringTicks) {
        this.dataTracker.set(HOVERING_TICKS, hoveringTicks);
    }

    public float lerpHoveringTicks(float lerp) {
        return MathHelper.lerp(lerp, (float) this.lastHoveringTicks, (float) this.getHoveringTicks());
    }
    @Override
    public PositionInterpolator getInterpolator() {
        return this.interpolator;
    }
    public PlayerUpgradeInstance getUpgrade() {
        return this.dataTracker.get(UPGRADE);
    }
    public void setUpgrade(PlayerUpgradeInstance upgrade) {
        this.dataTracker.set(UPGRADE, upgrade);
    }
    public PlayerUpgradeCollectionType getUpgradeCollectionType() {
        return this.dataTracker.get(UPGRADE_COLLECTION_TYPE);
    }
    public void setUpgradeCollectionType(PlayerUpgradeCollectionType collectionType) {
        this.dataTracker.set(UPGRADE_COLLECTION_TYPE, collectionType);
    }
}
