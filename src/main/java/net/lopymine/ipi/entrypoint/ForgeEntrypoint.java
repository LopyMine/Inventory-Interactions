package net.lopymine.ipi.entrypoint;

//? if forge {
/*import net.lopymine.ipi.InventoryInteractions;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;

@Mod(InventoryInteractions.MOD_ID)
public class ForgeEntrypoint {

	public ForgeEntrypoint() {
		InventoryInteractions.onInitialize();
		DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> ForgeClientEntrypoint::onInitializeClient);
	}

}

*///?}
