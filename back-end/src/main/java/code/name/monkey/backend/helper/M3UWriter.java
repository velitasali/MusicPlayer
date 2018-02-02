package code.name.monkey.backend.helper;

import android.content.Context;

import code.name.monkey.backend.loaders.PlaylistSongsLoader;
import code.name.monkey.backend.model.Playlist;
import code.name.monkey.backend.model.Song;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;


import io.reactivex.Observable;

public class M3UWriter implements M3UConstants {
    public static final String TAG = M3UWriter.class.getSimpleName();

    public static Observable<File> write(Context context, File dir, Playlist playlist) {
        if (!dir.exists()) //noinspection ResultOfMethodCallIgnored
            dir.mkdirs();
        File file = new File(dir, playlist.name.concat("." + EXTENSION));

        //ArrayList<PlaylistSong> songs = PlaylistSongsLoader.getPlaylistSongList(context, playlist.id);
        return Observable.create(e ->
                PlaylistSongsLoader.getPlaylistSongList(context, playlist.id)
                        .subscribe(songs -> {
                            if (songs.size() > 0) {
                                BufferedWriter bw = new BufferedWriter(new FileWriter(file));
                                bw.write(HEADER);
                                for (Song song : songs) {
                                    bw.newLine();
                                    bw.write(ENTRY + song.duration + DURATION_SEPARATOR + song.artistName + " - " + song.title);
                                    bw.newLine();
                                    bw.write(song.data);
                                }

                                bw.close();
                            }
                            e.onNext(file);
                            e.onComplete();
                        }));
    }
}