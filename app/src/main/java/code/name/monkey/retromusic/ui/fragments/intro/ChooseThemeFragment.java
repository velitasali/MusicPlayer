package code.name.monkey.retromusic.ui.fragments.intro;

import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.kabouzeid.appthemehelper.ThemeStore;

import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import code.name.monkey.retromusic.R;
import code.name.monkey.retromusic.appshortcuts.DynamicShortcutManager;
import code.name.monkey.retromusic.ui.activities.UserInfoActivity;
import code.name.monkey.retromusic.util.PreferenceUtil;

import static android.app.Activity.RESULT_OK;

/**
 * @author Hemanth S (h4h13).
 */

public class ChooseThemeFragment extends Fragment {
    private Unbinder mUnbinder;

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mUnbinder.unbind();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_choose_theme, container, false);
        mUnbinder = ButterKnife.bind(this, view);
        return view;
    }

    @OnClick({R.id.next, R.id.light, R.id.black, R.id.dark})
    void next(View view) {
        String themeName = "";
        switch (view.getId()) {
            case R.id.dark:
                themeName = "dark";
                break;
            case R.id.light:
                themeName = "light";
                break;
            case R.id.black:
                themeName = "black";
                break;
            case R.id.next:
                getActivity().setResult(RESULT_OK);
                getActivity().finish();
                break;
        }
        if (!TextUtils.isEmpty(themeName)) {
            Log.i("Hmm", "next: " + themeName);
            ThemeStore.editTheme(getContext())
                    .activityTheme(PreferenceUtil.getThemeResFromPrefValue(themeName))
                    .commit();

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
                getActivity().setTheme(PreferenceUtil.getThemeResFromPrefValue(themeName));
                new DynamicShortcutManager(getActivity()).updateDynamicShortcuts();
            }
            ((UserInfoActivity)getActivity()).postRecreate();
        }
    }
}
