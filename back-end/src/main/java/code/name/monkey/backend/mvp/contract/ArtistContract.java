package code.name.monkey.backend.mvp.contract;


import code.name.monkey.backend.model.Artist;
import code.name.monkey.backend.mvp.BasePresenter;
import code.name.monkey.backend.mvp.BaseView;

import java.util.ArrayList;


/**
 * Created by hemanths on 16/08/17.
 */

public interface ArtistContract {
    interface ArtistView extends BaseView<ArrayList<Artist>> {

    }

    interface Presenter extends BasePresenter<ArtistView> {
        void loadArtists();
    }
}
