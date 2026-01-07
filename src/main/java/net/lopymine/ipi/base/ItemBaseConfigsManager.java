package net.lopymine.ipi.base;

import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import net.lopymine.ip.t2o.*;
import net.lopymine.ipi.InventoryInteractions;
import net.lopymine.ipi.config.base.*;
import net.lopymine.ipi.config.physics.ItemPhysicsConfig;
import net.lopymine.ipi.config.vec.Vec2i;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.*;
import net.minecraft.resource.ResourceManager;
import org.jetbrains.annotations.NotNull;

public class ItemBaseConfigsManager {

	public static final Vec2i VANILLA_GRAB = new Vec2i(0, 0);
	public static final Vec2i MIDDLE_CENTER = new Vec2i(8, 8);
	public static final ItemBaseConfig STANDARD_CONFIG = new ItemBaseConfig(VANILLA_GRAB, MIDDLE_CENTER, ItemPhysicsConfig.getNewInstance().get());

	private static final Texture2ObjectPixelFilter GRAB_TEXTURE_FILTER = getColorFilter(RawItemBaseConfig.GRAB_COLOR);
	private static final Texture2ObjectPixelFilter SHAPE_TEXTURE_FILTER = getColorFilter(RawItemBaseConfig.SHAPE_COLOR);

	private static final Texture2Object<Vec2i> PIXEL_POSITION = (x, y, imageWidth, imageHeight, color) -> new Vec2i(x, y);

	public static final String FOLDER_NAME = "i-interactions/base";
	public static Map<Item, ItemBaseConfig> ITEM_CONFIGS = new HashMap<>();

	public static void reload() {
		ITEM_CONFIGS.clear();

		InventoryInteractions.LOGGER.info("Started registering particle configs from resources...");
		ResourceManager resourceManager = MinecraftClient.getInstance().getResourceManager();

		AtomicInteger foundOffsets = new AtomicInteger();
		AtomicInteger registeredConfigs = new AtomicInteger();

		resourceManager.findResources(FOLDER_NAME, (id) -> id.getPath().endsWith(".json5") || id.getPath().endsWith(".json")).forEach((id, resource) -> {
			foundOffsets.getAndIncrement();

			try (InputStream inputStream = resource.getInputStream(); BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
				RawItemBaseConfig config = RawItemBaseConfig.CODEC.decode(JsonOps.INSTANCE, JsonParser.parseReader(reader))/*? if >=1.20.5 {*/.getOrThrow()/*?} else {*//*.getOrThrow(false, InventoryParticlesClient.LOGGER::error)*//*?}*/.getFirst();
				registerItemOffsetFromConfig(config);
				InventoryInteractions.LOGGER.debug("Registered item offset config at \"{}\"", id);
				registeredConfigs.getAndIncrement();
			} catch (Exception e) {
				InventoryInteractions.LOGGER.error("Failed to parse item offset config from \"{}\"! Reason:", id, e);
			}
		});

		InventoryInteractions.LOGGER.info("Registering finished, found: {}, registered: {}", foundOffsets.get(), registeredConfigs.get());
	}

	private static void registerItemOffsetFromConfig(RawItemBaseConfig rawConfig) {
		Item item = rawConfig.getItem();
		if (item == Items.AIR) {
			return;
		}

		Vec2i grabCenter = Optional.of(Texture2ObjectsManager.readFromTexture(rawConfig.getBaseTextureInFolder(),
				"grab center position",
				GRAB_TEXTURE_FILTER,
				PIXEL_POSITION
		)).filter((list) -> !list.isEmpty()).map((list) -> findCenter(list, VANILLA_GRAB)).orElse(VANILLA_GRAB);

		Vec2i massCenter = Optional.of(Texture2ObjectsManager.readFromTexture(rawConfig.getBaseTextureInFolder(),
				"mass center position",
				SHAPE_TEXTURE_FILTER,
				PIXEL_POSITION
		)).filter((list) -> !list.isEmpty()).map((list) -> findCenter(list, MIDDLE_CENTER)).orElse(MIDDLE_CENTER);

		ItemBaseConfig config = new ItemBaseConfig(grabCenter, massCenter, rawConfig.getPhysicsConfig());
		ITEM_CONFIGS.put(item, config);
	}

	private static @NotNull Vec2i findCenter(List<Vec2i> list, Vec2i standardValue) {
		int x = 0;
		int y = 0;
		int c = 0;
		for (Vec2i itemOffset : list) {
			x += itemOffset.offsetX();
			y += itemOffset.offsetY();
			c++;
		}
		if (c == 0) {
			return standardValue;
		}
		return new Vec2i(x / c, y / c);
	}

	public static ItemBaseConfig get(Item item) {
		ItemBaseConfig config = ITEM_CONFIGS.get(item);
		if (config == null) {
			return STANDARD_CONFIG;
		}
		return config;
	}

	private static @NotNull Texture2ObjectPixelFilter getColorFilter(int color) {
		return () -> (x, y, imageWidth, imageHeight, c) -> c == color;
	}
}
