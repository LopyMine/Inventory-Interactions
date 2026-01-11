package net.lopymine.ipi.resourcepack;

import java.util.concurrent.*;
import net.lopymine.ipi.InventoryInteractions;
import net.lopymine.ipi.base.BaseConfigsManager;
import net.lopymine.mossylib.reload.AbstractResourceReloadListener;
import net.minecraft.server.packs.resources.ResourceManager;

public class InventoryInteractionsClientReloadListener extends AbstractResourceReloadListener {

	@Override
	public String getModId() {
		return InventoryInteractions.MOD_ID;
	}

	@Override
	protected void reloadStuff(PreparationBarrier synchronizer, ResourceManager resourceManager, Executor executor, Executor executor1) {
		BaseConfigsManager.reload();
	}

}
