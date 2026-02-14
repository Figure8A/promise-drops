package com.pdrops.entity.client.renderer;



import com.pdrops.PromiseDrops;
import com.pdrops.entity.client.model.DeathFaceEntityModel;
import com.pdrops.entity.client.model.layers.PromiseDropsModelLayers;
import com.pdrops.entity.custom.DeathFaceEntity;
import com.pdrops.entity.state.DeathFaceEntityState;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.state.CameraRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;

public class DeathFaceEntityRenderer extends LivingEntityRenderer<DeathFaceEntity, DeathFaceEntityState, DeathFaceEntityModel> {


    public DeathFaceEntityRenderer(EntityRendererFactory.Context context) {
        super(context, new DeathFaceEntityModel(context.getPart(PromiseDropsModelLayers.DEATH_FACE)), 1f);
    }

    @Override
    public Identifier getTexture(DeathFaceEntityState state) {
        return PromiseDrops.of("textures/entity/death_face/death_face.png");
    }

    @Override
    public void render(DeathFaceEntityState livingEntityRenderState, MatrixStack matrixStack, OrderedRenderCommandQueue orderedRenderCommandQueue, CameraRenderState cameraRenderState) {
        super.render(livingEntityRenderState, matrixStack, orderedRenderCommandQueue, cameraRenderState);
    }

    @Override
    protected boolean hasLabel(DeathFaceEntity livingEntity, double d) {
        return false;
    }

    @Override
    public DeathFaceEntityState createRenderState() {
        return new DeathFaceEntityState();
    }

    @Override
    protected int getBlockLight(DeathFaceEntity entity, BlockPos pos) {
        return 15;
    }

    @Override
    public void updateRenderState(DeathFaceEntity livingEntity, DeathFaceEntityState livingEntityRenderState, float f) {
        super.updateRenderState(livingEntity, livingEntityRenderState, f);
        livingEntityRenderState.idleAnimationState.copyFrom(livingEntity.idleAnimationState);
        livingEntityRenderState.angerAnimationState.copyFrom(livingEntity.angerAnimationState);
    }


    @Override
    protected Box getBoundingBox(DeathFaceEntity livingEntity) {
        return super.getBoundingBox(livingEntity);
    }
}