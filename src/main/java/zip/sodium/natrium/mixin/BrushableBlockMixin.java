package zip.sodium.natrium.mixin;

import net.minecraft.block.BrushableBlock;
import net.minecraft.sound.SoundEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import zip.sodium.natrium.block.Brushable;

@Mixin(BrushableBlock.class)
public abstract class BrushableBlockMixin implements Brushable {
    @Shadow public abstract SoundEvent getBrushingSound();

    @Override
    public SoundEvent natrium$getBrushingSound() {
        return getBrushingSound();
    }
}
