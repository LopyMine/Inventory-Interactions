package net.lopymine.mossy.testrender.action;

import net.lopymine.mossy.testrender.argument.IRenderArgument;

public class ScaleRenderAction implements IRenderAction {

	@Override
	public String getId() {
		return "scale";
	}

	@Override
	public void render(RenderContext context, IRenderArgument... args) {
		context.stack().scale(Float.parseFloat(args[0].get()),Float.parseFloat(args[1].get()),Float.parseFloat(args[2].get()));
	}
}
