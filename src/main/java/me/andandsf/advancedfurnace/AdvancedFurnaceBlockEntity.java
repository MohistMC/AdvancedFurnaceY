package me.andandsf.advancedfurnace;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class AdvancedFurnaceBlockEntity extends BlockEntity implements MenuProvider, Container, WorldlyContainer {
    private NonNullList<ItemStack> inventory = NonNullList.withSize(9, ItemStack.EMPTY);
    private int burnTime;
    private int fuelTime;
    private int[] cookTime = new int[4];
    private int[] cookTimeTotal = new int[4];

    private static final int[] TOP_SLOTS = new int[]{1, 3, 5, 7};
    private static final int[] BOTTOM_SLOTS = new int[]{2, 4, 6, 8};
    private static final int[] SIDE_SLOTS = new int[]{0};

    private final ContainerData propertyDelegate = new ContainerData() {
        @Override
        public int get(int index) {
            switch (index) {
                case 0:
                    return burnTime;
                case 1:
                    return fuelTime;
                case 2:
                    return cookTime[0];
                case 3:
                    return cookTimeTotal[0];
                case 4:
                    return cookTime[1];
                case 5:
                    return cookTimeTotal[1];
                case 6:
                    return cookTime[2];
                case 7:
                    return cookTimeTotal[2];
                case 8:
                    return cookTime[3];
                case 9:
                    return cookTimeTotal[3];
                default:
                    return 0;
            }
        }

        @Override
        public void set(int index, int value) {
            switch (index) {
                case 0:
                    burnTime = value;
                case 1:
                    fuelTime = value;
                case 2:
                    cookTime[0] = value;
                case 3:
                    cookTimeTotal[0] = value;
                case 4:
                    cookTime[1] = value;
                case 5:
                    cookTimeTotal[1] = value;
                case 6:
                    cookTime[2] = value;
                case 7:
                    cookTimeTotal[2] = value;
                case 8:
                    cookTime[3] = value;
                case 9:
                    cookTimeTotal[3] = value;
            }
        }

        //this is supposed to return the amount of integers you have in your delegate, in our example only one
        @Override
        public int getCount() {
            return 10;
        }
    };

    public AdvancedFurnaceBlockEntity(BlockPos pos, BlockState state) {
        super(AdvancedFurnace.ADVANCED_FURNACE_BLOCK_ENTITY, pos, state);
    }

    @Override
    public int size() {
        return inventory.size();
    }

    @Override
    public boolean isEmpty() {
        for (int i = 0; i < size(); i++) {
            ItemStack stack = getStack(i);
            if (!stack.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public ItemStack getItem(int slot) {
        return inventory.get(slot);
    }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        ItemStack result = ContainerHelper.removeItem(inventory, slot, amount);
        if (!result.isEmpty()) {
            setChanged();
        }
        return result;
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        return ContainerHelper.takeItem(inventory, slot);
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        inventory.set(slot, stack);
        if (stack.getCount() > getMaxStackSize()) {
            stack.setCount(getMaxStackSize());
        }
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.advancedfurnace.advanced_furnace");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int syncId, Inventory inv, Player player) {
        return new AdvancedFurnaceScreenHandler(syncId, inv, this, propertyDelegate);
    }

    @Override
    public void clear() {
        inventory.clear();
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);
        this.inventory = DefaultedList.ofSize(this.size(), ItemStack.EMPTY);
        Inventories.readNbt(nbt, this.inventory);
        this.burnTime = nbt.getShort("BurnTime");
        this.cookTime = nbt.getIntArray("CookTime");
        this.cookTimeTotal = nbt.getIntArray("CookTimeTotal");
        this.fuelTime = this.getFuelTime((ItemStack)this.inventory.get(1));
    }

    @Override
    public void writeNbt(NbtCompound nbt) {
        super.writeNbt(nbt);
        nbt.putShort("BurnTime", (short)this.burnTime);
        nbt.putIntArray("CookTime", this.cookTime);
        nbt.putIntArray("CookTimeTotal", this.cookTimeTotal);
        Inventories.writeNbt(nbt, this.inventory);
    }

    private boolean isBurning() {
        return this.burnTime > 0;
    }

    public static void tick(ServerWorld world, BlockPos pos, BlockState state, AdvancedFurnaceBlockEntity be) {
        boolean bl = be.isBurning();
        boolean bl2 = false;
        if (be.isBurning()) {
            be.burnTime -= 4;
        }

        if (!world.isClient()) {
            ItemStack itemStack = (ItemStack)be.inventory.get(0);
            if (!be.isBurning() && (itemStack.isEmpty() || ((ItemStack)be.inventory.get(0)).isEmpty())) {
                for (int i = 0; i < 4; i++) {
                    if (!be.isBurning() && be.cookTime[i] > 0) {
                        be.cookTime[i] = MathHelper.clamp(be.cookTime[i] - 2, 0, be.cookTimeTotal[i]);
                    }
                }
            } else {
                for (int i = 0; i < 4; i++) {
                    Inventory tempInventory = new SimpleInventory(3);
                    tempInventory.setStack(0, be.inventory.get(1+i*2));
                    SingleStackRecipeInput singleStackRecipeInput = new SingleStackRecipeInput(itemStack);
                    RecipeEntry<? extends AbstractCookingRecipe> recipe = world.getRecipeManager().getFirstMatch(singleStackRecipeInput, world).orElse(null);

                    if (!be.isBurning() && be.canAcceptRecipeOutput(world.getRegistryManager(), recipe, i)) {
                        be.burnTime = be.getFuelTime(itemStack);
                        be.fuelTime = be.burnTime;
                        if (be.isBurning()) {
                            bl2 = true;
                            if (!itemStack.isEmpty()) {
                                Item item = itemStack.getItem();
                                itemStack.decrement(1);
                                if (itemStack.isEmpty()) {
                                    Item item2 = item.getRecipeRemainder();
                                    be.inventory.set(0, item2 == null ? ItemStack.EMPTY : new ItemStack(item2));
                                }
                            }
                        }
                    }

                    if (be.isBurning() && be.canAcceptRecipeOutput(world.getRegistryManager(), recipe, i)) {
                        be.cookTime[i] += 2;
                        be.cookTimeTotal[i] = world.getRecipeManager().getFirstMatch(RecipeType.SMELTING, tempInventory, world).map(AbstractCookingRecipe::getCookTime).orElse(200);
                        if (be.cookTime[i] == be.cookTimeTotal[i]) {
                            be.cookTime[i] = 0;
                            be.cookTimeTotal[i] = be.world.getRecipeManager().getFirstMatch(RecipeType.SMELTING, tempInventory, be.world).map(AbstractCookingRecipe::getCookTime).orElse(200);
                            be.craftRecipe(world.getRegistryManager(), recipe, i);
                            bl2 = true;
                        }
                    } else {
                        be.cookTime[i] = 0;
                    }
                }
            }
            if (bl != be.isBurning()) {
                bl2 = true;
                world.setBlockState(be.pos, (BlockState)world.getBlockState(be.pos).with(AdvancedFurnaceBlock.LIT, be.isBurning()), 3);
            }
        }
        if (bl2) {
            be.markDirty();
        }
    }

    protected boolean canAcceptRecipeOutput(DynamicRegistryManager registryManager, @Nullable RecipeEntry<? extends AbstractCookingRecipe> recipe, int i) {
        if (!(this.inventory.get(1+2*i)).isEmpty() && recipe != null) {
            ItemStack itemStack = recipe.getOutput(registryManager);
            if (itemStack.isEmpty()) {
                return false;
            } else {
                ItemStack itemStack2 = (ItemStack)this.inventory.get(2+2*i);
                if (itemStack2.isEmpty()) {
                    return true;
                } else if (!ItemStack.areItemsEqual(itemStack2, itemStack)) {
                    return false;
                } else if (itemStack2.getCount() < this.getMaxCountPerStack() && itemStack2.getCount() < itemStack2.getMaxCount()) {
                    return true;
                } else {
                    return itemStack2.getCount() < itemStack.getMaxCount();
                }
            }
        } else {
            return false;
        }
    }

    protected int getFuelTime(ItemStack fuel) {
        if (fuel.isEmpty()) {
            return 0;
        } else {
            Item item = fuel.getItem();
            return AbstractFurnaceBlockEntity.createFuelTimeMap().getOrDefault(item, 0);
        }
    }

    private void craftRecipe(DynamicRegistryManager registryManager, @Nullable RecipeEntry<? extends AbstractCookingRecipe> recipe, SingleStackRecipeInput input, DefaultedList<ItemStack> inventory, int maxCount) {
        if (recipe != null && this.canAcceptRecipeOutput(registryManager, recipe, i)) {
            ItemStack itemStack = this.inventory.get(1+2*i);
            ItemStack itemStack2 = recipe.getOutput(registryManager);
            ItemStack itemStack3 = this.inventory.get(2+2*i);
            if (itemStack3.isEmpty()) {
                this.inventory.set(2+2*i, itemStack2.copy());
            } else if (itemStack3.getItem() == itemStack2.getItem()) {
                itemStack3.increment(1);
            }
            itemStack.decrement(1);
        }
    }

    @Override
    public int[] getSlotsForFace(Direction side) {
        if (side == Direction.DOWN) {
            return BOTTOM_SLOTS;
        } else {
            return side == Direction.UP ? TOP_SLOTS : SIDE_SLOTS;
        }
    }

    @Override
    public boolean canPlaceItemThroughFace(int slot, ItemStack stack, @Nullable Direction dir) {
        return true;
    }

    @Override
    public boolean canTakeItemThroughFace(int slot, ItemStack stack, Direction dir) {
        return true;
    }
}
