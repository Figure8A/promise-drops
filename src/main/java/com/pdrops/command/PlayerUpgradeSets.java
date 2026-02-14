package com.pdrops.command;


import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.pdrops.PromiseDrops;
import com.pdrops.data.UpgradeSavesManager;
import com.pdrops.upgrades.PlayerUpgrade;
import com.pdrops.upgrades.PlayerUpgradeContainer;
import com.pdrops.upgrades.PlayerUpgradeInstance;
import com.pdrops.upgrades.registry.DropRegistries;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.DefaultPermissions;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.command.argument.RegistryEntryReferenceArgumentType;
import net.minecraft.command.argument.RegistryKeyArgumentType;
import net.minecraft.command.permission.PermissionCheck;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Pair;
import net.minecraft.world.gen.structure.Structure;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static net.minecraft.command.argument.RegistryEntryReferenceArgumentType.getRegistryEntry;

public class PlayerUpgradeSets {
    public static final PermissionCheck PERMISSION_CHECK = new PermissionCheck.Require(DefaultPermissions.GAMEMASTERS);


    public static void register(CommandDispatcher<ServerCommandSource> serverCommandSourceCommandDispatcher, CommandRegistryAccess commandRegistryAccess, CommandManager.RegistrationEnvironment registrationEnvironment) {

        serverCommandSourceCommandDispatcher.register
                (CommandManager.literal("upgrades").requires(CommandManager.requirePermissionLevel(PERMISSION_CHECK))
                        .then(CommandManager.literal("set")
                                .then(CommandManager.argument("upgrade", RegistryEntryReferenceArgumentType.registryEntry(commandRegistryAccess, DropRegistries.PLAYER_UPGRADE_KEY))
                                        .executes(context -> PlayerUpgradeSets.setUpgrade(context.getSource(), getPlayerUpgrade(context, "upgrade")))

                                        .then(CommandManager.argument("level", IntegerArgumentType.integer(0, 100))
                                                .executes(context -> PlayerUpgradeSets.setUpgrade(context.getSource(), getPlayerUpgrade(context, "upgrade"), IntegerArgumentType.getInteger(context, "level"))))))

                        .then(CommandManager.literal("get")
                                .executes(context -> PlayerUpgradeSets.getAllUpgrades(context.getSource())))

                        .then(CommandManager.literal("max")
                                .executes(context -> PlayerUpgradeSets.maxAllUpgrades(context.getSource()))
                                .then(CommandManager.argument("upgrade", RegistryEntryReferenceArgumentType.registryEntry(commandRegistryAccess, DropRegistries.PLAYER_UPGRADE_KEY))
                                        .executes(context -> PlayerUpgradeSets.maxUpgrade(context.getSource(), getPlayerUpgrade(context, "upgrade")))))

                        .then(CommandManager.literal("death")
                                .then(CommandManager.literal("start").executes(context -> setDeathTicks(context.getSource(), 1200)))
                                .then(CommandManager.literal("stop").executes(context -> setDeathTicks(context.getSource(), 0)))
                                .then(CommandManager.literal("minutes")
                                        .then(CommandManager.argument("minute", IntegerArgumentType.integer())
                                                .executes(context -> setDeathTicks(context.getSource(), IntegerArgumentType.getInteger(context, "minute") * 1200))))
                                .then(CommandManager.literal("seconds")
                                        .then(CommandManager.argument("second", IntegerArgumentType.integer())
                                                .executes(context -> setDeathTicks(context.getSource(), IntegerArgumentType.getInteger(context, "second") * 20))))
                                .then(CommandManager.literal("ticks")
                                        .then(CommandManager.argument("tick", IntegerArgumentType.integer())
                                                .executes(context -> setDeathTicks(context.getSource(), IntegerArgumentType.getInteger(context, "tick"))))))


                        .then(CommandManager.literal("reset")
                                .executes(context -> PlayerUpgradeSets.resetUpgrades(context.getSource()))));
    }

    private static int setDeathTicks(ServerCommandSource source, int ticks) {
        ServerWorld world = source.getServer().getOverworld();
        var holder = PromiseDrops.getUpgradeContainer(world);
        UpgradeSavesManager.setDeathFaceTimer(world, ticks);
        if (ticks == 0) {
            holder.spawnDeathFace(world);
        }
        source.sendFeedback(() -> Text.literal("Set death face spawn timer to: " + ticks + " ticks."), true);
        return 0;
    }

    private static int maxUpgrade(ServerCommandSource source, RegistryEntry.Reference<PlayerUpgrade> upgrade) {
        ServerWorld world = source.getServer().getOverworld();
        PromiseDrops.getUpgradeContainer(world).setPlayerUpgrade(new PlayerUpgradeInstance(upgrade).maxLevel());
        source.sendFeedback(() -> Text.literal("Set upgrade" + upgrade.registryKey() + " to max level. (" + upgrade.value().getMaxLevel()+ ")"), true);
        return 0;
    }

    private static int maxAllUpgrades(ServerCommandSource source) {
        ServerWorld world = source.getServer().getOverworld();
        for (var entry : DropRegistries.PLAYER_UPGRADE.streamEntries().toList()) {
            PromiseDrops.getUpgradeContainer(world).setPlayerUpgrade(new PlayerUpgradeInstance(entry).maxLevel());
        }
        source.sendFeedback(() -> Text.literal("Set all upgrades to max)"), true);
        return 0;
    }

    private static int getAllUpgrades(ServerCommandSource source) {
        PlayerUpgradeContainer upgradeContainer = PromiseDrops.getUpgradeContainer(source.getWorld());
        StringBuilder stringBuilder = new StringBuilder("Upgrades: ");
        for (PlayerUpgradeInstance upgrade : upgradeContainer.getUpgrades()) {
            stringBuilder.append(upgrade);
        }
        source.sendFeedback(() -> Text.literal(stringBuilder.toString()), true);
        return 0;
    }


    private static int resetUpgrades(ServerCommandSource source) {
        PlayerUpgradeContainer upgradeContainer = PromiseDrops.getUpgradeContainer(source.getWorld());
        upgradeContainer.resetUpgrades();
        upgradeContainer.markDirty();
        source.sendFeedback(() -> Text.literal("Removed All Upgrades"), true);
        return 0;
    }
    private static int setUpgrade(ServerCommandSource source, RegistryEntry.Reference<PlayerUpgrade> upgrade) {
        return setUpgrade(source, upgrade, 1);
    }


    private static int setUpgrade(ServerCommandSource source, RegistryEntry.Reference<PlayerUpgrade> upgrade, int level) {
        ServerWorld world = source.getServer().getOverworld();
        PromiseDrops.getUpgradeContainer(world).setPlayerUpgrade(new PlayerUpgradeInstance(upgrade, level));
        source.sendFeedback(() -> Text.literal("Set upgrade" + upgrade.registryKey() + " to level " + level), true);
        return 0;
    }


    public static RegistryEntry.Reference<PlayerUpgrade> getPlayerUpgrade(CommandContext<ServerCommandSource> context, String name) throws CommandSyntaxException {
        return getRegistryEntry(context, name, DropRegistries.PLAYER_UPGRADE_KEY);
    }
}
