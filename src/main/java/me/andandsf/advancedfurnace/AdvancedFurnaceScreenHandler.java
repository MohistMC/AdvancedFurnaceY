package me.andandsf.advancedfurnace;


import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.FurnaceResultSlot;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class AdvancedFurnaceScreenHandler extends AbstractContainerMenu {
    private final Container inventory;
    ContainerData propertyDelegate;

    public AdvancedFurnaceScreenHandler(int syncId, Inventory playerInventory) {
        this(syncId, playerInventory, new SimpleContainer(9), new SimpleContainerData(10));
    }

    public AdvancedFurnaceScreenHandler(int syncId, Inventory playerInventory, Container inventory, ContainerData propertyDelegate) {
        super(AdvancedFurnace.ADVANCED_FURNACE_SCREEN_HANDLER, syncId);
        checkContainerSize(inventory, 9);
        this.inventory = inventory;
        this.propertyDelegate = propertyDelegate;
        inventory.startOpen(playerInventory.player);
        this.addDataSlots(propertyDelegate);
        int m,l;

        this.addSlot(new Slot(inventory, 0, 12, 33){
            @Override
            public boolean mayPlace(ItemStack stack) {
                return playerInventory.player.level().fuelValues().isFuel(stack) || stack.getItem() == Items.BUCKET;
            }
            @Override
            public int getMaxStackSize(ItemStack stack) {
                return stack.getItem() == Items.BUCKET ? 1 : super.getMaxStackSize(stack);
            }
        });

        for (m=0; m < 4; ++m) {
            this.addSlot(new Slot(inventory, 2*m+1, 46 + 27*m, 19));
            this.addSlot(new FurnaceResultSlot(playerInventory.player, inventory, 2*m+2, 46 + 27*m, 63));
        }

        for (m = 0; m < 3; ++m) {
            for (l = 0; l < 9; ++l) {
                this.addSlot(new Slot(playerInventory, l + m * 9 + 9, 8 + l * 18, 95 + m * 18));
            }
        }
        //The player Hotbar
        for (m = 0; m < 9; ++m) {
            this.addSlot(new Slot(playerInventory, m, 8 + m * 18, 153));
        }
    }

    @Override
    public boolean stillValid(Player player) {
        return this.inventory.stillValid(player);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int invSlot) {
        ItemStack newStack = ItemStack.EMPTY;
        Slot slot = this.slots.get(invSlot);
        if (slot != null && slot.hasItem()) {
            ItemStack originalStack = slot.getItem();
            newStack = originalStack.copy();
            if (invSlot < this.inventory.getContainerSize()) {
                if (!this.moveItemStackTo(originalStack, this.inventory.getContainerSize(), this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.moveItemStackTo(originalStack, 0, this.inventory.getContainerSize(), false)) {
                return ItemStack.EMPTY;
            }

            if (originalStack.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
        }

        return newStack;
    }

    public boolean isBurning() {
        return propertyDelegate.get(0) > 0;
    }

    public int getFuelProgress() {
        int i = getFuelTime();
        if (i == 0) {
            i = 200;
        }

        return getBurnTime() * 13 / i;
    }

    public int getCookProgress(int index) {
        int i = getCookTime(index);
        int j = getCookTimeTotal(index);
        return j != 0 && i != 0 ? i * 24 / j : 0;
    }

    private int getBurnTime() {
        return propertyDelegate.get(0);
    }

    private int getFuelTime() {
        return propertyDelegate.get(1);
    }

    private int getCookTime(int i) {
        return propertyDelegate.get(2 + i * 2);
    }

    private int getCookTimeTotal(int i) {
        return propertyDelegate.get(3 + i * 2);
    }
}
