package net.lopymine.ipi.renderer;

import java.util.List;
import lombok.*;
import lombok.experimental.ExtensionMethod;
import net.lopymine.ip.element.base.*;
import net.lopymine.ipi.config.InventoryInteractionsConfig;
import net.lopymine.ipi.config.model.*;
import net.lopymine.ipi.config.physics.PhysicsConfig;
import net.lopymine.ipi.family.generation.BaseTextureGenerationManager;
import net.lopymine.ipi.renderer.CursorItemRenderer.Renderer;
import net.lopymine.ipi.resourcepack.manager.PhysicsModelsConfigsManager;
import net.lopymine.ipi.utils.DimensionOffset;
import net.lopymine.mossylib.extension.DrawContextExtension;
import net.lopymine.mossylib.utils.ArgbUtils;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.util.Mth;
import net.minecraft.world.item.*;
import org.joml.Vector2d;

@Getter
@Setter
@ExtensionMethod(DrawContextExtension.class)
public class CursorItem extends TickElement implements IMovableElement, IRotatableElement {

	protected PhysicsConfig physicsConfig = PhysicsModelsConfigsManager.STANDARD_MODEL.getPhysicsConfig();
	protected DimensionOffset massCenter = PhysicsModelsConfigsManager.STANDARD_MODEL.getMassCenter();
	protected DimensionOffset grabPos = PhysicsModelsConfigsManager.STANDARD_MODEL.getGrabPos();
	protected double radius = 0.0D;

	protected Item item = Items.AIR;

	protected double x = 0.0D;
	protected double y = 0.0D;

	protected double lastX = 0.0D;
	protected double lastY = 0.0D;

	protected double lastSpeedX = 0.0D;
	protected double lastSpeedY = 0.0D;

	protected double speedX = 0.0D;
	protected double speedY = 0.0D;

	protected double angle = 0.0D;
	protected double lastAngle = 0.0D;

	protected double lastCursorSpeedX = 0.0D;
	protected double lastCursorSpeedY = 0.0D;

	public void reset(Item item, PhysicsModel model, double pivotX, double pivotY) {
		this.physicsConfig = model.getPhysicsConfig();
		this.massCenter    = model.getMassCenter();
		this.grabPos       = model.getGrabPos();
		this.radius        = model.getRadius();
		this.item          = item;

		double radius = model.getRadius();

		DimensionOffset massCenter = model.getMassCenter();
		DimensionOffset grabPos = model.getGrabPos();

		double cos = (massCenter.x() - grabPos.x()) / radius;
		double sin = (massCenter.y() - grabPos.y()) / radius;

		this.x = pivotX + cos * radius;
		this.y = pivotY + sin * radius;

		this.lastX = this.x;
		this.lastY = this.y;

		this.lastSpeedX = 0.0F;
		this.lastSpeedY = 0.0F;

		this.speedX = 0.0F;
		this.speedY = 0.0F;

		double angleDeg = Math.toDegrees(Math.atan2(this.y - pivotY, this.x - pivotX));
		this.angle     = this.normalize360(angleDeg);
		this.lastAngle = this.angle;

		this.lastCursorSpeedX = 0.0D;
		this.lastCursorSpeedY = 0.0D;
	}

	public void render(GuiGraphicsExtractor context, float tickProgress, Renderer drawItem) {
		float renderX = this.getRenderX(tickProgress);
		float renderY = this.getRenderY(tickProgress);
		float renderAngle = this.getRenderAngle(tickProgress);

		context.push();
		context.translate(renderX, renderY, 0F);
		context.rotateZ(renderAngle);
		context.translate((float) (-this.massCenter.getOffsetX() - 0.5F), (float) (-this.massCenter.getOffsetY() - 0.5F), 0);

		drawItem.run(0, 0);

		this.renderDebugDots(context, this.massCenter, this.grabPos);
		context.pop();
		this.renderDebugItemPosition(context, renderX, renderY, tickProgress);
	}

	private void renderDebugDots(GuiGraphicsExtractor context, DimensionOffset massCenter, DimensionOffset grabPos) {
		if (!InventoryInteractionsConfig.getInstance().getMainConfig().isDebugModeEnabled()) {
			return;
		}
		context.fill(0, 0, 1, 1, ArgbUtils.getArgb(255, 255, 255, 0));
		context.fill((int) massCenter.getOffsetX(), (int) massCenter.getOffsetY(), (int) massCenter.getOffsetX() + 2, (int) massCenter.getOffsetY() + 2, PhysicsModelConfig.SHAPE_COLOR);
		context.fill((int) grabPos.getOffsetX(), (int) grabPos.getOffsetY(), (int) grabPos.getOffsetX() + 1, (int) grabPos.getOffsetY() + 1, PhysicsModelConfig.GRAB_POS_COLOR);
		List<DimensionOffset> offsets = BaseTextureGenerationManager.ITEM_SEPARATORS.get(this.item);
		if (offsets != null) {
			for (DimensionOffset offset : offsets) {
				context.fill((int) offset.getOffsetX(), (int) offset.getOffsetY(), (int) offset.getOffsetX() + 1, (int) offset.getOffsetY() + 1, ArgbUtils.getArgb(255, 0, 255, 0));
			}
		}
	}

	private void renderDebugItemPosition(GuiGraphicsExtractor context, float renderX, float renderY, float tickProgress) {
		if (!InventoryInteractionsConfig.getInstance().getMainConfig().isDebugModeEnabled()) {
			return;
		}
		context.push();
		context.translate(renderX, renderY, 0F);
		context.fill(0, 0, 3, 3, ArgbUtils.getArgb(255, 255, 255, 255));

		double localAngle = this.normalize360(Math.toDegrees(Math.atan2((float) Mth.lerp(tickProgress, this.lastSpeedX, this.speedX), (float) Mth.lerp(tickProgress, this.lastSpeedY, this.speedY))));

		context.push();
		context.rotateZ((float) -localAngle);
		context.fill(0, 0, 1, 10, ArgbUtils.getArgb(255, 128, 128, 255));
		context.pop();

		context.pop();
	}

	public float getRenderAngle(float tickProgress) {
		float angle = (float) (this.lastAngle + Mth.wrapDegrees(this.angle - this.lastAngle) * tickProgress);
		double localX = this.massCenter.x() - this.grabPos.x();
		double localY = this.massCenter.y() - this.grabPos.y();
		double localAngle = Math.toDegrees(Math.atan2(localY, localX));
		return (float) this.normalize360(angle - localAngle);
	}

	public float getRenderX(float tickProgress) {
		return (float) Mth.lerp(tickProgress, this.lastX, this.x);
	}

	public float getRenderY(float tickProgress) {
		return (float) Mth.lerp(tickProgress, this.lastY, this.y);
	}

	private double normalize360(double angle) {
		angle %= 360D;
		if (angle < 0f) {
			angle += 360D;
		}
		return angle;
	}

	public void tick(IMovableElement pivot) {
		this.lastAngle = this.angle;

		super.tick();
		this.applyNativeSpeed();

		this.applyPivotImpulse(pivot);
		this.goBack(pivot);

		this.angle = this.normalize360(this.calculateAngleFromCursor2Item(pivot));
	}

	private void applyNativeSpeed() {
		this.lastSpeedX = this.speedX;
		this.lastSpeedY = this.speedY;

		this.speedX = this.x - this.lastX;
		this.speedY = this.y - this.lastY;

		double braking = this.physicsConfig.getBraking();
		this.speedX *= braking;
		this.speedY *= braking;

		this.speedY += 0.5D * this.physicsConfig.getGravity();

		this.lastX = this.x;
		this.lastY = this.y;

		this.x += this.speedX;
		this.y += this.speedY;
	}

	private float calculateAngleFromCursor2Item(IMovableElement pivot) {
		double pivotX = pivot.getX();
		double pivotY = pivot.getY();

		double relativeX = this.x - pivotX;
		double relativeY = this.y - pivotY;
		return (float) Math.toDegrees(Math.atan2(relativeY, relativeX));
	}

	private void goBack(IMovableElement pivot) {
		double pivotX = pivot.getX();
		double pivotY = pivot.getY();

		double relativeX = this.x - pivotX;
		double relativeY = this.y - pivotY;
		double distance = Math.sqrt(relativeX * relativeX + relativeY * relativeY);

		if (distance != 0.0) {
			double difference = (this.radius - distance) / distance;
			this.x += relativeX * difference;
			this.y += relativeY * difference;
		}
	}

	private void applyPivotImpulse(IMovableElement pivot) {
		double cursorSpeedX = pivot.getSpeedX();
		double cursorSpeedY = pivot.getSpeedY();
		double pivotX = pivot.getX();
		double pivotY = pivot.getY();

		double cursorAccelerationX = cursorSpeedX - this.lastCursorSpeedX;
		double cursorAccelerationY = cursorSpeedY - this.lastCursorSpeedY;

		this.lastCursorSpeedX = cursorSpeedX;
		this.lastCursorSpeedY = cursorSpeedY;

		double relativeX = this.x - pivotX;
		double relativeY = this.y - pivotY;
		double distance = Math.sqrt(relativeX * relativeX + relativeY * relativeY);

		if (distance > 1e-6) {
			relativeX /= distance;
			relativeY /= distance;

			double dx = -relativeY;
			double dy = relativeX;

			double acceleration = cursorAccelerationX * dx + cursorAccelerationY * dy;

			double impulse = 0.1 * this.physicsConfig.getCursorImpulseInheritCoefficient();

			this.x += dx * acceleration * impulse;
			this.y += dy * acceleration * impulse;
		}
	}

	public boolean isItemChanged(Item item) {
		return this.item != item;
	}
}
