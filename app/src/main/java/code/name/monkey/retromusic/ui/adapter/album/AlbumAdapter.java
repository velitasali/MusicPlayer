package code.name.monkey.retromusic.ui.adapter.album;

import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.util.Pair;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView;
import com.velitasali.music.R;

import java.util.ArrayList;
import java.util.List;

import code.name.monkey.backend.model.Album;
import code.name.monkey.backend.model.Song;
import code.name.monkey.retromusic.glide.RetroMusicColoredTarget;
import code.name.monkey.retromusic.glide.SongGlideRequest;
import code.name.monkey.retromusic.helper.menu.SongsMenuHelper;
import code.name.monkey.retromusic.interfaces.CabHolder;
import code.name.monkey.retromusic.ui.adapter.base.AbsMultiSelectAdapter;
import code.name.monkey.retromusic.ui.adapter.base.MediaEntryViewHolder;
import code.name.monkey.retromusic.util.MusicUtil;
import code.name.monkey.retromusic.util.NavigationUtil;
import uk.co.chrisjenx.calligraphy.CalligraphyUtils;

/**
 * @author Karim Abou Zeid (kabouzeid)
 */
public class AlbumAdapter extends AbsMultiSelectAdapter<AlbumAdapter.ViewHolder, Album> implements
		FastScrollRecyclerView.SectionedAdapter
{

	public static final String TAG = AlbumAdapter.class.getSimpleName();

	protected final AppCompatActivity activity;
	protected ArrayList<Album> dataSet;

	protected int itemLayoutRes;

	protected boolean usePalette = false;
	private Typeface mTypeface;

	public AlbumAdapter(@NonNull AppCompatActivity activity,
						ArrayList<Album> dataSet,
						@LayoutRes int itemLayoutRes,
						boolean usePalette,
						@Nullable CabHolder cabHolder)
	{
		super(activity, cabHolder, R.menu.menu_media_selection);
		this.activity = activity;
		this.dataSet = dataSet;
		this.itemLayoutRes = itemLayoutRes;
		this.usePalette = usePalette;
		setHasStableIds(true);
		mTypeface = Typeface.createFromAsset(activity.getAssets(), activity.getString(R.string.sans_regular));
	}

	public void usePalette(boolean usePalette)
	{
		this.usePalette = usePalette;
		notifyDataSetChanged();
	}

	public void swapDataSet(ArrayList<Album> dataSet)
	{
		this.dataSet = dataSet;
		notifyDataSetChanged();
	}

	public ArrayList<Album> getDataSet()
	{
		return dataSet;
	}

	@NonNull
	@Override
	public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
	{
		View view = LayoutInflater.from(activity).inflate(itemLayoutRes, parent, false);
		return createViewHolder(view, viewType);
	}

	protected ViewHolder createViewHolder(View view, int viewType)
	{
		return new ViewHolder(view);
	}

	private String getAlbumTitle(Album album)
	{
		return album.getTitle();
	}

	protected String getAlbumText(Album album)
	{
		return album.getArtistName();
	}

	@Override
	public void onBindViewHolder(@NonNull final ViewHolder holder, int position)
	{
		final Album album = dataSet.get(position);

		final boolean isChecked = isChecked(album);
		holder.itemView.setActivated(isChecked);

		if (holder.getAdapterPosition() == getItemCount() - 1) {
			if (holder.shortSeparator != null) {
				holder.shortSeparator.setVisibility(View.GONE);
			}
		} else {
			if (holder.shortSeparator != null) {
				holder.shortSeparator.setVisibility(View.VISIBLE);
			}
		}

		if (holder.title != null) {
			holder.title.setText(getAlbumTitle(album));
		}
		if (holder.text != null) {
			holder.text.setText(getAlbumText(album));
		}

		loadAlbumCover(album, holder);
	}

	protected void setColors(int color, ViewHolder holder)
	{
	   /* if (holder.paletteColorContainer != null) {
            holder.paletteColorContainer.setBackgroundColor(color);
            if (holder.title != null) {
                holder.title.setTextColor(MaterialValueHelper.getPrimaryTextColor(activity, ColorUtil.isColorLight(color)));
            }
            if (holder.text != null) {
                holder.text.setTextColor(MaterialValueHelper.getSecondaryTextColor(activity, ColorUtil.isColorLight(color)));
            }
        }*/
	}

	protected void loadAlbumCover(Album album, final ViewHolder holder)
	{
		if (holder.image == null) return;

		SongGlideRequest.Builder.from(Glide.with(activity), album.safeGetFirstSong())
				.checkIgnoreMediaStore(activity)
				.generatePalette(activity).build()
				.into(new RetroMusicColoredTarget(holder.image)
				{
					@Override
					public void onLoadCleared(Drawable placeholder)
					{
						super.onLoadCleared(placeholder);
						setColors(getDefaultFooterColor(), holder);
					}

					@Override
					public void onColorReady(int color)
					{
						if (usePalette)
							setColors(color, holder);
						else
							setColors(getDefaultFooterColor(), holder);
					}
				});
	}

	@Override
	public int getItemCount()
	{
		return dataSet.size();
	}

	@Override
	public long getItemId(int position)
	{
		return dataSet.get(position).getId();
	}

	@Override
	protected Album getIdentifier(int position)
	{
		return dataSet.get(position);
	}

	@Override
	protected String getName(Album album)
	{
		return album.getTitle();
	}

	@Override
	protected void onMultipleItemAction(@NonNull MenuItem menuItem,
										@NonNull ArrayList<Album> selection)
	{
		SongsMenuHelper.handleMenuClick(activity, getSongList(selection), menuItem.getItemId());
	}

	@NonNull
	private ArrayList<Song> getSongList(@NonNull List<Album> albums)
	{
		final ArrayList<Song> songs = new ArrayList<>();
		for (Album album : albums) {
			songs.addAll(album.songs);
		}
		return songs;
	}

	@NonNull
	@Override
	public String getSectionName(int position)
	{
		return CalligraphyUtils.applyTypefaceSpan(
				MusicUtil.getSectionName(dataSet.get(position).getTitle()), mTypeface).toString();
	}

	public class ViewHolder extends MediaEntryViewHolder
	{

		public ViewHolder(@NonNull final View itemView)
		{
			super(itemView);
			setImageTransitionName(activity.getString(R.string.transition_album_art));
			if (menu != null) {
				menu.setVisibility(View.GONE);
			}
		}

		@Override
		public void onClick(View v)
		{
			if (isInQuickSelectMode()) {
				toggleChecked(getAdapterPosition());
			} else {
				Pair[] albumPairs = new Pair[]{Pair.create(image,
						activity.getResources().getString(R.string.transition_album_art))};
				NavigationUtil.goToAlbum(activity, dataSet.get(getAdapterPosition()).getId());
			}
		}

		@Override
		public boolean onLongClick(View view)
		{
			toggleChecked(getAdapterPosition());
			return true;
		}
	}
}
