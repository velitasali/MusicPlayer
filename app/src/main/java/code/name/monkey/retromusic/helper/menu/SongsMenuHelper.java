package code.name.monkey.retromusic.helper.menu;

import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;

import com.velitasali.music.R;

import java.util.ArrayList;

import code.name.monkey.backend.model.Song;
import code.name.monkey.retromusic.dialogs.AddToPlaylistDialog;
import code.name.monkey.retromusic.dialogs.DeleteSongsDialog;
import code.name.monkey.retromusic.helper.MusicPlayerRemote;


/**
 * @author Karim Abou Zeid (kabouzeid)
 */
public class SongsMenuHelper
{
	public static boolean handleMenuClick(@NonNull FragmentActivity activity, @NonNull ArrayList<Song> songs, int menuItemId)
	{
		switch (menuItemId) {
			case R.id.action_play_next:
				MusicPlayerRemote.playNext(songs);
				return true;
			case R.id.action_add_to_current_playing:
				MusicPlayerRemote.enqueue(songs);
				return true;
			case R.id.action_add_to_playlist:
				AddToPlaylistDialog.create(songs).show(activity.getSupportFragmentManager(), "ADD_PLAYLIST");
				return true;
			case R.id.action_delete_from_device:
				DeleteSongsDialog.create(songs).show(activity.getSupportFragmentManager(), "DELETE_SONGS");
				return true;
		}
		return false;
	}
}
