package code.name.monkey.backend.interfaces;

import android.support.v4.app.Fragment;

/**
 * Created by hemanths on 14/08/17.
 */

public interface MainActivityFragmentCallbacks {
    boolean handleBackPress();

    void selectedFragment(Fragment fragment);
}
