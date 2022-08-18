package dev.codedsakura.blossom.experience_bottling.mixin;

import dev.codedsakura.blossom.experience_bottling.BottledXpUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayerEntity.class)
public class BlossomXpBottleLoreUpdateMixin {

    @Inject(
            method = "playerTick",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/network/packet/s2c/play/ExperienceBarUpdateS2CPacket;<init>(FII)V")
    )
    void BlossomExperienceBottling$playerTickXpChange(CallbackInfo ci) {
        ServerPlayerEntity self = (ServerPlayerEntity) (Object) this;

        for (int i = 0; i < self.getInventory().size(); i++) {
            ItemStack stack = self.getInventory().getStack(i);

            if (!stack.isOf(Items.EXPERIENCE_BOTTLE)) {
                continue;
            }

            if (!stack.hasNbt()) {
                continue;
            }
            assert stack.getNbt() != null;
            if (!stack.getNbt().contains(BottledXpUtils.NBT_KEY)) {
                continue;
            }

            NbtCompound bottleNbt = stack.getNbt();

            BottledXpUtils.updateNbt(self, bottleNbt);

            stack.setNbt(bottleNbt);
        }
    }
}
