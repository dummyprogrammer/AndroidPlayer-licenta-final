package com.player.licenta.androidplayer.spotify;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.util.Log;

import com.player.licenta.androidplayer.model.Song;
import com.player.licenta.androidplayer.database.MoodSortedHelper;
import com.player.licenta.androidplayer.database.MoodSorterContract;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.NameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by razvan on 8/27/18.
 */

public class SpotifyAuthentication extends AsyncTask<ArrayList<Song>, Void, Void> {

    private String mToken;
    private ArrayList<Song> songList;
    private Context mContext;

    private final static String TAG = "SpotifyAuthentication";

    public SpotifyAuthentication(Context context){
        mContext = context;
    }

    @Override
    protected Void doInBackground(ArrayList<Song>... receivedSongList) {
        songList = receivedSongList[0];
        mToken = getAuthenticationToken();

        return null;
    }

    public String getAuthenticationToken() {

        try {
            URI uri = new URI(SpotifyConstants.AUTH_TOKEN_URL);
            HttpClient httpClient = new DefaultHttpClient();
            HttpPost httpPost = new HttpPost();
            httpPost.setURI(uri);
            httpPost.setHeader(SpotifyConstants.AUTHORIZATION, SpotifyConstants.AUTH_HEADER);

            List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair(SpotifyConstants.GRANT_TYPE, SpotifyConstants.CLIENT_CREDENTIALS));
            httpPost.setEntity(new UrlEncodedFormEntity(params));

            HttpResponse httpResponse = httpClient.execute(httpPost);

            InputStream is = httpResponse.getEntity().getContent();
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            String line = "";
            StringBuffer sb = new StringBuffer();
            while ((line = br.readLine()) != null) {
                sb.append(line);
                sb.append(" ");
            }

            String response = sb.toString();
            if (response.contains("access_token")) {
                JSONObject jsonObject = new JSONObject(response);
                mToken = SpotifyConstants.BEARER +
                        jsonObject.getString("access_token");
                getTrackData(mToken);
            }
            Log.d(TAG, "Response from Spotify" + mToken);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return mToken;
    }

    private void getTrackData(String mToken) {
        for (Song song: songList){
            String songTitle = song.getTitle();
            String songArtist = song.getArtist();
            if(!isSongStored(songTitle, songArtist)){
                SpotifySearch spotifySearch = new SpotifySearch(mContext);
                spotifySearch.execute(mToken, songTitle, songArtist);
            }
        }
    }


    public boolean isSongStored(String songTitle, String songArtist){

        selectAllRecords();
        //mContext.deleteDatabase(MoodSortedHelper.DATABASE_NAME);

        MoodSortedHelper moodSortedHelper = new MoodSortedHelper(mContext);
        SQLiteDatabase db = moodSortedHelper.getReadableDatabase();

        String[] projection = {
                MoodSorterContract.MoodSorterEntry.COLUMN_NAME_SONGTITLE,
                MoodSorterContract.MoodSorterEntry.COLUMN_NAME_SONGARTIST
        };
        String selection = MoodSorterContract.MoodSorterEntry.COLUMN_NAME_SONGTITLE + " =?" +
                " AND " + MoodSorterContract.MoodSorterEntry.COLUMN_NAME_SONGARTIST + " =?" ;
        String[] selectionArgs = {songTitle, songArtist};

        Cursor cursor = db.query(
                MoodSorterContract.MoodSorterEntry.TABLE_NAME,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                null
            );
        if((cursor != null) && !(cursor.getCount() == 0)){
            return true;
        }
        cursor.close();
        db.close();
        return false;
    }

    public void selectAllRecords() {
        MoodSortedHelper moodSortedHelper = new MoodSortedHelper(mContext);
        SQLiteDatabase db = moodSortedHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM moodsorter", null);
        List<String> records = new ArrayList<>();
        if (cursor.moveToFirst()){
            records.add(cursor.getString(cursor.getColumnIndex(MoodSorterContract.MoodSorterEntry.COLUMN_NAME_SONGTITLE)));
            while(cursor.moveToNext()){
                records.add(cursor.getString(cursor.getColumnIndex(MoodSorterContract.MoodSorterEntry.COLUMN_NAME_SONGTITLE)));
            }
        }
        cursor.close();
        db.close();

        for (String songTitle: records){
            Log.d(TAG, "Song titles in the database: " + songTitle);
        }
    }

}
