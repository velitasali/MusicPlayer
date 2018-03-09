/*
 * Copyright (C) 2017. Alexander Bilchuk <a.bilchuk@sandrlab.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package code.name.monkey.retromusic.ui.adapter;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.support.annotation.NonNull;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import code.name.monkey.backend.loaders.PlaylistSongsLoader;
import code.name.monkey.backend.model.Playlist;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import com.velitasali.music.R;
import code.name.monkey.retromusic.util.NavigationUtil;
import code.name.monkey.retromusic.views.MetalRecyclerViewPager;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class AbsPlaylistAdapter extends MetalRecyclerViewPager.MetalAdapter<AbsPlaylistAdapter.FullMetalViewHolder> {

    private Activity mContext;
    private List<Playlist> mList = new ArrayList<>();

    public AbsPlaylistAdapter(@NonNull Activity context,
                              @NonNull DisplayMetrics metrics) {
        super(metrics);
        mContext = context;
    }

    public void swapData(ArrayList<Playlist> list) {
        mList = list;
        notifyDataSetChanged();
    }

    public void swapData(Playlist playlist) {
        mList.add(playlist);
        notifyDataSetChanged();
    }

    @Override
    public FullMetalViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View viewItem = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.pager_item, parent, false);
        return new FullMetalViewHolder(viewItem);
    }

    private Bitmap combineImageIntoOne(ArrayList<Bitmap> bitmap) {
        int w = 0, h = 0;
        for (int i = 0; i < bitmap.size(); i++) {
            if (i < bitmap.size() - 1) {
                h = bitmap.get(i).getWidth() > bitmap.get(i + 1).getWidth() ? bitmap.get(i).getWidth() : bitmap.get(i + 1).getWidth();
            }
            w += bitmap.get(i).getHeight();
        }

        Bitmap temp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(temp);
        int top = 0, left = 0;
        for (int i = 0; i < bitmap.size(); i++) {
            Log.d("HTML", "Combine: " + i + "/" + bitmap.size() + 1);

            top = (i == 0 ? 0 : top + bitmap.get(i).getHeight());
            left = (i == 0 ? 0 : top + bitmap.get(i).getWidth());
            canvas.drawBitmap(bitmap.get(i), left, 0f, null);
        }
        return temp;
    }

    @Override
    public void onBindViewHolder(FullMetalViewHolder holder, int position) {
        // don't forget about calling supper.onBindViewHolder!
        super.onBindViewHolder(holder, position);

        Playlist playlist = mList.get(position);
        if (holder.title != null) {
            holder.title.setText(playlist.name);
        }
        if (holder.image != null) {
            Glide.with(mContext)
                    .load(R.drawable.default_album_art)
                    .into(holder.image);
        }
        PlaylistSongsLoader.getPlaylistSongList(mContext, playlist)
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(songs -> {
                    if (songs.size() > 0) {
                        if (holder.text != null) {
                            holder.text.setText(String.format(Locale.getDefault(),
                                    "%d%s", songs.size(), songs.size() <= 1 ? " Song" : " Songs"));
                        }
                    }
                });
        holder.itemView.setOnClickListener(view -> NavigationUtil.goToPlaylistNew(mContext, playlist));

    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    static class FullMetalViewHolder extends MetalRecyclerViewPager.MetalViewHolder {

        public FullMetalViewHolder(View itemView) {
            super(itemView);

        }
    }
}