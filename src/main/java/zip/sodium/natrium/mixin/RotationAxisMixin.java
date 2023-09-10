package zip.sodium.natrium.mixin;

import net.minecraft.util.math.MathConstants;
import net.minecraft.util.math.RotationAxis;
import org.joml.Quaternionf;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(RotationAxis.class)
public interface RotationAxisMixin {
    @Shadow Quaternionf rotation(float var1);

    /**
     * @author Sodium.zip
     * @reason Optimization
     */
    @Overwrite
    default Quaternionf rotationDegrees(float deg) {
        return rotation(deg * MathConstants.RADIANS_PER_DEGREE);
    }
}
