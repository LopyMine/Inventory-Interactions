package net.lopymine.ipi.config.base;

import com.mojang.serialization.Codec;
import lombok.*;
import net.lopymine.ip.config.misc.CachedItem;
import net.minecraft.item.Item;
import net.minecraft.util.Identifier;
import static com.mojang.serialization.codecs.RecordCodecBuilder.create;
import static net.lopymine.ipi.utils.CodecUtils.option;

@Getter
@Setter
@AllArgsConstructor
public class RawItemBaseConfig {

	public static final Identifier NO_OFFSET_TEXTURE = Identifier.of("");
	public static final Identifier FULL_SHAPE = Identifier.of("");

	public static final Codec<RawItemBaseConfig> CODEC = create((instance) -> instance.group(
			option("offset_texture", NO_OFFSET_TEXTURE, Identifier.CODEC, RawItemBaseConfig::getOffsetTexture),
			option("shape_texture", FULL_SHAPE, Identifier.CODEC, RawItemBaseConfig::getOffsetTexture),
			option("item", new CachedItem(), CachedItem.CODEC, RawItemBaseConfig::getCachedItem)
	).apply(instance, RawItemBaseConfig::new));

	private Identifier offsetTexture;
	private Identifier shapeTexture;
	private CachedItem cachedItem;

	public Item getItem() {
		return this.cachedItem.getItem();
	}

}
