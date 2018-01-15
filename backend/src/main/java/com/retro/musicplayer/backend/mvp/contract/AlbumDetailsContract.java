package com.retro.musicplayer.backend.mvp.contract;


import com.retro.musicplayer.backend.model.Album;
import com.retro.musicplayer.backend.mvp.BasePresenter;
import com.retro.musicplayer.backend.mvp.BaseView;


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
