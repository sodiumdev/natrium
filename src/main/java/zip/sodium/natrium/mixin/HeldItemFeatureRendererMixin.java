package zip.sodium.natrium.mixin;

import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.feature.HeldItemFeatureRenderer;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.render.entity.model.ModelWithArms;
import net.minecraft.client.render.item.HeldItemRenderer;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Arm;
import net.minecraft.util.UseAction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import org.spongepowered.asm.mixin.*;

@Mixin(HeldItemFeatureRenderer.class)
public abstract class HeldItemFeatureRendererMixin<T extends LivingEntity, M extends EntityModel<T> & ModelWithArms>
        extends FeatureRenderer<T, M> {
    @Shadow @Final private HeldItemRenderer heldItemRenderer;

    @Unique
    private static final float TAU = 2 * (float) Math.PI;

    public HeldItemFeatureRendererMixin(FeatureRendererContext<T, M> context) {
        super(context);
    }

    /**
     * @author Sodium.zip
     * @reason Optimize code and redirect render item calls
     */
    @Overwrite
    public void render(MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int light, T livingEntity, float f, float g, float tickDelta, float j, float k, float l) {
        final boolean isLeftArm = livingEntity.getMainArm() == Arm.RIGHT;

        ItemStack itemStackLeft = isLeftArm ? livingEntity.getOffHandStack() : livingEntity.getMainHandStack();
        ItemStack itemStackRight = isLeftArm ? livingEntity.getMainHandStack() : livingEntity.getOffHandStack();
        if (itemStackLeft.isEmpty()
                && itemStackRight.isEmpty())
            return;

        matrixStack.push();

        if (this.getContextModel().child) {
            matrixStack.translate(0.0f, 0.75f, 0.0f);
            matrixStack.scale(0.5f, 0.5f, 0.5f);
        }

        renderItem(livingEntity, itemStackRight, ModelTransformationMode.THIRD_PERSON_RIGHT_HAND, Arm.RIGHT, matrixStack, vertexConsumerProvider, light, tickDelta);
        renderItem(livingEntity, itemStackLeft, ModelTransformationMode.THIRD_PERSON_LEFT_HAND, Arm.LEFT, matrixStack, vertexConsumerProvider, light, tickDelta);

        matrixStack.pop();
    }

    @Unique
    private void renderItem(LivingEntity entity, ItemStack stack, ModelTransformationMode transformationMode, Arm arm, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, float tickDelta) {
        if (stack.isEmpty())
            return;

        matrices.push();
        getContextModel().setArmAngle(arm, matrices);

        boolean isLeftArm = arm == Arm.LEFT;

        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-90.0f));
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180.0f));
        matrices.translate(isLeftArm ? -0.0625f : 0.0625f, 0.125f, -0.625f);

        if (stack.getUseAction() == UseAction.BRUSH)
            applyBrushTransformations(matrices, entity, tickDelta);

        heldItemRenderer.renderItem(entity, stack, transformationMode, isLeftArm, matrices, vertexConsumers, light);

        matrices.pop();
    }

    @Unique
    private void applyBrushTransformations(final MatrixStack matrices,
                                           final LivingEntity entity,
                                           final float tickDelta) {
        if (!entity.isUsingItem() ||
                entity.getItemUseTimeLeft() <= 0)
            return;

        matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(75.0f * MathHelper.cos(TAU * (1.0f - ((entity.getItemUseTimeLeft() % 10) - tickDelta + 1.0f) * 0.1f)) - 15.0f));
    }
}
