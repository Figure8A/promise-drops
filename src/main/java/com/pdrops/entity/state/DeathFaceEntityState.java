package com.pdrops.entity.state;


import net.minecraft.client.render.entity.state.LivingEntityRenderState;
import net.minecraft.entity.AnimationState;

public class DeathFaceEntityState extends LivingEntityRenderState {

    public final AnimationState idleAnimationState = new AnimationState();
    public final AnimationState angerAnimationState = new AnimationState();

}