package zip.sodium.natrium.entity.renderer;

import com.jme3.math.Quaternion;
import dev.lazurite.rayon.impl.bullet.math.Convert;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.BlockRenderManager;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.random.Random;
import zip.sodium.natrium.entity.CubeEntity;

public class CubeEntityRenderer extends EntityRenderer<CubeEntity> {
    private final BlockRenderManager blockRenderManager;

    public CubeEntityRenderer(EntityRendererFactory.Context ctx) {
        super(ctx);

        blockRenderManager = ctx.getBlockRenderManager();
    }

    @Override
    public void render(final CubeEntity entity, float yaw, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light) {
        matrices.push();
        matrices.multiply(Convert.toMinecraft(entity.getPhysicsRotation(new Quaternion(), tickDelta)));
        matrices.translate(-0.5, -0.5, -0.5);

        blockRenderManager.renderBlockAsEntity(entity.getBlockState(), matrices, vertexConsumers, light, OverlayTexture.DEFAULT_UV);

        matrices.pop();
    }

    @Override
    public Identifier getTexture(CubeEntity entity) {
        return null;
    }
}
