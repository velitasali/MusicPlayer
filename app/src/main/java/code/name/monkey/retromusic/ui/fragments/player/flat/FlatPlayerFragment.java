package code.name.monkey.retromusic.ui.fragments.player.flat;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import code.name.monkey.appthemehelper.util.ATHUtil;
import code.name.monkey.appthemehelper.util.ColorUtil;
import code.name.monkey.appthemehelper.util.MaterialValueHelper;
import code.name.monkey.backend.DrawableGradient;
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
import code.name.monkey.retromusic.util.ViewUtil;

/**
 * Created by hemanths on 02/09/17.
 */

public class FlatPlayerFragment extends AbsPlayerFragment implements PlayerAlbumCoverFragment.Callbacks {

    @BindView(R.id.player_toolbar)
    Toolbar mToolbar;
    @BindView(R.id.gradient_background)
    View colorBackground;
    @BindView(R.id.toolbar_container)
    FrameLayout toolbarContainer;

    private PlayerAlbumCoverFragment playerAlbumCoverFragment;
    private Unbinder unbinder;
    private ValueAnimator valueAnimator;
    private FlatPlaybackControlsFragment mFlatPlaybackControlsFragment;
    private int lastColor;

    private void setUpSubFragments() {
        mFlatPlaybackControlsFragment = (FlatPlaybackControlsFragment) getChildFragmentManager().findFragmentById(R.id.playback_controls_fragment);

        playerAlbumCoverFragment = (PlayerAlbumCoverFragment) getChildFragmentManager().findFragmentById(R.id.player_album_cover_fragment);
        playerAlbumCoverFragment.setCallbacks(this);
    }

    private void setUpPlayerToolbar() {
        mToolbar.inflateMenu(R.menu.menu_player);
        mToolbar.setNavigationOnClickListener(v -> getActivity().onBackPressed());
        mToolbar.setOnMenuItemClickListener(this);

        ToolbarColorizeHelper.colorizeToolbar(mToolbar, ATHUtil.resolveColor(getContext(), R.attr.iconColor), getActivity());
    }

    private void colorize(int i) {
        if (valueAnimator != null) valueAnimator.cancel();

        valueAnimator = ValueAnimator.ofObject(new ArgbEvaluator(), android.R.color.transparent, i);
        valueAnimator.addUpdateListener(animation -> {
            GradientDrawable drawable = new DrawableGradient(GradientDrawable.Orientation.TOP_BOTTOM, new int[]{(int) animation.getAnimatedValue(), android.R.color.transparent}, 0);
            if (colorBackground != null) {
                colorBackground.setBackground(drawable);
            }
        });
        valueAnimator.setDuration(ViewUtil.RETRO_MUSIC_ANIM_TIME).start();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_flat_player, container, false);
        unbinder = ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        /*Hide status bar view for !full screen mode*/
        if (PreferenceUtil.getInstance(getContext()).getFullScreenMode()) {
            view.findViewById(R.id.status_bar).setVisibility(View.GONE);
        }
        setUpPlayerToolbar();
        setUpSubFragments();

    }

    @Override
    public int getPaletteColor() {
        return lastColor;
    }

    @Override
    public void onShow() {
        mFlatPlaybackControlsFragment.show();
    }

    @Override
    public void onHide() {
        mFlatPlaybackControlsFragment.hide();
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
        boolean isLight = ColorUtil.isColorLight(lastColor);
        return PreferenceUtil.getInstance(getContext()).getAdaptiveColor() ? MaterialValueHelper.getPrimaryTextColor(getContext(), isLight) : ATHUtil.resolveColor(getContext(), R.attr.iconColor);
    }

    @Override
    public void onColorChanged(int color) {
        lastColor = color;
        mFlatPlaybackControlsFragment.setDark(color);
        getCallbacks().onPaletteColorChanged();


        boolean isLight = ColorUtil.isColorLight(color);

        //TransitionManager.beginDelayedTransition(mToolbar);
        int iconColor = PreferenceUtil.getInstance(getContext()).getAdaptiveColor() ? MaterialValueHelper.getPrimaryTextColor(getContext(), isLight) : ATHUtil.resolveColor(getContext(), R.attr.iconColor);
        ToolbarColorizeHelper.colorizeToolbar(mToolbar, iconColor, getActivity());
        if (PreferenceUtil.getInstance(getContext()).getAdaptiveColor()) colorize(color);
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

    @Override
    protected void toggleFavorite(Song song) {
        super.toggleFavorite(song);
        if (song.id == MusicPlayerRemote.getCurrentSong().id) {
            updateIsFavorite();
        }
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

}
