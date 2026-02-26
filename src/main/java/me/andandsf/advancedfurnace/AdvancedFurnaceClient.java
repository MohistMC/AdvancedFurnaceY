package me.andandsf.advancedfurnace;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

@Mod(value = AdvancedFurnace.MOD_ID, dist = Dist.CLIENT)
@EventBusSubscriber(modid = AdvancedFurnace.MOD_ID, value = Dist.CLIENT)
public class AdvancedFurnaceClient {

    public AdvancedFurnaceClient(ModContainer container) {
    }

    @SubscribeEvent
    static void onClientSetup(RegisterMenuScreensEvent event) {
        event.register(AdvancedFurnace.ADVANCED_FURNACE_SCREEN_HANDLER.get(), AdvancedFurnaceScreen::new);
    }
}
