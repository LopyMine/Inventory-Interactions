package net.lopymine.ipi.family.gui;

import java.util.*;
import net.lopymine.ip.InventoryParticles;
import net.lopymine.ip.family.cache.FamilyParticlesCacheManager;
import net.lopymine.ip.resourcepack.manager.ParticlesConfigsManager;
import net.lopymine.ipi.resourcepack.base.BaseConfigsManager;
import net.lopymine.ipi.resourcepack.base.BaseConfigsManager.ReloadInfo;
import net.lopymine.mossylib.utils.DrawUtils;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Util;
import static net.lopymine.ip.family.gui.ParticlesLinkingInfo.LOADING_0;
import static net.lopymine.ip.family.gui.ParticlesLinkingInfo.LOADING_1;

public class BaseConfigsLinkingInfo {

	public static void render(GuiGraphics graphics, int x, int y, int mouseX, int mouseY) {
		ReloadInfo reloadInfo = BaseConfigsManager.RELOAD_INFO;
		int progress = reloadInfo.getProgress();
		int totalItems = reloadInfo.getTotalItems();
		if (progress == -1 || progress == totalItems) {
			return;
		}

		boolean hovered = mouseX >= x && mouseY >= y && mouseX < x + 8 && mouseY < y + 8;
		if (hovered) {
			double averageSeconds = reloadInfo.getLastProcessedItemsTime().getAverageSeconds();
			String averageTime = averageSeconds < 0.01D ? "<0.01" : String.format(Locale.US, "%.2f", averageSeconds);
			String remainingTime = String.format(Locale.US, "%.2f", Math.max(0D, (averageSeconds * (totalItems - progress))) / 60D);

			List<ClientTooltipComponent> components = new ArrayList<>();
			components.add(ClientTooltipComponent.create(InventoryParticles.text("particles_linking.title").append("                                     ").getVisualOrderText()));
			components.add(ClientTooltipComponent.create(Component.literal(reloadInfo.getCurrentItem()).getVisualOrderText()));
			components.add(ClientTooltipComponent.create(InventoryParticles.text("particles_linking.loading", progress, totalItems).getVisualOrderText()));
			components.add(ClientTooltipComponent.create(InventoryParticles.text("particles_linking.average_time", averageTime).getVisualOrderText()));
			components.add(ClientTooltipComponent.create(InventoryParticles.text("particles_linking.remaining_time", remainingTime).getVisualOrderText()));

			DrawUtils.drawTooltip(graphics, components, mouseX, mouseY + 15);
		}
		DrawUtils.drawTexture(graphics, getSprite(), x, y, 0, 0, 8, 8, 8, 8);
	}


	private static Identifier getSprite() {
		return Util.getMillis() / 1000L % 2L == 0L ? LOADING_0 : LOADING_1;
	}

}
