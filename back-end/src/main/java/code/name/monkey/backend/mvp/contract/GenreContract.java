package code.name.monkey.backend.mvp.contract;

import code.name.monkey.backend.model.Genre;
import code.name.monkey.backend.mvp.BasePresenter;
import code.name.monkey.backend.mvp.BaseView;

import java.util.ArrayList;

/**
 * @author Hemanth S (h4h13).
 */

public interface GenreContract {
    interface GenreView extends BaseView<ArrayList<Genre>> {

    }

    interface Presenter extends BasePresenter<GenreView> {
        void loadGenre();
    }
}
