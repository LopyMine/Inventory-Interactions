package net.lopymine.ipi.renderer;

import lombok.experimental.ExtensionMethod;
import net.lopymine.ip.element.InventoryCursor;
import net.lopymine.ipi.config.base.ItemBaseConfig;
import net.lopymine.ipi.base.*;
import net.lopymine.ipi.config.vec.Vec2i;
import net.lopymine.ipi.extension.DrawContextExtension;
import net.lopymine.ipi.utils.ArgbUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.item.*;
import net.minecraft.util.math.MathHelper;

@ExtensionMethod(DrawContextExtension.class)
public class CursorItemRenderer {

	private static final CursorItemRenderer INSTANCE = new CursorItemRenderer();

	private Item currentItem = Items.AIR;
	private ItemBaseConfig currentConfig = ItemBaseConfigsManager.STANDARD_CONFIG;
	private float speed = 0.0F;
	private float lastAngle = 0.0F;
	private float angle = 0.0F;

	private CursorItemRenderer() { }

	public static CursorItemRenderer getInstance() {
		return INSTANCE;
	}

	public void render(DrawContext context, ItemStack stack, int mouseX, int mouseY, int originalX, int originalY, Renderer drawItem) {
		Item item = stack.getItem();
		if (item == Items.AIR) {
			this.currentItem = Items.AIR;
			this.currentConfig = ItemBaseConfigsManager.STANDARD_CONFIG;
			drawItem.run(originalX, originalY);
			return;
		}

		this.updateItemDataIfChanged(item);
		context.push();
		context.translate(mouseX, mouseY, 0);
		float angle = this.lastAngle + MathHelper.wrapDegrees(this.angle - this.lastAngle) * MinecraftClient.getInstance().getRenderTickCounter().getTickProgress(false);
		context.rotateZ(135F + this.normalize360(angle));
		Vec2i offset = this.currentConfig.getOffset();
		context.push();
		context.translate(-offset.offsetX() - 0.5F, -offset.offsetY() - 0.5F, 0);
		drawItem.run(0, 0);
		context.pop();

		int i = this.currentConfig.getCenter().offsetX();
		int i1 = -this.currentConfig.getCenter().offsetY();

		context.fill(i, i1, i - 1, i1 - 1, ArgbUtils.getArgb(255, 255, 0, 0));

		//int radius = (int) this.currentConfig.getRadius();
		//context.fill(0, 0, radius, -radius, ArgbUtils.getArgb(, 255, 255, 0));

		context.pop();
	}

	private void updateItemDataIfChanged(Item item) {
		if (this.currentItem == item) {
			return;
		}
		this.currentItem = item;
		this.currentConfig = ItemBaseConfigsManager.get(item);
	}

	public void tick(InventoryCursor cursor) {
		if (this.currentItem == Items.AIR || cursor.getCurrentStack().isEmpty()) {
			this.lastAngle = 0.0F;
			this.angle = 0.0F;
			this.speed = 0.0F;
			return;
		}

		this.lastAngle = this.normalize360(this.angle);

		double cx = cursor.getSpeedX();
		double cy = cursor.getSpeedY();
		double radians = Math.toRadians(this.angle % 360F);
		float tangential = (float) ((cx * ((float) Math.cos(radians))) + (cy * ((float) Math.sin(radians))));

		this.speed += (tangential / Math.max(this.currentConfig.getRadius(), 1.0F)) * 10.0F;
		this.speed += ((-0.9F * (float) Math.sin(radians)) / Math.max(this.currentConfig.getRadius(), 1.0F)) * 20.0F;
		this.speed *= 0.98F;
		if (Math.abs(this.speed) < 0.01F) {
			this.speed = 0.0F;
		}
		this.angle = this.normalize360(this.angle + this.speed);
	}

	private float normalize360(float angle) {
		angle %= 360f;
		if (angle < 0f) {
			angle += 360f;
		}
		return angle;
	}


	public interface Renderer {
		void run(int x, int y);
	}
}
