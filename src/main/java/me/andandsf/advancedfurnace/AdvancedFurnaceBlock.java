package me.andandsf.advancedfurnace;

import com.mojang.serialization.MapCodec;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.HorizontalFacingBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class AdvancedFurnaceBlock extends BlockWithEntity {
    public static final EnumProperty<Direction> FACING;
    public static final BooleanProperty LIT;

    static {
        FACING = HorizontalFacingBlock.FACING;
        LIT = Properties.LIT;
    }

    public AdvancedFurnaceBlock(Settings settings) {
        super(settings);
        this.setDefaultState(this.stateManager.getDefaultState().with(FACING, Direction.NORTH).with(LIT, false));
    }

    public static final MapCodec<AdvancedFurnaceBlock> CODEC = createCodec(AdvancedFurnaceBlock::new);

    @Override
    public MapCodec<AdvancedFurnaceBlock> getCodec() {
        return CODEC;
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
        if (world.isClient()) {
            return ActionResult.SUCCESS;
        } else {
            this.openScreen(world, pos, player);
            return ActionResult.CONSUME;
        }
    }

    private void openScreen(World world, BlockPos pos, PlayerEntity player) {
        BlockEntity blockEntity = world.getBlockEntity(pos);
        if (blockEntity instanceof AdvancedFurnaceBlockEntity) {
            player.openHandledScreen((NamedScreenHandlerFactory)blockEntity);
            player.incrementStat(Stats.INTERACT_WITH_FURNACE);
        }
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(FACING, LIT);
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        return this.getDefaultState().with(FACING, ctx.getHorizontalPlayerFacing().getOpposite());
    }

    @Override
    public void randomDisplayTick(BlockState state, World world, BlockPos pos, Random random) {
        if (state.get(LIT)) {
            if (world.isClient()) {
                double d = (double)pos.getX() + 0.5D;
                double e = (double)pos.getY();
                double f = (double)pos.getZ() + 0.5D;
                if (random.nextDouble() < 0.1D) {
                    world.playSound(null, d, e, f, SoundEvents.BLOCK_FURNACE_FIRE_CRACKLE, SoundCategory.BLOCKS, 1.0F, 1.0F);
                }

                Direction direction = state.get(FACING);
                Direction.Axis axis = direction.getAxis();
                double g = 0.52D;
                double h = random.nextDouble() * 0.6D - 0.3D;
                double i = axis == Direction.Axis.X ? (double)direction.getOffsetX() * 0.52D : h;
                double j = random.nextDouble() * 6.0D / 16.0D;
                double k = axis == Direction.Axis.Z ? (double)direction.getOffsetZ() * 0.52D : h;
                world.addParticleClient(ParticleTypes.SMOKE,  d + i, e + j, f + k, 0.0D, 0.0D, 0.0D);
            }
        }
    }

    @Override
    public void onStateReplaced(BlockState state, ServerWorld world, BlockPos pos, boolean moved) {
        if (state.getBlock() != world.getBlockState(pos).getBlock()) {
            BlockEntity blockEntity = world.getBlockEntity(pos);
            if (blockEntity instanceof AdvancedFurnaceBlockEntity) {
                if (world instanceof ServerWorld) {
                    ItemScatterer.spawn(world, pos, (AdvancedFurnaceBlockEntity)blockEntity);
                }
                world.updateComparators(pos,this);
            }
            super.onStateReplaced(state, world, pos, moved);
        }
    }

    @Override
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }

    @Override
    public boolean hasComparatorOutput(BlockState state) {
        return true;
    }

    @Override
    public int getComparatorOutput(BlockState state, World world, BlockPos pos, Direction direction) {
        return ScreenHandler.calculateComparatorOutput(world.getBlockEntity(pos));
    }

    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new AdvancedFurnaceBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
        if (world.isClient()) return null;
        return validateTicker(type, AdvancedFurnace.ADVANCED_FURNACE_BLOCK_ENTITY, AdvancedFurnaceBlockEntity::tick);
    }
}
