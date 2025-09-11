package net.lopymine.mossy.testrender.argument;

import net.minecraft.util.Util;

public class DynamicRotationRenderArgument implements IDynamicRenderArgument {

	@Override
	public String getId() {
		return "ROTATION";
	}

	@Override
	public String get() {
		long currentTime = Util.getMeasuringTimeMs();
		float rotationSpeed = 0.05f;
		float rotation = (currentTime * rotationSpeed) % 360;
		return String.valueOf(rotation);
	}

}
