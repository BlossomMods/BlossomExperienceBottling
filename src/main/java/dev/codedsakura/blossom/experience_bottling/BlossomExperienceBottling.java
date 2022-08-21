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
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.s2c.play.PlaySoundIdS2CPacket;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;
import net.minecraft.util.registry.Registry;
import org.apache.logging.log4j.core.Logger;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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


    public static void playSound(PlayerEntity player, @Nullable BlossomExperienceBottlingConfig.Sound sound) {
        if (player.world.isClient) {
            return;
        }

        if (sound != null) {
            if (player instanceof ServerPlayerEntity) {
                ((ServerPlayerEntity) player).networkHandler
                        .sendPacket(new PlaySoundIdS2CPacket(
                                Identifier.tryParse(sound.identifier),
                                SoundCategory.PLAYERS,
                                player.getPos(),
                                sound.volume,
                                sound.pitch,
                                player.getWorld().getRandom().nextLong()
                        ));
            }
        }
    }

    private Map<Item, Integer> getMissingRequiredItems(ServerPlayerEntity player) {
        if (CONFIG.items == null) {
            return Map.of();
        }

        var hasItems = Stream.of(player.getInventory().main, player.getInventory().armor, player.getInventory().offHand)
                .flatMap(List::stream)
                .collect(Collectors.groupingBy(ItemStack::getItem))
                .entrySet()
                .stream()
                .collect(Collectors.toMap(
                        entry -> entry.getKey().asItem(),
                        entry -> entry.getValue().stream().mapToInt(ItemStack::getCount).sum()
                ));

        return Stream.of(CONFIG.items.consumeItems, CONFIG.items.requireItems)
                .flatMap(Arrays::stream)
                .collect(Collectors.groupingBy(i -> i.identifier))
                .entrySet()
                .stream()
                .map(e -> new Pair<>(
                        Registry.ITEM.get(Identifier.tryParse(e.getKey())),
                        e.getValue().stream().mapToInt(i -> i.count).sum()
                ))
                .filter(e -> !hasItems.containsKey(e.getLeft()) || hasItems.get(e.getLeft()) < e.getRight())
                .collect(Collectors.toMap(Pair::getLeft, Pair::getRight));
    }

    private void consumeItems(ServerPlayerEntity player, int multiplier) {
        if (CONFIG.items == null) {
            return;
        }

        Stream.of(CONFIG.items.consumeItems)
                .collect(Collectors.groupingBy(i -> i.identifier))
                .forEach((identifier, itemList) -> {
                    Item item = Registry.ITEM.get(Identifier.tryParse(identifier));
                    int count = itemList.stream().mapToInt(i -> i.count).sum() * multiplier;
                    player.getInventory().remove(
                            itemStack -> itemStack.getItem().equals(item),
                            count,
                            player.playerScreenHandler.getCraftingInput()
                    );
                });
    }

    public static void returnItems(PlayerEntity player) {
        if (CONFIG.items == null) {
            return;
        }

        Stream.of(CONFIG.items.returnItems)
                .collect(Collectors.groupingBy(i -> i.identifier))
                .forEach((identifier, itemList) -> {
                    Item item = Registry.ITEM.get(Identifier.tryParse(identifier));
                    int count = itemList.stream().mapToInt(i -> i.count).sum();
                    player.getInventory().insertStack(new ItemStack(item, count));
                });
    }


    private void giveOrDropStack(ServerPlayerEntity player, ItemStack stack) {
        if (!player.giveItemStack(stack)) {
            player.dropItem(stack, false);
        }
    }

    private void storePointsIncremental(CommandContext<ServerCommandSource> ctx, int totalPoints, int increment) throws CommandSyntaxException {
        ServerPlayerEntity player = ctx.getSource().getPlayerOrThrow();

        int playerAvailableXp = getPlayerXpAsPoints(player);
        if (totalPoints == 0 || totalPoints > playerAvailableXp) {
            TextUtils.sendErr(ctx, "blossom.bottling.error.not-enough", totalPoints, playerAvailableXp);
            return;
        }

        if (increment == 0) {
            TextUtils.sendErr(ctx, "blossom.bottling.error.div-by-0");
            return;
        }

        Map<Item, Integer> missingRequiredItems = getMissingRequiredItems(player);
        if (!missingRequiredItems.isEmpty()) {
            LOGGER.info(missingRequiredItems);
            TextUtils.sendErr(
                    ctx,
                    "blossom.bottling.error.requirements." + missingRequiredItems.size(),
                    missingRequiredItems
                            .entrySet()
                            .stream()
                            .map(e -> new Object[]{
                                    TextUtils.translation(e.getKey().getTranslationKey()),
                                    e.getValue()
                            })
                            .flatMap(Arrays::stream)
                            .toList()
                            .toArray()
            );
            return;
        }

        int count = Math.floorDiv(totalPoints, increment);
        int leftover = totalPoints % increment;

        consumeItems(player, count + (leftover > 0 ? 1 : 0));
        giveOrDropStack(player, create(increment, count));

        if (leftover > 0) {
            giveOrDropStack(player, create(leftover));
        }

        player.addExperience(-totalPoints);
        playSound(player, CONFIG.bottlingSound);

        TextUtils.sendSuccess(ctx, "blossom.bottling.success", totalPoints);
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
