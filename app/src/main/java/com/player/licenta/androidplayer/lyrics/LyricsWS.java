package com.player.licenta.androidplayer.lyrics;

import android.os.AsyncTask;
import android.util.Log;

import com.google.auth.Credentials;
import com.player.licenta.androidplayer.Song;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

/**
 * Created by razvan on 7/10/18.
 */

public class LyricsWS extends AsyncTask<Object, Void, Void>  {

    private final static String TAG = "LyricsWS";
    private final String LYRICS_URL = "http://api.chartlyrics.com/apiv1.asmx/SearchLyricDirect?";
    private Song m_song;
    URI m_uri;
    private String m_lyrics;
    private Credentials m_credentials;

    private static HashMap<Song, String> songs = new HashMap<>();
    private String songName;
    private String artistName;
    private Song currentSong;

    public LyricsWS(Song song)
    {
        m_song = song;
        String artist = song.getArtist().replaceAll(" ", "+");
        String songName = song.getTitle().replaceAll(" ", "+");
        try {
            m_uri = new URI(LYRICS_URL +
                    "artist=" + artist +
                    "&" +
                    "song=" + songName);
        }
        catch (URISyntaxException e)
        {
            e.printStackTrace();
        }
    }

    public void setCredentials(Credentials credentials)
    {
        m_credentials = credentials;
    }

    public void getLyricsFromWS() {

        try {
            HttpClient httpClient=new DefaultHttpClient();
            HttpGet httpGet = new HttpGet();
            httpGet.setURI(m_uri);

            HttpResponse httpResponse = httpClient.execute(httpGet);
            InputStream is = httpResponse.getEntity().getContent();
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            String line = "";
            StringBuffer sb = new StringBuffer();
            while ( (line = br.readLine()) != null ){
                sb.append(line);
                sb.append(" ");
            }
            String response = sb.toString();

            Document doc = convertStringToDocument(response);
            NodeList nodeList = doc.getElementsByTagName("GetLyricResult");
            Node node = (Element) nodeList.item(0);
            Element element = (Element) node;
            m_lyrics = element.getElementsByTagName("Lyric").item(0).getTextContent();
            m_song.setLyrics(m_lyrics);

            Log.d(TAG, m_lyrics);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected Void doInBackground(Object[] objects) {
        getLyricsFromWS();
        return null;
    }

    private static Document convertStringToDocument(String xmlStr) {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder;
        try
        {
            builder = factory.newDocumentBuilder();
            Document doc = builder.parse( new InputSource( new StringReader( xmlStr ) ) );
            return doc;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        // FIXME
        //new SentimentWS(m_lyrics, m_credentials).execute();
    }
}
