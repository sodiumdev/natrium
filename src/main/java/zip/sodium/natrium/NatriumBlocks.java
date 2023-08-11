package zip.sodium.natrium;

import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.block.*;
import net.minecraft.block.enums.Instrument;
import net.minecraft.block.piston.PistonBehavior;
import net.minecraft.item.BlockItem;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.Identifier;
import zip.sodium.natrium.block.LayeredSandBlock;

public final class NatriumBlocks {
    @SuppressWarnings("deprecation")
    public static final LayeredSandBlock LAYERED_SAND = new LayeredSandBlock(
            false,
            AbstractBlock.Settings.create()
                    .mapColor(MapColor.PALE_YELLOW)
                    .replaceable()
                    .notSolid()
                    .instrument(Instrument.SNARE)
                    .strength(0.4f)
                    .sounds(BlockSoundGroup.SAND)
                    .pistonBehavior(PistonBehavior.DESTROY)
    );

    @SuppressWarnings("deprecation")
    public static final LayeredSandBlock LAYERED_RED_SAND = new LayeredSandBlock(
            true,
            AbstractBlock.Settings.create()
                    .mapColor(MapColor.ORANGE)
                    .replaceable()
                    .notSolid()
                    .instrument(Instrument.SNARE)
                    .strength(0.4f)
                    .sounds(BlockSoundGroup.SAND)
                    .pistonBehavior(PistonBehavior.DESTROY)
    );

    private static void registerBlockWithItem(final String name,
                                              final Block block) {
        Registry.register(Registries.BLOCK,
                new Identifier(Natrium.MOD_ID, name),
                block);
        Registry.register(Registries.ITEM,
                new Identifier(Natrium.MOD_ID, name),
                new BlockItem(block, new FabricItemSettings()));
    }

    static void registerBlocks() {
        registerBlockWithItem("layered_sand", LAYERED_SAND);
        registerBlockWithItem("layered_red_sand", LAYERED_RED_SAND);
    }
}
