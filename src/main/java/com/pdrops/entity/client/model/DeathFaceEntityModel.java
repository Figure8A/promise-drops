package com.pdrops.entity.client.model;

import com.pdrops.entity.client.animation.DeathfaceAnimations;
import com.pdrops.entity.state.DeathFaceEntityState;
import net.minecraft.client.model.*;
import net.minecraft.client.render.entity.animation.Animation;
import net.minecraft.client.render.entity.animation.SnifferAnimations;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.render.entity.state.SnifferEntityRenderState;

public class DeathFaceEntityModel extends EntityModel<DeathFaceEntityState> {

    private final ModelPart jaw;
    private final ModelPart head;
    private final ModelPart eyeleft;
    private final ModelPart eyeright;

    private final Animation idleAnimation;
    private final Animation angerAnimation;


    public DeathFaceEntityModel(ModelPart modelPart) {
        super(modelPart);
        this.jaw = modelPart.getChild("jaw");
        this.head = modelPart.getChild("head");
        this.eyeleft = this.head.getChild("eyeleft");
        this.eyeright = this.head.getChild("eyeright");
        this.idleAnimation = DeathfaceAnimations.DEATH_FACE_IDLE.createAnimation(modelPart);
        this.angerAnimation = DeathfaceAnimations.DEATH_FACE_ANGER.createAnimation(modelPart);
    }


    public static TexturedModelData getTexturedModelData() {
        ModelData modelData = new ModelData();
        ModelPartData modelPartData = modelData.getRoot();
        ModelPartData jaw = modelPartData.addChild("jaw", ModelPartBuilder.create().uv(-1, 63).cuboid(-16.0F, 4.5F, -33.0F, 32.0F, 7.0F, 33.0F, new Dilation(0.0F))
                .uv(144, 84).cuboid(8.0F, -6.5F, -14.0F, 8.0F, 11.0F, 14.0F, new Dilation(0.0F))
                .uv(128, 58).cuboid(-16.0F, -6.5F, -15.0F, 8.0F, 11.0F, 15.0F, new Dilation(0.0F)), ModelTransform.origin(0.0F, 12.5F, 15.0F));
        ModelPartData head = modelPartData.addChild("head", ModelPartBuilder.create().uv(128, 0).cuboid(-16.0F, -30.0F, -34.0F, 32.0F, 30.0F, 2.0F, new Dilation(0.0F))
                .uv(0, 0).cuboid(-16.0F, -32.0F, -32.0F, 32.0F, 32.0F, 32.0F, new Dilation(0.0F))
                .uv(0, 103).cuboid(-20.0F, -13.0F, -32.0F, 4.0F, 13.0F, 32.0F, new Dilation(0.0F))
                .uv(72, 103).cuboid(16.0F, -13.0F, -32.0F, 4.0F, 13.0F, 32.0F, new Dilation(0.0F)), ModelTransform.origin(0.0F, 13.0F, 16.0F));
        ModelPartData cube_r1 = head.addChild("cube_r1", ModelPartBuilder.create().uv(128, 32).cuboid(9.0F, -26.0F, 1.0F, 25.0F, 26.0F, 0.0F, new Dilation(0.0F)), ModelTransform.of(-25.0F, -18.0F, -17.0F, 0.0F, 0.0F, -0.3927F));
        ModelPartData eyeleft = head.addChild("eyeleft", ModelPartBuilder.create().uv(144, 120).cuboid(-4.0F, -4.0F, -1.5F, 8.0F, 8.0F, 3.0F, new Dilation(0.0F)), ModelTransform.origin(7.0F, -19.0F, -33.5F));
        ModelPartData eyeright = head.addChild("eyeright", ModelPartBuilder.create().uv(144, 109).cuboid(-4.0F, -4.0F, -1.5F, 8.0F, 8.0F, 3.0F, new Dilation(0.0F)), ModelTransform.origin(-7.0F, -19.0F, -33.5F));
        return TexturedModelData.of(modelData, 256, 256);
    }

    @Override
    public void setAngles(DeathFaceEntityState state) {
        super.setAngles(state);
        this.root.pitch = state.pitch * (float) (Math.PI / 180.0);
        this.root.yaw = state.relativeHeadYaw * (float) (Math.PI / 180.0);

        this.idleAnimation.apply(state.idleAnimationState, state.age);
        this.angerAnimation.apply(state.angerAnimationState, state.age);
    }

}
