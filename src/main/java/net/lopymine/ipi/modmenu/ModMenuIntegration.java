package net.lopymine.ipi.modmenu;

import net.lopymine.ipi.InventoryInteractions;
import net.lopymine.ipi.yacl.YACLConfigurationScreen;
import net.lopymine.mossylib.modmenu.AbstractModMenuIntegration;
import net.minecraft.client.gui.screen.Screen;

public class ModMenuIntegration extends AbstractModMenuIntegration {

	@Override
	protected String getModId() {
		return InventoryInteractions.MOD_ID;
	}

	@Override
	protected Screen createConfigScreen(Screen parent) {
		return YACLConfigurationScreen.createScreen(parent);
	}
}
