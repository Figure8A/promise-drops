package com.pdrops.mixin;

import com.pdrops.PromiseDrops;
import com.pdrops.entity.custom.DeathFaceEntity;
import com.pdrops.entity.inter.IDeathFaceGetter;
import com.pdrops.upgrades.registry.PlayerUpgrades;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin extends LivingEntity implements IDeathFaceGetter {

	public DeathFaceEntity deathFaceEntity;
	@Shadow
	protected abstract void collideWithEntity(Entity entity);

	protected PlayerEntityMixin(EntityType<? extends LivingEntity> entityType, World world) {
		super(entityType, world);
	}

	@Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;getOtherEntities(Lnet/minecraft/entity/Entity;Lnet/minecraft/util/math/Box;)Ljava/util/List;"), method = "tickMovement")
	private void addMagnetar(CallbackInfo ci) {
		boolean hasM = PromiseDrops.getUpgradeContainer(this.getEntityWorld()).hasUpgrade(PlayerUpgrades.MAGNETAR);
		if (hasM) {
			Box box;
			if (this.hasVehicle() && !this.getVehicle().isRemoved()) {
				box = this.getBoundingBox().union(this.getVehicle().getBoundingBox()).expand(1.0, 0.0, 1.0);
			} else {
				box = this.getBoundingBox().expand(1.0, 0.5, 1.0);
			}
			var d = PromiseDrops.getUpgradeContainer(this.getEntityWorld()).getUpgrade(PlayerUpgrades.MAGNETAR);
			double ex = d.getUpgradeBonus();
			this.getEntityWorld().getEntitiesByClass(ItemEntity.class, box.expand(ex, ex * 0.5, ex), Entity::isAlive).forEach(this::collideWithEntity);
		}
	}
	@Inject(at = @At(value = "RETURN"), method = "getOffGroundSpeed", cancellable = true)
	private void addDragReduction(CallbackInfoReturnable<Float> cir) {
		boolean hasDragReduction = PromiseDrops.getUpgradeContainer(this.getEntityWorld()).hasUpgrade(PlayerUpgrades.DRAG_REDUCTION);
		if (hasDragReduction) {
			cir.setReturnValue(cir.getReturnValue() + (float) PromiseDrops.getUpgradeContainer(this.getEntityWorld()).getUpgrade(PlayerUpgrades.DRAG_REDUCTION).getUpgradeBonus());
		}
	}



	@Override
	public DeathFaceEntity getDeathFaceEntity() {
		return deathFaceEntity;
	}

	@Override
	public void setDeathFaceEntity(DeathFaceEntity deathFaceEntity) {
		this.deathFaceEntity = deathFaceEntity;
	}
}