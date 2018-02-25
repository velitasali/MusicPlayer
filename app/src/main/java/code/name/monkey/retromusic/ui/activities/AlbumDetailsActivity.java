package code.name.monkey.retromusic.ui.activities;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.AppCompatImageButton;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.name.monkey.retromusic.ui.activities.base.AbsSlidingMusicPanelActivity;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import code.name.monkey.appthemehelper.ThemeStore;
import code.name.monkey.appthemehelper.util.ColorUtil;
import code.name.monkey.appthemehelper.util.TintHelper;
import code.name.monkey.backend.Injection;
import code.name.monkey.backend.helper.SortOrder.AlbumSongSortOrder;
import code.name.monkey.backend.model.Album;
import code.name.monkey.backend.model.Song;
import code.name.monkey.backend.mvp.contract.AlbumDetailsContract;
import code.name.monkey.backend.mvp.presenter.AlbumDetailsPresenter;
import code.name.monkey.retromusic.R;
import code.name.monkey.retromusic.dialogs.AddToPlaylistDialog;
import code.name.monkey.retromusic.dialogs.DeleteSongsDialog;
import code.name.monkey.retromusic.glide.RetroMusicColoredTarget;
import code.name.monkey.retromusic.glide.SongGlideRequest;
import code.name.monkey.retromusic.glide.palette.BitmapPaletteWrapper;
import code.name.monkey.retromusic.helper.MusicPlayerRemote;
import code.name.monkey.retromusic.ui.activities.tageditor.AbsTagEditorActivity;
import code.name.monkey.retromusic.ui.activities.tageditor.AlbumTagEditorActivity;
import code.name.monkey.retromusic.ui.adapter.song.SimpleSongAdapter;
import code.name.monkey.retromusic.util.NavigationUtil;
import code.name.monkey.retromusic.util.PreferenceUtil;
import code.name.monkey.retromusic.util.ViewUtil;

public class AlbumDetailsActivity extends AbsSlidingMusicPanelActivity implements AlbumDetailsContract.AlbumDetailsView {
    public static final String EXTRA_ALBUM_ID = "extra_album_id";
    private static final int TAG_EDITOR_REQUEST = 2001;
    @BindView(R.id.image)
    ImageView image;
    @BindView(R.id.recycler_view)
    RecyclerView recyclerView;

    @BindView(R.id.title)
    TextView title;
    @BindView(R.id.text)
    TextView text;
    @BindView(R.id.menu_close)
    AppCompatImageButton close;
    @BindView(R.id.menu)
    AppCompatImageButton menu;
    @BindView(R.id.song_title)
    AppCompatTextView songTitle;
    @BindView(R.id.action_shuffle_all)
    FloatingActionButton shuffleButton;
    private AlbumDetailsPresenter albumDetailsPresenter;
    private Album album;
    private SimpleSongAdapter adapter;

    @Override
    protected View createContentView() {
        return wrapSlidingMusicPanel(R.layout.activity_album);
    }

    @OnClick({R.id.action_shuffle_all, R.id.menu_close, R.id.menu})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.menu_close:
                onBackPressed();
                break;
            case R.id.menu:
                PopupMenu popupMenu = new PopupMenu(this, view);
                popupMenu.getMenuInflater().inflate(R.menu.menu_album_detail, popupMenu.getMenu());
                MenuItem sortOrder = popupMenu.getMenu().findItem(R.id.action_sort_order);
                setUpSortOrderMenu(sortOrder.getSubMenu());
                popupMenu.setOnMenuItemClickListener(this::onOptionsItemSelected);
                popupMenu.show();
                break;
            case R.id.action_shuffle_all:
                MusicPlayerRemote.openAndShuffleQueue(album.songs, true);
                break;
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        return false;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setDrawUnderStatusbar(true);
        super.onCreate(savedInstanceState);
        ButterKnife.bind(this);

        setBottomBarVisibility(View.GONE);

        albumDetailsPresenter = new AlbumDetailsPresenter(Injection.provideRepository(this),
                this, getIntent().getIntExtra(EXTRA_ALBUM_ID, -1));

        ColorStateList colorState = ColorStateList.valueOf(ColorUtil.withAlpha(ThemeStore.textColorPrimary(this), 0.2f));
        TintHelper.setTintAuto(close, Color.WHITE, false);
        menu.setBackgroundTintList(colorState);
        close.setBackgroundTintList(colorState);
    }

    @Override
    protected void onResume() {
        super.onResume();
        albumDetailsPresenter.subscribe();
    }

    @Override
    protected void onPause() {
        super.onPause();
        albumDetailsPresenter.unsubscribe();
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
    public void showData(Album album) {
        this.album = album;

        title.setText(album.getTitle());
        text.setText(String.format("%s%s", album.getArtistName(),
                album.getYear() == 0 ? "" : " • " + album.getYear()));

        loadAlbumCover();

        adapter = new SimpleSongAdapter(this, this.album.songs, R.layout.item_song);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(adapter);
        recyclerView.setNestedScrollingEnabled(false);
    }

    public Album getAlbum() {
        return album;
    }

    private void loadAlbumCover() {
        SongGlideRequest.Builder.from(Glide.with(this), getAlbum().safeGetFirstSong())
                .checkIgnoreMediaStore(this)
                .generatePalette(this).build()
                .dontAnimate()
                .listener(new RequestListener<Object, BitmapPaletteWrapper>() {
                    @Override
                    public boolean onException(Exception e, Object model,
                                               Target<BitmapPaletteWrapper> target,
                                               boolean isFirstResource) {
                        //supportStartPostponedEnterTransition();
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(BitmapPaletteWrapper resource, Object model,
                                                   Target<BitmapPaletteWrapper> target,
                                                   boolean isFromMemoryCache, boolean isFirstResource) {
                        //supportStartPostponedEnterTransition();
                        return false;
                    }
                })
                .into(new RetroMusicColoredTarget(image) {
                    @Override
                    public void onColorReady(int color) {
                        setColors(color);
                    }
                });
    }

    private void setColors(int color) {
        int themeColor = PreferenceUtil.getInstance(this).getAdaptiveColor() ? color : ThemeStore.accentColor(this);
        songTitle.setTextColor(themeColor);
        TintHelper.setTintAuto(shuffleButton, themeColor, true);
        //findViewById(R.id.gradient_background).setBackgroundTintList(ColorStateList.valueOf(themeColor));
        /*shuffleSongs.setTextColor(ColorUtil.isColorLight(themeColor) ?
                MaterialValueHelper.getPrimaryTextColor(this, true) :
                MaterialValueHelper.getPrimaryTextColor(this, false));*/
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_album_detail, menu);
        MenuItem sortOrder = menu.findItem(R.id.action_sort_order);
        setUpSortOrderMenu(sortOrder.getSubMenu());
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return handleSortOrderMenuItem(item);
    }

    private boolean handleSortOrderMenuItem(@NonNull MenuItem item) {
        String sortOrder = null;
        final ArrayList<Song> songs = adapter.getDataSet();
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
            case R.id.action_delete_from_device:
                DeleteSongsDialog.create(songs).show(getSupportFragmentManager(), "DELETE_SONGS");
                return true;
            case android.R.id.home:
                super.onBackPressed();
                return true;
            case R.id.action_tag_editor:
                Intent intent = new Intent(this, AlbumTagEditorActivity.class);
                intent.putExtra(AbsTagEditorActivity.EXTRA_ID, getAlbum().getId());
                startActivityForResult(intent, TAG_EDITOR_REQUEST);
                return true;
            case R.id.action_go_to_artist:
                NavigationUtil.goToArtist(this, getAlbum().getArtistId());
                return true;
            /*Sort*/
            case R.id.action_sort_order_title:
                sortOrder = AlbumSongSortOrder.SONG_A_Z;
                break;
            case R.id.action_sort_order_title_desc:
                sortOrder = AlbumSongSortOrder.SONG_Z_A;
                break;
            case R.id.action_sort_order_track_list:
                sortOrder = AlbumSongSortOrder.SONG_TRACK_LIST;
                break;
            case R.id.action_sort_order_artist_song_duration:
                sortOrder = AlbumSongSortOrder.SONG_DURATION;
                break;
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
            case AlbumSongSortOrder.SONG_A_Z:
                sortOrder.findItem(R.id.action_sort_order_title).setChecked(true);
                break;
            case AlbumSongSortOrder.SONG_Z_A:
                sortOrder.findItem(R.id.action_sort_order_title_desc).setChecked(true);
                break;
            case AlbumSongSortOrder.SONG_TRACK_LIST:
                sortOrder.findItem(R.id.action_sort_order_track_list).setChecked(true);
                break;
            case AlbumSongSortOrder.SONG_DURATION:
                sortOrder.findItem(R.id.action_sort_order_artist_song_duration).setChecked(true);
                break;
        }
    }

    private void setSaveSortOrder(String sortOrder) {
        PreferenceUtil.getInstance(this).setAlbumDetailSongSortOrder(sortOrder);
        reload();
    }

    @Override
    public void onMediaStoreChanged() {
        super.onMediaStoreChanged();
        reload();
    }

    private void reload() {
        albumDetailsPresenter.subscribe();
    }
}