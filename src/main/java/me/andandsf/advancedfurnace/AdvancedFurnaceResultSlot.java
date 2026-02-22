package me.andandsf.advancedfurnace;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/**
 * @author Mgazul
 * @date 2026/2/23 06:45
 */
public class AdvancedFurnaceResultSlot extends Slot {
    private final Player player;
    private int removeCount;

    public AdvancedFurnaceResultSlot(Player player, Container container, int i, int j, int k) {
        super(container, i, j, k);
        this.player = player;
    }

    public boolean mayPlace(ItemStack itemStack) {
        return false;
    }

    public ItemStack remove(int i) {
        if (this.hasItem()) {
            this.removeCount += Math.min(i, this.getItem().getCount());
        }

        return super.remove(i);
    }

    public void onTake(Player player, ItemStack itemStack) {
        this.checkTakeAchievements(itemStack);
        super.onTake(player, itemStack);
    }

    protected void onQuickCraft(ItemStack itemStack, int i) {
        this.removeCount += i;
        this.checkTakeAchievements(itemStack);
    }

    protected void checkTakeAchievements(ItemStack itemStack) {
        itemStack.onCraftedBy(this.player, this.removeCount);
        Player var4 = this.player;
        if (var4 instanceof ServerPlayer serverPlayer) {
            Container var5 = this.container;
            if (var5 instanceof AbstractAdvancedFurnaceBlockEntity abstractFurnaceBlockEntity) {
                abstractFurnaceBlockEntity.awardUsedRecipesAndPopExperience(serverPlayer);
            }
        }

        this.removeCount = 0;
    }
}
