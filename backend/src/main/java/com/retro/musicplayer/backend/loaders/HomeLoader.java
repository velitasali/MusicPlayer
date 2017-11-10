package com.retro.musicplayer.backend.loaders;

import android.content.Context;

import com.retro.musicplayer.backend.model.Playlist;
import com.retro.musicplayer.backend.model.smartplaylist.HistoryPlaylist;
import com.retro.musicplayer.backend.model.smartplaylist.LastAddedPlaylist;
import com.retro.musicplayer.backend.model.smartplaylist.MyTopTracksPlaylist;

import java.util.ArrayList;

import io.reactivex.Observable;

/**
 * Created by hemanths on 20/08/17.
 */

public class HomeLoader {


    public static Observable<ArrayList<Playlist>> getHomeLoader(Context context) {
        ArrayList<Playlist> playlists = new ArrayList<>();

        new LastAddedPlaylist(context).getSongs(context).subscribe(songs -> {
            if (songs.size() > 0)
                playlists.add(new LastAddedPlaylist(context));
        });
        new MyTopTracksPlaylist(context).getSongs(context).subscribe(songs -> {
            if (songs.size() > 0)
                playlists.add(new MyTopTracksPlaylist(context));
        });

        new HistoryPlaylist(context).getSongs(context).subscribe(songs -> {
            if (songs.size() > 0)
                playlists.add(new HistoryPlaylist(context));
        });

        PlaylistLoader.getAllPlaylists(context)
                .subscribe(playlists1 -> playlists.addAll(playlists1));
        return Observable.just(playlists);
    }
}
