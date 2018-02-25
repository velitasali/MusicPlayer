package code.name.monkey.retromusic.ui.activities;

import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.view.WindowManager;

import butterknife.BindView;
import butterknife.ButterKnife;
import code.name.monkey.appthemehelper.util.ColorUtil;
import code.name.monkey.appthemehelper.util.MaterialValueHelper;
import code.name.monkey.backend.swipebtn.SwipeButton;
import code.name.monkey.retromusic.R;
import code.name.monkey.retromusic.ui.activities.base.AbsMusicServiceActivity;
import code.name.monkey.retromusic.ui.fragments.player.PlayerAlbumCoverFragment;
import code.name.monkey.retromusic.ui.fragments.player.normal.PlayerPlaybackControlsFragment;
import code.name.monkey.retromusic.util.PreferenceUtil;

public class LockScreenActivity extends AbsMusicServiceActivity implements PlayerAlbumCoverFragment.Callbacks {

    @BindView(R.id.swipe_btn)
    SwipeButton mSwipeButton;

    private PlayerPlaybackControlsFragment mPlayerPlaybackControlsFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
        setDrawUnderStatusbar(true);
        setContentView(R.layout.activity_lock_screen);
        hideStatusBar();
        setStatusbarColorAuto();
        setNavigationbarColorAuto();
        setTaskDescriptionColorAuto();

        ButterKnife.bind(this);

        PlayerAlbumCoverFragment albumCoverFragment = (PlayerAlbumCoverFragment) getSupportFragmentManager().findFragmentById(R.id.player_album_cover_fragment);
        albumCoverFragment.setCallbacks(this);

        mPlayerPlaybackControlsFragment = (PlayerPlaybackControlsFragment) getSupportFragmentManager().findFragmentById(R.id.playback_controls_fragment);
        mPlayerPlaybackControlsFragment.hideVolumeIfAvailable();

        swipButtonSetup();
    }

    private void swipButtonSetup() {
        mSwipeButton.setDisabledDrawable(ContextCompat.getDrawable(this, R.drawable.ic_lock_outline_black_24dp));
        mSwipeButton.setEnabledDrawable(ContextCompat.getDrawable(this, R.drawable.ic_lock_open_white_24dp));
        mSwipeButton.setOnActiveListener(this::finish);
    }

    @Override
    public void onServiceConnected() {
        super.onServiceConnected();
        //loadSong();
    }


    private void changeColor(int color) {
        int colorPrimary = MaterialValueHelper.getPrimaryTextColor(this, ColorUtil.isColorLight(color));
        mSwipeButton.setCenterTextColor(colorPrimary);

        Drawable drawable = ContextCompat.getDrawable(LockScreenActivity.this, R.drawable.lockscreen_gradient);
        if (drawable != null) {
            if (PreferenceUtil.getInstance(this).getAdaptiveColor()) {
                drawable.setColorFilter(color, PorterDuff.Mode.SRC_IN);
            } else {
                mSwipeButton.setCenterTextColor(Color.WHITE);
            }
            mSwipeButton.setBackground(drawable);
        }
    }

    @Override
    public void onPlayingMetaChanged() {
        super.onPlayingMetaChanged();
        //loadSong();
    }

    @Override
    public void onColorChanged(int color) {
        mPlayerPlaybackControlsFragment.setDark(color);
        changeColor(color);
    }

    @Override
    public void onFavoriteToggled() {

    }

}
