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
import net.lopymine.ipi.InventoryInteractions;
import net.lopymine.ipi.config.InventoryInteractionsConfig;
import net.lopymine.ipi.config.base.*;
import net.lopymine.ipi.resourcepack.base.BaseConfigsManager.BaseTexture;
import net.lopymine.mossylib.utils.ArgbUtils;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;
import static net.lopymine.ipi.resourcepack.base.BaseConfigsManager.*;

@ExtensionMethod(NativeImageExtension.class)
public class BaseTextureGenerationManager {

	public static final Map<Item, List<ItemOffset>> ITEM_SEPARATORS = new HashMap<>();

	@Nullable
	public static BaseTexture generateBaseTexture(Identifier itemId, Item item) {
		RenderedItemImage renderedItemImage = ItemRenderingManager.getRenderedItemImage(item, itemId, TextureExtractMode.ITEM);
		if (renderedItemImage == null) {
			return null;
		}

		NativeImage image = renderedItemImage.getImage();

		ItemOffset massCenter = Optional.of(Texture2ObjectsManager.readFromTexture(image, itemId,
				"mass center position",
				Texture2ObjectPixelFilter.NOT_TRANSPARENT,
				PIXEL_POSITION
		)).filter((list) -> !list.isEmpty()).map((list) -> findCenter(list, STANDARD_MIDDLE_BOTTOM_MASS_POS)).orElse(STANDARD_MIDDLE_BOTTOM_MASS_POS);
		ItemOffset partConnectionCenter = getPartConnectionCenter(itemId, item, image, massCenter);

		return new BaseTexture(partConnectionCenter, STANDARD_MIDDLE_PART_CONNECTION_POS, massCenter);

	}

	private static @NonNull ItemOffset getPartConnectionCenter(Identifier itemId, Item item, NativeImage image, ItemOffset massCenter) {
		List<SplitResult> list = new ArrayList<>();

		if ("minecraft:trident".equals(itemId.toString())) {
			InventoryInteractions.LOGGER.info(
					"trident massCenter {}, {}, diag45Divider {}",
					(massCenter.getOffsetX() + 0.5D),
					(massCenter.getOffsetY() + 0.5D),
					getDiagonal45Divider(image.getWidth(), image.getHeight(), massCenter)
			);
		}

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

		if (result.symmetry < 0.5) {
			InventoryInteractions.LOGGER.info(
					"[1] {}: symmetry {}, splitter {}, other {}",
					itemId.toString(),
					String.format(Locale.US, "%.2f", result.symmetry),
					result.splitter.name(),
					allResults
			);
			return getFarthestGrabPos(image, massCenter);
		}

		if (InventoryInteractionsConfig.getInstance().isDebugModeEnabled()) {
			for (ImageSplitter value : ImageSplitter.values()) {
				if (value == result.splitter) {
					continue;
				}
				getFarthestGrabPosOnSplitDiagonal(image, item, massCenter, value);
			}
		}

		ItemOffset farthestGrabPosOnSplitDiagonal = getFarthestGrabPosOnSplitDiagonal(image, item, massCenter, result.splitter);

		double distance = Math.hypot(
				farthestGrabPosOnSplitDiagonal.getOffsetX() - massCenter.getOffsetX(),
				farthestGrabPosOnSplitDiagonal.getOffsetY() - massCenter.getOffsetY()
		);

		if (distance <= 2.0D) {
			InventoryInteractions.LOGGER.info(
					"[22] {}: symmetry {}, splitter {}, distance {}, other {}",
					itemId.toString(),
					String.format(Locale.US, "%.2f", result.symmetry),
					result.splitter.name(),
					String.format(Locale.US, "%.2f", distance),
					allResults
			);
			return getFarthestGrabPos(image, massCenter);
		}

		InventoryInteractions.LOGGER.info(
				"[333] {}: symmetry {}, splitter {}, distance {},, other {}",
				itemId.toString(),
				String.format(Locale.US, "%.2f", result.symmetry),
				result.splitter.name(),
				String.format(Locale.US, "%.2f", distance),
				allResults
		);

		return farthestGrabPosOnSplitDiagonal;
	}

	private static ItemOffset getFarthestGrabPosOnSplitDiagonal(NativeImage image, Item item, ItemOffset massCenter, ImageSplitter splitter) {
		List<ItemOffset> fallbackOffsets = new ArrayList<>();
		List<ItemOffset> offsets = new ArrayList<>();

		switch (splitter) {
			case HORIZONTAL -> {
				for (int y : getNearestAxisIndexes((massCenter.getOffsetY() + 0.5D), image.getHeight())) {
					for (int x = 0; x < image.getWidth(); x++) {
						addLineOffset(image, x, y, offsets, fallbackOffsets);
					}
				}
			}
			case VERTICAL -> {
				for (int x : getNearestAxisIndexes((massCenter.getOffsetX() + 0.5D), image.getWidth())) {
					for (int y = 0; y < image.getHeight(); y++) {
						addLineOffset(image, x, y, offsets, fallbackOffsets);
					}
				}
			}
			case DIAGONAL_45, DIAGONAL_135 -> {
				collectDiagonalOffsets(image, massCenter, splitter, offsets, fallbackOffsets);
			}
		}

		List<ItemOffset> resultOffsets = offsets.isEmpty() ? fallbackOffsets : offsets;

		ITEM_SEPARATORS.computeIfAbsent(item, (k) -> new ArrayList<>()).addAll(resultOffsets);

		return getFarthestItemOffset(massCenter, resultOffsets);
	}

	private static ItemOffset getFarthestGrabPos(NativeImage image, ItemOffset massCenter) {
		List<ItemOffset> offsets = new ArrayList<>();
		for (int x = 0; x < image.getWidth(); x++) {
			for (int y = 0; y < image.getHeight(); y++) {
				if (withoutPixelsAround(image, x, y) >= 1) {
					continue;
				}
				offsets.add(new ItemOffset(x, y, image.getWidth(), image.getHeight()));
			}
		}

		if (offsets.isEmpty()) {
			for (int x = 0; x < image.getWidth(); x++) {
				for (int y = 0; y < image.getHeight(); y++) {
					if (transparentPixel(image, x, y)) {
						continue;
					}
					offsets.add(new ItemOffset(x, y, image.getWidth(), image.getHeight()));
				}
			}
		}

		return getFarthestItemOffset(massCenter, offsets);
	}

	private static ItemOffset getFarthestItemOffset(ItemOffset massCenter, List<ItemOffset> offsets) {
		if (offsets.isEmpty()) {
			return STANDARD_MIDDLE_PART_CONNECTION_POS;
		}

		offsets.sort((one, two) -> {
			double distance = Math.hypot(one.getOffsetX() - (massCenter.getOffsetX() + 0.5D), one.getOffsetY() - (massCenter.getOffsetY() + 0.5D));
			double distance2 = Math.hypot(two.getOffsetX() - (massCenter.getOffsetX() + 0.5D), two.getOffsetY() - (massCenter.getOffsetY() + 0.5D));
			return Double.compare(distance2, distance);
		});

		return offsets.get(0);
	}

	// 2 = skip, 0 = false, 1 = true
	private static int withoutPixelsAround(NativeImage image, int x, int y) {
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

	private static boolean transparentPixel(NativeImage image, int x, int y) {
		return ArgbUtils.getAlpha(image.getPixelArgb(x, y)) < 10;
	}

	private static float getSymmetry(DividedImage image, ImageSplitter splitter, ItemOffset massCenter) {
		int[][] first = image.first();
		int[][] second = image.second();

		int width = first.length;
		int height = first[0].length;

		long diagonal45Divider = getDiagonal45Divider(width, height, massCenter);
		long diagonal135Divider = getDiagonal135Divider(width, height, massCenter);

		int matched = 0;
		int total = 0;

		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				int a = first[x][y];

				int reflectedX = x;
				int reflectedY = y;

				switch (splitter) {
					case HORIZONTAL -> {
						reflectedY = (int) Math.round(2D * (massCenter.getOffsetY() + 0.5D) - y);
					}
					case VERTICAL -> {
						reflectedX = (int) Math.round(2D * (massCenter.getOffsetX() + 0.5D) - x);
					}
					case DIAGONAL_45 -> {
						int[] reflected = reflectAcrossLine(
								x,
								y,
								height - 1D,
								width - 1D,
								getDiagonal45Divider(width, height, massCenter)
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
								getDiagonal135Divider(width, height, massCenter)
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

	private static int[] getNearestAxisIndexes(double axis, int size) {
		if (size <= 0) {
			return new int[0];
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

	private static void addLineOffset(NativeImage image, int x, int y, List<ItemOffset> offsets, List<ItemOffset> fallbackOffsets) {
		ItemOffset offset = new ItemOffset(x, y, image.getWidth(), image.getHeight());

		int i = withoutPixelsAround(image, x, y);
		if (i >= 1) {
			if (i == 1) {
				fallbackOffsets.add(offset);
			}
			return;
		}

		offsets.add(offset);
	}

	private static long getDiagonal45Value(int x, int y, int width, int height) {
		return (long) x * (height - 1L) + (long) y * (width - 1L);
	}

	private static long getDiagonal45Divider(int width, int height, ItemOffset massCenter) {
		return Math.round(
				(massCenter.getOffsetX() + 0.5D) * (height - 1D) +
						(massCenter.getOffsetY() + 0.5D) * (width - 1D)
		);
	}

	private static long getDiagonal135Value(int x, int y, int width, int height) {
		return (long) x * (height - 1L) - (long) y * (width - 1L);
	}

	private static long getDiagonal135Divider(int width, int height, ItemOffset massCenter) {
		return Math.round(
				(massCenter.getOffsetX() + 0.5D) * (height - 1D) -
						(massCenter.getOffsetY() + 0.5D) * (width - 1D)
		);
	}

	private static int reflectXOnDiagonal45(int x, int y, int width, int height, long divider) {
		if (height <= 1) {
			return x;
		}

		return (int) Math.round((divider - y * (width - 1D)) / (height - 1D));
	}

	private static int reflectYOnDiagonal45(int x, int y, int width, int height, long divider) {
		if (width <= 1) {
			return y;
		}

		return (int) Math.round((divider - x * (height - 1D)) / (width - 1D));
	}

	private static int reflectXOnDiagonal135(int x, int y, int width, int height, long divider) {
		if (height <= 1) {
			return x;
		}

		return (int) Math.round((divider + y * (width - 1D)) / (height - 1D));
	}

	private static int reflectYOnDiagonal135(int x, int y, int width, int height, long divider) {
		if (width <= 1) {
			return y;
		}

		return (int) Math.round((x * (height - 1D) - divider) / (width - 1D));
	}

	private static void collectDiagonalOffsets(
			NativeImage image,
			ItemOffset massCenter,
			ImageSplitter splitter,
			List<ItemOffset> offsets,
			List<ItemOffset> fallbackOffsets
	) {
		int width = image.getWidth();
		int height = image.getHeight();

		long divider = splitter == ImageSplitter.DIAGONAL_45
				? getDiagonal45Divider(width, height, massCenter)
				: getDiagonal135Divider(width, height, massCenter);

		long bestDiff = Long.MAX_VALUE;
		List<int[]> bestPoints = new ArrayList<>();
		boolean hasExact = false;

		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				long value = splitter == ImageSplitter.DIAGONAL_45
						? getDiagonal45Value(x, y, width, height)
						: getDiagonal135Value(x, y, width, height);

				long diff = Math.abs(value - divider);

				if (diff == 0) {
					hasExact = true;
					addLineOffset(image, x, y, offsets, fallbackOffsets);
					continue;
				}

				if (!hasExact) {
					if (diff < bestDiff) {
						bestDiff = diff;
						bestPoints.clear();
						bestPoints.add(new int[] { x, y });
					} else if (diff == bestDiff) {
						bestPoints.add(new int[] { x, y });
					}
				}
			}
		}

		if (!hasExact) {
			for (int[] point : bestPoints) {
				addLineOffset(image, point[0], point[1], offsets, fallbackOffsets);
			}
		}
	}

	private static int[] reflectAcrossLine(int x, int y, double a, double b, double c) {
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
	private static class SplitResult {

		float symmetry;
		ImageSplitter splitter;

	}

	public enum ImageSplitter {

		VERTICAL {
			@Override
			public DividedImage splitImage(NativeImage image, ItemOffset massCenter) {
				int height = image.getHeight();
				int width = image.getWidth();

				double dividerX = (massCenter.getOffsetX() + 0.5D);

				int[][] first = new int[width][height];
				int[][] second = new int[width][height];

				for (int x = 0; x < width; x++) {
					for (int y = 0; y < height; y++) {
						if (ArgbUtils.getAlpha(image.getPixelArgb(x, y)) == 0) {
							continue;
						}

						if (Double.compare(x, dividerX) == 0) {
							continue;
						}

						if (x < dividerX) {
							first[x][y] = 1;
						} else {
							second[x][y] = 1;
						}
					}
				}

				return new DividedImage(first, second);
			}
		},
		HORIZONTAL {
			@Override
			public DividedImage splitImage(NativeImage image, ItemOffset massCenter) {
				int height = image.getHeight();
				int width = image.getWidth();

				double dividerY = (massCenter.getOffsetY() + 0.5D);

				int[][] first = new int[width][height];
				int[][] second = new int[width][height];

				for (int x = 0; x < width; x++) {
					for (int y = 0; y < height; y++) {
						if (ArgbUtils.getAlpha(image.getPixelArgb(x, y)) == 0) {
							continue;
						}

						if (Double.compare(y, dividerY) == 0) {
							continue;
						}

						if (y < dividerY) {
							first[x][y] = 1;
						} else {
							second[x][y] = 1;
						}
					}
				}

				return new DividedImage(first, second);
			}
		},

		DIAGONAL_45 {
			@Override
			public DividedImage splitImage(NativeImage image, ItemOffset massCenter) {
				int height = image.getHeight();
				int width = image.getWidth();

				long divider = getDiagonal45Divider(width, height, massCenter);

				int[][] first = new int[width][height];
				int[][] second = new int[width][height];

				for (int x = 0; x < width; x++) {
					for (int y = 0; y < height; y++) {
						if (ArgbUtils.getAlpha(image.getPixelArgb(x, y)) == 0) {
							continue;
						}

						long value = getDiagonal45Value(x, y, width, height);

						if (value == divider) {
							continue;
						}

						if (value < divider) {
							first[x][y] = 1;
						} else {
							second[x][y] = 1;
						}
					}
				}

				return new DividedImage(first, second);
			}
		},

		DIAGONAL_135 {
			@Override
			public DividedImage splitImage(NativeImage image, ItemOffset massCenter) {
				int height = image.getHeight();
				int width = image.getWidth();

				long divider = getDiagonal135Divider(width, height, massCenter);

				int[][] first = new int[width][height];
				int[][] second = new int[width][height];

				for (int x = 0; x < width; x++) {
					for (int y = 0; y < height; y++) {
						if (ArgbUtils.getAlpha(image.getPixelArgb(x, y)) == 0) {
							continue;
						}

						long value = getDiagonal135Value(x, y, width, height);

						if (value == divider) {
							continue;
						}

						if (value < divider) {
							first[x][y] = 1;
						} else {
							second[x][y] = 1;
						}
					}
				}

				return new DividedImage(first, second);
			}
		};

		public abstract DividedImage splitImage(NativeImage image, ItemOffset massCenter);
	}

	public record DividedImage(int[][] first, int[][] second) {}

}
