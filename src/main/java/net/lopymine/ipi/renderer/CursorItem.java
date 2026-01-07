package net.lopymine.ipi.renderer;

import lombok.*;
import net.lopymine.ip.element.InventoryCursor;
import net.lopymine.ip.element.base.*;
import net.lopymine.ip.renderer.InventoryParticlesRenderer;
import net.lopymine.ipi.base.ItemBaseConfigsManager;
import net.lopymine.ipi.config.base.ItemBaseConfig;
import net.lopymine.ipi.config.physics.ItemPhysicsConfig;
import net.lopymine.ipi.config.vec.Vec2i;
import net.minecraft.item.*;
import net.minecraft.util.math.MathHelper;

@Getter
@Setter
public class CursorItem extends TickElement implements IMovableElement, IRotatableElement {

	private ItemPhysicsConfig physicsConfig = ItemBaseConfigsManager.STANDARD_CONFIG.getPhysicsConfig();
	private Vec2i massCenter = ItemBaseConfigsManager.STANDARD_CONFIG.getMassCenter();
	private Vec2i grabCenter = ItemBaseConfigsManager.STANDARD_CONFIG.getGrabCenter();
	private double radius = 0.0D;

	private Item item = Items.AIR;

	private double x = 0.0D;
	private double y = 0.0D;

	private double lastX = 0.0D;
	private double lastY = 0.0D;

	private double speedX = 0.0D;
	private double speedY = 0.0D;

	private double angle = 0.0D;
	private double lastAngle = 0.0D;

	private double lastCursorSpeedX = 0.0D;
	private double lastCursorSpeedY = 0.0D;

	public void reset(Item item, ItemBaseConfig config, int cursorX, int cursorY) {
		this.physicsConfig = config.getPhysicsConfig();
		this.massCenter    = config.getMassCenter();
		this.grabCenter    = config.getGrabCenter();
		this.radius        = config.getRadius();
		this.item          = item;

		double radius = config.getRadius();

		Vec2i massCenter = config.getMassCenter();
		Vec2i grabCenter = config.getGrabCenter();

		double cos = (massCenter.offsetX() - grabCenter.offsetX()) / radius;
		double sin = (massCenter.offsetY() - grabCenter.offsetY()) / radius;

		this.x = cursorX + cos * radius;
		this.y = cursorY + sin * radius;

		this.lastX = this.x;
		this.lastY = this.y;

		this.speedX = 0.0F;
		this.speedY = 0.0F;

		double angleDeg = Math.toDegrees(Math.atan2(this.y - cursorY, this.x - cursorX));
		this.angle     = this.normalize360(angleDeg);
		this.lastAngle = this.angle;
	}

	public float getRenderAngle(float tickProgress) {
		float angle = (float) (this.lastAngle + MathHelper.wrapDegrees(this.angle - this.lastAngle) * tickProgress);
		double localX = this.massCenter.offsetX() - this.grabCenter.offsetX();
		double localY = this.massCenter.offsetY() - this.grabCenter.offsetY();
		double localAngle = Math.toDegrees(Math.atan2(localY, localX));
		return (float) this.normalize360(angle - localAngle);
	}

	public float getRenderX(float tickProgress) {
		return (float) MathHelper.lerp(tickProgress, this.lastX, this.x);
	}

	public float getRenderY(float tickProgress) {
		return (float) MathHelper.lerp(tickProgress, this.lastY, this.y);
	}

	private double normalize360(double angle) {
		angle %= 360D;
		if (angle < 0f) {
			angle += 360D;
		}
		return angle;
	}

	public void tick() {
		this.lastAngle = this.angle;

		super.tick();
		this.applyNativeSpeed();

		InventoryCursor cursor = InventoryParticlesRenderer.getInstance().getCursor();
		this.applyCursorImpulse(cursor);
		this.goBack(cursor);

		this.angle = this.normalize360(this.calculateAngleFromCursor2Item(cursor));
	}

	private void applyNativeSpeed() {
		double speedX = this.x - this.lastX;
		double speedY = this.y - this.lastY;

		double braking = 0.99D * this.physicsConfig.getBraking();
		speedX *= braking;
		speedY *= braking;

		speedY += 0.5D * this.physicsConfig.getGravity();

		this.lastX = this.x;
		this.lastY = this.y;

		this.x += speedX;
		this.y += speedY;
	}

	private float calculateAngleFromCursor2Item(InventoryCursor cursor) {
		double mouseX = cursor.getMouseX();
		double mouseY = cursor.getMouseY();

		double relativeX = this.x - mouseX;
		double relativeY = this.y - mouseY;
		return (float) Math.toDegrees(Math.atan2(relativeY, relativeX));
	}

	private void goBack(InventoryCursor cursor) {
		double mouseX = cursor.getMouseX();
		double mouseY = cursor.getMouseY();

		double relativeX = this.x - mouseX;
		double relativeY = this.y - mouseY;
		double distance = Math.sqrt(relativeX * relativeX + relativeY * relativeY);

		if (distance != 0.0) {
			double difference = (this.radius - distance) / distance;
			this.x += relativeX * difference;
			this.y += relativeY * difference;
		}
	}

	private void applyCursorImpulse(InventoryCursor cursor) {
		double cursorSpeedX = cursor.getSpeedX();
		double cursorSpeedY = cursor.getSpeedY();
		double mouseX = cursor.getMouseX();
		double mouseY = cursor.getMouseY();

		double cursorAccelerationX = cursorSpeedX - this.lastCursorSpeedX;
		double cursorAccelerationY = cursorSpeedY - this.lastCursorSpeedY;

		this.lastCursorSpeedX = cursorSpeedX;
		this.lastCursorSpeedY = cursorSpeedY;

		double relativeX = this.x - mouseX;
		double relativeY = this.y - mouseY;
		double distance = Math.sqrt(relativeX * relativeX + relativeY * relativeY);

		if (distance > 1e-6) {
			relativeX /= distance;
			relativeY /= distance;

			double dx = -relativeY;
			double dy = relativeX;

			double acceleration = cursorAccelerationX * dx + cursorAccelerationY * dy;

			double impulse = 0.15 * this.physicsConfig.getCursorImpulseInheritCoefficient();
			this.x += dx * acceleration * impulse;
			this.y += dy * acceleration * impulse;
		}
	}

	public boolean isItemChanged(Item item) {
		return this.item != item;
	}
}
