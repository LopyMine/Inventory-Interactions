package net.lopymine.ipi.renderer;

import lombok.*;
import lombok.experimental.ExtensionMethod;
import net.lopymine.ip.element.base.*;
import net.lopymine.ipi.base.BaseConfigsManager;
import net.lopymine.ipi.config.InventoryInteractionsConfig;
import net.lopymine.ipi.config.base.*;
import net.lopymine.ipi.config.base.model.CursorItemModel;
import net.lopymine.ipi.config.physics.ItemPhysicsConfig;
import net.lopymine.ipi.renderer.CursorItemRenderer.Renderer;
import net.lopymine.mossylib.extension.DrawContextExtension;
import net.lopymine.mossylib.utils.*;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.item.*;
import org.jetbrains.annotations.Nullable;

@Getter
@Setter
@ExtensionMethod(DrawContextExtension.class)
public class CursorItemPart extends TickElement implements IMovableElement, IRotatableElement {

	protected ItemPhysicsConfig physicsConfig = BaseConfigsManager.STANDARD_MODEL.getPhysicsConfig();
	protected ItemOffset massCenter = BaseConfigsManager.STANDARD_MODEL.getMassCenter();
	protected ItemOffset partConnectionCenter = BaseConfigsManager.STANDARD_MODEL.getPartConnectionCenter();
	@Nullable
	protected ItemOffset nextPartConnectionCenter = BaseConfigsManager.STANDARD_MODEL.getNextPartConnectionCenter();
	protected double radius = 0.0D;

	protected Item item = Items.AIR;

	protected double x = 0.0D;
	protected double y = 0.0D;

	protected double lastX = 0.0D;
	protected double lastY = 0.0D;

	protected double speedX = 0.0D;
	protected double speedY = 0.0D;

	protected double angle = 0.0D;
	protected double lastAngle = 0.0D;

	protected double lastCursorSpeedX = 0.0D;
	protected double lastCursorSpeedY = 0.0D;

	@Nullable
	private CursorItemPart part;
	@Nullable
	private Identifier overrideTexture;

	public void reset(Item item, CursorItemModel model, double pivotX, double pivotY) {
		this.physicsConfig            = model.getPhysicsConfig();
		this.massCenter               = model.getMassCenter();
		this.partConnectionCenter     = model.getPartConnectionCenter();
		this.nextPartConnectionCenter = model.getNextPartConnectionCenter();
		this.radius                   = model.getRadius();
		this.item                     = item;

		double radius = model.getRadius();

		ItemOffset massCenter = model.getMassCenter();
		ItemOffset grabCenter = model.getPartConnectionCenter();

		double cos = (massCenter.x() - grabCenter.x()) / radius;
		double sin = (massCenter.y() - grabCenter.y()) / radius;

		this.x = pivotX + cos * radius;
		this.y = pivotY + sin * radius;

		this.lastX = this.x;
		this.lastY = this.y;

		this.speedX = 0.0F;
		this.speedY = 0.0F;

		double angleDeg = Math.toDegrees(Math.atan2(this.y - pivotY, this.x - pivotX));
		this.angle     = this.normalize360(angleDeg);
		this.lastAngle = this.angle;

		this.part = null;
		CursorItemModel partModel = model.getPartModel();
		if (partModel != null) {
			CursorItemPart part = new CursorItemPart();

			ItemOffset offset = this.nextPartConnectionCenter == null ? BaseConfigsManager.NO_PART_CONNECTION_POS : this.nextPartConnectionCenter;

			part.reset(item, partModel, this.x + offset.getOffsetX(), this.y + offset.getOffsetY());
			this.part = part;
		}

		this.overrideTexture = model.getModelTexture();
	}

	public void render(GuiGraphics context, float tickProgress, Renderer drawItem) {
		float renderX = this.getRenderX(tickProgress);
		float renderY = this.getRenderY(tickProgress);
		float renderAngle = this.getRenderAngle(tickProgress);

		context.push();
		context.translate(renderX, renderY, 0F);
		context.rotateZ(renderAngle);
		context.translate(-this.massCenter.getOffsetX() - 0.5F, -this.massCenter.getOffsetY() - 0.5F, 0);

		if (this.overrideTexture == null) {
			drawItem.run(0, 0);
		} else {
			DrawUtils.drawTexture(context, this.overrideTexture, 0, 0, 0, 0, 16, 16, 16, 16);
		}

		this.renderDebugDots(context, this.massCenter, this.partConnectionCenter);
		context.pop();
		this.renderDebugItemPosition(context, renderX, renderY);

		if (this.part != null) {
			this.part.render(context, tickProgress, drawItem);
		}
	}

	private void renderDebugDots(GuiGraphics context, ItemOffset massCenter, ItemOffset grabCenter) {
		if (!InventoryInteractionsConfig.getInstance().isDebugModeEnabled()) {
			return;
		}
		context.fill(0, 0, 1, 1, ArgbUtils.getArgb(255, 255, 255, 0));
		context.fill(massCenter.getOffsetX(), massCenter.getOffsetY(), massCenter.getOffsetX() + 2, massCenter.getOffsetY() + 2, RawItemBaseConfig.SHAPE_COLOR);
		context.fill(grabCenter.getOffsetX(), grabCenter.getOffsetY(), grabCenter.getOffsetX() + 1, grabCenter.getOffsetY() + 1, RawItemBaseConfig.PART_CONNECTION_COLOR);
	}

	private void renderDebugItemPosition(GuiGraphics context, float renderX, float renderY) {
		if (!InventoryInteractionsConfig.getInstance().isDebugModeEnabled()) {
			return;
		}
		context.push();
		context.translate(renderX, renderY, 0F);
		context.fill(0, 0, 3, 3, ArgbUtils.getArgb(255, 255, 255, 255));
		context.pop();
	}

	public float getRenderAngle(float tickProgress) {
		float angle = (float) (this.lastAngle + Mth.wrapDegrees(this.angle - this.lastAngle) * tickProgress);
		double localX = this.massCenter.x() - this.partConnectionCenter.x();
		double localY = this.massCenter.y() - this.partConnectionCenter.y();
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

		if (this.part != null) {
			this.part.tick(this);
		}
	}

	private void applyNativeSpeed() {
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
