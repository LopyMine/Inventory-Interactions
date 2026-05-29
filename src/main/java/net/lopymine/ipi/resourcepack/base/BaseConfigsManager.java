package net.lopymine.ipi.resourcepack.base;

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
import net.lopymine.ipi.config.base.*;
import net.lopymine.ipi.config.base.model.*;
import net.lopymine.ipi.config.physics.ItemPhysicsConfig;
import net.lopymine.ipi.config.base.ItemOffset;
import net.lopymine.ipi.family.generation.BaseTextureGenerationManager;
import net.lopymine.mossylib.logger.MossyLogger;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class BaseConfigsManager extends AbstractConfigsManager<RawItemBaseConfig> {

	public static final ItemOffset STANDARD_MIDDLE_PART_CONNECTION_POS = new ItemOffset(8, 8, 16, 16);
	public static final ItemOffset STANDARD_MIDDLE_BOTTOM_MASS_POS = new ItemOffset(8, 16, 16, 16);
	public static final CursorItemModel STANDARD_MODEL = new CursorItemModel(STANDARD_MIDDLE_PART_CONNECTION_POS, STANDARD_MIDDLE_BOTTOM_MASS_POS, ItemPhysicsConfig.getNewInstance().get());
	public static final Texture2ObjectPixelFilter NEXT_PART_CONNECTION_TEXTURE_FILTER = getColorFilter(RawItemBaseConfig.NEXT_PART_CONNECTION_COLOR);
	public static final Texture2ObjectPixelFilter PART_CONNECTION_TEXTURE_FILTER = getColorFilter(RawItemBaseConfig.PART_CONNECTION_COLOR);
	public static final Texture2ObjectPixelFilter SHAPE_TEXTURE_FILTER = getColorFilter(RawItemBaseConfig.SHAPE_COLOR);
	public static final Texture2Object<ItemOffset> PIXEL_POSITION = (x, y, imageWidth, imageHeight, color) -> new ItemOffset(x, y, imageWidth, imageHeight);
	public static final Map<Identifier, RawItemBaseConfig> REGISTERED_CONFIGS = new HashMap<>();
	public static volatile Map<Item, CursorItemModel> PER_ITEM_MODELS = new IdentityHashMap<>();
	public static volatile Map<Item, CursorItemModel> COMBINED_MAP = new IdentityHashMap<>();

	public static ReloadInfo RELOAD_INFO = new ReloadInfo();
	private static final AtomicInteger VERSION = new AtomicInteger(0);

	private static final BaseConfigsManager INSTANCE = new BaseConfigsManager();

	public static BaseConfigsManager getInstance() {
		return INSTANCE;
	}

	public void reload() {
		BaseTextureGenerationManager.ITEM_SEPARATORS.clear();
		VERSION.incrementAndGet();
		COMBINED_MAP = new IdentityHashMap<>();
		REGISTERED_CONFIGS.clear();
		PER_ITEM_MODELS.clear();
		super.reload();
	}

	@Override
	protected String getFolderName() {
		return "i-interactions/base";
	}

	@Override
	protected Codec<RawItemBaseConfig> getCodec() {
		return RawItemBaseConfig.CODEC;
	}

	@Override
	protected String getConfigName() {
		return "particle base rotation config";
	}

	@Override
	protected MossyLogger getLogger() {
		return InventoryInteractionsClient.LOGGER;
	}

	@Override
	protected void registerConfig(RawItemBaseConfig rawConfig, Identifier identifier) {
		REGISTERED_CONFIGS.put(identifier, rawConfig);

		CursorItemModel model;
		if (rawConfig.getCustomModelConfig() != RawItemModelConfig.DUMMY_MODEL) {
			model = createCustomCursorItemModel(rawConfig.getCustomModelConfig());
		} else {
			BaseTexture parsed = parseBaseTexture(rawConfig.getBaseTextureInFolder());
			model = new CursorItemModel(null, parsed.partConnectionCenter(), parsed.nextPartConnectionCenter(), parsed.massCenter(), rawConfig.getPhysicsConfig(), null);
		}

		for (CachedItem item : rawConfig.getItems()) {
			PER_ITEM_MODELS.put(item.getItem(), model);
		}
	}

	public static void updateCombined() {
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
				InventoryInteractionsClient.LOGGER.error("Failed to link base configs for VANILLA items:", throwable);
				return null;
			}
			if (reloadData == null || Minecraft.getInstance().level == null) {
				return null;
			}
			Map<Item, CursorItemModel> combinedMap = new IdentityHashMap<>(reloadData.getModels());
			if (VERSION.get() != reloadData.getVersion()) {
				return null;
			}
			COMBINED_MAP = combinedMap;
			return new Pair<>(combinedMap, reloadData);
		}).thenCompose((pair) -> {
			if (pair == null) {
				return CompletableFuture.completedFuture(null);
			}
			Map<Item, CursorItemModel> combinedMap = pair.getFirst();
			ReloadData vanillaReloadData = pair.getSecond();
			reloadInfo.setModdedItems(true);
			return startLinkingFuture(currentVersion, "MODDED", reloadInfo, map.get(false), debug).whenComplete((reloadData, throwable) -> {
				if (throwable != null) {
					InventoryInteractionsClient.LOGGER.error("Failed to link base configs for MODDED items:", throwable);
					return;
				}
				if (reloadData == null || Minecraft.getInstance().level == null || VERSION.get() != reloadData.getVersion()) {
					return;
				}

				Map<Item, CursorItemModel> combinedMap2 = new IdentityHashMap<>(combinedMap);
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
			InventoryInteractionsClient.LOGGER.error("Failed to update combined base models:", throwable);
			return null;
		});
	}

	private static @NonNull CompletableFuture<ReloadData> startLinkingFuture(int currentVersion, String stage, ReloadInfo reloadInfo, Collection<Entry<ResourceKey<Item>, Item>> entries, boolean debug) {
		return CompletableFuture.supplyAsync(() -> {
			InventoryInteractionsClient.LOGGER.info("Started linking base configs for {} items...", stage);
			ReloadData reloadData = new ReloadData(currentVersion);

			reloadInfo.setProgress(0);
			reloadInfo.setTotalItems(entries.size());

			for (Entry<ResourceKey<Item>, Item> entry : entries) {
				if (VERSION.get() != reloadData.getVersion() || Minecraft.getInstance().level == null) {
					InventoryInteractionsClient.LOGGER.warn("Canceled linking base configs for {} items.", stage);
					return null;
				}
				Identifier id = entry.getKey().identifier();
				Item item = entry.getValue();

				reloadInfo.setCurrentItem(id.toString());
				long before = System.currentTimeMillis();
				getItemModels(debug, id, item, reloadData);
				long after = System.currentTimeMillis();
				reloadInfo.getLastProcessedItemsTime().add(after - before);
				reloadInfo.setProgress(reloadInfo.getProgress() + 1);
			}

			InventoryInteractionsClient.LOGGER.info("Finished linking base configs for {} items!", stage);
			return reloadData;
		});
	}

	private static void getItemModels(boolean debug, Identifier itemId, Item item, ReloadData reloadData) {
		if (debug) {
			CursorItemModel familyModel = getFamilyModel(itemId, item);
			if (familyModel != null) {
				reloadData.getFamilyModels().put(item, familyModel);
			}
			return;
		}

		CursorItemModel model = PER_ITEM_MODELS.get(item);
		if (model != null) {
			reloadData.getModels().put(item, model);
			return;
		}

		if (!itemId.getNamespace().equals("minecraft")) {
			CursorItemModel familyModel = getFamilyModel(itemId, item);
			if (familyModel != null) {
				reloadData.getFamilyModels().put(item, familyModel);
			}
		}
	}

	@Nullable
	private static CursorItemModel getFamilyModel(Identifier itemId, Item item) {
		// FamilyBaseConfigsManager.getFamily();

		BaseTexture baseTexture = BaseTextureGenerationManager.generateBaseTexture(itemId, item);

		if (baseTexture == null || baseTexture.partConnectionCenter() == null) {
			return null;
		}

		return new CursorItemModel(null, baseTexture.partConnectionCenter(), baseTexture.nextPartConnectionCenter(), baseTexture.massCenter(), ItemPhysicsConfig.getNewInstance().get(), null);
	}

	@Getter
	public static class ReloadData {

		private final int version;
		private final Map<Item, CursorItemModel> models = new IdentityHashMap<>();
		private final Map<Item, CursorItemModel> familyModels = new IdentityHashMap<>();

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

	private static CursorItemModel createCustomCursorItemModel(RawItemModelConfig rawModelConfig) {
		BaseTexture parsed = parseBaseTexture(rawModelConfig.getBaseTextureInFolder());
		RawItemModelConfig nextPart = rawModelConfig.getModelPartConfig();

		return new CursorItemModel(
				rawModelConfig.getModelTexture(),
				parsed.partConnectionCenter(),
				parsed.nextPartConnectionCenter(),
				parsed.massCenter(),
				rawModelConfig.getPhysicsConfig(),
				nextPart != null ?
						createCustomCursorItemModel(nextPart)
								:
						null
		);
	}

	private static @NotNull BaseConfigsManager.BaseTexture parseBaseTexture(Identifier baseTexture) {
		ItemOffset partConnectionCenter = Optional.of(Texture2ObjectsManager.readFromTexture(baseTexture,
				 "part connection center",
				PART_CONNECTION_TEXTURE_FILTER,
				PIXEL_POSITION
		)).filter((list) -> !list.isEmpty()).map((list) -> findCenter(list, STANDARD_MIDDLE_PART_CONNECTION_POS)).orElse(STANDARD_MIDDLE_PART_CONNECTION_POS);

		ItemOffset nextPartConnectionCenter = Optional.of(Texture2ObjectsManager.readFromTexture(baseTexture,
				"next part connection center",
				NEXT_PART_CONNECTION_TEXTURE_FILTER,
				PIXEL_POSITION
		)).filter((list) -> !list.isEmpty()).map((list) -> findCenter(list, STANDARD_MIDDLE_PART_CONNECTION_POS)).orElse(STANDARD_MIDDLE_PART_CONNECTION_POS);

		ItemOffset massCenter = Optional.of(Texture2ObjectsManager.readFromTexture(baseTexture,
				"mass center position",
				SHAPE_TEXTURE_FILTER,
				PIXEL_POSITION
		)).filter((list) -> !list.isEmpty()).map((list) -> findCenter(list, STANDARD_MIDDLE_BOTTOM_MASS_POS)).orElse(STANDARD_MIDDLE_BOTTOM_MASS_POS);

		return new BaseTexture(partConnectionCenter, nextPartConnectionCenter, massCenter);
	}

	public record BaseTexture(ItemOffset partConnectionCenter, ItemOffset nextPartConnectionCenter, ItemOffset massCenter) { }

	public static @NotNull ItemOffset findCenter(List<ItemOffset> list, ItemOffset standardValue) {
		if (list.isEmpty()) {
			return standardValue;
		}

		double x = 0.0D;
		double y = 0.0D;

		ItemOffset any = list.get(0);

		for (ItemOffset itemOffset : list) {
			x += itemOffset.getOffsetX();
			y += itemOffset.getOffsetY();
		}

		double centerX = x / list.size();
		double centerY = y / list.size();

		return new ItemOffset(centerX, centerY, any.width(), any.height());
	}

	public static CursorItemModel get(Item item) {
		CursorItemModel config = COMBINED_MAP.get(item);
		if (config == null) {
			return STANDARD_MODEL;
		}
		return config;
	}

	private static @NotNull Texture2ObjectPixelFilter getColorFilter(int color) {
		return () -> (x, y, imageWidth, imageHeight, c) -> c == color;
	}
}
