package net.lopymine.mossy.testrender.action;

import net.lopymine.mossy.testrender.argument.IRenderArgument;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.RotationAxis;

public class RotateRenderAction implements IRenderAction {

	@Override
	public String getId() {
		return "rotate";
	}

	@Override
	public void render(RenderContext context, IRenderArgument... args) {
		MatrixStack stack = context.stack();
		String axis = args[0].get();
		float degrees = Float.parseFloat(args[1].get());

		switch (axis) {
			case "x" -> stack.multiply(RotationAxis.POSITIVE_X.rotationDegrees(degrees));
			case "y" -> stack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(degrees));
			case "z" -> stack.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(degrees));
		}
	}

}
