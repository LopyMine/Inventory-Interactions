package net.lopymine.ipi.entrypoint;

//? if fabric {

import net.fabricmc.api.ModInitializer;
import net.lopymine.ipi.InventoryInteractions;

public class FabricEntrypoint implements ModInitializer {

	@Override
	public void onInitialize() {
		InventoryInteractions.onInitialize();
	}
}

//?}
