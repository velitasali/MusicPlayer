package code.name.monkey.backend.mvp.presenter;

import android.support.annotation.NonNull;

import code.name.monkey.backend.model.Song;
import code.name.monkey.backend.mvp.Presenter;
import code.name.monkey.backend.mvp.contract.GenreDetailsContract;
import code.name.monkey.backend.providers.interfaces.Repository;

import java.util.ArrayList;


/**
 * Created by hemanths on 20/08/17.
 */

public class GenreDetailsPresenter extends Presenter
        implements GenreDetailsContract.Presenter {
    @NonNull
    private final int genreId;
    @NonNull
    private GenreDetailsContract.GenreDetailsView view;

    public GenreDetailsPresenter(@NonNull Repository repository,
                                 @NonNull GenreDetailsContract.GenreDetailsView view,
                                 @NonNull int genreId) {
        super(repository);
        this.view = view;
        this.genreId = genreId;
    }

    @Override
    public void subscribe() {
        loadGenre(genreId);
    }

    @Override
    public void unsubscribe() {
        disposable.clear();
    }

    @Override
    public void loadGenre(int genreId) {
        disposable.add(repository.getGenre(genreId)
                .subscribeOn(schedulerProvider.computation())
                .observeOn(schedulerProvider.ui())
                .doOnSubscribe(disposable1 -> view.loading())
                .subscribe(this::showGenre,
                        throwable -> view.showEmptyView(),
                        () -> view.completed()));
    }

    private void showGenre(ArrayList<Song> songs) {
        if (songs != null) {
            view.showData(songs);
        } else {
            view.showEmptyView();
        }
    }
}
