package net.lopymine.ipi.config.base.model;

import lombok.*;
import net.lopymine.ipi.config.base.ItemOffset;
import net.lopymine.ipi.config.physics.ItemPhysicsConfig;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.Nullable;

@Getter
@Setter
@AllArgsConstructor
public class CursorItemModel {

	@Nullable
	private Identifier modelTexture;
	private ItemOffset partConnectionCenter;
	@Nullable
	private ItemOffset nextPartConnectionCenter;
	private ItemOffset massCenter;
	private ItemPhysicsConfig physicsConfig;
	@Nullable
	private CursorItemModel partModel;
	private double radius;

	public CursorItemModel(ItemOffset partConnectionCenter, ItemOffset massCenter, ItemPhysicsConfig itemPhysicsConfig) {
		this(null, partConnectionCenter, null, massCenter, itemPhysicsConfig, null);
	}

	public CursorItemModel(@Nullable Identifier modelTexture, ItemOffset partConnectionCenter, @Nullable ItemOffset nextPartConnectionCenter, ItemOffset massCenter, ItemPhysicsConfig physicsConfig, @Nullable CursorItemModel partModel) {
		this.modelTexture             = modelTexture;
		this.partConnectionCenter     = partConnectionCenter;
		this.nextPartConnectionCenter = nextPartConnectionCenter;
		this.massCenter               = massCenter;
		this.physicsConfig            = physicsConfig;
		this.partModel                = partModel;
		int x = MathHelper.abs(massCenter.x() - partConnectionCenter.x());
		int y = MathHelper.abs(massCenter.y() - partConnectionCenter.y());
		double h = MathHelper.hypot(x, y);
		if (h == 0) {
			h = 16.0F;
		}
		this.radius = h;
	}
}
