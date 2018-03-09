package code.name.monkey.retromusic.ui.fragments.mainactivity;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;

import com.velitasali.music.R;

import java.util.ArrayList;

import code.name.monkey.backend.Injection;
import code.name.monkey.backend.helper.SortOrder.SongSortOrder;
import code.name.monkey.backend.model.Song;
import code.name.monkey.backend.mvp.contract.SongContract;
import code.name.monkey.backend.mvp.presenter.SongPresenter;
import code.name.monkey.retromusic.ui.adapter.song.ShuffleButtonSongAdapter;
import code.name.monkey.retromusic.ui.adapter.song.SongAdapter;
import code.name.monkey.retromusic.ui.fragments.base.AbsLibraryPagerRecyclerViewCustomGridSizeFragment;
import code.name.monkey.retromusic.util.PreferenceUtil;

/**
 * A simple {@link Fragment} subclass.
 */
@SuppressWarnings("ConstantConditions")
public class SongsFragment extends AbsLibraryPagerRecyclerViewCustomGridSizeFragment<SongAdapter, GridLayoutManager> implements SongContract.SongView
{
	private static final String TAG = "Songs";
	private SongPresenter mPresenter;

	public SongsFragment()
	{
		// Required empty public constructor
	}

	public static SongsFragment newInstance()
	{
		Bundle args = new Bundle();
		SongsFragment fragment = new SongsFragment();
		fragment.setArguments(args);
		return fragment;
	}

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		mPresenter = new SongPresenter(Injection.provideRepository(getContext()), this);
	}

	@NonNull
	@Override
	protected GridLayoutManager createLayoutManager()
	{
		return new GridLayoutManager(getActivity(), getGridSize());
	}

	@Override
	protected int getEmptyMessage()
	{
		return R.string.no_songs;
	}

	@NonNull
	@Override
	protected SongAdapter createAdapter()
	{
		int itemLayoutRes = getItemLayoutRes();
		notifyLayoutResChanged(itemLayoutRes);
		boolean usePalette = loadUsePalette();
		ArrayList<Song> dataSet = getAdapter() == null ? new ArrayList<Song>() : getAdapter().getDataSet();

		if (getGridSize() <= getMaxGridSizeForList()) {
			return new ShuffleButtonSongAdapter(getLibraryFragment().getMainActivity(), dataSet, itemLayoutRes, usePalette, getLibraryFragment());
		}
		return new SongAdapter(getLibraryFragment().getMainActivity(), dataSet, itemLayoutRes, usePalette, getLibraryFragment());
	}

	@Override
	public void onMediaStoreChanged()
	{
		mPresenter.loadSongs();
	}

	@Override
	protected int loadGridSize()
	{
		return PreferenceUtil.getInstance(getActivity()).getSongGridSize(getActivity());
	}

	@Override
	protected void saveGridSize(int gridSize)
	{
		PreferenceUtil.getInstance(getActivity()).setSongGridSize(gridSize);
	}

	@Override
	protected int loadGridSizeLand()
	{
		return PreferenceUtil.getInstance(getActivity()).getSongGridSizeLand(getActivity());
	}

	@Override
	protected void saveGridSizeLand(int gridSize)
	{
		PreferenceUtil.getInstance(getActivity()).setSongGridSizeLand(gridSize);
	}

	@Override
	public void saveUsePalette(boolean usePalette)
	{
		PreferenceUtil.getInstance(getActivity()).setSongColoredFooters(usePalette);
	}

	@Override
	public boolean loadUsePalette()
	{
		return PreferenceUtil.getInstance(getActivity()).songColoredFooters();
	}

	@Override
	public void setUsePalette(boolean usePalette)
	{
		getAdapter().usePalette(usePalette);
	}

	@Override
	protected void setGridSize(int gridSize)
	{
		getLayoutManager().setSpanCount(gridSize);
		getAdapter().notifyDataSetChanged();
	}

	@Override
	public void onResume()
	{
		super.onResume();
		getLibraryFragment().getToolbar().setTitle(PreferenceUtil.getInstance(getContext()).tabTitles() ? R.string.library : R.string.songs);
		if (getAdapter().getDataSet().isEmpty()) mPresenter.subscribe();
	}

	@Override
	public void setMenuVisibility(boolean menuVisible)
	{
		super.setMenuVisibility(menuVisible);
		if (menuVisible)
			getLibraryFragment().getToolbar().setTitle(PreferenceUtil.getInstance(getContext()).tabTitles() ? R.string.library : R.string.songs);
	}

	@Override
	public void onDestroy()
	{
		mPresenter.unsubscribe();
		super.onDestroy();
	}

	@Override
	public void loading()
	{
		getProgressBar().setVisibility(View.VISIBLE);
	}

	@Override
	public void showData(ArrayList<Song> songs)
	{
		getAdapter().swapDataSet(songs);
	}

	@Override
	public void showEmptyView()
	{
		getAdapter().swapDataSet(new ArrayList<Song>());
	}

	@Override
	public void completed()
	{
		getProgressBar().setVisibility(View.GONE);
	}


	private boolean handleSortOrderMenuItem(@NonNull MenuItem item)
	{
		String sortOrder = null;
		switch (item.getItemId()) {
			case R.id.action_sort_order_album:
				sortOrder = SongSortOrder.SONG_ALBUM;
				break;
			case R.id.action_sort_order_title:
				sortOrder = SongSortOrder.SONG_A_Z;
				break;
			case R.id.action_sort_order_title_desc:
				sortOrder = SongSortOrder.SONG_Z_A;
				break;
			case R.id.action_sort_order_artist:
				sortOrder = SongSortOrder.SONG_ARTIST;
				break;
			case R.id.action_sort_order_year:
				sortOrder = SongSortOrder.SONG_YEAR;
				break;
			case R.id.action_sort_order_duration:
				sortOrder = SongSortOrder.SONG_DURATION;
				break;
			case R.id.action_sort_order_date:
				sortOrder = SongSortOrder.SONG_DATE;
				break;
		}
		if (sortOrder != null) {
			item.setChecked(true);
			setSaveSortOrder(sortOrder);
		}
		return true;
	}

	private void setSaveSortOrder(String sortOrder)
	{
		PreferenceUtil.getInstance(getContext()).setSongSortOrder(sortOrder);
		reload();
	}

	private void reload()
	{
		mPresenter.loadSongs();
	}

	private void setUpSortOrderMenu(@NonNull SubMenu sortOrder)
	{
		sortOrder.removeItem(R.id.action_sort_order_album_desc);
		sortOrder.removeItem(R.id.action_sort_order_artist_desc);

		switch (getSavedSortOrder()) {
			case SongSortOrder.SONG_ALBUM:
				sortOrder.findItem(R.id.action_sort_order_album).setChecked(true);
				break;
			case SongSortOrder.SONG_A_Z:
				sortOrder.findItem(R.id.action_sort_order_title).setChecked(true);
				break;
			case SongSortOrder.SONG_ARTIST:
				sortOrder.findItem(R.id.action_sort_order_artist).setChecked(true);
				break;
			case SongSortOrder.SONG_DATE:
				sortOrder.findItem(R.id.action_sort_order_date).setChecked(true);
				break;
			case SongSortOrder.SONG_DURATION:
				sortOrder.findItem(R.id.action_sort_order_duration).setChecked(true);
				break;
			case SongSortOrder.SONG_YEAR:
				sortOrder.findItem(R.id.action_sort_order_year).setChecked(true);
				break;
			case SongSortOrder.SONG_Z_A:
				sortOrder.findItem(R.id.action_sort_order_title_desc).setChecked(true);
				break;
		}
	}

	private String getSavedSortOrder()
	{
		return PreferenceUtil.getInstance(getContext()).getSongSortOrder();
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
	{
		super.onCreateOptionsMenu(menu, inflater);
		MenuItem sortOrder = menu.findItem(R.id.action_sort_order);
		setUpSortOrderMenu(sortOrder.getSubMenu());
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		return handleSortOrderMenuItem(item);
	}
}
