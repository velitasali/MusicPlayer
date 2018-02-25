package code.name.monkey.backend.mvp.presenter;

import android.support.annotation.NonNull;

import code.name.monkey.backend.mvp.Presenter;
import code.name.monkey.backend.mvp.contract.HomeContract;
import code.name.monkey.backend.providers.interfaces.Repository;


/**
 * Created by hemanths on 20/08/17.
 */

public class HomePresenter extends Presenter implements HomeContract.HomePresenter {
    @NonNull
    private HomeContract.HomeView view;

    public HomePresenter(@NonNull Repository repository,
                         @NonNull HomeContract.HomeView view) {
        super(repository);
        this.view = view;
    }


    @Override
    public void subscribe() {
        loadAllThings();
    }

    @Override
    public void unsubscribe() {
        disposable.clear();
    }

    @Override
    public void loadAllThings() {
        disposable.add(repository.getRecentArtists()
                .observeOn(schedulerProvider.io())
                .subscribeOn(schedulerProvider.ui())
                .doOnSubscribe(disposable1 -> view.loading())
                .subscribe(artists -> {
                            if (!artists.isEmpty()) view.recentArtist(artists);
                        },
                        throwable -> view.showEmptyView(), () -> view.completed()));

        disposable.add(repository.getTopArtists()
                .observeOn(schedulerProvider.io())
                .subscribeOn(schedulerProvider.ui())
                .doOnSubscribe(disposable1 -> view.loading())
                .subscribe(artists -> {
                            if (!artists.isEmpty()) view.topArtists(artists);
                        },
                        throwable -> view.showEmptyView(), () -> view.completed()));

        disposable.add(repository.getRecentAlbums()
                .observeOn(schedulerProvider.io())
                .subscribeOn(schedulerProvider.ui())
                .doOnSubscribe(disposable1 -> view.loading())
                .subscribe(artists -> {
                            if (!artists.isEmpty()) view.recentAlbum(artists);
                        },
                        throwable -> view.showEmptyView(), () -> view.completed()));

        disposable.add(repository.getTopAlbums()
                .observeOn(schedulerProvider.io())
                .subscribeOn(schedulerProvider.ui())
                .doOnSubscribe(disposable1 -> view.loading())
                .subscribe(artists -> {
                            if (!artists.isEmpty()) view.topAlbums(artists);
                        },
                        throwable -> view.showEmptyView(), () -> view.completed()));

        disposable.add(repository.getSuggestionSongs()
                .observeOn(schedulerProvider.io())
                .subscribeOn(schedulerProvider.ui())
                .doOnSubscribe(disposable1 -> view.loading())
                .subscribe(artists -> {
                            if (!artists.isEmpty()) view.suggestions(artists);
                        },
                        throwable -> view.showEmptyView(), () -> view.completed()));


    }
}
