package com.player.licenta.androidplayer.adapter;

import android.content.ContentUris;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.provider.MediaStore;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.player.licenta.androidplayer.R;
import com.player.licenta.androidplayer.model.Song;


public class SongAdapter extends BaseAdapter {
    private final static int INVALID_ROW_INDEX = -1;
    private ArrayList<Song> songs;
    private LayoutInflater songInf;

    private RelativeLayout songLayout;

    private ImageView albumArtView;
    private TextView artistView;
    private TextView songView;
    private TextView genreView;
    private Song currSong;
    private int highlightedRowIndex = INVALID_ROW_INDEX;
    private Context context;

    private static final String HIGHLITED_COLOR = "#80bfff";


    public SongAdapter(Context context, ArrayList<Song> theSongs) {
        this.context = context;
        songs = theSongs;
        songInf = LayoutInflater.from(context);
    }


    @Override
    public int getCount() {
        return songs.size();
    }

    @Override
    public Object getItem(int arg0) {
        return null;
    }

    @Override
    public long getItemId(int arg0) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        //map to song layout
        songLayout = (RelativeLayout) songInf.inflate
                (R.layout.song, parent, false);

        //get title and artist views
        songView = (TextView) songLayout.findViewById(R.id.song_title);
        artistView = (TextView) songLayout.findViewById(R.id.song_artist);
        albumArtView = (ImageView) songLayout.findViewById(R.id.songListCover);
        //genreView = (TextView) songLayout.findViewById(R.id.genre);

        //get song using position
        currSong = songs.get(position);

        //get title and artist strings
        songView.setText(currSong.getSongTitle());
        artistView.setText(currSong.getSongArtist());

        String songFilePath = currSong.getPath();
        Long albumArtId = currSong.getAlbumArtId();

        Uri sArtworkUri = Uri
                .parse("content://media/external/audio/albumart");
        Uri albumArtUri = ContentUris.withAppendedId(sArtworkUri, albumArtId);

        Glide.with(context)
                .load(albumArtUri)
                .placeholder(R.drawable.fallback_cover)
                .into(albumArtView);

        //genreView.setText(currSong.getGenre());

        if ((highlightedRowIndex != INVALID_ROW_INDEX) &&
                (position == highlightedRowIndex)) {
            songView.setTextColor(Color.parseColor(HIGHLITED_COLOR));
            artistView.setTextColor(Color.parseColor(HIGHLITED_COLOR));
            //genreView.setTextColor(Color.parseColor(HIGHLITED_COLOR));
        }

        //set position as tag
        songLayout.setTag(position);
        return songLayout;
    }

    public void setHighlightRow(int index) {
        highlightedRowIndex = index;
        notifyDataSetChanged();
    }

    private Bitmap getBitmapImage(Uri albumArtUri) {
        Bitmap bitmap = null;
        try {
            bitmap = MediaStore.Images.Media.getBitmap(
                    context.getContentResolver(), albumArtUri);
            bitmap = Bitmap.createScaledBitmap(bitmap, 30, 30, true);

        } catch (FileNotFoundException exception) {
            exception.printStackTrace();
            bitmap = BitmapFactory.decodeResource(context.getResources(),
                    R.drawable.fallback_cover);
        } catch (IOException e) {

            e.printStackTrace();
        }
        return bitmap;
    }
}