package com.pdrops;

import com.pdrops.data.UpgradeSavesManager;
import com.pdrops.entity.DropsEntities;
import com.pdrops.entity.client.renderer.DeathFaceEntityRenderer;
import com.pdrops.entity.client.renderer.UpgradeEntityRenderer;
import com.pdrops.entity.client.model.DeathFaceEntityModel;
import com.pdrops.entity.client.model.layers.PromiseDropsModelLayers;
import com.pdrops.gui.UpgradeRenderer;
import com.pdrops.networking.registry.DropsNetworking;
import com.pdrops.particles.UpgradeParticle;
import com.pdrops.particles.registry.DropsParticles;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientWorldEvents;
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.entity.EntityRendererFactories;

public class PromiseDropsClient implements ClientModInitializer {


	@Override
	public void onInitializeClient() {
		DropsNetworking.registerS2CDropPackets();
		HudElementRegistry.attachElementAfter(VanillaHudElements.BOSS_BAR, PromiseDrops.of("upgrade_ui"), (context, tickCounter) -> UpgradeRenderer.renderUpgrades(context, tickCounter.getTickProgress(true), MinecraftClient.getInstance()));
		EntityRendererFactories.register(DropsEntities.UPGRADE_ENTITY, UpgradeEntityRenderer::new);
		ParticleFactoryRegistry.getInstance().register(DropsParticles.DROPPED_UPGRADE, UpgradeParticle.Factory::new);
		ParticleFactoryRegistry.getInstance().register(DropsParticles.COLLECTED_UPGRADE, UpgradeParticle.FactoryShort::new);
		EntityRendererRegistry.register(DropsEntities.DEATH_FACE, DeathFaceEntityRenderer::new);
		EntityModelLayerRegistry.registerModelLayer(PromiseDropsModelLayers.DEATH_FACE, DeathFaceEntityModel::getTexturedModelData);

		ClientWorldEvents.AFTER_CLIENT_WORLD_CHANGE.register((client, world) -> world.onAttachedSet(UpgradeSavesManager.DEATH_FACE_TIMER).register((before, after) -> {
			if (before == null || after == null) {
				return;
			}
			PromiseDrops.getUpgradeContainer(world).onDeathFaceUpdate(before, after);
		}));
	}
}