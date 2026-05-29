package net.lopymine.ipi.mixin;

import net.lopymine.ip.resourcepack.manager.ParticlesConfigsManager;
import net.lopymine.ipi.resourcepack.base.BaseConfigsManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ParticlesConfigsManager.class)
public class ParticlesConfigsManagerMixin {

	@Inject(at = @At("TAIL"), method = "updateCombinedMap")
	private static void hookUpdate(CallbackInfo ci) {
		BaseConfigsManager.updateCombined();
	}

}
