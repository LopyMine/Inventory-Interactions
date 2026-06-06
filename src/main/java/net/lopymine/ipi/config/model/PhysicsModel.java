package net.lopymine.ipi.config.model;

import lombok.*;
import net.lopymine.ipi.config.physics.PhysicsConfig;
import net.lopymine.ipi.utils.DimensionOffset;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;

@Getter
@Setter
@AllArgsConstructor
public class PhysicsModel {

	private DimensionOffset massCenter;
	private DimensionOffset grabPos;
	private PhysicsConfig physicsConfig;
	private double radius;

	public PhysicsModel(DimensionOffset massCenter, DimensionOffset grabPos, PhysicsConfig physicsConfig) {
		this.massCenter = massCenter;
		this.grabPos = grabPos;
		this.physicsConfig = physicsConfig;

		float x = Mth.abs((float) massCenter.x() - (float) grabPos.x());
		float y = Mth.abs((float) massCenter.y() - (float) grabPos.y());
		double h = Mth.length(x, y);
		if (h == 0) {
			h = 16.0F;
		}
		this.radius = h;
	}
}
