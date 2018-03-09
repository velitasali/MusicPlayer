package code.name.monkey.retromusic.ui.adapter.artist;

import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
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

import code.name.monkey.backend.model.Artist;
import code.name.monkey.backend.model.Song;
import code.name.monkey.retromusic.glide.ArtistGlideRequest;
import code.name.monkey.retromusic.glide.RetroMusicColoredTarget;
import code.name.monkey.retromusic.helper.menu.SongsMenuHelper;
import code.name.monkey.retromusic.interfaces.CabHolder;
import code.name.monkey.retromusic.ui.adapter.base.AbsMultiSelectAdapter;
import code.name.monkey.retromusic.ui.adapter.base.MediaEntryViewHolder;
import code.name.monkey.retromusic.util.MusicUtil;
import code.name.monkey.retromusic.util.NavigationUtil;

/**
 * @author Karim Abou Zeid (kabouzeid)
 */
public class ArtistAdapter extends AbsMultiSelectAdapter<ArtistAdapter.ViewHolder, Artist> implements FastScrollRecyclerView.SectionedAdapter
{

	protected final FragmentActivity activity;
	protected ArrayList<Artist> dataSet;

	protected int itemLayoutRes;

	protected boolean usePalette = false;


	public ArtistAdapter(@NonNull FragmentActivity activity, ArrayList<Artist> dataSet, @LayoutRes int itemLayoutRes, boolean usePalette, @Nullable CabHolder cabHolder)
	{
		super(activity, cabHolder, R.menu.menu_media_selection);
		this.activity = activity;
		this.dataSet = dataSet;
		this.itemLayoutRes = itemLayoutRes;
		this.usePalette = usePalette;
		//setHasStableIds(true);
	}

	public void swapDataSet(ArrayList<Artist> dataSet)
	{
		this.dataSet = dataSet;
		notifyDataSetChanged();
	}

	public ArrayList<Artist> getDataSet()
	{
		return dataSet;
	}

	public void usePalette(boolean usePalette)
	{
		this.usePalette = usePalette;
		notifyDataSetChanged();
	}

	@Override
	public long getItemId(int position)
	{
		return dataSet.get(position).getId();
	}

	@Override
	public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
	{
		View view = LayoutInflater.from(activity).inflate(itemLayoutRes, parent, false);
		return createViewHolder(view);
	}

	protected ViewHolder createViewHolder(View view)
	{
		return new ViewHolder(view);
	}

	@Override
	public void onBindViewHolder(@NonNull final ViewHolder holder, int position)
	{
		final Artist artist = dataSet.get(position);

		boolean isChecked = isChecked(artist);
		holder.itemView.setActivated(isChecked);

		if (holder.title != null) {
			holder.title.setText(artist.getName());
		}
		if (holder.text != null) {
			holder.text.setVisibility(View.GONE);
		}
		if (holder.shortSeparator != null) {
			holder.shortSeparator.setVisibility(View.VISIBLE);
		}
		loadArtistImage(artist, holder);
	}

	private void loadArtistImage(Artist artist, final ViewHolder holder)
	{
		if (holder.image == null) return;
		ArtistGlideRequest.Builder.from(Glide.with(activity), artist)
				.generatePalette(activity).build()
				.into(new RetroMusicColoredTarget(holder.image)
				{
					@Override
					public void onColorReady(int color)
					{

					}
				});
	}

	@Override
	public int getItemCount()
	{
		return dataSet.size();
	}

	@Override
	protected Artist getIdentifier(int position)
	{
		return dataSet.get(position);
	}

	@Override
	protected String getName(Artist artist)
	{
		return artist.getName();
	}

	@Override
	protected void onMultipleItemAction(@NonNull MenuItem menuItem, @NonNull ArrayList<Artist> selection)
	{
		SongsMenuHelper.handleMenuClick(activity, getSongList(selection), menuItem.getItemId());
	}

	@NonNull
	private ArrayList<Song> getSongList(@NonNull List<Artist> artists)
	{
		final ArrayList<Song> songs = new ArrayList<>();
		for (Artist artist : artists) {
			songs.addAll(artist.getSongs()); // maybe async in future?
		}
		return songs;
	}

	@NonNull
	@Override
	public String getSectionName(int position)
	{
		return MusicUtil.getSectionName(dataSet.get(position).getName());
	}

	public class ViewHolder extends MediaEntryViewHolder
	{

		public ViewHolder(@NonNull View itemView)
		{
			super(itemView);
			setImageTransitionName(activity.getString(R.string.transition_artist_image));
			if (menu != null) {
				menu.setVisibility(View.INVISIBLE);
			}
		}

		@Override
		public void onClick(View v)
		{
			if (isInQuickSelectMode()) {
				toggleChecked(getAdapterPosition());
			} else {
				NavigationUtil.goToArtist(activity, dataSet.get(getAdapterPosition()).getId());
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
