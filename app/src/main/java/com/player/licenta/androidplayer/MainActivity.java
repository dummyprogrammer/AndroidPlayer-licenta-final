package com.player.licenta.androidplayer;

import android.app.Activity;
import android.app.LoaderManager;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.Loader;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;

import com.google.auth.Credentials;
import com.google.auth.oauth2.AccessToken;
import com.google.auth.oauth2.OAuth2Credentials;
import com.player.licenta.androidplayer.MusicService.MusicBinder;
import com.player.licenta.androidplayer.adapter.PlaylistAdapter;
import com.player.licenta.androidplayer.lyrics.AccessTokenLoader;
import com.player.licenta.androidplayer.moodsorter.MoodSorter;
import com.player.licenta.androidplayer.util.Utils;

import java.util.ArrayList;
import java.util.Date;

public class MainActivity extends Activity
{
    private static final int LOADER_ACCESS_TOKEN = 1;
    private ArrayList<Song> songList;
    private ArrayList<String> genresList;
	private ListView songView;
    //private SeekBar volumeControl;

	private MusicService musicSrv;
	private Intent playIntent;
	private boolean musicBound=false;


	private MusicController controller;

	private ListView genresListView;
	private SongAdapter songAdt;
	private PlaylistAdapter genresListAdapter;

	private boolean paused=false, playbackPaused=false;

	private MusicService.OnSongChangedListener songChangedLister;

    Credentials m_credentials;
    Integer m_selectedSongIndex = 0;

	private final static String TAG = "MainActivity";

    private final static String ALL_MUSIC = "all music";

	//connect to the service
	private ServiceConnection musicConnection = new ServiceConnection()
	{
		@Override
		public void onServiceConnected(ComponentName name, IBinder service)
		{
			MusicBinder binder = (MusicBinder)service;
			musicSrv = binder.getService();
			musicSrv.setList(songList);
			musicSrv.setOnSongFinishedListener(songChangedLister);
			musicBound = true;
			//setController();
		}

		@Override
		public void onServiceDisconnected(ComponentName name)
		{
			musicBound = false;
		}


	};

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
		super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        songList = new ArrayList<Song>();
		getSongList();
		getGenresFromUtils();
        genresListView = (ListView) findViewById(R.id.playlist);
        genresListAdapter = new PlaylistAdapter(this, genresList);
        genresListView.setAdapter(genresListAdapter);

		Log.d(TAG, "onCreate called");
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

    @Override
    protected void onResume()
    {
        super.onResume();
//		if (controller != null)
//        {
//            controller.show();
//            songAdt.setHighlightRow(musicSrv.getSongIndex());
//			musicSrv.setOnSongFinishedListener(songChangedLister);
//        }
    }

    @Override
    protected void onPause()
    {
        super.onPause();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    public void getSongList()
    {
    	  //retrieve song info
    	ContentResolver musicResolver = getContentResolver();
    	Uri musicUri = android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
    	Cursor musicCursor = musicResolver.query(musicUri, null, null, null, null);

    	String[] genresProjection = {
                MediaStore.Audio.Genres.NAME,
                MediaStore.Audio.Genres._ID
        };


        if(musicCursor!=null && musicCursor.moveToFirst())
        {
			//get columns
			int titleColumn = musicCursor.getColumnIndex(android.provider.MediaStore.Audio.Media.TITLE);
			int idColumn = musicCursor.getColumnIndex(android.provider.MediaStore.Audio.Media._ID);
			int artistColumn = musicCursor.getColumnIndex(android.provider.MediaStore.Audio.Media.ARTIST);

			//add songs to list
			do
			{
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
                if(genre.isEmpty()){
                    genre = ALL_MUSIC;
                }
				songList.add(new Song(songId, thisTitle, thisArtist, genre));
			}
			while (musicCursor.moveToNext());
        }
    }

    public void getGenresFromUtils(){
        genresList = Utils.getGenres(songList);
    }

	public void songPicked(View view)
    {
		try
		{
			int songIndex = Integer.parseInt(view.getTag().toString());
			if(songIndex >= 0)
			{
				if (songIndex != musicSrv.getSongIndex())
				{
					musicSrv.playSong(songIndex);
					songAdt.setHighlightRow(musicSrv.getSongIndex());
				}
				controller.show();
				showCoverArtActivity(view);
                sendInfoToLyricsWS(view);
			}
		}
		catch(NumberFormatException ex)
		{
            // safe to ignore for now
            ex.printStackTrace();
		}
	}

    public void playlistPicked(View view){

        ArrayList<Song> groupedSongs = new ArrayList<>();
        String currentGenre = view.getTag().toString();
        if(currentGenre.equals(ALL_MUSIC)){
            groupedSongs = songList;
        }else{
            groupedSongs = Utils.getGroupedSongs(songList, currentGenre);
        }
        Bundle extras = new Bundle();
        extras.putSerializable("grouped_songs", groupedSongs);
        extras.putString("chosen_genre", currentGenre);

        Intent intent = new Intent(this, PlaylistActivity.class);
        intent.putExtras(extras);

        startActivity(intent);

    }

	private void sendInfoToLyricsWS(View view)
    {
        m_selectedSongIndex = Integer.parseInt(view.getTag().toString());
        //retrieveCredentials(); // FIXME
        onCredentialsRetrieved();
    }

    private void retrieveCredentials() {
        // Initiate token refresh
        getLoaderManager().initLoader(LOADER_ACCESS_TOKEN, null,
                new LoaderManager.LoaderCallbacks<String>() {
                    @Override
                    public Loader<String> onCreateLoader(int id, Bundle args) {
                        return new AccessTokenLoader(MainActivity.this);
                    }

                    @Override
                    public void onLoadFinished(Loader<String> loader, String token) {
                        setAccessToken(token);
                    }

                    @Override
                    public void onLoaderReset(Loader<String> loader) {
                    }
                });
    }

    public void setAccessToken(String token) {
        Log.d(TAG, "setAccessToken:" + token);

        long oneHourMs = 3600 * 1000;
        Date expirationTime = new Date(oneHourMs);
        AccessToken accessToken = new AccessToken(token, expirationTime);
        m_credentials = OAuth2Credentials.create(accessToken);
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

/*		Context context = getApplicationContext();
		CharSequence text = songPath;
		int duration = Toast.LENGTH_SHORT;

		Toast toast = Toast.makeText(context, text, duration);
		toast.show();*/

		startActivity(intent);
	}

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
    	//menu item selected
    	switch (item.getItemId())
    	{
    		case R.id.sort_item:
    		    sendSongsInfoToMoodSorter();
    			break;

    		case R.id.action_shuffle:
    			 musicSrv.setShuffle();
    			 break;
    	}
    	return super.onOptionsItemSelected(item);
    }

    private void sendSongsInfoToMoodSorter() {
        MoodSorter moodSorter = new MoodSorter(songList);
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
						songAdt.setHighlightRow(musicSrv.getSongIndex());
					}
				},
				new View.OnClickListener()
				{
					@Override
					public void onClick(View v)
					{
						musicSrv.playPrev();
						songAdt.setHighlightRow(musicSrv.getSongIndex());
					}
				}
		);
        MusicController.LayoutParams p = new MusicController.LayoutParams(controller.getLayoutParams());
        
        controller.setLayoutParams(p);


        controller.setMediaPlayer(musicSrv);
		controller.setAnchorView(findViewById(R.id.song_list));
		controller.setEnabled(true);
		controller.show();
	}
}
