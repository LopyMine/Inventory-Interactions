package net.lopymine.ipi.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.*;
import com.llamalad7.mixinextras.sugar.Local;
import net.lopymine.ip.config.InventoryParticlesConfig;
import net.lopymine.ip.config.sub.InventoryParticlesMainConfig;
import net.lopymine.ip.element.InventoryCursor;
import net.lopymine.ip.renderer.InventoryParticlesRenderer;
import net.lopymine.ipi.renderer.CursorItemRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = HandledScreen.class, priority = 950)
public class HandledScreenMixin {

	@WrapOperation(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/ingame/HandledScreen;drawItem(Lnet/minecraft/client/gui/DrawContext;Lnet/minecraft/item/ItemStack;IILjava/lang/String;)V"), method = "renderCursorStack")
	private void offsetItem(HandledScreen<?> instance, DrawContext context, ItemStack stack, int x, int y, String amountText, Operation<Void> original, @Local(argsOnly = true, ordinal = 0) int mouseX, @Local(argsOnly = true, ordinal = 1) int mouseY) {
		CursorItemRenderer.getInstance().render(context, stack, mouseX, mouseY, x, y, (mx, my) -> original.call(instance, context, stack, mx, my, amountText));
	}

	@Inject(at = @At("HEAD"), method = "tick")
	private void tickInventoryParticles(CallbackInfo ci) {
		CursorItemRenderer.getInstance().tick(InventoryParticlesRenderer.getInstance().getCursor());
	}

}