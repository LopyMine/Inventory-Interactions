package net.lopymine.ipi.config;

import lombok.*;
import net.lopymine.ipi.config.sub.*;
import net.lopymine.mossylib.loader.MossyLoader;
import net.lopymine.mossylib.utils.*;
import org.slf4j.*;

import com.mojang.serialization.*;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.lopymine.ipi.InventoryInteractions;

import java.io.*;
import java.util.concurrent.CompletableFuture;

import static net.lopymine.mossylib.utils.CodecUtils.option;

@Getter
@Setter
@AllArgsConstructor
public class InventoryInteractionsConfig {

	public static final Codec<InventoryInteractionsConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			option("main", InventoryInteractionsMainConfig.getNewInstance(), InventoryInteractionsMainConfig.CODEC, InventoryInteractionsConfig::getMainConfig),
			option("cache", InventoryInteractionsCacheConfig.getNewInstance(), InventoryInteractionsCacheConfig.CODEC, InventoryInteractionsConfig::getCacheConfig)
	).apply(instance, InventoryInteractionsConfig::new));

	private static final File CONFIG_FILE = MossyLoader.getConfigDir().resolve(InventoryInteractions.MOD_ID + ".json5").toFile();
	private static final Logger LOGGER = LoggerFactory.getLogger(InventoryInteractions.MOD_NAME + "/Config");
	private static InventoryInteractionsConfig INSTANCE;
	
	private InventoryInteractionsMainConfig mainConfig;
	private InventoryInteractionsCacheConfig cacheConfig;

	private InventoryInteractionsConfig() {
		throw new IllegalArgumentException();
	}

	public static InventoryInteractionsConfig getInstance() {
		return INSTANCE == null ? reload() : INSTANCE;
	}

	public static InventoryInteractionsConfig reload() {
		return INSTANCE = InventoryInteractionsConfig.read();
	}

	public static InventoryInteractionsConfig getNewInstance() {
		return CodecUtils.parseNewInstanceHacky(CODEC);
	}

	private static InventoryInteractionsConfig read() {
		return ConfigUtils.readConfig(CODEC, CONFIG_FILE, LOGGER);
	}

	public void saveAsync() {
		CompletableFuture.runAsync(this::save);
	}

	public void save() {
		ConfigUtils.saveConfig(this, CODEC, CONFIG_FILE, LOGGER);
	}
}
