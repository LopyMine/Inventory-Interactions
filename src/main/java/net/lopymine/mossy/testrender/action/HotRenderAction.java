package net.lopymine.mossy.testrender.action;

import net.lopymine.mossy.testrender.argument.IRenderArgument;

public class HotRenderAction implements IRenderAction {

	@Override
	public String getId() {
		return "RENDER";
	}

	@Override
	public void render(RenderContext context, IRenderArgument... args) {
		context.mainRender().run();
	}
}
