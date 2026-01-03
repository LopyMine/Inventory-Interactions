package net.lopymine.ipi.base;

import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import net.lopymine.ipi.InventoryInteractions;
import net.lopymine.ipi.config.base.*;
import net.lopymine.ipi.config.vec.Vec2i;
import net.lopymine.ipi.t2o.Texture2ObjectsManager;
import net.lopymine.ipi.utils.ArgbUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.*;
import net.minecraft.resource.ResourceManager;

public class ItemBaseConfigsManager {

	public static final Vec2i NO_OFFSET = new Vec2i(0, 0);
	public static final Vec2i MIDDLE_CENTER = new Vec2i(8, 8);
	public static final ItemBaseConfig STANDARD_CONFIG = new ItemBaseConfig(NO_OFFSET, MIDDLE_CENTER);

	public static final String FOLDER_NAME = "base";
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
		Vec2i offset = Optional.of(Texture2ObjectsManager.readFromTexture(rawConfig.getOffsetTexture(), (x, y, color) -> {
			if (ArgbUtils.getAlpha(color) == 0) {
				return null;
			}
			return new Vec2i(x, y);
		})).filter((list) -> !list.isEmpty()).map((list) -> list.get(0)).orElse(NO_OFFSET);

		Vec2i center = Optional.of(Texture2ObjectsManager.readFromTexture(rawConfig.getShapeTexture(), (x, y, color) -> {
			if (ArgbUtils.getAlpha(color) == 0) {
				return null;
			}
			return new Vec2i(x, y);
		})).filter((list) -> !list.isEmpty()).map((list) -> {
			int x = 0;
			int y = 0;
			int c = 0;
			for (Vec2i itemOffset : list) {
				x += itemOffset.offsetX();
				y += itemOffset.offsetY();
				c++;
			}
			if (c == 0) {
				return MIDDLE_CENTER;
			}
			return new Vec2i(x / c, y / c);
		}).orElse(MIDDLE_CENTER);

		ItemBaseConfig config = new ItemBaseConfig(offset, center);
		ITEM_CONFIGS.put(item, config);
	}

	public static ItemBaseConfig get(Item item) {
		ItemBaseConfig config = ITEM_CONFIGS.get(item);
		if (config == null) {
			return STANDARD_CONFIG;
		}
		return config;
	}
}
