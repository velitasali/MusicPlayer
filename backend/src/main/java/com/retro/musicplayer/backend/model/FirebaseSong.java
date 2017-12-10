package com.retro.musicplayer.backend.model;

/**
 * @author Hemanth S (h4h13).
 */

public class FirebaseSong {
    public String title;
    public String albumName;
    public String artistName;
    public int playedCount;

    public FirebaseSong(String title, String albumName, String artistName, int playedCount) {
        this.title = title;
        this.albumName = albumName;
        this.artistName = artistName;
        this.playedCount = playedCount;
    }

    public FirebaseSong() {

    }

    @Override
    public String toString() {
        return "FirebaseSong{" +
                "title='" + title + '\'' +
                ", albumName='" + albumName + '\'' +
                ", artistName='" + artistName + '\'' +
                ", playedCount=" + playedCount +
                '}';
    }
}
