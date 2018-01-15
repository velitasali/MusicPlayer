package code.name.monkey.retromusic.ui.fragments.player.card;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import code.name.monkey.retromusic.R;
import code.name.monkey.retromusic.ui.fragments.base.AbsPlayerFragment;
import code.name.monkey.retromusic.util.PreferenceUtil;

/**
 * @author Hemanth S (h4h13).
 */

public class CardPlayerFragment extends AbsPlayerFragment {
    @BindView(R.id.player_container)
    ViewGroup mViewGroup;
    private Unbinder unbinder;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.fragment_card_player, container, false);
        unbinder = ButterKnife.bind(this, layout);
        /*Adding margin to toolbar for !full screen mode*/
        if (PreferenceUtil.getInstance(getContext()).getFullScreenMode() < 1) {
            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) mViewGroup.getLayoutParams();
            params.topMargin = getResources().getDimensionPixelOffset(R.dimen.status_bar_padding);
            //mViewGroup.setLayoutParams(params);
        }
        return layout;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    @Override
    public int getPaletteColor() {
        return 0;
    }

    @Override
    public void onShow() {

    }

    @Override
    public void onHide() {

    }

    @Override
    public boolean onBackPressed() {
        return false;
    }

    @Override
    public Toolbar getToolbar() {
        return null;
    }

    @Override
    public int toolbarIconColor() {
        return 0;
    }
}
