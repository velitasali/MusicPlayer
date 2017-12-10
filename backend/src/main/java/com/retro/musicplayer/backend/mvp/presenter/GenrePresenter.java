package com.retro.musicplayer.backend.mvp.presenter;

import android.support.annotation.NonNull;

import com.retro.musicplayer.backend.model.Genre;
import com.retro.musicplayer.backend.mvp.Presenter;
import com.retro.musicplayer.backend.mvp.contract.GenreContract;
import com.retro.musicplayer.backend.providers.interfaces.Repository;

import java.util.ArrayList;

import io.reactivex.functions.Function;

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
                .map(genres -> {
                    ArrayList<Genre> list = new ArrayList<>();
                    for (Genre genre : genres) {
                        if (genre.songCount > 0) {
                            list.add(genre);
                        }
                    }
                    return list;
                })
                .doOnSubscribe(disposable1 -> view.loading())
                .subscribe(this::showList,
                        throwable -> view.showEmptyView(),
                        () -> view.completed()));
    }

    private void showList(@NonNull ArrayList<Genre> songs) {
        if (songs.isEmpty()) {
            view.showEmptyView();
        } else {
            view.showGenre(songs);
        }
    }
}
