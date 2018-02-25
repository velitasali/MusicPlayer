package code.name.monkey.retromusic.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.support.annotation.ColorInt;
import android.support.annotation.ColorRes;
import android.support.annotation.DimenRes;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.widget.LinearLayout;

import code.name.monkey.retromusic.R;
import code.name.monkey.retromusic.util.ViewUtils;


/**
 * Created by ArmanSo on 4/16/17.
 */

public class RoundLinerLayoutNormal extends LinearLayout {

    int radiusCorner;

    int elevation;
    @ColorInt
    private int shadowColor;
    @ColorInt
    private int roundBackgroundColor;

    public RoundLinerLayoutNormal(Context context) {
        super(context);
        initBackground(null);
    }

    public RoundLinerLayoutNormal(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initBackground(attrs);
    }

    public RoundLinerLayoutNormal(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initBackground(attrs);
    }

    public void setRadiusCorner(@DimenRes int radiusCorner) {
        this.radiusCorner = radiusCorner;
    }

    public void setElevation(@DimenRes int elevation) {
        this.elevation = elevation;
    }

    public void setShadowColor(@ColorRes int shadowColor) {
        this.shadowColor = shadowColor;
    }

    public void setRoundBackgroundColor(@ColorRes int roundBackgroundColor) {
        this.roundBackgroundColor = roundBackgroundColor;
    }

    private void initBackground(@Nullable AttributeSet attrs) {

        TypedArray typedValue = getContext().obtainStyledAttributes(attrs, R.styleable.RoundLinerLayoutNormal);


        try {
            radiusCorner = typedValue.getInteger(R.styleable.RoundLinerLayoutNormal_shadowCornerRadius, 2);
            elevation = typedValue.getInteger(R.styleable.RoundLinerLayoutNormal_shadowElevation, 2);
            roundBackgroundColor = typedValue.getColor(R.styleable.RoundLinerLayoutNormal_roundBackgroundColor, Color.BLUE);
            shadowColor = typedValue.getColor(R.styleable.RoundLinerLayoutNormal_roundBackgroundColor, Color.RED);

        } finally {
            typedValue.recycle();
        }

        setBackground(ViewUtils.generateBackgroundWithShadow(this,
                roundBackgroundColor,
                radiusCorner,
                shadowColor,
                elevation,
                Gravity.CENTER));
    }
}