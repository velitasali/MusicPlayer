package code.name.monkey.retromusic.ui.fragments.player.full;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import code.name.monkey.appthemehelper.util.ToolbarContentTintHelper;
import code.name.monkey.backend.model.Song;
import code.name.monkey.retromusic.R;
import code.name.monkey.retromusic.helper.MusicPlayerRemote;
import code.name.monkey.retromusic.ui.fragments.base.AbsPlayerFragment;
import code.name.monkey.retromusic.ui.fragments.player.PlayerAlbumCoverFragment;

/**
 * Created by hemanths on 14/09/17.
 */

public class FullPlayerFragment extends AbsPlayerFragment implements
        PlayerAlbumCoverFragment.Callbacks {
    @BindView(R.id.player_toolbar)
    Toolbar mToolbar;
    @BindView(R.id.toolbar_container)
    FrameLayout toolbarContainer;
    Unbinder unbinder;
    @BindView(R.id.now_playing_container)
    ViewGroup viewGroup;
    private int lastColor;
    private FullPlaybackControlsFragment fullPlaybackControlsFragment;

    private void setUpPlayerToolbar() {
        mToolbar.inflateMenu(R.menu.menu_player);
        mToolbar.setNavigationIcon(R.drawable.ic_close_white_24dp);
        mToolbar.setNavigationOnClickListener(v -> getActivity().onBackPressed());
        mToolbar.setOnMenuItemClickListener(this);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_full, container, false);
        unbinder = ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        toggleStatusBar(viewGroup);
        setUpSubFragments();
        setUpPlayerToolbar();
    }

    private void setUpSubFragments() {
        fullPlaybackControlsFragment = (FullPlaybackControlsFragment) getChildFragmentManager().findFragmentById(R.id.playback_controls_fragment);

        PlayerAlbumCoverFragment playerAlbumCoverFragment = (PlayerAlbumCoverFragment) getChildFragmentManager().findFragmentById(R.id.player_album_cover_fragment);
        playerAlbumCoverFragment.setCallbacks(this);
        playerAlbumCoverFragment.removeSlideEffect();
    }

    @Override
    @ColorInt
    public int getPaletteColor() {
        return lastColor;
    }

    @Override
    public void onShow() {

    }

    @Override
    public void onHide() {

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
        return Color.WHITE;
    }

    @Override
    public void onColorChanged(int color) {
        lastColor = color;
        fullPlaybackControlsFragment.setDark(color);
        getCallbacks().onPaletteColorChanged();
        ToolbarContentTintHelper.colorizeToolbar(mToolbar, Color.WHITE, getActivity());
    }

    @Override
    public void onFavoriteToggled() {
        toggleFavorite(MusicPlayerRemote.getCurrentSong());
    }

    @Override
    protected void toggleFavorite(Song song) {
        super.toggleFavorite(song);
        if (song.id == MusicPlayerRemote.getCurrentSong().id) {
            updateIsFavorite();
        }
    }

    @Override
    public void onToolbarToggled() {
        //Toggle hiding toolbar for effect
        //toggleToolbar(toolbarContainer);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }
}
