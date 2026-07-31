package com.cannibunger.bnchiseledenchanting.block.entity;

import com.cannibunger.bnchiseledenchanting.screen.custom.EnchantingInscriberMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Vec3i;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.Nameable;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChiseledBookShelfBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class EnchantingInscriberBlockEntity extends BlockEntity implements Nameable, MenuProvider {
    public int time;
    public float flip;
    public float oFlip;
    public float flipT;
    public float flipA;
    public float open;
    public float oOpen;
    public float rot;
    public float oRot;
    public float tRot;
    private static final RandomSource RANDOM = RandomSource.create();
    @javax.annotation.Nullable
    private Component name;

    // constructor
    public EnchantingInscriberBlockEntity(BlockPos pos, BlockState blockState) {
        super(ModBlockEntities.ENCHANTING_INSCRIBER_BE.get(), pos, blockState);
    }

    // save data to world
    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        if (this.hasCustomName()) {
            tag.putString("CustomName", Component.Serializer.toJson(this.name, registries));
        }
    }

    // load data from world
    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.contains("CustomName", 8)) {
            this.name = parseCustomNameSafe(tag.getString("CustomName"), registries);
        }
    }

    // animate book from tick
    public static void bookAnimationTick(Level level, BlockPos pos, BlockState state, EnchantingInscriberBlockEntity enchantingInscriber) {
        enchantingInscriber.oOpen = enchantingInscriber.open;
        enchantingInscriber.oRot = enchantingInscriber.rot;
        Player player = level.getNearestPlayer((double)pos.getX() + 0.5, (double)pos.getY() + 0.5, (double)pos.getZ() + 0.5, 3.0, false);
        if (player != null) {
            double d0 = player.getX() - ((double)pos.getX() + 0.5);
            double d1 = player.getZ() - ((double)pos.getZ() + 0.5);
            enchantingInscriber.tRot = (float) Mth.atan2(d1, d0);
            enchantingInscriber.open += 0.1F;
            if (enchantingInscriber.open < 0.5F || RANDOM.nextInt(40) == 0) {
                float f1 = enchantingInscriber.flipT;

                do {
                    enchantingInscriber.flipT = enchantingInscriber.flipT + (float)(RANDOM.nextInt(4) - RANDOM.nextInt(4));
                } while (f1 == enchantingInscriber.flipT);
            }
        } else {
            enchantingInscriber.tRot += 0.02F;
            enchantingInscriber.open -= 0.1F;
        }

        while (enchantingInscriber.rot >= (float) Math.PI) {
            enchantingInscriber.rot -= (float) (Math.PI * 2);
        }

        while (enchantingInscriber.rot < (float) -Math.PI) {
            enchantingInscriber.rot += (float) (Math.PI * 2);
        }

        while (enchantingInscriber.tRot >= (float) Math.PI) {
            enchantingInscriber.tRot -= (float) (Math.PI * 2);
        }

        while (enchantingInscriber.tRot < (float) -Math.PI) {
            enchantingInscriber.tRot += (float) (Math.PI * 2);
        }

        float f2 = enchantingInscriber.tRot - enchantingInscriber.rot;

        while (f2 >= (float) Math.PI) {
            f2 -= (float) (Math.PI * 2);
        }

        while (f2 < (float) -Math.PI) {
            f2 += (float) (Math.PI * 2);
        }

        enchantingInscriber.rot += f2 * 0.4F;
        enchantingInscriber.open = Mth.clamp(enchantingInscriber.open, 0.0F, 1.0F);
        enchantingInscriber.time++;
        enchantingInscriber.oFlip = enchantingInscriber.flip;
        float f = (enchantingInscriber.flipT - enchantingInscriber.flip) * 0.4F;
        float f3 = 0.2F;
        f = Mth.clamp(f, -0.2F, 0.2F);
        enchantingInscriber.flipA = enchantingInscriber.flipA + (f - enchantingInscriber.flipA) * 0.9F;
        enchantingInscriber.flip = enchantingInscriber.flip + enchantingInscriber.flipA;
    }

    public static Component parseCustomNameSafe(String json, HolderLookup.Provider registries) {
        try {
            return Component.Serializer.fromJson(json, registries);
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public Component getDisplayName() {
        return getName();
    }

    @Override
    public Component getName() {
        return (Component)(this.name != null ? this.name : Component.translatable("block.bnchiseledenchanting.enchanting_inscriber"));
    }

    public void setCustomName(@javax.annotation.Nullable Component customName) {
        this.name = customName;
    }

    @javax.annotation.Nullable
    @Override
    public Component getCustomName() {
        return this.name;
    }

    @Override
    protected void applyImplicitComponents(BlockEntity.DataComponentInput componentInput) {
        super.applyImplicitComponents(componentInput);
        this.name = componentInput.get(DataComponents.CUSTOM_NAME);
    }

    @Override
    protected void collectImplicitComponents(DataComponentMap.Builder components) {
        super.collectImplicitComponents(components);
        components.set(DataComponents.CUSTOM_NAME, this.name);
    }

    @Override
    public void removeComponentsFromTag(CompoundTag tag) {
        tag.remove("CustomName");
    }

    // scan for nearby bookshelves, 2 blocks out and up to 3 blocks upward
    private static final List<Vec3i> bookshelfList = scanBookshelves();
    private static List<Vec3i> scanBookshelves() {
        List<Vec3i> offsets = new ArrayList<>();
        for (int dy = 0; dy <= 3; dy++) {
            for (int dx = -2; dx <= 2; dx++) {
                for (int dz = -2; dz <= 2; dz++) {
                    if (Math.max(Math.abs(dx), Math.abs(dz)) != 2) {
                        continue;
                    }
                    offsets.add(new Vec3i(dx, dy, dz));
                }
            }
        }
        return offsets;
    }

    // scan enchantments from bookshelves and create list [ench, level, cost]
    public record EnchantmentOption(Holder<Enchantment> enchantment, int level, int xpCost) {}
    public List<EnchantmentOption> scanAvailableEnchantments() {
        List<EnchantmentOption> result = new ArrayList<>();                     // list of enchants
        Map<Holder<Enchantment>, Integer> bestLevels = new LinkedHashMap<>();   // map for best levels
        BlockPos origin = getBlockPos();                                        // coord for inscriber

        // in case not loaded
        if(level == null) {
            return result;
        }

        int bookShelvesFound = 0;                   // debug counter
        for (Vec3i offset : bookshelfList) {
            BlockPos pos = origin.offset(offset);   // get coord of bookshelf

            // catch if not chiseled bookshelf
            if (!(level.getBlockEntity(pos) instanceof ChiseledBookShelfBlockEntity bookshelf)) {
                continue;
            }

            bookShelvesFound++;                     // debug counter

            // search thru contents of chiseled bookshelf
            for (int slot = 0; slot < bookshelf.getContainerSize(); slot++) {
                ItemStack stack = bookshelf.getItem(slot);

                if (stack.isEmpty()) {
                    continue;                       // skip if empty
                }

                ItemEnchantments stored = stack.get(DataComponents.STORED_ENCHANTMENTS); // grab enchantment data

                if (stored == null) {
                    continue;                       // skip if no enchantments
                }

                for (Holder<Enchantment> enchantment : stored.keySet()) {
                    int enchLevel = stored.getLevel(enchantment);               // get level
                    bestLevels.merge(enchantment, enchLevel, Math::max);        // replace if highest
                    //System.out.println("found " + enchantment + " " + enchLevel); // debug
                }
            }
        }

        //System.out.println((level.isClientSide ? "CLIENT" : "SERVER") + " Bookshelves: " + bookShelvesFound + ", enchants: " + bestLevels.size());  // debug

        // assemble result
        for (Map.Entry<Holder<Enchantment>, Integer> enchant : bestLevels.entrySet()) {
            result.add(new EnchantmentOption(enchant.getKey(), enchant.getValue(), enchant.getValue()));
        }

        // sort alphabetically
        result.sort(Comparator.comparing(o -> o.enchantment().value().description().getString()));

        return result;
    }

    // menu handler
    @Override
    public @Nullable AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new EnchantingInscriberMenu(containerId, playerInventory, this);
    }
}
