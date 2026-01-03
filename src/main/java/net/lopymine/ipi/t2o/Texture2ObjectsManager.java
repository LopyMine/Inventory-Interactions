package net.lopymine.ipi.t2o;

import java.io.InputStream;
import java.util.*;
import net.lopymine.ip.client.InventoryParticlesClient;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.resource.Resource;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

public class Texture2ObjectsManager {

	public static <T> List<T> readFromTexture(Identifier id, Texture2Object<T> texture2Object) {
		try {
			Optional<Resource> optional = MinecraftClient.getInstance().getResourceManager().getResource(id);
			if (optional.isEmpty()) {
				InventoryParticlesClient.LOGGER.error("Failed to find texture from \"{}\" to create objects!", id);
				return List.of();
			}
			Resource resource = optional.get();
			InputStream inputStream = resource.getInputStream();
			NativeImage image = NativeImage.read(inputStream);

			List<T> list = new ArrayList<>();

			int width = image.getWidth();
			int height = image.getHeight();
			for (int x = 0; x < width; x++) {
				for (int y = 0; y < height; y++) {
					T object = texture2Object.accept(x, y, image./*? if <=1.21.1 {*/ /*getColor *//*?} else {*/ getColorArgb /*?}*/(x, y));
					if (object != null) {
						list.add(object);
					}
				}
			}

			return list;
		} catch (Exception e) {
			InventoryParticlesClient.LOGGER.error("Failed to load objects by texture from \"{}\"! Reason:", id, e);
		}
		return List.of();
	}

}
