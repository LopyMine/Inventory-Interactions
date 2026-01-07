package net.lopymine.ipi.config.physics;

import com.mojang.serialization.Codec;
import java.util.function.Supplier;
import lombok.*;
import net.lopymine.mossylib.utils.CodecUtils;
import static com.mojang.serialization.codecs.RecordCodecBuilder.create;
import static net.lopymine.mossylib.utils.CodecUtils.option;

@Getter
@Setter
@AllArgsConstructor
public class ItemPhysicsConfig {

	public static final Codec<ItemPhysicsConfig> CODEC = create((instance) -> instance.group(
			option("gravity", 1.0D, Codec.DOUBLE, ItemPhysicsConfig::getGravity),
			option("braking", 1.0D, Codec.DOUBLE, ItemPhysicsConfig::getBraking),
			option("cursor_impulse_inherit_coefficient", 1.0D, Codec.DOUBLE, ItemPhysicsConfig::getCursorImpulseInheritCoefficient)
	).apply(instance, ItemPhysicsConfig::new));

	private double gravity;
	private double braking;
	private double cursorImpulseInheritCoefficient;

	public static Supplier<ItemPhysicsConfig> getNewInstance() {
		return () -> CodecUtils.parseNewInstanceHacky(CODEC);
	}

}
