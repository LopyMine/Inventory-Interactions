package net.lopymine.ipi.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import net.lopymine.ip.family.gui.ParticlesLinkingInfo;
import net.lopymine.ipi.family.gui.BaseConfigsLinkingInfo;
import net.minecraft.client.gui.GuiGraphics;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ParticlesLinkingInfo.class)
public class ParticlesLinkingInfoMixin {

	@Inject(at = @At("HEAD"), method = "render")
	private static void hookRender2(CallbackInfo ci, @Local(argsOnly = true) GuiGraphics context, @Local(argsOnly = true, ordinal = 0) int x, @Local(argsOnly = true, ordinal = 1) int y, @Local(argsOnly = true, ordinal = 2) int mouseX, @Local(argsOnly = true, ordinal = 3) int mouseY) {
		BaseConfigsLinkingInfo.render(context, x, y + 8 + 2, mouseX, mouseY);
	}

}
