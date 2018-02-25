package code.name.monkey.backend.mvp.contract;

import java.util.ArrayList;

import code.name.monkey.backend.model.Album;
import code.name.monkey.backend.model.Artist;
import code.name.monkey.backend.model.Song;
import code.name.monkey.backend.mvp.BasePresenter;
import code.name.monkey.backend.mvp.BaseView;


/**
 * Created by hemanths on 20/08/17.
 */

public interface HomeContract {
    interface HomeView extends BaseView<ArrayList<Object>> {
        void recentArtist(ArrayList<Artist> artists);

        void recentAlbum(ArrayList<Album> albums);

        void topArtists(ArrayList<Artist> artists);

        void topAlbums(ArrayList<Album> albums);

        void suggestions(ArrayList<Song> songs);
    }

    interface HomePresenter extends BasePresenter<HomeView> {
        void loadAllThings();
    }
}
