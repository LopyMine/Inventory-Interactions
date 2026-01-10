package net.lopymine.ipi.yacl;

import lombok.experimental.ExtensionMethod;
import net.lopymine.ipi.InventoryInteractions;
import net.lopymine.mossylib.yacl.api.*;
import net.lopymine.mossylib.yacl.extension.SimpleOptionExtension;
import net.minecraft.client.gui.screen.Screen;

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
				.groups(getMain(defConfig, config));
	}

	private static SimpleGroup getMain(InventoryInteractionsConfig defConfig, InventoryInteractionsConfig config) {
		return SimpleGroup.startBuilder("main_group").options(
				SimpleOption.<Boolean>startBuilder("mod_enabled")
						.withBinding(defConfig.isModEnabled(), config::isModEnabled, config::setModEnabled, false)
						.withController()
						.withDescription(SimpleContent.NONE),
				SimpleOption.<Boolean>startBuilder("debug_log")
						.withBinding(defConfig.isDebugLog(), config::isDebugLog, config::setDebugLog, false)
						.withController()
						.withDescription(SimpleContent.NONE)
		);
	}

}


