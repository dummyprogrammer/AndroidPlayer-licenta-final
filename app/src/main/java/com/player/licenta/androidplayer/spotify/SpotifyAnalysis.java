package com.player.licenta.androidplayer.spotify;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.player.licenta.androidplayer.model.Song;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;

import static com.player.licenta.androidplayer.spotify.SpotifySearch.counterValidatedTrackId;

/**
 * Created by razvan on 8/29/18.
 */

public class SpotifyAnalysis extends AsyncTask<String, Void, Void> {

    private String mTrackId;
    private String mToken;
    private final static String TAG = "SpotifyAnalysis";
    private Context mContext;
    private String mSongTitle;
    private String mSongArtist;
    private String mChosenFeature;
    private Song mSong;


    public static int analysisFeatureCount = 0;

    public SpotifyAnalysis(Context context, Song song, String token, String trackId) {
        mContext = context;
        mSong = song;
        mToken = token;
        mTrackId = trackId;
    }

    @Override
    protected Void doInBackground(String... strings) {
        mTrackId = strings[0];
        mToken = strings[1];
        mSongTitle = strings[2];
        mSongArtist = strings[3];
        mChosenFeature = strings[4];
        getAudioFeatures();
        return null;
    }

    public String getAudioFeatures() {

        try {
            URI uri = getSearchURI();

            HttpClient httpClient = new DefaultHttpClient();
            HttpGet httpGet = new HttpGet();
            httpGet.setURI(uri);
            httpGet.setHeader(SpotifyConstants.AUTHORIZATION, mToken);

            HttpResponse httpResponse = httpClient.execute(httpGet);
            InputStream is = httpResponse.getEntity().getContent();
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            String line = "";
            StringBuffer sb = new StringBuffer();
            while ((line = br.readLine()) != null) {
                sb.append(line);
                sb.append(" ");
            }
            String response = sb.toString();
            Log.d(TAG, response);

            return response;

        } catch (URISyntaxException ex) {
            ex.printStackTrace();
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /*public void insertDataToDB(String response) throws JSONException {

        MoodSortedHelper moodSortedHelper = new MoodSortedHelper(mContext);
        SQLiteDatabase db = moodSortedHelper.getWritableDatabase();

        MoodSorterEntity moodSorterEntity = getMoodSorterEntity(response);

        ContentValues values = new ContentValues();
        values.put(MoodSorterContract.MoodSorterEntry.COLUMN_NAME_DANCEABILITY, moodSorterEntity.getDanceability());
        values.put(MoodSorterContract.MoodSorterEntry.COLUMN_NAME_ENERGY, moodSorterEntity.getDanceability());
        values.put(MoodSorterContract.MoodSorterEntry.COLUMN_NAME_LOUDNESS, moodSorterEntity.getLoudness());
        values.put(MoodSorterContract.MoodSorterEntry.COLUMN_NAME_SPEECHINESS, moodSorterEntity.getSpeechiness());
        values.put(MoodSorterContract.MoodSorterEntry.COLUMN_NAME_ACOUSTICNESS, moodSorterEntity.getAcousticness());
        values.put(MoodSorterContract.MoodSorterEntry.COLUMN_NAME_INSTRUMENTALNESS, moodSorterEntity.getInstrumentalness());
        values.put(MoodSorterContract.MoodSorterEntry.COLUMN_NAME_VALENCE, moodSorterEntity.getValence());
        values.put(MoodSorterContract.MoodSorterEntry.COLUMN_NAME_TEMPO, moodSorterEntity.getTempo());
        values.put(MoodSorterContract.MoodSorterEntry.COLUMN_NAME_URI, moodSorterEntity.getUri());
        values.put(MoodSorterContract.MoodSorterEntry.COLUMN_NAME_SONGTITLE, moodSorterEntity.getSongtitle());
        values.put(MoodSorterContract.MoodSorterEntry.COLUMN_NAME_SONGARTIST, moodSorterEntity.getSongartist());

        long newRowId = db.insert(MoodSorterContract.MoodSorterEntry.TABLE_NAME, null, values);
        Log.d(TAG, "Entry added to the dataabse");
        db.close();
    }

    public MoodSorterEntity getMoodSorterEntity(String response) throws JSONException {

        JSONObject jsonObject = new JSONObject(response);
        MoodSorterEntity moodSorterEntity = new MoodSorterEntity();

        moodSorterEntity.setDanceability(jsonObject.getDouble(MoodSorterContract.MoodSorterEntry.COLUMN_NAME_DANCEABILITY));
        moodSorterEntity.setEnergy(jsonObject.getDouble(MoodSorterContract.MoodSorterEntry.COLUMN_NAME_ENERGY));
        moodSorterEntity.setLoudness(jsonObject.getDouble(MoodSorterContract.MoodSorterEntry.COLUMN_NAME_LOUDNESS));
        moodSorterEntity.setSpeechiness(jsonObject.getDouble(MoodSorterContract.MoodSorterEntry.COLUMN_NAME_SPEECHINESS));
        moodSorterEntity.setAcousticness(jsonObject.getDouble(MoodSorterContract.MoodSorterEntry.COLUMN_NAME_ACOUSTICNESS));
        moodSorterEntity.setInstrumentalness(jsonObject.getDouble(MoodSorterContract.MoodSorterEntry.COLUMN_NAME_INSTRUMENTALNESS));
        moodSorterEntity.setValence(jsonObject.getDouble(MoodSorterContract.MoodSorterEntry.COLUMN_NAME_VALENCE));
        moodSorterEntity.setTempo(jsonObject.getDouble(MoodSorterContract.MoodSorterEntry.COLUMN_NAME_TEMPO));
        moodSorterEntity.setUri(jsonObject.getString(MoodSorterContract.MoodSorterEntry.COLUMN_NAME_URI));
        moodSorterEntity.setSongtitle(mSongTitle);
        moodSorterEntity.setSongartist(mSongArtist);

        return moodSorterEntity;
    }*/

    public URI getSearchURI() throws URISyntaxException {
        String uri = SpotifyConstants.AUDIO_FEATURES_URL + mTrackId;
        return new URI(uri);
    }
}
