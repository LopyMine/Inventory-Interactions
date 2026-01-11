package net.lopymine.ipi.entrypoint;

//? if fabric {
import net.fabricmc.api.ClientModInitializer;
import net.lopymine.ipi.client.InventoryInteractionsClient;

public class FabricClientEntrypoint implements ClientModInitializer {

	@Override
	public void onInitializeClient() {
		InventoryInteractionsClient.onInitializeClient();
	}
}

//?}
