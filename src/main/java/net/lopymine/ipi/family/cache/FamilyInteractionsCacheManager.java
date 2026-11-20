package net.lopymine.ipi.family.cache;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;
import lombok.experimental.ExtensionMethod;
import net.lopymine.ip.extension.NativeImageExtension;
import net.lopymine.ipi.InventoryInteractions;
import net.lopymine.ipi.client.InventoryInteractionsClient;
import net.lopymine.mossylib.loader.MossyLoader;
import net.minecraft.util.Util;

@ExtensionMethod(NativeImageExtension.class)
public class FamilyInteractionsCacheManager {

	public static final Path FOLDER = MossyLoader.getConfigDir()
			.resolve(InventoryInteractions.MOD_ID.replace("_", "-"))
			.resolve("cache");

	private static boolean clearLoadedOnNextLinking;

	public static void deleteSilence() {
		try {
			delete();
		} catch (IOException e) {
			InventoryInteractionsClient.LOGGER.error("Failed to delete cache folder:", e);
		}
	}

	public static CompletableFuture<Boolean> deleteAsync() {
		return CompletableFuture.supplyAsync(() -> {
			deleteSilence();
			return !Files.exists(FOLDER);
		}, Util.ioPool());
	}

	public static void clearLoaded() {
		FamilyBaseTextureCacheManager.clear();
	}

	public static void clearLoadedOnNextLinking() {
		clearLoadedOnNextLinking = true;
	}

	public static void clearLoadedIfRequested() {
		if (clearLoadedOnNextLinking) {
			clearLoadedOnNextLinking = false;
			clearLoaded();
		}
	}

	public static void delete() throws IOException {
		if (!Files.exists(FOLDER)) {
			return;
		}
		try (Stream<Path> stream = Files.walk(FOLDER).sorted(Comparator.reverseOrder())) {
			stream.forEach(path -> {
				try {
					Files.delete(path);
				} catch (IOException e) {
					InventoryInteractionsClient.LOGGER.error("Failed to delete specific cache path {}:", path, e);
				}
			});
		} catch (IOException e) {
			InventoryInteractionsClient.LOGGER.error("Failed to delete cache folder:", e);
			throw e;
		}
	}
}