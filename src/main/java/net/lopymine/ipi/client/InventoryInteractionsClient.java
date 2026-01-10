package net.lopymine.ipi.client;

import net.lopymine.ipi.resourcepack.InventoryInteractionsClientReloadListener;
import net.lopymine.mossylib.loader.MossyLoader;
import net.lopymine.mossylib.logger.MossyLogger;
import org.slf4j.*;

import net.fabricmc.api.ClientModInitializer;

import net.lopymine.ipi.InventoryInteractions;

public class InventoryInteractionsClient implements ClientModInitializer {

	public static MossyLogger LOGGER = InventoryInteractions.LOGGER.extend("Client");

	@Override
	public void onInitializeClient() {
		LOGGER.info("{} Client Initialized", InventoryInteractions.MOD_NAME);
		MossyLoader.registerReloadListener(new InventoryInteractionsClientReloadListener());
	}
}
