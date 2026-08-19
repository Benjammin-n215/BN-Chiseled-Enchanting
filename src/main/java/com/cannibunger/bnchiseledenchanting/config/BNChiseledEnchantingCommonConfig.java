package com.cannibunger.bnchiseledenchanting.config;

import net.neoforged.neoforge.common.ModConfigSpec;
import org.apache.commons.lang3.tuple.Pair;

public class BNChiseledEnchantingCommonConfig {
    public static final BNChiseledEnchantingCommonConfig CONFIG;
    public static final ModConfigSpec SPEC;

    static {
        Pair<BNChiseledEnchantingCommonConfig, ModConfigSpec> pair = new ModConfigSpec.Builder().configure(BNChiseledEnchantingCommonConfig::new);
        CONFIG = pair.getLeft();
        SPEC = pair.getRight();
    }

    // list of variables
    public final ModConfigSpec.ConfigValue<Integer> minLevel;
    public final ModConfigSpec.ConfigValue<Double> growthMultiplier;
    public final ModConfigSpec.ConfigValue<Double> discountMultiplier;

    public final ModConfigSpec.ConfigValue<String> itemA;
    public final ModConfigSpec.ConfigValue<String> itemB;
    public final ModConfigSpec.ConfigValue<String> itemC;
    public final ModConfigSpec.ConfigValue<String> itemD;

    // variable implementation
    private BNChiseledEnchantingCommonConfig(ModConfigSpec.Builder builder) {
        minLevel = builder
                .comment("Minimum level cost\n")
                .translation("config.bnchiseledenchanting.min_level")
                .defineInRange("minLevel", 7, 0, Integer.MAX_VALUE);
        growthMultiplier = builder
                .comment("Cost growth multiplier, bigger growth for higher levels.\n\n0 = Flat cost regardless of lvl or items.\n")
                .translation("config.bnchiseledenchanting.growth_multiplier")
                .defineInRange("growthMultiplier", 1, 0, Double.MAX_VALUE);
        discountMultiplier = builder
                .comment("Cost reduction multiplier per item.\n\n0 = No Discount\n0.425 = Flat cost with 4 items\n")
                .translation("config.bnchiseledenchanting.discount_multiplier")
                .defineInRange("discountMultiplier", 0.4, 0, 0.425);


        itemA = builder
                .comment("Item to be used in top-left slot\n\n Default: \n 'minecraft:amethyst_shard'")
                .translation("config.bnchiseledenchanting.item_a")
                .define("itemA", "minecraft:amethyst_shard");
        itemB = builder
                .comment("Item to be used in top-right slot\n\n Default: \n 'minecraft:trial_key'")
                .translation("config.bnchiseledenchanting.item_b")
                .define("itemB", "minecraft:trial_key");
        itemC = builder
                .comment("Item to be used in bottom-left slot\n\n Default: \n 'minecraft:blaze_powder'")
                .translation("config.bnchiseledenchanting.item_c")
                .define("itemC", "minecraft:blaze_powder");
        itemD = builder
                .comment("Item to be used in bottom-right slot\n\n Default: \n 'minecraft:dragon_breath'")
                .translation("config.bnchiseledenchanting.item_d")
                .define("itemD", "minecraft:dragon_breath");
    }
}
