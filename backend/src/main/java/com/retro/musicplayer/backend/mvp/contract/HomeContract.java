package com.retro.musicplayer.backend.mvp.contract;

import com.retro.musicplayer.backend.mvp.BasePresenter;
import com.retro.musicplayer.backend.mvp.BaseView;

import java.util.ArrayList;


/**
 * Created by hemanths on 20/08/17.
 */

public interface HomeContract {
    interface HomeView extends BaseView {
        void showAllThingsList(ArrayList<Object> homes);
    }

    interface HomePresenter extends BasePresenter<HomeView> {
        void loadAllThings();
    }
}
