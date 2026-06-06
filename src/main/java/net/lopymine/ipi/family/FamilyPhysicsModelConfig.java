package net.lopymine.ipi.family;

import com.mojang.serialization.Codec;
import java.util.*;
import lombok.*;
import net.lopymine.ip.family.FamilyParticleConfig.WhitelistAndBlacklist;
import net.lopymine.ipi.config.physics.PhysicsConfig;
import net.minecraft.resources.Identifier;
import net.minecraft.util.StringRepresentable;
import static com.mojang.serialization.codecs.RecordCodecBuilder.create;
import static net.lopymine.ip.family.FamilyParticleConfig.COMPATIBLE_CODEC;
import static net.lopymine.ip.family.FamilyParticleConfig.LIST_CODEC;
import static net.lopymine.mossylib.utils.CodecUtils.option;

@Getter
@Setter
@AllArgsConstructor
public class FamilyPhysicsModelConfig {

	public static final Codec<FamilyPhysicsModelConfig> CODEC = create((instance) -> instance.group(
			option("special_markers", new WhitelistAndBlacklist(), LIST_CODEC, FamilyPhysicsModelConfig::getSpecialMarkers),
			option("keywords", new WhitelistAndBlacklist(), LIST_CODEC, FamilyPhysicsModelConfig::getKeywords),
			option("tags", new WhitelistAndBlacklist(), LIST_CODEC, FamilyPhysicsModelConfig::getTags),
			option("namespaces", new WhitelistAndBlacklist(), LIST_CODEC, FamilyPhysicsModelConfig::getNamespaces),
			option("grab_corner", GrabCorner.ANY, GrabCorner.CODEC, FamilyPhysicsModelConfig::getGrabCorner),
			option("physics", PhysicsConfig.getNewInstance(), PhysicsConfig.CODEC, FamilyPhysicsModelConfig::getPhysics),
			option("family_groups", new ArrayList<>(), Codec.STRING, FamilyPhysicsModelConfig::getFamilyGroups),
			option("compatible", new ArrayList<>(), COMPATIBLE_CODEC, FamilyPhysicsModelConfig::getCompatibleGroups),
			option("priority", 1000, Codec.INT, FamilyPhysicsModelConfig::getPriority)
	).apply(instance, FamilyPhysicsModelConfig::new));

	private Identifier location;

	private WhitelistAndBlacklist specialMarkers;
	private WhitelistAndBlacklist keywords;
	private WhitelistAndBlacklist tags;
	private WhitelistAndBlacklist namespaces;
	private GrabCorner grabCorner;
	private PhysicsConfig physics;
	private ArrayList<String> familyGroups;
	private ArrayList<String> compatibleGroups;
	private int priority;

	public FamilyPhysicsModelConfig(
			WhitelistAndBlacklist specialMarkers,
			WhitelistAndBlacklist keywords,
			WhitelistAndBlacklist tags,
			WhitelistAndBlacklist namespaces,
			GrabCorner grabCorner,
			PhysicsConfig physicsConfig,
			ArrayList<String> familyGroups,
			ArrayList<String> compatibleGroups,
			int priority
	) {
		this.specialMarkers   = specialMarkers;
		this.keywords         = keywords;
		this.tags             = tags;
		this.namespaces       = namespaces;
		this.grabCorner       = grabCorner;
		this.physics          = physicsConfig;
		this.familyGroups     = familyGroups;
		this.compatibleGroups = compatibleGroups;
		this.priority         = priority;
	}

	public enum GrabCorner implements StringRepresentable {

		ANY,
		BOTTOM,
		BOTTOM_LEFT,
		BOTTOM_RIGHT,
		TOP,
		TOP_LEFT,
		TOP_RIGHT,
		LEFT,
		RIGHT;

		public static final Codec<GrabCorner> CODEC = StringRepresentable.fromEnum(GrabCorner::values);


		@Override
		public String getSerializedName() {
			return this.name().toLowerCase(Locale.ROOT);
		}
	}
}
