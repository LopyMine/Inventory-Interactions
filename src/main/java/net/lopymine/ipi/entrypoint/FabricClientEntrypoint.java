package net.lopymine.ipi.entrypoint;

//? if fabric {
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.lopymine.ipi.client.InventoryInteractionsClient;
import net.lopymine.ipi.resourcepack.manager.PhysicsModelsConfigsManager;

public class FabricClientEntrypoint implements ClientModInitializer {

	@Override
	public void onInitializeClient() {
		InventoryInteractionsClient.onInitializeClient();

		ClientPlayConnectionEvents.JOIN.register((aa, bb, vv) -> {
			PhysicsModelsConfigsManager.updateCombinedMap();
		});
	}
}

//?}
