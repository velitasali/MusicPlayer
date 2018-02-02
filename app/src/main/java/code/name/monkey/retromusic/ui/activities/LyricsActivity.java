package code.name.monkey.retromusic.ui.activities;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.transition.TransitionManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import code.name.monkey.appthemehelper.ThemeStore;
import code.name.monkey.backend.lyrics.ParseLyrics;
import code.name.monkey.backend.model.Song;
import code.name.monkey.backend.model.lyrics.Lyrics;
import code.name.monkey.backend.providers.RepositoryImpl;
import code.name.monkey.backend.providers.interfaces.Repository;
import code.name.monkey.backend.util.LyricUtil;
import code.name.monkey.retromusic.R;
import code.name.monkey.retromusic.helper.MusicPlayerRemote;
import code.name.monkey.retromusic.helper.MusicProgressViewUpdateHelper;
import code.name.monkey.retromusic.ui.activities.base.AbsMusicServiceActivity;
import code.name.monkey.retromusic.util.MusicUtil;
import code.name.monkey.retromusic.util.PreferenceUtil;
import code.name.monkey.retromusic.views.LyricView;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public class LyricsActivity extends AbsMusicServiceActivity implements MusicProgressViewUpdateHelper.Callback {

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
    @BindView(R.id.actions)
    LinearLayout mActions;
    private MusicProgressViewUpdateHelper mUpdateHelper;
    private AsyncTask updateLyricsAsyncTask;
    private Repository loadLyrics;
    private CompositeDisposable mDisposable;
    private float fontSize = 17.0f;
    private RotateAnimation rotateAnimation;
    private AsyncTask<String, Void, String> mTask;

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
        rotate();

    }

    private void setupWakelock() {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    private void setupLyricsView() {
        mDisposable = new CompositeDisposable();
        //mLyricView.setLineSpace(15.0f);
        //mLyricView.setTextSize(17.0f);
        //mLyricView.setPlayable(true);
        //mLyricView.setTranslationY(DensityUtil.getScreenWidth(this) + DensityUtil.dip2px(this, 120));
        mLyricView.setOnPlayerClickListener((progress, content) -> {
            MusicPlayerRemote.seekTo((int) progress);
            MusicPlayerRemote.pauseSong();
        });

        //mLyricView.setHighLightTextColor(ThemeStore.accentColor(this));
        mLyricView.setDefaultColor(ContextCompat.getColor(this, R.color.md_grey_400));
        //mLyricView.setTouchable(false);
        mLyricView.setHintColor(Color.WHITE);


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
    public void onDestroy() {
        super.onDestroy();
        mDisposable.clear();
        mLyricView.setOnPlayerClickListener(null);

        if (mTask != null && !mTask.isCancelled()) {
            mTask.cancel(true);
        }
        if (updateLyricsAsyncTask != null && !updateLyricsAsyncTask.isCancelled()) {
            updateLyricsAsyncTask.cancel(true);
        }
    }

    private void loadLrcFile() {
        Song song = MusicPlayerRemote.getCurrentSong();
        String title = song.title;
        String artist = song.artistName;

        loadLyricsProvider(title, artist);

        mTitle.setText(title);
        mText.setText(artist);

    }

    private void loadLyricsProvider(String title, String artist) {
        hideLyrics(View.GONE);
        switch (PreferenceUtil.getInstance(this).lyricsOptions()) {
            case 0://Offline
                loadSongLyrics();
                break;
            case 2://Kogou
                mLyricView.reset();
                if (LyricUtil.isLrcFileExist(title, artist)) {
                    showLyricsLocal(LyricUtil.getLocalLyricFile(title, artist));
                } else {
                    callAgain(title, artist);
                }
                break;
            case 1://Lyrics Wiki
                loadLyricsWIki(title, artist);
                break;
        }


    }

    private void loadLyricsWIki(String title, String artist) {
        if (mTask != null) {
            mTask.cancel(false);
        }
        mTask = new ParseLyrics(new ParseLyrics.LyricsCallback() {
            @Override
            public void onShowLyrics(String lyrics) {
                mOfflineLyrics.setVisibility(View.VISIBLE);
                mOfflineLyrics.setText(lyrics);
            }

            @Override
            public void onError() {
                loadSongLyrics();
            }
        }).execute(title, artist);
    }

    private void callAgain(final String title, final String artist) {
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
                            loadLyricsWIki(title, artist);
                            hideLyrics(View.GONE);
                        }, () -> {
                            mRefresh.clearAnimation();
                            Toast.makeText(this, "Lyrics downloaded", Toast.LENGTH_SHORT).show();
                        }));
    }

    private void showLyricsLocal(File file) {
        if (file == null) {
            mLyricView.reset();
        } else {
            hideLyrics(View.VISIBLE);
            mLyricView.setLyricFile(file, "UTF-8");
        }
    }

    private void hideLyrics(int gone) {
        mLyricsContainer.setVisibility(gone);
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
                    mOfflineLyrics.setText(R.string.no_lyrics_found);
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

    @OnClick({R.id.refresh,
            R.id.dec_font_size,
            R.id.inc_font_size,
            R.id.edit})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.refresh:
                Song song = MusicPlayerRemote.getCurrentSong();
                String title = song.title;
                String artist = song.artistName;
                mLyricView.reset();
                if (LyricUtil.deleteLrcFile(title, artist))
                    callAgain(title, artist);
                break;
            case R.id.dec_font_size:
                fontSize--;
                if (fontSize <= 17.0f) {
                    fontSize = 17.0f;
                }
                //mLyricView.setTextSize(fontSize);
                break;
            case R.id.edit:
                TransitionManager.beginDelayedTransition(findViewById(R.id.root));
                mActions.setVisibility(mActions.getVisibility() == View.GONE ? View.VISIBLE : View.GONE);
                break;
            case R.id.inc_font_size:
                fontSize++;
                if (fontSize >= 40.0f) {
                    fontSize = 40.0f;
                }
                //mLyricView.setTextSize(fontSize);
                break;
        }
    }
}
