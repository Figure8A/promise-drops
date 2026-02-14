package com.pdrops.particles;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.particle.ExplosionLargeParticle;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleFactory;
import net.minecraft.client.particle.SpriteProvider;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particle.SimpleParticleType;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.random.Random;

@Environment(EnvType.CLIENT)
public class UpgradeParticle extends ExplosionLargeParticle {
	protected UpgradeParticle(ClientWorld clientWorld, double d, double e, double f, double g, SpriteProvider spriteProvider, boolean shorter) {
		super(clientWorld, d, e, f, g, spriteProvider);
		this.maxAge = shorter ? 13 : 14;
		this.scale = 1F;
		this.updateSprite(spriteProvider);
	}

	@Override
	public int getBrightness(float tint) {
		float f = (this.age + tint) / this.maxAge;
		f = MathHelper.clamp(f, 0.0F, 1.0F);
		int i = super.getBrightness(tint);
		int j = i & 0xFF;
		int k = i >> 16 & 0xFF;
		j += (int)(f * 15.0F * 16.0F);
		if (j > 240) {
			j = 240;
		}

		return j | k << 16;
	}

	@Environment(EnvType.CLIENT)
	public static class Factory implements ParticleFactory<SimpleParticleType> {
		private final SpriteProvider spriteProvider;

		public Factory(SpriteProvider spriteProvider) {
			this.spriteProvider = spriteProvider;
		}

		public Particle createParticle(
			SimpleParticleType simpleParticleType, ClientWorld clientWorld, double d, double e, double f, double g, double h, double i, Random random
		) {
			return new UpgradeParticle(clientWorld, d, e, f, g, this.spriteProvider, false);
		}

		public SpriteProvider getSpriteProvider() {
			return spriteProvider;
		}
	}

	public static class FactoryShort extends Factory {
		public FactoryShort(SpriteProvider spriteProvider) {
			super(spriteProvider);
		}
		public Particle createParticle(SimpleParticleType simpleParticleType, ClientWorld clientWorld, double d, double e, double f, double g, double h, double i, Random random) {
			return new UpgradeParticle(clientWorld, d, e, f, g, this.getSpriteProvider(), true);
		}
	}
}
