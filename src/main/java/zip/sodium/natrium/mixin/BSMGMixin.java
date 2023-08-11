package zip.sodium.natrium.mixin;

import com.google.gson.JsonElement;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.data.client.*;
import net.minecraft.state.property.Properties;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import zip.sodium.natrium.NatriumBlocks;
import zip.sodium.natrium.block.LayeredSandBlock;

import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

@Mixin(BlockStateModelGenerator.class)
public abstract class BSMGMixin {
    @Shadow @Final public BiConsumer<Identifier, Supplier<JsonElement>> modelCollector;

    @Shadow @Final public Consumer<BlockStateSupplier> blockStateCollector;

    @Shadow public abstract void registerParentedItemModel(Block block, Identifier parentModelId);

    @Inject(method = "register()V", at = @At("TAIL"))
    private void natrium$registerLayeredSandModel(CallbackInfo ci) {
        registerLayeredSand(NatriumBlocks.LAYERED_SAND);
        registerLayeredSand(NatriumBlocks.LAYERED_RED_SAND);
    }

    private void registerLayeredSand(LayeredSandBlock block) {
        final Block parentBlock = block.getParentBlock();

        final TextureMap textureMap = TextureMap.all(parentBlock);
        Identifier identifier = Models.CUBE_ALL.upload(block, textureMap, this.modelCollector);
        this.blockStateCollector.accept(VariantsBlockStateSupplier.create(block).coordinate(BlockStateVariantMap.create(Properties.LAYERS).register(height -> BlockStateVariant.create().put(VariantSettings.MODEL, height < LayeredSandBlock.MAX_LAYERS ? ModelIds.getBlockSubModelId(block, "_height" + height * 2) : identifier))));
        this.registerParentedItemModel(block, ModelIds.getBlockSubModelId(block, "_height2"));
    }
}
