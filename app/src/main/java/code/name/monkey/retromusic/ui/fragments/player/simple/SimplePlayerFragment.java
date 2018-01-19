package code.name.monkey.retromusic.ui.fragments.player.simple;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import code.name.monkey.appthemehelper.util.ATHUtil;
import code.name.monkey.backend.model.Song;

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

public class SimplePlayerFragment extends AbsPlayerFragment implements PlayerAlbumCoverFragment.Callbacks {

    @BindView(R.id.gradient_background)
    View mView;
    @BindView(R.id.player_toolbar)
    Toolbar mToolbar;
    @BindView(R.id.toolbar_container)
    FrameLayout toolbarContainer;
    private Unbinder mUnbinder;
    private SimplePlaybackControlsFragment mSimplePlaybackControlsFragment;
    private int mLastColor;

    @Override
    public void onToolbarToggled() {
        //Toggle hiding toolbar for effect
        //toggleToolbar(toolbarContainer);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mUnbinder.unbind();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_simple_player, container, false);
        mUnbinder = ButterKnife.bind(this, view);
        /*Hide status bar view for !full screen mode*/
        if (PreferenceUtil.getInstance(getContext()).getFullScreenMode()) {
            view.findViewById(R.id.status_bar).setVisibility(View.GONE);
        }
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setUpSubFragments();
        setUpPlayerToolbar();
    }

    private void setUpSubFragments() {
        PlayerAlbumCoverFragment playerAlbumCoverFragment = (PlayerAlbumCoverFragment) getChildFragmentManager().findFragmentById(R.id.player_album_cover_fragment);
        playerAlbumCoverFragment.setCallbacks(this);
        mSimplePlaybackControlsFragment = (SimplePlaybackControlsFragment) getChildFragmentManager().findFragmentById(R.id.playback_controls_fragment);

    }

    @Override
    public int getPaletteColor() {
        return mLastColor;
    }

    @Override
    public void onShow() {
        mSimplePlaybackControlsFragment.show();
    }

    @Override
    public void onHide() {
        mSimplePlaybackControlsFragment.hide();
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
        return ATHUtil.resolveColor(getContext(), R.attr.iconColor);
    }

    @Override
    public void onColorChanged(int color) {
        mLastColor = color;
        getCallbacks().onPaletteColorChanged();
        mSimplePlaybackControlsFragment.setDark(color);
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

    private void setUpPlayerToolbar() {
        mToolbar.inflateMenu(R.menu.menu_player);
        mToolbar.setNavigationOnClickListener(v -> getActivity().onBackPressed());
        mToolbar.setOnMenuItemClickListener(this);

        ToolbarColorizeHelper.colorizeToolbar(mToolbar,
                ATHUtil.resolveColor(getContext(), R.attr.iconColor),
                getActivity());
    }
}
