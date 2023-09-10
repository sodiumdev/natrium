package zip.sodium.natrium.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.FallingBlock;
import net.minecraft.block.ShapeContext;
import net.minecraft.entity.FallingBlockEntity;
import net.minecraft.entity.ai.pathing.NavigationType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.IntProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;
import org.jetbrains.annotations.Nullable;
import zip.sodium.natrium.NatriumBlockTags;

@SuppressWarnings("deprecation")
public class LayeredFallingBlock extends FallingBlock {
    public static final int MAX_LAYERS = 8;
    public static final IntProperty LAYERS = Properties.LAYERS;
    protected static final VoxelShape[] LAYERS_TO_SHAPE = new VoxelShape[]{VoxelShapes.empty(), Block.createCuboidShape(0.0, 0.0, 0.0, 16.0, 2.0, 16.0), Block.createCuboidShape(0.0, 0.0, 0.0, 16.0, 4.0, 16.0), Block.createCuboidShape(0.0, 0.0, 0.0, 16.0, 6.0, 16.0), Block.createCuboidShape(0.0, 0.0, 0.0, 16.0, 8.0, 16.0), Block.createCuboidShape(0.0, 0.0, 0.0, 16.0, 10.0, 16.0), Block.createCuboidShape(0.0, 0.0, 0.0, 16.0, 12.0, 16.0), Block.createCuboidShape(0.0, 0.0, 0.0, 16.0, 14.0, 16.0), Block.createCuboidShape(0.0, 0.0, 0.0, 16.0, 16.0, 16.0)};
    private final Block parent;

    public LayeredFallingBlock(Block parent, Settings settings) {
        super(settings);

        this.parent = parent;

        setDefaultState(stateManager.getDefaultState().with(LAYERS, 1));
    }

    public final Block getParentBlock() {
        return parent;
    }

    @Override
    public int getColor(BlockState state, BlockView world, BlockPos pos) {
        if (parent instanceof FallingBlock fallingBlock)
            return fallingBlock.getColor(state, world, pos);

        return -16777216;
    }

    @Override
    public boolean canPathfindThrough(BlockState state, BlockView world, BlockPos pos, NavigationType type) {
        if (NavigationType.LAND.equals(type))
            return state.get(LAYERS) < 5;

        return false;
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return LAYERS_TO_SHAPE[state.get(LAYERS)];
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return LAYERS_TO_SHAPE[state.get(LAYERS) - 1];
    }

    @Override
    public VoxelShape getSidesShape(BlockState state, BlockView world, BlockPos pos) {
        return LAYERS_TO_SHAPE[state.get(LAYERS)];
    }

    @Override
    public VoxelShape getCameraCollisionShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return LAYERS_TO_SHAPE[state.get(LAYERS)];
    }

    @Override
    public boolean hasSidedTransparency(BlockState state) {
        return true;
    }

    @Override
    public float getAmbientOcclusionLightLevel(BlockState state, BlockView world, BlockPos pos) {
        return state.get(LAYERS) == MAX_LAYERS ? 0.2f : 1.0f;
    }

    @Override
    public void onDestroyedOnLanding(World world, BlockPos pos, FallingBlockEntity fallingBlockEntity) {
        BlockState state = world.getBlockState(pos);
        if (state.getBlock().getDefaultState() != getDefaultState())
            return;

        int layers = state.get(LAYERS) + fallingBlockEntity.getBlockState().get(LAYERS);
        boolean isOverflowing = layers >= MAX_LAYERS;

        if (isOverflowing) {
            state = parent.getDefaultState();
            layers -= MAX_LAYERS;
        } else {
            state = state.with(LAYERS, layers);
            layers = 0;
        }

        world.setBlockState(pos, state, Block.NOTIFY_ALL);

        if (layers == 0)
            return;

        overflowing(getDefaultState(), world, pos.up(), layers);
    }

    public static void overflowing(final BlockState defaultState, final World world, final BlockPos pos, int overflowingLayerCount) {
        if (!world.getBlockState(pos).isReplaceable())
            return;

        world.setBlockState(pos, defaultState.with(LAYERS, overflowingLayerCount), Block.NOTIFY_ALL);
    }

    @Override
    public boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos) {
        BlockState blockState = world.getBlockState(pos.down());

        return !blockState.isIn(NatriumBlockTags.SAND_LAYER_CANNOT_SURVIVE_ON)
                && (blockState.isIn(NatriumBlockTags.SAND_LAYER_CAN_SURVIVE_ON)
                    || (Block.isFaceFullSquare(blockState.getCollisionShape(world, pos.down()), Direction.UP)
                    || blockState.isOf(this)
                    && blockState.get(LAYERS) == MAX_LAYERS));
    }

    @Override
    public boolean canReplace(BlockState state, ItemPlacementContext context) {
        return context.getStack().isOf(this.asItem())
                && state.get(LAYERS) < MAX_LAYERS
                && (!context.canReplaceExisting() || context.getSide() == Direction.UP);
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        turnIntoBlockIfPossible(state, world, pos);

        return ActionResult.PASS;
    }

    private void turnIntoBlockIfPossible(BlockState state, World world, BlockPos pos) {
        if (state.get(LAYERS) < MAX_LAYERS)
            return;

        world.setBlockState(pos, parent.getDefaultState());
    }

    @Override
    @Nullable
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        BlockState blockState = ctx.getWorld().getBlockState(ctx.getBlockPos());
        if (!blockState.isOf(this))
            return super.getPlacementState(ctx);

        return blockState.with(LAYERS, Math.min(MAX_LAYERS, blockState.get(LAYERS) + 1));
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(LAYERS);
    }
}
