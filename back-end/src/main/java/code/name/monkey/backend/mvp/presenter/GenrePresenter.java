package code.name.monkey.backend.mvp.presenter;

import android.support.annotation.NonNull;

import code.name.monkey.backend.model.Genre;
import code.name.monkey.backend.mvp.Presenter;
import code.name.monkey.backend.mvp.contract.GenreContract;
import code.name.monkey.backend.providers.interfaces.Repository;

import java.util.ArrayList;

/**
 * @author Hemanth S (h4h13).
 */

public class GenrePresenter extends Presenter
        implements GenreContract.Presenter {
    @NonNull
    private GenreContract.GenreView view;

    public GenrePresenter(@NonNull Repository repository,
                          @NonNull GenreContract.GenreView view) {
        super(repository);
        this.view = view;
    }

    @Override
    public void subscribe() {
        loadGenre();
    }

    @Override
    public void unsubscribe() {
        disposable.clear();
    }

    @Override
    public void loadGenre() {
        disposable.add(repository.getAllGenres()
                .subscribeOn(schedulerProvider.computation())
                .observeOn(schedulerProvider.ui())
                .doOnSubscribe(disposable1 -> view.loading())
                .subscribe(this::showList,
                        throwable -> view.showEmptyView(),
                        () -> view.completed()));
    }

    private void showList(@NonNull ArrayList<Genre> genres) {
        if (genres.isEmpty()) {
            view.showEmptyView();
        } else {
            view.showData(genres);
        }
    }
}
