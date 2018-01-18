package code.name.monkey.retromusic.ui.fragments.player.solid;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.kabouzeid.appthemehelper.util.ATHUtil;
import com.kabouzeid.appthemehelper.util.ColorUtil;
import com.retro.musicplayer.backend.model.Song;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import code.name.monkey.retromusic.R;
import code.name.monkey.retromusic.glide.RetroMusicColoredTarget;
import code.name.monkey.retromusic.glide.SongGlideRequest;
import code.name.monkey.retromusic.glide.palette.BitmapPaletteWrapper;
import code.name.monkey.retromusic.helper.MusicPlayerRemote;
import code.name.monkey.retromusic.ui.fragments.base.AbsPlayerFragment;
import code.name.monkey.retromusic.ui.fragments.player.PlayerAlbumCoverFragment;
import code.name.monkey.retromusic.util.PreferenceUtil;
import code.name.monkey.retromusic.util.RetroMusicColorUtil;
import code.name.monkey.retromusic.util.ToolbarColorizeHelper;
import code.name.monkey.retromusic.util.ViewUtil;

/**
 * @author Hemanth S (h4h13).
 */

public class SolidPlayerFragment extends AbsPlayerFragment implements PlayerAlbumCoverFragment.Callbacks {

    @BindView(R.id.player_toolbar)
    Toolbar mToolbar;
    SolidPlaybackControlsFragment mPlaybackControlsFragment;
    @BindView(R.id.now_playing_container)
    ViewGroup mViewGroup;
    @BindView(R.id.mask)
    View mMask;
    @BindView(R.id.image)
    ImageView mImage;
    private Unbinder unbinder;
    private int lastColor;
    private ValueAnimator valueAnimator;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.fragment_solid_player, container, false);
        unbinder = ButterKnife.bind(this, layout);
        return layout;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
         /*Adding margin to toolbar for !full screen mode*/
        if (PreferenceUtil.getInstance(getContext()).getFullScreenMode()) {
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

    private void setUpSubFragments() {
        mPlaybackControlsFragment = (SolidPlaybackControlsFragment) getChildFragmentManager().findFragmentById(R.id.playback_controls_fragment);

        PlayerAlbumCoverFragment playerAlbumCoverFragment = (PlayerAlbumCoverFragment) getChildFragmentManager().findFragmentById(R.id.player_album_cover_fragment);
        playerAlbumCoverFragment.setCallbacks(this);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    @Override
    public void onToolbarToggled() {
        //Toggle hiding toolbar for effect
        //toggleToolbar(toolbarContainer);
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
        return lastColor;
    }

    @Override
    public void onColorChanged(int color) {
        mPlaybackControlsFragment.setDark(ColorUtil.darkenColor(color));
        getCallbacks().onPaletteColorChanged();
        lastColor = color;
        ToolbarColorizeHelper.colorizeToolbar(mToolbar, color, getActivity());
    }

    @Override
    public void onPlayingMetaChanged() {
        super.onPlayingMetaChanged();
        loadSongDetails();
    }

    @Override
    public void onServiceConnected() {
        super.onServiceConnected();
        loadSongDetails();
    }

    private void colorize(int i) {
        if (valueAnimator != null) valueAnimator.cancel();

        valueAnimator = ValueAnimator.ofObject(new ArgbEvaluator(), lastColor, i);
        valueAnimator.addUpdateListener(animation -> {
            if (mMask != null) {
                mMask.setBackgroundColor((int) animation.getAnimatedValue());
            }
        });
        valueAnimator.setDuration(ViewUtil.RETRO_MUSIC_ANIM_TIME).start();
    }

    private void loadSongDetails() {
        Activity activity = getActivity();
        SongGlideRequest.Builder.from(Glide.with(activity), MusicPlayerRemote.getCurrentSong())
                .checkIgnoreMediaStore(activity)
                .generatePalette(activity).build()
                .listener(new RequestListener<Object, BitmapPaletteWrapper>() {
                    @Override
                    public boolean onException(Exception e, Object model, Target<BitmapPaletteWrapper> target, boolean isFirstResource) {
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(BitmapPaletteWrapper resource, Object model, Target<BitmapPaletteWrapper> target, boolean isFromMemoryCache, boolean isFirstResource) {
                        int color = ATHUtil.resolveColor(getView().getContext(), R.attr.defaultFooterColor);
                        int color1 = RetroMusicColorUtil.getColor(resource.getPalette(), color);
                        onColorChanged(ColorUtil.darkenColor(color1));
                        return false;

                    }
                })
                .into(new RetroMusicColoredTarget(mImage) {
                    @Override
                    public void onLoadFailed(Exception e, Drawable errorDrawable) {
                        super.onLoadFailed(e, errorDrawable);
                        onColorReady(getDefaultFooterColor());
                    }

                    @Override
                    public void onColorReady(int color) {
                        colorize(color);
                    }
                });
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

}
