package code.name.monkey.retromusic.ui.activities;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.kabouzeid.appthemehelper.ThemeStore;
import com.name.monkey.retromusic.ui.activities.base.AbsSlidingMusicPanelActivity;
import com.retro.musicplayer.backend.Injection;
import com.retro.musicplayer.backend.helper.SortOrder.AlbumSongSortOrder;
import com.retro.musicplayer.backend.model.Album;
import com.retro.musicplayer.backend.model.Song;
import com.retro.musicplayer.backend.mvp.contract.AlbumDetailsContract;
import com.retro.musicplayer.backend.mvp.presenter.AlbumDetailsPresenter;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
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
import code.name.monkey.retromusic.util.ToolbarColorizeHelper;
import code.name.monkey.retromusic.util.ViewUtil;

/**
 * Created by hemanths on 20/08/17.
 */

public class AlbumDetailsActivity extends AbsSlidingMusicPanelActivity implements AlbumDetailsContract.AlbumDetailsView {
    public static final String EXTRA_ALBUM_ID = "extra_album_id";
    private static final int TAG_EDITOR_REQUEST = 2001;
    @BindView(R.id.toolbar)
    Toolbar mToolbar;
    @BindView(R.id.image)
    ImageView image;
    @BindView(R.id.recycler_view)
    RecyclerView mRecyclerView;
    @BindView(R.id.play_songs)
    AppCompatButton playSongs;
    @BindView(R.id.action_shuffle_all)
    AppCompatButton shuffleSongs;
    @BindView(R.id.status_bar)
    View statusBar;
    @BindView(R.id.container)
    ViewGroup mContainer;
    @BindView(R.id.root)
    ViewGroup mViewGroup;
    private AlbumDetailsPresenter mAlbumDetailsPresenter;
    private Album mAlbum;
    private SimpleSongAdapter mAdapter;


    @Override
    protected View createContentView() {
        return wrapSlidingMusicPanel(R.layout.activity_album);
    }

    @OnClick({R.id.action_shuffle_all, R.id.play_songs})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.action_shuffle_all:
                MusicPlayerRemote.openAndShuffleQueue(mAlbum.songs, true);
                break;
            case R.id.play_songs:
                //showHeartAnimation();
                MusicPlayerRemote.openQueue(mAlbum.songs, 0, true);
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

        ViewUtil.setStatusBarHeight(this, statusBar);

        setUpToolBar();
        supportPostponeEnterTransition();
        mAlbumDetailsPresenter = new AlbumDetailsPresenter(Injection.provideRepository(this), this, getIntent().getIntExtra(EXTRA_ALBUM_ID, -1));
    }

    @Override
    protected void onResume() {
        super.onResume();
        mAlbumDetailsPresenter.subscribe();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mAlbumDetailsPresenter.unsubscribe();
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
        mAlbum = album;

        mToolbar.setTitle(album.getTitle());
        mToolbar.setSubtitle(album.getArtistName());

        loadAlbumCover();
        mAdapter = new SimpleSongAdapter(this, mAlbum.songs, R.layout.item_song);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setNestedScrollingEnabled(false);

    }

    public Album getAlbum() {
        return mAlbum;
    }

    private void setUpToolBar() {
        mToolbar.setTitle("");
        mToolbar.setNavigationOnClickListener(v -> onBackPressed());
        mToolbar.setBackgroundColor(ThemeStore.primaryColor(this));
        setSupportActionBar(mToolbar);
        setTitle(R.string.app_name);

    }

    private void loadAlbumCover() {
        SongGlideRequest.Builder.from(Glide.with(this), getAlbum().safeGetFirstSong())
                .checkIgnoreMediaStore(this)
                .generatePalette(this).build()
                .dontAnimate()
                .listener(new RequestListener<Object, BitmapPaletteWrapper>() {
                    @Override
                    public boolean onException(Exception e, Object model, Target<BitmapPaletteWrapper> target, boolean isFirstResource) {
                        supportStartPostponedEnterTransition();
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(BitmapPaletteWrapper resource, Object model, Target<BitmapPaletteWrapper> target, boolean isFromMemoryCache, boolean isFirstResource) {
                        supportStartPostponedEnterTransition();
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
        new Handler().postDelayed(() -> ToolbarColorizeHelper.colorizeToolbar(mToolbar, PreferenceUtil.getInstance(this).getAdaptiveColor() ? color : ThemeStore.accentColor(this), AlbumDetailsActivity.this), 1);


        int themeColor = PreferenceUtil.getInstance(this).getAdaptiveColor() ? color : ThemeStore.accentColor(this);
        playSongs.setSupportBackgroundTintList(ColorStateList.valueOf(themeColor));
        //ViewCompat.setBackgroundTintList(playSongs, ColorStateList.valueOf(themeColor));
        shuffleSongs.setTextColor(themeColor);

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
        final ArrayList<Song> songs = mAdapter.getDataSet();
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
        mAlbumDetailsPresenter.subscribe();
    }
}