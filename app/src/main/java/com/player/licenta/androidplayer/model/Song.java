package com.player.licenta.androidplayer.model;

import android.graphics.Bitmap;
import android.net.Uri;

import java.io.Serializable;

public class Song implements Serializable {
    private long id;
    private String songTitle;
    private String songArtist;
    private String lyrics;
    private String genre;
    private Long albumArtId;
    private String path;

    public Song(long id, String songTitle, String songArtist, String songGenre, Long albumArtId, String path) {
        this.id = id;
        this.songTitle = songTitle;
        this.songArtist = songArtist;
        this.genre = songGenre;
        this.albumArtId = albumArtId;
        this.path = path;
    }

    public long getID() {
        return id;
    }

    public String getSongTitle() {
        return songTitle;
    }

    public String getSongArtist() {
        return songArtist;
    }

    public String getLyrics() {
        return lyrics;
    }


    public String getPath() {
        return path;
    }

    public Long getAlbumArtId() {
        return albumArtId;
    }

    public void setLyrics(String lyrics) {
        this.lyrics = lyrics;
    }

    public String getGenre() {
        return genre;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }
}
