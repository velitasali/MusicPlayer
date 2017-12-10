package code.name.monkey.retromusic.util;

import android.content.Context;
import android.content.res.Configuration;
import android.support.annotation.NonNull;

import java.util.Locale;

/**
 * @author Hemanth S (h4h13).
 */

public class LanguageUtil {
    public static void setForceEnglish(@NonNull Context context) {
        String languageToLoad = "de"; // your language
        Locale locale = new Locale(languageToLoad);
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.locale = locale;
        context.getResources().updateConfiguration(config, context.getResources().getDisplayMetrics());
    }
}
