package net.lopymine.mossy.testrender.action;

import net.lopymine.mossy.testrender.argument.IRenderArgument;

public interface IRenderAction {

	String getId();

	void render(RenderContext context, IRenderArgument... args);

}
