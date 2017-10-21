package code.name.monkey.retromusic.views;

import android.content.Context;
import android.graphics.Typeface;
import android.support.design.widget.CollapsingToolbarLayout;
import android.util.AttributeSet;

/**
 * @author Hemanth S (h4h13).
 */

public class SansFontCollapsingToolbarLayout extends CollapsingToolbarLayout {
    public SansFontCollapsingToolbarLayout(Context context) {
        super(context);
        init(context);
    }

    public SansFontCollapsingToolbarLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public SansFontCollapsingToolbarLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        Typeface typefaceBold = Typeface.createFromAsset(context.getAssets(), "fonts/sans_bold.ttf");
        setExpandedTitleTypeface(typefaceBold);

        Typeface typefaceNormal = Typeface.createFromAsset(context.getAssets(), "fonts/sans_regular.ttf");
        setCollapsedTitleTypeface(typefaceBold);

    }
}
