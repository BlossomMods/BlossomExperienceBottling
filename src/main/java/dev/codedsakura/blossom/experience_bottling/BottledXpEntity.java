package dev.codedsakura.blossom.experience_bottling;

import net.minecraft.entity.ExperienceOrbEntity;
import net.minecraft.entity.projectile.thrown.ExperienceBottleEntity;
import net.minecraft.potion.PotionUtil;
import net.minecraft.potion.Potions;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.hit.HitResult;
import net.minecraft.world.World;

public class BottledXpEntity extends ExperienceBottleEntity {
    private final int xpAmount;

    public BottledXpEntity(World world, double x, double y, double z, int xpAmount) {
        super(world, x, y, z);
        this.xpAmount = xpAmount;
    }

    protected void onCollision(HitResult hitResult) {
        if (this.world instanceof ServerWorld) {
            this.world.syncWorldEvent(2002, this.getBlockPos(), PotionUtil.getColor(Potions.WATER));
            ExperienceOrbEntity.spawn((ServerWorld) this.world, this.getPos(), this.xpAmount);
            this.discard();
        }
    }
}
