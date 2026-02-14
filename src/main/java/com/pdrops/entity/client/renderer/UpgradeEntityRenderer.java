package com.pdrops.entity.client.renderer;



import com.pdrops.PromiseDrops;
import com.pdrops.entity.custom.DeathFaceEntity;
import com.pdrops.entity.custom.UpgradeEntity;
import com.pdrops.entity.state.UpgradeEntityState;
import com.pdrops.upgrades.PlayerUpgradeCollectionType;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.item.ItemModelManager;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayers;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.state.CameraRenderState;
import net.minecraft.client.texture.SpriteHolder;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Colors;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;

public class UpgradeEntityRenderer extends EntityRenderer<UpgradeEntity, UpgradeEntityState> {
    private final SpriteHolder materials;
    public ItemModelManager itemModelManager;
//    public static final SpriteMapper SPRITE_MAPPER = new SpriteMapper(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE, "entity/holy_butterfly");
//    public static final SpriteIdentifier BUTTERFLY = SPRITE_MAPPER.map(PromiseDrops.of("img_god"));

    public UpgradeEntityRenderer(EntityRendererFactory.Context context) {
        super(context);
        this.itemModelManager = context.getItemModelManager();
        this.materials = context.getSpriteHolder();
    }

    @Override
    public void updateRenderState(UpgradeEntity entity, UpgradeEntityState state, float tickDelta) {
        super.updateRenderState(entity, state, tickDelta);
        state.hoverTicks = entity.lerpHoveringTicks(tickDelta);
        state.upgrade = entity.getUpgrade();
        state.collectionType = entity.getUpgradeCollectionType();
        if (state.collectionType.equals(PlayerUpgradeCollectionType.HEART) && MinecraftClient.getInstance().getCameraEntity() instanceof LivingEntity livingEntity) {
            if (livingEntity.getHealth() >= livingEntity.getMaxHealth()) {
                state.collectionType = PlayerUpgradeCollectionType.FOOD;
            }
        }
    }

    @Override
    public void render(UpgradeEntityState state, MatrixStack matrices, OrderedRenderCommandQueue queue, CameraRenderState cameraState) {
        matrices.push();
        float scale = 1f;
        matrices.scale(scale, scale, scale);
        matrices.multiply(cameraState.orientation);
        String name = switch (state.collectionType) {
            case UPGRADE -> state.upgrade.getUpgrade().value().getName();
            case HEART, FOOD, SINGLE_SKILL_POINT, SKILL_POINTS -> state.collectionType.asString();
        };
        Identifier upgradeSprite = PromiseDrops.of("textures/entity/upgrade/upgrade_" + name + ".png");
        queue.submitCustom(matrices, RenderLayers.itemEntityTranslucentCull(upgradeSprite), (entry, vertexConsumer) -> {
            int light = LightmapTextureManager.MAX_LIGHT_COORDINATE;
            produceVertex(vertexConsumer, entry, light, 0.0F, 0, 0, 1);
            produceVertex(vertexConsumer, entry, light, 1.0F, 0, 1, 1);
            produceVertex(vertexConsumer, entry, light, 1.0F, 1, 1, 0);
            produceVertex(vertexConsumer, entry, light, 0.0F, 1, 0, 0);
        });
        int level1 = MathHelper.clamp(state.upgrade.getLevel(), 1, 10);
        matrices.translate(0,0,0.0005);
        if (level1 > 1) {
            Identifier level = PromiseDrops.of("textures/entity/upgrade/border/upgrade_tier_" + level1 + ".png");
            queue.submitCustom(matrices, RenderLayers.entityCutoutNoCullZOffset(level), (entry, vertexConsumer) -> {
                int light = LightmapTextureManager.MAX_LIGHT_COORDINATE;
                produceVertex(vertexConsumer, entry, light, 0.0F, 0, 0, 1);
                produceVertex(vertexConsumer, entry, light, 1.0F, 0, 1, 1);
                produceVertex(vertexConsumer, entry, light, 1.0F, 1, 1, 0);
                produceVertex(vertexConsumer, entry, light, 0.0F, 1, 0, 0);
            });
        }

        matrices.pop();

        super.render(state, matrices, queue, cameraState);
    }


    private static void produceVertex(VertexConsumer vertexConsumer, MatrixStack.Entry matrix, int light, float x, int z, float textureU, float textureV) {
        vertexConsumer.vertex(matrix, x - 0.5F, z - 0.25F, 0.0F)
                .color(Colors.WHITE)
                .texture(textureU, textureV)
                .overlay(OverlayTexture.DEFAULT_UV)
                .light(light)
                .normal(matrix, 0.0F, 1.0F, 0.0F);
    }
    @Override
    public UpgradeEntityState createRenderState() {
        return new UpgradeEntityState();
    }

    @Override
    protected int getBlockLight(UpgradeEntity entity, BlockPos pos) {
        return 15;
    }
}