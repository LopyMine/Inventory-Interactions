package net.lopymine.ipi.family.cache;

import com.mojang.blaze3d.platform.NativeImage;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.experimental.ExtensionMethod;
import net.lopymine.ip.extension.NativeImageExtension;
import net.lopymine.ipi.client.InventoryInteractionsClient;
import net.lopymine.ipi.resourcepack.manager.PhysicsModelsConfigsManager.BaseTexture;
import net.lopymine.ipi.utils.DimensionOffset;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Util;
import org.jetbrains.annotations.Nullable;

@ExtensionMethod(NativeImageExtension.class)
public class FamilyBaseTextureCacheManager {

	private static final int MAGIC = 0x49505058; // "IPPX"
	private static final int VERSION = 2;

	private static final Map<String, Map<String, BaseTexture>> NAMESPACE_BASE_TEXTURES = new ConcurrentHashMap<>();
	private static final Map<String, Object> NAMESPACE_WRITE_LOCKS = new ConcurrentHashMap<>();

	private static Object lock(String namespace) {
		return NAMESPACE_WRITE_LOCKS.computeIfAbsent(namespace, ignoredNamespace -> new Object());
	}

	public static void clear() {
		NAMESPACE_BASE_TEXTURES.clear();
	}

	@Nullable
	public static BaseTexture load(Identifier itemId) {
		Map<String, BaseTexture> map = getOrLoadNamespaceBaseTextures(itemId.getNamespace());
		if (map == null) {
			return null;
		}
		return map.get(itemId.getPath());
	}

	@Nullable
	private static Map<String, BaseTexture> getOrLoadNamespaceBaseTextures(String namespace) {
		Map<String, BaseTexture> pixels = NAMESPACE_BASE_TEXTURES.get(namespace);
		if (pixels == null) {
			synchronized (lock(namespace)) {
				pixels = NAMESPACE_BASE_TEXTURES.get(namespace);

				if (pixels == null) {
					Path file = FamilyInteractionsCacheManager.FOLDER.resolve(namespace).resolve("base_textures.cached");

					try {
						pixels = readNamespaceBaseTextures(file);
					} catch (IOException exception) {
						InventoryInteractionsClient.LOGGER.error(
								"Failed to load cached spawn areas for namespace {}, reason:",
								namespace,
								exception
						);

						return null;
					}

					NAMESPACE_BASE_TEXTURES.put(namespace, pixels);
				}
			}
		}
		return pixels;
	}

	private static Map<String, BaseTexture> readNamespaceBaseTextures(Path file) throws IOException {
		Map<String, BaseTexture> namespaceBaseTextures = new HashMap<>();

		if (!Files.isRegularFile(file)) {
			return namespaceBaseTextures;
		}

		try (DataInputStream inputStream = new DataInputStream(new BufferedInputStream(Files.newInputStream(file)))) {
			int magic = inputStream.readInt();
			if (magic != MAGIC) {
				return namespaceBaseTextures;
			}

			int version = inputStream.readInt();
			if (version != VERSION) {
				return namespaceBaseTextures;
			}

			int entriesCount = inputStream.readInt();
			if (entriesCount < 0) {
				return namespaceBaseTextures;
			}

			for (int entryIndex = 0; entryIndex < entriesCount; entryIndex++) {
				String itemPath = inputStream.readUTF();

				double width = inputStream.readDouble();
				double height = inputStream.readDouble();

				double firstX = inputStream.readDouble();
				double firstY = inputStream.readDouble();

				double secondX = inputStream.readDouble();
				double secondY = inputStream.readDouble();

				DimensionOffset massCenter = new DimensionOffset(firstX, firstY, width, height);
				DimensionOffset grabPos = new DimensionOffset(secondX, secondY, width, height);

				namespaceBaseTextures.put(itemPath, new BaseTexture(massCenter, grabPos));
			}
		}

		return namespaceBaseTextures;
	}

	public static void add(Identifier itemId, BaseTexture baseTexture) {
		String namespace = itemId.getNamespace();

		synchronized (lock(namespace)) {
			Map<String, BaseTexture> map = NAMESPACE_BASE_TEXTURES.computeIfAbsent(
					namespace,
					ignoredNamespace -> new HashMap<>()
			);

			map.put(itemId.getPath(), baseTexture);
		}
	}

	public static void save() {
		for (String namespace : NAMESPACE_BASE_TEXTURES.keySet()) {
			save(namespace);
		}
	}

	public static void save(String namespace) {
		Map<String, BaseTexture> map = NAMESPACE_BASE_TEXTURES.get(namespace);
		if (map == null || map.isEmpty()) {
			return;
		}

		Map<String, BaseTexture> snapshot;

		synchronized (lock(namespace)) {
			map = NAMESPACE_BASE_TEXTURES.get(namespace);
			if (map == null || map.isEmpty()) {
				return;
			}
			snapshot = new HashMap<>(map);
		}

		Path file = FamilyInteractionsCacheManager.FOLDER.resolve(namespace).resolve("base_textures.cached");
		Path temporaryFile = file.resolveSibling(file.getFileName() + ".tmp");

		Util.ioPool().execute(() -> {
			synchronized (lock(namespace)) {
				try {
					Files.createDirectories(file.getParent());

					try (DataOutputStream out = new DataOutputStream(new BufferedOutputStream(Files.newOutputStream(temporaryFile)))) {
						out.writeInt(MAGIC);
						out.writeInt(VERSION);
						out.writeInt(snapshot.size());

						for (Map.Entry<String, BaseTexture> entry : snapshot.entrySet()) {
							out.writeUTF(entry.getKey());
							writeBaseTexture(out, entry.getValue());
						}
					}

					Files.move(
							temporaryFile,
							file,
							StandardCopyOption.REPLACE_EXISTING,
							StandardCopyOption.ATOMIC_MOVE
					);
				} catch (IOException exception) {
					InventoryInteractionsClient.LOGGER.error(
							"Failed to cache spawn areas for namespace {}, reason:",
							namespace,
							exception
					);

					try {
						Files.deleteIfExists(temporaryFile);
					} catch (IOException ignoredException) {
					}
				}
			}
		});
	}

	private static final double EPS = 1.0E-7;

	private static void paintPixelPoint(NativeImage image, double x, double y, int color) {
		boolean onXLine = isInteger(x); // граница между пикселями по X
		boolean onYLine = isInteger(y); // граница между пикселями по Y

		if (onXLine && onYLine) {
			// точка на пересечении 4 пикселей
			int px = (int) Math.round(x);
			int py = (int) Math.round(y);

			setPixelSafe(image, px - 1, py - 1, color);
			setPixelSafe(image, px,     py - 1, color);
			setPixelSafe(image, px - 1, py,     color);
			setPixelSafe(image, px,     py,     color);

		} else if (onXLine) {
			// точка между 2 пикселями по X
			// пример: 1.0, 0.5 -> пиксели [0,0] и [1,0]
			int px = (int) Math.round(x);
			int py = (int) Math.floor(y);

			setPixelSafe(image, px - 1, py, color);
			setPixelSafe(image, px,     py, color);

		} else if (onYLine) {
			// точка между 2 пикселями по Y
			// пример: 0.5, 1.0 -> пиксели [0,0] и [0,1]
			int px = (int) Math.floor(x);
			int py = (int) Math.round(y);

			setPixelSafe(image, px, py - 1, color);
			setPixelSafe(image, px, py,     color);

		} else {
			// точка внутри одного пикселя
			// центр пикселя: 0.5, 0.5 -> пиксель [0,0]
			int px = (int) Math.floor(x);
			int py = (int) Math.floor(y);

			setPixelSafe(image, px, py, color);
		}
	}

	private static boolean isInteger(double value) {
		return Math.abs(value - Math.round(value)) < EPS;
	}

	private static void setPixelSafe(NativeImage image, int x, int y, int color) {
		if (x < 0 || y < 0 || x >= image.getWidth() || y >= image.getHeight()) {
			return;
		}

		image.setPixelArgb(x, y, color);
	}

	private static void writeBaseTexture(DataOutputStream out, BaseTexture baseTexture) throws IOException {
		DimensionOffset massCenter = baseTexture.massCenter();
		DimensionOffset grabPos = baseTexture.grabPos();

		out.writeDouble(massCenter.width());
		out.writeDouble(massCenter.height());

		out.writeDouble(massCenter.x());
		out.writeDouble(massCenter.y());

		out.writeDouble(grabPos.x());
		out.writeDouble(grabPos.y());
	}

}