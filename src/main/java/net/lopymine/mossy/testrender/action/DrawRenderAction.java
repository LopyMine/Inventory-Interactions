package net.lopymine.mossy.testrender.action;

import net.lopymine.mossy.testrender.argument.IRenderArgument;

public class DrawRenderAction implements IRenderAction {

	@Override
	public String getId() {
		return "draw";
	}

	@Override
	public void render(RenderContext context, IRenderArgument... args) {
		context.immediate().draw();
	}
}
