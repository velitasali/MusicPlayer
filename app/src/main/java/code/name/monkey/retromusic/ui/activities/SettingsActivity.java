package code.name.monkey.retromusic.ui.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.TabLayout;
import android.support.transition.TransitionManager;
import android.support.v4.app.DialogFragment;
import android.support.v4.view.ViewPager;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.preference.TwoStatePreference;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.afollestad.materialdialogs.color.ColorChooserDialog;

import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import code.name.monkey.appthemehelper.ThemeStore;
import code.name.monkey.appthemehelper.common.prefs.supportv7.ATEColorPreference;
import code.name.monkey.appthemehelper.common.prefs.supportv7.ATEPreferenceFragmentCompat;
import code.name.monkey.appthemehelper.util.ATHUtil;
import code.name.monkey.appthemehelper.util.ColorUtil;
import code.name.monkey.appthemehelper.util.TabLayoutUtil;
import code.name.monkey.appthemehelper.util.ToolbarContentTintHelper;
import code.name.monkey.retromusic.R;
import code.name.monkey.retromusic.appshortcuts.DynamicShortcutManager;
import code.name.monkey.retromusic.helper.MusicPlayerRemote;
import code.name.monkey.retromusic.preferences.BlacklistPreference;
import code.name.monkey.retromusic.preferences.BlacklistPreferenceDialog;
import code.name.monkey.retromusic.preferences.LibraryPreference;
import code.name.monkey.retromusic.preferences.LibraryPreferenceDialog;
import code.name.monkey.retromusic.preferences.NowPlayingScreenPreference;
import code.name.monkey.retromusic.preferences.NowPlayingScreenPreferenceDialog;
import code.name.monkey.retromusic.service.MusicService;
import code.name.monkey.retromusic.ui.activities.base.AbsBaseActivity;
import code.name.monkey.retromusic.ui.adapter.SettingsPagerAdapter;
import code.name.monkey.retromusic.util.NavigationUtil;
import code.name.monkey.retromusic.util.PreferenceUtil;

import static code.name.monkey.backend.RetroConstants.TELEGRAM_CHANGE_LOG;

public class SettingsActivity extends AbsBaseActivity implements ColorChooserDialog.ColorCallback {
    public static final int REQUEST_CODE_OPEN_DIRECTORY = 1;
    @BindView(R.id.toolbar)
    Toolbar mToolbar;
    @BindView(R.id.app_bar)
    AppBarLayout mAppBarLayout;
    @BindView(R.id.tabs)
    TabLayout mTabLayout;
    @BindView(R.id.pager)
    ViewPager mViewPager;

    @Override
    public void onColorSelection(@NonNull ColorChooserDialog dialog, @ColorInt int selectedColor) {
        switch (dialog.getTitle()) {
            case R.string.primary_color:
                ThemeStore.editTheme(this).primaryColor(selectedColor).commit();
                break;
            case R.string.accent_color:
                ThemeStore.editTheme(this).accentColor(selectedColor).commit();
                break;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
            new DynamicShortcutManager(this).updateDynamicShortcuts();
        }
        recreate();
    }

    @Override
    public void onColorChooserDismissed(@NonNull ColorChooserDialog dialog) {

    }


    @Override
    protected void onCreate(@Nullable Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.activity_settings);
        ButterKnife.bind(this);

        setStatusbarColorAuto();
        setNavigationbarColorAuto();
        setTaskDescriptionColorAuto();

        setupToolbar();

        SettingsPagerAdapter settingsPagerAdapter = new SettingsPagerAdapter(this, getSupportFragmentManager());
        mViewPager.setAdapter(settingsPagerAdapter);
        mTabLayout.setupWithViewPager(mViewPager);

        int primaryColor = ThemeStore.primaryColor(this);
        int normalColor = ToolbarContentTintHelper.toolbarSubtitleColor(this, primaryColor);
        int selectedColor = ToolbarContentTintHelper.toolbarTitleColor(this, primaryColor);
        TabLayoutUtil.setTabIconColors(mTabLayout, normalColor, selectedColor);
        mTabLayout.setTabTextColors(normalColor, selectedColor);
        mTabLayout.setSelectedTabIndicatorColor(ThemeStore.accentColor(this));

        setResult(RESULT_CANCELED);

    }

    private void setupToolbar() {
        mAppBarLayout.setBackgroundColor(ThemeStore.primaryColor(this));
        mToolbar.setBackgroundColor(ThemeStore.primaryColor(this));
        setTitle(R.string.action_settings);
        setSupportActionBar(mToolbar);
        mToolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    @Override
    public void onBackPressed() {
        finish();
        super.onBackPressed();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void addAppbarLayoutElevation(float v) {
        TransitionManager.beginDelayedTransition(mAppBarLayout);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mAppBarLayout.setElevation(v);
        }
    }

    public static class AdvancedSettingsFragment extends ATEPreferenceFragmentCompat {


        private static void setSummary(@NonNull Preference preference) {
            setSummary(preference, PreferenceManager
                    .getDefaultSharedPreferences(preference.getContext())
                    .getString(preference.getKey(), ""));
        }

        private static void setSummary(Preference preference, @NonNull Object value) {
            String stringValue = value.toString();
            if (preference instanceof ListPreference) {
                ListPreference listPreference = (ListPreference) preference;
                int index = listPreference.findIndexOfValue(stringValue);
                preference.setSummary(index >= 0 ? listPreference.getEntries()[index] : null);
            } else {
                preference.setSummary(stringValue);
            }
        }

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            addPreferencesFromResource(R.xml.pref_advanced);
            addPreferencesFromResource(R.xml.pref_others);
        }

        @Nullable
        @Override
        public DialogFragment onCreatePreferenceDialog(Preference preference) {

            return super.onCreatePreferenceDialog(preference);
        }

        private void openUrl(String url) {
            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setData(Uri.parse(url));
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(i);
        }

        @Override
        public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);
            getListView().setPadding(0, 0, 0, 0);
            getListView().addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);
                    if (getActivity() != null) {
                        ((SettingsActivity) getActivity()).addAppbarLayoutElevation(recyclerView.canScrollVertically(RecyclerView.NO_POSITION) ? 8f : 0f);
                    }
                }
            });

            getListView().setBackgroundColor(ATHUtil.resolveColor(getContext(), R.attr.colorPrimary));

            invalidateSettings();
        }

        public void setLangRecreate(String langval) {
            Locale locale = new Locale(langval);
            Locale.setDefault(locale);
            Configuration configuration = new Configuration();
            configuration.setLocale(locale);

            getActivity().getBaseContext().getResources().updateConfiguration(configuration,
                    getActivity().getBaseContext().getResources().getDisplayMetrics());

            PreferenceManager.getDefaultSharedPreferences(getContext()).edit().putString("LANG", langval).apply();
            getActivity().recreate();
        }

        private void invalidateSettings() {

            Preference findPreference = findPreference("changelog");
            findPreference.setOnPreferenceClickListener(preference -> {
                openUrl(TELEGRAM_CHANGE_LOG);
                return true;
            });

            findPreference = findPreference("external_storage_access");
            findPreference.setVisible(false);
            findPreference.setOnPreferenceClickListener(preference -> {
                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
                intent.addFlags(0);
                startActivityForResult(intent, REQUEST_CODE_OPEN_DIRECTORY);
                return true;
            });

            findPreference = findPreference("day_dream");
            findPreference.setOnPreferenceClickListener(preference -> {
                Intent intent = new Intent(Settings.ACTION_DREAM_SETTINGS);

                return true;
            });

            findPreference = findPreference("user_info");
            findPreference.setOnPreferenceClickListener(preference -> {
                startActivity(new Intent(getContext(), UserInfoActivity.class));
                return true;
            });
            findPreference = findPreference("open_source");
            findPreference.setOnPreferenceClickListener(preference -> {
                startActivity(new Intent(getActivity(), LicenseActivity.class));
                return true;
            });
            findPreference = findPreference("about");
            findPreference.setOnPreferenceClickListener(preference -> {
                startActivity(new Intent(getContext(), AboutActivity.class));
                return true;
            });
            findPreference = findPreference("app_version");
            try {
                PackageInfo packageInfo = getActivity().getPackageManager().getPackageInfo(getActivity().getPackageName(), 0);
                findPreference.setSummary(packageInfo.versionName);
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }


            TwoStatePreference toggleLanguage = (TwoStatePreference) findPreference("language_en");
            toggleLanguage.setVisible(false);
            toggleLanguage.setOnPreferenceChangeListener((preference, o) -> {

                String languageToLoad;
                if ((Boolean) o) {
                    languageToLoad = "en";
                    Toast.makeText(getContext(), "English is set", Toast.LENGTH_SHORT).show();
                } else {
                    languageToLoad = Locale.getDefault().getDisplayLanguage();
                    Toast.makeText(getContext(), "Local language is set", Toast.LENGTH_SHORT).show();
                }
                setLangRecreate(languageToLoad);
                return true;
            });


        }

    }

    public static class SettingsFragment extends ATEPreferenceFragmentCompat implements
            SharedPreferences.OnSharedPreferenceChangeListener {
        private static void setSummary(@NonNull Preference preference) {
            setSummary(preference, PreferenceManager
                    .getDefaultSharedPreferences(preference.getContext())
                    .getString(preference.getKey(), ""));
        }

        private static void setSummary(Preference preference, @NonNull Object value) {
            String stringValue = value.toString();
            if (preference instanceof ListPreference) {
                ListPreference listPreference = (ListPreference) preference;
                int index = listPreference.findIndexOfValue(stringValue);
                preference.setSummary(index >= 0 ? listPreference.getEntries()[index] : null);
            } else {
                preference.setSummary(stringValue);
            }
        }

        @Nullable
        @Override
        public DialogFragment onCreatePreferenceDialog(Preference preference) {
            if (preference instanceof NowPlayingScreenPreference) {
                return NowPlayingScreenPreferenceDialog.newInstance();
            } else if (preference instanceof BlacklistPreference) {
                return BlacklistPreferenceDialog.newInstance();
            } else if (preference instanceof LibraryPreference) {
                return LibraryPreferenceDialog.newInstance();
            }
            return super.onCreatePreferenceDialog(preference);
        }

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            addPreferencesFromResource(R.xml.pref_general);
            addPreferencesFromResource(R.xml.pref_audio);
            addPreferencesFromResource(R.xml.pref_ui);
            addPreferencesFromResource(R.xml.pref_images);
            addPreferencesFromResource(R.xml.pref_lockscreen);
            addPreferencesFromResource(R.xml.pref_now_playing_screen);
            addPreferencesFromResource(R.xml.pref_notification);
            addPreferencesFromResource(R.xml.pref_playlists);
            addPreferencesFromResource(R.xml.pref_window);
            addPreferencesFromResource(R.xml.pref_blacklist);
        }

        @Override
        public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);
            getListView().setPadding(0, 0, 0, 0);
            getListView().addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);
                    if (getActivity() != null) {
                        ((SettingsActivity) getActivity()).addAppbarLayoutElevation(recyclerView.canScrollVertically(RecyclerView.NO_POSITION) ? 8f : 0f);
                    }
                }
            });
            invalidateSettings();
            PreferenceUtil.getInstance(getActivity()).registerOnSharedPreferenceChangedListener(this);
        }

        private boolean hasEqualizer() {
            return getActivity().getPackageManager().resolveActivity(new Intent("android.media.action.DISPLAY_AUDIO_EFFECT_CONTROL_PANEL"), 0) != null;
        }

        private void invalidateSettings() {
            final Preference lyricsOptions = findPreference("lyrics_options");
            setSummary(lyricsOptions);
            lyricsOptions.setOnPreferenceChangeListener((preference, newValue) -> {
                setSummary(lyricsOptions, newValue);
                return true;
            });

            final ATEColorPreference primaryColorPref = (ATEColorPreference) findPreference("primary_color");
            primaryColorPref.setVisible(PreferenceUtil.getInstance(getActivity()).getGeneralTheme() == R.style.Theme_RetroMusic_Color);
            final int primaryColor = ThemeStore.primaryColor(getActivity());
            primaryColorPref.setColor(primaryColor, ColorUtil.darkenColor(primaryColor));
            primaryColorPref.setOnPreferenceClickListener(preference -> {
                new ColorChooserDialog.Builder(((SettingsActivity) getActivity()), R.string.primary_color)
                        .accentMode(false)
                        .allowUserColorInput(true)
                        .allowUserColorInputAlpha(false)
                        .preselect(primaryColor)
                        .show();
                return true;
            });

            final Preference generalTheme = findPreference("general_theme");
            setSummary(generalTheme);
            generalTheme.setOnPreferenceChangeListener((preference, newValue) -> {
                setSummary(generalTheme, newValue);
                String theme = (String) newValue;
                ThemeStore.editTheme(getActivity())
                        .activityTheme(PreferenceUtil.getThemeResFromPrefValue(theme))
                        .commit();

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
                    getActivity().setTheme(PreferenceUtil.getThemeResFromPrefValue(theme));
                    new DynamicShortcutManager(getActivity()).updateDynamicShortcuts();
                }
                getActivity().recreate();
                return true;
            });


            ATEColorPreference accentColorPref = (ATEColorPreference) findPreference("accent_color");
            final int accentColor = ThemeStore.accentColor(getActivity());
            accentColorPref.setColor(accentColor, ColorUtil.darkenColor(accentColor));

            accentColorPref.setOnPreferenceClickListener(preference -> {
                new ColorChooserDialog.Builder(((SettingsActivity) getActivity()), R.string.accent_color)
                        .accentMode(true)
                        .allowUserColorInput(true)
                        .allowUserColorInputAlpha(false)
                        .preselect(accentColor)
                        .show();
                return true;
            });

            TwoStatePreference colorAppShortcuts = (TwoStatePreference) findPreference("should_color_app_shortcuts");
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N_MR1) {
                colorAppShortcuts.setVisible(false);
            } else {
                colorAppShortcuts.setChecked(PreferenceUtil.getInstance(getActivity()).coloredAppShortcuts());
                colorAppShortcuts.setOnPreferenceChangeListener((preference, newValue) -> {
                    // Save preference
                    PreferenceUtil.getInstance(getActivity()).setColoredAppShortcuts((Boolean) newValue);
                    // Update app shortcuts
                    new DynamicShortcutManager(getActivity()).updateDynamicShortcuts();

                    return true;
                });
            }

            TwoStatePreference cornerWindow = (TwoStatePreference) findPreference("corner_window");
            cornerWindow.setOnPreferenceChangeListener((preference, newValue) -> {
                getActivity().recreate();
                getActivity().setResult(RESULT_OK);
                return true;
            });

            final Preference autoDownloadImagesPolicy = findPreference("auto_download_images_policy");
            setSummary(autoDownloadImagesPolicy);
            autoDownloadImagesPolicy.setOnPreferenceChangeListener((preference, o) -> {
                setSummary(autoDownloadImagesPolicy, o);
                return true;
            });
            final TwoStatePreference classicNotification = (TwoStatePreference) findPreference("classic_notification");
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
                classicNotification.setVisible(false);
            } else {
                classicNotification.setChecked(PreferenceUtil.getInstance(getActivity()).classicNotification());
                classicNotification.setOnPreferenceChangeListener((preference, newValue) -> {
                    // Save preference
                    PreferenceUtil.getInstance(getActivity()).setClassicNotification((Boolean) newValue);

                    final MusicService service = MusicPlayerRemote.musicService;
                    if (service != null) {
                        service.initNotification();
                        service.updateNotification();
                    }

                    return true;
                });
            }

            TwoStatePreference twoSatePreference = (TwoStatePreference) findPreference("adaptive_color_app");
            twoSatePreference.setOnPreferenceChangeListener((preference, newValue) -> {
                //getActivity().recreate();
                getActivity().setResult(RESULT_OK);
                return true;
            });

            TwoStatePreference colorNavBar = (TwoStatePreference) findPreference("should_color_navigation_bar");
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                colorNavBar.setEnabled(false);
                colorNavBar.setSummary(R.string.pref_only_lollipop);
            } else {
                colorNavBar.setChecked(ThemeStore.coloredNavigationBar(getActivity()));
                colorNavBar.setOnPreferenceChangeListener((preference, newValue) -> {
                    ThemeStore.editTheme(getActivity())
                            .coloredNavigationBar((Boolean) newValue)
                            .commit();
                    getActivity().recreate();
                    return true;
                });
            }
            Preference findPreference = findPreference("equalizer");
            if (!hasEqualizer()) {
                findPreference.setEnabled(false);
                findPreference.setSummary(getResources().getString(R.string.no_equalizer));
            }
            findPreference.setOnPreferenceClickListener(preference -> {
                NavigationUtil.openEqualizer(SettingsFragment.this.getActivity());
                return true;
            });
            updateNowPlayingScreenSummary();
        }

        private void updateNowPlayingScreenSummary() {
            findPreference("now_playing_screen_id").setSummary(PreferenceUtil.getInstance(getActivity()).getNowPlayingScreen().titleRes);
        }

        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            switch (key) {
                case PreferenceUtil.NOW_PLAYING_SCREEN_ID:
                    updateNowPlayingScreenSummary();
                    break;
            }
        }
    }
}
