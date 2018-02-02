package code.name.monkey.backend.volume;

public interface OnAudioVolumeChangedListener {

    void onAudioVolumeChanged(int currentVolume, int maxVolume);
}