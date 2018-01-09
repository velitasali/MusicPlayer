package code.name.monkey.retromusic.ui.adapter.home;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.LinearSnapHelper;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SnapHelper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.retro.musicplayer.backend.loaders.SongLoader;
import com.retro.musicplayer.backend.model.Album;
import com.retro.musicplayer.backend.model.Artist;
import com.retro.musicplayer.backend.model.Playlist;
import com.retro.musicplayer.backend.model.Song;
import com.retro.musicplayer.backend.model.smartplaylist.HistoryPlaylist;
import com.retro.musicplayer.backend.model.smartplaylist.LastAddedPlaylist;
import com.retro.musicplayer.backend.model.smartplaylist.MyTopTracksPlaylist;

import java.util.ArrayList;

import butterknife.BindView;
import code.name.monkey.retromusic.R;
import code.name.monkey.retromusic.helper.MusicPlayerRemote;
import code.name.monkey.retromusic.ui.adapter.PlaylistAdapter;
import code.name.monkey.retromusic.ui.adapter.album.AlbumAdapter;
import code.name.monkey.retromusic.ui.adapter.artist.ArtistAdapter;
import code.name.monkey.retromusic.ui.adapter.base.MediaEntryViewHolder;
import code.name.monkey.retromusic.ui.adapter.song.SongAdapter;
import code.name.monkey.retromusic.util.NavigationUtil;

/**
 * Created by hemanths on 19/07/17.
 */

public class HomeAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int SUB_HEADER = 0;
    private static final int ABS_PLAYLITS = 1;
    private static final int DATA = 2;
    private ArrayList<Object> dataSet = new ArrayList<>();
    private AppCompatActivity activity;

    public HomeAdapter(@NonNull AppCompatActivity activity) {
        this.activity = activity;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        switch (i) {
            case ABS_PLAYLITS:
                return new ViewHolder(LayoutInflater.from(activity)
                        .inflate(R.layout.abs_playlists, viewGroup, false));
            default:
            case DATA:
                return new ViewHolder(LayoutInflater.from(activity)
                        .inflate(R.layout.recycler_view_sec, viewGroup, false));
            case SUB_HEADER:
                return new ViewHolder(LayoutInflater.from(activity)
                        .inflate(R.layout.sub_header, viewGroup, false));
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (dataSet.get(position) instanceof String) {
            return SUB_HEADER;
        } else if (dataSet.get(position) instanceof Integer) {
            return ABS_PLAYLITS;
        } else if (dataSet.get(position) instanceof ArrayList) {
            return DATA;
        }
        return super.getItemViewType(position);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int i) {
        ViewHolder viewholder = (ViewHolder) holder;
        switch (getItemViewType(i)) {
            case ABS_PLAYLITS:
                if (viewholder.history != null) {
                    viewholder.history.setOnClickListener(view ->
                            NavigationUtil.goToPlaylistNew(activity, new HistoryPlaylist(activity)));
                }
                if (viewholder.lastAdded != null) {
                    viewholder.lastAdded.setOnClickListener(view ->
                            NavigationUtil.goToPlaylistNew(activity, new LastAddedPlaylist(activity)));
                }
                if (viewholder.topTracks != null) {
                    viewholder.topTracks.setOnClickListener(view ->
                            NavigationUtil.goToPlaylistNew(activity, new MyTopTracksPlaylist(activity)));
                }
                if (viewholder.shuffle != null) {
                    viewholder.shuffle.setOnClickListener(view ->
                            MusicPlayerRemote.openAndShuffleQueue(SongLoader.getAllSongs(activity)
                                    .blockingFirst(), true));
                }
                break;
            case SUB_HEADER:
                String title = (String) dataSet.get(i);
                if (viewholder.title != null) {
                    viewholder.title.setText(title);
                }
                break;
            case DATA:
                parseAllSections(i, viewholder);
                break;
        }
    }

    private void parseAllSections(int i, ViewHolder viewholder) {
        if (viewholder.recyclerView != null) {
            SnapHelper snapHelper = new LinearSnapHelper();
            viewholder.recyclerView.setOnFlingListener(null);
            snapHelper.attachToRecyclerView(viewholder.recyclerView);

            ArrayList arrayList = (ArrayList) dataSet.get(i);
            if (arrayList.isEmpty()) {
                return;
            }
            Object something = arrayList.get(0);
            if (something instanceof Artist) {
                viewholder.recyclerView.setLayoutManager(new GridLayoutManager(activity, 1, GridLayoutManager.HORIZONTAL, false));
                viewholder.recyclerView.setItemAnimator(new DefaultItemAnimator());
                viewholder.recyclerView.setAdapter(new ArtistAdapter(activity, (ArrayList<Artist>) arrayList, R.layout.item_artist, false, null));
            } else if (something instanceof Album) {
                viewholder.recyclerView.setLayoutManager(new GridLayoutManager(activity, 1, GridLayoutManager.HORIZONTAL, false));
                viewholder.recyclerView.setItemAnimator(new DefaultItemAnimator());
                viewholder.recyclerView.setAdapter(new AlbumAdapter(activity, (ArrayList<Album>) arrayList, R.layout.pager_item, false, null));
            } else if (something instanceof Playlist) {
                viewholder.recyclerView.setLayoutManager(new LinearLayoutManager(activity));
                viewholder.recyclerView.setItemAnimator(new DefaultItemAnimator());
                viewholder.recyclerView.setAdapter(new PlaylistAdapter(activity, (ArrayList<Playlist>) arrayList, R.layout.item_list, null));

            } else if (something instanceof Song) {
                GridLayoutManager layoutManager = new GridLayoutManager(activity, 1, LinearLayoutManager.HORIZONTAL, false);
                viewholder.recyclerView.setLayoutManager(layoutManager);
                viewholder.recyclerView.setItemAnimator(new DefaultItemAnimator());
                viewholder.recyclerView.setAdapter(new SongAdapter(activity, (ArrayList<Song>) arrayList, R.layout.item_image, false, null));

            }
        }
    }


    @Override
    public int getItemCount() {
        return dataSet.size();
    }

    public void swapData(@NonNull ArrayList<Object> data) {
        dataSet = data;
        notifyDataSetChanged();
    }

    public ArrayList<Object> getDataset() {
        return dataSet;
    }

    public class ViewHolder extends MediaEntryViewHolder {
        @BindView(R.id.see_all)
        @Nullable
        TextView seeAll;

        @BindView(R.id.history)
        @Nullable
        View history;
        @BindView(R.id.last_added)
        @Nullable
        View lastAdded;
        @BindView(R.id.top_tracks)
        @Nullable
        View topTracks;
        @BindView(R.id.action_shuffle)
        @Nullable
        View shuffle;

        public ViewHolder(View itemView) {
            super(itemView);
        }
    }
}
