package code.name.monkey.retromusic.ui.activities;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.TextView;
import android.widget.Toast;

import com.kabouzeid.appthemehelper.ThemeStore;
import com.retro.musicplayer.backend.model.Song;
import com.retro.musicplayer.backend.model.lyrics.Lyrics;
import com.retro.musicplayer.backend.providers.RepositoryImpl;
import com.retro.musicplayer.backend.providers.interfaces.Repository;
import com.retro.musicplayer.backend.util.LyricUtil;

import java.io.File;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import code.name.monkey.retromusic.R;
import code.name.monkey.retromusic.helper.MusicPlayerRemote;
import code.name.monkey.retromusic.helper.MusicProgressViewUpdateHelper;
import code.name.monkey.retromusic.ui.activities.base.AbsMusicServiceActivity;
import code.name.monkey.retromusic.util.MusicUtil;
import code.name.monkey.retromusic.views.LyricView;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public class LyricsActivity extends AbsMusicServiceActivity
        implements MusicProgressViewUpdateHelper.Callback {

    @BindView(R.id.title)
    TextView mTitle;
    @BindView(R.id.text)
    TextView mText;
    @BindView(R.id.lyrics)
    LyricView mLyricView;
    @BindView(R.id.toolbar)
    Toolbar mToolbar;
    @BindView(R.id.offline_lyrics)
    TextView mOfflineLyrics;
    @BindView(R.id.lyrics_container)
    View mLyricsContainer;
    @BindView(R.id.refresh)
    AppCompatImageView mRefresh;
    private MusicProgressViewUpdateHelper mUpdateHelper;
    private AsyncTask updateLyricsAsyncTask;
    private Repository loadLyrics;
    private CompositeDisposable mDisposable;
    private int fontSize = 20;
    private RotateAnimation rotateAnimation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lyrics);
        ButterKnife.bind(this);

        setStatusbarColorAuto();
        setNavigationbarColorAuto();
        setTaskDescriptionColorAuto();

        mUpdateHelper = new MusicProgressViewUpdateHelper(this, 500, 1000);
        loadLyrics = new RepositoryImpl(this);

        setupToolbar();
        setupLyricsView();
        setupWakelock();

    }

    private void setupWakelock() {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    private void setupLyricsView() {
        mDisposable = new CompositeDisposable();
        //mLyricView.setLineSpace(15.0f);
        mLyricView.setTextSize(fontSize);
        //mLyricView.setTranslationY(DensityUtil.getScreenWidth(getActivity()) + DensityUtil.dip2px(getActivity(), 120));
        mLyricView.setOnPlayerClickListener(new LyricView.OnPlayerClickListener() {
            @Override
            public void onPlayerClicked(long progress, String content) {
                MusicPlayerRemote.seekTo((int) progress);
            }
        });
        rotate();
        //mLyricView.setTouchable(true);
        //mLyricView.setPlayable(true);

        //mLyricView.setDefaultColor(Color.WHITE);
    }

    private void setupToolbar() {
        mToolbar.setBackgroundColor(ThemeStore.primaryColor(this));
        mToolbar.setTitle("");
        mToolbar.setNavigationOnClickListener(v -> onBackPressed());
        setSupportActionBar(mToolbar);
    }

    @Override
    public void onPlayingMetaChanged() {
        super.onPlayingMetaChanged();
        loadLrcFile();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mUpdateHelper.start();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mUpdateHelper.stop();
    }

    @Override
    public void onServiceConnected() {
        super.onServiceConnected();
        loadLrcFile();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mDisposable.clear();
        mLyricView.setOnPlayerClickListener(null);

        if (updateLyricsAsyncTask != null && !updateLyricsAsyncTask.isCancelled()) {
            updateLyricsAsyncTask.cancel(true);
        }
    }

    private void loadLrcFile() {
        Song song = MusicPlayerRemote.getCurrentSong();
        String title = song.title;
        String artist = song.artistName;

        mTitle.setText(title);
        mText.setText(artist);

        mLyricView.reset();

        if (LyricUtil.isLrcFileExist(title, artist)) {
            showLyricsLocal(LyricUtil.getLocalLyricFile(title, artist));
        } else {
            callAgain(title, artist);
        }

    }

    private void callAgain(String title, String artist) {
        mDisposable.clear();
        mDisposable.add(loadLyrics.downloadLrcFile(title, artist, MusicPlayerRemote.getSongDurationMillis())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .doOnSubscribe(disposable -> {
                    mRefresh.startAnimation(rotateAnimation);
                })
                .subscribe(this::showLyricsLocal,
                        throwable -> {
                            mRefresh.clearAnimation();
                            showLyricsLocal(null);
                        }, () -> {
                            mRefresh.clearAnimation();
                            Toast.makeText(this, "Lyrics downloaded", Toast.LENGTH_SHORT).show();
                        }));
    }

    private void showLyricsLocal(File file) {
        if (file == null) {
            mLyricView.reset();
            loadSongLyrics();
            hideLyrics();
        } else {
            mLyricView.setLyricFile(file, "UTF-8");
        }
    }

    private void hideLyrics() {
        mLyricsContainer.setVisibility(View.GONE);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onUpdateProgressViews(int progress, int total) {
        mLyricView.setCurrentTimeMillis(progress);
    }

    @SuppressLint("StaticFieldLeak")
    private void loadSongLyrics() {
        if (updateLyricsAsyncTask != null) updateLyricsAsyncTask.cancel(false);
        final Song song = MusicPlayerRemote.getCurrentSong();
        updateLyricsAsyncTask = new AsyncTask<Void, Void, Lyrics>() {
            @Override
            protected Lyrics doInBackground(Void... params) {
                String data = MusicUtil.getLyrics(song);
                if (TextUtils.isEmpty(data)) {
                    return null;
                }
                return Lyrics.parse(song, data);
            }

            @Override
            protected void onPostExecute(Lyrics l) {
                mOfflineLyrics.setVisibility(View.VISIBLE);
                if (l == null) {
                    return;
                }
                mOfflineLyrics.setText(l.data);
            }

            @Override
            protected void onCancelled(Lyrics s) {
                onPostExecute(null);
            }
        }.execute();
    }

    private void rotate() {
        rotateAnimation = new RotateAnimation(0, 180, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        rotateAnimation.setDuration(600);
        rotateAnimation.setInterpolator(new LinearInterpolator());

    }

    @OnClick({R.id.align_left,
            R.id.align_center,
            R.id.align_right,
            R.id.refresh,
            R.id.dec_font_size,
            R.id.inc_font_size})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.refresh:
                Song song = MusicPlayerRemote.getCurrentSong();
                String title = song.title;
                String artist = song.artistName;
                mLyricView.reset();
                LyricUtil.deleteLrcFile(title, artist);
                callAgain(title, artist);
                break;
            case R.id.align_left:
                //mLyricView.setTextAlign(LyricView.LEFT);
                break;
            case R.id.align_center:
                // mLyricView.setTextAlign(LyricView.CENTER);
                break;
            case R.id.align_right:
                //mLyricView.setTextAlign(LyricView.RIGHT);
                break;
            case R.id.dec_font_size:
                fontSize--;
                mLyricView.setTextSize(fontSize);
                break;
            case R.id.inc_font_size:
                fontSize++;
                mLyricView.setTextSize(fontSize);
                break;
        }
    }
}
