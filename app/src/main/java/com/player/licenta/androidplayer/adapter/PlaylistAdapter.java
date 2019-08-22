package com.player.licenta.androidplayer.adapter;

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


    public PlaylistAdapter(Context c, ArrayList<String> genres) {
        this.genres = genres;
        songInf = LayoutInflater.from(c);
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

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        //map to song layout
        playlistLayout = (RelativeLayout) songInf.inflate
                (R.layout.playlists, parent, false);

        playlisttextView = (TextView) playlistLayout.findViewById(R.id.genre);

        currentGenre = genres.get(position);

        playlisttextView.setText(currentGenre);

        if ((highlightedRowIndex != INVALID_ROW_INDEX) &&
                (position == highlightedRowIndex)) {
            playlisttextView.setTextColor(Color.BLUE);
        }

        playlistLayout.setTag(currentGenre);
        return playlistLayout;
    }

    public void setHighlightRow() {
        //highlightedRowIndex = index;
        notifyDataSetChanged();
    }
}
