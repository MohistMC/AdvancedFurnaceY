package me.andandsf.advancedfurnace;

import java.util.List;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.world.World;

public class UpdateToolItem extends Item {
    public UpdateToolItem(Settings settings) {
        super(settings);
    }

    @Override
    public void appendTooltip(ItemStack stack, World world, List<Text> tooltip, TooltipContext context) {
        super.appendTooltip(stack, world, tooltip, context);
        // 添加带自定义颜色的lore文本
        tooltip.add(Text.literal("仅在先进熔炉完全停止工作的时候才可放置").setStyle(Style.EMPTY.withColor(Formatting.RED)));
    }
}
