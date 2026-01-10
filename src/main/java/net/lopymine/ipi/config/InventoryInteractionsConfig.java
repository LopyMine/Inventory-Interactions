package net.lopymine.ipi.config;

import lombok.*;
import net.lopymine.mossylib.utils.*;
import org.slf4j.*;

import com.mojang.serialization.*;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.fabricmc.loader.api.FabricLoader;

import net.lopymine.ipi.InventoryInteractions;

import java.io.*;
import java.util.concurrent.CompletableFuture;

import static net.lopymine.mossylib.utils.CodecUtils.option;

@Getter
@Setter
@AllArgsConstructor
public class InventoryInteractionsConfig {

	public static final Codec<InventoryInteractionsConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			option("mod_enabled", true, Codec.BOOL, InventoryInteractionsConfig::isModEnabled),
			option("debug_log", false, Codec.BOOL, InventoryInteractionsConfig::isDebugLog)
	).apply(instance, InventoryInteractionsConfig::new));

	private static final File CONFIG_FILE = FabricLoader.getInstance().getConfigDir().resolve(InventoryInteractions.MOD_ID + ".json5").toFile();
	private static final Logger LOGGER = LoggerFactory.getLogger(InventoryInteractions.MOD_NAME + "/Config");
	private static InventoryInteractionsConfig INSTANCE;
	
	private boolean modEnabled;
	private boolean debugLog;

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
