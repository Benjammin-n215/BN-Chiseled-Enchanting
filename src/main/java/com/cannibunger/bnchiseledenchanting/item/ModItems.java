package com.cannibunger.bnchiseledenchanting.item;

import com.cannibunger.bnchiseledenchanting.BNChiseledEnchanting;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(BNChiseledEnchanting.MODID);

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}
