package com.cannibunger.bnchiseledenchanting.screen.custom;

import com.cannibunger.bnchiseledenchanting.BNChiseledEnchanting;
import com.cannibunger.bnchiseledenchanting.block.entity.EnchantingInscriberBlockEntity;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.model.BookModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.EnchantmentTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;

import java.util.List;

public class EnchantingInscriberScreen extends AbstractContainerScreen<EnchantingInscriberMenu> {
    // book texture
    private static final ResourceLocation ENCHANTING_BOOK_LOCATION = ResourceLocation.withDefaultNamespace("textures/entity/enchanting_table_book.png");
    // gui texture
    private static final ResourceLocation GUI_TEXTURE = ResourceLocation.fromNamespaceAndPath(BNChiseledEnchanting.MODID, "textures/gui/enchantinginscriber/enchantinginscriber_gui.png");
    // enchant option texture


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


    private static final int LIST_X = 56;                                       // pixel offset from left of panel
    private static final int LIST_Y = 17;                                       // pixel offset from top of panel
    private static final int ROW_HEIGHT = 15;                                   // row height
    private static final int ROW_WIDTH = 130 - (6);                             // length of texture - 6 (bar width + 1 L/R margin
    private static final int VISIBLE_ROWS = 7;                                  // # of rows
    private int scrollOffset = 0;                                               // offset to scroll thru rows
    private static final int SCROLLBAR_X = LIST_X + ROW_WIDTH+1;                // just right of the button list + 1 gap
    private static final int SCROLLBAR_HEIGHT = VISIBLE_ROWS * ROW_HEIGHT - 2;  // matching to buttons height
    private static final int SCROLLBAR_WIDTH = 4;                               // width of scrollbar


    // constructor
    public EnchantingInscriberScreen(EnchantingInscriberMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = 194;  // keep 176, length of inventory
        this.imageHeight = 222; // keep 222, height of menu

        this.inventoryLabelY = imageHeight - 94; // offset for inventory label
        this.inventoryLabelX = EnchantingInscriberMenu.invXStart;
    }

    // on load, build buttons
    @Override
    protected void init() {
        super.init();
        this.bookModel = new BookModel(this.minecraft.getEntityModels().bakeLayer(ModelLayers.BOOK));
        buildButtons();
    }

    // handler to build buttons
    private void buildButtons() {
        clearWidgets(); // remove all UI buttons (reset)

        List<EnchantingInscriberMenu.indexedOption> options = menu.getCompatibleOptions();  // get enchants

        int visible = Math.max(0, Math.min(VISIBLE_ROWS, options.size() - scrollOffset));   // limit visible rows if less than max
        int maxOffset = Math.max(0, options.size() - VISIBLE_ROWS);                         // get max available offset
        scrollOffset = Math.max(0, Math.min(maxOffset, scrollOffset));                      // limit scrolling to max available offset


        // populate each button with enchants
        for (int i = 0; i < visible; i++) {
            EnchantingInscriberMenu.indexedOption indexed = options.get(scrollOffset + i);      // enchant index, offset by scroll
            EnchantingInscriberBlockEntity.EnchantmentOption option = indexed.option();         // copy enchant from options

            boolean affordable = canAfford(option.xpCost());

            // name text + formatting
            Component rawName = Enchantment.getFullname(option.enchantment(), option.level());
            boolean isCurse = option.enchantment().is(EnchantmentTags.CURSE);
            ChatFormatting nameColor = !affordable ? ChatFormatting.DARK_GRAY : (isCurse ? ChatFormatting.RED : ChatFormatting.WHITE);
            Component nameLabel = rawName.copy().withStyle(nameColor);

            // xp cost text
            Component costLabel = Component.literal(String.valueOf(option.xpCost()));

            // calculate y position of row then render
            int rowY = topPos + LIST_Y + i * ROW_HEIGHT;
            int realIndex = indexed.index();
            EnchantOptionButton button = new EnchantOptionButton(leftPos + LIST_X, rowY, ROW_WIDTH, ROW_HEIGHT, nameLabel, costLabel, b -> selectOption(realIndex));
            button.active = affordable;
            addRenderableWidget(button);
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
        renderScrollbar(graphics);                                          // scrollbar

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
        buildButtons();                                                                   // rebuild visisble buttons
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
            buildButtons();
        }
    }

    // book renderer
    private void renderBook(GuiGraphics guiGraphics, int x, int y, float partialTick) {
        float f = Mth.lerp(partialTick, this.oOpen, this.open);
        float f1 = Mth.lerp(partialTick, this.oFlip, this.flip);
        Lighting.setupForEntityInInventory();
        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate((float)x + 29.0F, (float)y + 38.0F, 100.0F);           // BOOK SCREEN COORDS
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

    // scrollbar renderer
    private void renderScrollbar(GuiGraphics graphics) {
        List<EnchantingInscriberMenu.indexedOption> options = menu.getCompatibleOptions();
        if (options.size() <= VISIBLE_ROWS) {
            return; // nothing to scroll
        }

        int trackX = leftPos + SCROLLBAR_X;
        int trackY = topPos + LIST_Y + 1;

        // scrollbar background
        graphics.fill(trackX, trackY, trackX + SCROLLBAR_WIDTH, trackY + SCROLLBAR_HEIGHT, 0xFF51493a);

        // make bar proportional to list size
        int maxOffset = options.size() - VISIBLE_ROWS;
        int thumbHeight = Math.max(10, SCROLLBAR_HEIGHT * VISIBLE_ROWS / options.size());
        int scrollableTrackSpace = SCROLLBAR_HEIGHT - thumbHeight;
        int thumbY = trackY + (maxOffset == 0 ? 0 : scrollableTrackSpace * scrollOffset / maxOffset);

        graphics.fill(trackX, thumbY, trackX + SCROLLBAR_WIDTH, thumbY + thumbHeight, 0xFFBDBDBD);
    }

    // scrollbar functionality
    private boolean draggingScrollbar = false;
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        List<EnchantingInscriberMenu.indexedOption> options = menu.getCompatibleOptions();

        if (button == 0 && options.size() > VISIBLE_ROWS) {
            int trackX = leftPos + SCROLLBAR_X;
            int trackY = topPos + LIST_Y + 1;

            if (mouseX >= trackX && mouseX <= trackX + SCROLLBAR_WIDTH && mouseY >= trackY && mouseY <= trackY + SCROLLBAR_HEIGHT) {
                draggingScrollbar = true;
                updateScrollFromMouse(mouseY, options.size());
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    // check for click-dragging
    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (draggingScrollbar) {
            updateScrollFromMouse(mouseY, menu.getCompatibleOptions().size());
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    // check for when released
    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        draggingScrollbar = false;
        return super.mouseReleased(mouseX, mouseY, button);
    }

    // get scrollwheel
    private void updateScrollFromMouse(double mouseY, int totalOptions) {
        int maxOffset = Math.max(0, totalOptions - VISIBLE_ROWS);
        if (maxOffset == 0) {
            scrollOffset = 0;
            return;
        }
        int trackY = topPos + LIST_Y;
        double relative = (mouseY - trackY) / (double) SCROLLBAR_HEIGHT;
        scrollOffset = Math.max(0, Math.min(maxOffset, (int) Math.round(relative * maxOffset)));
        buildButtons();
    }

    // custom button class for text formatting
    private static class EnchantOptionButton extends Button {
        private static final int PADDING = 3;
        private static final int GAP = 4;
        private static final double SCROLL_SPEED_PX_PER_SEC = 20.0;
        private static final long SCROLL_PAUSE_MS = 500;

        private final Component nameLabel;
        private final Component costLabel;

        protected EnchantOptionButton(int x, int y, int width, int height, Component nameLabel, Component costLabel, OnPress onPress) {
            super(x, y, width, height, nameLabel.copy().append(" ").append(costLabel), onPress, DEFAULT_NARRATION);
            this.nameLabel = nameLabel;
            this.costLabel = costLabel;
        }

        @Override
        public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
            // background color, dim if not affordable
            int bgColor;
            if (!this.active) {
                bgColor = 0xFF51493a;
            } else {
                bgColor = this.isHoveredOrFocused() ? 0xFFb688ae : 0xFFa09172;
            }

            graphics.fill(getX(), getY(), getX() + width, getY() + height, bgColor);

            Font font = Minecraft.getInstance().font;
            int nameColor = 0xFFFFFF;       // text color: NOT FUNCTIONAL
            int costColor = this.active ? 0xFF7efc20 : 0xFFAA0000;     // xp color: green if affordable, red if not
            int textY = getY() + (height - font.lineHeight) / 2;

            // cost: right-aligned
            int costWidth = font.width(costLabel);
            int costX = getX() + width - PADDING - costWidth;
            graphics.drawString(font, costLabel, costX, textY, costColor, true);

            int nameAreaX = getX() + PADDING;
            int nameAreaWidth = width - PADDING * 2 - costWidth - GAP;
            int nameWidth = font.width(nameLabel);

            // name: left-aligned
            if (nameWidth <= nameAreaWidth) {
                // if fits, no scroll
                graphics.drawString(font, nameLabel, nameAreaX, textY, nameColor, true);
                return;
            }

            if (!this.isHoveredOrFocused()) {
                // if not hovered, no scroll
                graphics.enableScissor(nameAreaX, getY(), nameAreaX + nameAreaWidth, getY() + height);
                graphics.drawString(font, nameLabel, nameAreaX, textY, nameColor, true);
                graphics.disableScissor();
                return;
            }

            // if hovered+overflowing, scroll
            int scrollRange = nameWidth - nameAreaWidth;
            long travelTimeMs = Math.max(1, (long) (scrollRange / SCROLL_SPEED_PX_PER_SEC * 1000));
            long cycle = travelTimeMs * 2 + SCROLL_PAUSE_MS * 2;
            long t = Util.getMillis() % cycle;

            int offset;
            if (t < SCROLL_PAUSE_MS) {
                offset = 0;
            } else if (t < SCROLL_PAUSE_MS + travelTimeMs) {
                offset = (int) ((t - SCROLL_PAUSE_MS) / (double) travelTimeMs * scrollRange);
            } else if (t < SCROLL_PAUSE_MS * 2 + travelTimeMs) {
                offset = scrollRange;
            } else {
                long t2 = t - SCROLL_PAUSE_MS * 2 - travelTimeMs;
                offset = scrollRange - (int) (t2 / (double) travelTimeMs * scrollRange);
            }

            graphics.enableScissor(nameAreaX, getY(), nameAreaX + nameAreaWidth, getY() + height);
            graphics.drawString(font, nameLabel, nameAreaX - offset, textY, nameColor, true);
            graphics.disableScissor();
        }
    }

    // check if able to afford enchant
    private boolean canAfford(int xpCost) {
        if (minecraft == null || minecraft.player == null) {
            return false;
        }
        return minecraft.player.getAbilities().instabuild || minecraft.player.experienceLevel >= xpCost;
    }
}