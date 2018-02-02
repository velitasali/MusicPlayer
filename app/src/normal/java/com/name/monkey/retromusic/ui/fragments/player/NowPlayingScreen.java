package com.name.monkey.retromusic.ui.fragments.player;

import android.support.annotation.DrawableRes;
import android.support.annotation.StringRes;

import code.name.monkey.retromusic.R;


public enum NowPlayingScreen {
    NORMAL(R.string.normal, R.drawable.np_normal, 0),
    FLAT(R.string.flat, R.drawable.np_flat, 1),
    FULL(R.string.full, R.drawable.np_full, 2),
    PLAIN(R.string.plain, R.drawable.np_plain, 3),
    /*SIMPLE(R.string.simple, R.drawable.np_simple, 4),
    TINY(R.string.tiny, R.drawable.np_tiny, 5),*/
    BLUR(R.string.blur, R.drawable.np_normal, 4);
    //SOLID(R.string.solid, R.drawable.np_normal, 8);

    @StringRes
    public final int titleRes;
    @DrawableRes
    public final int drawableResId;
    public final int id;

    NowPlayingScreen(@StringRes int titleRes, @DrawableRes int drawableResId, int id) {
        this.titleRes = titleRes;
        this.drawableResId = drawableResId;
        this.id = id;
    }
}
