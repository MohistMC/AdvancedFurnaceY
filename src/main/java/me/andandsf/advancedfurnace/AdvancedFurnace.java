package me.andandsf.advancedfurnace;

import java.util.function.Function;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class AdvancedFurnace implements ModInitializer {

	public static final String MOD_ID = "advancedfurnace";
	public static final Logger LOGGER = LogManager.getFormatterLogger(MOD_ID);

	public static final Block ADVANCED_FURNACE_BLOCK;

	public static final Item ADVANCED_FURNACE_ITEM;
	public static final Item UPDATE_TOOL_ITEM;

	public static final BlockEntityType<AdvancedFurnaceBlockEntity> ADVANCED_FURNACE_BLOCK_ENTITY;

	public static final MenuType<AdvancedFurnaceScreenHandler> ADVANCED_FURNACE_SCREEN_HANDLER;

	public static final Identifier ADVANCED_FURNACE_ID = Identifier.fromNamespaceAndPath(MOD_ID, "advanced_furnace");


	static {
		ADVANCED_FURNACE_BLOCK = register("advanced_furnace", AdvancedFurnaceBlock::new, BlockBehaviour.Properties.ofFullCopy(Blocks.FURNACE).requiresCorrectToolForDrops());
		ADVANCED_FURNACE_ITEM = register("advanced_furnace", settings -> new BlockItem(ADVANCED_FURNACE_BLOCK, settings), new Item.Properties());
		UPDATE_TOOL_ITEM = register("update_tool", UpdateToolItem::new, new Item.Properties());
		ADVANCED_FURNACE_BLOCK_ENTITY = Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, ADVANCED_FURNACE_ID, FabricBlockEntityTypeBuilder.create(AdvancedFurnaceBlockEntity::new, ADVANCED_FURNACE_BLOCK).build());

		ADVANCED_FURNACE_SCREEN_HANDLER = Registry.register(BuiltInRegistries.MENU, ADVANCED_FURNACE_ID,  new MenuType<>(AdvancedFurnaceScreenHandler::new, FeatureFlags.VANILLA_SET));
	}

	private static Block register(String path, Function<BlockBehaviour.Properties, Block> factory, BlockBehaviour.Properties settings) {
		final Identifier identifier = Identifier.fromNamespaceAndPath(MOD_ID, path);
		final ResourceKey<Block> registryKey = ResourceKey.create(Registries.BLOCK, identifier);

        return Blocks.register(registryKey, factory, settings);
	}

	public static Item register(String path, Function<Item.Properties, Item> factory, Item.Properties settings) {
		final ResourceKey<Item> registryKey = ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(MOD_ID, path));
		var item = factory.apply(settings.setId(registryKey));
		Registry.register(BuiltInRegistries.ITEM, registryKey, item);
		return item;
	}

	@Override
	public void onInitialize() {
		CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.FUNCTIONAL_BLOCKS).register(content -> {
            content.accept(ADVANCED_FURNACE_ITEM);
            content.accept(UPDATE_TOOL_ITEM);
        });
	}
}
