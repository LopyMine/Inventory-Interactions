package net.lopymine.ipi.config.base;

import lombok.*;
import net.lopymine.ipi.config.physics.ItemPhysicsConfig;
import net.lopymine.ipi.config.vec.Vec2i;
import net.minecraft.util.math.MathHelper;

@Getter
@Setter
@AllArgsConstructor
public class ItemBaseConfig {

	private Vec2i grabCenter;
	private Vec2i massCenter;
	private ItemPhysicsConfig physicsConfig;
	private double radius;

	public ItemBaseConfig(Vec2i grabCenter, Vec2i massCenter, ItemPhysicsConfig physicsConfig) {
		this.grabCenter = grabCenter;
		this.massCenter = massCenter;
		this.physicsConfig = physicsConfig;
		int x = MathHelper.abs(massCenter.offsetX() - grabCenter.offsetX());
		int y = MathHelper.abs(massCenter.offsetY() - grabCenter.offsetY());
		double h = MathHelper.hypot(x, y);
		if (h == 0) {
			h = 16.0F;
		}
		this.radius = h;
	}
}
