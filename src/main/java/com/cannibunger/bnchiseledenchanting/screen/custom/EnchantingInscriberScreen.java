package com.cannibunger.bnchiseledenchanting.screen.custom;

import com.cannibunger.bnchiseledenchanting.BNChiseledEnchanting;
import com.cannibunger.bnchiseledenchanting.block.entity.EnchantingInscriberBlockEntity;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.model.BookModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;

import java.util.List;

public class EnchantingInscriberScreen extends AbstractContainerScreen<EnchantingInscriberMenu> {
    // book texture
    private static final ResourceLocation ENCHANTING_BOOK_LOCATION = ResourceLocation.withDefaultNamespace("textures/entity/enchanting_table_book.png");
    private static final ResourceLocation GUI_TEXTURE = ResourceLocation.fromNamespaceAndPath(BNChiseledEnchanting.MODID, "textures/gui/enchantinginscriber/enchantinginscribergui.png");
    private final RandomSource random = RandomSource.create();
    private BookModel bookModel;
    public int time;
    public float flip;
    public float oFlip;
    public float flipT;
    public float flipA;
    public float open;
    public float oOpen;
    private ItemStack last = ItemStack.EMPTY;

    private static final int LIST_X = 59;       // pixel offset from left of panel
    private static final int LIST_Y = 17;       // pixel offset from top of panel
    private static final int ROW_HEIGHT = 15;   // row height
    private static final int ROW_WIDTH = 110;
    private static final int VISIBLE_ROWS = 7;  // # of rows
    private int scrollOffset = 0;               // offset to scroll thru rows


    // constructor
    public EnchantingInscriberScreen(EnchantingInscriberMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = 176;  // keep 176, length of inventory
        this.imageHeight = 222; // keep 222, height of menu

        this.inventoryLabelY = imageHeight - 94; // offset for inventory label
    }

    // on load, build buttons
    @Override
    protected void init() {
        super.init();
        this.bookModel = new BookModel(this.minecraft.getEntityModels().bakeLayer(ModelLayers.BOOK));
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
            Component label = Component.literal(Enchantment.getFullname(option.enchantment(), option.level()).getString())
                    .append(Component.literal("  (" + option.xpCost() + ")"));

            // calculate y position of row then render
            int rowY = topPos + LIST_Y + i * ROW_HEIGHT;
            int realIndex = indexed.index();
            addRenderableWidget(Button.builder(label, button -> selectOption(realIndex)).bounds(leftPos + LIST_X, rowY, ROW_WIDTH, ROW_HEIGHT).build());
        }
    }

    // client side button click handler
    private void selectOption(int optionIndex) {
        if (minecraft != null && minecraft.gameMode != null) {
            minecraft.gameMode.handleInventoryButtonClick(menu.containerId, optionIndex);
        }
    }

    // gui render
    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        int i = (this.width - this.imageWidth) / 2;     // x center
        int j = (this.height - this.imageHeight) / 2;   // y center

        graphics.blit(GUI_TEXTURE, i, j, 0, 0, this.imageWidth, this.imageHeight);
        this.renderBook(graphics, i, j, partialTick);
    }

    // per-frame render handler
    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        ItemStack target = menu.getInventory();                             // fetch inscriber inventory
        boolean enchantsAvailable = !menu.getOptions().isEmpty();           // is enchants available?

        // render text if not meeting needs
        int textX = leftPos + LIST_X + ROW_WIDTH/2;
        int textY = topPos + LIST_Y + (VISIBLE_ROWS * ROW_HEIGHT) / 2 - font.lineHeight / 2;
        if (target.isEmpty()) {
            graphics.drawCenteredString(font, Component.translatable("gui.bnchiseledenchanting.awaiting_item"), textX, textY, 0xFFFFFF);
        } else if (enchantsAvailable && menu.getCompatibleOptions().isEmpty()) {
            graphics.drawCenteredString(font, Component.translatable("gui.bnchiseledenchanting.no_compatible_enchantments1"), textX, textY - font.lineHeight/2, 0xFFFFFF);
            graphics.drawCenteredString(font, Component.translatable("gui.bnchiseledenchanting.no_compatible_enchantments2"), textX, textY +1+ font.lineHeight/2, 0xFFFFFF);
        } else if (!enchantsAvailable) {
            graphics.drawCenteredString(font, Component.translatable("gui.bnchiseledenchanting.no_enchantments_found1"), textX, textY - font.lineHeight/2, 0xFFFFFF);
            graphics.drawCenteredString(font, Component.translatable("gui.bnchiseledenchanting.no_enchantments_found2"), textX, textY +1+ font.lineHeight/2, 0xFFFFFF);
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

        this.tickBook();

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

    // book renderer
    private void renderBook(GuiGraphics guiGraphics, int x, int y, float partialTick) {
        float f = Mth.lerp(partialTick, this.oOpen, this.open);
        float f1 = Mth.lerp(partialTick, this.oFlip, this.flip);
        Lighting.setupForEntityInInventory();
        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate((float)x + 33.0F, (float)y + 35.0F, 100.0F);
        float f2 = 40.0F;
        guiGraphics.pose().scale(-40.0F, 40.0F, 40.0F);
        guiGraphics.pose().mulPose(Axis.XP.rotationDegrees(25.0F));
        guiGraphics.pose().translate((1.0F - f) * 0.2F, (1.0F - f) * 0.1F, (1.0F - f) * 0.25F);
        float f3 = -(1.0F - f) * 90.0F - 90.0F;
        guiGraphics.pose().mulPose(Axis.YP.rotationDegrees(f3));
        guiGraphics.pose().mulPose(Axis.XP.rotationDegrees(180.0F));
        float f4 = Mth.clamp(Mth.frac(f1 + 0.25F) * 1.6F - 0.3F, 0.0F, 1.0F);
        float f5 = Mth.clamp(Mth.frac(f1 + 0.75F) * 1.6F - 0.3F, 0.0F, 1.0F);
        this.bookModel.setupAnim(0.0F, f4, f5, f);
        VertexConsumer vertexconsumer = guiGraphics.bufferSource().getBuffer(this.bookModel.renderType(ENCHANTING_BOOK_LOCATION));
        this.bookModel.renderToBuffer(guiGraphics.pose(), vertexconsumer, 15728880, OverlayTexture.NO_OVERLAY);
        guiGraphics.flush();
        guiGraphics.pose().popPose();
        Lighting.setupFor3DItems();
    }

    public void tickBook() {
        ItemStack target = menu.getInventory();                             // fetch inscriber inventory
        boolean enchantsAvailable = !menu.getCompatibleOptions().isEmpty();           // is enchants available?
        if (!ItemStack.matches(target, this.last)) {
            this.last = target;

            do {
                this.flipT = this.flipT + (float)(this.random.nextInt(4) - this.random.nextInt(4));
            } while (this.flip <= this.flipT + 1.0F && this.flip >= this.flipT - 1.0F);
        }

        this.time++;
        this.oFlip = this.flip;
        this.oOpen = this.open;

        boolean flag = false;
        if (enchantsAvailable) {
            flag = true;
        }

        if (flag) {
            this.open += 0.2F;
        } else {
            this.open -= 0.2F;
        }

        this.open = Mth.clamp(this.open, 0.0F, 1.0F);
        float f1 = (this.flipT - this.flip) * 0.4F;
        float f = 0.2F;
        f1 = Mth.clamp(f1, -0.2F, 0.2F);
        this.flipA = this.flipA + (f1 - this.flipA) * 0.9F;
        this.flip = this.flip + this.flipA;
    }
}