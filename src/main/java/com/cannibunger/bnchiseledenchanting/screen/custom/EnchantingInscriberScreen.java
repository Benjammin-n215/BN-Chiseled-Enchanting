package com.cannibunger.bnchiseledenchanting.screen.custom;

import com.cannibunger.bnchiseledenchanting.block.entity.EnchantingInscriberBlockEntity;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;

import java.util.List;

public class EnchantingInscriberScreen extends AbstractContainerScreen<EnchantingInscriberMenu> {
    // highlighted enchantment texture
    private static final ResourceLocation ENCHANTMENT_SLOT_HIGHLIGHTED_SPRITE = ResourceLocation.withDefaultNamespace("container/enchanting_table/enchantment_slot_highlighted");
    // enchantment slot texture
    private static final ResourceLocation ENCHANTMENT_SLOT_SPRITE = ResourceLocation.withDefaultNamespace("container/enchanting_table/enchantment_slot");
    // table GUI texture
    private static final ResourceLocation ENCHANTING_TABLE_LOCATION = ResourceLocation.withDefaultNamespace("textures/gui/container/enchanting_table.png");
    // book texture
    private static final ResourceLocation ENCHANTING_BOOK_LOCATION = ResourceLocation.withDefaultNamespace("textures/entity/enchanting_table_book.png");

    //private static final ResourceLocation GUI_TEXTURE = ResourceLocation.fromNamespaceAndPath(BNChiseledEnchanting.MODID, "textures/gui/enchantinginscriber/enchantinginscribergui.png");
    //private static final ResourceLocation BACKGROUND = ResourceLocation.withDefaultNamespace("textures/gui/container/enchanting_table.png");

    private static final int LIST_X = 60;       // pixel offset from left of panel
    private static final int LIST_Y = 15;       // pixel offset from top of panel
    private static final int ROW_HEIGHT = 15;   // row height
    private static final int VISIBLE_ROWS = 5;  // # of rows
    private int scrollOffset = 0;               // offset to scroll thru rows

    // constructor
    public EnchantingInscriberScreen(EnchantingInscriberMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = 176;
        this.imageHeight = 200;

//        this.inventoryLabelY = 74; // offset for inventory label
    }

    // on load, build buttons
    @Override
    protected void init() {
        super.init();
        rebuildButtons();
    }

    // handler to build buttons
    private void rebuildButtons() {
        clearWidgets(); // remove all UI buttons (reset)

        List<EnchantingInscriberMenu.indexedOption> options = menu.getCompatibleOptions();  // get enchants
        int visible = Math.max(0, Math.min(VISIBLE_ROWS, options.size() - scrollOffset));   // limit visible rows if less than max

        // populate each button with enchants
        for (int i = 0; i < visible; i++) {
            EnchantingInscriberMenu.indexedOption indexed = options.get(scrollOffset + i);      // enchant index, offset by scroll
            EnchantingInscriberBlockEntity.EnchantmentOption option = indexed.option();         // copy enchant from options
            Enchantment enchantment = option.enchantment().value();                             // unwrap enchant

            // display enchant text [name + num + cost]
            Component label = Component.literal(enchantment.description().getString())
                    .append(Component.literal(" " + option.level() + "  (" + option.xpCost() + " Levels)"));

            // calculate y position of row then render
            int rowY = topPos + LIST_Y + i * ROW_HEIGHT;
            int realIndex = indexed.index();
            addRenderableWidget(Button.builder(label, button -> selectOption(realIndex)).bounds(leftPos + LIST_X, rowY, 140, ROW_HEIGHT - 2).build());
        }
    }

    // client side button click handler
    private void selectOption(int optionIndex) {
        if (minecraft != null && minecraft.gameMode != null) {
            minecraft.gameMode.handleInventoryButtonClick(menu.containerId, optionIndex);
        }
    }

    // ------------------- NOT FINISHED, temp gray box bg
    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.fill(leftPos, topPos, leftPos + imageWidth, topPos + imageHeight, 0xFFC6C6C6);
        graphics.fill(leftPos + 1, topPos + 1, leftPos + imageWidth - 1, topPos + imageHeight - 1, 0xFF8B8B8B);
    }

    // per-frame render handler
    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        ItemStack target = menu.getInventory();                             // fetch inscriber inventory
        boolean enchantsAvailable = !menu.getOptions().isEmpty();           // is enchants available?

        // check if empty, then check for compat enchants, then check for any enchants
        if (target.isEmpty()) {
            graphics.drawCenteredString(font, Component.translatable("gui.bnchiseledenchanting.awaiting_item"), leftPos + imageWidth / 2, topPos + LIST_Y, 0xFFFFFF);
        } else if (enchantsAvailable && menu.getCompatibleOptions().isEmpty()) {
            graphics.drawCenteredString(font, Component.translatable("gui.bnchiseledenchanting.no_compatible_enchantments"), leftPos + imageWidth / 2, topPos + LIST_Y, 0xFFFFFF);
        } else if (!enchantsAvailable) {
            graphics.drawCenteredString(font, Component.translatable("gui.bnchiseledenchanting.no_enchantments_found"), leftPos + imageWidth / 2, topPos + LIST_Y, 0xFFFFFF);
        }

        renderTooltip(graphics, mouseX, mouseY);    // draw tooltip for hovered item
    }

    // mouse scrolling handler
    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        List<EnchantingInscriberMenu.indexedOption> options = menu.getCompatibleOptions();  // get enchants
        int maxOffset = Math.max(0, options.size() - VISIBLE_ROWS);                         // max scrollable
        int newOffset = scrollOffset - (int) Math.signum(scrollY);                          // calculate current scroll
        scrollOffset = Math.max(0, Math.min(maxOffset, newOffset));                         // clamp scrolling
        rebuildButtons();                                                                   // rebuild visisble buttons
        return true;
    }

    // check every tick whether the contents have changed and update buttons
    private ItemStack lastKnownTarget = ItemStack.EMPTY;
    private final ItemStack[] lastKnownIngredients = {ItemStack.EMPTY, ItemStack.EMPTY, ItemStack.EMPTY, ItemStack.EMPTY};
    @Override
    protected void containerTick() {
        super.containerTick();

        boolean changed = false;

        // check if enchanted changes
        ItemStack currentTarget = menu.slots.get(0).getItem();
        if (!ItemStack.matches(currentTarget, lastKnownTarget)) {
            lastKnownTarget = currentTarget.copy();
            changed = true;
        }

        // check if ingredients changes
        for (int i = 0; i < 4; i++) {
            ItemStack currentIngredient = menu.slots.get(1 + i).getItem();
            if (!ItemStack.matches(currentIngredient, lastKnownIngredients[i])) {
                lastKnownIngredients[i] = currentIngredient.copy();
                changed = true;
            }
        }

        if (changed) {
            rebuildButtons();
        }
    }
}