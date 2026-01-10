package net.lopymine.ipi.config.base;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import java.util.*;
import lombok.*;
import net.lopymine.ip.config.misc.CachedItem;
import net.lopymine.ipi.InventoryInteractions;
import net.lopymine.ipi.config.base.model.RawItemModelConfig;
import net.lopymine.ipi.config.physics.ItemPhysicsConfig;
import net.lopymine.mossylib.utils.ArgbUtils;
import net.minecraft.util.Identifier;
import static com.mojang.serialization.codecs.RecordCodecBuilder.create;
import static net.lopymine.mossylib.utils.CodecUtils.option;

@Getter
@Setter
@AllArgsConstructor
public class RawItemBaseConfig {

	public static final Identifier DEFAULT_BASE_TEXTURE = Identifier.of("");
	public static final int PART_CONNECTION_COLOR = ArgbUtils.getArgb(255, 255, 0, 0);
	public static final int NEXT_PART_CONNECTION_COLOR = ArgbUtils.getArgb(255, 255, 255, 0);
	public static final int SHAPE_COLOR = ArgbUtils.getArgb(255, 0, 0, 255);

	public static final Codec<List<CachedItem>> CACHED_ITEMS_CODEC = Codec.either(CachedItem.CODEC, CachedItem.CODEC.listOf()).xmap((either) -> {
		Optional<List<CachedItem>> right = either.right();
		return right.orElseGet(() -> either.left().map((cachedItem) -> new ArrayList<>(List.of(cachedItem))).orElse(null));
	}, Either::right);

	public static final Codec<RawItemBaseConfig> CODEC = create((instance) -> instance.group(
			option("base_texture", DEFAULT_BASE_TEXTURE, Identifier.CODEC, RawItemBaseConfig::getBaseTexture),
			option("items", new ArrayList<>(), CACHED_ITEMS_CODEC, RawItemBaseConfig::getCachedItems),
			option("physics", ItemPhysicsConfig.getNewInstance(), ItemPhysicsConfig.CODEC, RawItemBaseConfig::getPhysicsConfig),
			option("custom_model", RawItemModelConfig.DUMMY_MODEL, RawItemModelConfig.CODEC, RawItemBaseConfig::getCustomModelConfig)
	).apply(instance, RawItemBaseConfig::new));

	private Identifier baseTexture;
	private List<CachedItem> cachedItems;
	private ItemPhysicsConfig physicsConfig;
	private RawItemModelConfig customModelConfig;

	public Identifier getBaseTextureInFolder() {
		return Identifier.of(InventoryInteractions.MOD_ID, "textures/i-interactions/%s".formatted(this.baseTexture.getPath()));
	}

}
