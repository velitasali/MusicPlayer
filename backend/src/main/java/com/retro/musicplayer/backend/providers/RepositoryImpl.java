package com.retro.musicplayer.backend.providers;

import android.content.Context;
import android.support.annotation.NonNull;

import com.retro.musicplayer.backend.loaders.AlbumLoader;
import com.retro.musicplayer.backend.loaders.ArtistLoader;
import com.retro.musicplayer.backend.loaders.GenreLoader;
import com.retro.musicplayer.backend.loaders.HomeLoader;
import com.retro.musicplayer.backend.loaders.PlaylistLoader;
import com.retro.musicplayer.backend.loaders.PlaylistSongsLoader;
import com.retro.musicplayer.backend.loaders.SearchLoader;
import com.retro.musicplayer.backend.loaders.SongLoader;
import com.retro.musicplayer.backend.model.Album;
import com.retro.musicplayer.backend.model.Artist;
import com.retro.musicplayer.backend.model.Genre;
import com.retro.musicplayer.backend.model.Playlist;
import com.retro.musicplayer.backend.model.Song;
import com.retro.musicplayer.backend.providers.interfaces.Repository;
import com.retro.musicplayer.backend.rest.KogouClient;
import com.retro.musicplayer.backend.rest.model.KuGouRawLyric;
import com.retro.musicplayer.backend.rest.model.KuGouSearchLyricResult;
import com.retro.musicplayer.backend.rest.service.KuGouApiService;
import com.retro.musicplayer.backend.util.LyricUtil;

import java.io.File;
import java.util.ArrayList;

import io.reactivex.Observable;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by hemanths on 11/08/17.
 */

public class RepositoryImpl implements Repository {
    private static RepositoryImpl INSTANCE;
    private Context context;

    public RepositoryImpl(Context context) {
        this.context = context;
    }

    public static synchronized RepositoryImpl getInstance(@NonNull Context context) {
        if (INSTANCE == null) {
            INSTANCE = new RepositoryImpl(context);
        }
        return INSTANCE;
    }

    @Override
    public Observable<ArrayList<Song>> getAllSongs() {
        return SongLoader.getAllSongs(context);
    }

    @Override
    public Observable<Song> getSong(int id) {
        return SongLoader.getSong(context, id);
    }

    @Override
    public Observable<ArrayList<Album>> getAllAlbums() {
        return AlbumLoader.getAllAlbums(context);
    }

    @Override
    public Observable<Album> getAlbum(int albumId) {
        return AlbumLoader.getAlbum(context, albumId);
    }

    @Override
    public Observable<ArrayList<Artist>> getAllArtists() {
        return ArtistLoader.getAllArtists(context);
    }

    @Override
    public Observable<Artist> getArtistById(long artistId) {
        return ArtistLoader.getArtist(context, (int) artistId);
    }

    @Override
    public Observable<ArrayList<Playlist>> getAllPlaylists() {
        return PlaylistLoader.getAllPlaylists(context);
    }

    @Override
    public Observable<ArrayList<Song>> getFavoriteSongs() {
        return null;
    }

    @Override
    public Observable<ArrayList<Object>> search(String query) {
        return SearchLoader.searchAll(context, query);
    }

    @Override
    public Observable<ArrayList<Song>> getPlaylistSongs(Playlist playlist) {
        return PlaylistSongsLoader.getPlaylistSongList(context, playlist);
    }

    @Override
    public Observable<ArrayList<Playlist>> getHomeList() {
        return HomeLoader.getHomeLoader(context);
    }

    @Override
    public Observable<ArrayList<Object>> getAllThings() {
        return HomeLoader.getRecentAndTopThings(context);
    }

    @Override
    public Observable<ArrayList<Genre>> getAllGenres() {
        return GenreLoader.getAllGenres(context);
    }

    @Override
    public Observable<ArrayList<Song>> getGenre(int genreId) {
        return GenreLoader.getSongs(context, genreId);
    }

    @Override
    public Observable<File> downloadLrcFile(String title, String artist, long duration) {
        KuGouApiService service = new KogouClient(context).getApiService();
        return service.searchLyric(title, String.valueOf(duration))
                .subscribeOn(Schedulers.io())
                .flatMap(kuGouSearchLyricResult -> {
                    if (kuGouSearchLyricResult.status == 200
                            && kuGouSearchLyricResult.candidates != null
                            && kuGouSearchLyricResult.candidates.size() != 0) {
                        KuGouSearchLyricResult.Candidates candidates = kuGouSearchLyricResult.candidates.get(0);
                        return service.getRawLyric(candidates.id, candidates.accesskey);
                    } else {
                        return Observable.just(new KuGouRawLyric());
                    }
                }).map(kuGouRawLyric -> {
                    if (kuGouRawLyric == null) {
                        return null;
                    }
                    String rawLyric = LyricUtil.decryptBASE64(kuGouRawLyric.content);
                    if (rawLyric != null && rawLyric.isEmpty()) {
                        return null;
                    }
                    return LyricUtil.writeLrcToLoc(title, artist, rawLyric);
                });
    }
}
