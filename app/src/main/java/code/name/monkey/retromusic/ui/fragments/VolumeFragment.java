package code.name.monkey.retromusic.ui.fragments;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SeekBar;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import code.name.monkey.appthemehelper.ThemeStore;
import code.name.monkey.appthemehelper.util.ColorUtil;
import code.name.monkey.appthemehelper.util.MaterialValueHelper;
import code.name.monkey.appthemehelper.util.TintHelper;
import code.name.monkey.backend.volume.AudioVolumeObserver;
import code.name.monkey.backend.volume.OnAudioVolumeChangedListener;
import com.velitasali.music.R;

/**
 * Created by BlackFootSanji on 5/5/2017.
 */

public class VolumeFragment extends Fragment implements SeekBar.OnSeekBarChangeListener, OnAudioVolumeChangedListener {
    private static final String TAG = "VolumeFragment";
    @BindView(R.id.volume_seekbar)
    SeekBar volumeSeekbar;
    @BindView(R.id.volume_down)
    ImageView volumeDown;
    int color;
    @BindView(R.id.container)
    ViewGroup viewGroup;
    @BindView(R.id.volume_up)
    ImageView volumeUp;
    private Unbinder unbinder;
    private AudioVolumeObserver mAudioVolumeObserver;


    public static VolumeFragment newInstance() {
        return new VolumeFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_volume, container, false);
        unbinder = ButterKnife.bind(this, view);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setColor(ThemeStore.accentColor(getContext()));
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mAudioVolumeObserver == null) {
            mAudioVolumeObserver = new AudioVolumeObserver(getActivity());
        }
        mAudioVolumeObserver.register(AudioManager.STREAM_MUSIC, this);

        AudioManager audioManager = getAudioManager();
        if (audioManager != null) {
            volumeSeekbar.setMax(audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC));
            volumeSeekbar.setProgress(audioManager.getStreamVolume(AudioManager.STREAM_MUSIC));
        }
        volumeSeekbar.setOnSeekBarChangeListener(this);
    }

    @Override
    public void onAudioVolumeChanged(int currentVolume, int maxVolume) {
        if (volumeSeekbar == null) {
            return;
        }
        volumeSeekbar.setMax(maxVolume);
        volumeSeekbar.setProgress(currentVolume);
        volumeDown.setImageResource(currentVolume == 0 ? R.drawable.ic_volume_off_white_24dp : R.drawable.ic_volume_down_white_24dp);
    }

    private AudioManager getAudioManager() {
        return (AudioManager) getContext().getSystemService(Context.AUDIO_SERVICE);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
        if (mAudioVolumeObserver != null) {
            mAudioVolumeObserver.unregister();
        }
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
        AudioManager audioManager = getAudioManager();
        if (audioManager != null) {
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, i, 0);
        }
        volumeDown.setImageResource(i == 0 ? R.drawable.ic_volume_off_white_24dp : R.drawable.ic_volume_down_white_24dp);
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }

    @OnClick({R.id.volume_down, R.id.volume_up})
    public void onViewClicked(View view) {
        AudioManager audioManager = getAudioManager();
        switch (view.getId()) {
            case R.id.volume_down:
                if (audioManager != null) {
                    audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_LOWER, 0);
                }
                break;
            case R.id.volume_up:
                if (audioManager != null) {
                    audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_RAISE, 0);
                }
                break;
        }
    }

    public void setColor(int color) {
        this.color = color;
    }

    public void tintWhiteColor() {
        setProgressBarColor(Color.WHITE);
    }

    public void setProgressBarColor(int newColor) {
        ColorStateList colorState = ColorStateList.valueOf(newColor);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            volumeSeekbar.setProgressTintList(colorState);
            volumeSeekbar.setThumbTintList(colorState);
            volumeSeekbar.setSecondaryProgressTintList(ColorStateList.valueOf(ColorUtil.darkenColor(newColor)));
        } else {
            volumeSeekbar.getProgressDrawable().setColorFilter(color, PorterDuff.Mode.SRC_IN);
            //volumeSeekbar.getThumb().setColorFilter(color, PorterDuff.Mode.SRC_IN);
        }
        volumeDown.setColorFilter(newColor, PorterDuff.Mode.SRC_IN);
        volumeUp.setColorFilter(newColor, PorterDuff.Mode.SRC_IN);
    }

    public void dominantColor(int dark) {
        volumeSeekbar.setThumb(null);
        TintHelper.setTint(volumeSeekbar, dark, true);
    }

    public void setTintable(int color) {
        setProgressBarColor(color);
    }
}
