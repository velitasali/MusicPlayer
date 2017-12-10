package code.name.monkey.retromusic.ui.fragments.player.hmm;

import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.afollestad.materialdialogs.internal.MDTintHelper;
import com.kabouzeid.appthemehelper.util.ColorUtil;
import com.kabouzeid.appthemehelper.util.MaterialValueHelper;
import com.retro.musicplayer.backend.model.Song;
import com.retro.musicplayer.backend.util.LyricUtil;

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
import code.name.monkey.retromusic.util.Util;

/**
 * @author Hemanth S (h4h13).
 */

public class HmmPlayerFragment extends AbsPlayerFragment
        implements MusicProgressViewUpdateHelper.Callback, PlayerAlbumCoverFragment.Callbacks {
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
    private AsyncTask updateIsFavoriteTask;
    private AsyncTask updateLyricsAsyncTask;
    private int iconColor;

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
        if (updateLyricsAsyncTask != null && !updateLyricsAsyncTask.isCancelled()) {
            updateLyricsAsyncTask.cancel(true);
        }
        if (updateIsFavoriteTask != null && !updateIsFavoriteTask.isCancelled()) {
            updateIsFavoriteTask.cancel(true);
        }
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
        updateLyrics();
        updateIsFavorite();
    }

    @Override
    public void onPlayingMetaChanged() {
        super.onPlayingMetaChanged();
        updateSong();
        updateLyrics();
        updateIsFavorite();
    }

    @SuppressLint("StaticFieldLeak")
    private void updateLyrics() {
        if (updateLyricsAsyncTask != null) updateLyricsAsyncTask.cancel(false);
        final Song song = MusicPlayerRemote.getCurrentSong();
        updateLyricsAsyncTask = new AsyncTask<Void, Void, Boolean>() {
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                mToolbar.getMenu().removeItem(R.id.action_show_lyrics);
            }

            @Override
            protected Boolean doInBackground(Void... params) {
                return LyricUtil.isLrcFileExist(song.title, song.artistName);

            }

            @Override
            protected void onPostExecute(Boolean l) {
                if (l) {
                    Activity activity = getActivity();
                    if (mToolbar != null && activity != null) {
                        if (mToolbar.getMenu().findItem(R.id.action_show_lyrics) == null) {
                            // int color = ToolbarContentTintHelper.toolbarContentColor(activity, Color.TRANSPARENT);
                            Drawable drawable = Util.getTintedVectorDrawable(activity,
                                    R.drawable.ic_comment_text_outline_white_24dp,
                                    iconColor);
                            mToolbar.getMenu()
                                    .add(Menu.NONE, R.id.action_show_lyrics, Menu.NONE, R.string.action_show_lyrics)
                                    .setIcon(drawable)
                                    .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
                        }
                    } else {
                        if (mToolbar != null) {
                            mToolbar.getMenu().removeItem(R.id.action_show_lyrics);
                        }
                    }
                }

            }
        }.execute();
    }

    @SuppressLint("StaticFieldLeak")
    private void updateIsFavorite() {
        if (updateIsFavoriteTask != null) updateIsFavoriteTask.cancel(false);
        updateIsFavoriteTask = new AsyncTask<Song, Void, Boolean>() {
            @Override
            protected Boolean doInBackground(Song... params) {
                Activity activity = getActivity();
                if (activity != null) {
                    return MusicUtil.isFavorite(getActivity(), params[0]);
                } else {
                    cancel(false);
                    return null;
                }
            }

            @Override
            protected void onPostExecute(Boolean isFavorite) {
                Activity activity = getActivity();
                if (activity != null) {
                    int res = isFavorite ? R.drawable.ic_favorite_white_24dp : R.drawable.ic_favorite_border_white_24dp;
                    Drawable drawable = Util.getTintedVectorDrawable(activity, res, iconColor);
                    mToolbar.getMenu().findItem(R.id.action_toggle_favorite)
                            .setIcon(drawable)
                            .setTitle(isFavorite ? getString(R.string.action_remove_from_favorites) : getString(R.string.action_add_to_favorites));
                }
            }
        }.execute(MusicPlayerRemote.getCurrentSong());
    }

    @Override
    public void onColorChanged(int color) {
        getCallbacks().onPaletteColorChanged();
        mLastColor = color;
        mHmmPlaybackControlsFragment.setDark(color);
        setProgressBarColor(color);

        iconColor = MaterialValueHelper.getSecondaryTextColor(getContext(), ColorUtil.isColorLight(color));
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
        toggleToolbar(toolbarContainer);
    }
}
