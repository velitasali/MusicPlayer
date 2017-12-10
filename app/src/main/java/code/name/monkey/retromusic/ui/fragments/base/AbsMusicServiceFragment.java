package code.name.monkey.retromusic.ui.fragments.base;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.View;

import com.retro.musicplayer.backend.interfaces.MusicServiceEventListener;
import com.retro.musicplayer.backend.model.FirebaseSong;
import com.retro.musicplayer.backend.model.Song;

import code.name.monkey.retromusic.helper.MusicPlayerRemote;
import code.name.monkey.retromusic.ui.activities.base.AbsMusicServiceActivity;
import code.name.monkey.retromusic.util.Util;

/**
 * Created by hemanths on 18/08/17.
 */

public class AbsMusicServiceFragment extends Fragment implements MusicServiceEventListener {

    private AbsMusicServiceActivity activity;


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            activity = (AbsMusicServiceActivity) context;
        } catch (ClassCastException e) {
            throw new RuntimeException(context.getClass().getSimpleName() + " must be an instance of " + AbsMusicServiceActivity.class.getSimpleName());
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        activity = null;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        activity.addMusicServiceEventListener(this);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        activity.removeMusicServiceEventListener(this);
    }

    @Override
    public void onPlayingMetaChanged() {
        Song song = MusicPlayerRemote.getCurrentSong();

        FirebaseSong firebaseSong = new FirebaseSong(
                Util.EncodeString(song.title),
                Util.EncodeString(song.albumName),
                Util.EncodeString(song.artistName), 1);
        String key = "retro_music_" + firebaseSong.title;


    }

    @Override
    public void onServiceConnected() {

    }

    @Override
    public void onServiceDisconnected() {

    }

    @Override
    public void onQueueChanged() {

    }

    @Override
    public void onPlayStateChanged() {

    }

    @Override
    public void onRepeatModeChanged() {

    }

    @Override
    public void onShuffleModeChanged() {

    }

    @Override
    public void onMediaStoreChanged() {

    }
}
