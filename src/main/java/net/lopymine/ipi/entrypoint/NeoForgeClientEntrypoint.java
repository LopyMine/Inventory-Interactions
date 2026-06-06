package net.lopymine.ipi.entrypoint;

//? if neoforge {

/*import net.lopymine.ip.entrypoint.IPNeoForgeClientEntrypoint.LevelJoinEvent;
import net.lopymine.ipi.InventoryInteractions;
import net.lopymine.ipi.client.InventoryInteractionsClient;
import net.lopymine.ipi.modmenu.ModMenuIntegration;
import net.lopymine.ipi.resourcepack.manager.PhysicsModelsConfigsManager;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;

@Mod(value = InventoryInteractions.MOD_ID, dist = Dist.CLIENT)
public class NeoForgeClientEntrypoint {

	public NeoForgeClientEntrypoint(ModContainer container) {
		InventoryInteractionsClient.onInitializeClient();
		ModMenuIntegration integration = new ModMenuIntegration();
		integration.register(container);

		NeoForge.EVENT_BUS.addListener(LevelJoinEvent.class, (event) -> {
			PhysicsModelsConfigsManager.updateCombinedMap();
		});
	}

}

*///?}

