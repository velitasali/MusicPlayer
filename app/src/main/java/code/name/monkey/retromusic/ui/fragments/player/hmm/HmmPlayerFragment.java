package code.name.monkey.retromusic.ui.fragments.player.hmm;

import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.afollestad.materialdialogs.internal.MDTintHelper;
import code.name.monkey.appthemehelper.util.ColorUtil;
import code.name.monkey.appthemehelper.util.MaterialValueHelper;
import code.name.monkey.backend.model.Song;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import code.name.monkey.retromusic.R;
import code.name.monkey.retromusic.helper.MusicPlayerRemote;
import code.name.monkey.retromusic.helper.MusicProgressViewUpdateHelper;
import code.name.monkey.retromusic.helper.PlayPauseButtonOnClickHandler;
import code.name.monkey.retromusic.ui.fragments.MiniPlayerFragment;
import code.name.monkey.retromusic.ui.fragments.base.AbsPlayerFragment;
import code.name.monkey.retromusic.ui.fragments.player.PlayerAlbumCoverFragment;
import code.name.monkey.retromusic.util.MusicUtil;
import code.name.monkey.retromusic.util.PreferenceUtil;
import code.name.monkey.retromusic.util.ToolbarColorizeHelper;

/**
 * @author Hemanth S (h4h13).
 */

public class HmmPlayerFragment extends AbsPlayerFragment implements
        MusicProgressViewUpdateHelper.Callback,
        PlayerAlbumCoverFragment.Callbacks {
    @BindView(R.id.title)
    TextView mTitle;
    @BindView(R.id.text)
    TextView mText;
    @BindView(R.id.player_song_total_time)
    TextView mTime;
    @BindView(R.id.progress_bar)
    ProgressBar mProgressBar;
    @BindView(R.id.player_toolbar)
    Toolbar mToolbar;
    @BindView(R.id.toolbar_container)
    FrameLayout toolbarContainer;
    private MusicProgressViewUpdateHelper mProgressViewUpdateHelper;
    private Unbinder unBinder;
    private int mLastColor;
    private HmmPlaybackControlsFragment mHmmPlaybackControlsFragment;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mProgressViewUpdateHelper = new MusicProgressViewUpdateHelper(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        mProgressViewUpdateHelper.start();
        checkToggleToolbar(toolbarContainer);
    }

    @Override
    public void onPause() {
        super.onPause();
        mProgressViewUpdateHelper.stop();
    }

    private void updateSong() {
        Song song = MusicPlayerRemote.getCurrentSong();
        mTitle.setText(song.title);
        mText.setText(String.format("%s \nby -%s", song.albumName, song.artistName));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unBinder.unbind();
    }

    @SuppressLint("ClickableViewAccessibility")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_hmm_player, container, false);
        unBinder = ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
          /*Hide status bar view for !full screen mode*/
        if (PreferenceUtil.getInstance(getContext()).getFullScreenMode()) {
            view.findViewById(R.id.status_bar).setVisibility(View.GONE);
        }

        mProgressBar.setOnClickListener(new PlayPauseButtonOnClickHandler());
        mProgressBar.setOnTouchListener(new MiniPlayerFragment.FlingPlayBackController(getActivity()));
        setUpPlayerToolbar();
        setUpSubFragments();
    }

    private void setUpSubFragments() {
        mHmmPlaybackControlsFragment = (HmmPlaybackControlsFragment) getChildFragmentManager().findFragmentById(R.id.playback_controls_fragment);
        PlayerAlbumCoverFragment playerAlbumCoverFragment = (PlayerAlbumCoverFragment) getChildFragmentManager().findFragmentById(R.id.player_album_cover_fragment);
        playerAlbumCoverFragment.setCallbacks(this);

    }

    @Override
    public int getPaletteColor() {
        return mLastColor;
    }

    @Override
    public void onShow() {
        mHmmPlaybackControlsFragment.show();
    }

    @Override
    public void onHide() {
        mHmmPlaybackControlsFragment.hide();
    }

    @Override
    public boolean onBackPressed() {
        return false;
    }

    @Override
    public Toolbar getToolbar() {
        return mToolbar;
    }

    @Override
    public int toolbarIconColor() {
        int iconColor = MaterialValueHelper.getSecondaryTextColor(getContext(), ColorUtil.isColorLight(mLastColor));
        return iconColor;
    }

    @Override
    public void onUpdateProgressViews(int progress, int total) {
        mProgressBar.setMax(total);

        ObjectAnimator animator = ObjectAnimator.ofInt(mProgressBar, "progress", progress);
        animator.setDuration(1500);
        animator.setInterpolator(new LinearInterpolator());
        animator.start();

        mTime.setText(MusicUtil.getReadableDurationString(total) +
                "/" + MusicUtil.getReadableDurationString(progress));
        //songTotalTime.setText(MusicUtil.getReadableDurationString(total));
        //songCurrentProgress.setText(MusicUtil.getReadableDurationString(progress));
    }

    private void setUpPlayerToolbar() {
        mToolbar.inflateMenu(R.menu.menu_player);
        mToolbar.setNavigationOnClickListener(v -> getActivity().onBackPressed());
        mToolbar.setOnMenuItemClickListener(this);

    }


    @Override
    protected void toggleFavorite(Song song) {
        super.toggleFavorite(song);
        if (song.id == MusicPlayerRemote.getCurrentSong().id) {
            updateIsFavorite();
        }
    }

    @Override
    public void onServiceConnected() {
        super.onServiceConnected();
        updateSong();
    }

    @Override
    public void onPlayingMetaChanged() {
        super.onPlayingMetaChanged();
        updateSong();
    }

    @Override
    public void onColorChanged(int color) {
        mLastColor = color;
        getCallbacks().onPaletteColorChanged();
        mHmmPlaybackControlsFragment.setDark(color);
        setProgressBarColor(color);

        int iconColor = MaterialValueHelper.getSecondaryTextColor(getContext(), ColorUtil.isColorLight(color));
        ToolbarColorizeHelper.colorizeToolbar(mToolbar, iconColor, getActivity());

    }

    private void setProgressBarColor(int color) {
        MDTintHelper.setTint(mProgressBar, color);
    }

    @Override
    public void onFavoriteToggled() {
        toggleFavorite(MusicPlayerRemote.getCurrentSong());
    }

    @Override
    public void onToolbarToggled() {
        //Toggle hiding toolbar for effect
        //toggleToolbar(toolbarContainer);
    }
}
