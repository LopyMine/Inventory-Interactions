package net.lopymine.ipi.mixin;

import net.lopymine.ip.element.InventoryCursor;
import net.lopymine.ipi.renderer.CursorItemRenderer;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InventoryCursor.class)
public abstract class InventoryCursorMixin {

	@Shadow public abstract int getMouseX();

	@Shadow public abstract int getMouseY();

	@Inject(at = @At("TAIL"), method = "setCurrentStack")
	private void inject(ItemStack currentStack, CallbackInfo ci) {
		CursorItemRenderer.getInstance().update(currentStack.getItem(), this.getMouseX(), this.getMouseY());
	}

}
