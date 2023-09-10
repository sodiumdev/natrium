package zip.sodium.natrium.block;

import dev.lazurite.rayon.impl.bullet.collision.space.cache.ChunkCache;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

public interface Brushable {
    default boolean natrium$brush(World world, BlockPos pos, PlayerEntity playerEntity, Direction side, long worldTime) {
        return false;
    }

    default SoundEvent natrium$getBrushingSound() {
        return null;
    }
}
