package com.cannibunger.bnchiseledenchanting.config;

import net.neoforged.neoforge.common.ModConfigSpec;
import org.apache.commons.lang3.tuple.Pair;

public class BNChiseledEnchantingClientConfig {
    public static final BNChiseledEnchantingClientConfig CONFIG;
    public static final ModConfigSpec SPEC;

    static {
        Pair<BNChiseledEnchantingClientConfig, ModConfigSpec> pair = new ModConfigSpec.Builder().configure(BNChiseledEnchantingClientConfig::new);
        CONFIG = pair.getLeft();
        SPEC = pair.getRight();
    }

    // list of variables
    public final ModConfigSpec.ConfigValue<Boolean> hintMode;

    // variable implementation
    private BNChiseledEnchantingClientConfig(ModConfigSpec.Builder builder) {
        hintMode = builder
                .comment("Reveals the item intended for each slot.\n\nWill return 'Air' if configured item is not formatted correctly.")
                .translation("config.bnchiseledenchantment.hint_mode")
                .define("hintMode", false);
    }
}
