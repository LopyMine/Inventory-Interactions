package net.lopymine.ipi.config.base;

import lombok.*;
import net.lopymine.ipi.config.vec.Vec2i;
import net.minecraft.util.math.MathHelper;

@Getter
@Setter
@AllArgsConstructor
public class ItemBaseConfig {

	private Vec2i offset;
	private Vec2i center;
	private float radius;

	public ItemBaseConfig(Vec2i offset, Vec2i center) {
		this.offset = offset;
		this.center = center;
		int x = MathHelper.abs(center.offsetX() - offset.offsetX());
		int y = MathHelper.abs(center.offsetY() - offset.offsetY());
		float h = MathHelper.hypot(x, y);
		if (h == 0) {
			h = 16.0F;
		}
		this.radius = h;
	}
}
