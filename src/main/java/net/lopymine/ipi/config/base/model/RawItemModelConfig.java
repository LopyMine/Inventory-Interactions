package net.lopymine.ipi.config.base.model;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.function.Supplier;
import lombok.*;
import net.lopymine.ipi.InventoryInteractions;
import net.lopymine.ipi.config.physics.ItemPhysicsConfig;
import net.lopymine.mossylib.utils.CodecUtils;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;
import static net.lopymine.ipi.config.base.RawItemBaseConfig.DEFAULT_BASE_TEXTURE;
import static net.lopymine.mossylib.utils.CodecUtils.option;

@Getter
@Setter
@AllArgsConstructor
public class RawItemModelConfig {

	public static final Identifier NO_MODEL_TEXTURE = Identifier.of("");

	public static final Codec<RawItemModelConfig> CODEC = CodecUtils.recursive(
			"custom model config codec",
			(codec) ->
					RecordCodecBuilder.create((instance) -> instance.group(
							option("model_texture", NO_MODEL_TEXTURE, Identifier.CODEC, RawItemModelConfig::getModelTexture),
							option("base_texture", DEFAULT_BASE_TEXTURE, Identifier.CODEC, RawItemModelConfig::getBaseTexture),
							option("physics", ItemPhysicsConfig.getNewInstance(), ItemPhysicsConfig.CODEC, RawItemModelConfig::getPhysicsConfig),
							option("model_part", new RawItemModelConfig(), codec, RawItemModelConfig::getModelPartConfig)
					).apply(instance, RawItemModelConfig::new))
	);

	public static RawItemModelConfig DUMMY_MODEL = getNewInstance().get();

	private Identifier modelTexture;
	private Identifier baseTexture;
	private ItemPhysicsConfig physicsConfig;
	@Nullable
	private RawItemModelConfig modelPartConfig;

	public RawItemModelConfig() {
		this.modelTexture    = NO_MODEL_TEXTURE;
		this.baseTexture     = DEFAULT_BASE_TEXTURE;
		this.physicsConfig   = ItemPhysicsConfig.getNewInstance().get();
		this.modelPartConfig = null;
	}

	public static Supplier<RawItemModelConfig> getNewInstance() {
		return () -> CodecUtils.parseNewInstanceHacky(CODEC);
	}

	public Identifier getBaseTextureInFolder() {
		return Identifier.of(InventoryInteractions.MOD_ID, "textures/i-interactions/%s".formatted(this.baseTexture.getPath()));
	}


}
