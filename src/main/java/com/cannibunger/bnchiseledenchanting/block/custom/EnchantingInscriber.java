package com.cannibunger.bnchiseledenchanting.block.custom;

import com.cannibunger.bnchiseledenchanting.block.entity.EnchantingInscriberBlockEntity;
import com.cannibunger.bnchiseledenchanting.block.entity.ModBlockEntities;
import com.mojang.serialization.MapCodec;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nullable;
import java.util.List;

public class EnchantingInscriber extends BaseEntityBlock {
    public static final MapCodec<EnchantingInscriber> CODEC = simpleCodec(EnchantingInscriber::new);
    protected static final VoxelShape SHAPE = Block.box(0,0,0,16,12,16);
    public static final List<BlockPos> BOOKSHELF_OFFSETS = BlockPos.betweenClosedStream(-2, 0, -2, 2, 1, 2)
            .filter(p_341357_ -> Math.abs(p_341357_.getX()) == 2 || Math.abs(p_341357_.getZ()) == 2)
            .map(BlockPos::immutable)
            .toList();

    // codec
    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    // constructor
    public EnchantingInscriber(Properties properties) {
        super(properties);
    }

    // check for bookshelves around, used for particles
    public static boolean isValidBookShelf(Level level, BlockPos enchantingInscriberPos, BlockPos bookshelfPos) {
        return level.getBlockState(enchantingInscriberPos.offset(bookshelfPos)).is(Blocks.CHISELED_BOOKSHELF)
                && level.getBlockState(enchantingInscriberPos.offset(bookshelfPos.getX() / 2, bookshelfPos.getY(), bookshelfPos.getZ() / 2))
                .isAir();
    }

    // occlude light according to shape
    @Override
    protected boolean useShapeForLightOcclusion(BlockState state) {
        return true;
    }

    // bounding box
    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    // animate tick for particles
    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        super.animateTick(state, level, pos, random);

        for (BlockPos blockpos : BOOKSHELF_OFFSETS) {
            if (random.nextInt(16) == 0 && isValidBookShelf(level, pos, blockpos)) {
                level.addParticle(
                        ParticleTypes.ENCHANT,
                        (double)pos.getX() + 0.5,
                        (double)pos.getY() + 2.0,
                        (double)pos.getZ() + 0.5,
                        (double)((float)blockpos.getX() + random.nextFloat()) - 0.5,
                        (double)((float)blockpos.getY() - random.nextFloat() - 1.0F),
                        (double)((float)blockpos.getZ() + random.nextFloat()) - 0.5
                );
            }
        }
    }

    // render function to use
    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    // ???
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new EnchantingInscriberBlockEntity(pos, state);
    }

    // animate tick for book
    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
        return level.isClientSide ? createTickerHelper(blockEntityType, ModBlockEntities.ENCHANTING_INSCRIBER_BE.get(), EnchantingInscriberBlockEntity::bookAnimationTick) : null;
    }

    // interaction handler
    // tldr: if server, get enchantments from block entity and write info to player menu
    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (level.isClientSide) {
            level.playSound(player, pos, SoundEvents.CHISELED_BOOKSHELF_INSERT_ENCHANTED, SoundSource.BLOCKS, 1f, 1f);
            return InteractionResult.SUCCESS;
        } else {
            if (level.getBlockEntity(pos) instanceof  EnchantingInscriberBlockEntity blockEntity) {
                List<EnchantingInscriberBlockEntity.EnchantmentOption> options = blockEntity.scanAvailableEnchantments();

                player.openMenu(blockEntity, buf -> {
                    buf.writeBlockPos(pos);
                    buf.writeVarInt(options.size());
                    for (EnchantingInscriberBlockEntity.EnchantmentOption option : options) {
                        net.minecraft.network.codec.ByteBufCodecs.holderRegistry(net.minecraft.core.registries.Registries.ENCHANTMENT).encode(buf, option.enchantment());
                        buf.writeVarInt(option.level());
                        buf.writeVarInt(option.xpCost());
                    }
                });

            }

            return InteractionResult.CONSUME;
        }
    }

    // menu handler
    @Nullable
    @Override
    protected MenuProvider getMenuProvider(BlockState state, Level level, BlockPos pos) {
        if (level.getBlockEntity(pos) instanceof EnchantingInscriberBlockEntity blockEntity) {
            return blockEntity;
        }
        return null;
    }

    // pathfinding handling
    @Override
    protected boolean isPathfindable(BlockState state, PathComputationType pathComputationType) {
        return false;
    }

    // tooltip
    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        if (!Screen.hasShiftDown()) {
            tooltipComponents.add(Component.translatable("tooltip.bnchiseledenchanting.shiftdown.tooltip"));
        } else {
            tooltipComponents.add(Component.translatable("tooltip.bnchiseledenchanting.enchanting_inscriber.tooltip"));
        }

        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
    }
}
