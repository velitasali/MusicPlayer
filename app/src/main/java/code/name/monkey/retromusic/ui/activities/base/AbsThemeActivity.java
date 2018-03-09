package code.name.monkey.retromusic.ui.activities.base;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.ColorInt;
import android.support.v7.preference.PreferenceManager;
import android.view.KeyEvent;
import android.view.View;

import code.name.monkey.appthemehelper.ATH;
import code.name.monkey.appthemehelper.ThemeStore;
import code.name.monkey.appthemehelper.common.ATHToolbarActivity;
import code.name.monkey.appthemehelper.util.ATHUtil;
import code.name.monkey.appthemehelper.util.ColorUtil;
import code.name.monkey.appthemehelper.util.MaterialDialogsUtil;
import code.name.monkey.backend.util.Util;
import com.velitasali.music.R;
import code.name.monkey.retromusic.util.PreferenceUtil;

public abstract class AbsThemeActivity extends ATHToolbarActivity implements Runnable {

    private Handler handler = new Handler();

    public void toggleFullscreenMode(boolean isFullscreen) {
        View decorView = getWindow().getDecorView();
        if (isFullscreen) {
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
                    View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION |
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
                    View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                    View.SYSTEM_UI_FLAG_FULLSCREEN |
                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            hideStatusBar();
            handler.removeCallbacks(this);
            handler.postDelayed(this, 300);
        } else {
            handler.removeCallbacks(this);
        }
    }

    public void hideStatusBar() {
        hideStatusBar(PreferenceUtil.getInstance(this).getFullScreenMode());
    }

    private void hideStatusBar(boolean fullscreen) {
        final View statusBar = getWindow().getDecorView().getRootView().findViewById(R.id.status_bar);
        if (statusBar != null) {
            statusBar.setVisibility(fullscreen ? View.GONE : View.VISIBLE);
        }
        //toggleFullscreenMode(fullscreen);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        hideStatusBar();
        // default theme
        if (!ThemeStore.isConfigured(this, 1)) {
            ThemeStore.editTheme(this)
                    .activityTheme(R.style.Theme_RetroMusic_Light)
                    .accentColorRes(R.color.md_green_A200)
                    .commit();
        }
        getSharedPreferences("[[kabouzeid_app-theme-helper]]", 0)
                .edit()
                .putInt("activity_theme", PreferenceUtil.getInstance(this).getGeneralTheme())
                .apply(); // TEMPORARY FIX

        super.onCreate(savedInstanceState);

        MaterialDialogsUtil.updateMaterialDialogsThemeSingleton(this);
        changeBackgroundShape();


        setImmersiveFullscreen();
        registerSystemUiVisibility();
    }

    private void changeBackgroundShape() {
        if (PreferenceManager.getDefaultSharedPreferences(this).getBoolean("corner_window", false)) {
            getWindow().setBackgroundDrawableResource(R.drawable.round_window);
        } else {
            getWindow().setBackgroundDrawableResource(R.drawable.square_window);
        }
        View decor = getWindow().getDecorView();
        GradientDrawable gradientDrawable = (GradientDrawable) decor.getBackground();
        gradientDrawable.setColor(ATHUtil.resolveColor(this, android.R.attr.windowBackground));
    }

    protected void setDrawUnderStatusbar(boolean drawUnderStatusbar) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            Util.setAllowDrawUnderStatusBar(getWindow());
        else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
            Util.setStatusBarTranslucent(getWindow());
    }

    /**
     * This will set the color of the view with the id "status_bar" on KitKat and Lollipop.
     * On Lollipop if no such view is found it will set the statusbar color using the native method.
     *
     * @param color the new statusbar color (will be shifted down on Lollipop and above)
     */
    public void setStatusbarColor(int color) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            final View statusBar = getWindow().getDecorView().getRootView().findViewById(R.id.status_bar);
            if (statusBar != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    statusBar.setBackgroundColor(ColorUtil.darkenColor(color));
                    setLightStatusbarAuto(color);
                } else {
                    statusBar.setBackgroundColor(color);
                }
            } else if (Build.VERSION.SDK_INT >= 21) {
                getWindow().setStatusBarColor(ColorUtil.darkenColor(color));
                setLightStatusbarAuto(color);
            }
        }
    }

    public void setStatusbarColorAuto() {
        // we don't want to use statusbar color because we are doing the color darkening on our own to support KitKat
        setStatusbarColor(ThemeStore.primaryColor(this));
    }

    public void setTaskDescriptionColor(@ColorInt int color) {
        ATH.setTaskDescriptionColor(this, color);
    }

    public void setTaskDescriptionColorAuto() {
        setTaskDescriptionColor(ThemeStore.primaryColor(this));
    }

    public void setNavigationbarColor(int color) {
        if (ThemeStore.coloredNavigationBar(this)) {
            ATH.setNavigationbarColor(this, color);
        } else {
            ATH.setNavigationbarColor(this, Color.BLACK);
        }
        if (Util.isOreo() && ColorUtil.isColorLight(ThemeStore.navigationBarColor(this))) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR);
        }
    }

    public void setNavigationbarColorAuto() {
        setNavigationbarColor(ThemeStore.navigationBarColor(this));
    }

    public void setLightStatusbar(boolean enabled) {
        ATH.setLightStatusbar(this, enabled);
    }

    public void setLightStatusbarAuto(int bgColor) {
        setLightStatusbar(ColorUtil.isColorLight(bgColor));
    }

    private void registerSystemUiVisibility() {
        final View decorView = getWindow().getDecorView();
        decorView.setOnSystemUiVisibilityChangeListener(new View.OnSystemUiVisibilityChangeListener() {

            @Override
            public void onSystemUiVisibilityChange(int visibility) {
                if ((visibility & View.SYSTEM_UI_FLAG_FULLSCREEN) == 0) {
                    setImmersiveFullscreen();
                }
            }
        });
    }

    private void unregisterSystemUiVisibility() {
        final View decorView = getWindow().getDecorView();
        decorView.setOnSystemUiVisibilityChangeListener(null);
    }

    public void setImmersiveFullscreen() {
        int flags = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
                View.SYSTEM_UI_FLAG_FULLSCREEN |
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
                View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION |
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        if (PreferenceUtil.getInstance(this).getFullScreenMode())
            getWindow().getDecorView().setSystemUiVisibility(flags);
    }

    public void exitFullscreen() {
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
    }

    @Override
    public void run() {
        setImmersiveFullscreen();
    }

    @Override
    protected void onStop() {
        handler.removeCallbacks(this);
        super.onStop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterSystemUiVisibility();
        exitFullscreen();
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_VOLUME_DOWN || keyCode == KeyEvent.KEYCODE_VOLUME_UP)) {
            handler.removeCallbacks(this);
            handler.postDelayed(this, 500);
        }
        return super.onKeyDown(keyCode, event);

    }
}