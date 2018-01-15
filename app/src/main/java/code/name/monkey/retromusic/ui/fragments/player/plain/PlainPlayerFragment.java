package code.name.monkey.retromusic.ui.fragments.player.plain;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.kabouzeid.appthemehelper.util.ATHUtil;
import com.retro.musicplayer.backend.model.Song;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import code.name.monkey.retromusic.R;
import code.name.monkey.retromusic.helper.MusicPlayerRemote;
import code.name.monkey.retromusic.ui.fragments.base.AbsPlayerFragment;
import code.name.monkey.retromusic.ui.fragments.player.PlayerAlbumCoverFragment;
import code.name.monkey.retromusic.util.PreferenceUtil;

/**
 * @author Hemanth S (h4h13).
 */

public class PlainPlayerFragment extends AbsPlayerFragment implements PlayerAlbumCoverFragment.Callbacks {
    @BindView(R.id.title)
    TextView mTitle;
    @BindView(R.id.text)
    TextView mText;
    @BindView(R.id.player_toolbar)
    Toolbar mToolbar;
    @BindView(R.id.toolbar_container)
    FrameLayout toolbarContainer;
    private Unbinder unbinder;
    private PlainPlaybackControlsFragment mPlainPlaybackControlsFragment;
    private int mLastColor;

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

    @Override
    public void onPlayingMetaChanged() {
        super.onPlayingMetaChanged();
        updateSong();
    }

    private void updateSong() {
        Song song = MusicPlayerRemote.getCurrentSong();
        mTitle.setText(song.title);
        mText.setText(song.artistName);
    }

    @Override
    public void onServiceConnected() {
        super.onServiceConnected();
        updateSong();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_plain_player, container, false);
        unbinder = ButterKnife.bind(this, view);
        /*Hide status bar view for !full screen mode*/
        if (PreferenceUtil.getInstance(getContext()).getFullScreenMode() < 1) {
            view.findViewById(R.id.status_bar).setVisibility(View.GONE);
        }
        return view;
    }

    private void setUpPlayerToolbar() {
        mToolbar.inflateMenu(R.menu.menu_player);
        mToolbar.setNavigationOnClickListener(v -> getActivity().onBackPressed());
        mToolbar.setOnMenuItemClickListener(this);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setUpSubFragments();
        setUpPlayerToolbar();
    }

    private void setUpSubFragments() {
        mPlainPlaybackControlsFragment = (PlainPlaybackControlsFragment) getChildFragmentManager().findFragmentById(R.id.playback_controls_fragment);
        PlayerAlbumCoverFragment playerAlbumCoverFragment = (PlayerAlbumCoverFragment) getChildFragmentManager().findFragmentById(R.id.player_album_cover_fragment);
        playerAlbumCoverFragment.setCallbacks(this);
    }

    @Override
    public int getPaletteColor() {
        return mLastColor;
    }

    @Override
    public void onShow() {
        mPlainPlaybackControlsFragment.show();
    }

    @Override
    public void onHide() {
        mPlainPlaybackControlsFragment.hide();
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
        getCallbacks().onPaletteColorChanged();
        mLastColor = color;
        mPlainPlaybackControlsFragment.setDark(color);

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
}
