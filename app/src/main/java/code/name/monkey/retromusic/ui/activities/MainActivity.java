package code.name.monkey.retromusic.ui.activities;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.util.Pair;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.name.monkey.retromusic.ui.activities.base.AbsSlidingMusicPanelActivity;
import com.velitasali.music.R;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import code.name.monkey.backend.interfaces.MainActivityFragmentCallbacks;
import code.name.monkey.backend.loaders.AlbumLoader;
import code.name.monkey.backend.loaders.ArtistLoader;
import code.name.monkey.backend.loaders.PlaylistSongsLoader;
import code.name.monkey.backend.model.Song;
import code.name.monkey.backend.util.Util;
import code.name.monkey.retromusic.helper.MusicPlayerRemote;
import code.name.monkey.retromusic.helper.SearchQueryHelper;
import code.name.monkey.retromusic.service.MusicService;
import code.name.monkey.retromusic.ui.fragments.mainactivity.AlbumsFragment;
import code.name.monkey.retromusic.ui.fragments.mainactivity.ArtistsFragment;
import code.name.monkey.retromusic.ui.fragments.mainactivity.GenreFragment;
import code.name.monkey.retromusic.ui.fragments.mainactivity.LibraryFragment;
import code.name.monkey.retromusic.ui.fragments.mainactivity.PlaylistsFragment;
import code.name.monkey.retromusic.ui.fragments.mainactivity.SongsFragment;
import code.name.monkey.retromusic.ui.fragments.mainactivity.folders.FoldersFragment;
import code.name.monkey.retromusic.ui.fragments.mainactivity.home.HomeFragment;
import code.name.monkey.retromusic.util.PreferenceUtil;
import io.reactivex.Observable;


public class MainActivity extends AbsSlidingMusicPanelActivity implements
		SharedPreferences.OnSharedPreferenceChangeListener
{
	public static final int APP_INTRO_REQUEST = 2323;
	public static final int APP_USER_INFO_REQUEST = 9003;
	public static final int REQUEST_CODE_THEME = 9002;
	private static final String TAG = "MainActivity";

	private static final int LIBRARY = 0;
	private static final int FOLDERS = 1;
	private static final int SETTINGS = 2;
	private static final int ABOUT = 3;
	@Nullable
	MainActivityFragmentCallbacks currentFragment;
	@BindView(R.id.navigation_view)
	ViewGroup navigationView;
	@BindView(R.id.drawer_layout)
	DrawerLayout drawerLayout;

	@BindView(R.id.navigation_item)
	RecyclerView navigationItems;

	private boolean blockRequestPermissions;
	private BroadcastReceiver broadcastReceiver = new BroadcastReceiver()
	{
		@Override
		public void onReceive(Context context, Intent intent)
		{
			String action = intent.getAction();
			if (action != null && action.equals(Intent.ACTION_SCREEN_OFF)) {
				collapsePanel();
				if (PreferenceUtil.getInstance(context).getLockScreen() && MusicPlayerRemote.isPlaying()) {
					context.startActivity(new Intent(context, LockScreenActivity.class));
				}
			}
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		setDrawUnderStatusbar(true);
		super.onCreate(savedInstanceState);


		ButterKnife.bind(this);
		setBottomBarVisibility(View.VISIBLE);

		if (Build.VERSION.SDK_INT == Build.VERSION_CODES.KITKAT) {
			Util.setStatusBarTranslucent(getWindow());
			drawerLayout.setFitsSystemWindows(false);
			navigationView.setFitsSystemWindows(false);
			//noinspection ConstantConditions
			findViewById(R.id.drawer_content_container).setFitsSystemWindows(false);
		} else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			drawerLayout.setOnApplyWindowInsetsListener((view, windowInsets) -> {
				navigationView.dispatchApplyWindowInsets(windowInsets);
				return windowInsets.replaceSystemWindowInsets(0, 0, 0, 0);
			});
		}

		setUpNavigationView();

		if (savedInstanceState == null) {
			setMusicChooser(PreferenceUtil.getInstance(this).getLastMusicChooser());
		} else {
			restoreCurrentFragment();
		}
	}

	@Override
	protected void onResume()
	{
		super.onResume();
		IntentFilter screenOnOff = new IntentFilter();
		screenOnOff.addAction(Intent.ACTION_SCREEN_OFF);
		registerReceiver(broadcastReceiver, screenOnOff);
		PreferenceUtil.getInstance(this).registerOnSharedPreferenceChangedListener(this);
	}

	@Override
	public void onDestroy()
	{
		super.onDestroy();
		if (broadcastReceiver == null) {
			return;
		}
		unregisterReceiver(broadcastReceiver);
		PreferenceUtil.getInstance(this).unregisterOnSharedPreferenceChangedListener(this);
	}

	private boolean checkUserName()
	{
		return PreferenceUtil.getInstance(this).getUserName().isEmpty();
	}

	private void setMusicChooser(int key)
	{
		PreferenceUtil.getInstance(this).setLastMusicChooser(key);
		switch (key) {
			case FOLDERS:
				setCurrentFragment(FoldersFragment.newInstance(this), false);
				break;
			default:
			case LIBRARY:
				setCurrentFragment(LibraryFragment.newInstance(), false);
				break;
		}
	}


	private void setUpNavigationView()
	{
		navigationItems.setLayoutManager(new LinearLayoutManager(this));
		navigationItems.setItemAnimator(new DefaultItemAnimator());
		navigationItems.setAdapter(new NavigationItemsAdapter());

	}

	@Override
	protected View createContentView()
	{
		@SuppressLint("InflateParams")
		View contentView = getLayoutInflater().inflate(R.layout.activity_main_drawer_layout, null);
		ViewGroup drawerContent = contentView.findViewById(R.id.drawer_content_container);
		drawerContent.addView(wrapSlidingMusicPanel(R.layout.activity_main_content));
		return contentView;
	}

	public void setCurrentFragment(@Nullable Fragment fragment, boolean isStackAdd)
	{
		FragmentManager fragmentManager = getSupportFragmentManager();
		FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
		fragmentTransaction.replace(R.id.fragment_container, fragment, TAG);
		if (isStackAdd) {
			fragmentTransaction.addToBackStack(TAG);
		}
		fragmentTransaction.commit();

		currentFragment = (MainActivityFragmentCallbacks) fragment;
	}

	private void restoreCurrentFragment()
	{
		currentFragment = (MainActivityFragmentCallbacks) getSupportFragmentManager()
				.findFragmentById(R.id.fragment_container);
	}

	@Override
	public boolean onNavigationItemSelected(@NonNull MenuItem item)
	{
		PreferenceUtil.getInstance(this).setLastPage(item.getItemId());
		Observable.just(item)
				.throttleFirst(3, TimeUnit.SECONDS)
				.subscribe(menuItem -> {
					if (currentFragment != null) {
						switch (menuItem.getItemId()) {
							default:
							case R.id.action_song:
								currentFragment.selectedFragment(SongsFragment.newInstance());
								break;
							case R.id.action_album:
								currentFragment.selectedFragment(AlbumsFragment.newInstance());
								break;
							case R.id.action_artist:
								currentFragment.selectedFragment(ArtistsFragment.newInstance());
								break;
							case R.id.action_playlist:
								currentFragment.selectedFragment(PlaylistsFragment.newInstance());
								break;
							case R.id.action_home:
                                currentFragment.selectedFragment(HomeFragment.newInstance());
                                break;
						}
					}
				});
		return true;
	}

	private void handlePlaybackIntent(@Nullable Intent intent)
	{
		if (intent == null) {
			return;
		}

		Uri uri = intent.getData();
		String mimeType = intent.getType();
		boolean handled = false;

		if (intent.getAction() != null &&
				intent.getAction().equals(MediaStore.INTENT_ACTION_MEDIA_PLAY_FROM_SEARCH)) {
			final ArrayList<Song> songs = SearchQueryHelper.getSongs(this, intent.getExtras());
			if (MusicPlayerRemote.getShuffleMode() == MusicService.SHUFFLE_MODE_SHUFFLE) {
				MusicPlayerRemote.openAndShuffleQueue(songs, true);
			} else {
				MusicPlayerRemote.openQueue(songs, 0, true);
			}
			handled = true;
		}

		if (uri != null && uri.toString().length() > 0) {
			MusicPlayerRemote.playFromUri(uri);
			handled = true;
		} else if (MediaStore.Audio.Playlists.CONTENT_TYPE.equals(mimeType)) {
			final int id = (int) parseIdFromIntent(intent, "playlistId", "playlist");
			if (id >= 0) {
				int position = intent.getIntExtra("position", 0);
				ArrayList<Song> songs = new ArrayList<>();
				songs.addAll(PlaylistSongsLoader.getPlaylistSongList(this, id).blockingFirst());
				MusicPlayerRemote.openQueue(songs, position, true);
				handled = true;
			}
		} else if (MediaStore.Audio.Albums.CONTENT_TYPE.equals(mimeType)) {
			final int id = (int) parseIdFromIntent(intent, "albumId", "album");
			if (id >= 0) {
				int position = intent.getIntExtra("position", 0);
				MusicPlayerRemote.openQueue(AlbumLoader.getAlbum(this, id).blockingFirst().songs, position, true);
				handled = true;
			}
		} else if (MediaStore.Audio.Artists.CONTENT_TYPE.equals(mimeType)) {
			final int id = (int) parseIdFromIntent(intent, "artistId", "artist");
			if (id >= 0) {
				int position = intent.getIntExtra("position", 0);
				MusicPlayerRemote.openQueue(ArtistLoader.getArtist(this, id).blockingFirst().getSongs(), position, true);
				handled = true;
			}
		}
		if (handled) {
			setIntent(new Intent());
		}
	}

	private long parseIdFromIntent(@NonNull Intent intent,
								   String longKey,
								   String stringKey)
	{
		long id = intent.getLongExtra(longKey, -1);
		if (id < 0) {
			String idString = intent.getStringExtra(stringKey);
			if (idString != null) {
				try {
					id = Long.parseLong(idString);
				} catch (NumberFormatException e) {
					Log.e(TAG, e.getMessage());
				}
			}
		}
		return id;
	}

	@Override
	public void onPanelExpanded(View view)
	{
		super.onPanelExpanded(view);
		drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
	}

	@Override
	public void onPanelCollapsed(View view)
	{
		super.onPanelCollapsed(view);
		drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		super.onActivityResult(requestCode, resultCode, data);
		switch (requestCode) {
			case APP_INTRO_REQUEST:
				blockRequestPermissions = false;
				if (!hasPermissions()) {
					requestPermissions();
				}
				break;
			case REQUEST_CODE_THEME:
			case APP_USER_INFO_REQUEST:
				postRecreate();
				break;
		}

	}

	@Override
	public boolean handleBackPress()
	{
		if (drawerLayout.isDrawerOpen(navigationView)) {
			drawerLayout.closeDrawers();
			return true;
		}
		return super.handleBackPress() || (currentFragment != null &&
				currentFragment.handleBackPress()
		);
	}

	@Override
	public void onServiceConnected()
	{
		super.onServiceConnected();
		handlePlaybackIntent(getIntent());
	}

	@Override
	protected void requestPermissions()
	{
		if (!blockRequestPermissions) super.requestPermissions();
	}

	@Override
	public boolean onOptionsItemSelected(@NonNull MenuItem item)
	{
		if (item.getItemId() == android.R.id.home) {
			if (drawerLayout.isDrawerOpen(navigationView)) {
				drawerLayout.closeDrawer(navigationView);
			} else {
				drawerLayout.openDrawer(navigationView);
			}
			return true;
		}
		return super.onOptionsItemSelected(item);
	}


	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key)
	{
		if (key.equalsIgnoreCase(PreferenceUtil.GENERAL_THEME) ||
				key.equalsIgnoreCase(PreferenceUtil.ADAPTIVE_COLOR_APP) ||
				key.equalsIgnoreCase(PreferenceUtil.DOMINANT_COLOR) ||
				key.equalsIgnoreCase(PreferenceUtil.USER_NAME) ||
				key.equalsIgnoreCase(PreferenceUtil.TOGGLE_FULL_SCREEN) ||
				key.equalsIgnoreCase(PreferenceUtil.TOGGLE_VOLUME) ||
				key.equalsIgnoreCase(PreferenceUtil.TOGGLE_TAB_TITLES) ||
				key.equalsIgnoreCase(PreferenceUtil.ROUND_CORNERS) ||
				key.equals(PreferenceUtil.CAROUSEL_EFFECT) ||
				key.equals(PreferenceUtil.NOW_PLAYING_SCREEN_ID) ||
				key.equals(PreferenceUtil.TOGGLE_GENRE) ||
				key.equals(PreferenceUtil.BANNER_IMAGE_PATH) ||
				key.equals(PreferenceUtil.PROFILE_IMAGE_PATH)) {
			postRecreate();
		}
	}

	class NavigationItemsAdapter extends RecyclerView.Adapter<NavigationItemsAdapter.ViewHolder>
	{
		List<Pair<Integer, Integer>> mList = new ArrayList<>();

		NavigationItemsAdapter()
		{
			mList.add(new Pair<>(R.drawable.ic_library_music_white_24dp, R.string.library));
			mList.add(new Pair<>(R.drawable.ic_folder_white_24dp, R.string.folders));
			mList.add(new Pair<>(R.drawable.ic_settings_white_24dp, R.string.action_settings));
			mList.add(new Pair<>(R.drawable.ic_help_white_24dp, R.string.action_about));
		}

		@Override
		public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i)
		{
			return new ViewHolder(LayoutInflater.from(MainActivity.this).inflate(R.layout.item_navigation_item, viewGroup, false));
		}

		@Override
		public void onBindViewHolder(ViewHolder viewHolder, int i)
		{
			Pair<Integer, Integer> pair = mList.get(i);
			viewHolder.imageView.setImageResource(pair.first);
			viewHolder.title.setText(pair.second);
			viewHolder.itemView.setOnClickListener(view -> {
				drawerLayout.closeDrawers();
				switch (viewHolder.getAdapterPosition()) {
					case LIBRARY:
						new Handler().postDelayed(() -> setMusicChooser(LIBRARY), 200);
						break;
					case FOLDERS:
						new Handler().postDelayed(() -> setMusicChooser(FOLDERS), 200);
						break;
					case SETTINGS:
						new Handler().postDelayed(() -> startActivity(new Intent(MainActivity.this, SettingsActivity.class)), 200);
						break;
					case ABOUT:
						new Handler().postDelayed(() -> startActivity(new Intent(MainActivity.this, AboutActivity.class)), 200);
						break;
				}
			});
		}

		@Override
		public int getItemCount()
		{
			return mList.size();
		}

		public class ViewHolder extends RecyclerView.ViewHolder
		{
			@BindView(R.id.title)
			TextView title;
			@BindView(R.id.image)
			ImageView imageView;

			public ViewHolder(View itemView)
			{
				super(itemView);
				ButterKnife.bind(this, itemView);
			}
		}
	}

}
