package code.name.monkey.backend.mvp.contract;

import code.name.monkey.backend.model.Album;
import code.name.monkey.backend.mvp.BasePresenter;
import code.name.monkey.backend.mvp.BaseView;

import java.util.ArrayList;


/**
 * Created by hemanths on 12/08/17.
 */

public interface AlbumContract {

    interface AlbumView extends BaseView<ArrayList<Album>> {

    }

    interface Presenter extends BasePresenter<AlbumView> {
        void loadAlbums();
    }

}
