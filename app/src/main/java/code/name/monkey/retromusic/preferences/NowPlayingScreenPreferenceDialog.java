package code.name.monkey.retromusic.preferences;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.bumptech.glide.Glide;
import com.name.monkey.retromusic.ui.fragments.player.NowPlayingScreen;

import butterknife.ButterKnife;
import com.velitasali.music.R;
import code.name.monkey.retromusic.util.PreferenceUtil;
import code.name.monkey.retromusic.util.ViewUtil;

/**
 * Created by hemanths on 31/08/17.
 */

public class NowPlayingScreenPreferenceDialog extends DialogFragment implements ViewPager.OnPageChangeListener, MaterialDialog.SingleButtonCallback {
    public static final String TAG = NowPlayingScreenPreferenceDialog.class.getSimpleName();

    private DialogAction whichButtonClicked;
    private int viewPagerPosition;

    public static NowPlayingScreenPreferenceDialog newInstance() {
        return new NowPlayingScreenPreferenceDialog();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        @SuppressLint("InflateParams") View view = LayoutInflater.from(getActivity()).inflate(R.layout.preference_dialog_now_playing_screen, null);
        ViewPager viewPager = ButterKnife.findById(view, R.id.now_playing_screen_view_pager);
        viewPager.setAdapter(new NowPlayingScreenAdapter(getActivity()));
        viewPager.addOnPageChangeListener(this);
        viewPager.setPageMargin((int) ViewUtil.convertDpToPixel(32, getResources()));
        viewPager.setCurrentItem(PreferenceUtil.getInstance(getActivity()).getNowPlayingScreen().ordinal());


        return new MaterialDialog.Builder(getActivity())
                .title(R.string.pref_title_now_playing_screen_appearance)
                .positiveText(android.R.string.ok)
                .negativeText(android.R.string.cancel)
                .onAny(this)
                .customView(view, false)
                .build();
    }

    @Override
    public void onClick(@NonNull MaterialDialog dialog,
                        @NonNull DialogAction which) {
        whichButtonClicked = which;
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        if (whichButtonClicked == DialogAction.POSITIVE) {
            PreferenceUtil.getInstance(getActivity()).setNowPlayingScreen(NowPlayingScreen.values()[viewPagerPosition]);
        }
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {
        this.viewPagerPosition = position;
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    private static class NowPlayingScreenAdapter extends PagerAdapter {

        private Context context;

        public NowPlayingScreenAdapter(Context context) {
            this.context = context;
        }

        @NonNull
        @Override
        public Object instantiateItem(@NonNull ViewGroup collection
                , int position) {
            NowPlayingScreen nowPlayingScreen = NowPlayingScreen.values()[position];

            LayoutInflater inflater = LayoutInflater.from(context);
            ViewGroup layout = (ViewGroup) inflater.inflate(R.layout.preference_now_playing_screen_item, collection, false);
            collection.addView(layout);

            ImageView image = layout.findViewById(R.id.image);
            TextView title = layout.findViewById(R.id.title);
            Glide.with(context).load(nowPlayingScreen.drawableResId).into(image);
            title.setText(nowPlayingScreen.titleRes);

            return layout;
        }

        @Override
        public void destroyItem(@NonNull ViewGroup collection,
                                int position,
                                @NonNull Object view) {
            collection.removeView((View) view);
        }

        @Override
        public int getCount() {
            return NowPlayingScreen.values().length;
        }

        @Override
        public boolean isViewFromObject(@NonNull View view,
                                        @NonNull Object object) {
            return view == object;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return context.getString(NowPlayingScreen.values()[position].titleRes);
        }
    }
}
