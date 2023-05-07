package com.tomer.platformaker;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.math.MathUtils;

import com.tomer.platformaker.game.Player;

import java.util.HashMap;

public abstract class DisplayView extends SurfaceView implements SurfaceHolder.Callback {
    protected final SurfaceHolder holder;

    public static final int BACKGROUND_COLOR = Color.rgb(130, 250, 255);

    public Level level;

    protected float cameraX = 0, cameraY = 0, zoom = 1;
    protected RectF cameraConstraints = new RectF();
    private float minZoom;

    protected Rect dim;
    private float centerX, centerY;

    public RectF visibleArea;

    public DisplayView(Context context, AttributeSet attrs) {
        super(context, attrs);
        holder = getHolder();
        holder.addCallback(this);
        setBackgroundColor(Color.WHITE);
        setFocusable(true);
    }

    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);
        cameraX = MathUtils.clamp(cameraX, cameraConstraints.left, cameraConstraints.right);
        cameraY = MathUtils.clamp(cameraY, cameraConstraints.top, cameraConstraints.bottom);
        calcVisibleArea();
        canvas.drawColor(BACKGROUND_COLOR);
        level.draw(canvas);
    }

    public void restart(HashMap<String, Object> serializedLevel) {
        this.level = Level.deserialize(serializedLevel, this);
        if (dim != null) {
            calcMinZoomConstraint();
            calcCameraConstraints();
            changeZoom(1);
        }
    }

    protected void calcVisibleArea(){
        visibleArea = new RectF(-centerX/zoom+cameraX, -centerY/zoom+cameraY, centerX/zoom+cameraX, centerY/zoom+cameraY);
    }

    public float screenX(float wx){
        return (wx - cameraX) * zoom + centerX;
    }
    public float screenY(float wy){
        return (wy - cameraY) * zoom + centerY;
    }
    public float worldX(float sx){
        return (sx - centerX) / zoom + cameraX;
    }
    public float worldY(float sy){
        return (sy - centerY) / zoom + cameraY;
    }

    public RectF toScreenCoors(float left, float top, float right, float bottom){
        return new RectF(screenX(left), screenY(top), screenX(right), screenY(bottom));
    }

    public void zoomIn() {
        changeZoom(zoom*1.1f);
    }

    public void zoomOut() {
        changeZoom(zoom/1.1f);
    }

    public void changeZoom(float zoom){
        this.zoom = Math.max(zoom, minZoom);
        calcCameraConstraints();
    }

    public void calcMinZoomConstraint(){
        minZoom = Math.max(dim.right / level.width, dim.bottom / level.height);
    }

    public void calcCameraConstraints(){
        cameraConstraints.left = centerX / zoom;
        cameraConstraints.right = level.WIDTH * level.TILE_SIZE - centerX / zoom;
        cameraConstraints.top = centerY / zoom;
        cameraConstraints.bottom = level.HEIGHT * level.TILE_SIZE - centerY / zoom;
    }

    @Override
    public void surfaceCreated(@NonNull SurfaceHolder holder) {
        dim = holder.getSurfaceFrame();
        centerX = dim.width()/2f;
        centerY = dim.height()/2f;
        calcMinZoomConstraint();
        calcCameraConstraints();
        changeZoom(1);
    }

    @Override
    public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int width, int height) {
        dim = holder.getSurfaceFrame();
        centerX = dim.width()/2f;
        centerY = dim.height()/2f;
        calcMinZoomConstraint();
        calcCameraConstraints();
    }
}
