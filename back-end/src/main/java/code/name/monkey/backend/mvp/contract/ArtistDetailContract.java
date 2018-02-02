package code.name.monkey.backend.mvp.contract;


import code.name.monkey.backend.model.Artist;
import code.name.monkey.backend.mvp.BasePresenter;
import code.name.monkey.backend.mvp.BaseView;


/**
 * Created by hemanths on 20/08/17.
 */

public interface ArtistDetailContract {
    interface ArtistsDetailsView extends BaseView<Artist> {

    }

    interface Presenter extends BasePresenter<ArtistsDetailsView> {
        void loadArtistById(int artistId);
    }
}
