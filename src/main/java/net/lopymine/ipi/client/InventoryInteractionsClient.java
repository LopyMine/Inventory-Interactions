package net.lopymine.ipi.client;

import net.lopymine.ipi.resourcepack.InventoryInteractionsClientReloadListener;
import net.lopymine.mossylib.loader.MossyLoader;
import net.lopymine.mossylib.logger.MossyLogger;

import net.lopymine.ipi.InventoryInteractions;

public class InventoryInteractionsClient {

	public static MossyLogger LOGGER = InventoryInteractions.LOGGER.extend("Client");

	public static void onInitializeClient() {
		LOGGER.info("{} Client Initialized", InventoryInteractions.MOD_NAME);
		MossyLoader.registerReloadListener(new InventoryInteractionsClientReloadListener());
	}
}
