package net.lopymine.ipi.base;

import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import net.lopymine.ip.config.misc.CachedItem;
import net.lopymine.ip.t2o.*;
import net.lopymine.ipi.InventoryInteractions;
import net.lopymine.ipi.client.InventoryInteractionsClient;
import net.lopymine.ipi.config.base.*;
import net.lopymine.ipi.config.base.model.*;
import net.lopymine.ipi.config.physics.ItemPhysicsConfig;
import net.lopymine.ipi.config.base.ItemOffset;
import net.minecraft.client.Minecraft;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import org.jetbrains.annotations.NotNull;

public class BaseConfigsManager {

	public static final ItemOffset NO_PART_CONNECTION_POS = new ItemOffset(0, 0, 16, 16);
	public static final ItemOffset MIDDLE_CENTER = new ItemOffset(8, 8, 16, 16);
	public static final CursorItemModel STANDARD_MODEL = new CursorItemModel(NO_PART_CONNECTION_POS, MIDDLE_CENTER, ItemPhysicsConfig.getNewInstance().get());

	private static final Texture2ObjectPixelFilter NEXT_PART_CONNECTION_TEXTURE_FILTER = getColorFilter(RawItemBaseConfig.NEXT_PART_CONNECTION_COLOR);
	private static final Texture2ObjectPixelFilter PART_CONNECTION_TEXTURE_FILTER = getColorFilter(RawItemBaseConfig.PART_CONNECTION_COLOR);
	private static final Texture2ObjectPixelFilter SHAPE_TEXTURE_FILTER = getColorFilter(RawItemBaseConfig.SHAPE_COLOR);

	private static final Texture2Object<ItemOffset> PIXEL_POSITION = (x, y, imageWidth, imageHeight, color) -> new ItemOffset(x, y, imageWidth, imageHeight);

	public static final String FOLDER_NAME = "i-interactions/base";
	public static Map<Item, CursorItemModel> ITEM_MODELS = new HashMap<>();

	public static void reload() {
		ITEM_MODELS.clear();

		InventoryInteractionsClient.LOGGER.info("Started registering particle configs from resources...");
		ResourceManager resourceManager = Minecraft.getInstance().getResourceManager();

		AtomicInteger foundOffsets = new AtomicInteger();
		AtomicInteger registeredConfigs = new AtomicInteger();

		resourceManager.listResources(FOLDER_NAME, (id) -> id.getPath().endsWith(".json5") || id.getPath().endsWith(".json")).forEach((id, resource) -> {
			foundOffsets.getAndIncrement();

			try (InputStream inputStream = resource.open(); BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
				RawItemBaseConfig config = RawItemBaseConfig.CODEC.decode(JsonOps.INSTANCE, JsonParser.parseReader(reader))/*? if >=1.20.5 {*/.getOrThrow()/*?} else {*//*.getOrThrow(false, InventoryInteractionsClient.LOGGER::error)*//*?}*/.getFirst();
				registerItemOffsetFromConfig(config);
				InventoryInteractionsClient.LOGGER.debug("Registered item offset config at \"{}\"", id);
				registeredConfigs.getAndIncrement();
			} catch (Exception e) {
				InventoryInteractionsClient.LOGGER.error("Failed to parse item offset config from \"{}\"! Reason:", id, e);
			}
		});

		InventoryInteractionsClient.LOGGER.info("Registering finished, found: {}, registered: {}", foundOffsets.get(), registeredConfigs.get());
	}

	private static void registerItemOffsetFromConfig(RawItemBaseConfig rawConfig) {
		CursorItemModel model;
		if (rawConfig.getCustomModelConfig() != RawItemModelConfig.DUMMY_MODEL) {
			model = createCustomCursorItemModel(rawConfig.getCustomModelConfig());
		} else {
			ParsedBaseTexture parsed = parseBaseTexture(rawConfig.getBaseTextureInFolder());
			model = new CursorItemModel(null, parsed.partConnectionCenter(), parsed.nextPartConnectionCenter(), parsed.massCenter(), rawConfig.getPhysicsConfig(), null);
		}

		for (CachedItem cachedItem : rawConfig.getCachedItems()) {
			Item item = cachedItem.getItem();
			ITEM_MODELS.put(item, model);
		}
	}

	private static CursorItemModel createCustomCursorItemModel(RawItemModelConfig rawModelConfig) {
		ParsedBaseTexture parsed = parseBaseTexture(rawModelConfig.getBaseTextureInFolder());
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

	private static @NotNull BaseConfigsManager.ParsedBaseTexture parseBaseTexture(Identifier baseTexture) {
		ItemOffset partConnectionCenter = Optional.of(Texture2ObjectsManager.readFromTexture(baseTexture,
				 "part connection center",
				PART_CONNECTION_TEXTURE_FILTER,
				PIXEL_POSITION
		)).filter((list) -> !list.isEmpty()).map((list) -> findCenter(list, NO_PART_CONNECTION_POS)).orElse(NO_PART_CONNECTION_POS);

		ItemOffset nextPartConnectionCenter = Optional.of(Texture2ObjectsManager.readFromTexture(baseTexture,
				"next part connection center",
				NEXT_PART_CONNECTION_TEXTURE_FILTER,
				PIXEL_POSITION
		)).filter((list) -> !list.isEmpty()).map((list) -> findCenter(list, NO_PART_CONNECTION_POS)).orElse(NO_PART_CONNECTION_POS);

		ItemOffset massCenter = Optional.of(Texture2ObjectsManager.readFromTexture(baseTexture,
				"mass center position",
				SHAPE_TEXTURE_FILTER,
				PIXEL_POSITION
		)).filter((list) -> !list.isEmpty()).map((list) -> findCenter(list, MIDDLE_CENTER)).orElse(MIDDLE_CENTER);

		return new ParsedBaseTexture(partConnectionCenter, nextPartConnectionCenter, massCenter);
	}

	private record ParsedBaseTexture(ItemOffset partConnectionCenter, ItemOffset nextPartConnectionCenter, ItemOffset massCenter) { }

	private static @NotNull ItemOffset findCenter(List<ItemOffset> list, ItemOffset standardValue) {
		int x = 0;
		int y = 0;
		int c = 0;

		@SuppressWarnings("all")
		ItemOffset any = list.isEmpty() ? standardValue : list.get(0);

		for (ItemOffset itemOffset : list) {
			x += itemOffset.x();
			y += itemOffset.y();
			c++;
		}

		if (c == 0) {
			return standardValue;
		}

		return new ItemOffset(x / c, y / c, any.width(), any.height());
	}

	public static CursorItemModel get(Item item) {
		CursorItemModel config = ITEM_MODELS.get(item);
		if (config == null) {
			return STANDARD_MODEL;
		}
		return config;
	}

	private static @NotNull Texture2ObjectPixelFilter getColorFilter(int color) {
		return () -> (x, y, imageWidth, imageHeight, c) -> c == color;
	}
}
