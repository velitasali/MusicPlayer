package code.name.monkey.retromusic.ui.adapter.home;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.TooltipCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.kabouzeid.appthemehelper.ThemeStore;
import com.retro.musicplayer.backend.loaders.PlaylistSongsLoader;
import com.retro.musicplayer.backend.model.Playlist;

import java.util.ArrayList;
import java.util.Collections;

import butterknife.BindView;
import code.name.monkey.retromusic.R;
import code.name.monkey.retromusic.ui.adapter.base.MediaEntryViewHolder;
import code.name.monkey.retromusic.util.NavigationUtil;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by hemanths on 19/07/17.
 */

public class HomeAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private ArrayList<Playlist> dataSet = new ArrayList<>();
    private AppCompatActivity activity;
    private CompositeDisposable mDisposable;

    public HomeAdapter(@NonNull AppCompatActivity activity) {
        this.activity = activity;
        mDisposable = new CompositeDisposable();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        return new ViewHolder(LayoutInflater.from(this.activity)
                .inflate(R.layout.recycler_view_sec, viewGroup, false));
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int i) {
        ViewHolder viewholder = (ViewHolder) holder;
        Playlist playlist = dataSet.get(holder.getAdapterPosition());
        if (viewholder.title != null) {
            viewholder.title.setVisibility(View.VISIBLE);
            viewholder.title.setText(playlist.name);
        }

        int aColor = ThemeStore.accentColor(activity);
        viewholder.seeAll.setTextColor(aColor);
        viewholder.seeAll.setVisibility(View.VISIBLE);
        viewholder.seeAll.setOnClickListener(v -> NavigationUtil.goToPlaylistNew(activity, playlist));

        TooltipCompat.setTooltipText(viewholder.seeAll, activity.getString(R.string.tool_tip_see_all));

        if (viewholder.recyclerView != null) {
            HorizontalItemAdapter adapter = new HorizontalItemAdapter(activity);

            viewholder.recyclerView.setLayoutManager(new GridLayoutManager(activity, 1, GridLayoutManager.HORIZONTAL, false));
            viewholder.recyclerView.setItemAnimator(new DefaultItemAnimator());
            viewholder.recyclerView.setNestedScrollingEnabled(false);
            viewholder.recyclerView.setAdapter(adapter);

            mDisposable.add(PlaylistSongsLoader.getPlaylistSongList(activity, playlist)
                    .subscribeOn(Schedulers.computation())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(songs -> {
                        //Collections.reverse(songs);
                        if (songs.size() > 10) {
                            adapter.swapData(songs.subList(0, 10));
                        } else {
                            adapter.swapData(songs);
                        }

                    }));
        }
    }


    @Override
    public int getItemCount() {
        return dataSet.size();
    }

    public void swapData(@NonNull ArrayList<Playlist> data) {
        dataSet = data;
        notifyDataSetChanged();
    }

    public class ViewHolder extends MediaEntryViewHolder {
        @BindView(R.id.see_all)
        TextView seeAll;

        public ViewHolder(View itemView) {
            super(itemView);
        }
    }
}
