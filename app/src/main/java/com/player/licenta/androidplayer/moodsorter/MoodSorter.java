package com.player.licenta.androidplayer.moodsorter;

import android.os.AsyncTask;

import com.player.licenta.androidplayer.Song;
import com.player.licenta.androidplayer.lyrics.LyricsWS;

import java.util.ArrayList;

/**
 * Created by razvan on 7/12/18.
 */

public class MoodSorter{

    private ArrayList<Song> songList = new ArrayList<>();

    public MoodSorter(ArrayList<Song> songList){
        this.songList = songList;
        init();
    }

    private void init() {
        startLyricsWsTask();
        startSpotifyWSMoodTask();
    }

    private void startLyricsWsTask() {

        for(Song song: songList){
            LyricsWS lyricsWS = new LyricsWS(song);
            lyricsWS.execute();
        }

    }

    private void startSpotifyWSMoodTask() {
    }

}
