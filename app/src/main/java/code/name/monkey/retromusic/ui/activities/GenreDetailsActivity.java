package code.name.monkey.retromusic.ui.activities;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.afollestad.materialcab.MaterialCab;
import com.name.monkey.retromusic.ui.activities.base.AbsSlidingMusicPanelActivity;
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import code.name.monkey.appthemehelper.ThemeStore;
import code.name.monkey.appthemehelper.util.ATHUtil;
import code.name.monkey.appthemehelper.util.TintHelper;
import code.name.monkey.appthemehelper.util.ToolbarContentTintHelper;
import code.name.monkey.backend.Injection;
import code.name.monkey.backend.model.Genre;
import code.name.monkey.backend.model.Song;
import code.name.monkey.backend.mvp.contract.GenreDetailsContract;
import code.name.monkey.backend.mvp.presenter.GenreDetailsPresenter;
import com.velitasali.music.R;
import code.name.monkey.retromusic.helper.MusicPlayerRemote;
import code.name.monkey.retromusic.interfaces.CabHolder;
import code.name.monkey.retromusic.misc.AppBarStateChangeListener;
import code.name.monkey.retromusic.ui.adapter.song.SongAdapter;
import code.name.monkey.retromusic.util.RetroColorUtil;
import code.name.monkey.retromusic.util.ViewUtil;

/**
 * @author Hemanth S (h4h13).
 */

public class GenreDetailsActivity extends AbsSlidingMusicPanelActivity implements GenreDetailsContract.GenreDetailsView, CabHolder {
    public static final String EXTRA_GENRE_ID = "extra_genre_id";
    @BindView(R.id.recycler_view)
    RecyclerView recyclerView;

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @BindView(android.R.id.empty)
    TextView empty;

    @BindView(R.id.status_bar)
    View statusBar;

    @BindView(R.id.action_shuffle)
    FloatingActionButton shuffleButton;

    @BindView(R.id.progress_bar)
    ProgressBar progressBar;

    @BindView(R.id.app_bar)
    AppBarLayout appBarLayout;

    @BindView(R.id.collapsing_toolbar)
    CollapsingToolbarLayout toolbarLayout;

    private Genre genre;
    private GenreDetailsPresenter presenter;
    private SongAdapter songAdapter;
    private MaterialCab cab;

    private void checkIsEmpty() {
        empty.setVisibility(
                songAdapter.getItemCount() == 0 ? View.VISIBLE : View.GONE
        );
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setDrawUnderStatusbar(true);
        super.onCreate(savedInstanceState);
        ButterKnife.bind(this);

        setStatusbarColorAuto();
        setNavigationbarColorAuto();
        setTaskDescriptionColorAuto();

        setBottomBarVisibility(View.GONE);

        ViewUtil.setStatusBarHeight(this, statusBar);


        genre = getIntent().getExtras().getParcelable(EXTRA_GENRE_ID);
        presenter = new GenreDetailsPresenter(Injection.provideRepository(this),
                this,
                genre.id);

        setUpToolBar();
        setupRecyclerView();
    }

    @OnClick({R.id.action_shuffle})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.action_shuffle:
                MusicPlayerRemote.openAndShuffleQueue(songAdapter.getDataSet(), true);
                break;
        }
    }

    private void setUpToolBar() {
        toolbar.setBackgroundColor(ThemeStore.primaryColor(this));
        toolbar.setNavigationIcon(R.drawable.ic_close_white_24dp);
        setSupportActionBar(toolbar);
        //noinspection ConstantConditions
        getSupportActionBar().setTitle(genre.name);
        setTitle(R.string.app_name);

        appBarLayout.addOnOffsetChangedListener(new AppBarStateChangeListener() {
            @Override
            public void onStateChanged(AppBarLayout appBarLayout, AppBarStateChangeListener.State state) {
                int color;
                switch (state) {
                    default:
                    case COLLAPSED:
                    case EXPANDED:
                    case IDLE:
                        color = ATHUtil.resolveColor(GenreDetailsActivity.this, android.R.attr.textColorPrimary);
                        break;
                }
                toolbarLayout.setExpandedTitleColor(color);
                ToolbarContentTintHelper.colorizeToolbar(toolbar, color, GenreDetailsActivity.this);
            }
        });

        TintHelper.setTintAuto(shuffleButton, ThemeStore.accentColor(this), true);
    }

    @Override
    protected void onResume() {
        super.onResume();
        presenter.subscribe();
    }

    @Override
    protected void onPause() {
        super.onPause();
        presenter.unsubscribe();
    }

    @Override
    protected View createContentView() {
        return wrapSlidingMusicPanel(R.layout.activity_playlist_detail);
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

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    private void setupRecyclerView() {
        ViewUtil.setUpFastScrollRecyclerViewColor(this,
                ((FastScrollRecyclerView) recyclerView), ThemeStore.accentColor(this));
        songAdapter = new SongAdapter(this, new ArrayList<Song>(), R.layout.item_list, false, this);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(songAdapter);

        songAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onChanged() {
                super.onChanged();
                checkIsEmpty();
            }
        });
    }

    @Override
    public void showData(ArrayList<Song> songs) {
        songAdapter.swapDataSet(songs);
    }

    public void showHeartAnimation() {
        shuffleButton.clearAnimation();

        shuffleButton.setScaleX(0.9f);
        shuffleButton.setScaleY(0.9f);
        shuffleButton.setVisibility(View.VISIBLE);
        shuffleButton.setPivotX(shuffleButton.getWidth() / 2);
        shuffleButton.setPivotY(shuffleButton.getHeight() / 2);

        shuffleButton.animate()
                .setDuration(200)
                .setInterpolator(new DecelerateInterpolator())
                .scaleX(1.1f)
                .scaleY(1.1f)
                .withEndAction(() -> shuffleButton.animate()
                        .setDuration(200)
                        .setInterpolator(new AccelerateInterpolator())
                        .scaleX(1f)
                        .scaleY(1f)
                        .alpha(1f)
                        .start())
                .start();
    }

    @NonNull
    @Override
    public MaterialCab openCab(final int menu, final MaterialCab.Callback callback) {
        if (cab != null && cab.isActive()) cab.finish();
        cab = new MaterialCab(this, R.id.cab_stub)
                .setMenu(menu)
                .setCloseDrawableRes(R.drawable.ic_close_white_24dp)
                .setBackgroundColor(RetroColorUtil.shiftBackgroundColorForLightText(ThemeStore.primaryColor(this)))
                .start(callback);
        return cab;
    }

    @Override
    public void onBackPressed() {
        if (cab != null && cab.isActive()) cab.finish();
        else {
            recyclerView.stopScroll();
            super.onBackPressed();
        }
    }

    @Override
    public void onMediaStoreChanged() {
        super.onMediaStoreChanged();
        presenter.subscribe();
    }
}
