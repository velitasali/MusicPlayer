package code.name.monkey.retromusic.ui.fragments.mainactivity;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;

import com.velitasali.music.R;

import java.util.ArrayList;

import code.name.monkey.backend.Injection;
import code.name.monkey.backend.model.Genre;
import code.name.monkey.backend.mvp.contract.GenreContract;
import code.name.monkey.backend.mvp.presenter.GenrePresenter;
import code.name.monkey.retromusic.ui.adapter.GenreAdapter;
import code.name.monkey.retromusic.ui.fragments.base.AbsLibraryPagerRecyclerViewFragment;
import code.name.monkey.retromusic.util.PreferenceUtil;

/**
 * @author Hemanth S (h4h13).
 */

public class GenreFragment extends AbsLibraryPagerRecyclerViewFragment<GenreAdapter, LinearLayoutManager> implements GenreContract.GenreView
{
	private GenrePresenter mPresenter;

	public static GenreFragment newInstance()
	{
		Bundle args = new Bundle();
		GenreFragment fragment = new GenreFragment();
		fragment.setArguments(args);
		return fragment;
	}

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
		mPresenter = new GenrePresenter(Injection.provideRepository(getContext()), this);
	}

	@Override
	public void setMenuVisibility(boolean menuVisible)
	{
		super.setMenuVisibility(menuVisible);
		if (menuVisible)
			getLibraryFragment().getToolbar().setTitle(PreferenceUtil.getInstance(getContext()).tabTitles() ? R.string.library : R.string.genres);
	}

	@Override
	public void onResume()
	{
		super.onResume();
		getLibraryFragment().getToolbar().setTitle(PreferenceUtil.getInstance(getContext()).tabTitles() ? R.string.library : R.string.genres);
		if (getAdapter().getDataSet().isEmpty()) mPresenter.subscribe();
	}


	@Override
	public void onDestroy()
	{
		super.onDestroy();
		mPresenter.unsubscribe();
	}

	@NonNull
	@Override
	protected LinearLayoutManager createLayoutManager()
	{
		return new LinearLayoutManager(getActivity());
	}

	@NonNull
	@Override
	protected GenreAdapter createAdapter()
	{
		return new GenreAdapter(getLibraryFragment().getMainActivity(), R.layout.item_list);
	}

	@Override
	public void loading()
	{
		getProgressBar().setVisibility(View.VISIBLE);
	}

	@Override
	public void showData(ArrayList<Genre> songs)
	{
		getAdapter().swapDataSet(songs);
	}

	@Override
	public void showEmptyView()
	{
		getAdapter().swapDataSet(new ArrayList<Genre>());
	}

	@Override
	public void completed()
	{
		getProgressBar().setVisibility(View.GONE);
	}


	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
	{
		super.onCreateOptionsMenu(menu, inflater);
		menu.removeItem(R.id.action_sort_order);
		menu.removeItem(R.id.action_grid_size);
		menu.removeItem(R.id.action_new_playlist);
	}

	@Override
	protected int getEmptyMessage()
	{
		return R.string.no_genres;
	}
}
