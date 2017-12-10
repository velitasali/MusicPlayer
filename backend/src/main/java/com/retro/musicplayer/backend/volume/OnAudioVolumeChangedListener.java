package com.retro.musicplayer.backend.volume;

public interface OnAudioVolumeChangedListener {

    void onAudioVolumeChanged(int currentVolume, int maxVolume);
}