package code.name.monkey.retromusic.ui.fragments.player.holiday;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.retro.musicplayer.backend.model.Song;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import code.name.monkey.retromusic.R;
import code.name.monkey.retromusic.helper.MusicPlayerRemote;
import code.name.monkey.retromusic.ui.fragments.base.AbsPlayerFragment;
import code.name.monkey.retromusic.ui.fragments.player.PlayerAlbumCoverFragment;
import code.name.monkey.retromusic.util.PreferenceUtil;
import code.name.monkey.retromusic.util.ToolbarColorizeHelper;

/**
 * @author Hemanth S (h4h13).
 */

public class HolidayPlayerFragment extends AbsPlayerFragment implements PlayerAlbumCoverFragment.Callbacks {
    @BindView(R.id.player_toolbar)
    Toolbar mToolbar;
    @BindView(R.id.now_playing_container)
    ViewGroup mViewGroup;
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
       /*Adding margin to mToolbar for !full screen mode*/
        if (!PreferenceUtil.getInstance(getContext()).getFullScreenMode()) {
            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) mViewGroup.getLayoutParams();
            params.topMargin = getResources().getDimensionPixelOffset(R.dimen.status_bar_padding);
            mViewGroup.setLayoutParams(params);
        }

        setUpSubFragments();
        setUpPlayerToolbar();
    }

    private void setUpPlayerToolbar() {
        mToolbar.inflateMenu(R.menu.menu_player);
        mToolbar.setNavigationOnClickListener(v -> getActivity().onBackPressed());
        mToolbar.setOnMenuItemClickListener(this);

        ToolbarColorizeHelper.colorizeToolbar(mToolbar, Color.WHITE, getActivity());
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
    public Toolbar getToolbar() {
        return mToolbar;
    }

    @Override
    public int toolbarIconColor() {
        return 0;
    }

    @Override
    public void onColorChanged(int color) {
        mPlaybackControlsFragment.setDark(color);
        getCallbacks().onPaletteColorChanged();
        lastColor = color;
        ToolbarColorizeHelper.colorizeToolbar(mToolbar, Color.WHITE, getActivity());

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
}
