package net.lopymine.ipi.mixin;

import net.lopymine.ip.element.InventoryCursor;
import net.lopymine.ipi.config.InventoryInteractionsConfig;
import net.lopymine.ipi.renderer.CursorItemRenderer;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InventoryCursor.class)
public abstract class InventoryCursorMixin {

	@Inject(at = @At("TAIL"), method = "setCurrentStack", remap = false)
	private void inject(ItemStack currentStack, CallbackInfo ci) {
		if (!InventoryInteractionsConfig.getInstance().isModEnabled()) {
			return;
		}
		CursorItemRenderer.getInstance().update(currentStack.getItem(), (InventoryCursor) (Object) (this));
	}

}
