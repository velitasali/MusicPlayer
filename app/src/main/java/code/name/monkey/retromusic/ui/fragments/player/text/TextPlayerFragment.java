package code.name.monkey.retromusic.ui.fragments.player.text;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

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
import code.name.monkey.retromusic.util.ToolbarColorizeHelper;

/**
 * @author Hemanth S (h4h13).
 */

public class TextPlayerFragment extends AbsPlayerFragment implements PlayerAlbumCoverFragment.Callbacks {
    @BindView(R.id.player_toolbar)
    Toolbar mToolbar;
    @BindView(R.id.album_cover_container)
    ViewGroup mViewGroup;
    private Unbinder unbinder;
    private int lastColor;
    private TextPlaybackControlsFragment mPlaybackControlsFragment;

    private void setUpSubFragments() {
        mPlaybackControlsFragment = (TextPlaybackControlsFragment) getChildFragmentManager().findFragmentById(R.id.playback_controls_fragment);

        PlayerAlbumCoverFragment playerAlbumCoverFragment = (PlayerAlbumCoverFragment) getChildFragmentManager().findFragmentById(R.id.player_album_cover_fragment);
        playerAlbumCoverFragment.setCallbacks(this);
    }

    private void setUpPlayerToolbar() {
        mToolbar.inflateMenu(R.menu.menu_player);
        mToolbar.setNavigationOnClickListener(v -> getActivity().onBackPressed());
        mToolbar.setOnMenuItemClickListener(this);

        ToolbarColorizeHelper.colorizeToolbar(mToolbar,
                ATHUtil.resolveColor(getContext(), R.attr.iconColor),
                getActivity());
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.fragment_text_player, container, false);
        unbinder = ButterKnife.bind(this, layout);
         /*Adding margin to toolbar for !full screen mode*/
        if (PreferenceUtil.getInstance(getContext()).getFullScreenMode() < 1) {
            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) mViewGroup.getLayoutParams();
            params.topMargin = getResources().getDimensionPixelOffset(R.dimen.status_bar_padding);
            mViewGroup.setLayoutParams(params);
        }
        return layout;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setUpSubFragments();
        setUpPlayerToolbar();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    @Override
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
        return ATHUtil.resolveColor(getActivity(), R.attr.iconColor);
    }

    @Override
    public void onColorChanged(int color) {
        mPlaybackControlsFragment.setDark(color);
        lastColor = color;
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
