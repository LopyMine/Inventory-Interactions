package net.lopymine.ipi.config.base.model;

import lombok.*;
import net.lopymine.ipi.config.base.ItemOffset;
import net.lopymine.ipi.config.physics.ItemPhysicsConfig;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
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
		// todo remove me later after updating Inventory Particles to 1.3.3!!!
		//? if >=1.21.4 {
		this.partConnectionCenter = partConnectionCenter;
		this.massCenter           = massCenter;
		//?} else {
		/*this.partConnectionCenter     = massCenter;
		this.massCenter               = partConnectionCenter;
		*///?}
		this.nextPartConnectionCenter = nextPartConnectionCenter;
		this.physicsConfig            = physicsConfig;
		this.partModel                = partModel;
		int x = Mth.abs(massCenter.x() - partConnectionCenter.x());
		int y = Mth.abs(massCenter.y() - partConnectionCenter.y());
		double h = Mth.length(x, y);
		if (h == 0) {
			h = 16.0F;
		}
		this.radius = h;
	}
}
