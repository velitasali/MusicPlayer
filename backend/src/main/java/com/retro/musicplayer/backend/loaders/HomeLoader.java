package com.retro.musicplayer.backend.loaders;

import android.content.Context;
import android.support.annotation.NonNull;

import com.retro.musicplayer.backend.R;
import com.retro.musicplayer.backend.helper.ShuffleHelper;
import com.retro.musicplayer.backend.model.Playlist;
import com.retro.musicplayer.backend.model.Song;

import java.util.ArrayList;

import io.reactivex.Observable;

/**
 * Created by hemanths on 20/08/17.
 */

public class HomeLoader {
    public static Observable<ArrayList<Object>> getRecentAndTopThings(@NonNull Context context) {
        ArrayList<Object> objects = new ArrayList<>();
        /*
        * 1st element will be integer because for showing smart playlist
        * */
        objects.add(1);

        /*
        * Adding all other things to list
        * */
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

            suggestSongs(context).subscribe(songs -> {
                objects.add(context.getString(R.string.suggestions));
                objects.add(songs);
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
        PlaylistLoader.getAllPlaylists(context)
                .subscribe(playlists1 -> {
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

    public static Observable<ArrayList<Song>> suggestSongs(@NonNull Context context) {
        return Observable.create(observer -> {
            SongLoader.getAllSongs(context).subscribe(songs -> {
                ArrayList<Song> list = new ArrayList<>();
                if (songs.isEmpty()) {
                    observer.onNext(new ArrayList<Song>());
                    observer.onComplete();
                    return;
                }
                ShuffleHelper.makeShuffleList(songs, -1);
                if (songs.size() > 10) {
                    list.addAll(songs.subList(0, 10));
                }
                observer.onNext(list);
                observer.onComplete();
            });
        });
    }
}
