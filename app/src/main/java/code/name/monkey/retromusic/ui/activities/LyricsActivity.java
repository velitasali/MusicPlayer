package code.name.monkey.retromusic.ui.activities;

import android.annotation.SuppressLint;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.transition.TransitionManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;

import org.jaudiotagger.tag.FieldKey;

import java.io.File;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import code.name.monkey.appthemehelper.ThemeStore;
import code.name.monkey.backend.lyrics.ParseLyrics;
import code.name.monkey.backend.lyrics.TTDownloader;
import code.name.monkey.backend.model.Song;
import code.name.monkey.backend.model.lyrics.Lyrics;
import code.name.monkey.backend.providers.RepositoryImpl;
import code.name.monkey.backend.providers.interfaces.Repository;
import code.name.monkey.backend.util.LyricUtil;
import code.name.monkey.retromusic.R;
import code.name.monkey.retromusic.helper.MusicPlayerRemote;
import code.name.monkey.retromusic.helper.MusicProgressViewUpdateHelper;
import code.name.monkey.retromusic.ui.activities.base.AbsMusicServiceActivity;
import code.name.monkey.retromusic.ui.activities.tageditor.WriteTagsAsyncTask;
import code.name.monkey.retromusic.util.MusicUtil;
import code.name.monkey.retromusic.util.PreferenceUtil;
import code.name.monkey.retromusic.util.Util;
import code.name.monkey.retromusic.views.LyricView;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public class LyricsActivity extends AbsMusicServiceActivity implements MusicProgressViewUpdateHelper.Callback {

    private static final String lrcRootPath = android.os.Environment
            .getExternalStorageDirectory().toString() + "/RetroMusic/lyrics/";
    @BindView(R.id.title)
    TextView title;
    @BindView(R.id.text)
    TextView text;
    @BindView(R.id.lyrics)
    LyricView lyricView;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.offline_lyrics)
    TextView offlineLyrics;
    @BindView(R.id.lyrics_container)
    View lyricsContainer;
    @BindView(R.id.refresh)
    View refresh;
    @BindView(R.id.actions)
    LinearLayout actionsLayout;
    @BindView(R.id.edit)
    View edit;
    private MusicProgressViewUpdateHelper updateHelper;
    private AsyncTask updateLyricsAsyncTask;
    private Repository loadLyrics;
    private CompositeDisposable disposable;
    private float fontSize = 17.0f;
    private RotateAnimation rotateAnimation;
    private AsyncTask<String, Void, String> lyricsWikiTask;

    /**
     * Open another app.
     *
     * @param packageName the full package name of the app to open
     * @return true if likely successful, false if unsuccessful
     */
    public void openApp(String packageName) {
        PackageManager manager = getPackageManager();
        try {
            Intent i = manager.getLaunchIntentForPackage(packageName);
            if (i == null) {
                return;
                //throw new ActivityNotFoundException();
            }
            i.addCategory(Intent.CATEGORY_LAUNCHER);
            startActivity(i);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, "App not installed", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lyrics);
        ButterKnife.bind(this);

        setStatusbarColorAuto();
        setNavigationbarColorAuto();
        setTaskDescriptionColorAuto();

        updateHelper = new MusicProgressViewUpdateHelper(this, 500, 1000);
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
        disposable = new CompositeDisposable();
        //lyricView.setLineSpace(15.0f);
        //lyricView.setTextSize(17.0f);
        //lyricView.setPlayable(true);
        //lyricView.setTranslationY(DensityUtil.getScreenWidth(this) + DensityUtil.dip2px(this, 120));
        lyricView.setOnPlayerClickListener((progress, content) -> {
            MusicPlayerRemote.seekTo((int) progress);
        });

        //lyricView.setHighLightTextColor(ThemeStore.accentColor(this));
        lyricView.setDefaultColor(ContextCompat.getColor(this, R.color.md_grey_400));
        //lyricView.setTouchable(false);
        lyricView.setHintColor(Color.WHITE);


    }

    private void setupToolbar() {
        toolbar.setBackgroundColor(ThemeStore.primaryColor(this));
        toolbar.setTitle("");
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
        setSupportActionBar(toolbar);
    }

    @Override
    public void onPlayingMetaChanged() {
        super.onPlayingMetaChanged();
        loadLrcFile();
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateHelper.start();
    }

    @Override
    protected void onPause() {
        super.onPause();
        updateHelper.stop();
    }

    @Override
    public void onServiceConnected() {
        super.onServiceConnected();
        loadLrcFile();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        disposable.clear();
        lyricView.setOnPlayerClickListener(null);

        if (lyricsWikiTask != null && !lyricsWikiTask.isCancelled()) {
            lyricsWikiTask.cancel(true);
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
        //netEaseLyrics(title, artist);

        this.title.setText(title);
        text.setText(artist);

    }

    private void loadLyricsProvider(String title, String artist) {
        hideLyrics(View.GONE);
        switch (PreferenceUtil.getInstance(this).lyricsOptions()) {
            default:
            case "offline":
                loadSongLyrics();
                break;
            case "kugou":
                lyricView.reset();
                if (LyricUtil.isLrcFileExist(title, artist)) {
                    showLyricsLocal(LyricUtil.getLocalLyricFile(title, artist));
                } else {
                    callAgain(title, artist);
                }
                break;
        }
    }

    private void loadLyricsWIki(String title, String artist) {
        offlineLyrics.setVisibility(View.GONE);
        if (lyricsWikiTask != null) {
            lyricsWikiTask.cancel(false);
        }
        lyricsWikiTask = new ParseLyrics(new ParseLyrics.LyricsCallback() {
            @Override
            public void onShowLyrics(String lyrics) {
                offlineLyrics.setVisibility(View.VISIBLE);
                offlineLyrics.setText(lyrics);
            }

            @Override
            public void onError() {
                loadSongLyrics();
            }
        }).execute(title, artist);
    }

    private void callAgain(final String title, final String artist) {
        disposable.clear();
        disposable.add(loadLyrics.downloadLrcFile(title, artist, MusicPlayerRemote.getSongDurationMillis())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .doOnSubscribe(disposable -> {
                    refresh.startAnimation(rotateAnimation);
                })
                .subscribe(this::showLyricsLocal, throwable -> {
                    refresh.clearAnimation();
                    showLyricsLocal(null);
                    //loadLyricsWIki(title, artist);
                    hideLyrics(View.GONE);
                }, () -> {
                    refresh.clearAnimation();
                    Toast.makeText(this, "Lyrics downloaded", Toast.LENGTH_SHORT).show();
                }));
    }

    private void showLyricsLocal(File file) {
        if (file == null) {
            edit.setVisibility(View.VISIBLE);
            lyricView.reset();
        } else {
            hideLyrics(View.VISIBLE);
            lyricView.setLyricFile(file, "UTF-8");
        }
    }

    private void hideLyrics(int gone) {
        lyricsContainer.setVisibility(gone);
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
        lyricView.setCurrentTimeMillis(progress);
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
                offlineLyrics.setVisibility(View.VISIBLE);
                if (l == null) {
                    edit.setVisibility(View.VISIBLE);
                    offlineLyrics.setText(R.string.no_lyrics_found);
                    return;
                }
                offlineLyrics.setText(l.data);
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

    @OnClick({R.id.refresh, R.id.dec_font_size, R.id.inc_font_size, R.id.search, R.id.edit,
            R.id.edit_lyrics, R.id.genius})
    public void onViewClicked(View view) {
        Song song = MusicPlayerRemote.getCurrentSong();
        String title = song.title;
        String artist = song.artistName;

        switch (view.getId()) {
            case R.id.genius:
                openApp("com.genius.android");
                break;
            case R.id.edit_lyrics:
                showLyricsSaveDialog(song);
                break;
            case R.id.search:
                Util.openUrl(this, getGoogleSearchUrl(title, artist));
                break;
            case R.id.refresh:

                lyricView.reset();
                if (LyricUtil.deleteLrcFile(title, artist))
                    callAgain(title, artist);
                break;
            case R.id.dec_font_size:
                fontSize--;
                if (fontSize <= 17.0f) {
                    fontSize = 17.0f;
                }
                //lyricView.setTextSize(fontSize);
                break;
            case R.id.edit:
                TransitionManager.beginDelayedTransition(findViewById(R.id.root));
                actionsLayout.setVisibility(actionsLayout.getVisibility() == View.GONE ? View.VISIBLE : View.GONE);
                break;
            case R.id.inc_font_size:
                fontSize++;
                if (fontSize >= 40.0f) {
                    fontSize = 40.0f;
                }
                //lyricView.setTextSize(fontSize);
                break;
        }
    }

    private void showLyricsSaveDialog(Song song) {
        new MaterialDialog.Builder(this)
                .title("Add lyrics")
                .inputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE)
                .input("Paste lyrics here", "", new MaterialDialog.InputCallback() {
                    @Override
                    public void onInput(@NonNull MaterialDialog dialog, CharSequence input) {
                        Map<FieldKey, String> fieldKeyValueMap = new EnumMap<>(FieldKey.class);
                        fieldKeyValueMap.put(FieldKey.LYRICS, input.toString());

                        new WriteTagsAsyncTask(LyricsActivity.this)
                                .execute(new WriteTagsAsyncTask.LoadingInfo(getSongPaths(song), fieldKeyValueMap, null));
                        loadLrcFile();
                    }
                })
                .show();
    }

    private ArrayList<String> getSongPaths(Song song) {
        ArrayList<String> paths = new ArrayList<>(1);
        paths.add(song.data);
        return paths;
    }

    private String getGoogleSearchUrl(String title, String text) {
        String baseUrl = "http://www.google.com/search?";
        String query = title + "+" + text;
        query = "q=" + query.replace(" ", "+") + " lyrics";
        baseUrl += query;
        return baseUrl;
    }

    private void netEaseLyrics(String title, String artist) {
        Observable.fromCallable(() -> TTDownloader.query(artist, title))
                .map(queryResults -> TTDownloader.download(queryResults.get(0), lrcRootPath + title + " - " + artist + ".lrc"))
                .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .subscribe(aBoolean -> {
                    Log.i(TAG, "netEaseLyrics: " + aBoolean);
                });
    }
}
