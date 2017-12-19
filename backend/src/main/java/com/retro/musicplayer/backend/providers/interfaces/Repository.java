package com.retro.musicplayer.backend.providers.interfaces;

import com.retro.musicplayer.backend.model.Album;
import com.retro.musicplayer.backend.model.Artist;
import com.retro.musicplayer.backend.model.Genre;
import com.retro.musicplayer.backend.model.Playlist;
import com.retro.musicplayer.backend.model.Song;

import java.util.ArrayList;

import io.reactivex.Observable;

/**
 * Created by hemanths on 11/08/17.
 */

public interface Repository {
    Observable<ArrayList<Song>> getAllSongs();

    Observable<Song> getSong(int id);

    Observable<ArrayList<Album>> getAllAlbums();

    Observable<Album> getAlbum(int albumId);

    Observable<ArrayList<Artist>> getAllArtists();

    Observable<Artist> getArtistById(long artistId);

    Observable<ArrayList<Playlist>> getAllPlaylists();

    Observable<ArrayList<Song>> getFavoriteSongs();

    Observable<ArrayList<Object>> search(String query);

    Observable<ArrayList<Song>> getPlaylistSongs(Playlist playlist);

    Observable<ArrayList<Playlist>> getHomeList();

    Observable<ArrayList<Object>> getAllThings();

    Observable<ArrayList<Genre>> getAllGenres();

    Observable<ArrayList<Song>> getGenre(int genreId);
}
