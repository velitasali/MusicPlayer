package com.retro.musicplayer.backend.mvp.contract;

import com.retro.musicplayer.backend.model.Song;
import com.retro.musicplayer.backend.mvp.BasePresenter;
import com.retro.musicplayer.backend.mvp.BaseView;

import java.util.ArrayList;

/**
 * @author Hemanth S (h4h13).
 */

public interface GenreDetailsContract {
    interface GenreDetailsView extends BaseView {
        void showList(ArrayList<Song> songs);
    }

    interface Presenter extends BasePresenter<GenreDetailsView> {
        void loadGenre(int genreId);
    }
}
