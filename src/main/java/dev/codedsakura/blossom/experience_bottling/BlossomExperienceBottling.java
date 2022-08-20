package dev.codedsakura.blossom.experience_bottling;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import dev.codedsakura.blossom.lib.BlossomLib;
import dev.codedsakura.blossom.lib.config.ConfigManager;
import dev.codedsakura.blossom.lib.permissions.Permissions;
import dev.codedsakura.blossom.lib.text.TextUtils;
import dev.codedsakura.blossom.lib.utils.CustomLogger;
import net.fabricmc.api.ModInitializer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import org.apache.logging.log4j.core.Logger;

import static dev.codedsakura.blossom.experience_bottling.BottledXpUtils.*;
import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class BlossomExperienceBottling implements ModInitializer {
    public static BlossomExperienceBottlingConfig CONFIG = ConfigManager.register(BlossomExperienceBottlingConfig.class, "BlossomExperienceBottling.json", newConfig -> CONFIG = newConfig);
    public static final Logger LOGGER = CustomLogger.createLogger("BlossomExperienceBottling");

    @Override
    public void onInitialize() {
        LOGGER.info("BlossomExperienceBottling is starting...");

        BlossomLib.addCommand(literal("bottle")
                .requires(Permissions.require("blossom.bottle", true))
                .executes(this::runBottleAll)
                .then(argument("amount", IntegerArgumentType.integer(0))
                        .then(literal("levels").executes(ctx -> runBottle(ctx, "levels")))
                        .then(literal("points").executes(ctx -> runBottle(ctx, "points"))))
                .then(literal("exactly")
                        .then(argument("amount", IntegerArgumentType.integer(0))
                                .then(literal("levels").executes(ctx -> runBottle(ctx, "levels")))
                                .then(literal("points").executes(ctx -> runBottle(ctx, "points")))))
                .then(literal("all")
                        .executes(this::runBottleAll)
                        .then(literal("in-increments-of")
                                .then(argument("incrementAmount", IntegerArgumentType.integer(0))
                                        .then(literal("levels").executes(ctx -> runBottleAllIncremental(ctx, "levels")))
                                        .then(literal("points").executes(ctx -> runBottleAllIncremental(ctx, "points")))))
                        .then(literal("to-level")
                                .then(argument("amount", IntegerArgumentType.integer(0))
                                        .executes(this::runBottleAllToLevel)
                                        .then(literal("in-increments-of")
                                                .then(argument("incrementAmount", IntegerArgumentType.integer(0))
                                                        .then(literal("levels").executes(ctx -> runBottleAllToLevelIncremental(ctx, "levels")))
                                                        .then(literal("points").executes(ctx -> runBottleAllToLevelIncremental(ctx, "points")))))))));
    }


    private void storePointsIncremental(CommandContext<ServerCommandSource> ctx, int totalPoints, int increment) throws CommandSyntaxException {
        ServerPlayerEntity player = ctx.getSource().getPlayerOrThrow();

        int playerAvailableXp = getPlayerXpAsPoints(player);
        if (totalPoints > playerAvailableXp) {
            TextUtils.sendErr(ctx, "blossom.bottling.error.not-enough", totalPoints, playerAvailableXp);
            return;
        }

        int count = Math.floorDiv(totalPoints, increment);
        int leftover = totalPoints % increment;

        player.giveItemStack(
                create(player, increment, count)
        );

        if (leftover > 0) {
            player.giveItemStack(
                    create(player, leftover)
            );
        }

        player.addExperience(-totalPoints);

        if (CONFIG.bottlingSound != null) {
            player.playSound(
                    new SoundEvent(Identifier.tryParse(CONFIG.bottlingSound.identifier)),
                    SoundCategory.PLAYERS,
                    CONFIG.bottlingSound.volume,
                    CONFIG.bottlingSound.pitch
            );
        }
    }

    private void storePoints(CommandContext<ServerCommandSource> ctx, int points) throws CommandSyntaxException {
        storePointsIncremental(ctx, points, points);
    }

    private int getPoints(String type, int amount) {
        return switch (type) {
            case "levels" -> levelToTotalPoints(amount);
            case "points" -> amount;
            default -> throw new IllegalStateException("Unexpected value: " + type);
        };
    }


    private int runBottle(CommandContext<ServerCommandSource> ctx, String type) throws CommandSyntaxException {
        int amount = IntegerArgumentType.getInteger(ctx, "amount");

        storePoints(ctx, getPoints(type, amount));

        return Command.SINGLE_SUCCESS;
    }

    private int runBottleAll(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        int points = getPlayerXpAsPoints(ctx.getSource().getPlayerOrThrow());

        storePoints(ctx, points);

        return Command.SINGLE_SUCCESS;
    }

    private int runBottleAllToLevel(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        ServerPlayerEntity player = ctx.getSource().getPlayerOrThrow();
        int amount = IntegerArgumentType.getInteger(ctx, "amount");

        int points = getPlayerXpAsPoints(player) - levelToTotalPoints(amount);

        storePoints(ctx, points);
        return Command.SINGLE_SUCCESS;
    }

    private int runBottleAllIncremental(CommandContext<ServerCommandSource> ctx, String type) throws CommandSyntaxException {
        ServerPlayerEntity player = ctx.getSource().getPlayerOrThrow();
        int incrementAmount = IntegerArgumentType.getInteger(ctx, "incrementAmount");

        int points = getPlayerXpAsPoints(player);

        storePointsIncremental(ctx, points, getPoints(type, incrementAmount));

        return Command.SINGLE_SUCCESS;
    }

    private int runBottleAllToLevelIncremental(CommandContext<ServerCommandSource> ctx, String type) throws CommandSyntaxException {
        ServerPlayerEntity player = ctx.getSource().getPlayerOrThrow();
        int incrementAmount = IntegerArgumentType.getInteger(ctx, "incrementAmount");
        int amount = IntegerArgumentType.getInteger(ctx, "amount");

        int points = getPlayerXpAsPoints(player) - levelToTotalPoints(amount);

        storePointsIncremental(ctx, points, getPoints(type, incrementAmount));

        return Command.SINGLE_SUCCESS;
    }
}
