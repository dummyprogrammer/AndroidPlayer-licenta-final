package com.player.licenta.androidplayer.adapter;

import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.player.licenta.androidplayer.R;
import com.player.licenta.androidplayer.model.Song;


public class SongAdapter extends BaseAdapter {
    private final static int INVALID_ROW_INDEX = -1;
    private ArrayList<Song> songs;
    private LayoutInflater songInf;

    private RelativeLayout songLayout;

    private TextView artistView;
    private TextView songView;
    private TextView genreView;
    private Song currSong;
    private int highlightedRowIndex = INVALID_ROW_INDEX;

    private static final String HIGHLITED_COLOR = "#80bfff";


    public SongAdapter(Context c, ArrayList<Song> theSongs) {
        songs = theSongs;
        songInf = LayoutInflater.from(c);
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
        //genreView = (TextView) songLayout.findViewById(R.id.genre);

        //get song using position
        currSong = songs.get(position);

        //get title and artist strings
        songView.setText(currSong.getTitle());
        artistView.setText(currSong.getArtist());
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
}