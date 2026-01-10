package net.lopymine.ipi.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.*;
import com.llamalad7.mixinextras.sugar.Local;
import net.lopymine.ip.element.InventoryCursor;
import net.lopymine.ip.renderer.InventoryParticlesRenderer;
import net.lopymine.ipi.config.InventoryInteractionsConfig;
import net.lopymine.ipi.renderer.CursorItemRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Debug(export = true)
@Mixin(value = HandledScreen.class, priority = 950)
public class HandledScreenMixin {

	@WrapOperation(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/ingame/HandledScreen;drawItem(Lnet/minecraft/client/gui/DrawContext;Lnet/minecraft/item/ItemStack;IILjava/lang/String;)V"), method = "renderCursorStack")
	private void offsetItem(HandledScreen<?> instance, DrawContext context, ItemStack stack, int x, int y, String amountText, Operation<Void> original, @Local(argsOnly = true, ordinal = 0) int mouseX, @Local(argsOnly = true, ordinal = 1) int mouseY) {
		if (!InventoryInteractionsConfig.getInstance().isModEnabled()) {
			return;
		}
		CursorItemRenderer.getInstance().render(context, stack, mouseX, mouseY, x, y, (mx, my) -> original.call(instance, context, stack, mx, my, amountText));
	}

	@Inject(at = @At("HEAD"), method = "tick")
	private void tickCursorItem(CallbackInfo ci) {
		if (!InventoryInteractionsConfig.getInstance().isModEnabled()) {
			return;
		}
		InventoryCursor cursor = InventoryParticlesRenderer.getInstance().getCursor();
		CursorItemRenderer.getInstance().tick(cursor.getCurrentStack(), cursor);
	}

}