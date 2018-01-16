package code.name.monkey.retromusic.ui.fragments.player.normal;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.kabouzeid.appthemehelper.util.ATHUtil;
import com.retro.musicplayer.backend.DrawableGradient;
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
import code.name.monkey.retromusic.util.ViewUtil;

/**
 * Created by hemanths on 18/08/17.
 */

public class PlayerFragment extends AbsPlayerFragment implements PlayerAlbumCoverFragment.Callbacks {
    @BindView(R.id.player_toolbar)
    Toolbar mToolbar;
    @BindView(R.id.gradient_background)
    View colorBackground;
    @Nullable
    @BindView(R.id.toolbar_container)
    FrameLayout toolbarContainer;
    @BindView(R.id.now_playing_container)
    ViewGroup mViewGroup;
    private int lastColor;
    private PlayerPlaybackControlsFragment mPlaybackControlsFragment;
    private Unbinder unbinder;
    private ValueAnimator valueAnimator;

    public static PlayerFragment newInstance() {
        Bundle args = new Bundle();
        PlayerFragment fragment = new PlayerFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onToolbarToggled() {
        //Toggle hiding toolbar for effect
        //toggleToolbar(toolbarContainer);
    }

    private void colorize(int i) {
        if (valueAnimator != null) valueAnimator.cancel();

        valueAnimator = ValueAnimator.ofObject(new ArgbEvaluator(), android.R.color.transparent, i);
        valueAnimator.addUpdateListener(animation -> {
            GradientDrawable drawable = new DrawableGradient(
                    GradientDrawable.Orientation.TOP_BOTTOM,
                    new int[]{(int) animation.getAnimatedValue(), android.R.color.transparent},
                    0);
            if (colorBackground != null) {
                colorBackground.setBackground(drawable);
            }
        });
        valueAnimator.setDuration(ViewUtil.RETRO_MUSIC_ANIM_TIME).start();
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
    public void onResume() {
        super.onResume();
        checkToggleToolbar(toolbarContainer);
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
        mPlaybackControlsFragment.setDark(color);
        getCallbacks().onPaletteColorChanged();
        lastColor = color;
        if (PreferenceUtil.getInstance(getContext()).getAdaptiveColor()) colorize(color);
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
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_player, container, false);
        unbinder = ButterKnife.bind(this, view);
        return view;
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

    private void setUpSubFragments() {
        mPlaybackControlsFragment = (PlayerPlaybackControlsFragment) getChildFragmentManager().findFragmentById(R.id.playback_controls_fragment);

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

    @Override
    public void onServiceConnected() {
        updateIsFavorite();
        updateLyrics();
    }

    @Override
    public void onPlayingMetaChanged() {
        updateIsFavorite();
        updateLyrics();
    }

    @Override
    public void onQueueChanged() {
        //updateQueue();
    }

    @Override
    public void onMediaStoreChanged() {
        //updateQueue();
    }


}
