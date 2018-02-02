package code.name.monkey.backend.mvp.contract;


import code.name.monkey.backend.model.Album;
import code.name.monkey.backend.mvp.BasePresenter;
import code.name.monkey.backend.mvp.BaseView;


/**
 * Created by hemanths on 20/08/17.
 */

public interface AlbumDetailsContract {
    interface AlbumDetailsView extends BaseView<Album> {

    }

    interface Presenter extends BasePresenter<AlbumDetailsView> {
        void loadAlbumSongs(int albumId);
    }
}
