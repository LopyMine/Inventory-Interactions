package net.lopymine.ipi.entrypoint;

//? if forge {

/*import net.lopymine.ip.entrypoint.IPForgeClientEntrypoint.LevelJoinEvent;
import net.lopymine.ipi.client.InventoryInteractionsClient;
import net.lopymine.ipi.modmenu.ModMenuIntegration;
import net.lopymine.ipi.resourcepack.manager.PhysicsModelsConfigsManager;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.common.MinecraftForge;

public class ForgeClientEntrypoint {

	public static void onInitializeClient() {
		InventoryInteractionsClient.onInitializeClient();
		ModMenuIntegration integration = new ModMenuIntegration();
		integration.register(ModLoadingContext.get().getActiveContainer());

		MinecraftForge.EVENT_BUS.<LevelJoinEvent>addListener((event) -> {
			PhysicsModelsConfigsManager.updateCombinedMap();
		});
	}

}

*///?}

