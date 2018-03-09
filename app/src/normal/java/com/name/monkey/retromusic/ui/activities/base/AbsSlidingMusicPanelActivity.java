package com.name.monkey.retromusic.ui.activities.base;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.annotation.ColorInt;
import android.support.annotation.FloatRange;
import android.support.annotation.LayoutRes;
import android.support.design.widget.BottomNavigationView;
import android.support.transition.TransitionManager;
import android.support.v4.app.Fragment;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowInsets;
import android.view.animation.PathInterpolator;

import com.name.monkey.retromusic.ui.fragments.player.NowPlayingScreen;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;
import com.sothree.slidinguppanel.SlidingUpPanelLayout.PanelState;

import butterknife.BindView;
import butterknife.ButterKnife;
import code.name.monkey.appthemehelper.ThemeStore;
import code.name.monkey.appthemehelper.util.ColorUtil;
import code.name.monkey.appthemehelper.util.StatusBarUtil;
import com.velitasali.music.R;
import code.name.monkey.retromusic.helper.MusicPlayerRemote;
import code.name.monkey.retromusic.ui.activities.base.AbsMusicServiceActivity;
import code.name.monkey.retromusic.ui.fragments.MiniPlayerFragment;
import code.name.monkey.retromusic.ui.fragments.base.AbsPlayerFragment;
import code.name.monkey.retromusic.ui.fragments.player.blur.BlurPlayerFragment;
import code.name.monkey.retromusic.ui.fragments.player.color.ColorFragment;
import code.name.monkey.retromusic.ui.fragments.player.flat.FlatPlayerFragment;
import code.name.monkey.retromusic.ui.fragments.player.full.FullPlayerFragment;
import code.name.monkey.retromusic.ui.fragments.player.normal.PlayerFragment;
import code.name.monkey.retromusic.ui.fragments.player.plain.PlainPlayerFragment;
import code.name.monkey.retromusic.util.PreferenceUtil;
import code.name.monkey.retromusic.util.ViewUtil;
import code.name.monkey.retromusic.views.BottomNavigationViewEx;

public abstract class AbsSlidingMusicPanelActivity extends AbsMusicServiceActivity implements
        BottomNavigationView.OnNavigationItemSelectedListener,
        SlidingUpPanelLayout.PanelSlideListener,
        PlayerFragment.Callbacks {
    public static final String TAG = AbsSlidingMusicPanelActivity.class.getSimpleName();


    @BindView(R.id.bottom_navigation)
    BottomNavigationViewEx bottomNavigationView;

    @BindView(R.id.sliding_layout)
    SlidingUpPanelLayout slidingUpPanelLayout;

    private int navigationbarColor;
    private int taskColor;
    private boolean lightStatusbar;
    private NowPlayingScreen currentNowPlayingScreen;
    private AbsPlayerFragment playerFragment;
    private MiniPlayerFragment miniPlayerFragment;
    private ValueAnimator navigationBarColorAnimator;
    private ArgbEvaluator argbEvaluator = new ArgbEvaluator();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(createContentView());
        ButterKnife.bind(this);

        currentNowPlayingScreen = PreferenceUtil.getInstance(this).getNowPlayingScreen();
        Fragment fragment; // must implement AbsPlayerFragment
        switch (currentNowPlayingScreen) {
            case BLUR:
                fragment = new BlurPlayerFragment();
                break;
            case FLAT:
                fragment = new FlatPlayerFragment();
                break;
            case PLAIN:
                fragment = new PlainPlayerFragment();
                break;
            case FULL:
                fragment = new FullPlayerFragment();
                break;
            case COLOR:
                fragment = new ColorFragment();
                break;
            case NORMAL:
            default:
                fragment = new PlayerFragment();
                break;
        }
        getSupportFragmentManager().beginTransaction().replace(R.id.player_fragment_container, fragment).commit();
        getSupportFragmentManager().executePendingTransactions();

        playerFragment = (AbsPlayerFragment) getSupportFragmentManager().findFragmentById(R.id.player_fragment_container);
        miniPlayerFragment = (MiniPlayerFragment) getSupportFragmentManager().findFragmentById(R.id.mini_player_fragment);

        //noinspection ConstantConditions
        miniPlayerFragment.getView().setOnClickListener(v -> expandPanel());
        slidingUpPanelLayout.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                slidingUpPanelLayout.getViewTreeObserver().removeOnGlobalLayoutListener(this);

                if (getPanelState() == PanelState.EXPANDED) {
                    onPanelSlide(slidingUpPanelLayout, 1);
                    onPanelExpanded(slidingUpPanelLayout);
                } else if (getPanelState() == PanelState.COLLAPSED) {
                    onPanelCollapsed(slidingUpPanelLayout);
                } else {
                    playerFragment.onHide();
                }
            }
        });

        if (PreferenceUtil.getInstance(this).isGenreShown())
            bottomNavigationView.getMenu().removeItem(R.id.action_genre);

        setupBottomView();
        slidingUpPanelLayout.addPanelSlideListener(this);
    }

    private void setupBottomView() {
        bottomNavigationView.setOnNavigationItemSelectedListener(this);
        bottomNavigationView.enableAnimation(false);
        bottomNavigationView.enableItemShiftingMode(false);
        bottomNavigationView.enableShiftingMode(false);
        bottomNavigationView.setTextVisibility(PreferenceUtil.getInstance(this).tabTitles());
        //bottomNavigationView.setIconAndTextColor(PreferenceUtil.getInstance(this).isDominantColor() ? Color.WHITE : ThemeStore.accentColor(this));
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (currentNowPlayingScreen != PreferenceUtil.getInstance(this).getNowPlayingScreen()) {
            postRecreate();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (navigationBarColorAnimator != null) navigationBarColorAnimator.cancel(); // just in case
    }

    public void setAntiDragView(View antiDragView) {
        slidingUpPanelLayout.setAntiDragView(antiDragView);
    }

    protected abstract View createContentView();

    @Override
    public void onServiceConnected() {
        super.onServiceConnected();
        if (!MusicPlayerRemote.getPlayingQueue().isEmpty()) {
            slidingUpPanelLayout.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    slidingUpPanelLayout.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    hideBottomBar(false);
                }
            });
        }// don't call hideBottomBar(true) here as it causes a bug with the SlidingUpPanelLayout
    }

    @Override
    public void onQueueChanged() {
        super.onQueueChanged();
        hideBottomBar(MusicPlayerRemote.getPlayingQueue().isEmpty());
    }

    @Override
    public void onPlayingMetaChanged() {
        super.onPlayingMetaChanged();
    }

    @Override
    public void onPanelSlide(View panel, @FloatRange(from = 0, to = 1) float slideOffset) {
        bottomNavigationView.setTranslationY(slideOffset * 300);
        setMiniPlayerAlphaProgress(slideOffset);

        //if (navigationBarColorAnimator != null) navigationBarColorAnimator.cancel();
        //super.setNavigationbarColor((int) argbEvaluator.evaluate(slideOffset, navigationbarColor, playerFragment.getPaletteColor()));
    }

    @Override
    public void onPanelStateChanged(View panel, PanelState previousState, PanelState newState) {
        switch (newState) {
            case COLLAPSED:
                onPanelCollapsed(panel);
                break;
            case EXPANDED:
                onPanelExpanded(panel);
                break;
            case ANCHORED:
                collapsePanel(); // this fixes a bug where the panel would get stuck for some reason
                break;
        }
    }

    public void onPanelCollapsed(View panel) {
        // restore values
        super.setLightStatusbar(lightStatusbar);
        super.setTaskDescriptionColor(taskColor);
        super.setNavigationbarColor(navigationbarColor);

        playerFragment.setMenuVisibility(false);
        playerFragment.setUserVisibleHint(false);
        playerFragment.onHide();

    }

    public void onPanelExpanded(View panel) {
        // setting fragments values
        int playerFragmentColor = playerFragment.getPaletteColor();

        super.setLightStatusbar(false);
        super.setTaskDescriptionColor(playerFragmentColor);

        if (currentNowPlayingScreen == NowPlayingScreen.COLOR) {
            super.setNavigationbarColor(playerFragmentColor);
        } else {
            super.setNavigationbarColor(ThemeStore.primaryColor(this));
        }

        playerFragment.setMenuVisibility(true);
        playerFragment.setUserVisibleHint(true);
        playerFragment.onShow();
    }

    @Override
    public void onPaletteColorChanged() {
        int playerFragmentColor = playerFragment.getPaletteColor();

        if (getPanelState() == PanelState.EXPANDED) {
            super.setTaskDescriptionColor(playerFragmentColor);
            if (currentNowPlayingScreen == NowPlayingScreen.COLOR) {
                super.setNavigationbarColor(playerFragmentColor);
            }
        }

    }

    private void updateColor() {
        if (getPanelState() == PanelState.COLLAPSED) {
            //animateNavigationBarColor(playerFragment.getPaletteColor());
        }
    }

    private void setMiniPlayerAlphaProgress(@FloatRange(from = 0, to = 1) float progress) {
        if (miniPlayerFragment.getView() == null) return;
        float alpha = 1 - progress;
        miniPlayerFragment.getView().setAlpha(alpha);
        // necessary to make the views below clickable
        miniPlayerFragment.getView().setVisibility(alpha == 0 ? View.GONE : View.VISIBLE);
    }

    public SlidingUpPanelLayout.PanelState getPanelState() {
        return slidingUpPanelLayout == null ? null : slidingUpPanelLayout.getPanelState();
    }

    public void collapsePanel() {
        slidingUpPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
    }

    public void expandPanel() {
        slidingUpPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.EXPANDED);
    }

    public void hideBottomBar(final boolean hide) {
        if (hide) {
            slidingUpPanelLayout.setPanelHeight(0);
            collapsePanel();
        } else {
            if (!MusicPlayerRemote.getPlayingQueue().isEmpty())
                if (bottomNavigationView.getVisibility() == View.VISIBLE) {
                    slidingUpPanelLayout.setPanelHeight(getResources().getDimensionPixelSize(R.dimen.mini_player_height_expanded));
                } else {
                    slidingUpPanelLayout.setPanelHeight(getResources().getDimensionPixelSize(R.dimen.mini_player_height));
                }
        }
    }

    public void setBottomBarVisibility(int gone) {
        if (bottomNavigationView != null) {
            TransitionManager.beginDelayedTransition(bottomNavigationView);
            bottomNavigationView.setVisibility(gone);
            hideBottomBar(false);
        }
    }

    protected View wrapSlidingMusicPanel(@LayoutRes int resId) {
        @SuppressLint("InflateParams")
        View slidingMusicPanelLayout = getLayoutInflater().inflate(R.layout.sliding_music_panel_layout, null);
        ViewGroup contentContainer = slidingMusicPanelLayout.findViewById(R.id.content_container);
        getLayoutInflater().inflate(resId, contentContainer);
        return slidingMusicPanelLayout;
    }

    @Override
    public void onBackPressed() {
        if (!handleBackPress())
            super.onBackPressed();
    }

    public boolean handleBackPress() {
        if (slidingUpPanelLayout.getPanelHeight() != 0 && playerFragment.onBackPressed())
            return true;
        if (getPanelState() == SlidingUpPanelLayout.PanelState.EXPANDED) {
            collapsePanel();
            return true;
        }
        return false;
    }


    private void animateNavigationBarColor(int color) {
        if (navigationBarColorAnimator != null) navigationBarColorAnimator.cancel();
        navigationBarColorAnimator = ValueAnimator.ofArgb(getWindow().getNavigationBarColor(), color)
                .setDuration(ViewUtil.RETRO_MUSIC_ANIM_TIME);
        navigationBarColorAnimator.setInterpolator(new PathInterpolator(0.4f, 0f, 1f, 1f));
        navigationBarColorAnimator.addUpdateListener(animation -> {
            int playerFragmentColorDark = ColorUtil.darkenColor((Integer) animation.getAnimatedValue());

            bottomNavigationView.setBackgroundColor(playerFragmentColorDark);
            miniPlayerFragment.setColor(playerFragmentColorDark);
            AbsSlidingMusicPanelActivity.super.setNavigationbarColor(playerFragmentColorDark);

            View view = getWindow().getDecorView();
            view.setBackgroundColor(playerFragmentColorDark);
            view.findViewById(R.id.toolbar).setBackgroundColor(playerFragmentColorDark);
            view.findViewById(R.id.appbar).setBackgroundColor(playerFragmentColorDark);
            if (view.findViewById(R.id.status_bar) != null) {
                view.findViewById(R.id.status_bar).setBackgroundColor(ColorUtil.darkenColor(playerFragmentColorDark));
            }
        });
        navigationBarColorAnimator.start();
    }


    @Override
    public void setLightStatusbar(boolean enabled) {
        lightStatusbar = enabled;
        if (getPanelState() == SlidingUpPanelLayout.PanelState.COLLAPSED) {
            super.setLightStatusbar(enabled);
        }
    }


    @Override
    public void setTaskDescriptionColor(@ColorInt int color) {
        taskColor = color;
        if (getPanelState() == null || getPanelState() == SlidingUpPanelLayout.PanelState.COLLAPSED) {
            super.setTaskDescriptionColor(color);
        }
    }

    @Override
    public void setNavigationbarColor(int color) {
        navigationbarColor = color;
        if (getPanelState() == SlidingUpPanelLayout.PanelState.COLLAPSED) {
            if (navigationBarColorAnimator != null) navigationBarColorAnimator.cancel();
            super.setNavigationbarColor(color);
        }
    }

    @Override
    protected View getSnackBarContainer() {
        return findViewById(R.id.content_container);
    }

    public SlidingUpPanelLayout getSlidingUpPanelLayout() {
        return slidingUpPanelLayout;
    }

    public MiniPlayerFragment getMiniPlayerFragment() {
        return miniPlayerFragment;
    }

    public AbsPlayerFragment getPlayerFragment() {
        return playerFragment;
    }

    public BottomNavigationView getBottomNavigationView() {
        return bottomNavigationView;
    }
}
