package net.lopymine.ipi.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import net.lopymine.ip.family.gui.ParticlesLinkingInfo;
import net.lopymine.ip.resourcepack.manager.ParticlesConfigsManager;
import net.lopymine.ipi.family.gui.PhysicsConfigsLinkingInfo;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ParticlesLinkingInfo.class)
public class ParticlesLinkingInfoMixin {

	@Inject(at = @At("HEAD"), method = "render", remap = false)
	private static void hookRender(CallbackInfo ci, @Local(argsOnly = true) GuiGraphicsExtractor context, @Local(argsOnly = true, ordinal = 0) int x, @Local(argsOnly = true, ordinal = 1) int y, @Local(argsOnly = true, ordinal = 2) int mouseX, @Local(argsOnly = true, ordinal = 3) int mouseY) {
		ParticlesConfigsManager.ReloadInfo reloadInfo = ParticlesConfigsManager.RELOAD_INFO;
		int progress = reloadInfo.getProgress();
		int totalItems = reloadInfo.getTotalItems();
		boolean bl = progress != -1 && progress != totalItems;
		PhysicsConfigsLinkingInfo.render(context, x, y + (bl ? 8 + 2 : 0), mouseX, mouseY);
	}

}
