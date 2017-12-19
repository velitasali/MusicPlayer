package com.retro.musicplayer.backend.loaders;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.BaseColumns;
import android.provider.MediaStore;
import android.provider.MediaStore.Audio.Genres;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.retro.musicplayer.backend.R;
import com.retro.musicplayer.backend.model.Genre;
import com.retro.musicplayer.backend.model.Song;
import com.retro.musicplayer.backend.util.PreferenceUtil;

import java.util.ArrayList;

import io.reactivex.Observable;

/**
 * Created by hemanths on 16/08/17.
 */

public class GenreLoader {
    /*@NonNull
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
    }*/

    @NonNull
    public static Observable<ArrayList<Genre>> getAllGenres(@NonNull final Context context) {
        return getGenresFromCursor(context, makeGenreCursor(context));
    }

    @NonNull
    public static Observable<ArrayList<Song>> getSongs(@NonNull final Context context, final int genreId) {
        // The genres table only stores songs that have a genre specified,
        // so we need to get songs without a genre a different way.
        if (genreId == -1) {
            return getSongsWithNoGenre(context);
        }

        return SongLoader.getSongs(makeGenreSongCursor(context, genreId));
    }

    @NonNull
    private static Genre getGenreFromCursor(@NonNull Context context, @NonNull final Cursor cursor) {
        final int id = cursor.getInt(0);
        final String name = cursor.getString(1);
        final int songCount = getSongs(context, id).blockingFirst().size();
        return new Genre(id, name, songCount);

    }

    @NonNull
    private static Observable<ArrayList<Song>> getSongsWithNoGenre(@NonNull final Context context) {
        String selection = BaseColumns._ID + " NOT IN " +
                "(SELECT " + Genres.Members.AUDIO_ID + " FROM audio_genres_map)";
        return SongLoader.getSongs(SongLoader.makeSongCursor(context, selection, null));
    }

    private static boolean hasSongsWithNoGenre(@NonNull final Context context) {
        final Cursor allSongsCursor = SongLoader.makeSongCursor(context, null, null);
        final Cursor allSongsWithGenreCursor = makeAllSongsWithGenreCursor(context);

        if (allSongsCursor == null || allSongsWithGenreCursor == null) {
            return false;
        }

        final boolean hasSongsWithNoGenre = allSongsCursor.getCount() > allSongsWithGenreCursor.getCount();
        allSongsCursor.close();
        allSongsWithGenreCursor.close();
        return hasSongsWithNoGenre;
    }

    @Nullable
    private static Cursor makeAllSongsWithGenreCursor(@NonNull final Context context) {
        try {
            return context.getContentResolver().query(
                    Uri.parse("content://media/external/audio/genres/all/members"),
                    new String[]{Genres.Members.AUDIO_ID}, null, null, null);
        } catch (SecurityException e) {
            return null;
        }
    }

    @Nullable
    private static Cursor makeGenreSongCursor(@NonNull final Context context, int genreId) {
        try {
            return context.getContentResolver().query(
                    Genres.Members.getContentUri("external", genreId),
                    SongLoader.BASE_PROJECTION, SongLoader.BASE_SELECTION, null, PreferenceUtil.getInstance(context).getSongSortOrder());
        } catch (SecurityException e) {
            return null;
        }
    }

    @NonNull
    private static Observable<ArrayList<Genre>> getGenresFromCursor(@NonNull final Context context, @Nullable final Cursor cursor) {
        return Observable.create(e -> {
            final ArrayList<Genre> genres = new ArrayList<>();

            if (hasSongsWithNoGenre(context)) {
                int songCount = getSongs(context, -1).blockingFirst().size();
                genres.add(new Genre(context.getResources().getString(R.string.unknown_genre), songCount));
            }

            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    do {
                        genres.add(getGenreFromCursor(context, cursor));
                    } while (cursor.moveToNext());
                }
                cursor.close();
            }
            e.onNext(genres);
            e.onComplete();
        });
    }


    @Nullable
    private static Cursor makeGenreCursor(@NonNull final Context context) {
        final String[] projection = new String[]{
                Genres._ID,
                Genres.NAME
        };
        // Genres that actually have songs
        final String selection = Genres._ID + " IN" +
                " (SELECT " + Genres.Members.GENRE_ID + " FROM audio_genres_map WHERE " + Genres.Members.AUDIO_ID + " IN" +
                " (SELECT " + Genres._ID + " FROM audio_meta WHERE " + SongLoader.BASE_SELECTION + "))";

        try {
            return context.getContentResolver().query(
                    Genres.EXTERNAL_CONTENT_URI,
                    projection, selection, null, PreferenceUtil.getInstance(context).getGenreSortOrder());
        } catch (SecurityException e) {
            return null;
        }
    }

}
