package com.cannibunger.bnchiseledenchanting.block.entity;

import com.cannibunger.bnchiseledenchanting.BNChiseledEnchanting;
import com.cannibunger.bnchiseledenchanting.block.ModBlocks;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import java.util.function.Supplier;

public class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(BuiltInRegistries.BLOCK_ENTITY_TYPE, BNChiseledEnchanting.MODID);


    public static final Supplier<BlockEntityType<EnchantingInscriberBlockEntity>> ENCHANTING_INSCRIBER_BE = BLOCK_ENTITIES.register("enchanting_inscriber_be",
            () -> BlockEntityType.Builder.of(EnchantingInscriberBlockEntity::new, ModBlocks.ENCHANTING_INSCRIBER.get()).build(null));

    public static void register(IEventBus eventBus) {
        BLOCK_ENTITIES.register(eventBus);
    }
}
