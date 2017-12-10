package code.name.monkey.retromusic.util;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.support.annotation.ColorInt;
import android.support.annotation.Nullable;
import android.support.v7.graphics.Palette;

import com.kabouzeid.appthemehelper.util.ColorUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * @author Karim Abou Zeid (kabouzeid)
 */
public class RetroMusicColorUtil {

    @Nullable
    public static Palette generatePalette(Bitmap bitmap) {
        if (bitmap == null) return null;
        return Palette.from(bitmap).generate();
    }

    public static int getMatColor(Context context, String typeColor) {
        int returnColor = Color.BLACK;
        int arrayId = context.getResources().getIdentifier("mdcolor_" + typeColor, "array", context.getApplicationContext().getPackageName());

        if (arrayId != 0) {
            TypedArray colors = context.getResources().obtainTypedArray(arrayId);
            int index = (int) (Math.random() * colors.length());
            returnColor = colors.getColor(index, Color.BLACK);
            colors.recycle();
        }
        return returnColor;
    }

    @ColorInt
    public static int getColor(@Nullable Palette palette, int fallback) {
        if (palette != null) {
            if (palette.getVibrantSwatch() != null) {
                return palette.getVibrantSwatch().getRgb();
            } else if (palette.getMutedSwatch() != null) {
                return palette.getMutedSwatch().getRgb();
            } else if (palette.getDarkVibrantSwatch() != null) {
                return palette.getDarkVibrantSwatch().getRgb();
            } else if (palette.getDarkMutedSwatch() != null) {
                return palette.getDarkMutedSwatch().getRgb();
            } else if (palette.getLightVibrantSwatch() != null) {
                return palette.getLightVibrantSwatch().getRgb();
            } else if (palette.getLightMutedSwatch() != null) {
                return palette.getLightMutedSwatch().getRgb();
            } else if (!palette.getSwatches().isEmpty()) {
                return Collections.max(palette.getSwatches(), SwatchComparator.getInstance()).getRgb();
            }
        }
        return fallback;
    }

    public static int getDominantColor(Bitmap bitmap, int defaultFooterColor) {
        List<Palette.Swatch> swatchesTemp = Palette.from(bitmap).generate().getSwatches();
        List<Palette.Swatch> swatches = new ArrayList<Palette.Swatch>(swatchesTemp);
        Collections.sort(swatches, new Comparator<Palette.Swatch>() {
            @Override
            public int compare(Palette.Swatch swatch1, Palette.Swatch swatch2) {
                return swatch2.getPopulation() - swatch1.getPopulation();
            }
        });
        return swatches.size() > 0 ? swatches.get(0).getRgb() : defaultFooterColor;
    }

    @ColorInt
    public static int shiftBackgroundColorForLightText(@ColorInt int backgroundColor) {
        while (ColorUtil.isColorLight(backgroundColor)) {
            backgroundColor = ColorUtil.darkenColor(backgroundColor);
        }
        return backgroundColor;
    }

    private static class SwatchComparator implements Comparator<Palette.Swatch> {
        private static SwatchComparator sInstance;

        static SwatchComparator getInstance() {
            if (sInstance == null) {
                sInstance = new SwatchComparator();
            }
            return sInstance;
        }

        @Override
        public int compare(Palette.Swatch lhs, Palette.Swatch rhs) {
            return lhs.getPopulation() - rhs.getPopulation();
        }
    }
}
