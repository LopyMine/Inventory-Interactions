package net.lopymine.mossy.testrender.action;

import net.lopymine.mossy.testrender.argument.IRenderArgument;

public class PopRenderAction implements IRenderAction {

	@Override
	public String getId() {
		return "pop";
	}

	@Override
	public void render(RenderContext context, IRenderArgument... args) {
		context.stack().pop();
	}
}
