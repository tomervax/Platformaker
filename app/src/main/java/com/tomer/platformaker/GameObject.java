package com.tomer.platformaker;

import android.graphics.Canvas;


public abstract class GameObject {
    public float x, y, width, height;
    protected DisplayView displayView;

    public GameObject(DisplayView displayView, float x, float y) {
        this.displayView = displayView;
        this.x = x;
        this.y = y;
    }

    public void setDisplayView(DisplayView displayView) {
        this.displayView = displayView;
    }

    public abstract void draw(Canvas canvas);
    public abstract void update();
}
