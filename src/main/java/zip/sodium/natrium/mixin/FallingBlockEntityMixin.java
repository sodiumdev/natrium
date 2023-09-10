package zip.sodium.natrium.mixin;

import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.FallingBlockEntity;
import net.minecraft.entity.MovementType;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.AutomaticItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.packet.s2c.play.BlockUpdateS2CPacket;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.property.Properties;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameRules;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.*;
import zip.sodium.natrium.block.LayeredFallingBlock;

import java.util.Objects;

@Mixin(FallingBlockEntity.class)
public abstract class FallingBlockEntityMixin extends Entity {
    @Shadow private BlockState block;

    @Shadow public int timeFalling;

    @Shadow private boolean destroyedOnLanding;

    @Shadow @Nullable public NbtCompound blockEntityData;

    @Shadow public abstract void onDestroyedOnLanding(Block block, BlockPos pos);

    @Shadow public boolean dropItem;

    public FallingBlockEntityMixin(EntityType<?> type, World world) {
        super(type, world);
    }

    @Unique
    private void destroy(final Block block, final BlockPos blockPos, boolean dropItem) {
        discard();
        if (!shouldDropItem())
            return;

        onDestroyedOnLanding(block, blockPos);
        if (dropItem && !(block instanceof LayeredFallingBlock))
            dropItem(block);
    }

    @Unique
    private void destroy(final Block block, final BlockPos blockPos) {
        destroy(block, blockPos, false);
    }

    @Unique
    private void handleFall() {
        final Block block = this.block.getBlock();

        BlockHitResult blockHitResult;
        BlockPos blockPos = getBlockPos();
        boolean bl = this.block.getBlock() instanceof ConcretePowderBlock;
        boolean bl2 = bl && getWorld().getFluidState(blockPos).isIn(FluidTags.WATER);
        double d = getVelocity().lengthSquared();
        if (bl && d > 1.0
                && (blockHitResult = getWorld().raycast(
                        new RaycastContext(
                                new Vec3d(prevX, prevY, prevZ),
                                getPos(),
                                RaycastContext.ShapeType.COLLIDER,
                                RaycastContext.FluidHandling.SOURCE_ONLY,
                                this
                        ))).getType() != HitResult.Type.MISS
                && getWorld().getFluidState(blockHitResult.getBlockPos()).isIn(FluidTags.WATER)) {
            blockPos = blockHitResult.getBlockPos();
            bl2 = true;
        }

        if (isOnGround() || bl2) {
            BlockState blockState = getWorld().getBlockState(blockPos);
            setVelocity(getVelocity().multiply(0.7, -0.5, 0.7));
            if (blockState.isOf(Blocks.MOVING_PISTON))
                return;
            if (destroyedOnLanding) {
                destroy(block, blockPos);

                return;
            }

            boolean bl3 = blockState.canReplace(new AutomaticItemPlacementContext(getWorld(), blockPos, Direction.DOWN, ItemStack.EMPTY, Direction.UP));
            boolean bl4 = FallingBlock.canFallThrough(getWorld().getBlockState(blockPos.down())) && (!bl || !bl2);
            boolean bl5 = this.block.canPlaceAt(getWorld(), blockPos) && !bl4;

            if (bl3 && bl5) {
                if (this.block.contains(Properties.WATERLOGGED) && getWorld().getFluidState(blockPos).getFluid() == Fluids.WATER)
                    this.block = this.block.with(Properties.WATERLOGGED, true);

                if (getWorld().setBlockState(blockPos, this.block, Block.NOTIFY_ALL)) {
                    BlockEntity blockEntity;
                    ((ServerWorld) getWorld()).getChunkManager().threadedAnvilChunkStorage.sendToOtherNearbyPlayers(this, new BlockUpdateS2CPacket(blockPos, getWorld().getBlockState(blockPos)));

                    discard();
                    if (block instanceof LandingBlock)
                        ((LandingBlock) block).onLanding(getWorld(), blockPos, this.block, blockState, (FallingBlockEntity) ((Object) this));
                    if (blockEntityData == null
                            || !this.block.hasBlockEntity()
                            || (blockEntity = getWorld().getBlockEntity(blockPos)) == null)
                        return;

                    final NbtCompound nbtCompound = blockEntity.createNbt();
                    for (String key : blockEntityData.getKeys())
                        nbtCompound.put(key, Objects.requireNonNull(blockEntityData.get(key)).copy());

                    blockEntity.readNbt(nbtCompound);
                    blockEntity.markDirty();

                    return;
                }
            }

            destroy(block, blockPos, true);

            return;
        }

        if (getWorld().isClient
                || (timeFalling <= 100
                    || blockPos.getY() > getWorld().getBottomY()
                    && blockPos.getY() <= getWorld().getTopY())
                    && timeFalling <= 600)
            return;
        if (shouldDropItem())
            dropItem(block);

        discard();
    }

    @Unique
    private boolean shouldDropItem() {
        return dropItem && getWorld().getGameRules().getBoolean(GameRules.DO_ENTITY_DROPS);
    }

    /**
     * @author Sodium.zip
     * @reason Optimize falling block entity logic
     */
    @Overwrite
    public void tick() {
        if (block.isAir()) {
            this.discard();
            return;
        }

        ++timeFalling;
        if (!hasNoGravity())
            setVelocity(getVelocity().add(0.0, -0.04, 0.0));

        move(MovementType.SELF, getVelocity());
        if (!getWorld().isClient)
            handleFall();

        setVelocity(getVelocity().multiply(0.98));
    }
}
