package net.lopymine.ipi.config.sub;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.io.File;
import java.util.function.Supplier;
import lombok.*;
import net.lopymine.ip.config.sub.InventoryParticlesCacheConfig.CacheInvalidateMode;
import net.lopymine.ipi.InventoryInteractions;
import net.lopymine.ipi.config.InventoryInteractionsConfig;
import net.lopymine.mossylib.loader.MossyLoader;
import net.lopymine.mossylib.utils.CodecUtils;
import org.slf4j.*;
import static com.mojang.serialization.codecs.RecordCodecBuilder.create;
import static net.lopymine.mossylib.utils.CodecUtils.option;

@Getter
@Setter
@AllArgsConstructor
public class InventoryInteractionsMainConfig {

	public static final Codec<InventoryInteractionsMainConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			option("mod_enabled", true, Codec.BOOL, InventoryInteractionsMainConfig::isModEnabled),
			option("debug_mode_enabled", false, Codec.BOOL, InventoryInteractionsMainConfig::isDebugModeEnabled)
	).apply(instance, InventoryInteractionsMainConfig::new));

	private boolean modEnabled;
	private boolean debugModeEnabled;

	public static Supplier<InventoryInteractionsMainConfig> getNewInstance() {
		return () -> CodecUtils.parseNewInstanceHacky(CODEC);
	}
}
