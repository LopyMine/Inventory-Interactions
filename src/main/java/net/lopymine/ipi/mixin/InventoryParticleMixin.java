package net.lopymine.ipi.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.*;
import com.llamalad7.mixinextras.sugar.Local;
import net.lopymine.ip.element.InventoryParticle;
import net.lopymine.ip.spawner.*;
import net.lopymine.ip.spawner.context.ParticleSpawnContext;
import net.lopymine.ipi.base.ItemBaseConfigsManager;
import net.lopymine.ipi.config.vec.Vec2i;
import net.lopymine.ipi.renderer.*;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;

@Mixin(ParticleSpawner.class)
public class InventoryParticleMixin {

	@Shadow private @Nullable ParticleSpawnArea spawnArea;

	@WrapOperation(at = @At(value = "INVOKE", target = "Lnet/lopymine/ip/spawner/ParticleSpawner;offsetParticlePos(Lnet/lopymine/ip/element/InventoryParticle;)V"), method = "createParticles(ILnet/lopymine/ip/spawner/context/ParticleSpawnContext;Ljava/util/function/Consumer;)Ljava/util/List;")
	private void offsetParticlePosWithRotation(ParticleSpawner instance, InventoryParticle particle, Operation<Void> original, @Local(argsOnly = true) ParticleSpawnContext context) {
		if (context != ParticleSpawnContext.CURSOR_CONTEXT) {
			original.call(instance, particle);
			return;
		}

		CursorItem cursorItem = CursorItemRenderer.getInstance().getCursorItem();
//		CursorItemRenderer.getInstance().update(context.getStack().getItem(), context.getX(), context.getY());

		Vec2i massCenter = cursorItem.getMassCenter();
		IParticleSpawnPos particleSpawnPos = this.spawnArea == null ? null : this.spawnArea.getRandomPos(particle.getRandom());
		double xOffset = 4F - massCenter.offsetX() - (particle.getWidth() / 2.0D)  + (particleSpawnPos != null ? -particleSpawnPos.getXOffset() + particleSpawnPos.x() : 0);
		double yOffset = 4F - massCenter.offsetY() - (particle.getHeight() / 2.0D) + (particleSpawnPos != null ? -particleSpawnPos.getYOffset() + particleSpawnPos.y() : 0);

		double radians = Math.toRadians(cursorItem.getRenderAngle(1.0F));
		double cos = Math.cos(radians);
		double sin = Math.sin(radians);

		double rotatedX = xOffset * cos - yOffset * sin;
		double rotatedY = xOffset * sin + yOffset * cos;

		particle.setX(cursorItem.getX() - 4F + rotatedX);
		particle.setY(cursorItem.getY() - 4F + rotatedY);

		particle.setLastX(particle.getX());
		particle.setLastY(particle.getY());
	}

}
