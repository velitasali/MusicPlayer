package code.name.monkey.retromusic.helper.menu;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.widget.Toast;

import code.name.monkey.backend.loaders.PlaylistSongsLoader;
import code.name.monkey.backend.model.AbsCustomPlaylist;
import code.name.monkey.backend.model.Playlist;
import code.name.monkey.backend.model.Song;

import java.util.ArrayList;

import com.velitasali.music.R;
import code.name.monkey.retromusic.dialogs.AddToPlaylistDialog;
import code.name.monkey.retromusic.dialogs.DeletePlaylistDialog;
import code.name.monkey.retromusic.dialogs.RenamePlaylistDialog;
import code.name.monkey.retromusic.helper.MusicPlayerRemote;
import code.name.monkey.retromusic.util.PlaylistsUtil;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

/**
 * @author Karim Abou Zeid (kabouzeid)
 */
public class PlaylistMenuHelper {

    public static boolean handleMenuClick(@NonNull AppCompatActivity activity,
                                          @NonNull final Playlist playlist,
                                          @NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_play:
                MusicPlayerRemote.openQueue(getPlaylistSongs(activity, playlist), 9, true);
                return true;
            case R.id.action_play_next:
                MusicPlayerRemote.playNext(getPlaylistSongs(activity, playlist));
                return true;
            case R.id.action_add_to_playlist:
                AddToPlaylistDialog.create(getPlaylistSongs(activity, playlist))
                        .show(activity.getSupportFragmentManager(), "ADD_PLAYLIST");
                return true;
            case R.id.action_add_to_current_playing:
                MusicPlayerRemote.enqueue(getPlaylistSongs(activity, playlist));
                return true;
            case R.id.action_rename_playlist:
                RenamePlaylistDialog.create(playlist.id).show(activity.getSupportFragmentManager(), "RENAME_PLAYLIST");
                return true;
            case R.id.action_delete_playlist:
                DeletePlaylistDialog.create(playlist).show(activity.getSupportFragmentManager(), "DELETE_PLAYLIST");
                return true;
            case R.id.action_save_playlist:
                final Toast toast = Toast.makeText(activity, R.string.saving_to_file, Toast.LENGTH_SHORT);
                PlaylistsUtil.savePlaylist(activity, playlist)
                        .doOnSubscribe(disposable -> toast.show())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeOn(Schedulers.computation())
                        .subscribe(file -> {
                            if (toast != null) {
                                toast.setText(String.format(activity.getString(R.string.saved_playlist_to), file));
                                toast.show();
                            }
                        });
                return true;
        }
        return false;
    }

    @NonNull
    private static ArrayList<Song> getPlaylistSongs(@NonNull Activity activity,
                                                    @NonNull Playlist playlist) {
        ArrayList<Song> songs = new ArrayList<>();
        if (playlist instanceof AbsCustomPlaylist) {
            songs = ((AbsCustomPlaylist) playlist).getSongs(activity).blockingFirst();
        } else {
            songs = PlaylistSongsLoader.getPlaylistSongList(activity, playlist).blockingFirst();
        }
        return songs;
    }
}
