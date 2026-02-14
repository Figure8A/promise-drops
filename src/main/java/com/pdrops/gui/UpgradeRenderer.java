package com.pdrops.gui;

import com.pdrops.PromiseDrops;
import com.pdrops.data.UpgradeSavesManager;
import com.pdrops.upgrades.PlayerUpgrade;
import com.pdrops.upgrades.PlayerUpgradeCollectionType;
import com.pdrops.upgrades.PlayerUpgradeGuiElement;
import com.pdrops.upgrades.PlayerUpgradeInstance;
import com.pdrops.util.EasingUtil;
import com.pdrops.util.Easings;
import com.pdrops.util.IClientData;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.ColorHelper;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.random.Random;
import org.joml.Matrix3x2fStack;

import java.util.ArrayList;
import java.util.List;

public class UpgradeRenderer {


    public static void renderUpgrades(DrawContext context, float delta, MinecraftClient client) {
        var container = PromiseDrops.getUpgradeContainer(client.world);
        int x = context.getScaledWindowWidth() / 2;
        int y = context.getScaledWindowHeight() / 2;
        float width = 25;
        float x1 = -width / 2f;
        int rX = (int) ((int) -width / 2f);
        Matrix3x2fStack matrices = context.getMatrices();
        matrices.pushMatrix();
        float guiLevel = ((IClientData) client).getGuiLevel(delta);
        List<PlayerUpgradeInstance> upgrades = new ArrayList<>(container.getUpgrades());
        List<PlayerUpgradeGuiElement> upgradeGuiElements = container.getUpgradeGuiElements();
        int qSize = upgradeGuiElements.size();
        if (!upgrades.isEmpty()) {
            int size = upgrades.size();
            for (int i = 0; i < size; i++) {
                var entry = upgrades.get(i);
                Identifier name = PromiseDrops.of("upgrades/upgrademini_" + entry.getUpgrade().value().getName());
                float ticks = entry.getAmbientTicks() + delta;
                float floatUp = ticks % 125f / 125f;
                float rot = ticks % 275f / 275f;
                float upDown = EasingUtil.createEasing(floatUp, -0.55f, 0.55f, Easings::inOutQuad);
                float rotate = EasingUtil.createEasing(rot, -6f, 6f, t -> t);
                float yOffset = 0f;
                float hudLerp = -1;
                float upgradeSize = size + container.collectNewUpgradesInQueue();
                boolean shouldRender = true;
                int subLevels = 0;
                matrices.pushMatrix();
                if (container.hasElementsInQueue()) {
                    var qElement = container.getCurrentUpgradeGuiElement();
                    float dequeueTicks = container.getLerpUpgradeDequeueTicks(delta);
                    float sDequeueTicks = dequeueTicks < 0.75f ? 0f : (dequeueTicks - 0.75f) * 4f;
                    for (PlayerUpgradeGuiElement element : container.getUpgradeGuiElements()) {
                        if (element != qElement) {
                            if (element.getUpgradeInstance() != null && element.getUpgradeInstance().getLevel() == 1 && element.getUpgradeInstance().getUpgrade().matches(entry.getUpgrade())) {
                                shouldRender = false;
                                break;
                            }
                        }
                    }
                    if (qElement.getRenderType().equals(PlayerUpgradeCollectionType.UPGRADE)) {
                        PlayerUpgradeInstance newUpgradeInstance = qElement.getUpgradeInstance();
                        RegistryEntry<PlayerUpgrade> upgrade = newUpgradeInstance.getUpgrade();
                        if (newUpgradeInstance.getLevel() == 1) {
                            upgradeSize = MathHelper.lerp(Easings.inOutSine(sDequeueTicks), upgradeSize, upgradeSize - 1f);
                            if (upgrade.matches(entry.getUpgrade())) {
                                yOffset = MathHelper.lerp(Easings.inOutQuad(sDequeueTicks), 35f, 0f);
                                hudLerp = sDequeueTicks;
                                shouldRender = true;
                            }
                        } else {
                            if (upgrade.matches(entry.getUpgrade())) {
                                matrices.pushMatrix();
                                float newWidth = width * 0.75f;
                                float hudOffset = (newWidth / 2f) - (newWidth * (upgradeSize - i)) + ((upgradeSize * newWidth) / 2f) - (-width / 2f);
                                hudOffset = MathHelper.lerp(Easings.inOutQuad(sDequeueTicks), -rX, hudOffset);


                                float y1 = guiLevel + upDown - x1 + MathHelper.lerp(Easings.inOutQuad(sDequeueTicks), 35f, 0f);
                                matrices.translate(x - hudOffset - rX, y1)
                                        .rotate((float) (Math.PI / 180.0) * rotate)
                                        .scale(0.75f);


                                context.drawGuiTexture(RenderPipelines.GUI_TEXTURED, name, rX, rX, (int) width, (int) width);
                                int levelFor = newUpgradeInstance.getLevel();
                                context.drawGuiTexture(RenderPipelines.GUI_TEXTURED, PromiseDrops.of("upgrades/border/upgrademini_tier_" + MathHelper.clamp(levelFor, 1, 10)), rX, rX, (int) width, (int) width);
                                if (newUpgradeInstance.isAtMaxLevel()) {
                                    context.drawGuiTexture(RenderPipelines.GUI_TEXTURED, PromiseDrops.of("upgrades/upgrade_max_level"), rX, rX, (int) width, (int) width);
                                }
                                matrices.popMatrix();
                            }
                        }
                        if (upgrade.matches(entry.getUpgrade())) {
                            subLevels = 1;
                        }
                    }
//                    if (sDequeueTicks > 0f) {
//                        if (qSize > 1) {
//                            var nQueue = upgradeGuiElements.get(qSize - 2);
//                            int fade = colorWithAlpha(Colors.WHITE, Easings.inQuart(sDequeueTicks));
//                            if (nQueue.getUpgradeInstance() != null && nQueue.getUpgradeInstance().getUpgrade().matches(entry.getUpgrade())) {
//                                matrices.pushMatrix();
//                                float hudOffset = -rX;
//
//                                float y1 = guiLevel + upDown - x1 + 35f;
//                                matrices.translate(x - hudOffset - rX, y1)
//                                        .rotate((float) (Math.PI / 180.0) * rotate)
//                                        .scale(0.75f);
//
//                                context.drawGuiTexture(RenderPipelines.GUI_TEXTURED, name, rX, rX, (int) width, (int) width, fade);
//                                int levelFor = nQueue.getUpgradeInstance().getLevel();
//                                context.drawGuiTexture(RenderPipelines.GUI_TEXTURED, PromiseDrops.of("upgrades/border/upgrademini_tier_" + MathHelper.clamp(levelFor, 1, 10)), rX, rX, (int) width, (int) width, fade);
//                                if (nQueue.getUpgradeInstance().isAtMaxLevel()) {
//                                    context.drawGuiTexture(RenderPipelines.GUI_TEXTURED, PromiseDrops.of("upgrades/upgrade_max_level"), rX, rX, (int) width, (int) width, fade);
//                                }
//                                matrices.popMatrix();
//                            } else if (i == 0) {
//                                matrices.pushMatrix();
//                                matrices.translate(x + rX - rX, 35f + guiLevel - x1)
//                                        .scale(0.75f);
//
//                                context.drawGuiTexture(RenderPipelines.GUI_TEXTURED, PromiseDrops.of("upgrades/upgrademini_" + qElement.getRenderType().asString()), rX, rX, (int) width, (int) width, fade);
//                                matrices.popMatrix();
//                            }
//
//                        }
//                    }
                }
                float newWidth = width * 0.75f;
                float hudOffset = (newWidth / 2f) - (newWidth * (upgradeSize - i)) + ((upgradeSize * newWidth) / 2f) - (-width / 2f);
                if (hudLerp != -1) {
                    hudOffset = MathHelper.lerp(Easings.inOutQuad(hudLerp), -rX, hudOffset);
                }
                float y1 = guiLevel + upDown - x1 + yOffset;
                float x2 = x - hudOffset - rX;
                float xTraRot = 0;
                if (entry.shouldTickRemove()) {
                    int lawBreakTicks = entry.getRemoveTicks();
                    float lawBreakLerp = entry.getLerpRemoveTicks(delta);
                    boolean b = lawBreakLerp < 0.25f;
                    float exploTicks = b ? 0f : (lawBreakLerp - 0.25f) * 1.5f;
                    int seed = (8903 + (3524 * i));
                    int shakeInt = 6;
                    if (b) {
                        seed = seed + (lawBreakTicks % shakeInt);
                    }
                    Random random = Random.create(seed);
                    if (b) {
                        float shake = (lawBreakLerp % (float) shakeInt) / (float) shakeInt;
                        int max = 2;
                        x2 += MathHelper.lerp(shake, (float) random.nextBetween(-max, max), (float) random.nextBetween(-max, max));
                        y1 += MathHelper.lerp(shake, (float) random.nextBetween(-max, max), (float) random.nextBetween(-max, max));
                    } else {
                        y1 = EasingUtil.createEasing(exploTicks, y1, -(10f + random.nextBetween(1, 25)), (context.getScaledWindowHeight() * 3.25f) + 30, Easings::outExpo, Easings::inSine);
                        x2 = MathHelper.lerp(exploTicks, x2, x2 + entry.getXOffset());
                        //xTraRot = MathHelper.lerp(exploTicks * 2f, i, random.nextFloat() * 60f);
                    }
                }


                matrices.translate(x2, y1)
                        .rotate((float) (Math.PI / 180.0) * (rotate + xTraRot), matrices)
                        .scale(0.75f);
                if (shouldRender) {
                    context.drawGuiTexture(RenderPipelines.GUI_TEXTURED, name, rX, rX, (int) width, (int) width);
                    int levelFor = container.getLowestLevelFor(entry) - subLevels;
                    context.drawGuiTexture(RenderPipelines.GUI_TEXTURED, PromiseDrops.of("upgrades/border/upgrademini_tier_" + MathHelper.clamp(levelFor, 1, 10)), rX, rX, (int) width, (int) width);
                    if (entry.isAtMaxLevel(levelFor)) {
                        context.drawGuiTexture(RenderPipelines.GUI_TEXTURED, PromiseDrops.of("upgrades/upgrade_max_level"), rX, rX, (int) width, (int) width);
                    }
                }
                matrices.popMatrix();
            }
        }
        matrices.popMatrix();
        if (container.hasElementsInQueue()) {
            var qElement = container.getCurrentUpgradeGuiElement();
            float dequeueTicks = container.getLerpUpgradeDequeueTicks(delta);
            Text text = Text.of(qElement.getRenderType().asString());
            Text tier = Text.empty();
            int level = 0;
            int sub = 0;
            float v = dequeueTicks < 0.75f ? 0f : (dequeueTicks - 0.75f) * 4f;
            float sDequeueTicks = 1f - v;
            if (qElement.getUpgradeInstance() != null) {
                text = Text.translatable(qElement.getUpgradeInstance().getUpgradeTranslationName());
                tier = Text.of("Tier " + (level = qElement.getUpgradeInstance().getLevel()));
            } else {
                matrices.pushMatrix();
                float add = 0;
                if (v > 0f) {
                    add = EasingUtil.createEasing(v, 0, -1f, 1f, Easings::inOutSine, Easings::inSine);
                }

                matrices.translate(x + rX - rX, 35f + guiLevel - x1 + add)
                        .scale(0.75f);

                context.drawGuiTexture(RenderPipelines.GUI_TEXTURED, PromiseDrops.of("upgrades/upgrademini_" + qElement.getRenderType().asString()), rX, rX, (int) width, (int) width, colorWithAlpha(Colors.WHITE, sDequeueTicks));
                matrices.popMatrix();
                sub += 10;

            }
            if (qElement.getRenderType().equals(PlayerUpgradeCollectionType.FOOD)) {
                text = Text.of("Saturation Restored!");
            }
            if (qElement.getRenderType().equals(PlayerUpgradeCollectionType.HEART)) {
                text = Text.of("Health Restored!");
            }
            if (qElement.getRenderType().equals(PlayerUpgradeCollectionType.SINGLE_SKILL_POINT)) {
                text = Text.of("+1 Skill Point!");
            }
            if (qElement.getRenderType().equals(PlayerUpgradeCollectionType.SKILL_POINTS)) {
                text = Text.of("+1 Skill Point to all!");
            }
            int nameY = (int) (45 + 17 + guiLevel);
            int tY = (int) (55 + 17 + guiLevel);
            context.drawCenteredTextWithShadow(client.textRenderer, text, x, tY - sub, colorWithAlpha(Colors.WHITE, sDequeueTicks));
            if (!tier.equals(Text.empty())) {
                context.drawCenteredTextWithShadow(client.textRenderer, tier, x, nameY, colorWithAlpha(PromiseDrops.getColorFromTier(level), sDequeueTicks));
            }
        }
        matrices.pushMatrix();
        int deathTicks = UpgradeSavesManager.getDeathFaceTimer(client.world);
        int containerWidth = 82;
        int containerHeight = 26;
        int barWidth = 74;
        int maxAirSupply = client.player.getMaxAir();
        int airSupply = Math.clamp(client.player.getAir(), 0, maxAirSupply);
        boolean isInWater = client.player.isSubmergedIn(FluidTags.WATER);
        boolean shouldMoveHudUp = client.player.getArmor() > 0 || isInWater || airSupply < maxAirSupply;
        int barYOffset = client.player.isCreative() ? 51 : shouldMoveHudUp ? 78 : 68;
        int finalBarY = context.getScaledWindowHeight() - barYOffset;

        float animationTicks = client.player.age + delta;
        float floatTicks = animationTicks % 60f / 60f;
        float floatTicks1 = (animationTicks + 60f) % 120f / 120f;
        float fadeTicks = 1f;
        float deltaFadeTicks = container.getLerpDeathFaceBarTicks(delta);
        finalBarY = MathHelper.lerp(Easings.inOutQuad(deltaFadeTicks), context.getScaledWindowHeight() + (containerHeight * 2), finalBarY);

        matrices.translate(x - (containerWidth / 2f) + (EasingUtil.createEasing(floatTicks1, 1f, -1f, Easings::inOutSine)), finalBarY + (EasingUtil.createEasing(floatTicks, -1f, 1f, Easings::inOutSine)));
        int barAnimationTicks = MathHelper.lerp(animationTicks % 160f / 160f, barWidth, 0);
        int barHeight = 12;
        float barDelta = (float) deathTicks / container.getLastDeathFaceSync();
        int barW = MathHelper.lerp(MathHelper.clamp(barDelta, 0f, 1f), barWidth + 2, 0);
        context.drawGuiTexture(RenderPipelines.GUI_TEXTURED, PromiseDrops.of("death_bar/death_timer_bar"), barW + barAnimationTicks, barHeight, barAnimationTicks, 0, 4, 13, barWidth + 2, barHeight, colorWithAlpha(Colors.WHITE, fadeTicks));
        context.drawGuiTexture(RenderPipelines.GUI_TEXTURED, PromiseDrops.of("death_bar/death_timer_container"), 0, 0, containerWidth, containerHeight, colorWithAlpha(Colors.WHITE, fadeTicks));
        int tTime = deathTicks;
        int minutes = tTime / (1200);
        int seconds = (tTime / 20) % 60;
        String str = String.format("%d:%02d", minutes, seconds);
        if (minutes < 1) {
//            str = String.format("%02d", seconds);
//            if (seconds < 10) {
//                str = String.format("%01d", seconds);
//            }
        }

        context.drawCenteredTextWithShadow(client.textRenderer, str, containerWidth / 2, containerHeight / 2 + 1, Colors.WHITE);

        matrices.popMatrix();
    }

    public static int colorWithAlpha(int color, float alpha) {
        return ColorHelper.withAlpha(alpha, color);
    }
}
