package code.name.monkey.retromusic.ui.activities;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.kabouzeid.appthemehelper.ThemeStore;
import com.retro.musicplayer.backend.KogouLyricsFetcher;
import com.retro.musicplayer.backend.model.Song;
import com.retro.musicplayer.backend.model.lyrics.Lyrics;
import com.retro.musicplayer.backend.util.LyricUtil;

import java.io.File;

import butterknife.BindView;
import butterknife.ButterKnife;
import code.name.monkey.retromusic.R;
import code.name.monkey.retromusic.glide.RetroMusicColoredTarget;
import code.name.monkey.retromusic.glide.SongGlideRequest;
import code.name.monkey.retromusic.helper.MusicPlayerRemote;
import code.name.monkey.retromusic.helper.MusicProgressViewUpdateHelper;
import code.name.monkey.retromusic.lyrics.LyricsEngine;
import code.name.monkey.retromusic.lyrics.LyricsWikiEngine;
import code.name.monkey.retromusic.ui.activities.base.AbsMusicServiceActivity;
import code.name.monkey.retromusic.util.MusicUtil;
import code.name.monkey.retromusic.util.PreferenceUtil;
import code.name.monkey.retromusic.views.LyricView;

/**
 * Created by hemanths on 23/08/17.
 */

public class LyricsActivity extends AbsMusicServiceActivity
        implements MusicProgressViewUpdateHelper.Callback, AdapterView.OnItemSelectedListener,
        KogouLyricsFetcher.KogouLyricsCallback {
    private static final int KOGOU = 0;
    private static final int WIKI = 1;
    private static final int OFFLINE = 2;
    @BindView(R.id.image)
    ImageView mImage;
    @BindView(R.id.title)
    TextView mTitle;
    @BindView(R.id.text)
    TextView mText;
    @BindView(R.id.lyrics)
    LyricView lyricView;
    @BindView(R.id.lyrics_big)
    LyricView lyricViewBig;
    @BindView(R.id.toolbar)
    Toolbar mToolbar;
    @BindView(R.id.offline_lyrics)
    TextView mOfflineLyrics;
    @BindView(R.id.lyrics_options)
    Spinner mLyricsOptions;
    @BindView(R.id.lyrics_container)
    View mLyricsContainer;
    private MusicProgressViewUpdateHelper mUpdateHelper;
    private AsyncTask updateLyricsAsyncTask;
    private AsyncTask updateWikiLyricsAsyncTask;
    private LyricsEngine mEngine = new LyricsWikiEngine();
    private KogouLyricsFetcher mKogouLyricsFetcher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lyrics);
        ButterKnife.bind(this);
        setStatusbarColorAuto();
        setNavigationbarColorAuto();
        setTaskDescriptionColorAuto();

        mToolbar.setBackgroundColor(ThemeStore.primaryColor(this));
        mToolbar.setTitle(R.string.lyrics);
        mToolbar.setNavigationOnClickListener(v -> onBackPressed());
        setSupportActionBar(mToolbar);

        mKogouLyricsFetcher = new KogouLyricsFetcher(this, this);
        mUpdateHelper = new MusicProgressViewUpdateHelper(this, 500, 1000);

        mLyricsOptions.setOnItemSelectedListener(this);
        mLyricsOptions.setSelection(PreferenceUtil.getInstance(this).getLyricsOptions());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_lyrics, menu);
        return super.onCreateOptionsMenu(menu);

    }

    @Override
    public void onPlayingMetaChanged() {
        super.onPlayingMetaChanged();
        loadDetails();
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
        loadDetails();
    }

    private void loadDetails() {
        Song song = MusicPlayerRemote.getCurrentSong();
        mTitle.setText(song.title);
        mText.setText(song.artistName);
        SongGlideRequest.Builder.from(Glide.with(this), song)
                .checkIgnoreMediaStore(this)
                .generatePalette(this).build()
                .into(new RetroMusicColoredTarget(mImage) {
                    @Override
                    public void onColorReady(int color) {

                    }
                });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        lyricView.setOnPlayerClickListener(null);

        if (updateLyricsAsyncTask != null && !updateLyricsAsyncTask.isCancelled()) {
            updateLyricsAsyncTask.cancel(true);
        }
        if (updateWikiLyricsAsyncTask != null && !updateWikiLyricsAsyncTask.isCancelled()) {
            updateWikiLyricsAsyncTask.cancel(true);
        }

    }

    private void loadLrcFile() {
        Song song = MusicPlayerRemote.getCurrentSong();
        String title = song.title;
        String artist = song.artistName;

        if (lyricView == null) {
            return;
        }
        lyricView.reset();
        lyricViewBig.reset();


        if (LyricUtil.isLrcFileExist(title, artist)) {
            lyricView.setDefaultHint("Loading from local");
            lyricViewBig.setDefaultHint("Loading from local");
            showLyricsLocal(LyricUtil.getLocalLyricFile(title, artist));
        } else {
            mKogouLyricsFetcher.loadLyrics(song, String.valueOf(MusicPlayerRemote.getSongDurationMillis()));

        }

        lyricView.setOnPlayerClickListener((progress, content) -> {
            MusicPlayerRemote.seekTo((int) progress);
        });
        lyricViewBig.setOnPlayerClickListener((progress, content) -> {
            MusicPlayerRemote.seekTo((int) progress);
        });
    }

    private void showLyricsLocal(File file) {
        if (file == null) {
            lyricView.reset();
            lyricViewBig.reset();
        } else {
            lyricView.setLyricFile(file, "UTF-8");
            lyricViewBig.setLyricFile(file, "UTF-8");
            lyricViewBig.setTextSize(40);
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
        if (item.getItemId() == R.id.action_lyrics) {
            if (lyricViewBig.getVisibility() == View.INVISIBLE) {

                lyricViewBig.setVisibility(View.VISIBLE);
                lyricView.setVisibility(View.INVISIBLE);
            } else {

                lyricViewBig.setVisibility(View.INVISIBLE);
                lyricView.setVisibility(View.VISIBLE);
            }
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onUpdateProgressViews(int progress, int total) {
        lyricView.setCurrentTimeMillis(progress);
        lyricViewBig.setCurrentTimeMillis(progress);
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        PreferenceUtil.getInstance(this).setLyricsOptions(i);
        switch (i) {
            case KOGOU:
                mOfflineLyrics.setVisibility(View.GONE);
                loadLrcFile();
                break;
            case WIKI:
                hideLyrics();
                loadWikiLyrics();
                break;
            case OFFLINE:
                hideLyrics();
                loadSongLyrics();
                break;
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }

    @SuppressLint("StaticFieldLeak")
    private void loadWikiLyrics() {
        final Song song = MusicPlayerRemote.getCurrentSong();
        String title = song.title;
        String artist = song.artistName;
        if (updateWikiLyricsAsyncTask != null) updateWikiLyricsAsyncTask.cancel(false);
        updateWikiLyricsAsyncTask = new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... voids) {
                return mEngine.getLyrics(artist, title);
            }

            @Override
            protected void onPostExecute(String s) {
                super.onPostExecute(s);
                mOfflineLyrics.setVisibility(View.VISIBLE);
                if (s == null) {
                    return;
                }
                mOfflineLyrics.setText(s);
            }
        }.execute();
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


    @Override
    public void onNoLyrics() {
        hideLyrics();
    }

    @Override
    public void onLyrics(File file) {
        showLyricsLocal(file);
    }
}
