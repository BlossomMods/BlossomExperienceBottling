package dev.codedsakura.blossom.experience_bottling.mixin;

import dev.codedsakura.blossom.experience_bottling.BottledXpUtils;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ExperienceBottleItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static dev.codedsakura.blossom.experience_bottling.BlossomExperienceBottling.CONFIG;
import static dev.codedsakura.blossom.experience_bottling.BlossomExperienceBottling.LOGGER;

@Mixin(ExperienceBottleItem.class)
public class BlossomXpBottleUseMixin {

    @Inject(method = "use", at = @At("HEAD"), cancellable = true)
    void BlossomExperienceBottling$storedXpBottleUse(World world, PlayerEntity player, Hand hand, CallbackInfoReturnable<TypedActionResult<ItemStack>> cir) {
        LOGGER.info("A");
        if (world.isClient) {
            return;
        }

        LOGGER.info("B");

        ItemStack itemStack = player.getStackInHand(hand);
        NbtCompound nbt = itemStack.getNbt();
        if (nbt != null && nbt.contains(BottledXpUtils.NBT_KEY)) {
            int amount = nbt.getInt(BottledXpUtils.NBT_KEY);
            player.addExperience(amount);

            itemStack.decrement(1);

            LOGGER.info("checking to play levelup...");
            if (CONFIG.usageSound != null) {
                LOGGER.info("attempting to play levelup...");
                player.playSound(
                        new SoundEvent(Identifier.tryParse(CONFIG.usageSound.identifier)),
                        SoundCategory.PLAYERS,
                        CONFIG.usageSound.volume,
                        CONFIG.usageSound.pitch
                );
            }

            cir.setReturnValue(
                    TypedActionResult.success(itemStack, false)
            );
        }
    }
}
