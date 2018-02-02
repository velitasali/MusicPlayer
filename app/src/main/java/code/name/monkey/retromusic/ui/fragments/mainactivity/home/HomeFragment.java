package code.name.monkey.retromusic.ui.fragments.mainactivity.home;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;

import java.util.ArrayList;

import code.name.monkey.backend.Injection;
import code.name.monkey.backend.mvp.contract.HomeContract;
import code.name.monkey.backend.mvp.presenter.HomePresenter;
import code.name.monkey.retromusic.R;
import code.name.monkey.retromusic.ui.adapter.home.HomeAdapter;
import code.name.monkey.retromusic.ui.fragments.base.AbsLibraryPagerRecyclerViewFragment;

public class HomeFragment extends AbsLibraryPagerRecyclerViewFragment<HomeAdapter, LinearLayoutManager> implements
        HomeContract.HomeView {
    private HomePresenter presenter;

    public static HomeFragment newInstance() {
        Bundle args = new Bundle();
        HomeFragment fragment = new HomeFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        presenter = new HomePresenter(Injection.provideRepository(getContext()), this);

    }

    @Override
    public void setMenuVisibility(boolean menuVisible) {
        super.setMenuVisibility(menuVisible);
        if (menuVisible)
            getLibraryFragment().getToolbar().setTitle(R.string.home);
    }

    @Override
    public void onResume() {
        super.onResume();
        getLibraryFragment().getUserInfo().setVisibility(View.VISIBLE);
        getLibraryFragment().getToolbar().setTitle(R.string.home);
        presenter.subscribe();
    }

    @Override
    public void onDestroy() {
        presenter.unsubscribe();
        super.onDestroy();
    }

    @Override
    public void onMediaStoreChanged() {
        super.onMediaStoreChanged();
        presenter.loadAllThings();
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
    protected LinearLayoutManager createLayoutManager() {
        return new LinearLayoutManager(getContext());
    }

    @NonNull
    @Override
    protected HomeAdapter createAdapter() {
        return new HomeAdapter(getLibraryFragment().getMainActivity());
    }

    @Override
    public void showData(ArrayList<Object> list) {
        getAdapter().swapDataSet(list);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        menu.removeItem(R.id.action_shuffle_all);
        menu.removeItem(R.id.action_sort_order);
        menu.removeItem(R.id.action_search);
        menu.removeItem(R.id.action_grid_size);
        menu.removeItem(R.id.action_colored_footers);
        menu.removeItem(R.id.action_sleep_timer);
        menu.removeItem(R.id.action_equalizer);
        menu.removeItem(R.id.action_new_playlist);
    }



   /* @BindView(R.id.playlist_recycler_view)
    RecyclerView recyclerView;
    Unbinder unbinder;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.appbar)
    AppBarLayout appbar;
    @BindView(R.id.user_image)
    CircleImageView mUserImage;
    @BindView(R.id.title)
    TextView title;*//*

    private HomePresenter homePresenter;
    private CompositeDisposable disposable;
    private HomeAdapter homeAdapter;

    public static HomeFragment newInstance() {
        Bundle args = new Bundle();
        HomeFragment fragment = new HomeFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //disposable = new CompositeDisposable();
        homePresenter = new HomePresenter(Injection.provideRepository(getContext()), this);
    }

   *//* @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        unbinder = ButterKnife.bind(this, view);
        return view;
    }*//*

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getMainActivity().getSlidingUpPanelLayout().setShadowHeight(8);
        setStatusbarColorAuto(view);
        getMainActivity().setTaskDescriptionColorAuto();
        getMainActivity().setNavigationbarColorAuto();
        getMainActivity().setBottomBarVisibility(View.VISIBLE);
        getMainActivity().hideStatusBar();

        setupToolbar();
        setupAdapter();
        loadImageFromStorage();
    }

    @Override
    protected LinearLayoutManager createLayoutManager() {
        return new LinearLayoutManager(getContext());
    }

    @NonNull
    @Override
    protected HomeAdapter createAdapter() {
        return new HomeAdapter(getLibraryFragment().getMainActivity());
    }

   *//* private void setupAdapter() {
        homeAdapter = new HomeAdapter(getMainActivity());
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(homeAdapter);
    }*//*

    private void loadImageFromStorage() {
        new Compressor(getContext())
                .setMaxHeight(300)
                .setMaxWidth(300)
                .setQuality(75)
                .setCompressFormat(Bitmap.CompressFormat.WEBP)
                .compressToBitmapAsFlowable(new File(PreferenceUtil.getInstance(getContext()).getProfileImage(), USER_PROFILE))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(bitmap -> mUserImage.setImageBitmap(bitmap),
                        throwable -> mUserImage.setImageDrawable(ContextCompat
                                .getDrawable(getContext(), R.drawable.ic_person_flat)));
    }

    @SuppressWarnings("ConstantConditions")
    private void setupToolbar() {

        toolbar.setTitle(R.string.home);
        toolbar.setNavigationIcon(R.drawable.ic_menu_white_24dp);
        toolbar.setBackgroundColor(ThemeStore.primaryColor(getContext()));
        getActivity().setTitle(R.string.app_name);
        //getMainActivity().setSupportActionBar(toolbar);

        title.setText(PreferenceUtil.getInstance(getContext()).getUserName());
        title.setTextColor(ThemeStore.textColorPrimary(getContext()));
    }

    *//*@OnClick(R.id.search)
    void search(View view) {
        Activity activity = getMainActivity();
        ActivityOptionsCompat optionsCompat =
                ActivityOptionsCompat.makeSceneTransitionAnimation(activity, new Pair<>(view, getString(R.string.transition_search_bar)));
        startActivity(new Intent(activity, SearchActivity.class), optionsCompat.toBundle());

    }*//*

    @Override
    public boolean handleBackPress() {
        return false;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        disposable.clear();
        homePresenter.unsubscribe();
    }

    @Override
    public void loading() {

    }

    @Override
    public void showEmptyView() {

    }

    @Override
    public void completed() {

    }

    @Override
    public void onResume() {
        super.onResume();
        if (homeAdapter.getDataset().isEmpty())
            homePresenter.subscribe();
    }

    @Override
    public void showData(ArrayList<Object> homes) {
        homeAdapter.swapDataSet(homes);
    }

    @Override
    public void selectedFragment(Fragment fragment) {
        *//*ignore the method*//*
    }

    private void loadTimeImage(String day) {
        *//*Glide.with(getActivity()).load(day)
                .asBitmap()
                .placeholder(R.drawable.material_design_default)
                .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                .into(imageView);*//*
    }

    private String getTimeOfTheDay() {
        String message = getString(R.string.title_good_day);
        Calendar c = Calendar.getInstance();
        int timeOfDay = c.get(Calendar.HOUR_OF_DAY);

        String[] images = new String[]{};
        if (timeOfDay >= 0 && timeOfDay < 6) {
            message = getString(R.string.title_good_night);
            images = getResources().getStringArray(R.array.night);
        } else if (timeOfDay >= 6 && timeOfDay < 12) {
            message = getString(R.string.title_good_morning);
            images = getResources().getStringArray(R.array.morning);
        } else if (timeOfDay >= 12 && timeOfDay < 16) {
            message = getString(R.string.title_good_afternoon);
            images = getResources().getStringArray(R.array.after_noon);
        } else if (timeOfDay >= 16 && timeOfDay < 20) {
            message = getString(R.string.title_good_evening);
            images = getResources().getStringArray(R.array.evening);
        } else if (timeOfDay >= 20 && timeOfDay < 24) {
            message = getString(R.string.title_good_night);
            images = getResources().getStringArray(R.array.night);
        }
        String day = images[new Random().nextInt(images.length)];
        loadTimeImage(day);
        return message;
    }

    @Override
    public void onMediaStoreChanged() {
        super.onMediaStoreChanged();
        homePresenter.subscribe();
    }*/
}
