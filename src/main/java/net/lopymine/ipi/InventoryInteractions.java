package net.lopymine.ipi;

import net.lopymine.mossylib.logger.MossyLogger;
import net.minecraft.text.*;
import net.minecraft.util.Identifier;

import net.fabricmc.api.ModInitializer;

public class InventoryInteractions implements ModInitializer {

	public static final String MOD_NAME = /*$ mod_name*/ "Inventory Interactions";
	public static final String MOD_ID = /*$ mod_id*/ "inventory_interactions";
	public static final MossyLogger LOGGER = new MossyLogger(MOD_NAME);

	public static Identifier id(String path) {
		return Identifier.of(MOD_ID, path);
	}

	public static MutableText text(String path, Object... args) {
		return Text.translatable(String.format("%s.%s", MOD_ID, path), args);
	}

	@Override
	public void onInitialize() {
		LOGGER.info("{} Initialized", MOD_NAME);
	}
}