package zip.sodium.natrium.mixin;

import net.minecraft.block.*;
import net.minecraft.entity.FallingBlockEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import org.spongepowered.asm.mixin.Mixin;
import zip.sodium.natrium.block.LayeredFallingBlock;

import static net.minecraft.block.SnowBlock.LAYERS;
import static net.minecraft.block.SnowBlock.MAX_LAYERS;

@SuppressWarnings("deprecation")
@Mixin(SnowBlock.class)
public abstract class SnowBlockMixin extends Block implements LandingBlock {
    public SnowBlockMixin(Settings settings) {
        super(settings);
    }

    @Override
    public void onBlockAdded(BlockState state, World world, BlockPos pos, BlockState oldState, boolean notify) {
        world.scheduleBlockTick(pos, this, 2);
    }

    @Override
    public BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState neighborState, WorldAccess world, BlockPos pos, BlockPos neighborPos) {
        world.scheduleBlockTick(pos, this, 2);

        return super.getStateForNeighborUpdate(state, direction, neighborState, world, pos, neighborPos);
    }

    @Override
    public void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        if (!FallingBlock.canFallThrough(world.getBlockState(pos.down())) || pos.getY() < world.getBottomY())
            return;

        FallingBlockEntity.spawnFromBlock(world, pos, state);
    }

    @Override
    public void onDestroyedOnLanding(World world, BlockPos pos, FallingBlockEntity fallingBlockEntity) {
        BlockState state = world.getBlockState(pos);
        if (state.getBlock().getDefaultState() != getDefaultState())
            return;

        int layers = state.get(LAYERS) + fallingBlockEntity.getBlockState().get(LAYERS);
        boolean isOverflowing = layers >= MAX_LAYERS;

        if (isOverflowing) {
            state = Blocks.SNOW_BLOCK.getDefaultState();
            layers -= MAX_LAYERS;
        } else {
            state = state.with(LAYERS, layers);
            layers = 0;
        }

        world.setBlockState(pos, state, Block.NOTIFY_ALL);

        if (layers == 0)
            return;

        LayeredFallingBlock.overflowing(getDefaultState(), world, pos.up(), layers);
    }
}
