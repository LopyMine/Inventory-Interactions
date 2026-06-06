package net.lopymine.ipi.yacl;

import dev.isxander.yacl3.api.OptionGroup;
import lombok.experimental.ExtensionMethod;
import net.lopymine.ip.InventoryParticles;
import net.lopymine.ip.config.sub.InventoryParticlesCacheConfig.CacheInvalidateMode;
import net.lopymine.ipi.InventoryInteractions;
import net.lopymine.ipi.config.sub.*;
import net.lopymine.ipi.family.cache.FamilyInteractionsCacheManager;
import net.lopymine.mossylib.yacl.api.*;
import net.lopymine.mossylib.yacl.extension.SimpleOptionExtension;
import net.minecraft.client.gui.screens.Screen;

import net.lopymine.ipi.config.InventoryInteractionsConfig;

@ExtensionMethod(SimpleOptionExtension.class)
public class YACLConfigurationScreen {

	private YACLConfigurationScreen() {
		throw new IllegalStateException("Screen class");
	}

	public static Screen createScreen(Screen parent) {
		InventoryInteractionsConfig defConfig = InventoryInteractionsConfig.getNewInstance();
		InventoryInteractionsConfig config = InventoryInteractionsConfig.getInstance();

		return SimpleYACLScreen.startBuilder(InventoryInteractions.MOD_ID, parent, config::saveAsync)
				.categories(getGeneralCategory(defConfig, config))
				.build();
	}

	private static SimpleCategory getGeneralCategory(InventoryInteractionsConfig defConfig, InventoryInteractionsConfig config) {
		return SimpleCategory.startBuilder("general")
				.groups(getMainGroup(defConfig.getMainConfig(), config.getMainConfig()))
				.custom((builder) -> builder.group(getCacheGroup(defConfig.getCacheConfig(), config.getCacheConfig())));
	}

	private static SimpleGroup getMainGroup(InventoryInteractionsMainConfig defConfig, InventoryInteractionsMainConfig config) {
		return SimpleGroup.startBuilder("main").options(
				SimpleOption.<Boolean>startBuilder("mod_enabled")
						.withBinding(defConfig.isModEnabled(), config::isModEnabled, config::setModEnabled, false)
						.withController()
						.withDescription(SimpleContent.NONE),
				SimpleOption.<Boolean>startBuilder("debug_mode_enabled")
						.withBinding(defConfig.isDebugModeEnabled(), config::isDebugModeEnabled, config::setDebugModeEnabled, false)
						.withController()
						.withDescription(SimpleContent.NONE)
		);
	}

	private static OptionGroup getCacheGroup(InventoryInteractionsCacheConfig defConfig, InventoryInteractionsCacheConfig config) {
		return SimpleGroup.startBuilder("cache").options(
				SimpleOption.<CacheInvalidateMode>startBuilder("invalidate_mode")
						.withBinding(defConfig.getInvalidateMode(), config::getInvalidateMode, config::setInvalidateMode, true)
						.withController(CacheInvalidateMode.class)
						.withDescription(SimpleContent.NONE),
				SimpleOption.startButtonBuilder("invalidate_now", (screen, option) -> {
							FamilyInteractionsCacheManager.deleteSilence();
						}).withDescription(SimpleContent.NONE)
		).build(InventoryParticles.MOD_ID);
	}

}


