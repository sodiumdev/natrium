package zip.sodium.natrium;

import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import zip.sodium.natrium.entity.CubeEntity;
import zip.sodium.natrium.entity.renderer.CubeEntityRenderer;

public final class NatriumEntities {
    public static final EntityType<CubeEntity> CUBE = Registry.register(
            Registries.ENTITY_TYPE,
            new Identifier(Natrium.MOD_ID, "cube"),
            FabricEntityTypeBuilder.create(SpawnGroup.MISC, CubeEntity::new)
                    .dimensions(EntityDimensions.fixed(1, 1))
                    .build()
    );

    public static void registerEntityRenderers() {
        EntityRendererRegistry.register(CUBE, CubeEntityRenderer::new);
    }
}
