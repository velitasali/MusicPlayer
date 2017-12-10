package com.retro.musicplayer.backend.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * @author Hemanth S (h4h13).
 */

public class Genre implements Parcelable {

    public static final Creator<Genre> CREATOR = new Creator<Genre>() {
        @Override
        public Genre createFromParcel(Parcel in) {
            return new Genre(in);
        }

        @Override
        public Genre[] newArray(int size) {
            return new Genre[size];
        }
    };
    public String name;
    public int id;
    public int songCount;

    public Genre(String name, int id, int songCount) {
        this.name = name;
        this.id = id;
        this.songCount = songCount;
    }

    protected Genre(Parcel in) {
        name = in.readString();
        id = in.readInt();
        songCount = in.readInt();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeInt(id);
        dest.writeInt(songCount);
    }

    @Override
    public int describeContents() {
        return 0;
    }
}
