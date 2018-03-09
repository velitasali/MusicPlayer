package code.name.monkey.retromusic.ui.activities;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.transition.TransitionManager;
import android.support.v7.widget.AppCompatImageButton;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.Spanned;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.name.monkey.retromusic.ui.activities.base.AbsSlidingMusicPanelActivity;

import java.util.ArrayList;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import code.name.monkey.appthemehelper.ThemeStore;
import code.name.monkey.appthemehelper.util.ColorUtil;
import code.name.monkey.appthemehelper.util.TintHelper;
import code.name.monkey.backend.Injection;
import code.name.monkey.backend.helper.SortOrder.ArtistSongSortOrder;
import code.name.monkey.backend.model.Artist;
import code.name.monkey.backend.model.Song;
import code.name.monkey.backend.mvp.contract.ArtistDetailContract;
import code.name.monkey.backend.mvp.presenter.ArtistDetailsPresenter;
import code.name.monkey.backend.rest.LastFMRestClient;
import code.name.monkey.backend.rest.model.LastFmArtist;
import com.velitasali.music.R;
import code.name.monkey.retromusic.dialogs.AddToPlaylistDialog;
import code.name.monkey.retromusic.glide.ArtistGlideRequest;
import code.name.monkey.retromusic.glide.RetroMusicColoredTarget;
import code.name.monkey.retromusic.helper.MusicPlayerRemote;
import code.name.monkey.retromusic.ui.adapter.album.AlbumAdapter;
import code.name.monkey.retromusic.ui.adapter.song.SimpleSongAdapter;
import code.name.monkey.retromusic.util.CustomArtistImageUtil;
import code.name.monkey.retromusic.util.MusicUtil;
import code.name.monkey.retromusic.util.PreferenceUtil;
import code.name.monkey.retromusic.util.Util;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ArtistDetailActivity extends AbsSlidingMusicPanelActivity implements
        ArtistDetailContract.ArtistsDetailsView {
    public static final String EXTRA_ARTIST_ID = "extra_artist_id";
    private static final int REQUEST_CODE_SELECT_IMAGE = 9003;
    @BindView(R.id.image)
    ImageView image;
    @BindView(R.id.biography)
    TextView biographyTextView;
    @BindView(R.id.root)
    ViewGroup rootLayout;
    @BindView(R.id.status_bar)
    View statusBar;
    @BindView(R.id.recycler_view)
    RecyclerView recyclerView;
    @BindView(R.id.album_recycler_view)
    RecyclerView alnumRecyclerView;
    @BindView(R.id.album_title)
    AppCompatTextView albumTitle;
    @BindView(R.id.song_title)
    AppCompatTextView songTitle;
    @BindView(R.id.biography_title)
    AppCompatTextView biographyTitle;
    @BindView(R.id.title)
    TextView title;
    @BindView(R.id.text)
    TextView text;
    @BindView(R.id.menu_close)
    AppCompatImageButton close;
    @BindView(R.id.menu)
    AppCompatImageButton menu;
    @BindView(R.id.action_shuffle_all)
    FloatingActionButton shuffleButton;
    @Nullable
    private Spanned biography;
    private Artist artist;
    private LastFMRestClient lastFMRestClient;
    private ArtistDetailsPresenter artistDetailsPresenter;
    private SimpleSongAdapter songAdapter;
    private AlbumAdapter albumAdapter;
    private boolean forceDownload;

    private void setUpViews() {
        setupRecyclerView();
    }

    @Override
    protected void onCreate(Bundle bundle) {
        setDrawUnderStatusbar(true);
        super.onCreate(bundle);
        ButterKnife.bind(this);

        //supportPostponeEnterTransition();

        lastFMRestClient = new LastFMRestClient(this);

        setBottomBarVisibility(View.GONE);

        //ViewUtil.setStatusBarHeight(this, statusBar);

        setUpViews();

        int artistID = getIntent().getIntExtra(EXTRA_ARTIST_ID, -1);
        artistDetailsPresenter = new ArtistDetailsPresenter(Injection.provideRepository(this),
                this, artistID);

        ColorStateList colorState = ColorStateList.valueOf(ColorUtil.withAlpha(Color.BLACK, 0.2f));
        TintHelper.setTintAuto(close, Color.WHITE, false);
        menu.setBackgroundTintList(colorState);
        close.setBackgroundTintList(colorState);
    }

    private void setupRecyclerView() {

        albumAdapter = new AlbumAdapter(this, new ArrayList<>(), R.layout.item_image, false, null);
        alnumRecyclerView.setItemAnimator(new DefaultItemAnimator());
        alnumRecyclerView.setLayoutManager(new GridLayoutManager(this, 1, GridLayoutManager.HORIZONTAL, false));
        alnumRecyclerView.setAdapter(albumAdapter);


        songAdapter = new SimpleSongAdapter(this, new ArrayList<>(), R.layout.item_song);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(songAdapter);

    }

    @Override
    protected View createContentView() {
        return wrapSlidingMusicPanel(R.layout.activity_artist_details);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_CODE_SELECT_IMAGE:
                if (resultCode == RESULT_OK) {
                    CustomArtistImageUtil.getInstance(this).setCustomArtistImage(artist, data.getData());
                }
                break;
            default:
                if (resultCode == RESULT_OK) {
                    reload();
                }
                break;
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        return false;
    }

    @Override
    protected void onResume() {
        super.onResume();
        artistDetailsPresenter.subscribe();
    }

    @Override
    protected void onPause() {
        super.onPause();
        artistDetailsPresenter.unsubscribe();
    }

    @Override
    public void loading() {
    }

    @Override
    public void showEmptyView() {

    }

    @Override
    public void completed() {
        //supportStartPostponedEnterTransition();
    }

    @Override
    public void showData(Artist artist) {
        //supportStartPostponedEnterTransition();
        setArtist(artist);
    }

    private Artist getArtist() {
        if (artist == null) artist = new Artist();
        return artist;
    }

    private void setArtist(Artist artist) {
        this.artist = artist;
        loadArtistImage();

        if (Util.isAllowedToDownloadMetadata(this)) {
            loadBiography();
        }
        title.setText(artist.getName());
        text.setText(MusicUtil.getArtistInfoString(this, artist));

        songAdapter.swapDataSet(artist.getSongs());
        albumAdapter.swapDataSet(artist.albums);
    }

    private void loadBiography() {
        loadBiography(Locale.getDefault().getLanguage());
    }

    private void loadBiography(@Nullable final String lang) {
        biography = null;

        lastFMRestClient.getApiService()
                .getArtistInfo(getArtist().getName(), lang, null)
                .enqueue(new Callback<LastFmArtist>() {
                    @Override
                    public void onResponse(@NonNull Call<LastFmArtist> call, @NonNull Response<LastFmArtist> response) {
                        final LastFmArtist lastFmArtist = response.body();
                        if (lastFmArtist != null && lastFmArtist.getArtist() != null) {
                            final String bioContent = lastFmArtist.getArtist().getBio().getContent();
                            if (bioContent != null && !bioContent.trim().isEmpty()) {
                                //TransitionManager.beginDelayedTransition(titleContainer);
                                biographyTextView.setVisibility(View.VISIBLE);
                                biography = Html.fromHtml(bioContent);
                                biographyTextView.setText(biography);
                            }
                        }

                        // If the "lang" parameter is set and no biography is given, retry with default language
                        if (biography == null && lang != null) {
                            loadBiography(null);
                            return;
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<LastFmArtist> call, @NonNull Throwable t) {
                        t.printStackTrace();
                        biography = null;
                    }
                });
    }

    @OnClick(R.id.biography)
    void toggleArtistBiogrphy() {
        TransitionManager.beginDelayedTransition(rootLayout);
        if (biographyTextView.getMaxLines() == 4) {
            biographyTextView.setMaxLines(Integer.MAX_VALUE);
        } else {
            biographyTextView.setMaxLines(4);
        }
    }

    private void loadArtistImage() {
        ArtistGlideRequest.Builder.from(Glide.with(this), artist)
                .forceDownload(forceDownload)
                .generatePalette(this).build()
                .dontAnimate()
                .into(new RetroMusicColoredTarget(image) {
                    @Override
                    public void onColorReady(int color) {
                        setColors(color);
                    }
                });
        forceDownload = false;
    }

    private void setColors(int color) {
        int themeColor = PreferenceUtil.getInstance(this).getAdaptiveColor() ? color : ThemeStore.accentColor(this);

        albumTitle.setTextColor(themeColor);
        songTitle.setTextColor(themeColor);
        biographyTitle.setTextColor(themeColor);

        TintHelper.setTintAuto(shuffleButton, themeColor, true);
    }

    @OnClick({R.id.action_shuffle_all, R.id.menu_close, R.id.menu})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.menu_close:
                onBackPressed();
                break;
            case R.id.menu:
                PopupMenu popupMenu = new PopupMenu(this, view);
                popupMenu.getMenuInflater().inflate(R.menu.menu_artist_detail, popupMenu.getMenu());
                popupMenu.setOnMenuItemClickListener(this::onOptionsItemSelected);
                popupMenu.show();
                break;
            case R.id.action_shuffle_all:
                MusicPlayerRemote.openAndShuffleQueue(getArtist().getSongs(), true);
                break;
        }
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return handleSortOrderMenuItem(item);
    }

    private boolean handleSortOrderMenuItem(@NonNull MenuItem item) {
        String sortOrder = null;
        final ArrayList<Song> songs = getArtist().getSongs();
        switch (item.getItemId()) {
            case R.id.action_play_next:
                MusicPlayerRemote.playNext(songs);
                return true;
            case R.id.action_add_to_current_playing:
                MusicPlayerRemote.enqueue(songs);
                return true;
            case R.id.action_add_to_playlist:
                AddToPlaylistDialog.create(songs).show(getSupportFragmentManager(), "ADD_PLAYLIST");
                return true;
            case android.R.id.home:
                super.onBackPressed();
                return true;
            case R.id.action_sort_order_title:
                sortOrder = ArtistSongSortOrder.SONG_A_Z;
                break;
            case R.id.action_sort_order_title_desc:
                sortOrder = ArtistSongSortOrder.SONG_Z_A;
                break;
            case R.id.action_sort_order_album:
                sortOrder = ArtistSongSortOrder.SONG_ALBUM;
                break;
            case R.id.action_sort_order_year:
                sortOrder = ArtistSongSortOrder.SONG_YEAR;
                break;
            case R.id.action_sort_order_artist_song_duration:
                sortOrder = ArtistSongSortOrder.SONG_DURATION;
                break;
            case R.id.action_sort_order_date:
                sortOrder = ArtistSongSortOrder.SONG_DATE;
                break;
            case R.id.action_set_artist_image:
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivityForResult(Intent.createChooser(intent, getString(R.string.pick_from_local_storage)), REQUEST_CODE_SELECT_IMAGE);
                return true;
            case R.id.action_reset_artist_image:
                Toast.makeText(ArtistDetailActivity.this, getResources().getString(R.string.updating), Toast.LENGTH_SHORT).show();
                CustomArtistImageUtil.getInstance(ArtistDetailActivity.this).resetCustomArtistImage(artist);
                forceDownload = true;
                return true;
        }
        if (sortOrder != null) {
            item.setChecked(true);
            setSaveSortOrder(sortOrder);
        }
        return true;
    }

    private String getSavedSortOrder() {
        return PreferenceUtil.getInstance(this).getAlbumDetailSongSortOrder();
    }

    private void setUpSortOrderMenu(@NonNull SubMenu sortOrder) {
        switch (getSavedSortOrder()) {
            case ArtistSongSortOrder.SONG_A_Z:
                sortOrder.findItem(R.id.action_sort_order_title).setChecked(true);
                break;
            case ArtistSongSortOrder.SONG_Z_A:
                sortOrder.findItem(R.id.action_sort_order_title_desc).setChecked(true);
                break;
            case ArtistSongSortOrder.SONG_ALBUM:
                sortOrder.findItem(R.id.action_sort_order_album).setChecked(true);
                break;
            case ArtistSongSortOrder.SONG_YEAR:
                sortOrder.findItem(R.id.action_sort_order_year).setChecked(true);
                break;
            case ArtistSongSortOrder.SONG_DURATION:
                sortOrder.findItem(R.id.action_sort_order_artist_song_duration).setChecked(true);
                break;
            case ArtistSongSortOrder.SONG_DATE:
                sortOrder.findItem(R.id.action_sort_order_date).setChecked(true);
                break;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_artist_detail, menu);
        menu.removeItem(R.id.action_sort_order);
        //setUpSortOrderMenu(sortOrder.getSubMenu());
        return true;
    }

    private void setSaveSortOrder(String sortOrder) {
        PreferenceUtil.getInstance(this).setArtistDetailSongSortOrder(sortOrder);
        reload();
    }

    @Override
    public void onMediaStoreChanged() {
        super.onMediaStoreChanged();
        reload();
    }

    private void reload() {
        artistDetailsPresenter.unsubscribe();
        artistDetailsPresenter.subscribe();
    }


}
