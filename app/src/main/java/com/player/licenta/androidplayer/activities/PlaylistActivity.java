package com.player.licenta.androidplayer.activities;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import com.player.licenta.androidplayer.controller.MusicController;
import com.player.licenta.androidplayer.service.MusicService;
import com.player.licenta.androidplayer.R;
import com.player.licenta.androidplayer.model.Song;
import com.player.licenta.androidplayer.adapter.SongAdapter;
import com.player.licenta.androidplayer.spotify.SpotifyAuthentication;

import java.util.ArrayList;

public class PlaylistActivity extends Activity {

    private ListView songView;
    private ArrayList<Song> songList = new ArrayList<>();
    private String chosenGenre;
    private SongAdapter songAdapter;

    private MusicService musicSrv;
    private Intent playIntent;
    private boolean musicBound = false;
    private MusicService.OnSongChangedListener songChangedLister;
    private MusicController controller;
    private final static String TAG = "PlaylistActivity";
    Integer m_selectedSongIndex = 0;

    private TextView itemGenreView;
    private String lastGenre = "";

    //connect to the service
    private ServiceConnection musicConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MusicService.MusicBinder binder = (MusicService.MusicBinder) service;
            musicSrv = binder.getService();
            musicSrv.setList(songList);
            musicSrv.setOnSongFinishedListener(songChangedLister);
            musicBound = true;
            //setController();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            musicBound = false;
        }


    };

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_playlist);

        getSongsFromIntent();
        songView = (ListView) findViewById(R.id.song_list);
        songAdapter = new SongAdapter(this, songList);
        songAdapter.notifyDataSetChanged();
        songView.setAdapter(songAdapter);

    }

    private void getSongsFromIntent() {
        songList.clear();
        Intent intent = getIntent();
        songList = (ArrayList<Song>) intent.getSerializableExtra("all_songs");
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
                                controller = new MusicController(PlaylistActivity.this);

                                controller.setPrevNextListeners(
                                        new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                musicSrv.playNext();
                                                songAdapter.setHighlightRow(musicSrv.getSongIndex());
                                            }
                                        },
                                        new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                musicSrv.playPrev();
                                                songAdapter.setHighlightRow(musicSrv.getSongIndex());
                                            }
                                        }
                                );
                                MusicController.LayoutParams layoutParams = new MusicController.LayoutParams(controller.getLayoutParams());
                                controller.setLayoutParams(layoutParams);
                                controller.setMediaPlayer(musicSrv);
                                controller.setAnchorView(findViewById(R.id.overriddenTextPlaylist));
                                controller.setEnabled(true);
                                controller.show();
                            }
                        }, 100);
            }
        });
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

    public void songPicked(View view) {
        try {
            int songIndex = Integer.parseInt(view.getTag().toString());
            Song currentSong = (Song) songList.get(songIndex);
            String songTitle = currentSong.getSongTitle();
            String songArtist = currentSong.getSongArtist();

            if (songIndex >= 0) {
                if (songIndex != musicSrv.getSongIndex() || !chosenGenre.equals(lastGenre)) {
                    musicSrv.playSong(songIndex);
                    songAdapter.setHighlightRow(musicSrv.getSongIndex());
                }
                itemGenreView = (TextView) view.findViewById(R.id.genre);
                lastGenre = itemGenreView.getText().toString();
                controller.show();
                showCoverArtActivity(view);
                sendInfoToLyricsWS(view);
                sendInfoToSpotifyTask(songList);
            }
        } catch (NumberFormatException ex) {
            ex.printStackTrace();
        }
    }

    public void sendInfoToSpotifyTask(ArrayList<Song> songList) {
        SpotifyAuthentication spotifyAuthentication = new SpotifyAuthentication(getApplicationContext());
        spotifyAuthentication.execute(songList);
    }


    private void showCoverArtActivity(View view) {
        Intent intent = new Intent(this, SongPickedActivity.class);
        Integer index = Integer.parseInt(view.getTag().toString());

        Song currentSong = (Song) songList.get(index);
        String songTitle = currentSong.getSongTitle().toString();
        String songArtist = currentSong.getSongArtist().toString();
        String songPath = musicSrv.getSongPath();
        Long albumId = currentSong.getAlbumArtId();

        Bundle extras = new Bundle();
        extras.putString("SONG_PATH", songPath);
        extras.putString("SONG_ARTIST", songArtist);
        extras.putString("SONG_TITLE", songTitle);
        extras.putLong("ALBUM_ID", albumId);

        intent.putExtras(extras);

        intent.putExtra("songlist", songList);

        startActivity(intent);
    }

    private void sendInfoToLyricsWS(View view) {
        m_selectedSongIndex = Integer.parseInt(view.getTag().toString());
        //retrieveCredentials(); // FIXME
        onCredentialsRetrieved();
    }

    private void onCredentialsRetrieved() {
        Song currentSong = (Song) songList.get(m_selectedSongIndex);
        String songTitle = currentSong.getSongTitle();
        String songArtist = currentSong.getSongArtist();
        String[] songInfo = {songArtist, songTitle};
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "onDestroy called");

        controller.hide();
        stopService(playIntent);
        unbindService(musicConnection);

        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    public void openEqualizerActivity(MenuItem item) {
        Intent intent = new Intent(this, EqualizerActivity.class);
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.song_picked_activity_menu, menu);
        return true;
    }
}
