package com.player.licenta.androidplayer.database;

import android.provider.BaseColumns;

/**
 * Created by razvan on 9/3/18.
 */

public class MoodSorterContract {

    private MoodSorterContract(){
    }

    public static class MoodSorterEntry implements BaseColumns{

        public static final String TABLE_NAME = "moodsorter";
        public static final String COLUMN_NAME_DANCEABILITY = "danceability";
        public static final String COLUMN_NAME_ENERGY = "energy";
        public static final String COLUMN_NAME_LOUDNESS = "loudness";
        public static final String COLUMN_NAME_SPEECHINESS = "speechiness";
        public static final String COLUMN_NAME_ACOUSTICNESS = "acousticness";
        public static final String COLUMN_NAME_INSTRUMENTALNESS = "instrumentalness";
        public static final String COLUMN_NAME_LIVENESS = "liveness";
        public static final String COLUMN_NAME_VALENCE= "valence";
        public static final String COLUMN_NAME_TEMPO = "tempo";
        public static final String COLUMN_NAME_URI = "uri";
        public static final String COLUMN_NAME_SONGTITLE = "songtitle";
        public static final String COLUMN_NAME_SONGARTIST = "songartist";

    }
}
