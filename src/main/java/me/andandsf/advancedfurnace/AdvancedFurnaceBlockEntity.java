package me.andandsf.advancedfurnace;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.state.BlockState;

/**
 * @author Mgazul
 * @date 2026/2/23 06:02
 */
public class AdvancedFurnaceBlockEntity extends AbstractAdvancedFurnaceBlockEntity {

    public AdvancedFurnaceBlockEntity(BlockPos blockPos, BlockState blockState) {
        super(AdvancedFurnace.ADVANCED_FURNACE_BLOCK_ENTITY.get(), blockPos, blockState, RecipeType.SMELTING);
    }

    @Override
    protected Component getDefaultName() {
        return Component.translatable("block.advancedfurnace.advanced_furnace");
    }

    @Override
    protected AbstractContainerMenu createMenu(int p_59293_, Inventory p_59294_) {
        return new AdvancedFurnaceScreenHandler(p_59293_, p_59294_, this, this.dataAccess);
    }

}
