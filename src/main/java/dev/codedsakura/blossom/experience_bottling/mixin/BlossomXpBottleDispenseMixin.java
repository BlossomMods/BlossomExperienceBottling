package dev.codedsakura.blossom.experience_bottling.mixin;

import dev.codedsakura.blossom.experience_bottling.BottledXpEntity;
import dev.codedsakura.blossom.experience_bottling.BottledXpUtils;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Util;
import net.minecraft.util.math.Position;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;


@Mixin(targets = "net/minecraft/block/dispenser/DispenserBehavior$24")
public class BlossomXpBottleDispenseMixin {
    @Inject(method = "createProjectile", at = @At("HEAD"), cancellable = true)
    void BlossomExperienceBottling$dispenseBottledXp(World world, Position position, ItemStack stack, CallbackInfoReturnable<ProjectileEntity> cir) {
        if (BottledXpUtils.isBottledXp(stack)) {
            int amount = BottledXpUtils.getBottledXpAmount(stack);

            cir.setReturnValue(
                    Util.make(
                            new BottledXpEntity(world, position.getX(), position.getY(), position.getZ(), amount),
                            entity -> entity.setItem(stack)
                    )
            );
        }
    }
}
