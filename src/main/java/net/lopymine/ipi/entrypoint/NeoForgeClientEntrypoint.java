package net.lopymine.ipi.entrypoint;

//? if neoforge {

/*import net.lopymine.ipi.InventoryInteractions;
import net.lopymine.ipi.client.InventoryInteractionsClient;
import net.lopymine.ipi.modmenu.ModMenuIntegration;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;

@Mod(value = InventoryInteractions.MOD_ID, dist = Dist.CLIENT)
public class NeoForgeClientEntrypoint {

	public NeoForgeClientEntrypoint(ModContainer container) {
		InventoryInteractionsClient.onInitializeClient();
		ModMenuIntegration integration = new ModMenuIntegration();
		integration.register(container);
	}

}

*///?}

