package zip.sodium.natrium;

import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.block.*;
import net.minecraft.block.enums.Instrument;
import net.minecraft.block.piston.PistonBehavior;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.Identifier;
import zip.sodium.natrium.block.LayeredFallingBlock;

import java.util.ArrayList;
import java.util.List;

public final class NatriumBlocks {
    @SuppressWarnings("deprecation")
    public static final LayeredFallingBlock LAYERED_DIRT = new LayeredFallingBlock(
            Blocks.DIRT,
            AbstractBlock.Settings.create()
                    .mapColor(MapColor.DIRT_BROWN)
                    .replaceable()
                    .notSolid()
                    .dropsNothing()
                    .strength(0.4f)
                    .sounds(BlockSoundGroup.GRAVEL)
                    .pistonBehavior(PistonBehavior.DESTROY)
    );

    @SuppressWarnings("deprecation")
    public static final LayeredFallingBlock LAYERED_GRAVEL = new LayeredFallingBlock(
            Blocks.GRAVEL,
            AbstractBlock.Settings.create()
                    .mapColor(MapColor.STONE_GRAY)
                    .replaceable()
                    .notSolid()
                    .dropsNothing()
                    .instrument(Instrument.SNARE)
                    .strength(0.5f)
                    .sounds(BlockSoundGroup.GRAVEL)
                    .pistonBehavior(PistonBehavior.DESTROY)
    );

    @SuppressWarnings("deprecation")
    public static final LayeredFallingBlock LAYERED_SAND = new LayeredFallingBlock(
            Blocks.SAND,
            AbstractBlock.Settings.create()
                    .mapColor(MapColor.PALE_YELLOW)
                    .replaceable()
                    .notSolid()
                    .dropsNothing()
                    .instrument(Instrument.SNARE)
                    .strength(0.4f)
                    .sounds(BlockSoundGroup.SAND)
                    .pistonBehavior(PistonBehavior.DESTROY)
    );

    @SuppressWarnings("deprecation")
    public static final LayeredFallingBlock LAYERED_RED_SAND = new LayeredFallingBlock(
            Blocks.RED_SAND,
            AbstractBlock.Settings.create()
                    .mapColor(MapColor.ORANGE)
                    .replaceable()
                    .notSolid()
                    .dropsNothing()
                    .instrument(Instrument.SNARE)
                    .strength(0.4f)
                    .sounds(BlockSoundGroup.SAND)
                    .pistonBehavior(PistonBehavior.DESTROY)
    );

    private static final List<Item> REGISTERED_ITEMS = new ArrayList<>(4);

    private static void registerBlockWithItem(final String name,
                                              final Block block) {
        Registry.register(Registries.BLOCK,
                new Identifier(Natrium.MOD_ID, name),
                block);
        REGISTERED_ITEMS.add(
                Registry.register(Registries.ITEM,
                        new Identifier(Natrium.MOD_ID, name),
                        new BlockItem(block, new FabricItemSettings()))
        );
    }

    static void registerBlocks() {
        registerBlockWithItem("layered_dirt", LAYERED_DIRT);
        registerBlockWithItem("layered_gravel", LAYERED_GRAVEL);
        registerBlockWithItem("layered_sand", LAYERED_SAND);
        registerBlockWithItem("layered_red_sand", LAYERED_RED_SAND);

        ItemGroupEvents.modifyEntriesEvent(ItemGroups.BUILDING_BLOCKS).register(content -> {
            for (Item item : REGISTERED_ITEMS)
                content.add(item);
        });
    }
}
