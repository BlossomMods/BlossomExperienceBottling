package dev.codedsakura.blossom.experience_bottling.mixin;

import dev.codedsakura.blossom.experience_bottling.BottledXpUtils;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static dev.codedsakura.blossom.experience_bottling.BlossomExperienceBottling.CONFIG;

@Mixin(PlayerEntity.class)
public class PlayerXpDropOverride {

    @Inject(
            method = "getXpToDrop",
            at = @At(
                    value = "RETURN",
                    ordinal = 1
            ),
            cancellable = true
    )
    void BlossomExperienceBottling$overrideXpToDrop(CallbackInfoReturnable<Integer> cir) {
        if (CONFIG.xpDropOnDeathMultiplier < 0) {
            return;
        }

        int xpPoints = BottledXpUtils.getPlayerXpAsPoints((PlayerEntity) (Object) this);
        int resultingPoints = (int) ((float) xpPoints * CONFIG.xpDropOnDeathMultiplier);

        cir.setReturnValue(Math.min(xpPoints, resultingPoints));
    }
}
