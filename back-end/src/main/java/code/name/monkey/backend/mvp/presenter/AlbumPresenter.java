package code.name.monkey.backend.mvp.presenter;

import android.support.annotation.NonNull;

import code.name.monkey.backend.model.Album;
import code.name.monkey.backend.mvp.Presenter;
import code.name.monkey.backend.mvp.contract.AlbumContract;
import code.name.monkey.backend.providers.interfaces.Repository;

import java.util.ArrayList;


/**
 * Created by hemanths on 12/08/17.
 */

public class AlbumPresenter extends Presenter implements AlbumContract.Presenter {
    @NonNull
    private AlbumContract.AlbumView view;


    public AlbumPresenter(@NonNull Repository repository,
                          @NonNull AlbumContract.AlbumView view) {
        super(repository);
        this.view = view;
    }

    @Override
    public void subscribe() {
        loadAlbums();
    }

    @Override
    public void unsubscribe() {
        disposable.clear();
    }

    private void showList(@NonNull ArrayList<Album> albums) {
        view.showData(albums);
    }

    @Override
    public void loadAlbums() {
        disposable.add(repository.getAllAlbums()
                .subscribeOn(schedulerProvider.computation())
                .observeOn(schedulerProvider.ui())
                .doOnSubscribe(disposable1 -> view.loading())
                .subscribe(this::showList,
                        throwable -> view.showEmptyView(),
                        () -> view.completed()));
    }
}
