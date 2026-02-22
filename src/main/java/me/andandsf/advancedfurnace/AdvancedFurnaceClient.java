package me.andandsf.advancedfurnace;

import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.gui.screens.MenuScreens;

public class AdvancedFurnaceClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        MenuScreens.register(AdvancedFurnace.ADVANCED_FURNACE_SCREEN_HANDLER, AdvancedFurnaceScreen::new);
    }
}
