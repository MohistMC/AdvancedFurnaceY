package me.andandsf.advancedfurnace;


import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class AdvancedFurnaceScreen extends HandledScreen<ScreenHandler> {
    private static final Identifier TEXTURE = Identifier.tryParse(AdvancedFurnace.MOD_ID, "textures/gui/advanced_furnace.png");
    private AdvancedFurnaceScreenHandler handler;

    public AdvancedFurnaceScreen(ScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
        this.handler = (AdvancedFurnaceScreenHandler) handler;
    }

    @Override
    protected void drawBackground(DrawContext matrices, float delta, int mouseX, int mouseY) {
        int x = (width - backgroundWidth) / 2;
        int y = (height - backgroundHeight) / 2;
        matrices.drawTexture(RenderPipelines.GUI_TEXTURED, TEXTURE,  x, y, 0, 0, backgroundWidth, backgroundHeight+10, 256, 256);
        if (this.handler.isBurning()) {
            int l = this.handler.getFuelProgress();
            matrices.drawGuiTexture(RenderPipelines.GUI_TEXTURED, TEXTURE, x + 12, y + 52 + 13 - l, 176, 17 + 13 - l, 14, l + 1, 256, 256);
        }

        for (int k = 0; k < 4; k++) {
            int l = this.handler.getCookProgress(k);
            matrices.drawGuiTexture(RenderPipelines.GUI_TEXTURED, TEXTURE,x + 45 + 27 * k, y + 37, 176, 31, 17, l + 1, 256, 256);
        }
    }

    @Override
    protected void init() {
        super.init();
        // Center the title
        titleX = (backgroundWidth - textRenderer.getWidth(title)) / 2;
    }
}
