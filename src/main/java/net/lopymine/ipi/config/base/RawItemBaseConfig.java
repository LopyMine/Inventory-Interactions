package net.lopymine.ipi.config.base;

import com.mojang.serialization.Codec;
import lombok.*;
import net.lopymine.ip.config.misc.CachedItem;
import net.lopymine.ipi.InventoryInteractions;
import net.lopymine.ipi.config.physics.ItemPhysicsConfig;
import net.lopymine.mossylib.utils.ArgbUtils;
import net.minecraft.item.Item;
import net.minecraft.util.Identifier;
import static com.mojang.serialization.codecs.RecordCodecBuilder.create;
import static net.lopymine.mossylib.utils.CodecUtils.option;

@Getter
@Setter
@AllArgsConstructor
public class RawItemBaseConfig {

	public static final Identifier DEFAULT_BASE_TEXTURE = Identifier.of("");
	public static final int GRAB_COLOR = ArgbUtils.getArgb(255, 255, 0, 0);
	public static final int SHAPE_COLOR = ArgbUtils.getArgb(255, 0, 0, 255);

	public static final Codec<RawItemBaseConfig> CODEC = create((instance) -> instance.group(
			option("base_texture", DEFAULT_BASE_TEXTURE, Identifier.CODEC, RawItemBaseConfig::getBaseTexture),
			option("item", new CachedItem(), CachedItem.CODEC, RawItemBaseConfig::getCachedItem),
			option("physics", ItemPhysicsConfig.getNewInstance(), ItemPhysicsConfig.CODEC, RawItemBaseConfig::getPhysicsConfig)
	).apply(instance, RawItemBaseConfig::new));

	private Identifier baseTexture;
	private CachedItem cachedItem;
	private ItemPhysicsConfig physicsConfig;

	public Item getItem() {
		return this.cachedItem.getItem();
	}

	public Identifier getBaseTextureInFolder() {
		return Identifier.of(InventoryInteractions.MOD_ID, "textures/i-interactions/%s".formatted(this.baseTexture.getPath()));
	}

}
