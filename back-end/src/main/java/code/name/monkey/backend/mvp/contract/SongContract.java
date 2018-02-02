package code.name.monkey.backend.mvp.contract;


import code.name.monkey.backend.model.Song;
import code.name.monkey.backend.mvp.BasePresenter;
import code.name.monkey.backend.mvp.BaseView;

import java.util.ArrayList;


/**
 * Created by hemanths on 10/08/17.
 */

public interface SongContract {

    interface SongView extends BaseView<ArrayList<Song>> {

    }

    interface Presenter extends BasePresenter<SongView> {
        void loadSongs();
    }
}
