package code.name.monkey.retromusic.ui.fragments.mainactivity.home;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.velitasali.music.R;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import code.name.monkey.backend.Injection;
import code.name.monkey.backend.interfaces.MainActivityFragmentCallbacks;
import code.name.monkey.backend.loaders.SongLoader;
import code.name.monkey.backend.model.Album;
import code.name.monkey.backend.model.Artist;
import code.name.monkey.backend.model.Song;
import code.name.monkey.backend.model.smartplaylist.HistoryPlaylist;
import code.name.monkey.backend.model.smartplaylist.LastAddedPlaylist;
import code.name.monkey.backend.model.smartplaylist.MyTopTracksPlaylist;
import code.name.monkey.backend.mvp.contract.HomeContract;
import code.name.monkey.backend.mvp.presenter.HomePresenter;
import code.name.monkey.retromusic.dialogs.SleepTimerDialog;
import code.name.monkey.retromusic.helper.MusicPlayerRemote;
import code.name.monkey.retromusic.ui.adapter.album.AlbumAdapter;
import code.name.monkey.retromusic.ui.adapter.artist.ArtistAdapter;
import code.name.monkey.retromusic.ui.adapter.song.SongAdapter;
import code.name.monkey.retromusic.ui.fragments.base.AbsLibraryPagerFragment;
import code.name.monkey.retromusic.util.NavigationUtil;
import code.name.monkey.retromusic.util.PreferenceUtil;
import io.reactivex.disposables.CompositeDisposable;

public class HomeFragment extends AbsLibraryPagerFragment implements MainActivityFragmentCallbacks, HomeContract.HomeView
{
	private static final String TAG = "HomeFragment";

	Unbinder unbinder;
	@BindView(R.id.recent_artist)
	RecyclerView recentArtistRV;
	@BindView(R.id.recent_album)
	RecyclerView recentAlbumRV;
	@BindView(R.id.top_artist)
	RecyclerView topArtistRV;
	@BindView(R.id.top_album)
	RecyclerView topAlbumRV;
	@BindView(R.id.songs)
	RecyclerView songsRV;
	@BindView(R.id.recent_artist_container)
	View recentArtistContainer;
	@BindView(R.id.recent_albums_container)
	View recentAlbumsContainer;
	@BindView(R.id.top_artist_container)
	View topArtistContainer;
	@BindView(R.id.top_albums_container)
	View topAlbumContainer;
	@BindView(R.id.songs_container)
	View songsContainer;

	private HomePresenter homePresenter;
	private CompositeDisposable disposable;

	public static HomeFragment newInstance()
	{
		Bundle args = new Bundle();
		HomeFragment fragment = new HomeFragment();
		fragment.setArguments(args);
		return fragment;
	}


	@Override
	public void onCreate(@Nullable Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		disposable = new CompositeDisposable();
		homePresenter = new HomePresenter(Injection.provideRepository(getContext()), this);
	}

	@Nullable
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater,
							 @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
	{
		View view = inflater.inflate(R.layout.fragment_home, container, false);
		unbinder = ButterKnife.bind(this, view);
		return view;
	}

	@Override
	public boolean handleBackPress()
	{
		return false;
	}

	@Override
	public void selectedFragment(Fragment fragment)
	{

	}

	@Override
	public void onDestroyView()
	{
		super.onDestroyView();
		unbinder.unbind();
	}

	@Override
	public void onDestroy()
	{
		super.onDestroy();
		disposable.clear();
		homePresenter.unsubscribe();
	}

	@Override
	public void loading()
	{

	}

	@Override
	public void showEmptyView()
	{

	}

	@Override
	public void completed()
	{

	}

	@Override
	public void onResume()
	{
		super.onResume();
		homePresenter.subscribe();

		getLibraryFragment().getToolbar().setTitle(PreferenceUtil.getInstance(getContext()).tabTitles() ? R.string.library : R.string.home);
	}

	@Override
	public void showData(ArrayList<Object> homes)
	{
		//homeAdapter.swapDataSet(homes);
	}

	@Override
	public void onMediaStoreChanged()
	{
		super.onMediaStoreChanged();
		homePresenter.subscribe();
	}

	@Override
	public void recentArtist(ArrayList<Artist> artists)
	{
		recentArtistContainer.setVisibility(View.VISIBLE);
		recentArtistRV.setLayoutManager(new GridLayoutManager(getActivity(),
				1, GridLayoutManager.HORIZONTAL, false));
		ArtistAdapter artistAdapter = new ArtistAdapter(getActivity(), artists, R.layout.item_artist, false, null);
		recentArtistRV.setAdapter(artistAdapter);

	}

	@Override
	public void recentAlbum(ArrayList<Album> albums)
	{
		recentAlbumsContainer.setVisibility(View.VISIBLE);
		recentAlbumRV.setLayoutManager(new GridLayoutManager(getActivity(),
				1, GridLayoutManager.HORIZONTAL, false));
		AlbumAdapter artistAdapter = new AlbumAdapter(getActivity(), albums, R.layout.pager_item, false, null);
		recentAlbumRV.setAdapter(artistAdapter);

	}

	@Override
	public void topArtists(ArrayList<Artist> artists)
	{
		topArtistContainer.setVisibility(View.VISIBLE);
		topArtistRV.setLayoutManager(new GridLayoutManager(getActivity(),
				1, GridLayoutManager.HORIZONTAL, false));
		ArtistAdapter artistAdapter = new ArtistAdapter(getActivity(), artists, R.layout.item_artist, false, null);
		topArtistRV.setAdapter(artistAdapter);
	}

	@Override
	public void topAlbums(ArrayList<Album> albums)
	{
		topAlbumContainer.setVisibility(View.VISIBLE);
		topAlbumRV.setLayoutManager(new GridLayoutManager(getActivity(),
				1, GridLayoutManager.HORIZONTAL, false));
		AlbumAdapter artistAdapter = new AlbumAdapter(getActivity(), albums, R.layout.pager_item, false, null);
		topAlbumRV.setAdapter(artistAdapter);
	}

	@Override
	public void suggestions(ArrayList<Song> songs)
	{
		songsContainer.setVisibility(View.VISIBLE);
		songsRV.setLayoutManager(new GridLayoutManager(getActivity(),
				1, GridLayoutManager.HORIZONTAL, false));
		SongAdapter artistAdapter = new SongAdapter(getActivity(), songs, R.layout.item_image, false, null);
		songsRV.setAdapter(artistAdapter);
	}

	@OnClick({R.id.history, R.id.last_added, R.id.top_tracks, R.id.action_shuffle, R.id.timer})
	public void onViewClicked(View view)
	{
		Activity activity = getActivity();
		if (activity != null) {
			switch (view.getId()) {
				case R.id.history:
					NavigationUtil.goToPlaylistNew(activity, new HistoryPlaylist(activity));
					break;
				case R.id.last_added:
					NavigationUtil.goToPlaylistNew(activity, new LastAddedPlaylist(activity));
					break;
				case R.id.top_tracks:
					NavigationUtil.goToPlaylistNew(activity, new MyTopTracksPlaylist(activity));
					break;
				case R.id.action_shuffle:
					MusicPlayerRemote.openAndShuffleQueue(SongLoader.getAllSongs(activity).blockingFirst(), true);
					break;
				case R.id.timer:
					new SleepTimerDialog().show(getActivity().getSupportFragmentManager(), TAG);
					break;
			}
		}
	}
}