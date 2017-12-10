package code.name.monkey.retromusic.views;

import android.content.Context;
import android.graphics.Typeface;
import android.support.design.widget.TabLayout;
import android.util.AttributeSet;

import code.name.monkey.retromusic.R;

/**
 * @author Hemanth S (h4h13).
 */

public class SansFontTableLayout extends TabLayout {
    public SansFontTableLayout(Context context) {
        super(context);
        init(context);
    }

    public SansFontTableLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    private void init(Context context) {
        Typeface typefaceBold = Typeface.createFromAsset(context.getAssets(), getResources().getString(R.string.sans_bold));

    }
}
