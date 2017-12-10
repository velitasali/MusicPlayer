package code.name.monkey.retromusic.ui.activities;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

import butterknife.ButterKnife;
import code.name.monkey.retromusic.R;
import code.name.monkey.retromusic.ui.activities.base.AbsBaseActivity;
import code.name.monkey.retromusic.ui.fragments.intro.NameFragment;

/**
 * Created by hemanths on 23/08/17.
 */

public class UserInfoActivity extends AbsBaseActivity {
    private static final String TAG = "UserInfoActivity";
    private FragmentManager fragmentManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setDrawUnderStatusbar(true);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_info);

        ButterKnife.bind(this);


        setStatusbarColorAuto();
        setNavigationbarColorAuto();
        setTaskDescriptionColorAuto();

        if (savedInstanceState == null) {
            setFragment(new NameFragment(), false);
        }
    }

    public void setFragment(Fragment fragment, boolean addToBackStack) {
        fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.fragment_container, fragment, TAG);
        if (addToBackStack) {
            transaction.addToBackStack(TAG);
        }
        transaction.commit();

    }

    @Override
    public void onBackPressed() {
        if (fragmentManager == null) {
            return;
        }
        if (fragmentManager.getBackStackEntryCount() > 0) {
            fragmentManager.popBackStack();
        } else {
            super.onBackPressed();
        }
    }

}
