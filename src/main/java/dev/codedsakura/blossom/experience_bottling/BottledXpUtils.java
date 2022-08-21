package dev.codedsakura.blossom.experience_bottling;

import dev.codedsakura.blossom.lib.text.TextUtils;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;

import static dev.codedsakura.blossom.experience_bottling.BlossomExperienceBottling.CONFIG;

public class BottledXpUtils {
    public static final String NBT_KEY = "blossom:bottled-experience";

    public static int levelToTotalPoints(int n) {
        if (n < 15) {
            return n * n + n * 6;
        } else if (n < 30) {
            return ((5 * n * n - 81 * n) >> 1) + 360;
        } else {
            return ((9 * n * n - 325 * n) >> 1) + 2220;
        }
    }

    public static int getPlayerXpAsPoints(PlayerEntity player) {
        return levelToTotalPoints(player.experienceLevel) + (int) (player.experienceProgress * player.getNextLevelExperience());
    }


    private static NbtList makeLore(int amount) {
        NbtList lore = new NbtList();

        lore.add(NbtString.of(Text.Serializer.toJson(
                TextUtils.translation("blossom.bottling.bottle.description", amount)
                        .styled(style -> style
                                .withItalic(false)
                                .withColor(TextColor.parse(CONFIG.itemColors.description))))
        ));

        return lore;
    }


    public static ItemStack create(int amount, int bottleCount) {
        NbtCompound display = new NbtCompound();
        display.put(ItemStack.NAME_KEY, NbtString.of(Text.Serializer.toJson(
                TextUtils.translation("blossom.bottling.bottle.title")
                        .styled(style -> style
                                .withItalic(false)
                                .withColor(TextColor.parse(CONFIG.itemColors.title)))
        )));
        display.put(ItemStack.LORE_KEY, makeLore(amount));

        NbtCompound bottleData = new NbtCompound();
        bottleData.putLong(NBT_KEY, amount);
        bottleData.put(ItemStack.DISPLAY_KEY, display);

        ItemStack bottle = new ItemStack(Items.EXPERIENCE_BOTTLE, bottleCount);
        bottle.setNbt(bottleData);

        return bottle;
    }

    public static ItemStack create(int amount) {
        return create(amount, 1);
    }


    public static boolean isBottledXp(ItemStack itemStack) {
        NbtCompound nbt = itemStack.getNbt();
        return nbt != null && nbt.contains(BottledXpUtils.NBT_KEY);
    }

    public static int getBottledXpAmount(ItemStack itemStack) {
        if (!isBottledXp(itemStack)) {
            return 0;
        }

        NbtCompound nbt = itemStack.getNbt();
        assert nbt != null;
        return nbt.getInt(BottledXpUtils.NBT_KEY);
    }
}
