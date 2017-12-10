package com.retro.musicplayer.backend.loaders;

import android.content.Context;
import android.support.annotation.NonNull;

import com.retro.musicplayer.backend.model.Playlist;

import java.util.ArrayList;

import io.reactivex.Observable;

/**
 * Created by hemanths on 20/08/17.
 */

public class HomeLoader {
    public static Observable<ArrayList<Playlist>> getHomeLoader(@NonNull Context context) {
        ArrayList<Playlist> playlists = new ArrayList<>();
        PlaylistLoader.getAllPlaylists(context).subscribe(playlists1 -> {
            if (playlists1.size() > 0)
                for (Playlist playlist :
                        playlists1) {
                    PlaylistSongsLoader.getPlaylistSongList(context, playlist)
                            .subscribe(songs -> {
                                if (songs.size() > 0) {
                                    playlists.add(playlist);
                                }
                            });
                }
        });
        return Observable.just(playlists);
    }
}
