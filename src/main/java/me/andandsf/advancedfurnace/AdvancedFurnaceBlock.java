package me.andandsf.advancedfurnace;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

import static net.minecraft.world.level.storage.loot.entries.CompositeEntryBase.createCodec;

public class AdvancedFurnaceBlock extends BaseEntityBlock {
    public static final EnumProperty<Direction> FACING;
    public static final BooleanProperty LIT;

    static {
        FACING = HorizontalDirectionalBlock.FACING;
        LIT = BlockStateProperties.LIT;
    }

    public AdvancedFurnaceBlock(net.minecraft.world.level.block.state.BlockBehaviour.Properties settings) {
        super(settings);
        this.registerDefaultState(this.stateDefinition.getDefaultState().with(FACING, Direction.NORTH).with(LIT, false));
    }

    public static final MapCodec<AdvancedFurnaceBlock> CODEC = createCodec(AdvancedFurnaceBlock::new);

    @Override
    public MapCodec<AdvancedFurnaceBlock> codec() {
        return CODEC;
    }

    @Override
    public InteractionResult useWithoutItem(BlockState state, Level world, BlockPos pos, Player player, BlockHitResult hit) {
        if (world.isClientSide()) {
            return InteractionResult.SUCCESS;
        } else {
            this.openScreen(world, pos, player);
            return InteractionResult.CONSUME;
        }
    }

    private void openScreen(Level world, BlockPos pos, Player player) {
        BlockEntity blockEntity = world.getBlockEntity(pos);
        if (blockEntity instanceof AdvancedFurnaceBlockEntity) {
            player.openMenu((MenuProvider)blockEntity);
            player.awardStat(Stats.INTERACT_WITH_FURNACE);
        }
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, LIT);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        return this.defaultBlockState().setValue(FACING, ctx.getHorizontalDirection().getOpposite());
    }

    @Override
    public void animateTick(BlockState state, Level world, BlockPos pos, RandomSource random) {
        if (state.getValue(LIT)) {
            if (world.isClientSide()) {
                double d = (double)pos.getX() + 0.5D;
                double e = (double)pos.getY();
                double f = (double)pos.getZ() + 0.5D;
                if (random.nextDouble() < 0.1D) {
                    world.playSound(null, d, e, f, SoundEvents.FURNACE_FIRE_CRACKLE, SoundSource.BLOCKS, 1.0F, 1.0F);
                }

                Direction direction = state.getValue(FACING);
                Direction.Axis axis = direction.getAxis();
                double g = 0.52D;
                double h = random.nextDouble() * 0.6D - 0.3D;
                double i = axis == Direction.Axis.X ? (double)direction.getStepX() * 0.52D : h;
                double j = random.nextDouble() * 6.0D / 16.0D;
                double k = axis == Direction.Axis.Z ? (double)direction.getStepZ() * 0.52D : h;
                world.addParticle(ParticleTypes.SMOKE,  d + i, e + j, f + k, 0.0D, 0.0D, 0.0D);
            }
        }
    }

    @Override
    public void affectNeighborsAfterRemoval(BlockState state, ServerLevel world, BlockPos pos, boolean moved) {
        if (state.getBlock() != world.getBlockState(pos).getBlock()) {
            BlockEntity blockEntity = world.getBlockEntity(pos);
            if (blockEntity instanceof AdvancedFurnaceBlockEntity) {
                if (world instanceof ServerLevel) {
                    Containers.dropContents(world, pos, (AdvancedFurnaceBlockEntity)blockEntity);
                }
                world.updateNeighbourForOutputSignal(pos,this);
            }
            super.affectNeighborsAfterRemoval(state, world, pos, moved);
        }
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    @Override
    public int getAnalogOutputSignal(BlockState state, Level world, BlockPos pos, Direction direction) {
        return AbstractContainerMenu.getRedstoneSignalFromBlockEntity(world.getBlockEntity(pos));
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new AdvancedFurnaceBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level world, BlockState state, BlockEntityType<T> type) {
        if (world.isClientSide()) return null;
        return createTickerHelper(type, AdvancedFurnace.ADVANCED_FURNACE_BLOCK_ENTITY, AdvancedFurnaceBlockEntity::tick);
    }
}
