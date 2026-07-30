package com.cannibunger.bnchiseledenchanting.screen.custom;

import com.cannibunger.bnchiseledenchanting.block.ModBlocks;
import com.cannibunger.bnchiseledenchanting.block.entity.EnchantingInscriberBlockEntity;
import com.cannibunger.bnchiseledenchanting.screen.ModMenuTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.Level;
import net.minecraft.stats.Stats;
import net.minecraft.sounds.SoundEvents;

import java.util.ArrayList;
import java.util.List;

public class EnchantingInscriberMenu extends AbstractContainerMenu {
    private final EnchantingInscriberBlockEntity blockEntity;
    private final ContainerLevelAccess access;                              // level access helper
    private final Container inputContainer;                                 // inscriber inventory
    private final Container ingredientContainer;                            // ingredient inventory
    private List<EnchantingInscriberBlockEntity.EnchantmentOption> options; // enchant list

    // client side constructor -> shared constructor
    public EnchantingInscriberMenu(int containerID, Inventory playerInventory, BlockPos pos, List<EnchantingInscriberBlockEntity.EnchantmentOption> options) {
        this(containerID, playerInventory, resolveBlockEntity(playerInventory, pos), options);
    }

    // server side constructor, handles scanning enchantments -> shared constructor
    public EnchantingInscriberMenu(int containerId, Inventory playerInventory, EnchantingInscriberBlockEntity blockEntity) {
        this(containerId, playerInventory, blockEntity, blockEntity.scanAvailableEnchantments());
    }

    public final int invYStart = 140;               // 139 - start y for inventory
    public final int hotbarY = 198;                 // 197 - hotbar y

    // shared constructor
    private EnchantingInscriberMenu(int containerId, Inventory playerInventory, EnchantingInscriberBlockEntity blockEntity, List<EnchantingInscriberBlockEntity.EnchantmentOption> options) {
        super(ModMenuTypes.ENCHANTING_INSCRIBER_MENU.get(), containerId);

        this.blockEntity = blockEntity;                                                                 // update block entity
        this.access = ContainerLevelAccess.create(blockEntity.getLevel(), blockEntity.getBlockPos());   // update level access helper
        this.options = options;                                                                         // update options

        // create 1-slot inventory for enchanted, update block when contents are changed
        this.inputContainer = new SimpleContainer(1) {
            @Override
            public void setChanged() {
                super.setChanged();
                EnchantingInscriberMenu.this.slotsChanged(this);
            }
        };

        // create 4-slot inventory for enchanting ingredients, update block when contents are changed
        this.ingredientContainer = new SimpleContainer(4) {
            @Override
            public void setChanged() {
                super.setChanged();
                EnchantingInscriberMenu.this.slotsChanged(this);
            }
        };



        // add UI slot for enchanted, restrict to enchantable items and limit to 1 item
        addSlot(new Slot(inputContainer, 0, 25, 61) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return stack.getItem().isEnchantable(stack);
            }

            @Override
            public int getMaxStackSize() {
                return 1;
            }
        });

        // ingredient slots: amethyst shard, echo shard, blaze powder, dragon's breath
        addSlot(new Slot(ingredientContainer, 0, 15, 83) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return stack.is(net.minecraft.world.item.Items.AMETHYST_SHARD);
            }
        });
        addSlot(new Slot(ingredientContainer, 1, 35, 83) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return stack.is(net.minecraft.world.item.Items.ECHO_SHARD);
            }
        });
        addSlot(new Slot(ingredientContainer, 2, 15, 103) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return stack.is(net.minecraft.world.item.Items.BLAZE_POWDER);
            }
        });
        addSlot(new Slot(ingredientContainer, 3, 35, 103) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return stack.is(net.minecraft.world.item.Items.DRAGON_BREATH);
            }
        });

        // default inventory adder
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlot(new Slot(playerInventory, col + row * 9 + 9, 8 + col * 18, invYStart + row * 18));
            }
        }
        for (int col = 0; col < 9; col++) {
            addSlot(new Slot(playerInventory, col, 8 + col * 18, hotbarY));
        }
    }

    // check if block still exists, throw if not
    private static EnchantingInscriberBlockEntity resolveBlockEntity(Inventory playerInventory, BlockPos pos) {
        if (playerInventory.player.level().getBlockEntity(pos) instanceof EnchantingInscriberBlockEntity be) {
            return be;
        }
        throw new IllegalStateException("No EnchantingInscriberBlockEntity at " + pos);
    }

    // default accessor for options
    public List<EnchantingInscriberBlockEntity.EnchantmentOption> getOptions() {
        return options;
    }

    // map options to index
    public record indexedOption(int index, EnchantingInscriberBlockEntity.EnchantmentOption option) {}

    // accessor for options that allows checking compatibility
    // ALSO, apply xp cost formula
    public List<indexedOption> getCompatibleOptions() {
        List<indexedOption> result = new ArrayList<>();                                 // output list
        ItemStack target = inputContainer.getItem(0);                               // fetch inscriber inventory

        for (int i = 0; i < options.size(); i++) {
            EnchantingInscriberBlockEntity.EnchantmentOption option = options.get(i);   // get enchant

            if (canApply(target, options.get(i).enchantment())) {
                int cost = calculateXpCost(option.level());                             // calculate xp cost
                EnchantingInscriberBlockEntity.EnchantmentOption adjusted = new EnchantingInscriberBlockEntity.EnchantmentOption(option.enchantment(), option.level(), cost);
                result.add(new indexedOption(i, adjusted));                             // append to output
            }
        }
        return result;
    }

    // rescan enchantments whenever items change
    @Override
    public void slotsChanged(Container container) {
        super.slotsChanged(container);
        if (container == inputContainer || container == ingredientContainer) {
            Level level = blockEntity.getLevel();
            if (level != null && !level.isClientSide) {
                this.options = blockEntity.scanAvailableEnchantments();
                broadcastChanges();
            }
        }
    }

    // handler for clicking on enchantment buttons
    @Override
    public boolean clickMenuButton(Player player, int id) {
        EnchantingInscriberBlockEntity.EnchantmentOption option = options.get(id);   // enchant list
        ItemStack target = inputContainer.getItem(0);                           // item in slot
        int xpCost = calculateXpCost(option.level());                               // calculate cost

        // check if id is in bounds
        if (id < 0 || id >= options.size()) {
            return false;
        }
        // check if no item
        if (target.isEmpty()) {
            return false;
        }
        // check if unable to enchant
        if (!canApply(target, option.enchantment())) {
            return false;
        }
        // check if not in creative and not enough xp
        if (!player.getAbilities().instabuild && player.experienceLevel < xpCost) {
            return false;
        }

        access.execute((level, pos) -> {
            ItemEnchantments current = EnchantmentHelper.getEnchantmentsForCrafting(target);    // read enchants
            ItemEnchantments.Mutable mutable = new ItemEnchantments.Mutable(current);           // wrap enchants in mutable
            mutable.set(option.enchantment(), option.level());                                  // add chosen enchant
            EnchantmentHelper.setEnchantments(target, mutable.toImmutable());                   // unwrap enchants from mutable

            // subtract xp cost if not in creative
            if (!player.getAbilities().instabuild) {
                player.giveExperienceLevels(-xpCost);
            }

            consumeIngredients();                                                               // consume enchanting ingredients

            // grant enchanting stat and play enchanting sound
            player.awardStat(Stats.ENCHANT_ITEM);
            level.playSound(null, pos, SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.BLOCKS, 1.0F, level.random.nextFloat() * 0.1F + 0.9F);

            // rescan enchants
            this.options = blockEntity.scanAvailableEnchantments();
            broadcastChanges();
        });
        return true;
    }

    // check if able to apply enchantment
    private boolean canApply(ItemStack target, Holder<Enchantment> enchantmentHolder) {
        Enchantment enchantment = enchantmentHolder.value();    // target enchantment

        // check if compatible
        if (!enchantment.canEnchant(target)) {
            return false;
        }

        // check for conflicts
        ItemEnchantments current = EnchantmentHelper.getEnchantmentsForCrafting(target);
        for (Holder<Enchantment> existing : current.keySet()) {
            // if same enchant, skip
            if (existing.equals(enchantmentHolder)) {
                continue;
            }

            // check if enchanting conflicts exist
            boolean conflicts = existing.value().exclusiveSet().stream().anyMatch(h -> h.equals(enchantmentHolder))
                    || enchantment.exclusiveSet().stream().anyMatch(h -> h.equals(existing));
            if (conflicts) {
                return false;
            }
        }
        return true;
    }

    // handler for shift+click
    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack result = ItemStack.EMPTY;

        Slot slot = this.slots.get(index);
        if (slot != null && slot.hasItem()) {
            ItemStack slotStack = slot.getItem();
            result = slotStack.copy();

            if (index == 0) {
                // enchanted -> inventory
                if (!this.moveItemStackTo(slotStack, 5, 41, true)) {
                    return ItemStack.EMPTY;
                }
            } else if (index >= 1 && index <= 4) {
                // ingredient -> inventory
                if (!this.moveItemStackTo(slotStack, 5, 41, true)) {
                    return ItemStack.EMPTY;
                }
            } else {
                // inventory -> 1) enchanted, 2) ingredient
                if (!this.slots.get(0).hasItem() && this.slots.get(0).mayPlace(slotStack)) {
                    ItemStack single = slotStack.copyWithCount(1);
                    slotStack.shrink(1);
                    this.slots.get(0).setByPlayer(single);
                } else if (!this.moveItemStackTo(slotStack, 1, 5, false)) {
                    return ItemStack.EMPTY;
                }
            }

            if (slotStack.isEmpty()) {
                slot.setByPlayer(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }

            if (slotStack.getCount() == result.getCount()) {
                return ItemStack.EMPTY;
            }

            slot.onTake(player, slotStack);
        }
        return result;
    }

    // give items back if GUI is closed
    @Override
    public void removed(Player player) {
        super.removed(player);
        this.access.execute((level, pos) -> this.clearContainer(player, this.inputContainer));
        this.access.execute((level, pos) -> this.clearContainer(player, this.ingredientContainer));
    }

    // check for need to auto-close GUI
    @Override
    public boolean stillValid(Player player) {
        return AbstractContainerMenu.stillValid(this.access, player, ModBlocks.ENCHANTING_INSCRIBER.get());
    }

    // accessor for enchanted
    public ItemStack getInventory()  {
        return inputContainer.getItem(0);
    }

    // accessor for ingredients
    public ItemStack getIngredient(int slot) {
        return ingredientContainer.getItem(slot);
    }

    // check how many ingredient slots are filled
    private int countFilledIngredients() {
        int count = 0;

        for (int i = 0; i < ingredientContainer.getContainerSize(); i++) {
            if (!ingredientContainer.getItem(i).isEmpty()) {
                count++;
            }
        }

        return count;
    }

    // xp cost curve, x^3 -> x^2.5 -> x^2 -> x^1.5 -> x^1
    private int calculateXpCost(int enchantLevel) {
        double exponent = 3.0 - 0.5 * countFilledIngredients();
        return (int) Math.round(Math.pow(enchantLevel+1, exponent));
    }

    // consume one of each filled ingredient slot
    private void consumeIngredients() {
        for (int i = 0; i < ingredientContainer.getContainerSize(); i++) {
            ItemStack stack = ingredientContainer.getItem(i);

            if (!stack.isEmpty()) {
                stack.shrink(1);
            }
        }
    }
}
