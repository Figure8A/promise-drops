package com.pdrops.mixin.client;

import net.minecraft.client.gui.hud.BossBarHud;
import net.minecraft.client.gui.hud.ClientBossBar;
import net.minecraft.client.gui.hud.PlayerListHud;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;
import java.util.UUID;

@Mixin(BossBarHud.class)
public interface IBossBarHudGetter {

    @Accessor("bossBars")
    Map<UUID, ClientBossBar> getBossBars();
}
