package com.retro.musicplayer.backend.mvp.contract;

import com.retro.musicplayer.backend.model.Genre;
import com.retro.musicplayer.backend.mvp.BasePresenter;
import com.retro.musicplayer.backend.mvp.BaseView;

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
