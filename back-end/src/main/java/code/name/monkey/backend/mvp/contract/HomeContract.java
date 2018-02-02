package code.name.monkey.backend.mvp.contract;

import code.name.monkey.backend.mvp.BasePresenter;
import code.name.monkey.backend.mvp.BaseView;

import java.util.ArrayList;


/**
 * Created by hemanths on 20/08/17.
 */

public interface HomeContract {
    interface HomeView extends BaseView<ArrayList<Object>> {

    }

    interface HomePresenter extends BasePresenter<HomeView> {
        void loadAllThings();
    }
}
