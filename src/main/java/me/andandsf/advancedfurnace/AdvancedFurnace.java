package me.andandsf.advancedfurnace;

import java.util.function.Function;
import java.util.function.Supplier;
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
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(AdvancedFurnace.MOD_ID)
public class AdvancedFurnace  {

	public static final String MOD_ID = "advancedfurnace";
	public static final Logger LOGGER = LogManager.getFormatterLogger(MOD_ID);

	public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(MOD_ID);
	public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MOD_ID);
	public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, MOD_ID);
	public static final DeferredRegister<MenuType<?>> MENUTYPE_TYPES = DeferredRegister.create(Registries.MENU, MOD_ID);

	public static final DeferredBlock<Block> ADVANCED_FURNACE_BLOCK = BLOCKS.registerBlock("advanced_furnace", AdvancedFurnaceBlock::new, () -> BlockBehaviour.Properties.ofFullCopy(Blocks.FURNACE).requiresCorrectToolForDrops());
	public static final DeferredItem<BlockItem> ADVANCED_FURNACE_ITEM = ITEMS.registerSimpleBlockItem("advanced_furnace", ADVANCED_FURNACE_BLOCK);
	public static final DeferredItem<Item> UPDATE_TOOL_ITEM = ITEMS.registerItem("update_tool", UpdateToolItem::new, Item.Properties::new);
	public static final Supplier<BlockEntityType<AdvancedFurnaceBlockEntity>> ADVANCED_FURNACE_BLOCK_ENTITY = BLOCK_ENTITY_TYPES.register(
			"advanced_furnace",
			() -> new BlockEntityType<>(
					AdvancedFurnaceBlockEntity::new,
					false,
					ADVANCED_FURNACE_BLOCK.get()
			)
	);
	public static final Supplier<MenuType<AdvancedFurnaceScreenHandler>> ADVANCED_FURNACE_SCREEN_HANDLER = MENUTYPE_TYPES.register("advanced_furnace", () -> new MenuType<>(AdvancedFurnaceScreenHandler::new, FeatureFlags.VANILLA_SET));



	public AdvancedFurnace(IEventBus modEventBus, ModContainer modContainer) {
		BLOCKS.register(modEventBus);
		ITEMS.register(modEventBus);
		MENUTYPE_TYPES.register(modEventBus);
		BLOCK_ENTITY_TYPES.register(modEventBus);
		modEventBus.addListener(this::addCreative);
	}

	private void addCreative(BuildCreativeModeTabContentsEvent event) {
		if (event.getTabKey() == CreativeModeTabs.FUNCTIONAL_BLOCKS) {
			event.accept(ADVANCED_FURNACE_ITEM);
			event.accept(UPDATE_TOOL_ITEM);
		}
	}
}
