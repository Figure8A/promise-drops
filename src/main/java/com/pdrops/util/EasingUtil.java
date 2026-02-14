package com.pdrops.util;


import net.minecraft.util.math.MathHelper;

import java.util.function.Function;

public class EasingUtil {
    public static float createEasing(float delta, float from, float to, Function<Float, Float> easing) {
        return createEasing(delta, from, to, easing, easing);
    }
    public static float createEasing(float delta, float from, float to, Function<Float, Float> fromEasing, Function<Float, Float> backEasing) {
        return createEasing(delta, from, to, from, fromEasing, backEasing);
    }
    public static float createEasing(float delta, float from, float to, float finish, Function<Float, Float> easing) {
        return createEasing(delta, from, to, finish, easing, easing);
    }
    public static float createEasing(float delta, float from, float to, float finish, Function<Float, Float> fromEasing, Function<Float, Float> backEasing) {
        float x = delta * 2f;
        return delta < 0.5 ? MathHelper.lerp(fromEasing.apply(x), from, to) : MathHelper.lerp(backEasing.apply(x - 1f), to, finish);
    }

}
