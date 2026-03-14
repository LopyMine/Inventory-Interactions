package net.lopymine.ipi.base;

import com.mojang.serialization.*;
import java.util.*;
import net.lopymine.ip.config.misc.CachedItem;
import net.lopymine.ip.resourcepack.manager.AbstractConfigsManager;
import net.lopymine.ip.t2o.*;
import net.lopymine.ipi.client.InventoryInteractionsClient;
import net.lopymine.ipi.config.base.*;
import net.lopymine.ipi.config.base.model.*;
import net.lopymine.ipi.config.physics.ItemPhysicsConfig;
import net.lopymine.ipi.config.base.ItemOffset;
import net.lopymine.mossylib.logger.MossyLogger;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import org.jetbrains.annotations.NotNull;

public class BaseConfigsManager extends AbstractConfigsManager<RawItemBaseConfig> {

	public static final ItemOffset STANDARD_MIDDLE_PART_CONNECTION_POS = new ItemOffset(8, 8, 16, 16);
	public static final ItemOffset STANDARD_MIDDLE_BOTTOM_MASS_POS = new ItemOffset(8, 16, 16, 16);
	public static final CursorItemModel STANDARD_MODEL = new CursorItemModel(STANDARD_MIDDLE_PART_CONNECTION_POS, STANDARD_MIDDLE_BOTTOM_MASS_POS, ItemPhysicsConfig.getNewInstance().get());

	private static final Texture2ObjectPixelFilter NEXT_PART_CONNECTION_TEXTURE_FILTER = getColorFilter(RawItemBaseConfig.NEXT_PART_CONNECTION_COLOR);
	private static final Texture2ObjectPixelFilter PART_CONNECTION_TEXTURE_FILTER = getColorFilter(RawItemBaseConfig.PART_CONNECTION_COLOR);
	private static final Texture2ObjectPixelFilter SHAPE_TEXTURE_FILTER = getColorFilter(RawItemBaseConfig.SHAPE_COLOR);

	private static final Texture2Object<ItemOffset> PIXEL_POSITION = (x, y, imageWidth, imageHeight, color) -> new ItemOffset(x, y, imageWidth, imageHeight);

	public static Map<Item, CursorItemModel> ITEM_MODELS = new HashMap<>();

	private static final BaseConfigsManager INSTANCE = new BaseConfigsManager();

	public static BaseConfigsManager getInstance() {
		return INSTANCE;
	}

	public void reload() {
		ITEM_MODELS.clear();
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
