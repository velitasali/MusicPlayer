package code.name.monkey.backend.mvp.contract;

import code.name.monkey.backend.model.Playlist;
import code.name.monkey.backend.model.Song;
import code.name.monkey.backend.mvp.BasePresenter;
import code.name.monkey.backend.mvp.BaseView;

import java.util.ArrayList;


/**
 * Created by hemanths on 20/08/17.
 */

public interface PlaylistSongsContract {
    interface PlaylistSongsView extends BaseView<ArrayList<Song>> {

    }

    interface Presenter extends BasePresenter<PlaylistSongsView> {
        void loadSongs(Playlist playlist);
    }
}
