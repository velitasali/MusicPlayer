package code.name.monkey.backend.mvp.presenter;

import android.content.Context;
import android.support.annotation.NonNull;

import code.name.monkey.backend.model.Playlist;
import code.name.monkey.backend.model.Song;
import code.name.monkey.backend.model.smartplaylist.AbsSmartPlaylist;
import code.name.monkey.backend.model.smartplaylist.HistoryPlaylist;
import code.name.monkey.backend.mvp.Presenter;
import code.name.monkey.backend.mvp.contract.PlaylistContract;
import code.name.monkey.backend.providers.interfaces.Repository;
import io.reactivex.Observable;

import java.util.ArrayList;


/**
 * Created by hemanths on 19/08/17.
 */

public class PlaylistPresenter extends Presenter
        implements PlaylistContract.Presenter {
    @NonNull
    private PlaylistContract.PlaylistView mView;

    public PlaylistPresenter(@NonNull Repository repository,
                             @NonNull PlaylistContract.PlaylistView view) {
        super(repository);
        mView = view;
    }

    @Override
    public void subscribe() {
        loadPlaylists();
    }

    @Override
    public void unsubscribe() {
        disposable.clear();
    }

    @Override
    public void loadPlaylists() {
        disposable.add(repository.getAllPlaylists()
                .subscribeOn(schedulerProvider.computation())
                .observeOn(schedulerProvider.ui())
                .doOnSubscribe(disposable1 -> mView.loading())
                .subscribe(this::showList,
                        throwable -> mView.showEmptyView(),
                        () -> mView.completed()));
    }

    private void showList(@NonNull ArrayList<Playlist> songs) {
        mView.showData(songs);
    }
}
