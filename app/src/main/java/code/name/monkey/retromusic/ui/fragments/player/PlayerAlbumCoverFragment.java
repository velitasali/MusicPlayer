package code.name.monkey.retromusic.ui.fragments.player;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.view.ViewPager;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import com.name.monkey.retromusic.ui.fragments.player.NowPlayingScreen;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import code.name.monkey.backend.transform.CustPagerTransformer;
import code.name.monkey.backend.transform.NormalPageTransformer;
import code.name.monkey.backend.transform.ParallaxPagerTransformer;
import com.velitasali.music.R;
import code.name.monkey.retromusic.helper.MusicPlayerRemote;
import code.name.monkey.retromusic.ui.adapter.AlbumCoverPagerAdapter;
import code.name.monkey.retromusic.ui.fragments.base.AbsMusicServiceFragment;
import code.name.monkey.retromusic.util.PreferenceUtil;

/**
 * @author Karim Abou Zeid (kabouzeid)
 */
public class PlayerAlbumCoverFragment extends AbsMusicServiceFragment implements
        ViewPager.OnPageChangeListener {

    public static final String TAG = PlayerAlbumCoverFragment.class.getSimpleName();
    public static final long VISIBILITY_ANIM_DURATION = 300;
    @BindView(R.id.player_album_cover_viewpager)
    ViewPager viewPager;
    private Unbinder unbinder;
    private Callbacks callbacks;
    private int currentPosition;
    private AlbumCoverPagerAdapter.AlbumCoverFragment.ColorReceiver colorReceiver = new AlbumCoverPagerAdapter.AlbumCoverFragment.ColorReceiver() {
        @Override
        public void onColorReady(int color, int requestCode) {
            if (currentPosition == requestCode) {
                notifyColorChange(color);
            }
        }
    };

    public void removeSlideEffect() {
        ParallaxPagerTransformer transformer = new ParallaxPagerTransformer(R.id.player_image);
        transformer.setSpeed(0.3f);
        viewPager.setPageTransformer(true, transformer);

    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_player_album_cover, container, false);
        unbinder = ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewPager.addOnPageChangeListener(this);

        if (PreferenceUtil.getInstance(getContext()).carouselEffect() && !(
                (PreferenceUtil.getInstance(getContext()).getNowPlayingScreen() == NowPlayingScreen.FULL) ||
                        (PreferenceUtil.getInstance(getContext()).getNowPlayingScreen() == NowPlayingScreen.FLAT))) {
            viewPager.setClipToPadding(false);
            viewPager.setPadding(96, 0, 96, 0);
            viewPager.setPageMargin(18);

            viewPager.setPageTransformer(false, new CustPagerTransformer(getContext()));
        } else {
            viewPager.setPageTransformer(true, new NormalPageTransformer());
        }


    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        viewPager.removeOnPageChangeListener(this);
        unbinder.unbind();
    }

    @Override
    public void onServiceConnected() {
        updatePlayingQueue();
    }

    @Override
    public void onPlayingMetaChanged() {
        viewPager.setCurrentItem(MusicPlayerRemote.getPosition());
    }

    @Override
    public void onQueueChanged() {
        updatePlayingQueue();
    }

    private void updatePlayingQueue() {
        viewPager.setAdapter(new AlbumCoverPagerAdapter(getFragmentManager(), MusicPlayerRemote.getPlayingQueue()));
        viewPager.getAdapter().notifyDataSetChanged();
        viewPager.setCurrentItem(MusicPlayerRemote.getPosition());
        onPageSelected(MusicPlayerRemote.getPosition());

    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {
        currentPosition = position;
        if (viewPager.getAdapter() != null) {
            ((AlbumCoverPagerAdapter) viewPager.getAdapter()).receiveColor(colorReceiver, position);
        }
        if (position != MusicPlayerRemote.getPosition()) {
            MusicPlayerRemote.playSongAt(position);
        }
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }


    private void notifyColorChange(int color) {
        if (callbacks != null) callbacks.onColorChanged(color);
    }

    public void setCallbacks(Callbacks listener) {
        callbacks = listener;
    }


    public interface Callbacks {
        void onColorChanged(int color);

        void onFavoriteToggled();

    }
}
