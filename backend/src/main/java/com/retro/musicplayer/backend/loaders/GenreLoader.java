package com.retro.musicplayer.backend.loaders;

import android.content.Context;
import android.database.Cursor;
import android.provider.BaseColumns;
import android.provider.MediaStore;
import android.provider.MediaStore.Audio.GenresColumns;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.retro.musicplayer.backend.model.Genre;

import java.util.ArrayList;

import io.reactivex.Observable;

/**
 * Created by hemanths on 16/08/17.
 */

public class GenreLoader {
    @NonNull
    public static Observable<ArrayList<Genre>> getAllGenres(@NonNull Context context) {
        Cursor cursor = makeCursor(context, null, null);
        return getGenres(context, cursor);
    }

    @NonNull
    private static Observable<ArrayList<Genre>> getGenres(@NonNull Context context, @NonNull Cursor cursor) {
        return Observable.create(e -> {
            ArrayList<Genre> genres = new ArrayList<>();
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    genres.add(getGenreFromCursorImpl(context, cursor));
                } while (cursor.moveToNext());
            }

            if (cursor != null)
                cursor.close();
            e.onNext(genres);
            e.onComplete();
        });
    }

    private static Genre getGenreFromCursorImpl(@NonNull Context context, @NonNull Cursor cursor) {
        final int id = cursor.getInt(0);
        final String name = cursor.getString(1);

        Cursor cursor1 = context.getContentResolver().query(MediaStore.Audio.Genres.Members.getContentUri("external", id),
                new String[]{MediaStore.Audio.Media.DISPLAY_NAME},
                null, null, null);

        if (cursor1 != null && cursor1.getCount() > 0) {
            return new Genre(name, id, cursor1.getCount());
        } else {
            return new Genre(name, id, 0);
        }
    }

    @Nullable
    private static Cursor makeCursor(@NonNull final Context context, @Nullable final String selection, final String[] selectionValues) {
        return makeSongCursor(context, selection, selectionValues, MediaStore.Audio.Genres.NAME + " ASC");
    }

    @Nullable
    private static Cursor makeSongCursor(@NonNull Context context,
                                         @NonNull String selection,
                                         @NonNull String[] selectionValues,
                                         @NonNull String songSortOrder) {
        try {
            return context.getContentResolver().query(
                    MediaStore.Audio.Genres.EXTERNAL_CONTENT_URI,
                    new String[]{
                            BaseColumns._ID,
                            GenresColumns.NAME
                    },
                    selection,
                    selectionValues,
                    songSortOrder
            );
        } catch (SecurityException e) {
            return null;
        }
    }

}
