package code.name.monkey.retromusic.util;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.graphics.Palette;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import code.name.monkey.appthemehelper.util.ColorUtil;
import code.name.monkey.backend.util.ColorUtils;

/**
 * @author Karim Abou Zeid (kabouzeid)
 */
public class RetroColorUtil
{

	@Nullable
	public static Palette generatePalette(Bitmap bitmap)
	{
		return bitmap == null ? null : Palette.from(bitmap).clearFilters().generate();
	}

	public static int getTextColor(@Nullable Palette palette, Palette.Swatch swatch)
	{
		int background = swatch.getRgb();
		int inverse = -1;
		if (palette != null) {
			inverse = ColorUtils.isColorSaturated(background) ? palette.getMutedColor(-1) : palette.getVibrantColor(-1);

			if (inverse != -1) {
				return ColorUtils.getReadableText(inverse, background, 150);
			}
		}
		return ColorUtils.getReadableText(background, background);
	}


	@NonNull
	public static Palette.Swatch getSwatch(@Nullable Palette palette)
	{
		if (palette == null) {
			return new Palette.Swatch(Color.WHITE, 1);
		}

		Palette.Swatch swatch;
		swatch = palette.getDominantSwatch();
		if (swatch == null) {
			swatch = new Palette.Swatch(Color.WHITE, 1);
		}
		return swatch;
	}

	public static int getMatColor(Context context, String typeColor)
	{
		int returnColor = Color.BLACK;
		int arrayId = context.getResources().getIdentifier("md_" + typeColor, "array", context.getApplicationContext().getPackageName());

		if (arrayId != 0) {
			TypedArray colors = context.getResources().obtainTypedArray(arrayId);
			int index = (int) (Math.random() * colors.length());
			returnColor = colors.getColor(index, Color.BLACK);
			colors.recycle();
		}
		return returnColor;
	}

	@ColorInt
	public static int getColor(@Nullable Palette palette, int fallback)
	{
		if (palette != null) {
			if (palette.getVibrantSwatch() != null) {
				return palette.getVibrantSwatch().getRgb();
			} else if (palette.getDarkVibrantSwatch() != null) {
				return palette.getDarkVibrantSwatch().getRgb();
			} else if (palette.getLightVibrantSwatch() != null) {
				return palette.getLightVibrantSwatch().getRgb();
			} else if (palette.getMutedSwatch() != null) {
				return palette.getMutedSwatch().getRgb();
			} else if (palette.getLightMutedSwatch() != null) {
				return palette.getLightMutedSwatch().getRgb();
			} else if (palette.getDarkMutedSwatch() != null) {
				return palette.getDarkMutedSwatch().getRgb();
			} else if (!palette.getSwatches().isEmpty()) {
				return Collections.max(palette.getSwatches(), SwatchComparator.getInstance()).getRgb();
			}
		}
		return fallback;
	}

	@ColorInt
	public static int getTextColor(@Nullable Palette palette)
	{
		return getTextSwatch(palette).getRgb();
	}

	private static Palette.Swatch getTextSwatch(@Nullable Palette palette)
	{
		if (palette == null) {
			return new Palette.Swatch(Color.BLACK, 1);
		}
		if (palette.getVibrantSwatch() != null) {
			return palette.getVibrantSwatch();
		} else {
			return new Palette.Swatch(Color.BLACK, 1);
		}
	}

	@ColorInt
	public static int getBackgroundColor(@Nullable Palette palette)
	{
		return getProperBackgroundSwatch(palette).getRgb();
	}

	private static Palette.Swatch getProperBackgroundSwatch(@Nullable Palette palette)
	{
		if (palette == null) {
			return new Palette.Swatch(Color.BLACK, 1);
		}
		if (palette.getDarkMutedSwatch() != null) {
			return palette.getDarkMutedSwatch();
		} else if (palette.getMutedSwatch() != null) {
			return palette.getMutedSwatch();
		} else if (palette.getLightMutedSwatch() != null) {
			return palette.getLightMutedSwatch();
		} else {
			return new Palette.Swatch(Color.BLACK, 1);
		}
	}

	private static Palette.Swatch getBestPaletteSwatchFrom(Palette palette)
	{
		if (palette != null) {
			if (palette.getVibrantSwatch() != null)
				return palette.getVibrantSwatch();
			else if (palette.getMutedSwatch() != null)
				return palette.getMutedSwatch();
			else if (palette.getDarkVibrantSwatch() != null)
				return palette.getDarkVibrantSwatch();
			else if (palette.getDarkMutedSwatch() != null)
				return palette.getDarkMutedSwatch();
			else if (palette.getLightVibrantSwatch() != null)
				return palette.getLightVibrantSwatch();
			else if (palette.getLightMutedSwatch() != null)
				return palette.getLightMutedSwatch();
			else if (!palette.getSwatches().isEmpty())
				return getBestPaletteSwatchFrom(palette.getSwatches());
		}
		return null;
	}

	private static Palette.Swatch getBestPaletteSwatchFrom(List<Palette.Swatch> swatches)
	{
		if (swatches == null) return null;
		return Collections.max(swatches, (opt1, opt2) -> {
			int a = opt1 == null ? 0 : opt1.getPopulation();
			int b = opt2 == null ? 0 : opt2.getPopulation();
			return a - b;
		});
	}


	public static int getDominantColor(Bitmap bitmap, int defaultFooterColor)
	{
		List<Palette.Swatch> swatchesTemp = Palette.from(bitmap).generate().getSwatches();
		List<Palette.Swatch> swatches = new ArrayList<Palette.Swatch>(swatchesTemp);
		Collections.sort(swatches, (swatch1, swatch2) -> swatch2.getPopulation() - swatch1.getPopulation());
		return swatches.size() > 0 ? swatches.get(0).getRgb() : defaultFooterColor;
	}

	@ColorInt
	public static int shiftBackgroundColorForLightText(@ColorInt int backgroundColor)
	{
		while (ColorUtil.isColorLight(backgroundColor)) {
			backgroundColor = ColorUtil.darkenColor(backgroundColor);
		}
		return backgroundColor;
	}


	private static class SwatchComparator implements Comparator<Palette.Swatch>
	{
		private static SwatchComparator sInstance;

		static SwatchComparator getInstance()
		{
			if (sInstance == null) {
				sInstance = new SwatchComparator();
			}
			return sInstance;
		}

		@Override
		public int compare(Palette.Swatch lhs, Palette.Swatch rhs)
		{
			return lhs.getPopulation() - rhs.getPopulation();
		}
	}
}
