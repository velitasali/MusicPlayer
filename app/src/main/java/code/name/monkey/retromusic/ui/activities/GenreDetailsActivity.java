package code.name.monkey.retromusic.ui.activities;

import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ViewFlipper;

import com.bumptech.glide.Glide;
import com.kabouzeid.appthemehelper.ThemeStore;
import com.name.monkey.retromusic.ui.activities.base.AbsSlidingMusicPanelActivity;
import com.retro.musicplayer.backend.Injection;
import com.retro.musicplayer.backend.model.Genre;
import com.retro.musicplayer.backend.model.Song;
import com.retro.musicplayer.backend.mvp.contract.GenreDetailsContract;
import com.retro.musicplayer.backend.mvp.presenter.GenreDetailsPresenter;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import code.name.monkey.retromusic.R;
import code.name.monkey.retromusic.RetroApplication;
import code.name.monkey.retromusic.helper.MusicPlayerRemote;
import code.name.monkey.retromusic.ui.adapter.song.SongAdapter;
import code.name.monkey.retromusic.util.ToolbarColorizeHelper;
import code.name.monkey.retromusic.util.Util;
import code.name.monkey.retromusic.util.ViewUtil;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

/**
 * @author Hemanth S (h4h13).
 */

public class GenreDetailsActivity extends AbsSlidingMusicPanelActivity
        implements GenreDetailsContract.GenreDetailsView {
    public static final String EXTRA_GENRE_ID = "extra_genre_id";
    @BindView(R.id.recycler_view)
    RecyclerView mRecyclerView;
    @BindView(R.id.status_bar)
    View statusBar;
    @BindView(R.id.toolbar)
    Toolbar mToolbar;
    @BindView(R.id.image)
    ViewFlipper image;
    @BindView(R.id.play_songs)
    Button playSongs;
    @BindView(R.id.action_shuffle_all)
    Button shuffleSongs;
    private Genre mGenre;
    private GenreDetailsPresenter mPresenter;
    private SongAdapter mSongAdapter;
    private ArrayList<Song> mSongs = new ArrayList<>();
    private CompositeDisposable mDisposable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setDrawUnderStatusbar(true);
        super.onCreate(savedInstanceState);
        ButterKnife.bind(this);

        mDisposable = new CompositeDisposable();

        setBottomBarVisibility(View.GONE);

        ViewUtil.setStatusBarHeight(this, statusBar);


        mGenre = getIntent().getExtras().getParcelable(EXTRA_GENRE_ID);
        mPresenter = new GenreDetailsPresenter(Injection.provideRepository(this),
                this,
                mGenre.id);


        setUpToolBar();
        setupRecyclerView();


        int themeColor = ThemeStore.accentColor(this);
        ViewCompat.setBackgroundTintList(playSongs, ColorStateList.valueOf(themeColor));
        shuffleSongs.setTextColor(themeColor);
    }

    @OnClick({R.id.action_shuffle_all, R.id.play_songs})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.action_shuffle_all:
                MusicPlayerRemote.openAndShuffleQueue(mSongs, true);
                break;
            case R.id.play_songs:
                MusicPlayerRemote.openQueue(mSongs, 0, true);
                break;
        }
    }

    private void setUpToolBar() {
        mToolbar.setTitle(mGenre.name);
        mToolbar.setSubtitle(mGenre.songCount + " " + (mGenre.songCount > 1 ? getString(R.string.songs) : getString(R.string.song)));
        mToolbar.setNavigationOnClickListener(v -> onBackPressed());
        mToolbar.setBackgroundColor(ThemeStore.primaryColor(this));
        setSupportActionBar(mToolbar);
        setTitle(R.string.app_name);


        new Handler().postDelayed(() -> ToolbarColorizeHelper.colorizeToolbar(mToolbar,
                ThemeStore.accentColor(this), GenreDetailsActivity.this), 1);


    }

    @Override
    protected void onResume() {
        super.onResume();
        mPresenter.subscribe();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mPresenter.unsubscribe();
    }

    @Override
    protected View createContentView() {
        return wrapSlidingMusicPanel(R.layout.activity_genre_details);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        return false;
    }

    @Override
    public void loading() {

    }

    @Override
    public void showEmptyView() {

    }

    @Override
    public void completed() {
        loadImages();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    private void setupRecyclerView() {
        mSongAdapter = new SongAdapter(this, new ArrayList<>(), R.layout.item_list, false, null);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setAdapter(mSongAdapter);
        mRecyclerView.setNestedScrollingEnabled(false);
    }

    @Override
    public void showList(ArrayList<Song> songs) {
        mSongs = songs;
        mSongAdapter.swapDataSet(mSongs);
    }

    private void loadImages() {
        mDisposable.add(Observable.just(mSongs)
                .map(songs -> {
                    ArrayList<Bitmap> bitmaps = new ArrayList<>();
                    for (Song song : songs) {
                        try {
                            Bitmap bitmap = Glide.with(RetroApplication.getInstance())
                                    .load(Util.getAlbumArtUri(song.albumId))
                                    .asBitmap()
                                    .into(500, 500)
                                    .get();
                            if (bitmap != null) {
                                bitmaps.add(bitmap);
                            }
                        } catch (InterruptedException | ExecutionException ex) {
                            ex.printStackTrace();
                        }
                    }
                    return bitmaps;
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(bitmaps -> {
                    for (Bitmap bitmap : bitmaps) {
                        ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.MATCH_PARENT);

                        ImageView imageView = new ImageView(GenreDetailsActivity.this);
                        imageView.setLayoutParams(layoutParams);
                        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                        imageView.setImageBitmap(bitmap);
                        image.addView(imageView);
                    }
                    // Declare in and out animations and load them using AnimationUtils class
                    Animation in = AnimationUtils.loadAnimation(this, android.R.anim.slide_in_left);
                    Animation out = AnimationUtils.loadAnimation(this, android.R.anim.slide_out_right);
                    // set the animation type's to ViewFlipper
                    image.setInAnimation(in);
                    image.setOutAnimation(out);
                    image.startFlipping();
                    image.setAutoStart(true);
                    image.setFlipInterval(5000);
                }));
    }
}
