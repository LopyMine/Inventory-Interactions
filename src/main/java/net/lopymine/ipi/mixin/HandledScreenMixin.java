package net.lopymine.ipi.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.*;
import com.llamalad7.mixinextras.sugar.Local;
import lombok.experimental.ExtensionMethod;
import net.lopymine.ip.element.InventoryCursor;
import net.lopymine.ip.renderer.InventoryParticlesRenderer;
import net.lopymine.ipi.config.InventoryInteractionsConfig;
import net.lopymine.ipi.renderer.CursorItemRenderer;
import net.lopymine.mossylib.extension.DrawContextExtension;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@ExtensionMethod(DrawContextExtension.class)
@Debug(export = true)
@Mixin(value = AbstractContainerScreen.class, priority = 950)
public class HandledScreenMixin {

	//? if >=1.21.6 {
	@WrapOperation(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/inventory/AbstractContainerScreen;renderFloatingItem(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/world/item/ItemStack;IILjava/lang/String;)V"), method = "renderCarriedItem")
	private void offsetItem(AbstractContainerScreen<?> instance, GuiGraphics context, ItemStack stack, int x, int y, String amountText, Operation<Void> original, @Local(argsOnly = true, ordinal = 0) int mouseX, @Local(argsOnly = true, ordinal = 1) int mouseY) {
		if (!InventoryInteractionsConfig.getInstance().isModEnabled()) {
			return;
		}
		CursorItemRenderer.getInstance().render(context, stack, mouseX, mouseY, x, y, (mx, my) -> original.call(instance, context, stack, mx, my, amountText));
	}
	//?} else {

	/*@Shadow protected int leftPos;

	@Shadow protected int topPos;

	@WrapOperation(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/inventory/AbstractContainerScreen;renderFloatingItem(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/world/item/ItemStack;IILjava/lang/String;)V", ordinal = 0), method = "render")
	private void offsetItem(AbstractContainerScreen<?> instance, GuiGraphics context, ItemStack stack, int x, int y, String amountText, Operation<Void> original, @Local(argsOnly = true, ordinal = 0) int mouseX, @Local(argsOnly = true, ordinal = 1) int mouseY) {
		if (!InventoryInteractionsConfig.getInstance().isModEnabled()) {
			return;
		}
		context.push();
		context.translate(-this.leftPos, -this.topPos, 0);
		CursorItemRenderer.getInstance().render(context, stack, mouseX, mouseY, x, y, (mx, my) -> original.call(instance, context, stack, mx, my, amountText));
		context.pop();
	}
	*///?}

	@Inject(at = @At("HEAD"), method = "tick")
	private void tickCursorItem(CallbackInfo ci) {
		if (!InventoryInteractionsConfig.getInstance().isModEnabled()) {
			return;
		}
		InventoryCursor cursor = InventoryParticlesRenderer.getInstance().getCursor();
		CursorItemRenderer.getInstance().tick(cursor.getCurrentStack(), cursor);
	}

}