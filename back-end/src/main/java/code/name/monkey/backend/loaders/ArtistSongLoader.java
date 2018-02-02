package code.name.monkey.backend.loaders;

import android.content.Context;
import android.database.Cursor;
import android.provider.MediaStore;
import android.support.annotation.NonNull;

import code.name.monkey.backend.model.Song;
import code.name.monkey.backend.util.PreferenceUtil;

import java.util.ArrayList;

import io.reactivex.Observable;

/**
 * @author Karim Abou Zeid (kabouzeid)
 */
public class ArtistSongLoader extends SongLoader {

    @NonNull
    public static Observable<ArrayList<Song>> getArtistSongList(@NonNull final Context context, final int artistId) {
        return getSongs(makeArtistSongCursor(context, artistId));
    }

    public static Cursor makeArtistSongCursor(@NonNull final Context context, final int artistId) {
        try {
            return makeSongCursor(
                    context,
                    MediaStore.Audio.AudioColumns.ARTIST_ID + "=?",
                    new String[]{
                            String.valueOf(artistId)
                    },
                    PreferenceUtil.getInstance(context).getArtistSongSortOrder()
            );
        } catch (SecurityException e) {
            return null;
        }
    }
}