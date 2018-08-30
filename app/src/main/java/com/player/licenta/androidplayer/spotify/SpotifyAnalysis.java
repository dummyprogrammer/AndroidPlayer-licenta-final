package com.player.licenta.androidplayer.spotify;

import android.os.AsyncTask;
import android.util.Log;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * Created by razvan on 8/29/18.
 */

public class SpotifyAnalysis extends AsyncTask<String ,Void, Void> {

    private String mTrackId;
    private String mToken;
    private final static String TAG = "SpotifyAnalysis";

    @Override
    protected Void doInBackground(String... strings) {
        mTrackId = strings[0];
        mToken = strings[1];
        getAudioFeatures();
        return null;
    }

    public void getAudioFeatures(){

        try{
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
        } catch (URISyntaxException ex){
            ex.printStackTrace();
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public URI getSearchURI() throws URISyntaxException {
        String uri = SpotifyConstants.AUDIO_FEATURES_URL + mTrackId;
        return new URI(uri);
    }
}
