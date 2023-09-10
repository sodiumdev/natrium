package zip.sodium.natrium.mixin;

import net.minecraft.block.BlockState;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BrushItem;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Arm;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import zip.sodium.natrium.block.Brushable;

import java.util.Objects;

@Mixin(BrushItem.class)
public abstract class BrushItemMixin {
    @Shadow public abstract void addDustParticles(World world, BlockHitResult hitResult, BlockState state, Vec3d userRotation, Arm arm);

    @Shadow public abstract int getMaxUseTime(ItemStack stack);

    @Shadow protected abstract HitResult getHitResult(LivingEntity user);

    /**
     * @author Sodium.zip
     * @reason Optimize brushing logic and add support for layered blocks
     */
    @Overwrite
    public void usageTick(World world, LivingEntity user, ItemStack stack, int remainingUseTicks) {
        if (remainingUseTicks < 0
                || !(user instanceof PlayerEntity playerEntity)
                || !(getHitResult(user) instanceof BlockHitResult blockHitResult)) {
            user.stopUsingItem();
            return;
        }

        if ((getMaxUseTime(stack) - remainingUseTicks + 1) % 10 != 5)
            return;

        final BlockPos blockPos = blockHitResult.getBlockPos();
        final BlockState blockState = world.getBlockState(blockPos);
        final Arm arm = user.getActiveHand() == Hand.MAIN_HAND ? playerEntity.getMainArm() : playerEntity.getMainArm().getOpposite();

        addDustParticles(world, blockHitResult, blockState, user.getRotationVec(0.0f), arm);

        final Brushable brushable;
        if (world.getBlockEntity(blockPos) instanceof Brushable blockEntity)
            brushable = blockEntity;
        else if (blockState.getBlock() instanceof Brushable brushableBlock)
            brushable = brushableBlock;
        else brushable = null;

        final SoundEvent soundEvent;
        if (brushable != null) {
            soundEvent = brushable.natrium$getBrushingSound();
        } else soundEvent = SoundEvents.ITEM_BRUSH_BRUSHING_GENERIC;

        world.playSound(playerEntity, blockPos, Objects.requireNonNullElse(soundEvent, SoundEvents.ITEM_BRUSH_BRUSHING_GENERIC), SoundCategory.BLOCKS);

        if (world.isClient()
                || brushable == null
                || !brushable.natrium$brush(world, blockPos, playerEntity, blockHitResult.getSide(), world.getTime()))
            return;

        final EquipmentSlot equipmentSlot = arm == Arm.LEFT ? EquipmentSlot.OFFHAND : EquipmentSlot.MAINHAND;
        stack.damage(1, user, userx -> userx.sendEquipmentBreakStatus(equipmentSlot));
    }
}
