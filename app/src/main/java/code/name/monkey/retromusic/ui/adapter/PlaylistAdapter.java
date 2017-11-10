package code.name.monkey.retromusic.ui.adapter;

import android.graphics.Bitmap;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.PopupMenu;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.retro.musicplayer.backend.loaders.PlaylistSongsLoader;
import com.retro.musicplayer.backend.model.AbsCustomPlaylist;
import com.retro.musicplayer.backend.model.Playlist;
import com.retro.musicplayer.backend.model.Song;
import com.retro.musicplayer.backend.model.smartplaylist.AbsSmartPlaylist;
import com.retro.musicplayer.backend.model.smartplaylist.LastAddedPlaylist;
import com.stfalcon.multiimageview.MultiImageView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutionException;

import butterknife.BindView;
import code.name.monkey.retromusic.R;
import code.name.monkey.retromusic.dialogs.ClearSmartPlaylistDialog;
import code.name.monkey.retromusic.dialogs.DeletePlaylistDialog;
import code.name.monkey.retromusic.helper.MusicPlayerRemote;
import code.name.monkey.retromusic.helper.menu.PlaylistMenuHelper;
import code.name.monkey.retromusic.helper.menu.SongsMenuHelper;
import code.name.monkey.retromusic.interfaces.CabHolder;
import code.name.monkey.retromusic.ui.adapter.base.AbsMultiSelectAdapter;
import code.name.monkey.retromusic.ui.adapter.base.MediaEntryViewHolder;
import code.name.monkey.retromusic.util.NavigationUtil;
import code.name.monkey.retromusic.util.Util;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by BlackFootSanji on 9/19/2016.
 */
public class PlaylistAdapter extends AbsMultiSelectAdapter<PlaylistAdapter.ViewHolder, Playlist> {
    public static final String TAG = PlaylistAdapter.class.getSimpleName();

    private static final int SMART_PLAYLIST = 0;
    private static final int DEFAULT_PLAYLIST = 1;

    protected final AppCompatActivity activity;
    protected ArrayList<Playlist> dataSet;
    protected int itemLayoutRes;
    private ArrayList<Song> mSongs = new ArrayList<>();


    public PlaylistAdapter(AppCompatActivity activity, ArrayList<Playlist> dataSet, @LayoutRes int itemLayoutRes, @Nullable CabHolder cabHolder) {
        super(activity, cabHolder, R.menu.menu_playlists_selection);
        this.activity = activity;
        this.dataSet = dataSet;
        this.itemLayoutRes = itemLayoutRes;
        setHasStableIds(true);
    }

    public ArrayList<Playlist> getDataSet() {
        return dataSet;
    }

    public void swapDataSet(ArrayList<Playlist> dataSet) {
        this.dataSet = dataSet;
        notifyDataSetChanged();
    }

    @Override
    public long getItemId(int position) {
        return dataSet.get(position).id;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(activity).inflate(itemLayoutRes, parent, false);
        return createViewHolder(view, viewType);
    }

    protected ViewHolder createViewHolder(View view, int viewType) {
        return new ViewHolder(view, viewType);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        final Playlist playlist = dataSet.get(position);
        ArrayList<Song> songs = getSongs(playlist);
        holder.itemView.setActivated(isChecked(playlist));

        if (holder.title != null) {
            holder.title.setText(playlist.name);
        }
        if (holder.text != null) {
            holder.text.setText(String.format(Locale.getDefault(), "%d Songs", songs.size()));
        }
        if (holder.getAdapterPosition() == getItemCount() - 1) {
            if (holder.shortSeparator != null) {
                holder.shortSeparator.setVisibility(View.GONE);
            }
        } else {
            if (holder.shortSeparator != null && !(dataSet.get(position) instanceof AbsSmartPlaylist)) {
                holder.shortSeparator.setVisibility(View.VISIBLE);
            }
        }

        holder.songs.setOnClickListener(v -> MusicPlayerRemote.openQueue(getSongs(playlist), 0, true));

        if (songs != null) {
            loadBitmaps(songs)
                    .subscribeOn(Schedulers.computation())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(bitmaps -> {
                        if (bitmaps.size() > 0) {
                            for (Bitmap bitmap : bitmaps) {
                                holder.multipleImage.addImage(bitmap);
                            }
                        }
                    });
        }
    }

    @Override
    public int getItemViewType(int position) {
        return dataSet.get(position) instanceof AbsSmartPlaylist ? SMART_PLAYLIST : DEFAULT_PLAYLIST;
    }

    @Override
    public int getItemCount() {
        return dataSet.size();
    }

    @Override
    protected Playlist getIdentifier(int position) {
        return dataSet.get(position);
    }

    @Override
    protected String getName(Playlist playlist) {
        return playlist.name;
    }

    @Override
    protected void onMultipleItemAction(@NonNull MenuItem menuItem, @NonNull ArrayList<Playlist> selection) {
        switch (menuItem.getItemId()) {
            case R.id.action_delete_playlist:
                for (int i = 0; i < selection.size(); i++) {
                    Playlist playlist = selection.get(i);
                    if (playlist instanceof AbsSmartPlaylist) {
                        AbsSmartPlaylist absSmartPlaylist = (AbsSmartPlaylist) playlist;
                        ClearSmartPlaylistDialog.create(absSmartPlaylist).show(activity.getSupportFragmentManager(), "CLEAR_PLAYLIST_" + absSmartPlaylist.name);
                        selection.remove(playlist);
                        i--;
                    }
                }
                if (selection.size() > 0) {
                    DeletePlaylistDialog.create(selection).show(activity.getSupportFragmentManager(), "DELETE_PLAYLIST");
                }
                break;
            default:
                SongsMenuHelper.handleMenuClick(activity, getSongList(selection), menuItem.getItemId());
                break;
        }
    }

    public ArrayList<Song> getSongs() {
        return mSongs;
    }

    public void setSongs(ArrayList<Song> songs) {
        mSongs = songs;
    }

    @NonNull
    private ArrayList<Song> getSongList(@NonNull List<Playlist> playlists) {
        final ArrayList<Song> songs = new ArrayList<>();
        for (Playlist playlist : playlists) {
            if (playlist instanceof AbsCustomPlaylist) {
                songs.addAll(((AbsCustomPlaylist) playlist).getSongs(activity).blockingFirst());
                //((AbsCustomPlaylist) playlist).getSongs(activity).subscribe(this::setSongs);

            } else {
                songs.addAll(PlaylistSongsLoader.getPlaylistSongList(activity, playlist.id).blockingFirst());
            }
        }
        return songs;
    }

    @Nullable
    private ArrayList<Song> getSongs(@NonNull Playlist playlist) {
        final ArrayList<Song> songs = new ArrayList<>();
        if (playlist instanceof AbsSmartPlaylist) {
            songs.addAll(((AbsSmartPlaylist) playlist).getSongs(activity).blockingFirst());
        } else {
            songs.addAll(PlaylistSongsLoader.getPlaylistSongList(activity, playlist.id).blockingFirst());
        }
        return songs;
    }

    private Observable<ArrayList<Bitmap>> loadBitmaps(@NonNull ArrayList<Song> songs) {
        return Observable.create(e -> {
            ArrayList<Bitmap> bitmaps = new ArrayList<Bitmap>();
            for (Song song : songs) {
                try {
                    Bitmap bitmap = Glide.with(activity)
                            .load(Util.getAlbumArtUri(song.albumId))
                            .asBitmap()
                            .into(500, 500)
                            .get();
                    if (bitmap != null) {
                        Log.i(TAG, "loadBitmaps: has");
                        bitmaps.add(bitmap);
                    }
                    if (bitmaps.size() == 4) {
                        break;
                    }
                } catch (InterruptedException | ExecutionException ex) {
                    ex.printStackTrace();
                }
            }
            e.onNext(bitmaps);
            e.onComplete();
        });
    }

    public class ViewHolder extends MediaEntryViewHolder {
        @BindView(R.id.gradient_background)
        View gradientBackground;
        @BindView(R.id.multiple_images)
        MultiImageView multipleImage;
        @BindView(R.id.songs)
        View songs;

        public ViewHolder(@NonNull View itemView, int itemViewType) {
            super(itemView);

            if (menu != null) {
                menu.setOnClickListener(view -> {
                    final Playlist playlist = dataSet.get(getAdapterPosition());
                    final PopupMenu popupMenu = new PopupMenu(activity, view);
                    popupMenu.inflate(getItemViewType() == SMART_PLAYLIST ? R.menu.menu_item_smart_playlist : R.menu.menu_item_playlist);
                    if (playlist instanceof LastAddedPlaylist) {
                        popupMenu.getMenu().findItem(R.id.action_clear_playlist).setVisible(false);
                    }
                    popupMenu.setOnMenuItemClickListener(item -> {
                        if (item.getItemId() == R.id.action_clear_playlist) {
                            if (playlist instanceof AbsSmartPlaylist) {
                                ClearSmartPlaylistDialog.create((AbsSmartPlaylist) playlist).show(activity.getSupportFragmentManager(), "CLEAR_SMART_PLAYLIST_" + playlist.name);
                                return true;
                            }
                        }
                        return PlaylistMenuHelper.handleMenuClick(
                                activity, dataSet.get(getAdapterPosition()), item);
                    });
                    popupMenu.show();
                });
            }
        }

        @Override
        public void onClick(View view) {
            if (isInQuickSelectMode()) {
                toggleChecked(getAdapterPosition());
            } else {
                Playlist playlist = dataSet.get(getAdapterPosition());
                NavigationUtil.goToPlaylistNew(activity, playlist);
            }
        }

        @Override
        public boolean onLongClick(View view) {
            toggleChecked(getAdapterPosition());
            return true;
        }
    }
}
