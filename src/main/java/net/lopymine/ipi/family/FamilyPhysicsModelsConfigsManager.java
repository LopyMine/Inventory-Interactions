package net.lopymine.ipi.family;

import com.mojang.serialization.Codec;
import java.util.*;
import lombok.Getter;
import net.lopymine.ip.resourcepack.manager.AbstractConfigsManager;
import net.lopymine.ipi.InventoryInteractions;
import net.lopymine.ipi.client.InventoryInteractionsClient;
import net.lopymine.ipi.resourcepack.manager.PhysicsModelsConfigsManager;
import net.lopymine.mossylib.logger.MossyLogger;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;

@Getter
public class FamilyPhysicsModelsConfigsManager extends AbstractConfigsManager<FamilyPhysicsModelConfig> {

	public static final String FOLDER_NAME = "families/models";
	public static final String LEGACY_FOLDER_NAME = "%s/ifamilies/models".formatted(PhysicsModelsConfigsManager.MOD_FOLDER);

	public static final Identifier FALLBACK_CONFIG_ID = InventoryInteractions.id("%s/%s/standard"
			.formatted(InventoryInteractions.MOD_ID, FOLDER_NAME));

	private final Map<Identifier, FamilyPhysicsModelConfig> registeredConfigsMap = new HashMap<>();
	private final List<FamilyPhysicsModelConfig> registeredConfigs = new ArrayList<>();

	private static final FamilyPhysicsModelsConfigsManager INSTANCE = new FamilyPhysicsModelsConfigsManager();

	public static FamilyPhysicsModelsConfigsManager getInstance() {
		return INSTANCE;
	}

	@Override
	protected String getFolderName() {
		return FOLDER_NAME;
	}

	@Override
	protected String getLegacyFolderName() {
		return LEGACY_FOLDER_NAME;
	}

	@Override
	protected Codec<FamilyPhysicsModelConfig> getCodec() {
		return FamilyPhysicsModelConfig.CODEC;
	}

	@Override
	protected String getConfigName() {
		return "physics models family";
	}

	@Override
	protected String getModId() {
		return InventoryInteractions.MOD_ID;
	}

	@Override
	protected MossyLogger getLogger() {
		return InventoryInteractionsClient.LOGGER;
	}

	@Override
	protected void registerConfig(FamilyPhysicsModelConfig config, Identifier id) {
		config.setLocation(id);
		this.registeredConfigsMap.computeIfAbsent(id, (key) -> config);
		this.registeredConfigs.add(config);
	}

	@Override
	public void reload() {
		this.registeredConfigsMap.clear();
		this.registeredConfigs.clear();
		super.reload();
		this.getFallbackConfig();
		this.registeredConfigs.sort(Comparator.comparingInt(FamilyPhysicsModelConfig::getPriority).reversed());
	}

	@NotNull
	public FamilyPhysicsModelConfig getFallbackConfig() {
		FamilyPhysicsModelConfig fallbacks = this.registeredConfigsMap.get(FALLBACK_CONFIG_ID);
		if (fallbacks == null) {
			throw new IllegalArgumentException("Failed to find fallback family config for Inventory Particles!");
		}
		return fallbacks;
	}

}
