package me.andandsf.advancedfurnace;

import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.gui.screen.ingame.HandledScreens;

public class AdvancedFurnaceClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        HandledScreens.register(AdvancedFurnace.ADVANCED_FURNACE_SCREEN_HANDLER, AdvancedFurnaceScreen::new);
    }
}
