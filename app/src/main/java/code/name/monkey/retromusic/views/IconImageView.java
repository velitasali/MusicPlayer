package code.name.monkey.retromusic.views;

import android.content.Context;
import android.graphics.PorterDuff;
import android.util.AttributeSet;
import android.widget.ImageView;

import code.name.monkey.appthemehelper.util.ATHUtil;

import com.velitasali.music.R;

/**
 * @author Karim Abou Zeid (kabouzeid)
 */
public class IconImageView extends android.support.v7.widget.AppCompatImageView {
    public IconImageView(Context context) {
        super(context);
        init(context);
    }

    public IconImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public IconImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        if (context == null) return;
        setColorFilter(ATHUtil.resolveColor(context, R.attr.iconColor), PorterDuff.Mode.SRC_IN);
    }
}
