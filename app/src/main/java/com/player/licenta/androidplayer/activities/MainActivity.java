package com.player.licenta.androidplayer.activities;

import android.app.Activity;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import com.player.licenta.androidplayer.R;
import com.player.licenta.androidplayer.adapter.PlaylistAdapter;
import com.player.licenta.androidplayer.adapter.SongAdapter;
import com.player.licenta.androidplayer.controller.MusicController;
import com.player.licenta.androidplayer.model.Song;
import com.player.licenta.androidplayer.service.MusicService;
import com.player.licenta.androidplayer.service.MusicService.MusicBinder;
import com.player.licenta.androidplayer.util.Utils;

import java.util.ArrayList;

public class MainActivity extends Activity {
    private static final int LOADER_ACCESS_TOKEN = 1;
    private ArrayList<Song> songList;
    private ArrayList<String> genresList;

    private MusicService musicService;
    private Intent playIntent;
    private boolean musicBound = false;
    private MusicController controller;

    private SongAdapter songAdapter;
    private PlaylistAdapter genresListAdapter;

    private ListView songView;
    private ListView genresListView;
    private TextView overridenTextView;

    private boolean paused = false, playbackPaused = false;

    private MusicService.OnSongChangedListener songChangedLister;

    Integer m_selectedSongIndex = 0;

    private final static String TAG = "MainActivity";

    private final static String ALL_MUSIC = "all music";

    //connect to the service
    private ServiceConnection musicConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MusicBinder binder = (MusicBinder) service;
            musicService = binder.getService();
            musicService.setList(songList);
            musicService.setOnSongFinishedListener(songChangedLister);
            musicBound = true;
            //setController();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            musicBound = false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        songList = new ArrayList<Song>();
        getSongList();
        getGenresFromUtils();
        overridenTextView = (TextView) findViewById(R.id.overriddenTextMain);
        genresListView = (ListView) findViewById(R.id.playlist);
        genresListAdapter = new PlaylistAdapter(this, genresList);
        genresListView.setAdapter(genresListAdapter);


        Log.d(TAG, "onCreate called");
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (playIntent == null) {
            playIntent = new Intent(this, MusicService.class);
            bindService(playIntent, musicConnection, Context.BIND_AUTO_CREATE);
            startService(playIntent);
        }
        Log.d(TAG, "onStart called");
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(controller != null){
            controller.show();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    public void getSongList() {
        ContentResolver musicResolver = getContentResolver();
        Uri musicUri = android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        Cursor musicCursor = musicResolver.query(musicUri, null, null, null, null);

        String[] genresProjection = {
                MediaStore.Audio.Genres.NAME,
                MediaStore.Audio.Genres._ID
        };

        if (musicCursor != null && musicCursor.moveToFirst()) {
            int titleColumn = musicCursor.getColumnIndex(android.provider.MediaStore.Audio.Media.TITLE);
            int idColumn = musicCursor.getColumnIndex(android.provider.MediaStore.Audio.Media._ID);
            int artistColumn = musicCursor.getColumnIndex(android.provider.MediaStore.Audio.Media.ARTIST);

            do {
                int songId = musicCursor.getInt(idColumn);
                String thisTitle = musicCursor.getString(titleColumn);
                String thisArtist = musicCursor.getString(artistColumn);

                Uri uri = MediaStore.Audio.Genres.getContentUriForAudioId("external", songId);
                Cursor genresCursor = getContentResolver().query(uri,
                        genresProjection, null, null, null);
                int genre_column_index = genresCursor.getColumnIndexOrThrow(MediaStore.Audio.Genres.NAME);

                String genre = "";
                if (genresCursor.moveToFirst()) {
                    do {
                        genre += genresCursor.getString(genre_column_index) + " ";
                    } while (genresCursor.moveToNext());
                }
                if (genre.isEmpty()) {
                    genre = ALL_MUSIC;
                }
                songList.add(new Song(songId, thisTitle, thisArtist, genre));
            }
            while (musicCursor.moveToNext());
        }
        musicCursor.close();
    }

    public void getGenresFromUtils() {
        genresList = Utils.getGenres(songList);
    }

    public void playlistPicked(View view) {

        ArrayList<Song> groupedSongs = new ArrayList<>();
        String currentGenre = view.getTag().toString();
        if (currentGenre.equals(ALL_MUSIC)) {
            groupedSongs = songList;
        } else {
            groupedSongs = Utils.getGroupedSongs(songList, currentGenre);
        }
        Bundle extras = new Bundle();
        extras.putSerializable("grouped_songs", groupedSongs);
        extras.putString("chosen_genre", currentGenre);

        Intent intent = new Intent(this, PlaylistActivity.class);
        intent.putExtras(extras);

        startActivity(intent);

    }

    private void onCredentialsRetrieved() {
        Song currentSong = songList.get(m_selectedSongIndex);
        String songTitle = currentSong.getTitle();
        String songArtist = currentSong.getArtist();
        String[] songInfo = {songArtist, songTitle};
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //menu item selected
        switch (item.getItemId()) {
            case R.id.sort_item:
                break;

            case R.id.action_shuffle:
                musicService.setShuffle();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "onDestroy called");

        controller.hide();
        stopService(playIntent);
        unbindService(musicConnection);

        super.onDestroy();
    }

    private void setController() {

        //controller.show();
    }

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        showControllerDelayed();
    }

    private void showControllerDelayed() {
        runOnUiThread(new Runnable() {

            @Override
            public void run() {
                final Handler handler = new Handler();
                handler.postDelayed(
                        new Runnable() {
                            @Override
                            public void run() {
                                controller = new MusicController(MainActivity.this);

                                controller.setPrevNextListeners(
                                        new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                musicService.playNext();
                                                //songAdapter.setHighlightRow(musicService.getSongIndex());
                                            }
                                        },
                                        new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                musicService.playPrev();
                                                //songAdapter.setHighlightRow(musicService.getSongIndex());
                                            }
                                        }
                                );
                                MusicController.LayoutParams layoutParams = new MusicController.LayoutParams(controller.getLayoutParams());
                                controller.setLayoutParams(layoutParams);
                                controller.setMediaPlayer(musicService);
                                controller.setAnchorView(findViewById(R.id.overriddenTextMain));
                                controller.setEnabled(true);
                                controller.show();
                            }
                        }, 100);
            }
        });
    }
}
