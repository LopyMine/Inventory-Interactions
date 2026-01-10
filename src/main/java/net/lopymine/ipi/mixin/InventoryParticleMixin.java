package net.lopymine.ipi.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.*;
import com.llamalad7.mixinextras.sugar.Local;
import net.lopymine.ip.controller.speed.SpeedController;
import net.lopymine.ip.element.InventoryParticle;
import net.lopymine.ip.spawner.*;
import net.lopymine.ip.spawner.context.ParticleSpawnContext;
import net.lopymine.ipi.config.InventoryInteractionsConfig;
import net.lopymine.ipi.config.base.ItemOffset;
import net.lopymine.ipi.renderer.*;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.random.Random;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;

@Mixin(ParticleSpawner.class)
public class InventoryParticleMixin {

	@Shadow private @Nullable ParticleSpawnArea spawnArea;

	@WrapOperation(at = @At(value = "INVOKE", target = "Lnet/lopymine/ip/spawner/ParticleSpawner;offsetParticlePos(Lnet/lopymine/ip/element/InventoryParticle;)V"), method = "createParticles(ILnet/lopymine/ip/spawner/context/ParticleSpawnContext;Ljava/util/function/Consumer;)Ljava/util/List;")
	private void offsetParticlePosWithRotation(ParticleSpawner instance, InventoryParticle particle, Operation<Void> original, @Local(argsOnly = true) ParticleSpawnContext context) {
		if (!InventoryInteractionsConfig.getInstance().isModEnabled()) {
			original.call(instance, particle);
			return;
		}
		if (context != ParticleSpawnContext.CURSOR_CONTEXT) {
			original.call(instance, particle);
			return;
		}

		CursorItemPart cursorItem = CursorItemRenderer.getInstance().getCursorItem();

		ItemOffset massCenter = cursorItem.getMassCenter();
		IParticleSpawnPos particleSpawnPos = this.spawnArea == null ? null : this.spawnArea.getRandomPos(particle.getRandom());
		double xOffset = 4F - massCenter.getOffsetX() - (particle.getWidth() / 2.0D)  + (particleSpawnPos != null ? -particleSpawnPos.getXOffset() + particleSpawnPos.x() : 0);
		double yOffset = 4F - massCenter.getOffsetY() - (particle.getHeight() / 2.0D) + (particleSpawnPos != null ? -particleSpawnPos.getYOffset() + particleSpawnPos.y() : 0);

		float tickProgress = MinecraftClient.getInstance().getRenderTickCounter().getTickProgress(false);

		double radians = Math.toRadians(cursorItem.getRenderAngle(tickProgress));
		double cos = Math.cos(radians);
		double sin = Math.sin(radians);

		double x = cursorItem.getRenderX(tickProgress);
		double y = cursorItem.getRenderY(tickProgress);

		double rotatedX = xOffset * cos - yOffset * sin;
		double rotatedY = xOffset * sin + yOffset * cos;

		particle.setX(x - 4F + rotatedX);
		particle.setY(y - 4F + rotatedY);

		particle.setLastX(particle.getX());
		particle.setLastY(particle.getY());
	}

}