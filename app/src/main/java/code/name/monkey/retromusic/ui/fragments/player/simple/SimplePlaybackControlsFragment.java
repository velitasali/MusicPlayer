package code.name.monkey.retromusic.ui.fragments.player.simple;

import android.graphics.PorterDuff;
import android.graphics.drawable.ClipDrawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;

import com.kabouzeid.appthemehelper.ThemeStore;
import com.kabouzeid.appthemehelper.util.ATHUtil;
import com.kabouzeid.appthemehelper.util.ColorUtil;
import com.kabouzeid.appthemehelper.util.MaterialValueHelper;
import com.kabouzeid.appthemehelper.util.TintHelper;
import com.retro.musicplayer.backend.model.Song;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import code.name.monkey.retromusic.R;
import code.name.monkey.retromusic.helper.MusicPlayerRemote;
import code.name.monkey.retromusic.helper.MusicProgressViewUpdateHelper;
import code.name.monkey.retromusic.service.MusicService;
import code.name.monkey.retromusic.ui.fragments.VolumeFragment;
import code.name.monkey.retromusic.ui.fragments.base.AbsPlayerControlsFragment;
import code.name.monkey.retromusic.util.MusicUtil;
import code.name.monkey.retromusic.util.PreferenceUtil;
import com.retro.musicplayer.backend.views.PlayPauseDrawable;

/**
 * @author Hemanth S (h4h13).
 */

public class SimplePlaybackControlsFragment extends AbsPlayerControlsFragment {
    @BindView(R.id.player_play_pause_fab)
    ImageButton playPauseFab;
    @BindView(R.id.player_prev_button)
    ImageButton prevButton;
    @BindView(R.id.player_next_button)
    ImageButton nextButton;
    @BindView(R.id.player_repeat_button)
    ImageButton repeatButton;
    @BindView(R.id.player_shuffle_button)
    ImageButton shuffleButton;
    @BindView(R.id.player_song_current_progress)
    TextView songCurrentProgress;
    @BindView(R.id.volume_fragment_container)
    View mVolumeContainer;
    @BindView(R.id.title)
    TextView mTitle;
    @BindView(R.id.text)
    TextView mText;
    private Unbinder mUnbinder;
    private PlayPauseDrawable mPlayerFabPlayPauseDrawable;
    private int mLastPlaybackControlsColor;
    private int mLastDisabledPlaybackControlsColor;
    private MusicProgressViewUpdateHelper mProgressViewUpdateHelper;
    private VolumeFragment mVolumeFragment;

    @Override
    public void onPlayStateChanged() {
        updatePlayPauseDrawableState(true);
    }

    @Override
    public void onRepeatModeChanged() {
        updateRepeatState();
    }

    @Override
    public void onShuffleModeChanged() {
        updateShuffleState();
    }

    @Override
    public void onServiceConnected() {
        updatePlayPauseDrawableState(false);
        updateRepeatState();
        updateShuffleState();
        updateSong();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mProgressViewUpdateHelper = new MusicProgressViewUpdateHelper(this);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mUnbinder.unbind();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_simple_controls_fragment, container, false);
        mUnbinder = ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        mProgressViewUpdateHelper.start();
    }

    @Override
    public void onPause() {
        super.onPause();
        mProgressViewUpdateHelper.stop();
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setUpMusicControllers();
        if (PreferenceUtil.getInstance(getContext()).getVolumeToggle()) {
            mVolumeContainer.setVisibility(View.VISIBLE);
        } else {
            mVolumeContainer.setVisibility(View.GONE);
        }
        mVolumeFragment = (VolumeFragment) getChildFragmentManager().findFragmentById(R.id.volume_fragment);
    }

    private void setUpMusicControllers() {
        setUpPlayPauseFab();
        setUpPrevNext();
        setUpRepeatButton();
        setUpShuffleButton();
        setUpProgressSlider();
    }

    private void setUpPrevNext() {
        updatePrevNextColor();
        nextButton.setOnClickListener(v -> MusicPlayerRemote.playNextSong());
        prevButton.setOnClickListener(v -> MusicPlayerRemote.back());
    }

    private void updatePrevNextColor() {
        nextButton.setColorFilter(mLastPlaybackControlsColor, PorterDuff.Mode.SRC_IN);
        prevButton.setColorFilter(mLastPlaybackControlsColor, PorterDuff.Mode.SRC_IN);
    }

    private void setUpShuffleButton() {
        shuffleButton.setOnClickListener(v -> MusicPlayerRemote.toggleShuffleMode());
    }

    @Override
    protected void updateShuffleState() {
        switch (MusicPlayerRemote.getShuffleMode()) {
            case MusicService.SHUFFLE_MODE_SHUFFLE:
                shuffleButton.setColorFilter(mLastPlaybackControlsColor, PorterDuff.Mode.SRC_IN);
                break;
            default:
                shuffleButton.setColorFilter(mLastDisabledPlaybackControlsColor, PorterDuff.Mode.SRC_IN);
                break;
        }
    }

    private void setUpRepeatButton() {
        repeatButton.setOnClickListener(v -> MusicPlayerRemote.cycleRepeatMode());
    }

    @Override
    protected void updateRepeatState() {
        switch (MusicPlayerRemote.getRepeatMode()) {
            case MusicService.REPEAT_MODE_NONE:
                repeatButton.setImageResource(R.drawable.ic_repeat_white_24dp);
                repeatButton.setColorFilter(mLastDisabledPlaybackControlsColor, PorterDuff.Mode.SRC_IN);
                break;
            case MusicService.REPEAT_MODE_ALL:
                repeatButton.setImageResource(R.drawable.ic_repeat_white_24dp);
                repeatButton.setColorFilter(mLastPlaybackControlsColor, PorterDuff.Mode.SRC_IN);
                break;
            case MusicService.REPEAT_MODE_THIS:
                repeatButton.setImageResource(R.drawable.ic_repeat_one_white_24dp);
                repeatButton.setColorFilter(mLastPlaybackControlsColor, PorterDuff.Mode.SRC_IN);
                break;
        }
    }

    private void updateSong() {
        Song song = MusicPlayerRemote.getCurrentSong();
        mTitle.setText(song.title);
        mText.setText(song.artistName);
    }

    @Override
    public void onPlayingMetaChanged() {
        super.onPlayingMetaChanged();
        updateSong();

    }


    @Override
    protected void setUpProgressSlider() {

    }


    @Override
    protected void show() {
        playPauseFab.animate()
                .scaleX(1f)
                .scaleY(1f)
                .rotation(360f)
                .setInterpolator(new DecelerateInterpolator())
                .start();
    }

    @Override
    protected void hide() {
        if (playPauseFab != null) {
            playPauseFab.setScaleX(0f);
            playPauseFab.setScaleY(0f);
            playPauseFab.setRotation(0f);
        }
    }


    public void showBouceAnimation() {
        playPauseFab.clearAnimation();

        playPauseFab.setScaleX(0.9f);
        playPauseFab.setScaleY(0.9f);
        playPauseFab.setVisibility(View.VISIBLE);
        playPauseFab.setPivotX(playPauseFab.getWidth() / 2);
        playPauseFab.setPivotY(playPauseFab.getHeight() / 2);

        playPauseFab.animate()
                .setDuration(200)
                .setInterpolator(new DecelerateInterpolator())
                .scaleX(1.1f)
                .scaleY(1.1f)
                .withEndAction(() -> playPauseFab.animate()
                        .setDuration(200)
                        .setInterpolator(new AccelerateInterpolator())
                        .scaleX(1f)
                        .scaleY(1f)
                        .alpha(1f)
                        .start())
                .start();
    }

    @OnClick(R.id.player_play_pause_fab)
    void showAnimation() {
        if (MusicPlayerRemote.isPlaying()) {
            MusicPlayerRemote.pauseSong();
        } else {
            MusicPlayerRemote.resumePlaying();
        }
        showBouceAnimation();
    }

    @Override
    public void onUpdateProgressViews(int progress, int total) {
        songCurrentProgress.setText(String.format("%s/ %s", MusicUtil.getReadableDurationString(progress),
                MusicUtil.getReadableDurationString(total)));
    }

    @Override
    public void setDark(int dark) {
        int color = ATHUtil.resolveColor(getActivity(), android.R.attr.colorBackground);
        if (ColorUtil.isColorLight(color)) {
            mLastPlaybackControlsColor = MaterialValueHelper
                    .getSecondaryTextColor(getActivity(), true);
            mLastDisabledPlaybackControlsColor = MaterialValueHelper
                    .getSecondaryDisabledTextColor(getActivity(), true);
        } else {
            mLastPlaybackControlsColor = MaterialValueHelper
                    .getPrimaryTextColor(getActivity(), false);
            mLastDisabledPlaybackControlsColor = MaterialValueHelper
                    .getPrimaryDisabledTextColor(getActivity(), false);
        }

        if (PreferenceUtil.getInstance(getContext()).getAdaptiveColor()) {
            TintHelper.setTintAuto(playPauseFab, dark, true);
            mText.setTextColor(dark);
        } else {
            mText.setTextColor(ThemeStore.accentColor(getContext()));
        }

        updateRepeatState();
        updateShuffleState();
        updatePrevNextColor();
    }

    public void setProgressBarColor(SeekBar progressBar, int newColor) {
        LayerDrawable ld = (LayerDrawable) progressBar.getProgressDrawable();
        ClipDrawable clipDrawable = (ClipDrawable) ld.findDrawableByLayerId(android.R.id.progress);
        clipDrawable.setColorFilter(newColor, PorterDuff.Mode.SRC_IN);
    }

    private void setUpPlayPauseFab() {
        mPlayerFabPlayPauseDrawable = new PlayPauseDrawable(getActivity());

        playPauseFab.setImageDrawable(mPlayerFabPlayPauseDrawable); // Note: set the drawable AFTER TintHelper.setTintAuto() was called
        //playPauseFab.setColorFilter(MaterialValueHelper.getPrimaryTextColor(getContext(), ColorUtil.isColorLight(fabColor)), PorterDuff.Mode.SRC_IN);
        //playPauseFab.setOnClickListener(new PlayPauseButtonOnClickHandler());
        playPauseFab.post(() -> {
            if (playPauseFab != null) {
                playPauseFab.setPivotX(playPauseFab.getWidth() / 2);
                playPauseFab.setPivotY(playPauseFab.getHeight() / 2);
            }
        });
    }

    protected void updatePlayPauseDrawableState(boolean animate) {
        if (MusicPlayerRemote.isPlaying()) {
            mPlayerFabPlayPauseDrawable.setPause(animate);
        } else {
            mPlayerFabPlayPauseDrawable.setPlay(animate);
        }
    }
}
