package com.player.licenta.androidplayer.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.player.licenta.androidplayer.R;

import java.util.ArrayList;

/**
 * Created by razvan on 7/13/18.
 */

public class PlaylistAdapter extends BaseAdapter {

    private final static int INVALID_ROW_INDEX = -1;
    private ArrayList<String> genres;
    private LayoutInflater songInf;

    private RelativeLayout playlistLayout;

    private TextView playlisttextView;
    private String currentGenre;
    private int highlightedRowIndex = INVALID_ROW_INDEX;

    private static final String BLUE = "#5fb7ca";


    public PlaylistAdapter(Context context, ArrayList<String> genres) {
        this.genres = genres;
        songInf = LayoutInflater.from(context);
    }


    @Override
    public int getCount() {
        return genres.size();
    }

    @Override
    public Object getItem(int arg0) {
        return null;
    }

    @Override
    public long getItemId(int arg0) {
        return 0;
    }

    @SuppressLint("ViewHolder")
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        playlistLayout = (RelativeLayout) songInf.inflate
                (R.layout.genre, parent, false);

        playlisttextView = (TextView) playlistLayout.findViewById(R.id.genreItem);
        currentGenre = genres.get(position);

        playlisttextView.setText(currentGenre);

        if (position == 0) {
            playlisttextView.setTextColor(Color.parseColor(BLUE));
        }

        playlistLayout.setTag(currentGenre);
        return playlistLayout;
    }

    public void setHighlightRow() {
        //highlightedRowIndex = index;
        notifyDataSetChanged();
    }
}
