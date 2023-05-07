package com.tomer.platformaker.editor;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.math.MathUtils;

import com.tomer.platformaker.DisplayView;
import com.tomer.platformaker.Level;
import com.tomer.platformaker.R;
import com.tomer.platformaker.Tile;

import java.util.HashMap;

public class EditorView extends DisplayView implements View.OnTouchListener {
    private Paint gridPaint;

    private float lastX, lastY;
    private float lastCameraX, lastCameraY;

    private static final int PAN = 0, DRAW = 1, ERASE = 2, EYEDROPPER = 3;
    private int mode;
    private int paint;
    private static final Tile[] PAINTS = Tile.values();

    private static ImageButton[] BUTTONS;
    private ImageView TILE_IMAGE;

    private final Drawable SELECTED, DEFAULT;

    private int lastTx, lastTy;

    public EditorView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setOnTouchListener(this);

        gridPaint  = new Paint();
        gridPaint.setColor(Color.BLACK);
        gridPaint.setStyle(Paint.Style.STROKE);

        paint = Tile.GREEN_GRASS.ordinal();

        SELECTED = AppCompatResources.getDrawable(context.getApplicationContext(), R.drawable.selected_background);
        DEFAULT = AppCompatResources.getDrawable(context.getApplicationContext(), R.drawable.default_background);

        cameraX = 0;
        cameraY = 0;
    }

    public void init(ImageView tileImage, ImageButton... buttons){
        BUTTONS = buttons;

        for (ImageButton button: BUTTONS) {
            button.setOnTouchListener(this);
            button.setBackground(DEFAULT);
        }

        TILE_IMAGE = tileImage;
        setTileImage();
        setMode(DRAW);
    }

    @Override
    public void restart(HashMap<String, Object> serializedLevel) {
        if (serializedLevel == null) {
            if (level == null) {
                level = new Level(this, 30, 13);
                if (dim != null) {
                    calcMinZoomConstraint();
                    calcCameraConstraints();
                    changeZoom(1);
                }
            } else {
                level = new Level(this, level.WIDTH, level.HEIGHT);
            }
        } else {
            super.restart(serializedLevel);
        }
        updateView();
    }

    @Override
    public void draw(Canvas canvas) {
        if (canvas == null || dim == null){
            return;
        }
        super.draw(canvas);
        float tileSize = level.TILE_SIZE * zoom;
        float xOffset = screenX(0) % tileSize, yOffset = screenY(0) % tileSize;
        int verLines = (int) Math.ceil(dim.width()/tileSize) + 1, horLines = (int) Math.ceil(dim.height()/tileSize) + 1;
        for (int i = 0; i < horLines; i++) {
            canvas.drawLine(0, i * tileSize + yOffset, dim.width(), i * tileSize + yOffset, gridPaint);
        }
        for (int j = 0; j < verLines; j++) {
            canvas.drawLine(j * tileSize + xOffset,0,  j * tileSize + xOffset, dim.height(), gridPaint);
        }
    }

    private void setTileImage(){
        TILE_IMAGE.setImageDrawable(Tile.ICONS[paint]);
    }

    private void setMode(int mode){
        BUTTONS[this.mode].setBackground(DEFAULT);
        this.mode = mode;
        BUTTONS[this.mode].setBackground(SELECTED);
    }

    public void scrollTile(boolean up) {
        if (up) {
            paint++;
            if (paint > PAINTS.length-1){
                paint = 1;
            }
        } else {
            paint--;
            if (paint < 1){
                paint = PAINTS.length-1;
            }
        }
        setTileImage();
        setMode(DRAW);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (v == this) {
            float x = event.getX(), y = event.getY();
            switch (mode){
                case ERASE:
                case DRAW:
                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
                        lastTx = -1; lastTy = -1;
                    }
                    Tile tile = mode == DRAW ? PAINTS[paint] : Tile.AIR;
                    int tx = (int) (worldX(x)/ level.TILE_SIZE), ty = (int) (worldY(y) / level.TILE_SIZE);
                    if (lastTx == tx && lastTy == ty) {
                        break;
                    }
                    if (tile == Tile.PLAYER_SPAWN){
                        if (level.setSpawn(tx, ty)){
                            updateView();
                        }
                    } else if (tile == Tile.PLAYER_GOAL){
                        if (level.setGoal(tx, ty)){
                            updateView();
                        }
                    } else if (level.setTile(tx, ty, tile, true)) {
                        updateView();
                    }
                    lastTx = tx;
                    lastTy = ty;
                    break;
                case PAN:
                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
                        lastX = x;
                        lastY = y;
                        lastCameraX = cameraX;
                        lastCameraY = cameraY;
                    } else if (event.getAction() == MotionEvent.ACTION_MOVE) {
                        cameraX = lastCameraX + worldX(lastX) - worldX(x);
                        cameraY = lastCameraY + worldY(lastY) - worldY(y);
                        if (cameraX < cameraConstraints.left || cameraX > cameraConstraints.right) {
                            lastCameraX = cameraX < cameraConstraints.left ? cameraConstraints.left : cameraConstraints.right;
                            lastX = x;
                        }
                        if (cameraY < cameraConstraints.top || cameraY > cameraConstraints.bottom) {
                            lastCameraY = cameraY < cameraConstraints.top ? cameraConstraints.top : cameraConstraints.bottom;
                            lastY = y;
                        }
                        updateView();
                    }
                    break;
                case EYEDROPPER:
                    int newPaint = level.tiles[MathUtils.clamp((int) (worldY(y) / level.TILE_SIZE), 0, level.HEIGHT -1)]
                            [MathUtils.clamp((int) (worldX(x)/ level.TILE_SIZE), 0, level.WIDTH -1)].ordinal();
                    if (newPaint > 0) {
                        paint = newPaint;
                        setMode(DRAW);
                        setTileImage();
                    } else {
                        setMode(ERASE);
                    }
                    break;
            }
            return true;
        } else if (event.getAction() == MotionEvent.ACTION_DOWN) {
            if (v.getId() == R.id.button_pan){
                setMode(PAN);
            }  else if (v.getId() == R.id.button_draw){
                setMode(DRAW);
            } else if (v.getId() == R.id.button_erase){
                setMode(ERASE);
            } else if (v.getId() == R.id.button_eyedropper){
                setMode(EYEDROPPER);
            }
        }
        return false;
    }

    public void updateView() {
        Canvas canvas = this.holder.lockCanvas();
        synchronized (holder) {
            draw(canvas);
        }
        if(canvas!=null) {
            try {
                holder.unlockCanvasAndPost(canvas);
                postInvalidate();
            }
            catch(Exception e){e.printStackTrace();}
        }
    }

    @Override
    public void surfaceDestroyed(@NonNull SurfaceHolder holder) {}
}
