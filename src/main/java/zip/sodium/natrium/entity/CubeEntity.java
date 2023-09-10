package zip.sodium.natrium.entity;

import com.jme3.math.Vector3f;
import dev.lazurite.rayon.api.EntityPhysicsElement;
import dev.lazurite.rayon.impl.bullet.collision.body.ElementRigidBody;
import dev.lazurite.rayon.impl.bullet.collision.body.EntityRigidBody;
import dev.lazurite.rayon.impl.bullet.collision.body.shape.MinecraftShape;
import dev.lazurite.rayon.impl.bullet.collision.space.MinecraftSpace;
import dev.lazurite.rayon.impl.bullet.math.Convert;
import dev.lazurite.rayon.impl.event.network.EntityNetworking;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.Ownable;
import net.minecraft.entity.decoration.DisplayEntity;
import net.minecraft.entity.decoration.InteractionEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.nbt.NbtOps;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.IdentityHashMap;
import java.util.UUID;

public class CubeEntity extends Entity implements Holdable, Ownable {
    private EntityRigidBody rigidbody;

    private static final IdentityHashMap<BlockState, MinecraftShape.Convex> SHAPE_MAP = new IdentityHashMap<>();
    private static final MinecraftShape.Convex DEFAULT_SHAPE = MinecraftShape.convex(new Box(-0.5, -0.5, -0.5, 0.5, 0.5, 0.5));

    private UUID holder = null;

    private BlockState state;

    public CubeEntity(final EntityType<CubeEntity> type, final World world) {
        super(type, world);

        state = Blocks.DIRT.getDefaultState();

        intersectionChecked = true;
        refreshPosition();

        rigidbody = new EntityRigidBody(this, MinecraftSpace.get(world), MinecraftShape.box(getBoundingBox()));
        if (!state.isOpaqueFullCube(world, BlockPos.ORIGIN))
            rigidbody.setCollisionShape(createShape());

        rigidbody.setBuoyancyType(ElementRigidBody.BuoyancyType.WATER);
    }

    @Override
    public MinecraftShape.Convex createShape() {
        if (state == null
                || state.isFullCube(getWorld(), BlockPos.ORIGIN))
            return DEFAULT_SHAPE;

        return SHAPE_MAP.computeIfAbsent(state, statex -> {
            Box box;
            VoxelShape voxelShape = statex.getCollisionShape(getWorld(), BlockPos.ORIGIN);
            if (voxelShape.isEmpty()) {
                box = statex.getOutlineShape(getWorld(), BlockPos.ORIGIN).getBoundingBox();
            } else box = voxelShape.getBoundingBox();

            return MinecraftShape.convex(box.shrink(box.getXLength() * 0.15, box.getYLength() * 0.15, box.getZLength() * 0.15));
        });
    }

    @Override
    protected Box calculateBoundingBox() {
        if (rigidbody == null)
            return super.calculateBoundingBox();

        return rigidbody.getCurrentMinecraftBoundingBox();
    }

    @Override
    protected void initDataTracker() {}

    @Override
    public void tick() {
        if (getWorld().isClient)
            return;
        if (!isBeingHeld())
            return;

        rigidbody.setPhysicsLocation(Convert.toBullet(getHolder().raycast(3, 0, false).getPos()));
        rigidbody.setLinearVelocity(rigidbody.getFrame().getLocationDelta(new Vector3f()).mult(5));

        EntityNetworking.sendMovement(rigidbody);
    }

    @Override
    public boolean handleAttack(Entity attacker) {
        if (!getWorld().isClient)
            toggleHolder((PlayerEntity) attacker);

        return false;
    }

    @Override
    public ActionResult interactAt(PlayerEntity player, Vec3d hitPos, Hand hand) {
        return interact(player, hand);
    }

    @Override
    public ActionResult interact(PlayerEntity player, Hand hand) {
        if (!getWorld().isClient)
            toggleHolder(player);

        return ActionResult.PASS;
    }

    @Override
    public boolean canHit() {
        return true;
    }

    @Override
    public boolean canBeHitByProjectile() {
        return true;
    }

    @Override
    public boolean canUsePortals() {
        return true;
    }

    @Override
    public boolean isPushable() {
        return true;
    }

    @Override
    public void pushAwayFrom(Entity entity) {
        final var box = rigidbody.getCurrentBoundingBox();
        final var location = rigidbody.getPhysicsLocation(new Vector3f()).subtract(new Vector3f(0, -box.getYExtent(), 0));
        final var mass = rigidbody.getMass();

        final var vanillaBox = rigidbody.getCurrentMinecraftBoundingBox();

        final var entityPos = Convert.toBullet(entity.getPos().add(0, entity.getBoundingBox().getYLength(), 0));
        final var normal = location.subtract(entityPos).multLocal(new Vector3f(1, 0, 1)).normalize();

        final var intersection = entity.getBoundingBox().intersection(vanillaBox);
        final var force = normal.clone()
                .multLocal((float) intersection.getAverageSideLength() / (float) vanillaBox.getAverageSideLength())
                .multLocal(mass)
                .multLocal(new Vector3f(1, 0, 1));
        rigidbody.applyCentralImpulse(force);
    }

    @Override
    protected void readCustomDataFromNbt(NbtCompound nbt) {
        nbt.put("BlockData", NbtHelper.fromBlockState(getBlockState()));
        if (holder != null)
            nbt.putUuid("Holder", holder);
    }

    @Override
    protected void writeCustomDataToNbt(NbtCompound nbt) {
        state = BlockState.CODEC.parse(NbtOps.INSTANCE, nbt.get("BlockData"))
                .result().orElseGet(Blocks.DIRT::getDefaultState);
        holder = nbt.containsUuid("Holder") ? nbt.getUuid("Holder") : null;
    }

    public BlockState getBlockState() {
        return state;
    }

    @Override
    public @Nullable EntityRigidBody getRigidBody() {
        return rigidbody;
    }

    @Override
    public @Nullable PlayerEntity getHolder() {
        if (holder == null)
            return null;

        return getWorld().getPlayerByUuid(holder);
    }

    @Override
    public void toggleHolder(PlayerEntity player) {
        if (isBeingHeld() && holder.equals(player.getUuid()))
            setHolder(null);
        else setHolder(player);
    }

    @Override
    public void setHolder(@Nullable PlayerEntity holder) {
        if (holder == null) {
            this.holder = null;
        } else this.holder = holder.getUuid();
    }

    @Override
    public boolean isBeingHeld() {
        return holder != null;
    }

    @Nullable
    @Override
    public Entity getOwner() {
        return getHolder();
    }
}
