package net.lopymine.ipi.family.generation;

import com.mojang.blaze3d.platform.NativeImage;
import java.util.*;
import java.util.stream.Collectors;
import lombok.*;
import lombok.experimental.ExtensionMethod;
import net.lopymine.ip.extension.NativeImageExtension;
import net.lopymine.ip.family.FamilyParticleData.TextureExtractMode;
import net.lopymine.ip.family.generation.*;
import net.lopymine.ip.t2o.*;
import net.lopymine.ip.utils.iac.RenderedItemImage;
import net.lopymine.ipi.client.InventoryInteractionsClient;
import net.lopymine.ipi.config.InventoryInteractionsConfig;
import net.lopymine.ipi.family.FamilyPhysicsModelConfig.GrabCorner;
import net.lopymine.ipi.family.cache.FamilyBaseTextureCacheManager;
import net.lopymine.ipi.resourcepack.manager.PhysicsModelsConfigsManager;
import net.lopymine.ipi.utils.DimensionOffset;
import net.lopymine.ipi.resourcepack.manager.PhysicsModelsConfigsManager.BaseTexture;
import net.lopymine.mossylib.utils.ArgbUtils;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import org.jetbrains.annotations.Nullable;
import static net.lopymine.ipi.resourcepack.manager.PhysicsModelsConfigsManager.*;

@ExtensionMethod(NativeImageExtension.class)
public class BaseTextureGenerationManager {

	public static final Map<Item, List<DimensionOffset>> ITEM_SEPARATORS = new HashMap<>();

	@Nullable
	public static BaseTexture generateBaseTexture(Identifier itemId, Item item, GrabCorner grabCorner) {
		BaseTexture load = FamilyBaseTextureCacheManager.load(itemId);
		if (load != null) {
			return load;
		}

		RenderedItemImage renderedItemImage = ItemRenderingManager.getRenderedItemImage(item, itemId, TextureExtractMode.ITEM);
		if (renderedItemImage == null) {
			return null;
		}

		NativeImage image = renderedItemImage.getImage();
		DimensionOffset massCenter = Optional.of(Texture2ObjectsManager.readFromTexture(image, itemId,
				"mass center",
				Texture2ObjectPixelFilter.NOT_TRANSPARENT,
				DIMENSION_OFFSET_GENERATOR
		)).filter((list) -> !list.isEmpty()).map((list) -> findCenter(list, MIDDLE_BOTTOM_POS)).orElse(MIDDLE_BOTTOM_POS);
		DimensionOffset grabPos = getGrabPos(itemId, item, image, massCenter, grabCorner);

		BaseTexture baseTexture = new BaseTexture(PhysicsModelsConfigsManager.checkAndOffsetMassCenterDistance(massCenter, grabPos), grabPos, image);
		FamilyBaseTextureCacheManager.add(itemId, baseTexture);

		return baseTexture;
	}

	public static @NonNull DimensionOffset getGrabPos(Identifier itemId, Item item, NativeImage image, DimensionOffset massCenter, GrabCorner grabCorner) {
		List<SplitResult> list = new ArrayList<>();

		for (ImageSplitter value : ImageSplitter.values()) {
			list.add(new SplitResult(getSymmetry(value.splitImage(image, massCenter), value, massCenter), value));
		}

		list.sort((first, second) -> {
			int compare = Float.compare(second.symmetry, first.symmetry);
			if (compare == 0) {
				return Integer.compare(first.splitter.ordinal(), second.splitter.ordinal());
			}
			return compare;
		});
		SplitResult result = list.get(0);

		String allResults = list.stream()
				.map(splitResult -> String.format(
						Locale.US,
						"%s[%.2f]",
						splitResult.splitter.name(),
						splitResult.symmetry
				))
				.collect(Collectors.joining(", "));

		if (InventoryInteractionsConfig.getInstance().getMainConfig().isDebugModeEnabled()) {
			for (ImageSplitter value : ImageSplitter.values()) {
				if (value == result.splitter) {
					continue;
				}
				getFarthestGrabPosOnSplitDiagonal(image, item, massCenter, value, grabCorner);
			}
		}

		DimensionOffset farthestGrabPosOnSplitDiagonal = getFarthestGrabPosOnSplitDiagonal(image, item, massCenter, result.splitter, grabCorner);

		if (result.symmetry < 0.5) {
			InventoryInteractionsClient.LOGGER.debug(
					"[1] {}: symmetry {}, splitter {}, other {}",
					itemId.toString(),
					String.format(Locale.US, "%.2f", result.symmetry),
					result.splitter.name(),
					allResults
			);
			return getFarthestGrabPos(image, massCenter, grabCorner);
		}

		double distance = Math.hypot(
				farthestGrabPosOnSplitDiagonal.getOffsetX() - massCenter.getOffsetX(),
				farthestGrabPosOnSplitDiagonal.getOffsetY() - massCenter.getOffsetY()
		);

		if (distance <= 2.5D) {
			InventoryInteractionsClient.LOGGER.debug(
					"[22] {}: symmetry {}, splitter {}, distance {}, other {}",
					itemId.toString(),
					String.format(Locale.US, "%.2f", result.symmetry),
					result.splitter.name(),
					String.format(Locale.US, "%.2f", distance),
					allResults
			);
			return getFarthestGrabPos(image, massCenter, grabCorner);
		}

		InventoryInteractionsClient.LOGGER.debug(
				"[333] {}: symmetry {}, splitter {}, distance {},, other {}",
				itemId.toString(),
				String.format(Locale.US, "%.2f", result.symmetry),
				result.splitter.name(),
				String.format(Locale.US, "%.2f", distance),
				allResults
		);

		return farthestGrabPosOnSplitDiagonal;
	}

	public static DimensionOffset getFarthestGrabPosOnSplitDiagonal(
			NativeImage image,
			Item item,
			DimensionOffset massCenter,
			ImageSplitter splitter,
			GrabCorner grabCorner
	) {
		List<DimensionOffset> fallbackOffsets = new ArrayList<>();
		List<DimensionOffset> offsets = new ArrayList<>();

		switch (splitter) {
			case HORIZONTAL -> {
				for (int y : getAxisLineIndexes(massCenter.getOffsetY(), image.getHeight())) {
					for (int x = 0; x < image.getWidth(); x++) {
						addLineOffset(image, x, y, offsets, fallbackOffsets);
					}
				}
			}
			case VERTICAL -> {
				for (int x : getAxisLineIndexes(massCenter.getOffsetX(), image.getWidth())) {
					for (int y = 0; y < image.getHeight(); y++) {
						addLineOffset(image, x, y, offsets, fallbackOffsets);
					}
				}
			}
			case DIAGONAL_45, DIAGONAL_135 -> {
				collectDiagonalOffsets(image, massCenter, splitter, offsets, fallbackOffsets);
			}
		}

		List<DimensionOffset> resultOffsets = offsets.isEmpty() ? fallbackOffsets : offsets;

		ITEM_SEPARATORS.computeIfAbsent(item, (k) -> new ArrayList<>()).addAll(resultOffsets);

		return getFarthestItemOffset(massCenter, resultOffsets, grabCorner);
	}

	public static DimensionOffset getFarthestGrabPos(NativeImage image, DimensionOffset massCenter, GrabCorner grabCorner) {
		List<DimensionOffset> offsets = new ArrayList<>();

		for (int x = 0; x < image.getWidth(); x++) {
			for (int y = 0; y < image.getHeight(); y++) {
				if (withoutPixelsAround(image, x, y) >= 1) {
					continue;
				}
				offsets.add(new DimensionOffset(x, y, image.getWidth(), image.getHeight()));
			}
		}

		if (offsets.isEmpty()) {
			for (int x = 0; x < image.getWidth(); x++) {
				for (int y = 0; y < image.getHeight(); y++) {
					if (transparentPixel(image, x, y)) {
						continue;
					}
					offsets.add(new DimensionOffset(x, y, image.getWidth(), image.getHeight()));
				}
			}
		}

		return getFarthestItemOffset(massCenter, offsets, grabCorner);
	}

	public static DimensionOffset getFarthestItemOffset(
			DimensionOffset massCenter,
			List<DimensionOffset> offsets,
			GrabCorner grabCorner
	) {
		if (offsets.isEmpty()) {
			return MIDDLE_POS;
		}

		List<DimensionOffset> filteredOffsets = filterOffsetsByGrabCorner(offsets, massCenter, grabCorner);

		List<DimensionOffset> resultOffsets = filteredOffsets.isEmpty() ? offsets : filteredOffsets;

		resultOffsets.sort((one, two) -> {
			double distance = Math.hypot(
					one.getOffsetX() - massCenter.getOffsetX(),
					one.getOffsetY() - massCenter.getOffsetY()
			);
			double distance2 = Math.hypot(
					two.getOffsetX() - massCenter.getOffsetX(),
					two.getOffsetY() - massCenter.getOffsetY()
			);
			return Double.compare(distance2, distance);
		});

		return resultOffsets.get(0);
	}

	public static List<DimensionOffset> filterOffsetsByGrabCorner(
			List<DimensionOffset> offsets,
			DimensionOffset massCenter,
			GrabCorner grabCorner
	) {
		if (grabCorner == GrabCorner.ANY) {
			return offsets;
		}

		return offsets.stream()
				.filter((offset) -> isInGrabCorner(offset, massCenter, grabCorner))
				.collect(Collectors.toList());
	}

	public static boolean isInGrabCorner(DimensionOffset offset, DimensionOffset massCenter, GrabCorner grabCorner) {
		double x = offset.getOffsetX();
		double y = offset.getOffsetY();

		double centerX = massCenter.getOffsetX();
		double centerY = massCenter.getOffsetY();

		return switch (grabCorner) {
			case ANY -> true;
			case BOTTOM -> y > centerY;
			case TOP -> y < centerY;
			case LEFT -> x < centerX;
			case RIGHT -> x > centerX;

			case BOTTOM_LEFT -> x < centerX && y > centerY;
			case BOTTOM_RIGHT -> x > centerX && y > centerY;
			case TOP_LEFT -> x < centerX && y < centerY;
			case TOP_RIGHT -> x > centerX && y < centerY;
		};
	}

	// 2 = skip, 0 = false, 1 = true
	public static int withoutPixelsAround(NativeImage image, int x, int y) {
		if (x == 1 || x == image.getWidth() - 1 || y == 1 || y == image.getHeight() - 1) {
			return 2;
		}
		if (x - 1 < 0 || x + 1 >= image.getWidth() || y - 1 < 0 || y + 1 >= image.getHeight()) {
			return 2;
		}
		if (transparentPixel(image, x, y)) {
			return 2;
		}
		if (transparentPixel(image,x - 1, y)) {
			return 1;
		}
		if (transparentPixel(image,x + 1, y)) {
			return 1;
		}
		if (transparentPixel(image,x, y - 1)) {
			return 1;
		}
		if (transparentPixel(image,x, y + 1)) {
			return 1;
		}
		return 0;
	}

	public static boolean transparentPixel(NativeImage image, int x, int y) {
		return ArgbUtils.getAlpha(image.getPixelArgb(x, y)) < 10;
	}

	public static float getSymmetry(DividedImage image, ImageSplitter splitter, DimensionOffset massCenter) {
		int[][] first = image.first();
		int[][] second = image.second();

		int width = first.length;
		int height = first[0].length;

		int matched = 0;
		int total = 0;

		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				int a = first[x][y];

				int reflectedX = x;
				int reflectedY = y;

				switch (splitter) {
					case HORIZONTAL -> {
						reflectedY = (int) Math.round(2D * massCenter.getOffsetY() - y);
					}
					case VERTICAL -> {
						reflectedX = (int) Math.round(2D * massCenter.getOffsetX() - x);
					}
					case DIAGONAL_45 -> {
						int[] reflected = reflectAcrossLine(
								x,
								y,
								height - 1D,
								width - 1D,
								getDiagonal45DividerDouble(width, height, massCenter)
						);

						reflectedX = reflected[0];
						reflectedY = reflected[1];
					}
					case DIAGONAL_135 -> {
						int[] reflected = reflectAcrossLine(
								x,
								y,
								height - 1D,
								-(width - 1D),
								getDiagonal135DividerDouble(width, height, massCenter)
						);

						reflectedX = reflected[0];
						reflectedY = reflected[1];
					}
				}

				if (reflectedX < 0 || reflectedX >= width || reflectedY < 0 || reflectedY >= height) {
					if (a == 1) {
						total++;
					}
					continue;
				}

				int b = second[reflectedX][reflectedY];

				if (a == 1 || b == 1) {
					total++;

					if (a == 1 && b == 1) {
						matched++;
					}
				}
			}
		}

		return total == 0 ? 0.0F : (float) matched / total;
	}

	public static final double PIXEL_CENTER_EPSILON = 1.0E-7D;

	public static boolean isPixelCenterLine(double value) {
		return Math.abs(value - Math.rint(value)) < PIXEL_CENTER_EPSILON;
	}

	public static int[] getAxisLineIndexes(double axis, int size) {
		if (size <= 0) {
			return new int[0];
		}

		if (isPixelCenterLine(axis)) {
			int index = (int) Math.round(axis);
			index = Math.max(0, Math.min(size - 1, index));
			return new int[] { index };
		}

		int first = (int) Math.floor(axis);
		int second = (int) Math.ceil(axis);

		first = Math.max(0, Math.min(size - 1, first));
		second = Math.max(0, Math.min(size - 1, second));

		if (first == second) {
			return new int[] { first };
		}

		return new int[] { first, second };
	}

	public static void addLineOffset(NativeImage image, int x, int y, List<DimensionOffset> offsets, List<DimensionOffset> fallbackOffsets) {
		DimensionOffset offset = new DimensionOffset(x, y, image.getWidth(), image.getHeight());

		int i = withoutPixelsAround(image, x, y);
		if (i >= 1) {
			if (i == 1) {
				fallbackOffsets.add(offset);
			}
			return;
		}

		offsets.add(offset);
	}

	public static long getDiagonal45Value(int x, int y, int width, int height) {
		return (long) x * (height - 1L) + (long) y * (width - 1L);
	}

	public static double getDiagonal45DividerDouble(int width, int height, DimensionOffset massCenter) {
		return massCenter.getOffsetX() * (height - 1D) +
				massCenter.getOffsetY() * (width - 1D);
	}

	public static long getDiagonal135Value(int x, int y, int width, int height) {
		return (long) x * (height - 1L) - (long) y * (width - 1L);
	}

	public static double getDiagonal135DividerDouble(int width, int height, DimensionOffset massCenter) {
		return massCenter.getOffsetX() * (height - 1D) -
				massCenter.getOffsetY() * (width - 1D);
	}

	public static void collectDiagonalOffsets(
			NativeImage image,
			DimensionOffset massCenter,
			ImageSplitter splitter,
			List<DimensionOffset> offsets,
			List<DimensionOffset> fallbackOffsets
	) {
		int width = image.getWidth();
		int height = image.getHeight();
		long[] lineValues = getDiagonalLineValues(width, height, massCenter, splitter);

		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				long value = splitter == ImageSplitter.DIAGONAL_45
						? getDiagonal45Value(x, y, width, height)
						: getDiagonal135Value(x, y, width, height);

				for (long lineValue : lineValues) {
					if (value == lineValue) {
						addLineOffset(image, x, y, offsets, fallbackOffsets);
						break;
					}
				}
			}
		}
	}

	public static long[] getDiagonalLineValues(int width, int height, DimensionOffset massCenter, ImageSplitter splitter) {
		double divider = splitter == ImageSplitter.DIAGONAL_45
				? getDiagonal45DividerDouble(width, height, massCenter)
				: getDiagonal135DividerDouble(width, height, massCenter);

		if (isPixelCenterLine(divider)) {
			return new long[] { Math.round(divider) };
		}

		long lowerValue = Long.MIN_VALUE;
		long upperValue = Long.MAX_VALUE;

		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				long value = splitter == ImageSplitter.DIAGONAL_45
						? getDiagonal45Value(x, y, width, height)
						: getDiagonal135Value(x, y, width, height);

				if (value < divider && value > lowerValue) {
					lowerValue = value;
				}
				if (value > divider && value < upperValue) {
					upperValue = value;
				}
			}
		}

		if (lowerValue == Long.MIN_VALUE && upperValue == Long.MAX_VALUE) {
			return new long[0];
		}
		if (lowerValue == Long.MIN_VALUE) {
			return new long[] { upperValue };
		}
		if (upperValue == Long.MAX_VALUE) {
			return new long[] { lowerValue };
		}

		return lowerValue == upperValue ? new long[] { lowerValue } : new long[] { lowerValue, upperValue };
	}

	public static int[] reflectAcrossLine(int x, int y, double a, double b, double c) {
		double divisor = a * a + b * b;

		if (divisor == 0D) {
			return new int[] { x, y };
		}

		double value = a * x + b * y - c;

		int reflectedX = (int) Math.round(x - 2D * a * value / divisor);
		int reflectedY = (int) Math.round(y - 2D * b * value / divisor);

		return new int[] { reflectedX, reflectedY };
	}

	@Getter
	@Setter
	@AllArgsConstructor
	public static class SplitResult {

		private float symmetry;
		private ImageSplitter splitter;

	}

}
