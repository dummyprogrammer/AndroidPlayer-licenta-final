package com.player.licenta.androidplayer.activities;

import com.bumptech.glide.Glide;
import com.player.licenta.androidplayer.controller.MusicController;
import com.player.licenta.androidplayer.service.MusicService;
import com.player.licenta.androidplayer.service.MusicService.MusicBinder;
import com.player.licenta.androidplayer.util.OnSwipeTouchListener;
import com.player.licenta.androidplayer.R;
import com.player.licenta.androidplayer.model.Song;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.content.ComponentName;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

import java.util.ArrayList;

public class SongPickedActivity extends Activity {
    private boolean musicBound = false;
    private boolean paused = false, playbackPaused = false;
    private MusicController controller;
    private ImageView coverArt;
    private MusicService musicSrv;
    private ArrayList<Song> songList;
    private Intent playIntent;
    private SongPickedActivity mInstance;
    private Long albumId;


    private final static String TAG = "SongPickedActivity";

    private Context context;

    //connect to the service
    private ServiceConnection musicConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MusicBinder binder = (MusicBinder) service;
            musicSrv = binder.getService();
            musicSrv.setList(songList);
            musicBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            musicBound = false;
        }
    };

    private void showControllerDelayed() {
        runOnUiThread(new Runnable() {

            @Override
            public void run() {
                final Handler handler = new Handler();
                handler.postDelayed(
                        new Runnable() {
                            @Override
                            public void run() {
                                controller = new MusicController(SongPickedActivity.this);

                                controller.setPrevNextListeners(
                                        new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                musicSrv.playNext();
                                                updateArtwork();
                                                //songAdapter.setHighlightRow(musicService.getSongIndex());
                                            }
                                        },
                                        new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                musicSrv.playPrev();
                                                updateArtwork();
                                                //songAdapter.setHighlightRow(musicService.getSongIndex());
                                            }
                                        }
                                );
                                MusicController.LayoutParams layoutParams = new MusicController.LayoutParams(controller.getLayoutParams());
                                controller.setLayoutParams(layoutParams);
                                controller.setMediaPlayer(musicSrv);
                                controller.setAnchorView(findViewById(R.id.coverArt));
                                controller.setEnabled(true);
                                controller.show();
                            }
                        }, 100);
            }
        });
    }

    @Override
    public ActionBar getActionBar() {
        return super.getActionBar();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.song_picked);

        mInstance = this;

        // Get the message from the intent
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();

        songList = (ArrayList<Song>) intent.getSerializableExtra("songlist");

        String songFilePath = extras.getString("SONG_PATH");
        String songArtist = extras.getString("SONG_ARTIST");
        String songTitle = extras.getString("SONG_TITLE");
        albumId = extras.getLong("ALBUM_ID");

        coverArt = (ImageView) findViewById(R.id.coverArt);

        updateTitle(songArtist, songTitle);
        extractAlbumArt(albumId);

        Log.d(TAG, "onCreate() called");
    }

    private void updateTitle(String artist, String songName) {
        String title = artist + " - " + songName;
        setTitle(title);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (playIntent == null) {
            playIntent = new Intent(this, MusicService.class);
            startService(playIntent);
            bindService(playIntent, musicConnection, Context.BIND_AUTO_CREATE);

        }
    }

    @Override
    protected void onPause() {
        if (controller != null) {
            controller.hide();
        }
        super.onPause();
        Log.d(TAG, "onPause() called");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.song_picked_activity_menu, menu);
        return true;
    }

    @Override
    protected void onStop() {
        super.onStop();
        stopService(playIntent);
        Log.d(TAG, "onStop() called");
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "onDestroy called");

        if (controller != null) {
            controller.hide();
        }
        if (musicSrv != null) {
            unbindService(musicConnection);
        }
        super.onDestroy();
    }


/*	@Override
	public boolean onOptionsItemSelected(MenuItem item) 
	{
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) 
		{
			return true;
		}
		return super.onOptionsItemSelected(item);
	}*/

    @SuppressLint("ClickableViewAccessibility")
    public void extractAlbumArt(Long albumId) {

        Uri sArtworkUri = Uri
                .parse("content://media/external/audio/albumart");
        Uri albumArtUri = ContentUris.withAppendedId(sArtworkUri, albumId);

        Glide.with(this)
                .load(albumArtUri)
                .placeholder(R.drawable.fallback_cover)
                .into(coverArt);

        coverArt.setOnTouchListener(new OnSwipeTouchListener(getApplicationContext()) {
            @Override
            public void onSwipeRight() {
                onBackPressed();
            }
        });
    }

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        showControllerDelayed();
    }

    private void updateArtwork() {
        Song currentSong = musicSrv.getCurrentSong();
        if (currentSong != null) {
            extractAlbumArt(currentSong.getAlbumArtId());

            String songTitle = currentSong.getSongTitle().toString();
            String songArtist = currentSong.getSongArtist().toString();
            updateTitle(songArtist, songTitle);
        }
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
        Log.d(TAG, "onBackPressed called");
    }

    @Override
    public void onResume() {
        super.onResume();

        if (controller != null) {
            controller.show();
        }
    }

    public void openEqualizerActivity(MenuItem item) {
        Intent intent = new Intent(this, EqualizerActivity.class);
        startActivity(intent);
    }

}
