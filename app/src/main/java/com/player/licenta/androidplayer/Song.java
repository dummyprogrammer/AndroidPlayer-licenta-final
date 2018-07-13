package com.player.licenta.androidplayer;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;

public class Song implements Serializable
{
	private long id;
	private String title;
	private String artist;
	private String lyrics;
	private String genre;
	
	public Song(long songID, String songTitle, String songArtist, String songGenre)
	{
		id=songID;
		title=songTitle;
		artist=songArtist;
		genre=songGenre;
	}
	
	public long getID()
	{
		return id;
	}
	
	public String getTitle()
	{
		return title;
	}
	
	public String getArtist()
	{
		return artist;
	}

    public String getLyrics() {
        return lyrics;
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
