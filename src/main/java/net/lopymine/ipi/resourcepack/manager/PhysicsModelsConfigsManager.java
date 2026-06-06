package net.lopymine.ipi.resourcepack.manager;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.*;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import lombok.*;
import net.lopymine.ip.config.misc.CachedItem;
import net.lopymine.ip.resourcepack.manager.AbstractConfigsManager;
import net.lopymine.ip.t2o.*;
import net.lopymine.ipi.client.InventoryInteractionsClient;
import net.lopymine.ipi.config.model.*;
import net.lopymine.ipi.config.physics.*;
import net.lopymine.ipi.family.*;
import net.lopymine.ipi.family.cache.FamilyBaseTextureCacheManager;
import net.lopymine.ipi.utils.DimensionOffset;
import net.lopymine.ipi.family.generation.BaseTextureGenerationManager;
import net.lopymine.mossylib.logger.MossyLogger;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PhysicsModelsConfigsManager extends AbstractConfigsManager<PhysicsModelConfig> {

	public static final String MOD_FOLDER = "iinteractions";

	public static final DimensionOffset MIDDLE_POS = new DimensionOffset(8, 8, 16, 16);
	public static final DimensionOffset MIDDLE_BOTTOM_POS = new DimensionOffset(8, 16, 16, 16);
	public static final PhysicsModel STANDARD_MODEL = new PhysicsModel(MIDDLE_BOTTOM_POS, MIDDLE_POS, PhysicsConfig.getNewInstance().get());

	public static final Texture2ObjectPixelFilter GRAB_POS_FILTER = getColorFilter(PhysicsModelConfig.GRAB_POS_COLOR);
	public static final Texture2ObjectPixelFilter SHAPE_TEXTURE_FILTER = getColorFilter(PhysicsModelConfig.SHAPE_COLOR);

	public static final Texture2Object<DimensionOffset> DIMENSION_OFFSET_GENERATOR =
			(x, y, imageWidth, imageHeight, color) ->
			new DimensionOffset(x, y, imageWidth, imageHeight);

	public static final Map<Identifier, PhysicsModelConfig> REGISTERED_CONFIGS = new HashMap<>();
	public static volatile Map<Item, PhysicsModel> PER_ITEM_PHYSICS_MODELS = new IdentityHashMap<>();
	public static volatile Map<Item, PhysicsModel> COMBINED_MAP = new IdentityHashMap<>();

	public static ReloadInfo RELOAD_INFO = new ReloadInfo();
	private static final AtomicInteger VERSION = new AtomicInteger(0);

	private static final PhysicsModelsConfigsManager INSTANCE = new PhysicsModelsConfigsManager();

	public static PhysicsModelsConfigsManager getInstance() {
		return INSTANCE;
	}

	public static void updateCombinedMap() {
		boolean debug = Boolean.getBoolean("inventory_interactions.debug_generate");
		int currentVersion = VERSION.incrementAndGet();

		Set<Entry<ResourceKey<Item>, Item>> entries = new HashSet<>(BuiltInRegistries.ITEM.entrySet());
		COMBINED_MAP = new IdentityHashMap<>();

		Map<Boolean, List<Entry<ResourceKey<Item>, Item>>> map = entries.stream().collect(Collectors.partitioningBy(
				(entry) -> entry.getKey().identifier().getNamespace().equals("minecraft")
		));

		ReloadInfo reloadInfo = new ReloadInfo();
		RELOAD_INFO = reloadInfo;

		startLinkingFuture(currentVersion, "VANILLA", reloadInfo, map.get(true), debug).handle((reloadData, throwable) -> {
			if (throwable != null) {
				InventoryInteractionsClient.LOGGER.error("Failed to link physics models for VANILLA items:", throwable);
				return null;
			}
			if (reloadData == null || Minecraft.getInstance().level == null) {
				return null;
			}
			Map<Item, PhysicsModel> combinedMap = new IdentityHashMap<>(reloadData.getModels());
			if (VERSION.get() != reloadData.getVersion()) {
				return null;
			}
			COMBINED_MAP = combinedMap;
			return new Pair<>(combinedMap, reloadData);
		}).thenCompose((pair) -> {
			if (pair == null) {
				return CompletableFuture.completedFuture(null);
			}
			Map<Item, PhysicsModel> combinedMap = pair.getFirst();
			ReloadData vanillaReloadData = pair.getSecond();
			reloadInfo.setModdedItems(true);
			return startLinkingFuture(currentVersion, "MODDED", reloadInfo, map.get(false), debug).whenComplete((reloadData, throwable) -> {
				if (throwable != null) {
					InventoryInteractionsClient.LOGGER.error("Failed to link physics models for MODDED items:", throwable);
					return;
				}
				if (reloadData == null || Minecraft.getInstance().level == null || VERSION.get() != reloadData.getVersion()) {
					return;
				}

				FamilyBaseTextureCacheManager.save();

				Map<Item, PhysicsModel> combinedMap2 = new IdentityHashMap<>(combinedMap);
				combinedMap2.putAll(reloadData.getFamilyModels());
				if (debug) {
					combinedMap2.putAll(vanillaReloadData.getFamilyModels());
				}
				if (Minecraft.getInstance().level == null || VERSION.get() != reloadData.getVersion()) {
					return;
				}
				COMBINED_MAP = combinedMap2;
			});
		}).exceptionally(throwable -> {
			InventoryInteractionsClient.LOGGER.error("Failed to update physics models:", throwable);
			return null;
		});
	}

	private static @NonNull CompletableFuture<ReloadData> startLinkingFuture(int currentVersion, String stage, ReloadInfo reloadInfo, Collection<Entry<ResourceKey<Item>, Item>> entries, boolean debug) {
		return CompletableFuture.supplyAsync(() -> {
			InventoryInteractionsClient.LOGGER.info("Started linking physics models for {} items...", stage);
			ReloadData reloadData = new ReloadData(currentVersion);

			reloadInfo.setProgress(0);
			reloadInfo.setTotalItems(entries.size());

			for (Entry<ResourceKey<Item>, Item> entry : entries) {
				if (VERSION.get() != reloadData.getVersion() || Minecraft.getInstance().level == null) {
					InventoryInteractionsClient.LOGGER.warn("Canceled linking physics models for {} items.", stage);
					return null;
				}
				Identifier id = entry.getKey().identifier();
				Item item = entry.getValue();

				reloadInfo.setCurrentItem(id.toString());
				long before = System.currentTimeMillis();
				getPhysicsModelsForItem(debug, id, item, reloadData);
				long after = System.currentTimeMillis();
				reloadInfo.getLastProcessedItemsTime().add(after - before);
				reloadInfo.setProgress(reloadInfo.getProgress() + 1);
			}

			InventoryInteractionsClient.LOGGER.info("Finished linking physics models for {} items!", stage);
			return reloadData;
		});
	}

	private static void getPhysicsModelsForItem(boolean debug, Identifier itemId, Item item, ReloadData reloadData) {
		if (debug) {
			PhysicsModel familyModel = getFamilyPhysicsModel(itemId, item);
			if (familyModel != null) {
				reloadData.getFamilyModels().put(item, familyModel);
			}
			return;
		}

		PhysicsModel model = PER_ITEM_PHYSICS_MODELS.get(item);
		if (model != null) {
			reloadData.getModels().put(item, model);
			return;
		}

		if (!itemId.getNamespace().equals("minecraft")) {
			PhysicsModel familyModel = getFamilyPhysicsModel(itemId, item);
			if (familyModel != null) {
				reloadData.getFamilyModels().put(item, familyModel);
			}
		}
	}

	@Nullable
	private static PhysicsModel getFamilyPhysicsModel(Identifier itemId, Item item) {
		List<FamilyPhysicsModelConfig> list = FamilyPhysicsModelsManager.get(item);

		for (FamilyPhysicsModelConfig config : list) {
			BaseTexture baseTexture = BaseTextureGenerationManager.generateBaseTexture(itemId, item, config.getGrabCorner());
			if (baseTexture == null) {
				continue;
			}

			return new PhysicsModel(baseTexture.massCenter(), baseTexture.grabPos(), config.getPhysics());
		}

		return null;
	}

	public static DimensionOffset checkAndOffsetMassCenterDistance(DimensionOffset massCenter, DimensionOffset grabPos) {
		double minDistance = 6.7D;

		double dx = massCenter.getOffsetX() - grabPos.getOffsetX();
		double dy = massCenter.getOffsetY() - grabPos.getOffsetY();

		double distance = Math.hypot(dx, dy);

		if (distance > 0D && distance < minDistance) {
			double scale = minDistance / distance;

			double x = grabPos.getOffsetX() + dx * scale;
			double y = grabPos.getOffsetY() + dy * scale;

			return new DimensionOffset(x, y, massCenter.width(), massCenter.height());
		}

		return massCenter;
	}

	@Override
	public void reload() {
		BaseTextureGenerationManager.ITEM_SEPARATORS.clear();
		VERSION.incrementAndGet();
		COMBINED_MAP = new IdentityHashMap<>();
		REGISTERED_CONFIGS.clear();
		PER_ITEM_PHYSICS_MODELS.clear();
		super.reload();
	}

	@Override
	protected String getFolderName() {
		return "%s/models".formatted(MOD_FOLDER);
	}

	@Override
	protected Codec<PhysicsModelConfig> getCodec() {
		return PhysicsModelConfig.CODEC;
	}

	@Override
	protected String getConfigName() {
		return "physics model config";
	}

	@Override
	protected MossyLogger getLogger() {
		return InventoryInteractionsClient.LOGGER;
	}

	@Override
	protected void registerConfig(PhysicsModelConfig rawConfig, Identifier identifier) {
		REGISTERED_CONFIGS.put(identifier, rawConfig);

		BaseTexture parsed = parseBaseTexture(rawConfig.getBaseTextureInFolder());
		PhysicsModel model = new PhysicsModel(parsed.massCenter(), parsed.grabPos(), rawConfig.getPhysicsConfig());

		for (CachedItem item : rawConfig.getItems()) {
			PER_ITEM_PHYSICS_MODELS.put(item.getItem(), model);
		}
	}

	@Getter
	public static class ReloadData {

		private final int version;
		private final Map<Item, PhysicsModel> models = new IdentityHashMap<>();
		private final Map<Item, PhysicsModel> familyModels = new IdentityHashMap<>();

		public ReloadData(int version) {
			this.version = version;
		}
	}

	@Getter
	@Setter
	public static class ReloadInfo {

		private boolean moddedItems = false;
		private int totalItems = -1;
		private int progress = -1;
		private String currentItem = "air";
		private FixedSizeLongQueue lastProcessedItemsTime = new FixedSizeLongQueue(10);

	}

	public static class FixedSizeLongQueue {

		private final int capacity;
		private final Deque<Long> deque;
		private long sum = 0L;

		@Getter
		private volatile double averageSeconds = 0.0;

		public FixedSizeLongQueue(int capacity) {
			if (capacity <= 0) {
				throw new IllegalArgumentException("Capacity must be positive");
			}

			this.capacity = capacity;
			this.deque = new ArrayDeque<>(capacity);
		}

		public synchronized void add(long value) {
			if (this.deque.size() == this.capacity) {
				long removed = this.deque.removeFirst();
				this.sum -= removed;
			}

			this.deque.addLast(value);
			this.sum += value;

			this.averageSeconds = ((double) this.sum / this.deque.size()) / 1000.0;
		}

	}

	private static @NotNull PhysicsModelsConfigsManager.BaseTexture parseBaseTexture(Identifier baseTexture) {
		DimensionOffset grabPos = Optional.of(Texture2ObjectsManager.readFromTexture(baseTexture,
				 "grab pos",
				GRAB_POS_FILTER,
				DIMENSION_OFFSET_GENERATOR
		)).filter((list) -> !list.isEmpty()).map((list) -> findCenter(list, MIDDLE_POS)).orElse(MIDDLE_POS);

		DimensionOffset massCenter = Optional.of(Texture2ObjectsManager.readFromTexture(baseTexture,
				"mass center",
				SHAPE_TEXTURE_FILTER,
				DIMENSION_OFFSET_GENERATOR
		)).filter((list) -> !list.isEmpty()).map((list) -> findCenter(list, MIDDLE_BOTTOM_POS)).orElse(MIDDLE_BOTTOM_POS);

		return new BaseTexture(PhysicsModelsConfigsManager.checkAndOffsetMassCenterDistance(massCenter, grabPos), grabPos);
	}

	public record BaseTexture(DimensionOffset massCenter, @NotNull DimensionOffset grabPos, @Nullable NativeImage image) {

		public BaseTexture(DimensionOffset massCenter, DimensionOffset grabPos) {
			this(massCenter, grabPos, null);
		}
	}

	public static @NotNull DimensionOffset findCenter(List<DimensionOffset> list, DimensionOffset standardValue) {
		if (list.isEmpty()) {
			return standardValue;
		}

		double x = 0.0D;
		double y = 0.0D;

		DimensionOffset any = list.get(0);

		for (DimensionOffset itemOffset : list) {
			x += itemOffset.x();
			y += itemOffset.y();
		}

		double centerX = x / list.size();
		double centerY = y / list.size();

		return new DimensionOffset(centerX, centerY, any.width(), any.height());
	}

	public static PhysicsModel get(Item item) {
		PhysicsModel config = COMBINED_MAP.get(item);
		if (config == null) {
			return STANDARD_MODEL;
		}
		return config;
	}

	private static @NotNull Texture2ObjectPixelFilter getColorFilter(int color) {
		return () -> (x, y, imageWidth, imageHeight, c) -> c == color;
	}
}
