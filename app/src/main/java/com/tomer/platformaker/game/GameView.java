package com.tomer.platformaker.game;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.SurfaceHolder;

import androidx.annotation.NonNull;

import com.tomer.platformaker.DisplayView;

import java.util.HashMap;

public class GameView extends DisplayView {
    public final GameThread thread;
    public Player player;
    public GameActivity activity;

    public GameView(Context context, AttributeSet attrs) {
        super(context, attrs);
        thread = new GameThread(holder, this);
    }

    public void update() {
        player.update();
    }

    @Override
    public void restart(HashMap<String, Object> serializedLevel) {
        super.restart(serializedLevel);
        this.player = new Player(this);
    }

    @Override
    public void draw(Canvas canvas) {
        if (level == null || canvas == null || dim == null){
            return;
        }
        cameraX = player.x + player.width/2;
        cameraY = player.y + player.height/2;
        super.draw(canvas);
        player.draw(canvas);
    }

    @Override
    public void surfaceCreated(@NonNull SurfaceHolder holder) {
        thread.setRunning(true);
        if (!thread.isAlive()) {
            thread.start();
        }
        super.surfaceCreated(holder);
    }

    @Override
    public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int width, int height) {
        super.surfaceChanged(holder, format, width, height);
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        stopThread();
    }

    public void stopThread() {
        thread.pause = false;
        boolean retry = true;
        while (retry) {
            try {
                thread.setRunning(false);
                thread.join(0);
            } catch(InterruptedException e){
                e.printStackTrace();
            }
            retry = false;
        }
    }
}
