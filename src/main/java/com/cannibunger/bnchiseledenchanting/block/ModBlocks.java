package com.cannibunger.bnchiseledenchanting.block;

import com.cannibunger.bnchiseledenchanting.BNChiseledEnchanting;
import com.cannibunger.bnchiseledenchanting.item.ModItems;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModBlocks {
    // "enables" registering blocks from the mod
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(BNChiseledEnchanting.MODID);

    // data for enchanting inscriber
    public static final DeferredBlock<Block> ENCHANTING_INSCRIBER = registerBlock(
            "enchanting_inscriber",
            () -> new Block(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_RED)
                    .instrument(NoteBlockInstrument.BASEDRUM)
                    .requiresCorrectToolForDrops()
                    .lightLevel(p_152692_ -> 7)
                    .strength(5f, 1200.0F)
                    .sound(SoundType.ANVIL)
            )
    );

    // register block AND item
    private static <T extends Block> DeferredBlock<T> registerBlock(String name, Supplier<T> block) {
        DeferredBlock<T> toReturn = BLOCKS.register(name, block);
        registerBlockItem(name, toReturn);
        return toReturn;
    }

    // registers block item
    private static <T extends Block> void registerBlockItem(String name, DeferredBlock<T> block) {
        ModItems.ITEMS.register(name, () -> new BlockItem(block.get(), new Item.Properties()));
    }

    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
    }

}
