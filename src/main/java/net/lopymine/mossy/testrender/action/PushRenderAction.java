package net.lopymine.mossy.testrender.action;

import net.lopymine.mossy.testrender.argument.IRenderArgument;

public class PushRenderAction implements IRenderAction {

	@Override
	public String getId() {
		return "push";
	}

	@Override
	public void render(RenderContext context, IRenderArgument... args) {
		context.stack().push();
	}
}
