package code.name.monkey.retromusic.ui.fragments.mainactivity;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;

import com.velitasali.music.R;

import java.util.ArrayList;

import code.name.monkey.backend.Injection;
import code.name.monkey.backend.helper.SortOrder.ArtistSortOrder;
import code.name.monkey.backend.model.Artist;
import code.name.monkey.backend.mvp.contract.ArtistContract;
import code.name.monkey.backend.mvp.presenter.ArtistPresenter;
import code.name.monkey.retromusic.ui.adapter.artist.ArtistAdapter;
import code.name.monkey.retromusic.ui.fragments.base.AbsLibraryPagerRecyclerViewCustomGridSizeFragment;
import code.name.monkey.retromusic.util.PreferenceUtil;

/**
 * Created by hemanths on 18/08/17.
 */

public class ArtistsFragment extends AbsLibraryPagerRecyclerViewCustomGridSizeFragment<ArtistAdapter, GridLayoutManager> implements ArtistContract.ArtistView
{
	public static final String TAG = ArtistsFragment.class.getSimpleName();
	private ArtistPresenter mPresenter;

	public static ArtistsFragment newInstance()
	{

		Bundle args = new Bundle();

		ArtistsFragment fragment = new ArtistsFragment();
		fragment.setArguments(args);
		return fragment;
	}

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		mPresenter = new ArtistPresenter(Injection.provideRepository(getContext()), this);
	}

	@NonNull
	@Override
	protected GridLayoutManager createLayoutManager()
	{
		return new GridLayoutManager(getActivity(), getGridSize());
	}

	@NonNull
	@Override
	protected ArtistAdapter createAdapter()
	{
		int itemLayoutRes = getItemLayoutRes();
		notifyLayoutResChanged(itemLayoutRes);
		ArrayList<Artist> dataSet = getAdapter() == null ? new ArrayList<Artist>() : getAdapter().getDataSet();
		return new ArtistAdapter(getLibraryFragment().getMainActivity(), dataSet, itemLayoutRes, loadUsePalette(), getLibraryFragment());
	}

	@Override
	protected int getEmptyMessage()
	{
		return R.string.no_artists;
	}

	@Override
	public void onMediaStoreChanged()
	{
		mPresenter.loadArtists();
	}

	@Override
	protected int loadGridSize()
	{
		return PreferenceUtil.getInstance(getActivity()).getArtistGridSize(getActivity());
	}

	@Override
	protected void saveGridSize(int gridSize)
	{
		PreferenceUtil.getInstance(getActivity()).setArtistGridSize(gridSize);
	}

	@Override
	protected int loadGridSizeLand()
	{
		return PreferenceUtil.getInstance(getActivity()).getArtistGridSizeLand(getActivity());
	}

	@Override
	protected void saveGridSizeLand(int gridSize)
	{
		PreferenceUtil.getInstance(getActivity()).setArtistGridSizeLand(gridSize);
	}

	@Override
	protected void saveUsePalette(boolean usePalette)
	{
		PreferenceUtil.getInstance(getActivity()).setArtistColoredFooters(usePalette);
	}

	@Override
	public boolean loadUsePalette()
	{
		return PreferenceUtil.getInstance(getActivity()).artistColoredFooters();
	}

	@Override
	protected void setUsePalette(boolean usePalette)
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
	public void setMenuVisibility(boolean menuVisible)
	{
		super.setMenuVisibility(menuVisible);
		if (menuVisible)
			getLibraryFragment().getToolbar().setTitle(PreferenceUtil.getInstance(getContext()).tabTitles() ? R.string.library : R.string.artists);
	}

	@Override
	public void onResume()
	{
		super.onResume();
		getLibraryFragment().getToolbar().setTitle(PreferenceUtil.getInstance(getContext()).tabTitles() ? R.string.library : R.string.artists);
		if (getAdapter().getDataSet().isEmpty()) mPresenter.subscribe();
	}

	@Override
	public void onDestroy()
	{
		super.onDestroy();
		mPresenter.unsubscribe();
	}

	@Override
	public void loading()
	{
		getProgressBar().setVisibility(View.VISIBLE);
	}

	@Override
	public void showEmptyView()
	{
		getAdapter().swapDataSet(new ArrayList<>());
	}

	@Override
	public void completed()
	{
		getProgressBar().setVisibility(View.GONE);
	}

	@Override
	public void showData(ArrayList<Artist> artists)
	{
		getAdapter().swapDataSet(artists);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		return handleSortOrderMenuItem(item);
	}

	private boolean handleSortOrderMenuItem(@NonNull MenuItem item)
	{
		String sortOrder = null;
		switch (item.getItemId()) {
			case R.id.action_sort_order_artist:
				sortOrder = ArtistSortOrder.ARTIST_A_Z;
				break;
			case R.id.action_sort_order_artist_desc:
				sortOrder = ArtistSortOrder.ARTIST_Z_A;
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
		PreferenceUtil.getInstance(getContext()).setArtistSortOrder(sortOrder);
		mPresenter.loadArtists();
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
	{
		super.onCreateOptionsMenu(menu, inflater);
		MenuItem sortOrder = menu.findItem(R.id.action_sort_order);
		setUpSortOrderMenu(sortOrder.getSubMenu());
	}

	private void setUpSortOrderMenu(SubMenu subMenu)
	{
		subMenu.removeItem(R.id.action_sort_order_title_desc);
		subMenu.removeItem(R.id.action_sort_order_title);
		subMenu.removeItem(R.id.action_sort_order_date);
		subMenu.removeItem(R.id.action_sort_order_duration);
		subMenu.removeItem(R.id.action_sort_order_year);
		subMenu.removeItem(R.id.action_sort_order_album);
		subMenu.removeItem(R.id.action_sort_order_album_desc);

		switch (getSavedSortOrder()) {
			case ArtistSortOrder.ARTIST_A_Z:
				subMenu.findItem(R.id.action_sort_order_artist).setChecked(true);
				break;
			case ArtistSortOrder.ARTIST_Z_A:
				subMenu.findItem(R.id.action_sort_order_artist_desc).setChecked(true);
				break;
		}
	}

	private String getSavedSortOrder()
	{
		return PreferenceUtil.getInstance(getContext()).getArtistSortOrder();
	}
}
