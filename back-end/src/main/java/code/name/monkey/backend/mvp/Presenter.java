package code.name.monkey.backend.mvp;

import android.support.annotation.NonNull;

import code.name.monkey.backend.Injection;
import code.name.monkey.backend.providers.interfaces.Repository;
import code.name.monkey.backend.util.schedulers.BaseSchedulerProvider;

import io.reactivex.disposables.CompositeDisposable;

/**
 * Created by hemanths on 16/08/17.
 */

public class Presenter {
    @NonNull
    protected Repository repository;
    @NonNull
    protected CompositeDisposable disposable;
    @NonNull
    protected BaseSchedulerProvider schedulerProvider;

    public Presenter(@NonNull Repository repository) {
        this.repository = repository;
        this.schedulerProvider = Injection.provideSchedulerProvider();
        this.disposable = new CompositeDisposable();
    }
}
