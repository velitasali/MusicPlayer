package code.name.monkey.retromusic.ui.fragments.mainactivity.home;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.util.Pair;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.kabouzeid.appthemehelper.util.ATHUtil;
import com.retro.musicplayer.backend.Injection;
import com.retro.musicplayer.backend.interfaces.LibraryTabSelectedItem;
import com.retro.musicplayer.backend.interfaces.MainActivityFragmentCallbacks;
import com.retro.musicplayer.backend.loaders.LastAddedSongsLoader;
import com.retro.musicplayer.backend.model.Playlist;
import com.retro.musicplayer.backend.model.smartplaylist.HistoryPlaylist;
import com.retro.musicplayer.backend.model.smartplaylist.LastAddedPlaylist;
import com.retro.musicplayer.backend.model.smartplaylist.MyTopTracksPlaylist;
import com.retro.musicplayer.backend.mvp.contract.HomeContract;
import com.retro.musicplayer.backend.mvp.presenter.HomePresenter;
import com.retro.musicplayer.backend.util.schedulers.BaseSchedulerProvider;
import com.retro.musicplayer.backend.util.schedulers.SchedulerProvider;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Random;

import butterknife.BindView;
import butterknife.BindViews;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import code.name.monkey.retromusic.R;
import code.name.monkey.retromusic.misc.AppBarStateChangeListener;
import code.name.monkey.retromusic.ui.activities.SearchActivity;
import code.name.monkey.retromusic.ui.adapter.PlaylistAdapter;
import code.name.monkey.retromusic.ui.adapter.album.AlbumAdapter;
import code.name.monkey.retromusic.ui.adapter.artist.ArtistAdapter;
import code.name.monkey.retromusic.ui.fragments.base.AbsMainActivityFragment;
import code.name.monkey.retromusic.util.NavigationUtil;
import code.name.monkey.retromusic.util.PreferenceUtil;
import code.name.monkey.retromusic.util.ToolbarColorizeHelper;
import io.reactivex.disposables.CompositeDisposable;

import static code.name.monkey.retromusic.R.id.toolbar;

/**
 * Created by hemanths on 19/07/17.
 */

public class HomeFragment extends AbsMainActivityFragment
        implements MainActivityFragmentCallbacks, HomeContract.HomeView, LibraryTabSelectedItem {
    private static final String TAG = "HomeFragment";

    @BindView(R.id.recycler_view_albums)
    RecyclerView recyclerViewAlbums;
    @BindView(R.id.recycler_view_artist)
    RecyclerView recyclerViewArtists;
    @BindView(R.id.recycler_view)
    RecyclerView recyclerView;
    Unbinder unbinder;
    @BindView(toolbar)
    Toolbar mToolbar;
    @BindView(R.id.appbar)
    AppBarLayout mAppbar;
    @BindView(R.id.image)
    ImageView mImageView;
    @BindView(R.id.title)
    TextView mTitle;
    @BindView(R.id.collapsing_toolbar)
    CollapsingToolbarLayout mToolbarLayout;
    @BindView(R.id.container)
    LinearLayout mContainer;
    @BindView(R.id.genre_recycler_view)
    RecyclerView genreRecyclerView;
    @BindViews({R.id.album_container,
            R.id.artist_container,
            R.id.playlist_container})
    List<ViewGroup> mViewGroups;
    private PlaylistAdapter adapter;
    private HomePresenter mHomePresenter;
    private CompositeDisposable mDisposable;
    private BaseSchedulerProvider mProvider;

    public static HomeFragment newInstance() {
        Bundle args = new Bundle();
        HomeFragment fragment = new HomeFragment();
        fragment.setArguments(args);
        return fragment;
    }

    private void setupRecentArtists() {
        recyclerViewArtists.setLayoutManager(new GridLayoutManager(getContext(), 1, GridLayoutManager.HORIZONTAL, false));
        recyclerViewArtists.setItemAnimator(new DefaultItemAnimator());
        mDisposable.add(LastAddedSongsLoader.getLastAddedArtists(getContext())
                .subscribeOn(mProvider.io())
                .observeOn(mProvider.ui())
                .subscribe(artists -> {
                    if (artists.size() > 0) {
                        mViewGroups.get(1).setVisibility(View.VISIBLE);
                        recyclerViewArtists.setAdapter(new ArtistAdapter(getMainActivity(), artists, R.layout.item_artist, false, null));
                    }
                }));
    }

    private void setupRecentAlbums() {
        recyclerViewAlbums.setLayoutManager(new GridLayoutManager(getContext(), 1, GridLayoutManager.HORIZONTAL, false));
        recyclerViewAlbums.setItemAnimator(new DefaultItemAnimator());
        mDisposable.add(LastAddedSongsLoader.getLastAddedAlbums(getContext())
                .subscribeOn(mProvider.io())
                .observeOn(mProvider.ui())
                .subscribe(albums -> {
                    if (albums.size() > 0) {
                        mViewGroups.get(0).setVisibility(View.VISIBLE);
                        recyclerViewAlbums.setAdapter(new AlbumAdapter(getMainActivity(), albums, R.layout.item_image, false, null));
                    }
                }));
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mDisposable = new CompositeDisposable();
        mProvider = new SchedulerProvider();
        mHomePresenter = new HomePresenter(Injection.provideRepository(getContext()), this);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        unbinder = ButterKnife.bind(this, view);
        return view;
    }

    @OnClick({R.id.history, R.id.last_added, R.id.top_tracks})
    void onClicks(View view) {
        switch (view.getId()) {
            case R.id.history:
                NavigationUtil.goToPlaylistNew(getMainActivity(), new HistoryPlaylist(getContext()));
                break;
            case R.id.last_added:
                NavigationUtil.goToPlaylistNew(getMainActivity(), new LastAddedPlaylist(getContext()));
                break;
            case R.id.top_tracks:
                NavigationUtil.goToPlaylistNew(getMainActivity(), new MyTopTracksPlaylist(getContext()));
                break;
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getMainActivity().getSlidingUpPanelLayout().setShadowHeight(8);
        setStatusbarColorAuto(view);
        getMainActivity().setTaskDescriptionColorAuto();
        getMainActivity().setNavigationbarColorAuto();
        getMainActivity().setBottomBarVisibility(View.GONE);
        getMainActivity().hideStatusBar();

        /*Adding margin to toolbar for !full screen mode*/
        if (!PreferenceUtil.getInstance(getContext()).getFullScreenMode()) {
            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) mToolbar.getLayoutParams();
            params.topMargin = getResources().getDimensionPixelOffset(R.dimen.status_bar_padding);
            mToolbar.setLayoutParams(params);
        }

        setupToolbar();
        setupRecyclerView();
        setupRecentArtists();
        setupRecentAlbums();

    }

    @SuppressWarnings("ConstantConditions")
    private void setupToolbar() {
        mAppbar.addOnOffsetChangedListener(new AppBarStateChangeListener() {
            @Override
            public void onStateChanged(AppBarLayout appBarLayout, State state) {
                int color;
                switch (state) {
                    case COLLAPSED:
                        getMainActivity().setLightStatusbar(true);
                        color = ATHUtil.resolveColor(getContext(), R.attr.iconColor);
                        break;
                    default:
                    case EXPANDED:
                    case IDLE:
                        getMainActivity().setLightStatusbar(false);
                        color = ContextCompat.getColor(getContext(), R.color.md_white_1000);
                        break;
                }
                mToolbarLayout.setExpandedTitleColor(color);
                ToolbarColorizeHelper.colorizeToolbar(mToolbar, color, getActivity());
            }

        });
        mToolbar.setTitle(R.string.home);
        mToolbar.setNavigationIcon(R.drawable.ic_menu_white_24dp);
        getActivity().setTitle(R.string.app_name);
        getMainActivity().setSupportActionBar(mToolbar);

        mTitle.setText(getTimeOfTheDay());
    }

    @SuppressWarnings("ConstantConditions")
    private void setupRecyclerView() {
        adapter = new PlaylistAdapter(getMainActivity(), new ArrayList<Playlist>(), R.layout.item_list, null);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(adapter);
    }

    @OnClick(R.id.search)
    void search(View view) {
        Activity activity = getMainActivity();
        ActivityOptionsCompat optionsCompat =
                ActivityOptionsCompat.makeSceneTransitionAnimation(activity, new Pair<>(view, getString(R.string.transition_search_bar)));
        startActivity(new Intent(activity, SearchActivity.class), optionsCompat.toBundle());

    }

    @Override
    public boolean handleBackPress() {
        return false;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mDisposable.clear();
        mHomePresenter.unsubscribe();
        unbinder.unbind();
    }

    @Override
    public void loading() {

    }

    @Override
    public void showEmptyView() {

    }

    @Override
    public void completed() {

    }

    @Override
    public void onResume() {
        super.onResume();
        mHomePresenter.subscribe();
    }

    @Override
    public void showList(ArrayList<Playlist> playlists) {
        adapter.swapDataSet(playlists);
    }

    @Override
    public void selectedFragment(Fragment fragment) {

    }

    private void loadTimeImage(String day) {
        Glide.with(getActivity()).load(day)
                .asBitmap()
                .placeholder(R.drawable.material_design_default)
                .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                .into(mImageView);
    }

    private String getTimeOfTheDay() {
        String message = getString(R.string.title_good_day);
        Calendar c = Calendar.getInstance();
        int timeOfDay = c.get(Calendar.HOUR_OF_DAY);

        String[] images = new String[]{};
        if (timeOfDay >= 0 && timeOfDay < 6) {
            message = getString(R.string.title_good_night);
            images = getResources().getStringArray(R.array.night);
        } else if (timeOfDay >= 6 && timeOfDay < 12) {
            message = getString(R.string.title_good_morning);
            images = getResources().getStringArray(R.array.morning);
        } else if (timeOfDay >= 12 && timeOfDay < 16) {
            message = getString(R.string.title_good_afternoon);
            images = getResources().getStringArray(R.array.after_noon);
        } else if (timeOfDay >= 16 && timeOfDay < 20) {
            message = getString(R.string.title_good_evening);
            images = getResources().getStringArray(R.array.evening);
        } else if (timeOfDay >= 20 && timeOfDay < 24) {
            message = getString(R.string.title_good_night);
            images = getResources().getStringArray(R.array.night);
        }
        String day = images[new Random().nextInt(images.length)];
        loadTimeImage(day);
        return message;
    }

    @Override
    public void onMediaStoreChanged() {
        super.onMediaStoreChanged();
        mHomePresenter.subscribe();
    }
}
