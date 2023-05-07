package com.tomer.platformaker.game;

import android.graphics.Canvas;
import android.os.Looper;
import android.util.Log;
import android.view.SurfaceHolder;

public class GameThread extends Thread {
    private final GameView gameView;
    public final SurfaceHolder surfaceHolder;
    private boolean running;
    public Canvas canvas;
    public boolean pause;

    public GameThread(SurfaceHolder surfaceHolder, GameView gameView) {
        super();
        this.surfaceHolder = surfaceHolder;
        this.gameView = gameView;
    }

    @Override
    public void run() {
        int frameCount = 0;
        long startTime = System.nanoTime();
        while(running) {
            while (pause) {
                try {
                    sleep(1);
                } catch (InterruptedException e) {
                    break;
                }
            }
            canvas = this.surfaceHolder.lockCanvas();
            synchronized (surfaceHolder) {
                this.gameView.update();
                this.gameView.draw(canvas);
            }
            if(canvas!=null) {
                try {
                    surfaceHolder.unlockCanvasAndPost(canvas);
                    this.gameView.postInvalidate();
                }
                catch(Exception e){e.printStackTrace();}
            }
            frameCount++;
            if (System.nanoTime() - startTime > 1_000_000_000){
                Log.d("FPS", "fps: " + frameCount);
                startTime = System.nanoTime();
                frameCount = 0;
            }
        }
    }

    public void setRunning(boolean isRunning) {
        running = isRunning;
    }
}
