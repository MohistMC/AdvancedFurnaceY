package me.andandsf.advancedfurnace;

import com.google.common.collect.Lists;
import com.mojang.serialization.Codec;
import it.unimi.dsi.fastutil.objects.Reference2IntMap;
import it.unimi.dsi.fastutil.objects.Reference2IntOpenHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.StackedItemContents;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.RecipeCraftingHolder;
import net.minecraft.world.inventory.StackedContentsCompatible;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.FuelValues;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public abstract class AbstractAdvancedFurnaceBlockEntity extends BaseContainerBlockEntity implements WorldlyContainer, RecipeCraftingHolder, StackedContentsCompatible {
    protected static final int SLOT_FUEL = 0;
    protected static final int SLOT_INPUT_1 = 1;
    protected static final int SLOT_RESULT_1 = 2;
    protected static final int SLOT_INPUT_2 = 3;
    protected static final int SLOT_RESULT_2 = 4;
    protected static final int SLOT_INPUT_3 = 5;
    protected static final int SLOT_RESULT_3 = 6;
    protected static final int SLOT_INPUT_4 = 7;
    protected static final int SLOT_RESULT_4 = 8;

    public static final int DATA_LIT_TIME = 0;
    public static final int DATA_LIT_DURATION = 1;

    private static final int[] DATA_COOKING_PROGRESS = {2, 4, 6, 8};
    private static final int[] DATA_COOKING_TOTAL_TIME = {3, 5, 7, 9};

    public static final int NUM_DATA_VALUES = 10;

    private static final int[] SLOTS_FOR_UP = new int[]{SLOT_INPUT_1, SLOT_INPUT_2, SLOT_INPUT_3, SLOT_INPUT_4};
    private static final int[] SLOTS_FOR_DOWN = new int[]{SLOT_RESULT_1, SLOT_RESULT_2, SLOT_RESULT_3, SLOT_RESULT_4, SLOT_FUEL};
    private static final int[] SLOTS_FOR_SIDES = new int[]{SLOT_FUEL};

    public static final int BURN_TIME_STANDARD = 200;
    public static final int BURN_COOL_SPEED = 2;
    private static final Codec<Map<ResourceKey<Recipe<?>>, Integer>> RECIPES_USED_CODEC;
    private static final short DEFAULT_COOKING_TIMER = 0;
    private static final short DEFAULT_COOKING_TOTAL_TIME = 0;
    private static final short DEFAULT_LIT_TIME_REMAINING = 0;
    private static final short DEFAULT_LIT_TOTAL_TIME = 0;

    protected NonNullList<ItemStack> items;
    int litTimeRemaining;
    int litTotalTime;

    int[] cookingTimers = new int[4];
    int[] cookingTotalTimes = new int[4];

    protected final ContainerData dataAccess;
    private final Reference2IntOpenHashMap<ResourceKey<Recipe<?>>> recipesUsed;
    private final RecipeManager.CachedCheck<SingleRecipeInput, ? extends AbstractCookingRecipe> quickCheck;
    private final RecipeType<? extends AbstractCookingRecipe> recipeType;

    public AbstractAdvancedFurnaceBlockEntity(BlockEntityType<?> blockEntityType, BlockPos blockPos, BlockState blockState, RecipeType<? extends AbstractCookingRecipe> recipeType) {
        super(blockEntityType, blockPos, blockState);
        // 修改为9个槽位：1个燃料槽 + 4个输入槽 + 4个输出槽
        this.items = NonNullList.withSize(9, ItemStack.EMPTY);
        this.dataAccess = new ContainerData() {
            public int get(int i) {
                switch (i) {
                    case 0 -> {
                        return AbstractAdvancedFurnaceBlockEntity.this.litTimeRemaining;
                    }
                    case 1 -> {
                        return AbstractAdvancedFurnaceBlockEntity.this.litTotalTime;
                    }
                    case 2 -> {
                        return AbstractAdvancedFurnaceBlockEntity.this.cookingTimers[0];
                    }
                    case 3 -> {
                        return AbstractAdvancedFurnaceBlockEntity.this.cookingTotalTimes[0];
                    }
                    case 4 -> {
                        return AbstractAdvancedFurnaceBlockEntity.this.cookingTimers[1];
                    }
                    case 5 -> {
                        return AbstractAdvancedFurnaceBlockEntity.this.cookingTotalTimes[1];
                    }
                    case 6 -> {
                        return AbstractAdvancedFurnaceBlockEntity.this.cookingTimers[2];
                    }
                    case 7 -> {
                        return AbstractAdvancedFurnaceBlockEntity.this.cookingTotalTimes[2];
                    }
                    case 8 -> {
                        return AbstractAdvancedFurnaceBlockEntity.this.cookingTimers[3];
                    }
                    case 9 -> {
                        return AbstractAdvancedFurnaceBlockEntity.this.cookingTotalTimes[3];
                    }
                    default -> {
                        return 0;
                    }
                }
            }

            public void set(int i, int j) {
                switch (i) {
                    case 0 -> AbstractAdvancedFurnaceBlockEntity.this.litTimeRemaining = j;
                    case 1 -> AbstractAdvancedFurnaceBlockEntity.this.litTotalTime = j;
                    case 2 -> AbstractAdvancedFurnaceBlockEntity.this.cookingTimers[0] = j;
                    case 3 -> AbstractAdvancedFurnaceBlockEntity.this.cookingTotalTimes[0] = j;
                    case 4 -> AbstractAdvancedFurnaceBlockEntity.this.cookingTimers[1] = j;
                    case 5 -> AbstractAdvancedFurnaceBlockEntity.this.cookingTotalTimes[1] = j;
                    case 6 -> AbstractAdvancedFurnaceBlockEntity.this.cookingTimers[2] = j;
                    case 7 -> AbstractAdvancedFurnaceBlockEntity.this.cookingTotalTimes[2] = j;
                    case 8 -> AbstractAdvancedFurnaceBlockEntity.this.cookingTimers[3] = j;
                    case 9 -> AbstractAdvancedFurnaceBlockEntity.this.cookingTotalTimes[3] = j;
                }
            }

            public int getCount() {
                return NUM_DATA_VALUES;
            }
        };
        this.recipesUsed = new Reference2IntOpenHashMap();
        this.quickCheck = RecipeManager.createCheck(recipeType);
        this.recipeType = recipeType;
        for (int i = 0; i < 4; i++) {
            this.cookingTimers[i] = DEFAULT_COOKING_TIMER;
            this.cookingTotalTimes[i] = DEFAULT_COOKING_TOTAL_TIME;
        }
    }

    private boolean isLit() {
        return this.litTimeRemaining > 0;
    }

    protected void loadAdditional(ValueInput valueInput) {
        super.loadAdditional(valueInput);
        this.items = NonNullList.withSize(this.getContainerSize(), ItemStack.EMPTY);
        ContainerHelper.loadAllItems(valueInput, this.items);

        // 加载所有槽位的计时器数据
        this.litTimeRemaining = valueInput.getShortOr("lit_time_remaining", (short)0);
        this.litTotalTime = valueInput.getShortOr("lit_total_time", (short)0);

        for (int i = 0; i < 4; i++) {
            this.cookingTimers[i] = valueInput.getShortOr("cooking_time_spent_" + i, (short)0);
            this.cookingTotalTimes[i] = valueInput.getShortOr("cooking_total_time_" + i, (short)0);
        }

        this.recipesUsed.clear();
        this.recipesUsed.putAll((Map)valueInput.read("RecipesUsed", RECIPES_USED_CODEC).orElse(Map.of()));
    }

    protected void saveAdditional(ValueOutput valueOutput) {
        super.saveAdditional(valueOutput);
        valueOutput.putShort("lit_time_remaining", (short)this.litTimeRemaining);
        valueOutput.putShort("lit_total_time", (short)this.litTotalTime);

        for (int i = 0; i < 4; i++) {
            valueOutput.putShort("cooking_time_spent_" + i, (short)this.cookingTimers[i]);
            valueOutput.putShort("cooking_total_time_" + i, (short)this.cookingTotalTimes[i]);
        }

        ContainerHelper.saveAllItems(valueOutput, this.items);
        valueOutput.store("RecipesUsed", RECIPES_USED_CODEC, this.recipesUsed);
    }

    public static void serverTick(ServerLevel serverLevel, BlockPos blockPos, BlockState blockState, AbstractAdvancedFurnaceBlockEntity advancedFurnaceBlockEntity) {
        boolean wasLit = advancedFurnaceBlockEntity.isLit();
        boolean stateChanged = false;

        if (advancedFurnaceBlockEntity.isLit()) {
            --advancedFurnaceBlockEntity.litTimeRemaining;
        }

        ItemStack fuelStack = advancedFurnaceBlockEntity.items.get(SLOT_FUEL);
        boolean hasFuel = !fuelStack.isEmpty();

        boolean anyWorking = false;
        for (int slotIndex = 0; slotIndex < 4; slotIndex++) {
            int inputSlot = getInputSlot(slotIndex);
            int resultSlot = getResultSlot(slotIndex);

            ItemStack inputStack = advancedFurnaceBlockEntity.items.get(inputSlot);
            ItemStack resultStack = advancedFurnaceBlockEntity.items.get(resultSlot);

            if (!inputStack.isEmpty()) {
                SingleRecipeInput singleRecipeInput = new SingleRecipeInput(inputStack);
                RecipeHolder<? extends AbstractCookingRecipe> recipeHolder =
                        advancedFurnaceBlockEntity.quickCheck.getRecipeFor(singleRecipeInput, serverLevel).orElse(null);

                int maxStackSize = advancedFurnaceBlockEntity.getMaxStackSize();

                if (!advancedFurnaceBlockEntity.isLit() && canBurn(recipeHolder, singleRecipeInput, advancedFurnaceBlockEntity.items, maxStackSize, slotIndex)) {
                    advancedFurnaceBlockEntity.litTimeRemaining = advancedFurnaceBlockEntity.getBurnDuration(serverLevel.fuelValues(), fuelStack);
                    advancedFurnaceBlockEntity.litTotalTime = advancedFurnaceBlockEntity.litTimeRemaining;

                    if (advancedFurnaceBlockEntity.isLit()) {
                        stateChanged = true;
                        if (hasFuel) {
                            Item item = fuelStack.getItem();
                            fuelStack.shrink(1);
                            if (fuelStack.isEmpty()) {
                                ItemStackTemplate remainder = item.getCraftingRemainder();
                                advancedFurnaceBlockEntity.items.set(SLOT_FUEL, remainder != null ? remainder.create() : ItemStack.EMPTY);
                            }
                        }
                    }
                }

                if (advancedFurnaceBlockEntity.isLit() && canBurn(recipeHolder, singleRecipeInput, advancedFurnaceBlockEntity.items, maxStackSize, slotIndex)) {
                    advancedFurnaceBlockEntity.cookingTimers[slotIndex]++;
                    anyWorking = true;

                    if (advancedFurnaceBlockEntity.cookingTimers[slotIndex] >= advancedFurnaceBlockEntity.cookingTotalTimes[slotIndex]) {
                        advancedFurnaceBlockEntity.cookingTimers[slotIndex] = 0;
                        advancedFurnaceBlockEntity.cookingTotalTimes[slotIndex] = getTotalCookTime(serverLevel, advancedFurnaceBlockEntity, slotIndex);

                        if (burn(recipeHolder, singleRecipeInput, advancedFurnaceBlockEntity.items, maxStackSize, slotIndex)) {
                            advancedFurnaceBlockEntity.setRecipeUsed(recipeHolder);
                        }

                        stateChanged = true;
                    }
                } else if (!advancedFurnaceBlockEntity.isLit()) {
                    advancedFurnaceBlockEntity.cookingTimers[slotIndex] = 0;
                }
            }
        }

        if (!advancedFurnaceBlockEntity.isLit() && !anyWorking) {
            for (int i = 0; i < 4; i++) {
                if (advancedFurnaceBlockEntity.cookingTimers[i] > 0) {
                    advancedFurnaceBlockEntity.cookingTimers[i] = Mth.clamp(
                            advancedFurnaceBlockEntity.cookingTimers[i] - BURN_COOL_SPEED,
                            0,
                            advancedFurnaceBlockEntity.cookingTotalTimes[i]
                    );
                    stateChanged = true;
                }
            }
        }

        if (wasLit != advancedFurnaceBlockEntity.isLit()) {
            stateChanged = true;
            blockState = blockState.setValue(AbstractAdvancedFurnaceBlock.LIT, advancedFurnaceBlockEntity.isLit());
            serverLevel.setBlock(blockPos, blockState, 3);
        }

        if (stateChanged) {
            setChanged(serverLevel, blockPos, blockState);
        }
    }

    private static int getInputSlot(int index) {
        return switch (index) {
            case 0 -> SLOT_INPUT_1;
            case 1 -> SLOT_INPUT_2;
            case 2 -> SLOT_INPUT_3;
            case 3 -> SLOT_INPUT_4;
            default -> throw new IllegalArgumentException("Invalid slot index: " + index);
        };
    }

    private static int getResultSlot(int index) {
        return switch (index) {
            case 0 -> SLOT_RESULT_1;
            case 1 -> SLOT_RESULT_2;
            case 2 -> SLOT_RESULT_3;
            case 3 -> SLOT_RESULT_4;
            default -> throw new IllegalArgumentException("Invalid slot index: " + index);
        };
    }

    private static boolean canBurn(@Nullable RecipeHolder<? extends AbstractCookingRecipe> recipeHolder, SingleRecipeInput singleRecipeInput, NonNullList<ItemStack> items, int maxStackSize, int slotIndex) {
        int inputSlot = getInputSlot(slotIndex);
        int resultSlot = getResultSlot(slotIndex);

        if (items.get(inputSlot).isEmpty() || recipeHolder == null) {
            return false;
        }

        ItemStack resultStack = ((AbstractCookingRecipe)recipeHolder.value()).assemble(singleRecipeInput);
        if (resultStack.isEmpty()) {
            return false;
        }

        ItemStack existingResult = items.get(resultSlot);
        if (existingResult.isEmpty()) {
            return true;
        } else if (!ItemStack.isSameItemSameComponents(existingResult, resultStack)) {
            return false;
        } else if (existingResult.getCount() < maxStackSize && existingResult.getCount() < existingResult.getMaxStackSize()) {
            return true;
        } else {
            return existingResult.getCount() < resultStack.getMaxStackSize();
        }
    }

    private static boolean burn(@Nullable RecipeHolder<? extends AbstractCookingRecipe> recipeHolder, SingleRecipeInput singleRecipeInput, NonNullList<ItemStack> items, int maxStackSize, int slotIndex) {
        if (recipeHolder != null && canBurn(recipeHolder, singleRecipeInput, items, maxStackSize, slotIndex)) {
            int inputSlot = getInputSlot(slotIndex);
            int resultSlot = getResultSlot(slotIndex);

            ItemStack inputStack = items.get(inputSlot);
            ItemStack resultStack = recipeHolder.value().assemble(singleRecipeInput);
            ItemStack existingResult = items.get(resultSlot);

            if (existingResult.isEmpty()) {
                items.set(resultSlot, resultStack.copy());
            } else if (ItemStack.isSameItemSameComponents(existingResult, resultStack)) {
                existingResult.grow(1);
            }

            // 处理湿海绵特殊情况
            if (inputStack.is(Blocks.WET_SPONGE.asItem()) && !items.get(SLOT_FUEL).isEmpty() && items.get(SLOT_FUEL).is(Items.BUCKET)) {
                items.set(SLOT_FUEL, new ItemStack(Items.WATER_BUCKET));
            }

            inputStack.shrink(1);
            return true;
        } else {
            return false;
        }
    }

    protected int getBurnDuration(FuelValues fuelValues, ItemStack itemStack) {
        return fuelValues.burnDuration(itemStack);
    }

    private static int getTotalCookTime(ServerLevel serverLevel, AbstractAdvancedFurnaceBlockEntity advancedFurnaceBlockEntity, int slotIndex) {
        int inputSlot = getInputSlot(slotIndex);
        SingleRecipeInput singleRecipeInput = new SingleRecipeInput(advancedFurnaceBlockEntity.getItem(inputSlot));
        return advancedFurnaceBlockEntity.quickCheck.getRecipeFor(singleRecipeInput, serverLevel)
                .map(recipeHolder -> ((AbstractCookingRecipe) recipeHolder.value()).cookingTime())
                .orElse(BURN_TIME_STANDARD);
    }

    public int[] getSlotsForFace(Direction direction) {
        if (direction == Direction.DOWN) {
            return SLOTS_FOR_DOWN;
        } else {
            return direction == Direction.UP ? SLOTS_FOR_UP : SLOTS_FOR_SIDES;
        }
    }

    @Override
    public boolean canPlaceItemThroughFace(int slot, ItemStack itemStack, @Nullable Direction direction) {
        return this.canPlaceItem(slot, itemStack);
    }

    public boolean canTakeItemThroughFace(int slot, ItemStack itemStack, Direction direction) {
        if (direction == Direction.DOWN && slot == SLOT_FUEL) {
            return itemStack.is(Items.WATER_BUCKET) || itemStack.is(Items.BUCKET);
        } else {
            return true;
        }
    }

    public int getContainerSize() {
        return this.items.size();
    }

    protected NonNullList<ItemStack> getItems() {
        return this.items;
    }

    protected void setItems(NonNullList<ItemStack> items) {
        this.items = items;
    }

    public void setItem(int slot, ItemStack itemStack) {
        ItemStack oldStack = this.items.get(slot);
        boolean sameItem = !itemStack.isEmpty() && ItemStack.isSameItemSameComponents(oldStack, itemStack);
        this.items.set(slot, itemStack);
        itemStack.limitSize(this.getMaxStackSize(itemStack));

        if (isInputSlot(slot) && !sameItem) {
            Level level = this.level;
            if (level instanceof ServerLevel serverLevel) {
                int slotIndex = getInputSlotIndex(slot);
                if (slotIndex >= 0) {
                    this.cookingTotalTimes[slotIndex] = getTotalCookTime(serverLevel, this, slotIndex);
                    this.cookingTimers[slotIndex] = 0;
                    this.setChanged();
                }
            }
        }
    }

    private boolean isInputSlot(int slot) {
        return slot == SLOT_INPUT_1 || slot == SLOT_INPUT_2 || slot == SLOT_INPUT_3 || slot == SLOT_INPUT_4;
    }

    private int getInputSlotIndex(int slot) {
        return switch (slot) {
            case SLOT_INPUT_1 -> 0;
            case SLOT_INPUT_2 -> 1;
            case SLOT_INPUT_3 -> 2;
            case SLOT_INPUT_4 -> 3;
            default -> -1;
        };
    }

    public boolean canPlaceItem(int slot, ItemStack itemStack) {
        if (slot == SLOT_RESULT_1 || slot == SLOT_RESULT_2 || slot == SLOT_RESULT_3 || slot == SLOT_RESULT_4) {
            return false;
        }
        else if (slot == SLOT_FUEL) {
            ItemStack fuelStack = this.items.get(SLOT_FUEL);
            return itemStack.getBurnTime(this.recipeType, this.level.fuelValues()) > 0 ||
                    (itemStack.is(Items.BUCKET) && !fuelStack.is(Items.BUCKET));
        }
        else {
            return true;
        }
    }

    public void setRecipeUsed(@Nullable RecipeHolder<?> recipeHolder) {
        if (recipeHolder != null) {
            ResourceKey<Recipe<?>> resourceKey = recipeHolder.id();
            this.recipesUsed.addTo(resourceKey, 1);
        }
    }

    public @Nullable RecipeHolder<?> getRecipeUsed() {
        return null;
    }

    public void awardUsedRecipes(Player player, List<ItemStack> list) {
    }

    public void awardUsedRecipesAndPopExperience(ServerPlayer serverPlayer) {
        List<RecipeHolder<?>> recipes = this.getRecipesToAwardAndPopExperience(serverPlayer.level(), serverPlayer.position());
        serverPlayer.awardRecipes(recipes);

        for(RecipeHolder<?> recipeHolder : recipes) {
            serverPlayer.triggerRecipeCrafted(recipeHolder, this.items);
        }

        this.recipesUsed.clear();
    }

    public List<RecipeHolder<?>> getRecipesToAwardAndPopExperience(ServerLevel serverLevel, Vec3 position) {
        List<RecipeHolder<?>> recipes = Lists.newArrayList();

        for (Reference2IntMap.Entry<ResourceKey<Recipe<?>>> entry : this.recipesUsed.reference2IntEntrySet()) {
            serverLevel.recipeAccess().byKey(entry.getKey()).ifPresent((recipeHolder) -> {
                recipes.add(recipeHolder);
                createExperience(serverLevel, position, entry.getIntValue(), ((AbstractCookingRecipe) recipeHolder.value()).experience());
            });
        }

        return recipes;
    }

    private static void createExperience(ServerLevel serverLevel, Vec3 position, int amount, float experience) {
        int expAmount = Mth.floor((float)amount * experience);
        float remainder = Mth.frac((float)amount * experience);
        if (remainder != 0.0F && serverLevel.getRandom().nextFloat() < remainder) {
            ++expAmount;
        }

        ExperienceOrb.award(serverLevel, position, expAmount);
    }

    public void fillStackedContents(StackedItemContents stackedItemContents) {
        for(ItemStack itemStack : this.items) {
            stackedItemContents.accountStack(itemStack);
        }
    }

    public void preRemoveSideEffects(BlockPos blockPos, BlockState blockState) {
        super.preRemoveSideEffects(blockPos, blockState);
        if (this.level instanceof ServerLevel serverLevel) {
            this.getRecipesToAwardAndPopExperience(serverLevel, Vec3.atCenterOf(blockPos));
        }
    }

    static {
        RECIPES_USED_CODEC = Codec.unboundedMap(Recipe.KEY_CODEC, Codec.INT);
    }
}

