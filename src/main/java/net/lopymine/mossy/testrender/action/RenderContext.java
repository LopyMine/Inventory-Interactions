package net.lopymine.mossy.testrender.action;

import net.minecraft.client.render.DiffuseLighting;
import net.minecraft.client.render.VertexConsumerProvider.Immediate;
import net.minecraft.client.util.math.MatrixStack;
import org.jetbrains.annotations.NotNull;

public record RenderContext(@NotNull MatrixStack stack, @NotNull  Immediate immediate, @NotNull DiffuseLighting lighting, @NotNull Runnable mainRender) {

}
