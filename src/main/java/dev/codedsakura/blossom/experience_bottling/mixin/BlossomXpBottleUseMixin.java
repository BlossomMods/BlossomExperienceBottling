package dev.codedsakura.blossom.experience_bottling.mixin;

import dev.codedsakura.blossom.experience_bottling.BlossomExperienceBottling;
import dev.codedsakura.blossom.experience_bottling.BottledXpUtils;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ExperienceBottleItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static dev.codedsakura.blossom.experience_bottling.BlossomExperienceBottling.CONFIG;

@Mixin(ExperienceBottleItem.class)
public class BlossomXpBottleUseMixin {

    @Inject(method = "use", at = @At("HEAD"), cancellable = true)
    void BlossomExperienceBottling$storedXpBottleUse(World world, PlayerEntity player, Hand hand, CallbackInfoReturnable<TypedActionResult<ItemStack>> cir) {
        if (world.isClient) {
            return;
        }

        ItemStack itemStack = player.getStackInHand(hand);
        if (BottledXpUtils.isBottledXp(itemStack)) {
            int amount = BottledXpUtils.getBottledXpAmount(itemStack);
            player.addExperience(amount);

            itemStack.decrement(1);
            BlossomExperienceBottling.returnItems(player);

            BlossomExperienceBottling.playSound(player, CONFIG.usageSound);

            cir.setReturnValue(
                    TypedActionResult.success(itemStack, false)
            );
        }
    }
}
