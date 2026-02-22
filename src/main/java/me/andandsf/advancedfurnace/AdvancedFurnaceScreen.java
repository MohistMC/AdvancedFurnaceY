package me.andandsf.advancedfurnace;


import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;

public class AdvancedFurnaceScreen extends AbstractContainerScreen<AbstractContainerMenu> {
    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(AdvancedFurnace.MOD_ID, "textures/gui/advanced_furnace.png");
    private final AdvancedFurnaceScreenHandler handler;

    public AdvancedFurnaceScreen(AbstractContainerMenu handler, Inventory inventory, Component title) {
        super(handler, inventory, title);
        this.handler = (AdvancedFurnaceScreenHandler) handler;
    }

    @Override
    protected void renderBg(GuiGraphics matrices, float delta, int mouseX, int mouseY) {
        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;
        matrices.blit(RenderPipelines.GUI_TEXTURED, TEXTURE,  x, y, 0, 0, imageWidth, imageHeight+10, 256, 256);
        if (this.handler.isBurning()) {
            int l = this.handler.getFuelProgress();
            matrices.blitSprite(RenderPipelines.GUI_TEXTURED, TEXTURE, x + 12, y + 52 + 13 - l, 176, 17 + 13 - l, 14, l + 1, 256, 256);
        }

        for (int k = 0; k < 4; k++) {
            int l = this.handler.getCookProgress(k);
            matrices.blitSprite(RenderPipelines.GUI_TEXTURED, TEXTURE,x + 45 + 27 * k, y + 37, 176, 31, 17, l + 1, 256, 256);
        }
    }

    @Override
    protected void init() {
        super.init();
        // Center the title
        titleLabelX = (imageWidth - font.width(title)) / 2;
    }
}
