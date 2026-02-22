package me.andandsf.advancedfurnace;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.FurnaceBlock;
import org.jspecify.annotations.NonNull;

public class UpdateToolItem extends Item {
    public UpdateToolItem(Item.Properties settings) {
        super(settings);
    }

    @Override
    public @NonNull InteractionResult useOn(UseOnContext context) {
        Level world = context.getLevel();
        if (world.isClientSide()) return InteractionResult.PASS;
        BlockPos pos = context.getClickedPos();
        if (world.getBlockState(pos).getBlock() instanceof FurnaceBlock) {
            Direction direction = world.getBlockState(pos).getValue(FurnaceBlock.FACING);
            world.removeBlock(pos, false);
            world.setBlock(pos, AdvancedFurnace.ADVANCED_FURNACE_BLOCK.defaultBlockState().with(AdvancedFurnaceBlock.FACING, direction));
            context.getItemInHand().shrink(1);
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }
}
