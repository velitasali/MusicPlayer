package code.name.monkey.retromusic.ui.fragments.player.holiday;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.retro.musicplayer.backend.model.Song;
import com.retro.musicplayer.backend.util.LyricUtil;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import code.name.monkey.retromusic.R;
import code.name.monkey.retromusic.helper.MusicPlayerRemote;
import code.name.monkey.retromusic.ui.fragments.base.AbsPlayerFragment;
import code.name.monkey.retromusic.ui.fragments.player.PlayerAlbumCoverFragment;
import code.name.monkey.retromusic.util.MusicUtil;
import code.name.monkey.retromusic.util.PreferenceUtil;
import code.name.monkey.retromusic.util.ToolbarColorizeHelper;
import code.name.monkey.retromusic.util.Util;

/**
 * @author Hemanth S (h4h13).
 */

public class HolidayPlayerFragment extends AbsPlayerFragment implements PlayerAlbumCoverFragment.Callbacks {
    @BindView(R.id.player_toolbar)
    Toolbar toolbar;
    @BindView(R.id.now_playing_container)
    ViewGroup mViewGroup;
    private AsyncTask updateIsFavoriteTask;
    private AsyncTask updateLyricsAsyncTask;
    private int lastColor;
    private Unbinder unBinder;
    private HolidayPlaybackControlsFragment mPlaybackControlsFragment;
    private PlayerAlbumCoverFragment playerAlbumCoverFragment;

    private void setUpSubFragments() {
        mPlaybackControlsFragment = (HolidayPlaybackControlsFragment) getChildFragmentManager().findFragmentById(R.id.playback_controls_fragment);

        playerAlbumCoverFragment = (PlayerAlbumCoverFragment) getChildFragmentManager().findFragmentById(R.id.player_album_cover_fragment);
        playerAlbumCoverFragment.setCallbacks(this);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unBinder.unbind();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.fragment_holiday_player, container, false);
        unBinder = ButterKnife.bind(this, layout);
        return layout;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
       /*Adding margin to toolbar for !full screen mode*/
        if (!PreferenceUtil.getInstance(getContext()).getFullScreenMode()) {
            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) mViewGroup.getLayoutParams();
            params.topMargin = getResources().getDimensionPixelOffset(R.dimen.status_bar_padding);
            mViewGroup.setLayoutParams(params);
        }

        setUpSubFragments();
        setUpPlayerToolbar();
    }

    private void setUpPlayerToolbar() {
        toolbar.inflateMenu(R.menu.menu_player);
        toolbar.setNavigationOnClickListener(v -> getActivity().onBackPressed());
        toolbar.setOnMenuItemClickListener(this);

        ToolbarColorizeHelper.colorizeToolbar(toolbar, Color.WHITE, getActivity());
    }

    @Override
    @ColorInt
    public int getPaletteColor() {
        return lastColor;
    }


    @Override
    public void onShow() {
        mPlaybackControlsFragment.show();
    }

    @Override
    public void onHide() {
        mPlaybackControlsFragment.hide();
        onBackPressed();
    }

    @Override
    public boolean onBackPressed() {
        return false;
    }

    @Override
    public void onColorChanged(int color) {
        mPlaybackControlsFragment.setDark(color);
        getCallbacks().onPaletteColorChanged();
        lastColor = color;
        ToolbarColorizeHelper.colorizeToolbar(toolbar, Color.WHITE, getActivity());

    }

    @Override
    protected void toggleFavorite(Song song) {
        super.toggleFavorite(song);
        if (song.id == MusicPlayerRemote.getCurrentSong().id) {
            updateIsFavorite();
        }
    }

    @Override
    public void onFavoriteToggled() {
        toggleFavorite(MusicPlayerRemote.getCurrentSong());
    }


    @Override
    public void onToolbarToggled() {

    }

    @Override
    public void onServiceConnected() {
        //updateQueue();
        //updateCurrentSong();
        updateIsFavorite();
        updateLyrics();
    }

    @Override
    public void onPlayingMetaChanged() {
        //updateCurrentSong();
        //updateQueuePosition();
        updateIsFavorite();
        updateLyrics();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (updateLyricsAsyncTask != null && !updateLyricsAsyncTask.isCancelled()) {
            updateLyricsAsyncTask.cancel(true);
        }
        if (updateIsFavoriteTask != null && !updateIsFavoriteTask.isCancelled()) {
            updateIsFavoriteTask.cancel(true);
        }
    }

    @SuppressLint("StaticFieldLeak")
    private void updateLyrics() {
        if (updateLyricsAsyncTask != null) updateLyricsAsyncTask.cancel(false);
        final Song song = MusicPlayerRemote.getCurrentSong();
        updateLyricsAsyncTask = new AsyncTask<Void, Void, Boolean>() {
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                toolbar.getMenu().removeItem(R.id.action_show_lyrics);
            }

            @Override
            protected Boolean doInBackground(Void... params) {
                return LyricUtil.isLrcFileExist(song.title, song.artistName);

            }

            @Override
            protected void onPostExecute(Boolean l) {
                if (l) {
                    Activity activity = getActivity();
                    if (toolbar != null && activity != null)
                        if (toolbar.getMenu().findItem(R.id.action_show_lyrics) == null) {
                            int color = Color.WHITE;
                            Drawable drawable = Util.getTintedVectorDrawable(activity, R.drawable.ic_comment_text_outline_white_24dp, color);
                            toolbar.getMenu()
                                    .add(Menu.NONE, R.id.action_show_lyrics, Menu.NONE, R.string.action_show_lyrics)
                                    .setIcon(drawable)
                                    .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
                        }
                } else {
                    if (toolbar != null) {
                        toolbar.getMenu().removeItem(R.id.action_show_lyrics);
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
                    int color = Color.WHITE;
                    Drawable drawable = Util.getTintedVectorDrawable(activity, res, color);
                    toolbar.getMenu().findItem(R.id.action_toggle_favorite)
                            .setIcon(drawable)
                            .setTitle(isFavorite ? getString(R.string.action_remove_from_favorites) : getString(R.string.action_add_to_favorites));
                }
            }
        }.execute(MusicPlayerRemote.getCurrentSong());
    }
}
