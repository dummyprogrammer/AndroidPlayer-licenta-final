package com.player.licenta.androidplayer;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import com.player.licenta.androidplayer.spotify.SpotifyAuthentication;

import java.util.ArrayList;

public class PlaylistActivity extends Activity {

    private ListView songView;
    private ArrayList<Song> songList = new ArrayList<>();
    private String chosenGenre;
    private SongAdapter songAdapter;

    private MusicService musicSrv;
    private Intent playIntent;
    private boolean musicBound=false;
    private MusicService.OnSongChangedListener songChangedLister;
    private MusicController controller;
    private final static String TAG = "PlaylistActivity";
    Integer m_selectedSongIndex = 0;

    private TextView itemGenreView;
    private String lastGenre = "";

    //connect to the service
    private ServiceConnection musicConnection = new ServiceConnection()
    {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service)
        {
            MusicService.MusicBinder binder = (MusicService.MusicBinder)service;
            musicSrv = binder.getService();
            musicSrv.setList(songList);
            musicSrv.setOnSongFinishedListener(songChangedLister);
            musicBound = true;
            setController();
        }

        @Override
        public void onServiceDisconnected(ComponentName name)
        {
            musicBound = false;
        }


    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_playlist);

        getSongsFromIntent();
        songView = (ListView)findViewById(R.id.song_list);
        songAdapter = new SongAdapter(this, songList);
        songAdapter.notifyDataSetChanged();
        songView.setAdapter(songAdapter);

    }

    private void getSongsFromIntent() {
        songList.clear();
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();

        chosenGenre = intent.getStringExtra("chosen_genre");
        songList = (ArrayList<Song>)intent.getSerializableExtra("grouped_songs");
    }

    private void setController()
    {		//set the controller up
        controller = new MusicController(this);

        controller.setPrevNextListeners(
                new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        musicSrv.playNext();
                        songAdapter.setHighlightRow(musicSrv.getSongIndex());
                    }
                },
                new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        musicSrv.playPrev();
                        songAdapter.setHighlightRow(musicSrv.getSongIndex());
                    }
                }
        );
        MusicController.LayoutParams p = new MusicController.LayoutParams(controller.getLayoutParams());

        controller.setLayoutParams(p);

        controller.setMediaPlayer(musicSrv);
        controller.setAnchorView(findViewById(R.id.song_list));
        controller.setEnabled(true);
    }

    @Override
    protected void onStart()
    {
        super.onStart();
        if(playIntent==null)
        {
            playIntent = new Intent(this, MusicService.class);
            bindService(playIntent, musicConnection, Context.BIND_AUTO_CREATE);
            startService(playIntent);
        }
        Log.d(TAG, "onStart called");
    }

    public void songPicked(View view)
    {
        try
        {
            int songIndex = Integer.parseInt(view.getTag().toString());
            Song currentSong  = (Song)songList.get(songIndex);
            String songTitle = currentSong.getTitle().toString();
            String songArtist = currentSong.getArtist().toString();

            if(songIndex >= 0)
            {
                if (songIndex != musicSrv.getSongIndex() || !chosenGenre.equals(lastGenre))
                {
                    musicSrv.playSong(songIndex);
                    songAdapter.setHighlightRow(musicSrv.getSongIndex());
                }
                itemGenreView = (TextView) view.findViewById(R.id.genre);
                lastGenre = itemGenreView.getText().toString();
                controller.show();
                showCoverArtActivity(view);
                sendInfoToLyricsWS(view);
                sendInfoToSpotifyTask(songTitle, songArtist);
            }
        }
        catch(NumberFormatException ex)
        {
            ex.printStackTrace();
        }
    }

    public void sendInfoToSpotifyTask(String songTitle, String songArtist) {
        SpotifyAuthentication spotifyAuthentication = new SpotifyAuthentication();
        spotifyAuthentication.execute(songTitle, songArtist);
    }


    private void showCoverArtActivity(View view)
    {
        Intent intent = new Intent(this, SongPickedActivity.class);
        Integer index = Integer.parseInt(view.getTag().toString());

        Song currentSong  = (Song)songList.get(index);
        String songTitle = currentSong.getTitle().toString();
        String songArtist = currentSong.getArtist().toString();

        String songPath = musicSrv.getSongPath();

        Bundle extras = new Bundle();
        extras.putString("SONG_PATH", songPath);
        extras.putString("SONG_ARTIST", songArtist);
        extras.putString("SONG_TITLE", songTitle);

        intent.putExtras(extras);

        intent.putExtra("songlist", songList);

        startActivity(intent);
    }

    private void sendInfoToLyricsWS(View view)
    {
        m_selectedSongIndex = Integer.parseInt(view.getTag().toString());
        //retrieveCredentials(); // FIXME
        onCredentialsRetrieved();
    }

    private void onCredentialsRetrieved()
    {
        Song currentSong  = (Song)songList.get(m_selectedSongIndex);
        String songTitle = currentSong.getTitle();
        String songArtist = currentSong.getArtist();
        String[] songInfo = {songArtist, songTitle};
        //LyricsWS getLyricsTask = new LyricsWS();
        //getLyricsTask.setParams(songArtist, songTitle);
        //getLyricsTask.setCredentials(m_credentials);
        //getLyricsTask.execute();
    }

    @Override
    protected void onDestroy()
    {
        Log.d(TAG, "onDestroy called");

        controller.hide();
        stopService(playIntent);
        unbindService(musicConnection);

        super.onDestroy();
    }

    @Override
    protected void onResume()
    {
        super.onResume();
    }
}
