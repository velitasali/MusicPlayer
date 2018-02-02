package code.name.monkey.backend.providers.interfaces;

import code.name.monkey.backend.model.Album;
import code.name.monkey.backend.model.Artist;
import code.name.monkey.backend.model.Genre;
import code.name.monkey.backend.model.Playlist;
import code.name.monkey.backend.model.Song;

import java.io.File;
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

    Observable<File> downloadLrcFile(final String title, final String artist, final long duration);
}
