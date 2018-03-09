package code.name.monkey.retromusic.ui.fragments.mainactivity.home;

import android.app.Activity;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Random;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import code.name.monkey.appthemehelper.ThemeStore;
import code.name.monkey.appthemehelper.util.ATHUtil;
import code.name.monkey.appthemehelper.util.ColorUtil;
import code.name.monkey.appthemehelper.util.ToolbarContentTintHelper;
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
import com.velitasali.music.R;
import code.name.monkey.retromusic.dialogs.SleepTimerDialog;
import code.name.monkey.retromusic.helper.MusicPlayerRemote;
import code.name.monkey.retromusic.misc.AppBarStateChangeListener;
import code.name.monkey.retromusic.ui.activities.SearchActivity;
import code.name.monkey.retromusic.ui.adapter.album.AlbumAdapter;
import code.name.monkey.retromusic.ui.adapter.artist.ArtistAdapter;
import code.name.monkey.retromusic.ui.adapter.song.SongAdapter;
import code.name.monkey.retromusic.ui.fragments.base.AbsMainActivityFragment;
import code.name.monkey.retromusic.util.Compressor;
import code.name.monkey.retromusic.util.NavigationUtil;
import code.name.monkey.retromusic.util.PreferenceUtil;
import de.hdodenhof.circleimageview.CircleImageView;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

import static code.name.monkey.backend.RetroConstants.USER_BANNER;
import static code.name.monkey.backend.RetroConstants.USER_PROFILE;

public class HomeFragment extends AbsMainActivityFragment implements MainActivityFragmentCallbacks, HomeContract.HomeView {
    private static final String TAG = "HomeFragment";
    private static final int PICK_IMAGE_REQUEST = 1;

    Unbinder unbinder;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.appbar)
    AppBarLayout appbar;
    @BindView(R.id.image)
    ImageView imageView;
    @BindView(R.id.collapsing_toolbar)
    CollapsingToolbarLayout toolbarLayout;
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
    @BindView(R.id.user_image)
    CircleImageView userImage;
    @BindView(R.id.title)
    AppCompatTextView title;
    @BindView(R.id.title_welcome)
    AppCompatTextView titleWelcome;
    @BindView(R.id.search)
    LinearLayout search;
    @BindView(R.id.search_text)
    @Nullable
    View searchText;

    private HomePresenter homePresenter;
    private CompositeDisposable disposable;

    public static HomeFragment newInstance() {
        Bundle args = new Bundle();
        HomeFragment fragment = new HomeFragment();
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        disposable = new CompositeDisposable();
        homePresenter = new HomePresenter(Injection.provideRepository(getContext()), this);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        unbinder = ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getMainActivity().getSlidingUpPanelLayout().setShadowHeight(8);
        setStatusbarColorAuto(view);

        getMainActivity().setBottomBarVisibility(View.GONE);

        setupToolbar();

        loadImageFromStorage();
    }

    @SuppressWarnings("ConstantConditions")
    private void setupToolbar() {
         /*Adding margin to toolbar for !full screen mode*/
        if (!PreferenceUtil.getInstance(getContext()).getFullScreenMode()) {
            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) toolbar.getLayoutParams();
            params.topMargin = getResources().getDimensionPixelOffset(R.dimen.status_bar_padding);
            toolbar.setLayoutParams(params);
        }
        appbar.addOnOffsetChangedListener(new AppBarStateChangeListener() {
            @Override
            public void onStateChanged(AppBarLayout appBarLayout, State state) {

                int color;
                switch (state) {
                    case COLLAPSED:
                        getMainActivity().setLightStatusbar(!ATHUtil.isWindowBackgroundDark(getContext()));
                        color = ATHUtil.resolveColor(getContext(), R.attr.iconColor);
                        if (searchText != null) {
                            searchText.setVisibility(View.GONE);
                        }
                        break;
                    default:
                    case EXPANDED:
                    case IDLE:
                        getMainActivity().setLightStatusbar(false);
                        color = ContextCompat.getColor(getContext(), R.color.md_white_1000);
                        if (searchText != null) {
                            searchText.setVisibility(View.VISIBLE);
                        }
                        break;
                }
                toolbarLayout.setExpandedTitleColor(color);
                title.setTextColor(color);
                ToolbarContentTintHelper.colorizeToolbar(toolbar, color, getActivity());
            }

        });
        toolbar.setTitle(R.string.home);
        toolbar.setNavigationIcon(R.drawable.ic_menu_white_24dp);
        getActivity().setTitle(R.string.app_name);
        getMainActivity().setSupportActionBar(toolbar);

        titleWelcome.setText(getTimeOfTheDay());
        title.setText(PreferenceUtil.getInstance(getContext()).getUserName());
        title.setTextColor(ThemeStore.textColorPrimary(getContext()));

        search.setBackgroundTintList(ColorStateList.valueOf(ColorUtil.withAlpha(ThemeStore.textColorPrimary(getActivity()), 0.2f)));
    }

    private void loadTimeImage(String day) {
        if (PreferenceUtil.getInstance(getActivity()).getBannerImage().isEmpty()) {
            Glide.with(getActivity()).load(day)
                    .asBitmap()
                    .placeholder(R.drawable.material_design_default)
                    .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                    .into(imageView);
        } else {
            loadBannerFromStorage();
        }

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

    @OnClick(R.id.search)
    void search(View view) {
        Activity activity = getMainActivity();
        startActivity(new Intent(activity, SearchActivity.class));

    }


    @Override
    public boolean handleBackPress() {
        return false;
    }

    @Override
    public void selectedFragment(Fragment fragment) {

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        disposable.clear();
        homePresenter.unsubscribe();
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
        homePresenter.subscribe();
    }

    @Override
    public void showData(ArrayList<Object> homes) {
        //homeAdapter.swapDataSet(homes);
    }

    @Override
    public void onMediaStoreChanged() {
        super.onMediaStoreChanged();
        homePresenter.subscribe();
    }

    @Override
    public void recentArtist(ArrayList<Artist> artists) {
        recentArtistContainer.setVisibility(View.VISIBLE);
        recentArtistRV.setLayoutManager(new GridLayoutManager(getMainActivity(),
                1, GridLayoutManager.HORIZONTAL, false));
        ArtistAdapter artistAdapter = new ArtistAdapter(getMainActivity(), artists, R.layout.item_artist, false, null);
        recentArtistRV.setAdapter(artistAdapter);

    }

    @Override
    public void recentAlbum(ArrayList<Album> albums) {
        recentAlbumsContainer.setVisibility(View.VISIBLE);
        recentAlbumRV.setLayoutManager(new GridLayoutManager(getMainActivity(),
                1, GridLayoutManager.HORIZONTAL, false));
        AlbumAdapter artistAdapter = new AlbumAdapter(getMainActivity(), albums, R.layout.pager_item, false, null);
        recentAlbumRV.setAdapter(artistAdapter);

    }

    @Override
    public void topArtists(ArrayList<Artist> artists) {
        topArtistContainer.setVisibility(View.VISIBLE);
        topArtistRV.setLayoutManager(new GridLayoutManager(getMainActivity(),
                1, GridLayoutManager.HORIZONTAL, false));
        ArtistAdapter artistAdapter = new ArtistAdapter(getMainActivity(), artists, R.layout.item_artist, false, null);
        topArtistRV.setAdapter(artistAdapter);

    }

    @Override
    public void topAlbums(ArrayList<Album> albums) {
        topAlbumContainer.setVisibility(View.VISIBLE);
        topAlbumRV.setLayoutManager(new GridLayoutManager(getMainActivity(),
                1, GridLayoutManager.HORIZONTAL, false));
        AlbumAdapter artistAdapter = new AlbumAdapter(getMainActivity(), albums, R.layout.pager_item, false, null);
        topAlbumRV.setAdapter(artistAdapter);
    }

    @Override
    public void suggestions(ArrayList<Song> songs) {
        songsContainer.setVisibility(View.VISIBLE);
        songsRV.setLayoutManager(new GridLayoutManager(getMainActivity(),
                1, GridLayoutManager.HORIZONTAL, false));
        SongAdapter artistAdapter = new SongAdapter(getMainActivity(), songs, R.layout.item_image, false, null);
        songsRV.setAdapter(artistAdapter);
    }


    private void loadImageFromStorage() {
        new Compressor(getContext())
                .setMaxHeight(300)
                .setMaxWidth(300)
                .setQuality(75)
                .setCompressFormat(Bitmap.CompressFormat.WEBP)
                .compressToBitmapAsFlowable(new File(PreferenceUtil.getInstance(getContext()).getProfileImage(), USER_PROFILE))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(bitmap -> userImage.setImageBitmap(bitmap),
                        throwable -> userImage.setImageDrawable(ContextCompat
                                .getDrawable(getContext(), R.drawable.ic_person_flat)));

    }

    private void loadBannerFromStorage() {
        new Compressor(getContext())
                .setQuality(100)
                .setCompressFormat(Bitmap.CompressFormat.WEBP)
                .compressToBitmapAsFlowable(new File(PreferenceUtil.getInstance(getContext()).getBannerImage(), USER_BANNER))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(bitmap -> imageView.setImageBitmap(bitmap));

    }

    @OnClick({R.id.history, R.id.last_added, R.id.top_tracks, R.id.action_shuffle, R.id.timer})
    public void onViewClicked(View view) {
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