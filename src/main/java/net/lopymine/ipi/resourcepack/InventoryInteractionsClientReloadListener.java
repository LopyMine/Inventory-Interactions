package net.lopymine.ipi.resourcepack;

import java.util.concurrent.*;
import net.fabricmc.fabric.api.resource.*;
import net.lopymine.ipi.InventoryInteractions;
import net.lopymine.ipi.base.ItemBaseConfigsManager;
import net.minecraft.resource.*;
import net.minecraft.util.*;
import net.minecraft.util.profiler.*;

public class InventoryInteractionsClientReloadListener implements IdentifiableResourceReloadListener {

	public static void register() {
		ResourceManagerHelper.get(ResourceType.CLIENT_RESOURCES).registerReloadListener(new InventoryInteractionsClientReloadListener());
	}

	@Override
	public Identifier getFabricId() {
		return InventoryInteractions.id("%s-reload-listener".formatted(InventoryInteractions.MOD_ID));
	}

	@Override
	public CompletableFuture<Void> reload(Synchronizer synchronizer, ResourceManager manager, /*? if <=1.21.1 {*/ /*Profiler profiler, Profiler applyProfiler, *//*?}*/ Executor prepareExecutor, Executor applyExecutor) {
		return synchronizer.whenPrepared(Unit.INSTANCE).thenRunAsync(() -> {
			//? if >=1.21.2 {
			Profiler profiler = Profilers.get();
			//?}
			profiler.push("listener");
			this.reloadStuff();
			profiler.pop();
		}, applyExecutor);
	}

	public void reloadStuff() {
		ItemBaseConfigsManager.reload();
	}
}
