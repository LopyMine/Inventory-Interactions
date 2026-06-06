package net.lopymine.ipi.config.sub;

import com.mojang.serialization.Codec;
import java.util.function.Supplier;
import lombok.*;
import net.lopymine.ip.config.sub.InventoryParticlesCacheConfig.CacheInvalidateMode;
import net.lopymine.mossylib.utils.CodecUtils;
import static com.mojang.serialization.codecs.RecordCodecBuilder.create;
import static net.lopymine.mossylib.utils.CodecUtils.option;

@Getter
@Setter
@AllArgsConstructor
public class InventoryInteractionsCacheConfig {

	public static final Codec<InventoryInteractionsCacheConfig> CODEC = create((instance) -> instance.group(
			option("invalidate_mode", CacheInvalidateMode.MANUAL_INVALIDATE, CacheInvalidateMode.CODEC, InventoryInteractionsCacheConfig::getInvalidateMode)
	).apply(instance, InventoryInteractionsCacheConfig::new));

	private CacheInvalidateMode invalidateMode;

	public static Supplier<InventoryInteractionsCacheConfig> getNewInstance() {
		return () -> CodecUtils.parseNewInstanceHacky(CODEC);
	}
}
