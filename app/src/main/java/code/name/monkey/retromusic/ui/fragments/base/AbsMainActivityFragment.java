package code.name.monkey.retromusic.ui.fragments.base;

import android.os.Build;
import android.os.Bundle;
import android.view.View;

import com.velitasali.music.R;

import code.name.monkey.appthemehelper.ThemeStore;
import code.name.monkey.appthemehelper.util.ColorUtil;
import code.name.monkey.retromusic.ui.activities.MainActivity;

/**
 * @author Karim Abou Zeid (kabouzeid)
 */
public abstract class AbsMainActivityFragment extends AbsMusicServiceFragment
{

	public MainActivity getMainActivity()
	{
		return (MainActivity) getActivity();
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState)
	{
		super.onActivityCreated(savedInstanceState);
		setHasOptionsMenu(true);

		getMainActivity().setNavigationbarColorAuto();
		getMainActivity().setTaskDescriptionColorAuto();
		getMainActivity().hideStatusBar();
	}

	// WORKAROUND
	public void setStatusbarColor(View view, int color)
	{
		final View statusBar = view.findViewById(R.id.status_bar);
		if (statusBar != null) {
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
				statusBar.setBackgroundColor(ColorUtil.darkenColor(color));
			} else {
				statusBar.setBackgroundColor(ColorUtil.darkenColor(color));
			}
		}
	}

	public void setStatusbarColorAuto(View view)
	{
		// we don't want to use statusbar color because we are doing the color darkening on our own to support KitKat
		//noinspection ConstantConditions
		setStatusbarColor(view, ThemeStore.primaryColor(getContext()));

	}
}
