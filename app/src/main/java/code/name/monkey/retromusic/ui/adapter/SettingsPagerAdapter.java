package code.name.monkey.retromusic.ui.adapter;

import android.content.Context;
import android.graphics.Typeface;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import java.util.ArrayList;
import java.util.List;

import com.velitasali.music.R;
import code.name.monkey.retromusic.ui.activities.SettingsActivity;
import uk.co.chrisjenx.calligraphy.CalligraphyUtils;


/**
 * Created by hemanths on 15/06/17.
 */

public class SettingsPagerAdapter extends FragmentStatePagerAdapter {
    private Typeface mTypeface;
    private String[] tabs;// = new String[]{.getString(R.string.normal), "Experimental"};
    private List<Fragment> mFragments = new ArrayList<>();

    public SettingsPagerAdapter(Context context, FragmentManager fm) {
        super(fm);
        tabs = new String[]{context.getString(R.string.normal), context.getString(R.string.experimental)};
        mFragments.add(new SettingsActivity.SettingsFragment());
        mFragments.add(new SettingsActivity.AdvancedSettingsFragment());
        mTypeface = Typeface.createFromAsset(context.getAssets(), context.getResources().getString(R.string.sans_regular));
    }

    @Override
    public Fragment getItem(int position) {
        return mFragments.get(position);
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return CalligraphyUtils.applyTypefaceSpan(tabs[position], mTypeface);
    }

    @Override
    public int getCount() {
        return mFragments.size();
    }
}
