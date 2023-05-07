package com.tomer.platformaker.game;

import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.ColorInt;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.constraintlayout.widget.ConstraintSet;

import com.tomer.platformaker.R;

import java.util.HashMap;

public class GameController implements View.OnTouchListener {
    private static HashMap<Integer, Boolean> states = new HashMap<Integer, Boolean>();
    private static GameController singletonInstance;

    @ColorInt private static final int SELECTED = 0x80000000, DEFAULT = 0x40000000;

    public GameController(View ... buttons){
        singletonInstance = this;
        for (View button: buttons){
            button.setOnTouchListener(singletonInstance);
            states.put(button.getId(), false);
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN){
            states.put(v.getId(), true);
            v.setBackgroundColor(SELECTED);
        } else if (event.getAction() == MotionEvent.ACTION_UP){
            states.put(v.getId(), false);
            v.setBackgroundColor(DEFAULT);
        }  else {
            return false;
        }return true;
    }

    public static boolean isDown(int id){
        return states.get(id);
    }
}
