package com.player.licenta.androidplayer.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
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
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.player.licenta.androidplayer.R;
import com.player.licenta.androidplayer.adapter.PlaylistAdapter;
import com.player.licenta.androidplayer.adapter.SongAdapter;
import com.player.licenta.androidplayer.controller.MusicController;
import com.player.licenta.androidplayer.model.Song;
import com.player.licenta.androidplayer.service.MusicService;
import com.player.licenta.androidplayer.service.MusicService.MusicBinder;
import com.player.licenta.androidplayer.spotify.SpotifyAuthentication;
import com.player.licenta.androidplayer.util.Utils;

import static com.player.licenta.androidplayer.spotify.SpotifyAuthentication.songFeatureCache;
import static com.player.licenta.androidplayer.spotify.SpotifyConstants.*;

import java.util.ArrayList;
import java.util.Map;
import java.util.Random;

public class MainActivity extends Activity {
    private static final int LOADER_ACCESS_TOKEN = 1;
    private ArrayList<Song> songList;
    private ArrayList<Song> featuredSongList;
    private ArrayList<String> genresList;
    private ArrayList<String> groupedPlaylists;

    private MusicService musicService;
    private Intent playIntent;
    private boolean musicBound = false;
    private MusicController controller;

    private SongAdapter songAdapter;
    private PlaylistAdapter genresListAdapter;

    private ListView songView;
    private ListView genresListView;
    private TextView overridenTextView;

    ProgressDialog mDialog;

    private boolean paused = false, playbackPaused = false;

    private MusicService.OnSongChangedListener songChangedLister;

    Integer m_selectedSongIndex = 0;

    private final static String TAG = "MainActivity";

    private final static String ALL_MUSIC = "All Music";
    private final static String GENRES_PLAYLIST = "Genres playlists";
    private final static String CREATE_NEW_PLAYLIST = "+ Create new playlist";
    private static final String RANDOM_PLAYLIST = "Random playlist";
    private static final String PERSONALIZED_PLAYLIST = "Personalized playlist";
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
        groupedPlaylists = new ArrayList<>();
        groupedPlaylists.add(CREATE_NEW_PLAYLIST);
        groupedPlaylists.add(ALL_MUSIC);
        groupedPlaylists.add(GENRES_PLAYLIST);

        overridenTextView = (TextView) findViewById(R.id.overriddenTextMain);
        genresListView = (ListView) findViewById(R.id.playlist);
        genresListAdapter = new PlaylistAdapter(this, groupedPlaylists);
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
        getMenuInflater().inflate(R.menu.main_activity_menu, menu);
        return true;
    }

    public void getSongList() {
        ContentResolver musicResolver = getContentResolver();
        Uri musicUri = android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        Cursor musicCursor = musicResolver.query(musicUri, null,
                null, null, null);

        String[] genresProjection = new String[]{
                MediaStore.Audio.Genres.NAME,
                MediaStore.Audio.Genres._ID
        };

        if (musicCursor != null && musicCursor.moveToFirst()) {
            int titleColumn = musicCursor.getColumnIndex(android.provider.MediaStore.Audio.Media.TITLE);
            int idColumn = musicCursor.getColumnIndex(android.provider.MediaStore.Audio.Media._ID);
            int idArtistColumn = musicCursor.getColumnIndex(android.provider.MediaStore.Audio.Media.ARTIST);
            int idAlbumArt = musicCursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID);
            int idData = musicCursor.getColumnIndex(MediaStore.Audio.Media.DATA);

            do {
                int songId = musicCursor.getInt(idColumn);
                String thisTitle = musicCursor.getString(titleColumn);
                String thisArtist = musicCursor.getString(idArtistColumn);
                Long albumArtId = musicCursor.getLong(idAlbumArt);
                String path = musicCursor.getString(idData);

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
                genresCursor.close();
                if (genre.isEmpty()) {
                    genre = ALL_MUSIC;
                }

                songList.add(new Song(songId, thisTitle, thisArtist, genre, albumArtId, path));
            }
            while (musicCursor.moveToNext());
        }
        musicCursor.close();
    }

    public void getGenresFromUtils() {
        genresList = Utils.getGenres(songList);
    }

    @Override
    public void onBackPressed() {

    }

    public void playlistPicked(View view) {

        String currentGenre = view.getTag().toString();
        if (currentGenre.equals(ALL_MUSIC)) {
            startAllMusicActivity();
        } else if (currentGenre.equals(GENRES_PLAYLIST)){
            startGenresPlaylistActivity();
        } else if (currentGenre.equals(CREATE_NEW_PLAYLIST)){
            createNewPlaylistAlertDialog();
        } else if (currentGenre.equals(RANDOM_PLAYLIST)){
            startRandomPlaylistActivity();
        } else if (currentGenre.equals(PERSONALIZED_PLAYLIST)){
            startPersonalizedPlaylistAlertDialog();
        } else {
            startPersonalizedPlaylistActivity(currentGenre);
        }

    }

    private void startPersonalizedPlaylistAlertDialog() {
        AlertDialog.Builder builderSingle = new AlertDialog.Builder(this);
        builderSingle.setIcon(R.drawable.ic_launcher);
        builderSingle.setTitle("Select audio feature playlist:-");

        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.select_dialog_singlechoice);
        arrayAdapter.add(ACOUSTICNESS);
        arrayAdapter.add(DANCEABILITY);
        arrayAdapter.add(ENERGY);
        arrayAdapter.add(INSTRUMENTALNESS);
        arrayAdapter.add(LIVENESS);
        arrayAdapter.add(LOUDNESS);
        arrayAdapter.add(SPEECHINESS);
        arrayAdapter.add(VALENCE);


        builderSingle.setNegativeButton("cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        builderSingle.setAdapter(arrayAdapter, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                final String selectedPlaylist = arrayAdapter.getItem(which);
                AlertDialog.Builder builderInner = new AlertDialog.Builder(MainActivity.this);
                builderInner.setMessage(selectedPlaylist);
                builderInner.setTitle("Audio feature playlist selected: ");
                builderInner.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog,int which) {
                        dialog.dismiss();
                        if(!groupedPlaylists.contains(selectedPlaylist)){
                            groupedPlaylists.add(selectedPlaylist);
                            genresListAdapter.notifyDataSetChanged();
                            sendInfoToSpotifyTask(songList, selectedPlaylist);
                            mDialog = new ProgressDialog(MainActivity.this);
                            mDialog.setMessage("Please wait...");
                            mDialog.show();
                        }
                    }
                });
                builderInner.show();
            }
        });
        builderSingle.show();


    }

    public void hideProgressDialog(){
        mDialog.hide();
    }

    public void sendInfoToSpotifyTask(ArrayList<Song> songList, String chosenfeature) {
        SpotifyAuthentication spotifyAuthentication = new SpotifyAuthentication(getApplicationContext(), chosenfeature, this);
        spotifyAuthentication.execute(songList);
    }

    private void startPersonalizedPlaylistActivity(String selectedPlaylist) {

        Bundle extras = new Bundle();
        getFeaturedSongs(selectedPlaylist);
        extras.putSerializable("featured_songs", featuredSongList);
        extras.putString("chosenFeature", selectedPlaylist);
        Intent intent = new Intent(this, PersonalizedPlaylistActivity.class);
        intent.putExtras(extras);

        startActivity(intent);
    }

    private void getFeaturedSongs(String selectedPlaylist) {
        featuredSongList = new ArrayList<>();
        for (Map.Entry<String, Song> item : songFeatureCache.entrySet()) {
            String key = item.getKey();
            if(key.equals(selectedPlaylist)){
                featuredSongList.add(item.getValue());
            }
        }

    }

    private void startRandomPlaylistActivity() {
        ArrayList<Song> randomSongs = getRandomSongs();
        Bundle extras = new Bundle();
        extras.putSerializable("random_songs", randomSongs);
        Intent intent = new Intent(this, RandomPlaylistActivity.class);
        intent.putExtras(extras);

        startActivity(intent);

    }

    private ArrayList<Song> getRandomSongs() {
        ArrayList<Song> randomSongs = new ArrayList<>();
        boolean newSongFound = false;
        int randomElements = -1;
        if(songList.size() > 0 ){
            randomElements = songList.size()/3;
        }
        if (songList.size() > 10){
            randomElements = 10;
            for (int i = 0; i <= randomElements ; i++){
                if (!newSongFound){
                    i--;
                }
                Random random = new Random();
                if (songList.size() > 0){
                    int position = random.nextInt(songList.size());
                    if (!randomSongs.contains(songList.get(position))){
                        randomSongs.add(songList.get(position));
                        newSongFound = true;
                    }
                }
            }
        }
        return randomSongs;
    }

    private void createNewPlaylistAlertDialog() {
        AlertDialog.Builder builderSingle = new AlertDialog.Builder(this);
        builderSingle.setIcon(R.drawable.ic_launcher);
        builderSingle.setTitle("Select new playlist:-");

        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.select_dialog_singlechoice);
        arrayAdapter.add(RANDOM_PLAYLIST);
        arrayAdapter.add(PERSONALIZED_PLAYLIST);

        builderSingle.setNegativeButton("cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        builderSingle.setAdapter(arrayAdapter, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                final String selectedPlaylist = arrayAdapter.getItem(which);
                AlertDialog.Builder builderInner = new AlertDialog.Builder(MainActivity.this);
                builderInner.setMessage(selectedPlaylist);
                builderInner.setTitle("Your Selected Item is");
                builderInner.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog,int which) {
                        if(selectedPlaylist.equals(PERSONALIZED_PLAYLIST)){
                            dialog.dismiss();
                            startPersonalizedPlaylistAlertDialog();
                        } else {
                            dialog.dismiss();
                            if(!groupedPlaylists.contains(selectedPlaylist)){
                                groupedPlaylists.add(selectedPlaylist);
                                genresListAdapter.notifyDataSetChanged();
                            }
                        }
                    }
                });
                builderInner.show();
            }
        });
        builderSingle.show();
    }

    private void startGenresPlaylistActivity() {

        Bundle extras = new Bundle();
        extras.putSerializable("grouped_genres", genresList);
        extras.putSerializable("all_songs", songList);
        Intent intent = new Intent(this, PlaylistGenresActivity.class);
        intent.putExtras(extras);

        startActivity(intent);
    }

    private void startAllMusicActivity() {

        Bundle extras = new Bundle();
        extras.putSerializable("all_songs", songList);
        Intent intent = new Intent(this, AllMusicActivity.class);
        intent.putExtras(extras);

        startActivity(intent);
    }

    private void onCredentialsRetrieved() {
        Song currentSong = songList.get(m_selectedSongIndex);
        String songTitle = currentSong.getSongTitle();
        String songArtist = currentSong.getSongArtist();
        String[] songInfo = {songArtist, songTitle};
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
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

    public void openEqualizerActivity(MenuItem item) {
        Intent intent = new Intent(this, EqualizerActivity.class);
        startActivity(intent);
    }
}
