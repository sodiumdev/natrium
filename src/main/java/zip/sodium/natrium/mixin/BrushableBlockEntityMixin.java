package zip.sodium.natrium.mixin;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.entity.BrushableBlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import zip.sodium.natrium.block.Brushable;

@Mixin(BrushableBlockEntity.class)
public abstract class BrushableBlockEntityMixin extends BlockEntity implements Brushable {
    public BrushableBlockEntityMixin(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Shadow public abstract boolean brush(long worldTime, PlayerEntity player, Direction hitDirection);

    @Unique
    @Override
    public SoundEvent natrium$getBrushingSound() {
        if (getCachedState().getBlock() instanceof Brushable brushable)
            return brushable.natrium$getBrushingSound();

        return null;
    }

    @Override
    public boolean natrium$brush(World world, BlockPos pos, PlayerEntity playerEntity, Direction side, long worldTime) {
        return brush(worldTime, playerEntity, side);
    }
}
