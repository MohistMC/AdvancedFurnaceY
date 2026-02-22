package me.andandsf.advancedfurnace;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import org.jetbrains.annotations.Nullable;

public class AdvancedFurnaceBlock extends AbstractAdvancedFurnaceBlock {
    public static final EnumProperty<Direction> FACING = HorizontalDirectionalBlock.FACING;
    public static final BooleanProperty LIT = BlockStateProperties.LIT;

    public AdvancedFurnaceBlock(Properties settings) {
        super(settings);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(LIT, false));
    }

    public static final MapCodec<AdvancedFurnaceBlock> CODEC = simpleCodec(AdvancedFurnaceBlock::new);

    @Override
    public MapCodec<AdvancedFurnaceBlock> codec() {
        return CODEC;
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

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new AdvancedFurnaceBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> @org.jspecify.annotations.Nullable BlockEntityTicker<T> getTicker(Level p_153273_, BlockState p_153274_, BlockEntityType<T> p_153275_) {
        return createFurnaceTicker(p_153273_, p_153275_, AdvancedFurnace.ADVANCED_FURNACE_BLOCK_ENTITY);
    }
}
