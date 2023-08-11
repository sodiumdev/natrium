package zip.sodium.natrium;

import net.minecraft.block.Block;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;

public final class NatriumBlockTags {
    public static final TagKey<Block> SAND_LAYER_CANNOT_SURVIVE_ON = of("sand_layer_cannot_survive_on");
    public static final TagKey<Block> SAND_LAYER_CAN_SURVIVE_ON = of("sand_layer_can_survive_on");

    private static TagKey<Block> of(String id) {
        return TagKey.of(RegistryKeys.BLOCK, new Identifier(id));
    }
}
