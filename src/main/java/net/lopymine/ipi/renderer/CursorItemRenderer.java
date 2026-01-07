package net.lopymine.ipi.renderer;

import lombok.Getter;
import lombok.experimental.ExtensionMethod;
import net.lopymine.ip.element.InventoryCursor;
import net.lopymine.ip.renderer.InventoryParticlesRenderer;
import net.lopymine.ipi.base.ItemBaseConfigsManager;
import net.lopymine.ipi.config.base.*;
import net.lopymine.ipi.config.vec.Vec2i;
import net.lopymine.ipi.extension.DrawContextExtension;
import net.lopymine.mossylib.utils.ArgbUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.item.*;

@Getter
@ExtensionMethod(DrawContextExtension.class)
public class CursorItemRenderer {

	private static final CursorItemRenderer INSTANCE = new CursorItemRenderer();

	private final CursorItem cursorItem = new CursorItem();

	private CursorItemRenderer() {
	}

	public static CursorItemRenderer getInstance() {
		return INSTANCE;
	}

	public void render(DrawContext context, ItemStack stack, int mouseX, int mouseY, int originalX, int originalY, Renderer drawItem) {
		Item item = stack.getItem();
		if (item == Items.AIR) {
			drawItem.run(originalX, originalY);
			return;
		}

		this.update(stack.getItem(), mouseX, mouseY);

		float tickProgress = MinecraftClient.getInstance().getRenderTickCounter().getTickProgress(false);

		Vec2i massCenter = this.cursorItem.getMassCenter();
		Vec2i grabCenter = this.cursorItem.getGrabCenter();
		float renderX = this.cursorItem.getRenderX(tickProgress);
		float renderY = this.cursorItem.getRenderY(tickProgress);
		float renderAngle = this.cursorItem.getRenderAngle(tickProgress);

		context.push();
		context.translate(renderX, renderY, 0F);
		context.rotateZ(renderAngle);
		context.translate(-massCenter.offsetX() - 0.5F, -massCenter.offsetY() - 0.5F, 0);
		drawItem.run(0, 0);
		renderDebugDots(context, massCenter, grabCenter);
		context.pop();
		renderDebugItemPosition(context, renderX, renderY);
	}

	private void renderDebugDots(DrawContext context, Vec2i massCenter, Vec2i grabCenter) {
		context.fill(0, 0, 1, 1, ArgbUtils.getArgb(255, 255, 255, 0));
		context.fill(massCenter.offsetX(), massCenter.offsetY(), massCenter.offsetX() + 2, massCenter.offsetY() + 2, RawItemBaseConfig.SHAPE_COLOR);
		context.fill(grabCenter.offsetX(), grabCenter.offsetY(), grabCenter.offsetX() + 1, grabCenter.offsetY() + 1, RawItemBaseConfig.GRAB_COLOR);
	}

	private void renderDebugItemPosition(DrawContext context, float renderX, float renderY) {
		context.push();
		context.translate(renderX, renderY, 0F);
		context.fill(0, 0, 3, 3, ArgbUtils.getArgb(255, 255, 255, 255));
		context.pop();
	}

	public void update(Item item, int cursorX, int cursorY) {
		if (!this.cursorItem.isItemChanged(item)) {
			return;
		}

		ItemBaseConfig config = item == Items.AIR ? ItemBaseConfigsManager.STANDARD_CONFIG : ItemBaseConfigsManager.get(item);
		this.cursorItem.reset(item, config, cursorX, cursorY);
	}

	public void tick(ItemStack stack, int cursorX, int cursorY) {
		CursorItemRenderer.getInstance().update(stack.getItem(), cursorX, cursorY);

		boolean empty = this.cursorItem.getItem() == Items.AIR || stack.isEmpty();
		if (empty) {
			return;
		}

		this.cursorItem.tick();
	}

	public interface Renderer {

		void run(int x, int y);

	}
}