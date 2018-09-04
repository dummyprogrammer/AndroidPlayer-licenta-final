package com.player.licenta.androidplayer.database;

/**
 * Created by razvan on 9/3/18.
 */

public class MoodSorterConstants {

    protected static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + MoodSorterContract.MoodSorterEntry.TABLE_NAME + " (" +
                    MoodSorterContract.MoodSorterEntry.COLUMN_NAME_DANCEABILITY + " NUMERIC," +
                    MoodSorterContract.MoodSorterEntry.COLUMN_NAME_ENERGY + " NUMERIC," +
                    MoodSorterContract.MoodSorterEntry.COLUMN_NAME_LOUDNESS + " NUMERIC," +
                    MoodSorterContract.MoodSorterEntry.COLUMN_NAME_SPEECHINESS + " NUMERIC," +
                    MoodSorterContract.MoodSorterEntry.COLUMN_NAME_ACOUSTICNESS + " NUMERIC," +
                    MoodSorterContract.MoodSorterEntry.COLUMN_NAME_INSTRUMENTALNESS + " NUMERIC," +
                    MoodSorterContract.MoodSorterEntry.COLUMN_NAME_LIVENESS + " NUMERIC," +
                    MoodSorterContract.MoodSorterEntry.COLUMN_NAME_VALENCE + " NUMERIC," +
                    MoodSorterContract.MoodSorterEntry.COLUMN_NAME_TEMPO + " NUMERIC," +
                    MoodSorterContract.MoodSorterEntry.COLUMN_NAME_URI + " NUMERIC," +
                    MoodSorterContract.MoodSorterEntry.COLUMN_NAME_SONGTITLE + " NUMERIC," +
                    MoodSorterContract.MoodSorterEntry.COLUMN_NAME_SONGARTIST + " NUMERIC," +
                    " PRIMARY KEY(" + MoodSorterContract.MoodSorterEntry.COLUMN_NAME_SONGTITLE +
                    ", " + MoodSorterContract.MoodSorterEntry.COLUMN_NAME_SONGARTIST + "))";

    protected static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + MoodSorterContract.MoodSorterEntry.TABLE_NAME;
}
