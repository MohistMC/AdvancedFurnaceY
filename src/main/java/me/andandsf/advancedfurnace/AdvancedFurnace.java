package me.andandsf.advancedfurnace;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.fabricmc.fabric.api.screenhandler.v1.ScreenHandlerRegistry;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.util.Identifier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class AdvancedFurnace implements ModInitializer {

	public static final String MOD_ID = "advancedfurnace";
	public static final Logger LOGGER = LogManager.getFormatterLogger(MOD_ID);

	public static final Block ADVANCED_FURNACE_BLOCK;

	public static final BlockItem ADVANCED_FURNACE_ITEM;
	public static final Item UPDATE_TOOL_ITEM = new UpdateToolItem(new Item.Settings());

	public static final BlockEntityType<AdvancedFurnaceBlockEntity> ADVANCED_FURNACE_BLOCK_ENTITY;

	public static final ScreenHandlerType<AdvancedFurnaceScreenHandler> ADVANCED_FURNACE_SCREEN_HANDLER;

	public static final Identifier ADVANCED_FURNACE_ID = new Identifier(MOD_ID, "advanced_furnace");


	static {
		ADVANCED_FURNACE_BLOCK = Registry.register(Registries.BLOCK, ADVANCED_FURNACE_ID, new AdvancedFurnaceBlock(FabricBlockSettings.copyOf(Blocks.FURNACE).requiresTool()));
		ADVANCED_FURNACE_ITEM = Registry.register(Registries.ITEM, ADVANCED_FURNACE_ID, new BlockItem(ADVANCED_FURNACE_BLOCK, new Item.Settings()));
		Registry.register(Registries.ITEM, new Identifier(MOD_ID, "update_tool"), UPDATE_TOOL_ITEM);

		ADVANCED_FURNACE_BLOCK_ENTITY = Registry.register(Registries.BLOCK_ENTITY_TYPE, ADVANCED_FURNACE_ID, FabricBlockEntityTypeBuilder.create(AdvancedFurnaceBlockEntity::new, ADVANCED_FURNACE_BLOCK).build(null));

		ADVANCED_FURNACE_SCREEN_HANDLER = ScreenHandlerRegistry.registerSimple(ADVANCED_FURNACE_ID, AdvancedFurnaceScreenHandler::new);
	}

	@Override
	public void onInitialize() {
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.FUNCTIONAL).register(content -> {
            content.add(ADVANCED_FURNACE_ITEM);
            content.add(UPDATE_TOOL_ITEM);
        });
	}
}
