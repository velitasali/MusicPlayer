package code.name.monkey.retromusic.ui.fragments.mainactivity;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.view.Menu;
import android.view.MenuInflater;

import com.retro.musicplayer.backend.Injection;
import com.retro.musicplayer.backend.model.Genre;
import com.retro.musicplayer.backend.mvp.contract.GenreContract;
import com.retro.musicplayer.backend.mvp.presenter.GenrePresenter;

import java.util.ArrayList;

import code.name.monkey.retromusic.R;
import code.name.monkey.retromusic.ui.adapter.GenreAdapter;
import code.name.monkey.retromusic.ui.fragments.base.AbsLibraryPagerRecyclerViewFragment;

/**
 * @author Hemanth S (h4h13).
 */

public class GenreFragment extends AbsLibraryPagerRecyclerViewFragment<GenreAdapter, LinearLayoutManager>
        implements GenreContract.GenreView {
    private GenrePresenter mPresenter;

    public static GenreFragment newInstance() {
        Bundle args = new Bundle();
        GenreFragment fragment = new GenreFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        mPresenter = new GenrePresenter(Injection.provideRepository(getContext()), this);
    }

    @Override
    public void onResume() {
        super.onResume();
        mPresenter.subscribe();
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        mPresenter.unsubscribe();
    }

    @NonNull
    @Override
    protected LinearLayoutManager createLayoutManager() {
        return new LinearLayoutManager(getActivity());
    }

    @NonNull
    @Override
    protected GenreAdapter createAdapter() {
        return new GenreAdapter(getLibraryFragment().getMainActivity(), R.layout.item_list);
    }

    @Override
    public void loading() {

    }

    @Override
    public void showEmptyView() {
        getAdapter().swapData(new ArrayList<>());
    }

    @Override
    public void completed() {

    }

    @Override
    public void showGenre(ArrayList<Genre> list) {
        getAdapter().swapData(list);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        menu.removeItem(R.id.action_sort_order);
        menu.removeItem(R.id.action_grid_size);
        menu.removeItem(R.id.action_new_playlist);
    }
}
