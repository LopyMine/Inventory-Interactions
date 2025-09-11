package net.lopymine.mossy.testrender.action;

import net.lopymine.mossy.testrender.argument.IRenderArgument;

public class SetupLightRenderAction implements IRenderAction {

	@Override
	public String getId() {
		return "light";
	}

	@Override
	public void render(RenderContext context, IRenderArgument... args) {
		//? if >=1.21.6 {
		/*net.minecraft.client.render.DiffuseLighting.Type type = net.minecraft.client.render.DiffuseLighting.Type.valueOf(args[0].get().toUpperCase());
		context.lighting().setShaderLights(type);
		*///?}
	}
}
