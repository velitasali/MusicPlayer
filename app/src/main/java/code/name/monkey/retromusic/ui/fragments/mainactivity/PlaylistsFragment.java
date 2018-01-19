package code.name.monkey.retromusic.ui.fragments.mainactivity;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;

import code.name.monkey.backend.Injection;
import code.name.monkey.backend.model.Playlist;
import code.name.monkey.backend.mvp.contract.PlaylistContract;
import code.name.monkey.backend.mvp.presenter.PlaylistPresenter;

import java.util.ArrayList;

import code.name.monkey.retromusic.R;
import code.name.monkey.retromusic.ui.adapter.PlaylistAdapter;
import code.name.monkey.retromusic.ui.fragments.base.AbsLibraryPagerRecyclerViewFragment;
import code.name.monkey.retromusic.util.PreferenceUtil;

/**
 * Created by hemanths on 19/08/17.
 */

public class PlaylistsFragment extends AbsLibraryPagerRecyclerViewFragment<PlaylistAdapter, LinearLayoutManager> implements PlaylistContract.PlaylistView {
    private PlaylistPresenter mPresenter;

    public static PlaylistsFragment newInstance() {
        Bundle args = new Bundle();
        PlaylistsFragment fragment = new PlaylistsFragment();
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        mPresenter = new PlaylistPresenter(Injection.provideRepository(getContext()), this);
    }

    @Override
    protected LinearLayoutManager createLayoutManager() {
        return new LinearLayoutManager(getActivity());
    }

    @NonNull
    @Override
    protected PlaylistAdapter createAdapter() {
        return new PlaylistAdapter(getLibraryFragment().getMainActivity(), new ArrayList<>(), R.layout.item_list, null);
    }

    @Override
    public void setMenuVisibility(boolean menuVisible) {
        super.setMenuVisibility(menuVisible);
        if (menuVisible)
            getLibraryFragment().getToolbar().setTitle(PreferenceUtil.getInstance(getContext()).tabTitles() ? R.string.library : R.string.playlists);
    }

    @Override
    public void onResume() {
        super.onResume();
        getLibraryFragment().getToolbar().setTitle(PreferenceUtil.getInstance(getContext()).tabTitles() ? R.string.library : R.string.playlists);
        if (getAdapter().getDataSet().isEmpty()) mPresenter.subscribe();
    }

    @Override
    public void onDestroy() {
        mPresenter.unsubscribe();
        super.onDestroy();
    }

    @Override
    public void onMediaStoreChanged() {
        super.onMediaStoreChanged();
        mPresenter.loadPlaylists();
    }

    @Override
    public void loading() {
        getProgressBar().setVisibility(View.VISIBLE);
    }

    @Override
    public void showEmptyView() {
        getAdapter().swapDataSet(new ArrayList<>());
    }

    @Override
    public void completed() {
        getProgressBar().setVisibility(View.GONE);
    }

    @Override
    public void showData(ArrayList<Playlist> songs) {
        getAdapter().swapDataSet(songs);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        menu.removeItem(R.id.action_shuffle_all);
        menu.removeItem(R.id.action_sort_order);
    }

    @Override
    protected int getEmptyMessage() {
        return R.string.no_playlists;
    }
}
