package net.lopymine.ipi.client;

import net.lopymine.ipi.resourcepack.InventoryInteractionsClientReloadListener;
import org.slf4j.*;

import net.fabricmc.api.ClientModInitializer;

import net.lopymine.ipi.InventoryInteractions;

public class InventoryInteractionsClient implements ClientModInitializer {

	public static Logger LOGGER = LoggerFactory.getLogger(InventoryInteractions.MOD_NAME + "/Client");

	@Override
	public void onInitializeClient() {
		LOGGER.info("{} Client Initialized", InventoryInteractions.MOD_NAME);
		InventoryInteractionsClientReloadListener.register();
	}
}
