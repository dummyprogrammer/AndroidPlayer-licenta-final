package com.player.licenta.androidplayer.lyrics;

import android.annotation.TargetApi;
import android.os.AsyncTask;
import android.os.Build;
import android.util.Log;

import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.gax.core.FixedCredentialsProvider;
import com.google.api.services.language.v1.CloudNaturalLanguage;
import com.google.cloud.language.v1.AnalyzeEntitiesRequest;
import com.google.cloud.language.v1.AnalyzeSentimentRequest;
import com.google.cloud.language.v1.AnalyzeSentimentResponse;
import com.google.cloud.language.v1.Document;
import com.google.cloud.language.v1.LanguageServiceClient;
import com.google.cloud.language.v1.LanguageServiceSettings;
import com.google.cloud.language.v1.Sentiment;
import com.google.auth.Credentials;

import java.io.IOException;

/**
 * Created by razvan on 7/11/18.
 */

public class SentimentWS extends AsyncTask<Object, Void, Void> {

    private final static String TAG = "SentimentWS";
    private String m_lyrics;
    private Credentials m_credentials;

    public SentimentWS(String lyrics, Credentials credentials)
    {
        m_lyrics = lyrics;
        m_credentials = credentials;
    }

    @Override
    protected Void doInBackground(Object... params) {
        getSentimentAnalysis();
        return null;
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    private void getSentimentAnalysis() {

        try {
            LanguageServiceSettings settings = LanguageServiceSettings.newBuilder().
                    setCredentialsProvider(FixedCredentialsProvider.create(m_credentials)).build();

            // Instantiate the Language client com.google.cloud.language.v1.LanguageServiceClient
            try (LanguageServiceClient language = LanguageServiceClient.create(settings)) {
                Document doc = Document.newBuilder()
                        .setContent(m_lyrics)
                        .setType(Document.Type.PLAIN_TEXT)
                        .build();
                AnalyzeSentimentResponse response = language.analyzeSentiment(doc);
                Sentiment sentiment = response.getDocumentSentiment();
                if (sentiment == null) {
                    System.out.println("No sentiment found");
                } else {
                    System.out.printf("Sentiment magnitude: %.3f\n", sentiment.getMagnitude());
                    System.out.printf("Sentiment score: %.3f\n", sentiment.getScore());
                }
                String analysis = "Sentiment magnitude: %.3f " + sentiment.getMagnitude() + "Sentiment score: %.3f " + sentiment.getScore();
                Log.d(TAG, analysis);
            } catch (IOException e){
                e.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        catch (Throwable t)
        {
            t.printStackTrace();
        }
    }
}
