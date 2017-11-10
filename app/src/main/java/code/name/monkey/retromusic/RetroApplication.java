package code.name.monkey.retromusic;

import android.os.Build;
import android.support.multidex.MultiDexApplication;

import code.name.monkey.retromusic.appshortcuts.DynamicShortcutManager;
import code.name.monkey.retromusic.util.LanguageUtil;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;

/**
 * Created by hemanths on 11/08/17.
 */

public class RetroApplication extends MultiDexApplication {

    private static RetroApplication retroApplication;

    public static RetroApplication getInstance() {
        return retroApplication;

    }

    @Override
    public void onCreate() {
        super.onCreate();
        retroApplication = this;
        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath("fonts/sans_regular.ttf")
                .setFontAttrId(R.attr.fontPath)
                .build()
        );

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
            new DynamicShortcutManager(this).initDynamicShortcuts();
        }
        //LanguageUtil.setForceEnglish(this);
    }
}
