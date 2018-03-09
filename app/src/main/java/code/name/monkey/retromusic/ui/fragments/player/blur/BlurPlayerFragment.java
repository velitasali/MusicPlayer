package code.name.monkey.retromusic.ui.fragments.player.blur;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.h6ah4i.android.widget.advrecyclerview.animator.GeneralItemAnimator;
import com.h6ah4i.android.widget.advrecyclerview.animator.RefactoredDefaultItemAnimator;
import com.h6ah4i.android.widget.advrecyclerview.draggable.RecyclerViewDragDropManager;
import com.h6ah4i.android.widget.advrecyclerview.utils.WrapperAdapterUtils;
import com.velitasali.music.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import code.name.monkey.appthemehelper.util.ToolbarContentTintHelper;
import code.name.monkey.backend.model.Song;
import code.name.monkey.retromusic.glide.RetroMusicColoredTarget;
import code.name.monkey.retromusic.glide.SongGlideRequest;
import code.name.monkey.retromusic.helper.MusicPlayerRemote;
import code.name.monkey.retromusic.ui.adapter.song.PlayingQueueAdapter;
import code.name.monkey.retromusic.ui.fragments.base.AbsPlayerFragment;
import code.name.monkey.retromusic.ui.fragments.player.PlayerAlbumCoverFragment;
import code.name.monkey.retromusic.ui.fragments.player.normal.PlayerFragment;
import jp.wasabeef.glide.transformations.BlurTransformation;

/**
 * @author Hemanth S (h4h13).
 */

public class BlurPlayerFragment extends AbsPlayerFragment implements PlayerAlbumCoverFragment.Callbacks
{
	@BindView(R.id.player_toolbar)
	Toolbar toolbar;
	@BindView(R.id.gradient_background)
	ImageView colorBackground;
	@BindView(R.id.toolbar_container)
	FrameLayout toolbarContainer;
	@BindView(R.id.now_playing_container)
	ViewGroup viewGroup;
	@Nullable
	@BindView(R.id.recycler_view)
	RecyclerView mRecyclerView;
	private int lastColor;
	private BlurPlaybackControlsFragment playbackControlsFragment;
	private Unbinder unbinder;

	private RecyclerView.Adapter mWrappedAdapter;
	private RecyclerViewDragDropManager mRecyclerViewDragDropManager;
	private PlayingQueueAdapter mPlayingQueueAdapter;
	private LinearLayoutManager mLayoutManager;

	public static PlayerFragment newInstance()
	{
		Bundle args = new Bundle();
		PlayerFragment fragment = new PlayerFragment();
		fragment.setArguments(args);
		return fragment;
	}

	@Override
	@ColorInt
	public int getPaletteColor()
	{
		return lastColor;
	}

	@Override
	public void onShow()
	{
		playbackControlsFragment.show();
	}

	@Override
	public void onHide()
	{
		playbackControlsFragment.hide();
		onBackPressed();
	}

	@Override
	public void onResume()
	{
		super.onResume();
		checkToggleToolbar(toolbarContainer);
	}

	@Override
	public boolean onBackPressed()
	{
		return false;
	}

	@Override
	public Toolbar getToolbar()
	{
		return toolbar;
	}

	@Override
	public int toolbarIconColor()
	{
		return Color.WHITE;
	}

	@Override
	public void onColorChanged(int color)
	{
		playbackControlsFragment.setDark(color);
		lastColor = color;
		getCallbacks().onPaletteColorChanged();

		ToolbarContentTintHelper.colorizeToolbar(toolbar, Color.WHITE, getActivity());
	}

	@Override
	protected void toggleFavorite(Song song)
	{
		super.toggleFavorite(song);
		if (song.id == MusicPlayerRemote.getCurrentSong().id) {
			updateIsFavorite();
		}
	}

	@Override
	public void onFavoriteToggled()
	{
		toggleFavorite(MusicPlayerRemote.getCurrentSong());
	}

	@Override
	public void onDestroyView()
	{
		super.onDestroyView();
		if (mRecyclerViewDragDropManager != null) {
			mRecyclerViewDragDropManager.release();
			mRecyclerViewDragDropManager = null;
		}

		if (mRecyclerView != null) {
			mRecyclerView.setItemAnimator(null);
			mRecyclerView.setAdapter(null);
			mRecyclerView = null;
		}

		if (mWrappedAdapter != null) {
			WrapperAdapterUtils.releaseAll(mWrappedAdapter);
			mWrappedAdapter = null;
		}
		mPlayingQueueAdapter = null;
		mLayoutManager = null;
		super.onDestroyView();
		unbinder.unbind();
	}

	@Nullable
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater,
							 @Nullable ViewGroup container,
							 @Nullable Bundle savedInstanceState)
	{
		View view = inflater.inflate(R.layout.fragment_blur, container, false);
		unbinder = ButterKnife.bind(this, view);
		return view;
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState)
	{
		super.onViewCreated(view, savedInstanceState);
		toggleStatusBar(viewGroup);

		setUpSubFragments();
		setUpPlayerToolbar();
	}

	private void setUpSubFragments()
	{
		playbackControlsFragment = (BlurPlaybackControlsFragment) getChildFragmentManager().findFragmentById(R.id.playback_controls_fragment);

		PlayerAlbumCoverFragment playerAlbumCoverFragment = (PlayerAlbumCoverFragment) getChildFragmentManager().findFragmentById(R.id.player_album_cover_fragment);
		playerAlbumCoverFragment.setCallbacks(this);
	}

	private void setUpPlayerToolbar()
	{
		toolbar.inflateMenu(R.menu.menu_player);
		toolbar.setNavigationOnClickListener(v -> getActivity().onBackPressed());
		toolbar.setOnMenuItemClickListener(this);

		ToolbarContentTintHelper.colorizeToolbar(toolbar, Color.WHITE, getActivity());
	}

	private void updateBlur()
	{
		Activity activity = getActivity();
		if (activity == null) {
			return;
		}
		colorBackground.clearColorFilter();
		SongGlideRequest.Builder.from(Glide.with(activity), MusicPlayerRemote.getCurrentSong())
				.checkIgnoreMediaStore(activity)
				.generatePalette(activity)
				.build()
				.transform(new BlurTransformation(getActivity(), 150))
				.into(new RetroMusicColoredTarget(colorBackground)
				{
					@Override
					public void onColorReady(int color)
					{
						if (color == getDefaultFooterColor()) {
							colorBackground.setColorFilter(color);
						}
					}
				});
	}

	@Override
	public void onServiceConnected()
	{
		updateIsFavorite();
		updateBlur();
		setUpRecyclerView();
	}

	@Override
	public void onPlayingMetaChanged()
	{
		updateIsFavorite();
		updateBlur();
		updateQueuePosition();
	}

	private void setUpRecyclerView()
	{
		if (mRecyclerView != null) {
			mRecyclerViewDragDropManager = new RecyclerViewDragDropManager();
			final GeneralItemAnimator animator = new RefactoredDefaultItemAnimator();

			mPlayingQueueAdapter = new PlayingQueueAdapter(
					(AppCompatActivity) getActivity(),
					MusicPlayerRemote.getPlayingQueue(),
					MusicPlayerRemote.getPosition(),
					R.layout.item_song,
					false,
					null);
			mWrappedAdapter = mRecyclerViewDragDropManager.createWrappedAdapter(mPlayingQueueAdapter);

			mLayoutManager = new LinearLayoutManager(getContext());


			mRecyclerView.setLayoutManager(mLayoutManager);
			mRecyclerView.setAdapter(mWrappedAdapter);
			mRecyclerView.setItemAnimator(animator);
			mRecyclerViewDragDropManager.attachRecyclerView(mRecyclerView);
			mLayoutManager.scrollToPositionWithOffset(MusicPlayerRemote.getPosition() + 1, 0);
		}
	}

	@Override
	public void onQueueChanged()
	{
		updateQueue();
		updateCurrentSong();
	}

	@Override
	public void onMediaStoreChanged()
	{
		updateQueue();
		updateCurrentSong();
	}

	@SuppressWarnings("ConstantConditions")
	private void updateCurrentSong()
	{
	}

	private void updateQueuePosition()
	{
		if (mPlayingQueueAdapter != null) {
			mPlayingQueueAdapter.setCurrent(MusicPlayerRemote.getPosition());
			// if (slidingUpPanelLayout.getPanelState() == SlidingUpPanelLayout.PanelState.COLLAPSED) {
			resetToCurrentPosition();
			//}
		}
	}

	private void updateQueue()
	{
		if (mPlayingQueueAdapter != null) {
			mPlayingQueueAdapter.swapDataSet(MusicPlayerRemote.getPlayingQueue(), MusicPlayerRemote.getPosition());
			resetToCurrentPosition();
		}
	}

	private void resetToCurrentPosition()
	{
		if (mRecyclerView != null) {
			mRecyclerView.stopScroll();
			mLayoutManager.scrollToPositionWithOffset(MusicPlayerRemote.getPosition() + 1, 0);
		}

	}

	@Override
	public void onPause()
	{
		if (mRecyclerViewDragDropManager != null) {
			mRecyclerViewDragDropManager.cancelDrag();
		}
		super.onPause();
	}

}
