package com.retro.musicplayer.backend.loaders;

import android.content.Context;
import android.support.annotation.NonNull;

import com.retro.musicplayer.backend.R;
import com.retro.musicplayer.backend.model.Playlist;

import java.util.ArrayList;

import io.reactivex.Observable;

/**
 * Created by hemanths on 20/08/17.
 */

public class HomeLoader {
    public static Observable<ArrayList<Object>> getRecentAndTopThings(@NonNull Context context) {
        ArrayList<Object> objects = new ArrayList<>();

        return Observable.create(e -> {
            LastAddedSongsLoader.getLastAddedArtists(context).subscribe(
                    artists -> {
                        if (!artists.isEmpty()) {
                            objects.add(context.getString(R.string.recent_artists));
                            objects.add(artists);
                        }
                    });
            LastAddedSongsLoader.getLastAddedAlbums(context).subscribe(
                    albums -> {
                        if (!albums.isEmpty()) {
                            objects.add(context.getString(R.string.recent_albums));
                            objects.add(albums);
                        }
                    });
            TopAndRecentlyPlayedTracksLoader.getTopArtists(context).subscribe(
                    artists -> {
                        if (!artists.isEmpty()) {
                            objects.add(context.getString(R.string.top_artists));
                            objects.add(artists);
                        }
                    });
            TopAndRecentlyPlayedTracksLoader.getTopAlbums(context).subscribe(
                    albums -> {
                        if (!albums.isEmpty()) {
                            objects.add(context.getString(R.string.top_albums));
                            objects.add(albums);
                        }
                    });
            PlaylistLoader.getAllPlaylists(context).subscribe(
                    playlists -> {
                        if (!playlists.isEmpty()) {
                            objects.add(context.getString(R.string.playlists));
                            objects.add(playlists);
                        }
                    }
            );

            e.onNext(objects);
            e.onComplete();
        });
    }


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
