package net.lopymine.ipi.renderer;

import lombok.Getter;
import lombok.experimental.ExtensionMethod;
import net.lopymine.ip.element.InventoryCursor;
import net.lopymine.ip.element.base.IMovableElement;
import net.lopymine.ip.renderer.InventoryParticlesRenderer;
import net.lopymine.ipi.base.BaseConfigsManager;
import net.lopymine.ipi.config.base.model.CursorItemModel;
import net.lopymine.mossylib.extension.DrawContextExtension;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.*;

@Getter
@ExtensionMethod(DrawContextExtension.class)
public class CursorItemRenderer {

	private static final CursorItemRenderer INSTANCE = new CursorItemRenderer();

	private final CursorItemPart cursorItem = new CursorItemPart();

	private CursorItemRenderer() {
	}

	public static CursorItemRenderer getInstance() {
		return INSTANCE;
	}

	public void render(GuiGraphics context, ItemStack stack, int mouseX, int mouseY, int originalX, int originalY, Renderer drawItem) {
		Item item = stack.getItem();
		if (item == Items.AIR) {
			drawItem.run(originalX, originalY);
			return;
		}

		InventoryCursor cursor = InventoryParticlesRenderer.getInstance().getCursor();
		cursor.setMouseX(mouseX);
		cursor.setMouseY(mouseY);

		this.update(stack.getItem(), cursor);

		//? if >=1.21.5 {
		float tickProgress = Minecraft.getInstance().getDeltaTracker().getGameTimeDeltaPartialTick(true);
		//?} elif >=1.21.4 {
		/*float tickProgress = Minecraft.getInstance().getDeltaTracker().getGameTimeDeltaPartialTick(true);
		 *///?} elif >=1.21.1 {
		/*float tickProgress = Minecraft.getInstance().getTimer().getGameTimeDeltaPartialTick(true);
		 *///?} else {
		/*float tickProgress = Minecraft.getInstance().getFrameTime();
		 *///?}

		this.cursorItem.render(context, tickProgress, drawItem);
	}

	public void update(Item item, IMovableElement pivot) {
		if (!this.cursorItem.isItemChanged(item)) {
			return;
		}

		CursorItemModel config = item == Items.AIR ? BaseConfigsManager.STANDARD_MODEL : BaseConfigsManager.get(item);
		this.cursorItem.reset(item, config, pivot.getX(), pivot.getY());
	}

	public void tick(ItemStack stack, IMovableElement pivot) {
		CursorItemRenderer.getInstance().update(stack.getItem(), pivot);

		boolean empty = this.cursorItem.getItem() == Items.AIR || stack.isEmpty();
		if (empty) {
			return;
		}

		this.cursorItem.tick(pivot);
	}

	public interface Renderer {

		void run(int x, int y);

	}
}