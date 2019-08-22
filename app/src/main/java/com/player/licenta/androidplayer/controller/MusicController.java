package com.player.licenta.androidplayer.controller;

import android.app.Activity;
import android.content.Context;
import android.view.KeyEvent;
import android.widget.LinearLayout;
import android.widget.MediaController;

public class MusicController extends MediaController {

    public MusicController(Context context) {
        super(context);
    }

    @Override
    public void show() {
        super.show(0);
    }

    @Override
    public void show(int timeout) {
        super.show(0);
    }

    @Override
    public void hide() {
        super.hide();
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
            super.hide();//Hide mediaController
            ((Activity) getContext()).finish();
        }
        return super.dispatchKeyEvent(event);
    }
}