package com.cannibunger.bnchiseledenchanting.screen;

import com.cannibunger.bnchiseledenchanting.BNChiseledEnchanting;
import com.cannibunger.bnchiseledenchanting.block.entity.EnchantingInscriberBlockEntity;
import com.cannibunger.bnchiseledenchanting.screen.custom.EnchantingInscriberMenu;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.enchantment.Enchantment;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;


public class ModMenuTypes {
    public static final DeferredRegister<MenuType<?>> MENU_TYPES = DeferredRegister.create(BuiltInRegistries.MENU, BNChiseledEnchanting.MODID);

    // create local list of enchantments to send with menu
    public static final Supplier<MenuType<EnchantingInscriberMenu>> ENCHANTING_INSCRIBER_MENU = MENU_TYPES.register("enchanting_inscriber_menu",
            () -> IMenuTypeExtension.create((windowId, inv, buf) -> {
                var pos = buf.readBlockPos();
                int size = buf.readVarInt();                                                            // enchantment count
                List<EnchantingInscriberBlockEntity.EnchantmentOption> options = new ArrayList<>(size); // store enchantment list

                // fill list
                for (int i = 0; i < size; i++) {
                    Holder<Enchantment> enchantment = ByteBufCodecs.holderRegistry(Registries.ENCHANTMENT).decode(buf); // enchantment
                    int level = buf.readVarInt();                                                                       // level
                    int xpCost = buf.readVarInt();                                                                      // cost

                    options.add(new EnchantingInscriberBlockEntity.EnchantmentOption(enchantment, level, xpCost));
                }

                return new EnchantingInscriberMenu(windowId, inv, pos, options);
            }));

    public static void register(IEventBus eventBus) {
        MENU_TYPES.register(eventBus);
    }
}
