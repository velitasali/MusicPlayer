package code.name.monkey.retromusic.ui.fragments.intro;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import code.name.monkey.retromusic.R;
import code.name.monkey.retromusic.ui.activities.UserInfoActivity;
import code.name.monkey.retromusic.util.Compressor;
import code.name.monkey.retromusic.util.ImageUtil;
import code.name.monkey.retromusic.util.PreferenceUtil;
import de.hdodenhof.circleimageview.CircleImageView;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

import static android.app.Activity.RESULT_OK;
import static com.retro.musicplayer.backend.RetroConstants.USER_PROFILE;

/**
 * @author Hemanth S (h4h13).
 */

public class NameFragment extends Fragment {
    private static final int PICK_IMAGE_REQUEST = 9002;
    private static final int PROFILE_ICON_SIZE = 400;
    @BindView(R.id.name)
    EditText mName;
    @BindView(R.id.user_image)
    CircleImageView mUserImage;
    private Unbinder unbinder;

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = LayoutInflater.from(getActivity())
                .inflate(R.layout.fragment_name, container, false);
        unbinder = ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //noinspection ConstantConditions
        mName.setText(PreferenceUtil.getInstance(getActivity()).getUserName());
        if (!PreferenceUtil.getInstance(getActivity()).getProfileImage().isEmpty()) {
            loadImageFromStorage(PreferenceUtil.getInstance(getActivity()).getProfileImage());
        }
    }

    @OnClick({R.id.next})
    void next(View view) {
        switch (view.getId()) {
            case R.id.next:
                String name = mName.getText().toString().trim();
                if (TextUtils.isEmpty(name)) {
                    Toast.makeText(getActivity(), "Umm name is empty", Toast.LENGTH_SHORT).show();
                    return;
                }
                //noinspection ConstantConditions
                PreferenceUtil.getInstance(getActivity()).setUserName(name);
                getActivity().setResult(RESULT_OK);
                //((UserInfoActivity) getActivity()).setFragment(new ChooseThemeFragment(), true);
                getActivity().finish();
                break;
        }
    }

    @OnClick(R.id.image)
    public void onViewClicked() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri uri = data.getData();
            try {
                Bitmap bitmap = ImageUtil
                        .getResizedBitmap(MediaStore.Images.Media
                                .getBitmap(getActivity().getContentResolver(), uri), PROFILE_ICON_SIZE);
                String profileImagePath = saveToInternalStorage(bitmap);
                PreferenceUtil.getInstance(getActivity()).saveProfileImage(profileImagePath);
                loadImageFromStorage(profileImagePath);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void loadImageFromStorage(String path) {
        new Compressor(getActivity())
                .setMaxHeight(300)
                .setMaxWidth(300)
                .setQuality(75)
                .setCompressFormat(Bitmap.CompressFormat.WEBP)
                .compressToBitmapAsFlowable(new File(path, USER_PROFILE))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(bitmap -> {
                    mUserImage.setImageBitmap(bitmap);
                });
    }

    private String saveToInternalStorage(Bitmap bitmapImage) {
        ContextWrapper cw = new ContextWrapper(getActivity());
        // path to /data/data/yourapp/app_data/imageDir
        File directory = cw.getDir("imageDir", Context.MODE_PRIVATE);
        // Create imageDir
        File mypath = new File(directory, USER_PROFILE);

        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(mypath);
            // Use the compress method on the BitMap object to write image to the OutputStream
            bitmapImage.compress(Bitmap.CompressFormat.PNG, 100, fos);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return directory.getAbsolutePath();
    }
}
