package code.name.monkey.retromusic.lyrics;

import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;
import android.widget.TextView;

import com.retro.musicplayer.backend.model.Song;
import com.retro.musicplayer.backend.model.lyrics.Lyrics;

import java.lang.ref.WeakReference;

import code.name.monkey.retromusic.util.MusicUtil;

/**
 * @author Hemanth S (h4h13).
 */

public class LocalID3Lyrics extends AsyncTask<Void, Void, Lyrics> {
    private static final String TAG = "LocalID3Lyrics";
    private WeakReference<TextView> mTextViewWeakReference;

    private Song song;

    public LocalID3Lyrics(Song currentSong, TextView textView) {
        mTextViewWeakReference = new WeakReference<TextView>(textView);
        song = currentSong;
    }

    @Override
    protected Lyrics doInBackground(Void... songs) {
        if (song == null) {
            return null;
        }
        String data = MusicUtil.getLyrics(song);
        if (TextUtils.isEmpty(data)) {
            return null;
        }
        return Lyrics.parse(song, data);
    }

    @Override
    protected void onPostExecute(Lyrics lyrics) {
        super.onPostExecute(lyrics);
        TextView textView = mTextViewWeakReference.get();
        if (textView == null) {
            Log.i(TAG, "onPostExecute: Textview");
            return;
        }
        if (lyrics == null) {
            Log.i(TAG, "onPostExecute: Lyrics");
            return;
        }
        textView.setText(lyrics.data);
    }
}
