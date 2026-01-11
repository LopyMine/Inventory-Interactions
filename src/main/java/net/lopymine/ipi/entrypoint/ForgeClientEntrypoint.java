package net.lopymine.ipi.entrypoint;

//? if forge {

/*import net.lopymine.ipi.client.InventoryInteractionsClient;
import net.lopymine.ipi.modmenu.ModMenuIntegration;
import net.minecraftforge.fml.ModLoadingContext;

public class ForgeClientEntrypoint {

	public static void onInitializeClient() {
		InventoryInteractionsClient.onInitializeClient();
		ModMenuIntegration integration = new ModMenuIntegration();
		integration.register(ModLoadingContext.get().getActiveContainer());
	}

}

*///?}

