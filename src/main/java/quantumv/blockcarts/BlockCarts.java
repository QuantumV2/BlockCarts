package quantumv.blockcarts;

import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.vehicle.MinecartEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.item.Items;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Function;


public class BlockCarts implements ModInitializer {
	public static final String MOD_ID = "blockcarts";

	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
	//public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
	public static final Item BLOCKCART_ITEM = register("blockcart", BlockCartItem::new, new Item.Settings().maxCount(1));
	public static Item register(String path, Function<Item.Settings, Item> factory, Item.Settings settings) {
		final RegistryKey<Item> registryKey = RegistryKey.of(RegistryKeys.ITEM, Identifier.of(MOD_ID, path));
		return Items.register(registryKey, factory, settings);
	}
	@Override
	public void onInitialize() {
		ItemGroupEvents.modifyEntriesEvent(ItemGroups.REDSTONE).register(content -> {
			content.add(BLOCKCART_ITEM);
		});
		ItemGroupEvents.modifyEntriesEvent(ItemGroups.TOOLS).register(content -> {
			content.add(BLOCKCART_ITEM);
		});

		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.
		// Proceed with mild caution.

		//LOGGER.info("Hello Fabric world!");
	}
}