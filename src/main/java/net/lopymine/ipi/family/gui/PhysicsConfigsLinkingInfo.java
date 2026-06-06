package net.lopymine.ipi.family.gui;

import java.util.*;
import net.lopymine.ip.InventoryParticles;
import net.lopymine.ipi.InventoryInteractions;
import net.lopymine.ipi.resourcepack.manager.PhysicsModelsConfigsManager;
import net.lopymine.ipi.resourcepack.manager.PhysicsModelsConfigsManager.ReloadInfo;
import net.lopymine.mossylib.utils.DrawUtils;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Util;

public class PhysicsConfigsLinkingInfo {

	public static final Identifier LOADING_0 = InventoryInteractions.id("textures/gui/loading_0.png");
	public static final Identifier LOADING_1 = InventoryInteractions.id("textures/gui/loading_1.png");

	public static void render(GuiGraphicsExtractor graphics, int x, int y, int mouseX, int mouseY) {
		ReloadInfo reloadInfo = PhysicsModelsConfigsManager.RELOAD_INFO;
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
			components.add(ClientTooltipComponent.create(InventoryInteractions.text("models_linking.title").append("                                     ").getVisualOrderText()));
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
