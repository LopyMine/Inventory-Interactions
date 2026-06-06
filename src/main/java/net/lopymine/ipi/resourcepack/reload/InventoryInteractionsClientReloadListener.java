package net.lopymine.ipi.resourcepack.reload;

import java.util.concurrent.*;
import net.lopymine.ip.config.InventoryParticlesConfig;
import net.lopymine.ip.config.sub.InventoryParticlesCacheConfig.CacheInvalidateMode;
import net.lopymine.ipi.InventoryInteractions;
import net.lopymine.ipi.config.InventoryInteractionsConfig;
import net.lopymine.ipi.family.FamilyPhysicsModelsConfigsManager;
import net.lopymine.ipi.family.cache.*;
import net.lopymine.ipi.resourcepack.manager.PhysicsModelsConfigsManager;
import net.lopymine.mossylib.reload.AbstractResourceReloadListener;
import net.minecraft.client.Minecraft;
import net.minecraft.server.packs.resources.ResourceManager;

public class InventoryInteractionsClientReloadListener extends AbstractResourceReloadListener {

	private boolean first = true;

	@Override
	public String getModId() {
		return InventoryInteractions.MOD_ID;
	}

	@Override
	protected void reloadStuff(PreparationBarrier synchronizer, ResourceManager resourceManager, Executor executor, Executor executor1) {
		if (this.first && InventoryInteractionsConfig.getInstance().getCacheConfig().getInvalidateMode() == CacheInvalidateMode.AFTER_GAME_LAUNCH) {
			FamilyInteractionsCacheManager.deleteSilence();
		}
		if (!this.first && InventoryInteractionsConfig.getInstance().getCacheConfig().getInvalidateMode() == CacheInvalidateMode.AFTER_RESOURCE_RELOADING) {
			FamilyInteractionsCacheManager.deleteSilence();
		}
		this.first = false;

		FamilyBaseTextureCacheManager.clear(); // clear old base textures
		FamilyPhysicsModelsConfigsManager.getInstance().reload(); // load family configs
		PhysicsModelsConfigsManager.getInstance().reload(); // load physics models

		if (Minecraft.getInstance().level != null) {
			PhysicsModelsConfigsManager.updateCombinedMap(); // final combine
		}
	}

}
