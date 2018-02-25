package code.name.monkey.retromusic.ui.fragments.player.gradient;

import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import code.name.monkey.appthemehelper.util.ATHUtil;
import code.name.monkey.appthemehelper.util.ColorUtil;
import code.name.monkey.appthemehelper.util.MaterialValueHelper;
import code.name.monkey.appthemehelper.util.ToolbarContentTintHelper;
import code.name.monkey.backend.DrawableGradient;
import code.name.monkey.backend.model.Song;
import code.name.monkey.retromusic.R;
import code.name.monkey.retromusic.glide.RetroMusicColoredTarget;
import code.name.monkey.retromusic.glide.SongGlideRequest;
import code.name.monkey.retromusic.glide.palette.BitmapPaletteWrapper;
import code.name.monkey.retromusic.helper.MusicPlayerRemote;
import code.name.monkey.retromusic.ui.activities.LyricsActivity;
import code.name.monkey.retromusic.ui.fragments.base.AbsPlayerFragment;
import code.name.monkey.retromusic.util.RetroColorUtil;

public class GradientColorFragment extends AbsPlayerFragment {
    @BindView(R.id.player_toolbar)
    Toolbar toolbar;
    @BindView(R.id.gradient_background)
    View colorBackground;
    @Nullable
    @BindView(R.id.toolbar_container)
    FrameLayout toolbarContainer;
    @BindView(R.id.now_playing_container)
    ViewGroup viewGroup;
    @BindView(R.id.image)
    ImageView imageView;
    private int lastColor;
    private int backgroundColor;
    private GradientColorPlaybackControlsFragment playbackControlsFragment;
    private Unbinder unbinder;
    private ValueAnimator valueAnimator;

    public static GradientColorFragment newInstance() {
        Bundle args = new Bundle();
        GradientColorFragment fragment = new GradientColorFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @OnClick(R.id.image)
    void startLyrics() {
        startActivity(new Intent(getContext(), LyricsActivity.class));
    }

    @Override
    public void onShow() {
        playbackControlsFragment.show();
    }

    @Override
    public void onHide() {
        playbackControlsFragment.hide();
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
    @ColorInt
    public int getPaletteColor() {
        return backgroundColor;
    }

    @Override
    public Toolbar getToolbar() {
        return toolbar;
    }

    @Override
    public int toolbarIconColor() {
        return lastColor;
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
        if (valueAnimator != null) {
            valueAnimator.cancel();
            valueAnimator = null;
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_gradient_color_player, container, false);
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
        playbackControlsFragment = (GradientColorPlaybackControlsFragment) getChildFragmentManager().findFragmentById(R.id.playback_controls_fragment);

    }

    private void setUpPlayerToolbar() {
        toolbar.inflateMenu(R.menu.menu_player);
        toolbar.setNavigationOnClickListener(v -> getActivity().onBackPressed());
        toolbar.setOnMenuItemClickListener(this);

        ToolbarContentTintHelper.colorizeToolbar(toolbar, ATHUtil.resolveColor(getContext(), R.attr.iconColor), getActivity());
    }

    @Override
    public void onPlayingMetaChanged() {
        super.onPlayingMetaChanged();
        updateSong();
    }

    @Override
    public void onServiceConnected() {
        super.onServiceConnected();
        updateSong();
    }

    private void updateSong() {
        Activity activity = getActivity();

        SongGlideRequest.Builder.from(Glide.with(activity), MusicPlayerRemote.getCurrentSong())
                .checkIgnoreMediaStore(activity)
                .generatePalette(activity).build()
                .into(new RetroMusicColoredTarget(imageView) {
                    @Override
                    public void onLoadCleared(Drawable placeholder) {
                        super.onLoadCleared(placeholder);
                        //setColors(getDefaultFooterColor());
                    }

                    @Override
                    public void onColorReady(int color) {
                        //setColors(color);
                    }

                    @Override
                    public void onLoadFailed(Exception e, Drawable errorDrawable) {
                        super.onLoadFailed(e, errorDrawable);

                        int backgroundColor = getDefaultFooterColor();
                        int textColor = ColorUtil.isColorLight(getDefaultFooterColor()) ?
                                MaterialValueHelper.getPrimaryTextColor(getContext(), true) :
                                MaterialValueHelper.getPrimaryTextColor(getContext(), false);

                        setColors(backgroundColor, textColor);
                    }

                    @Override
                    public void onResourceReady(BitmapPaletteWrapper resource, GlideAnimation<? super BitmapPaletteWrapper> glideAnimation) {
                        super.onResourceReady(resource, glideAnimation);

                        int color1 = RetroColorUtil.getDominantColor(resource.getBitmap(), getDefaultFooterColor());
                        int color2 = RetroColorUtil.getTextColor(resource.getPalette());

                        Palette palette = resource.getPalette();
                        Palette.Swatch swatch = RetroColorUtil.getSwatch(palette);

                        int backgroundColor = RetroColorUtil.getTextColor(palette, swatch);
                        int textColor = swatch.getRgb();

                        setColors(color2, color1);

                    }
                });
    }

    private void colorize(int i, int i2) {
        GradientDrawable drawable = new DrawableGradient(GradientDrawable.Orientation.TL_BR,
                new int[]{i2, i}, 0);
        if (colorBackground != null) {
            colorBackground.setBackground(drawable);
        }
    }

    private void setColors(int color2, int color1) {
        playbackControlsFragment.setDark(color1);

        colorize(color2, color1);

        lastColor = ColorUtil.isColorLight(color1) ?
                MaterialValueHelper.getPrimaryTextColor(getContext(), true) :
                MaterialValueHelper.getPrimaryTextColor(getContext(), false);

        ToolbarContentTintHelper.colorizeToolbar(toolbar, lastColor, getActivity());


        this.backgroundColor = color1;

        getCallbacks().onPaletteColorChanged();
    }
}
