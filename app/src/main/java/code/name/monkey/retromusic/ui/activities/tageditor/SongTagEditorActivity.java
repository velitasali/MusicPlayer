package code.name.monkey.retromusic.ui.activities.tageditor;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.provider.DocumentFile;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.kabouzeid.appthemehelper.ThemeStore;
import com.kabouzeid.appthemehelper.util.TintHelper;
import com.kabouzeid.appthemehelper.util.ToolbarContentTintHelper;
import com.retro.musicplayer.backend.loaders.SongLoader;
import com.retro.musicplayer.backend.rest.LastFMRestClient;
import com.retro.musicplayer.backend.rest.model.LastFmTrack.Track;
import com.retro.musicplayer.backend.rest.model.LastFmTrack.Track.Album;
import com.retro.musicplayer.backend.rest.model.LastFmTrack.Track.Album.Attr;
import com.retro.musicplayer.backend.rest.model.LastFmTrack.Track.Toptags;
import com.retro.musicplayer.backend.rest.model.LastFmTrack.Track.Wiki;

import org.jaudiotagger.tag.FieldKey;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import code.name.monkey.retromusic.R;
import code.name.monkey.retromusic.RetroApplication;
import code.name.monkey.retromusic.tagger.CheckDocumentPermissionsTask;
import code.name.monkey.retromusic.tagger.TaggerTask;
import code.name.monkey.retromusic.tagger.TaggerUtils;
import code.name.monkey.retromusic.util.PreferenceUtil;
import code.name.monkey.retromusic.util.RetroUtils;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;


public class SongTagEditorActivity extends AbsTagEditorActivity implements TextWatcher {
    public static final String TAG = SongTagEditorActivity.class.getSimpleName();
    private static final int REQUEST_CODE = 9002;
    private static final int DOCUMENT_TREE_REQUEST_CODE = 9001;
    @BindView(R.id.title1)
    EditText songTitle;
    @BindView(R.id.title2)
    EditText albumTitle;
    @BindView(R.id.artist)
    EditText artist;
    @BindView(R.id.genre)
    EditText genre;
    @BindView(R.id.year)
    EditText year;
    @BindView(R.id.image_text)
    EditText trackNumber;
    @BindView(R.id.lyrics)
    EditText lyrics;
    @BindView(R.id.progressBar)
    ProgressBar progressBar;
    @BindView(R.id.load)
    Button loadTrackDetails;
    private LastFMRestClient lastFMRestClient;
    private List<DocumentFile> documentFiles = new ArrayList<>();
    private boolean hasCheckedPermissions = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ButterKnife.bind(this);

        setStatusbarColorAuto();
        setTaskDescriptionColorAuto();

        setNoImageMode();
        setUpViews();

        progressBar.setVisibility(View.GONE);
        lastFMRestClient = new LastFMRestClient(this);

        toolbar.setTitle(R.string.action_tag_editor);
        setSupportActionBar(toolbar);
        //noinspection ConstantConditions
        getSupportActionBar().setTitle(R.string.action_tag_editor);

        TintHelper.setTintAuto(loadTrackDetails, ThemeStore.accentColor(this), false);
    }

    @OnClick(R.id.load)
    void loadImage() {
        getImageFromLastFM();
    }

    private void setUpViews() {
        fillViewsWithFileTags();
        songTitle.addTextChangedListener(this);
        albumTitle.addTextChangedListener(this);
        artist.addTextChangedListener(this);
        genre.addTextChangedListener(this);
        year.addTextChangedListener(this);
        trackNumber.addTextChangedListener(this);
        lyrics.addTextChangedListener(this);
    }

    private void fillViewsWithFileTags() {
        songTitle.setText(getSongTitle());
        albumTitle.setText(getAlbumTitle());
        artist.setText(getArtistName());
        genre.setText(getGenreName());
        year.setText(getSongYear());
        trackNumber.setText(getTrackNumber());
        lyrics.setText(getLyrics());
    }

    @Override
    protected void loadCurrentImage() {

    }

    @Override
    protected void getImageFromLastFM() {
        String albumTitleStr = albumTitle.getText().toString();
        String albumArtistNameStr = artist.getText().toString();
        String songName = songTitle.getText().toString();
        if (albumArtistNameStr.trim().equals("") || albumTitleStr.trim().equals("")) {
            Toast.makeText(this, getResources().getString(R.string.album_or_artist_empty), Toast.LENGTH_SHORT).show();
            return;
        }

        lastFMRestClient.getApiService().getTrackInfo(albumArtistNameStr, songName)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.computation())
                .doOnSubscribe(disposable -> {
                    progressBar.setVisibility(View.VISIBLE);
                })
                .doOnComplete(() -> {
                    progressBar.setVisibility(View.GONE);
                })
                .subscribe(lastFmTrack -> {
                    Track track = lastFmTrack != null ? lastFmTrack.getTrack() : null;
                    Album albums = track != null ? track.getAlbum() : null;
                    Attr attr = null;
                    if (albums != null) attr = albums.getAttr();
                    Wiki wiki = track != null ? track.getWiki() : null;
                    List<Toptags.Tag> tags = track != null ? track.getToptags().getTag() : null;
                    if (attr != null) {
                        trackNumber.setText(attr.getPosition());
                    }
                    if (wiki != null) {
                        try {
                            Date date = new SimpleDateFormat("dd MMM YYYY, k:mm", Locale.getDefault()).parse(wiki.getPublished());
                            year.setText(DateFormat.format("yyyy", date));
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                    }
                    if (!(tags != null && tags.isEmpty())) {
                        genre.setText(tags != null ? tags.get(0).getName() : "");
                    }
                }, Throwable::printStackTrace, () -> {
                    progressBar.setVisibility(View.GONE);
                });
    }

    @Override
    protected void searchImageOnWeb() {

    }

    @Override
    protected void deleteImage() {

    }

    @Override
    protected void save() {

        Map<FieldKey, String> fieldKeyValueMap = new EnumMap<>(FieldKey.class);
        fieldKeyValueMap.put(FieldKey.TITLE, songTitle.getText().toString());
        fieldKeyValueMap.put(FieldKey.ALBUM, albumTitle.getText().toString());
        fieldKeyValueMap.put(FieldKey.ARTIST, artist.getText().toString());
        fieldKeyValueMap.put(FieldKey.GENRE, genre.getText().toString());
        fieldKeyValueMap.put(FieldKey.YEAR, year.getText().toString());
        fieldKeyValueMap.put(FieldKey.TRACK, trackNumber.getText().toString());
        fieldKeyValueMap.put(FieldKey.LYRICS, lyrics.getText().toString());
        //writeValuesToFiles(fieldKeyValueMap, deleteAlbumArt ? new ArtworkInfo(getId(), null) : albumArtBitmap == null ? null : new ArtworkInfo(getId(), albumArtBitmap));

        List<String> paths = getSongPaths();
        CheckDocumentPermissionsTask checkDocumentPermissionsTask =
                new CheckDocumentPermissionsTask(paths, documentFiles, hasPermission -> {
                    if (!hasPermission) {
                        TaggerUtils.showChooseDocumentDialog(SongTagEditorActivity.this, (dialog1, which1) -> {
                            if (RetroUtils.hasLollipop()) {
                                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
                                if (intent.resolveActivity(RetroApplication.getInstance().getPackageManager()) != null) {
                                    startActivityForResult(intent, DOCUMENT_TREE_REQUEST_CODE);
                                } else {
                                    Toast.makeText(SongTagEditorActivity.this, "Permission needed", Toast.LENGTH_LONG).show();
                                }
                            }
                        }, hasCheckedPermissions);
                        hasCheckedPermissions = true;
                    } else {
                        MaterialDialog saveProgressDialog = new MaterialDialog.Builder(this)
                                .progressIndeterminateStyle(true)
                                .content(getResources().getString(R.string.saving_tags))
                                .cancelable(false)
                                .progress(true, 0)
                                .progressIndeterminateStyle(true)
                                .build();


                        TaggerTask.TagCompletionListener tagCompletionListener = new TaggerTask.TagCompletionListener() {
                            @Override
                            public void onSuccess() {
                                saveProgressDialog.dismiss();
                            }

                            @Override
                            public void onFailure() {
                                saveProgressDialog.dismiss();
                                if (RetroUtils.hasKitKat() && !RetroUtils.hasLollipop()) {
                                    Toast.makeText(SongTagEditorActivity.this, R.string.tag_error_kitkat, Toast.LENGTH_LONG).show();
                                } else if (RetroUtils.hasLollipop()) {
                                    Toast.makeText(SongTagEditorActivity.this, R.string.tag_error_lollipop, Toast.LENGTH_LONG).show();
                                } else {
                                    Toast.makeText(SongTagEditorActivity.this, R.string.tag_edit_error, Toast.LENGTH_LONG).show();
                                }
                            }

                            @Override
                            public void onProgress(int progress) {
                                saveProgressDialog.setProgress(progress);
                            }
                        };
                        TaggerTask taggerTask = new TaggerTask()
                                //.showAlbum(showAlbum)
                                //.showTrack(showTrack)
                                .setPaths(paths)
                                .setDocumentfiles(documentFiles)
                                .title(songTitle.getText().toString())
                                .album(albumTitle.getText().toString())
                                .artist(artist.getText().toString())
                                //.albumArtist(albumArtistEditText.getText().toString())
                                .year(year.getText().toString())
                                .track(trackNumber.getText().toString())
                                //.trackTotal(tra.getText().toString())
                                //.disc(discEditText.getText().toString())
                                //.discTotal(discTotalEditText.getText().toString())
                                .lyrics(lyrics.getText().toString())
                                //.comment(commentEditText.getText().toString())
                                .genre(genre.getText().toString())
                                .listener(tagCompletionListener)
                                .build();
                        taggerTask.execute();

                        writeValuesToFiles(fieldKeyValueMap, null);

                    }
                });
        checkDocumentPermissionsTask.execute();



    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (RetroUtils.hasKitKat()) {
            switch (requestCode) {
                case DOCUMENT_TREE_REQUEST_CODE:
                    if (resultCode == Activity.RESULT_OK) {
                        Uri treeUri = data.getData();
                        RetroApplication.getInstance().getContentResolver().takePersistableUriPermission(treeUri, Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                        PreferenceUtil.getInstance(this).setDocumentTreeUri(data.getData().toString());
                        //saveTags();
                    }
                    break;
            }
        }
    }

    @Override
    protected int getContentViewLayout() {
        return R.layout.activity_song_tag_editor;
    }

    @NonNull
    @Override
    protected List<String> getSongPaths() {
        ArrayList<String> paths = new ArrayList<>(1);
        paths.add(SongLoader.getSong(this, getId()).blockingFirst().data);
        return paths;
    }

    @Override
    protected void loadImageFromFile(Uri imageFilePath) {

    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    @Override
    public void afterTextChanged(Editable s) {
        dataChanged();
    }

    @Override
    protected void setColors(int color) {
        super.setColors(color);
        int toolbarTitleColor = ToolbarContentTintHelper.toolbarTitleColor(this, color);
        songTitle.setTextColor(toolbarTitleColor);
        albumTitle.setTextColor(toolbarTitleColor);
    }
}
