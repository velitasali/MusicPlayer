package code.name.monkey.backend.mvp.contract;

import code.name.monkey.backend.model.Song;
import code.name.monkey.backend.mvp.BasePresenter;
import code.name.monkey.backend.mvp.BaseView;

import java.util.ArrayList;

/**
 * @author Hemanth S (h4h13).
 */

public interface GenreDetailsContract {
    interface GenreDetailsView extends BaseView<ArrayList<Song>> {
    }

    interface Presenter extends BasePresenter<GenreDetailsView> {
        void loadGenre(int genreId);
    }
}
