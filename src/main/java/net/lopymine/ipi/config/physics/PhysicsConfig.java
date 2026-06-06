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
public class PhysicsConfig {

	public static final Codec<PhysicsConfig> CODEC = create((instance) -> instance.group(
			option("gravity", 1.0D, Codec.DOUBLE, PhysicsConfig::getGravity),
			option("braking", 1.0D, Codec.DOUBLE, PhysicsConfig::getBraking),
			option("cursor_impulse_inherit_coefficient", 1.0D, Codec.DOUBLE, PhysicsConfig::getCursorImpulseInheritCoefficient)
	).apply(instance, PhysicsConfig::new));

	private double gravity;
	private double braking;
	private double cursorImpulseInheritCoefficient;

	public static Supplier<PhysicsConfig> getNewInstance() {
		return () -> CodecUtils.parseNewInstanceHacky(CODEC);
	}

}
