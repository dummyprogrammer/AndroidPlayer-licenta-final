package com.player.licenta.androidplayer.spotify;

import android.os.AsyncTask;
import android.util.Log;

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

public class SpotifyAuthentication extends AsyncTask<String, Void, Void> {

    private String mToken;
    private String mSongTitle;
    private String mSongArtist;

    private final static String TAG = "SpotifyAuthentication";

    @Override
    protected Void doInBackground(String... songData) {
        mSongTitle = songData[0];
        mSongArtist = songData[1];
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
        SpotifySearch spotifyUtil = new SpotifySearch();
        spotifyUtil.execute(mToken, mSongTitle, mSongArtist);
    }

}
