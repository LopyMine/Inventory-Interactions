package net.lopymine.ipi.family.generation;

import com.mojang.blaze3d.platform.NativeImage;
import java.util.Arrays;
import net.lopymine.ip.extension.NativeImageExtension;
import net.lopymine.ipi.utils.DimensionOffset;
import net.lopymine.mossylib.utils.ArgbUtils;

public enum ImageSplitter {

	VERTICAL {
		@Override
		public DividedImage splitImage(NativeImage image, DimensionOffset massCenter) {
			int height = image.getHeight();
			int width = image.getWidth();

			double dividerX = massCenter.getOffsetX();
			int[] lineIndexes = BaseTextureGenerationManager.getAxisLineIndexes(dividerX, width);
			int minLineX = Arrays.stream(lineIndexes).min().orElse(-1);
			int maxLineX = Arrays.stream(lineIndexes).max().orElse(-1);

			int[][] first = new int[width][height];
			int[][] second = new int[width][height];

			for (int x = 0; x < width; x++) {
				for (int y = 0; y < height; y++) {
					if (ArgbUtils.getAlpha(NativeImageExtension.getPixelArgb(image, x, y)) == 0) {
						continue;
					}

					if (contains(lineIndexes, x)) {
						continue;
					}

					if (x < minLineX) {
						first[x][y] = 1;
					} else if (x > maxLineX) {
						second[x][y] = 1;
					}
				}
			}

			return new DividedImage(first, second);
		}
	},
	HORIZONTAL {
		@Override
		public DividedImage splitImage(NativeImage image, DimensionOffset massCenter) {
			int height = image.getHeight();
			int width = image.getWidth();

			double dividerY = massCenter.getOffsetY();
			int[] lineIndexes = BaseTextureGenerationManager.getAxisLineIndexes(dividerY, height);
			int minLineY = Arrays.stream(lineIndexes).min().orElse(-1);
			int maxLineY = Arrays.stream(lineIndexes).max().orElse(-1);

			int[][] first = new int[width][height];
			int[][] second = new int[width][height];

			for (int x = 0; x < width; x++) {
				for (int y = 0; y < height; y++) {
					if (ArgbUtils.getAlpha(NativeImageExtension.getPixelArgb(image, x, y)) == 0) {
						continue;
					}

					if (contains(lineIndexes, y)) {
						continue;
					}

					if (y < minLineY) {
						first[x][y] = 1;
					} else if (y > maxLineY) {
						second[x][y] = 1;
					}
				}
			}

			return new DividedImage(first, second);
		}
	},

	DIAGONAL_45 {
		@Override
		public DividedImage splitImage(NativeImage image, DimensionOffset massCenter) {
			int height = image.getHeight();
			int width = image.getWidth();

			long[] lineValues = BaseTextureGenerationManager.getDiagonalLineValues(width, height, massCenter, this);
			long minLineValue = Arrays.stream(lineValues).min().orElse(Long.MIN_VALUE);
			long maxLineValue = Arrays.stream(lineValues).max().orElse(Long.MAX_VALUE);

			int[][] first = new int[width][height];
			int[][] second = new int[width][height];

			for (int x = 0; x < width; x++) {
				for (int y = 0; y < height; y++) {
					if (ArgbUtils.getAlpha(NativeImageExtension.getPixelArgb(image, x, y)) == 0) {
						continue;
					}

					long value = BaseTextureGenerationManager.getDiagonal45Value(x, y, width, height);

					if (contains(lineValues, value)) {
						continue;
					}

					if (value < minLineValue) {
						first[x][y] = 1;
					} else if (value > maxLineValue) {
						second[x][y] = 1;
					}
				}
			}

			return new DividedImage(first, second);
		}
	},

	DIAGONAL_135 {
		@Override
		public DividedImage splitImage(NativeImage image, DimensionOffset massCenter) {
			int height = image.getHeight();
			int width = image.getWidth();

			long[] lineValues = BaseTextureGenerationManager.getDiagonalLineValues(width, height, massCenter, this);
			long minLineValue = Arrays.stream(lineValues).min().orElse(Long.MIN_VALUE);
			long maxLineValue = Arrays.stream(lineValues).max().orElse(Long.MAX_VALUE);

			int[][] first = new int[width][height];
			int[][] second = new int[width][height];

			for (int x = 0; x < width; x++) {
				for (int y = 0; y < height; y++) {
					if (ArgbUtils.getAlpha(NativeImageExtension.getPixelArgb(image, x, y)) == 0) {
						continue;
					}

					long value = BaseTextureGenerationManager.getDiagonal135Value(x, y, width, height);

					if (contains(lineValues, value)) {
						continue;
					}

					if (value < minLineValue) {
						first[x][y] = 1;
					} else if (value > maxLineValue) {
						second[x][y] = 1;
					}
				}
			}

			return new DividedImage(first, second);
		}
	};

	public static boolean contains(int[] array, int value) {
		for (int i : array) {
			if (i == value) {
				return true;
			}
		}
		return false;
	}

	public static boolean contains(long[] array, long value) {
		for (long i : array) {
			if (i == value) {
				return true;
			}
		}
		return false;
	}

	public abstract DividedImage splitImage(NativeImage image, DimensionOffset massCenter);
}