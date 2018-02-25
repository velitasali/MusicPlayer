package code.name.monkey.backend.lyrics;

import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;

/**
 * @author Hemanth S (h4h13).
 */

public class ParseLyrics extends AsyncTask<String, Void, String> {
    private LyricsCallback mLyricsCallback;

    public ParseLyrics(LyricsCallback lyricsCallback) {
        mLyricsCallback = lyricsCallback;
    }

    @Override
    protected String doInBackground(String... strings) {
        LyricsEngine lyricsEngine = new LyricsWikiEngine();
        return lyricsEngine.getLyrics(strings[1], strings[0]);
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
        if (TextUtils.isEmpty(s)) {
            mLyricsCallback.onError();
            return;
        }
        mLyricsCallback.onShowLyrics(s);
    }

    public interface LyricsCallback {
        void onShowLyrics(String lyrics);

        void onError();
    }
}
