package net.lopymine.ipi.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.*;
import com.llamalad7.mixinextras.sugar.Local;
import net.lopymine.ip.element.mod.*;
import net.lopymine.ip.element.mod.spawner.*;
import net.lopymine.ip.element.mod.spawner.context.ParticleSpawnContext;
import net.lopymine.ipi.config.InventoryInteractionsConfig;
import net.lopymine.ipi.renderer.*;
import net.lopymine.ipi.utils.*;
import net.minecraft.util.RandomSource;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ParticleSpawner.class)
public class InventoryParticleMixin {

	@Shadow private @Nullable ParticleSpawnArea spawnArea;

	@WrapOperation(
			at = @At(
					value = "INVOKE",
					target = "Lnet/lopymine/ip/element/mod/spawner/ParticleSpawner;offsetParticlePos(Lnet/lopymine/ip/element/mod/InventoryParticle;)V",
					remap = false
			),
			method = "createParticles(ILnet/lopymine/ip/element/mod/spawner/context/ParticleSpawnContext;Ljava/util/function/Consumer;)Ljava/util/List;",
			remap = false
	)
	private void offsetParticlePosWithRotation(
			ParticleSpawner instance,
			InventoryParticle particle,
			Operation<Void> original,
			@Local(argsOnly = true) ParticleSpawnContext context
	) {
		if (!InventoryInteractionsConfig.getInstance().getMainConfig().isModEnabled()) {
			original.call(instance, particle);
			return;
		}
		if (context != ParticleSpawnContext.CURSOR_CONTEXT) {
			original.call(instance, particle);
			return;
		}

		CursorItem cursorItem = CursorItemRenderer.getInstance().getCursorItem();

		DimensionOffset massCenter = cursorItem.getMassCenter();
		IParticleSpawnPos particleSpawnPos = this.spawnArea == null ? null : this.spawnArea.getRandomPos(particle.getRandom());

		double xOffset = 4.0D - massCenter.getOffsetX() - particle.getWidth() / 2.0D + (particleSpawnPos != null ? -particleSpawnPos.getXOffset() + particleSpawnPos.x() : 0.0D);
		double yOffset = 4.0D - massCenter.getOffsetY() - particle.getHeight() / 2.0D + (particleSpawnPos != null ? -particleSpawnPos.getYOffset() + particleSpawnPos.y() : 0.0D);

		RandomSource random = particle.getRandom();

		float progress = random.nextIntBetweenInclusive(0, 100) / 100.0F;
		int randomX = random.nextIntBetweenInclusive(0, 2);
		int randomY = random.nextIntBetweenInclusive(0, 2);

		ParticlePoint previousPoint = this.ipi$getParticlePoint(cursorItem, xOffset, yOffset, 0.0F);
		ParticlePoint currentPoint  = this.ipi$getParticlePoint(cursorItem, xOffset, yOffset, 1.0F);

		double particleX = currentPoint.x() + (previousPoint.x() - currentPoint.x()) * progress + randomX;
		double particleY = currentPoint.y() + (previousPoint.y() - currentPoint.y()) * progress + randomY;

		particle.setX(particleX);
		particle.setY(particleY);

		particle.setLastX(particle.getX());
		particle.setLastY(particle.getY());
	}

	@Unique
	private ParticlePoint ipi$getParticlePoint(
			CursorItem cursorItem,
			double xOffset,
			double yOffset,
			float renderProgress
	) {
		double radians = Math.toRadians(cursorItem.getRenderAngle(renderProgress));
		double cos = Math.cos(radians);
		double sin = Math.sin(radians);

		double x = cursorItem.getRenderX(renderProgress);
		double y = cursorItem.getRenderY(renderProgress);

		double rotatedX = xOffset * cos - yOffset * sin;
		double rotatedY = xOffset * sin + yOffset * cos;

		return new ParticlePoint(
				x - 4.0D + rotatedX,
				y - 4.0D + rotatedY
		);
	}

	@Inject(
			at = @At("HEAD"),
			method = "spawnParticleAtCursorDeltaPath",
			cancellable = true,
			remap = false
	)
	private void offsetParticlePosWithRotationAtDeltaPath(
			InventoryParticle particle,
			InventoryCursor cursor,
			CallbackInfo ci
	) {
		if (!InventoryInteractionsConfig.getInstance().getMainConfig().isModEnabled()) {
			return;
		}

		ci.cancel();
	}

}