package com.player.licenta.androidplayer.util;

import com.player.licenta.androidplayer.activities.MainActivity;
import com.player.licenta.androidplayer.model.Song;

import java.util.ArrayList;

/**
 * Created by razvan on 7/13/18.
 */

public class Utils {

    private static ArrayList<String> m_genres = new ArrayList<>();
    private static ArrayList<Song> m_groupedSongs = new ArrayList<>();

    private final static String ALL_MUSIC = "All Music";

    public static ArrayList<String> getGenres(ArrayList<Song> songs){

        String genre = "";
        for(Song song:songs){
            genre = song.getGenre();
            if(!m_genres.contains(genre) && !genre.isEmpty()){
                m_genres.add(genre);
            }
        }
        return m_genres;
    }

    public static ArrayList<Song> getGroupedSongs(ArrayList<Song> songs, String currentGenre) {

        m_groupedSongs.clear();
        for(Song song:songs){
            if(song.getGenre().equals(currentGenre)){
                m_groupedSongs.add(song);
            }
        }
        return m_groupedSongs;
    }

    public static ArrayList<String> getGroupedPlaylists(ArrayList<String> genres) {
        for(String genre:genres){
                if(!genre.equals(ALL_MUSIC)){
                    m_genres.add(genre);
                }
        }
        return m_genres;
    }
}
