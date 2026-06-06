package net.lopymine.ipi.config.model;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import java.util.*;
import lombok.*;
import net.lopymine.ip.config.misc.CachedItem;
import net.lopymine.ipi.InventoryInteractions;
import net.lopymine.ipi.config.physics.PhysicsConfig;
import net.lopymine.mossylib.utils.*;
import net.minecraft.resources.Identifier;
import static com.mojang.serialization.codecs.RecordCodecBuilder.create;
import static net.lopymine.mossylib.utils.CodecUtils.option;

@Getter
@Setter
@AllArgsConstructor
public class PhysicsModelConfig {

	public static final Identifier DEFAULT_BASE_TEXTURE = InventoryInteractions.id("default_texture");

	public static final int GRAB_POS_COLOR = ArgbUtils.getArgb(255, 255, 0, 0);
	public static final int SHAPE_COLOR = ArgbUtils.getArgb(255, 0, 0, 255);

	public static final Codec<List<CachedItem>> CACHED_ITEMS_CODEC = Codec.either(CachedItem.CODEC, CachedItem.CODEC.listOf()).xmap((either) -> {
		Optional<List<CachedItem>> right = either.right();
		return right.orElseGet(() -> either.left().map((cachedItem) -> new ArrayList<>(List.of(cachedItem))).orElse(null));
	}, Either::right);

	public static final Codec<PhysicsModelConfig> CODEC = create((instance) -> instance.group(
			option("base_texture", DEFAULT_BASE_TEXTURE, Identifier.CODEC, PhysicsModelConfig::getBaseTexture),
			option("items", new ArrayList<>(), CACHED_ITEMS_CODEC, PhysicsModelConfig::getItems),
			CodecUtils.option("physics", PhysicsConfig.getNewInstance(), PhysicsConfig.CODEC, PhysicsModelConfig::getPhysicsConfig)
	).apply(instance, PhysicsModelConfig::new));

	private Identifier baseTexture;
	private List<CachedItem> items;
	private PhysicsConfig physicsConfig;

	public Identifier getBaseTextureInFolder() {
		return InventoryInteractions.id("textures/iinteractions/models/%s".formatted(this.baseTexture.getPath()));
	}

}
