package com.tomer.platformaker.game;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;

import androidx.core.math.MathUtils;

import com.tomer.platformaker.GameObject;
import com.tomer.platformaker.R;

public class Player extends GameObject {
    private static final int WIDTH = 60, HEIGHT = 120;

    private static final float MAX_SPEED = 10f, X_ACCEL = 0.8f, X_DECEL = 0.8f;
    private static final float G = 1.4f, DOWN_FACTOR = 0.5f, JUMP_SPEED = 20f, MAX_FALL_SPEED = 40;

    private static final int JUMP_TIME = 10, COYOTE_TIME = 8, JUMP_BUFFER_TIME = 8;
    private int jumpTimer = 0, coyoteTimer = 0, jumpBufferTimer = 0;
    private boolean isJumping, hadJumpedOnPress;

    private static final int DEATH_ANIM_TIME = 30;
    private int deathAnimTimer = 0;
    private final Paint deathAnimPaint;

    private final Bitmap RIGHT_IMAGE, LEFT_IMAGE;
    private boolean facingRight = true;

    private boolean isGrounded;

    private boolean win;

    float xVel, yVel;

    public Player(GameView gameView) {
        super(gameView, 0, 0);
        this.width = WIDTH;
        this.height = HEIGHT;

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inScaled = false;
        this.RIGHT_IMAGE = BitmapFactory.decodeResource(gameView.getResources(), R.drawable.player, options).copy(Bitmap.Config.RGB_565, false);
        Matrix matrix = new Matrix();
        matrix.setScale(-1 , 1);
        this.LEFT_IMAGE = Bitmap.createBitmap(RIGHT_IMAGE, 0, 0, RIGHT_IMAGE.getWidth(), RIGHT_IMAGE.getHeight(), matrix, false);

        deathAnimPaint = new Paint();
        deathAnimPaint.setColor(Color.RED);

        restart();
    }

    @Override
    public void draw(Canvas canvas) {
        if (deathAnimTimer > 0 && (deathAnimTimer+1) % 5 == 0) {
            canvas.drawRect(displayView.toScreenCoors(x, y, x+width, y+height), deathAnimPaint);
            return;
        }
        canvas.drawBitmap(facingRight ? RIGHT_IMAGE : LEFT_IMAGE, null, displayView.toScreenCoors(x, y, x+width, y+height), null);
    }

    @Override
    public void update() {
        if (deathAnimTimer > 0) {
            deathAnimTimer--;
            if (deathAnimTimer == 0 && displayView instanceof GameView) {
                restart();
            }
            return;
        }
        updateVelocity();
        updatePositionAndApplyCollision();
        checkWin();
    }

    public void kill(){
        deathAnimTimer = DEATH_ANIM_TIME;
    }

    public void checkWin() {
        if (isGrounded && displayView.level.hasGoal() && displayView.level.getGoalRect().contains(new RectF(x, y, x+width, y+height-5)) && displayView instanceof GameView && !win) {
            win = true;
            ((GameView) displayView).activity.showWinScreen();
        }
    }

    public void restart(){
        x = displayView.level.getPlayerSpawnX();
        y = displayView.level.getPlayerSpawnY() - HEIGHT + displayView.level.TILE_SIZE;
        xVel = 0;
        yVel = 0;
        facingRight = true;
        deathAnimTimer = 0;
        jumpTimer = 0;
        coyoteTimer = 0;
        jumpBufferTimer = 0;
        isJumping = false;
        hadJumpedOnPress = false;
        win = false;
    }

    private void updatePositionAndApplyCollision(){
        boolean currentIsGrounded = false;
        x += xVel;
        if (CollisionSolver.resolvePlayerTileMapCollisions(this, displayView.level, CollisionSolver.VERTICAL)){
//            xVel = 0;
        }
        y += yVel;
        if (CollisionSolver.resolvePlayerTileMapCollisions(this, displayView.level, CollisionSolver.HORIZONTAL)){
            jumpTimer = 0;
            if (yVel > 0)
                currentIsGrounded = true;
            yVel = 0;
        }
        if (currentIsGrounded){
            coyoteTimer = COYOTE_TIME;
            isGrounded = true;
        } else if (coyoteTimer > 0){
            coyoteTimer--;
            isGrounded = true;
        } else {
            isGrounded = false;
        }

        if (CollisionSolver.checkPlayerSpikeCollisions(this, displayView.level)) {
            kill();
        }
        resolveEdgeCollisions();
    }

    private void updateVelocity(){
        if (GameController.isDown(R.id.button_left) ^ GameController.isDown(R.id.button_right)){
            if (GameController.isDown(R.id.button_left)){
                xVel = xVel - (xVel < 0 ? X_ACCEL : X_DECEL);
                facingRight = false;
            } else {
                xVel = xVel + (xVel > 0 ? X_ACCEL : X_DECEL);
                facingRight = true;
            }
        } else {
            xVel = xVel > 0 ? xVel - X_DECEL : xVel + X_DECEL;
            if (Math.abs(xVel) < 2 * X_DECEL){
                xVel = 0;
            }
        }
        xVel = MathUtils.clamp(xVel, -MAX_SPEED, MAX_SPEED);

        boolean isJumpBtnPressed = GameController.isDown(R.id.button_jump);
        if (!isJumpBtnPressed && isGrounded){
            hadJumpedOnPress = false;
        }

        if (!isJumpBtnPressed && hadJumpedOnPress){
            jumpBufferTimer = JUMP_BUFFER_TIME;
        } else if (isJumpBtnPressed && hadJumpedOnPress && jumpBufferTimer > 0){
            jumpBufferTimer--;
        }

        if (isJumpBtnPressed && isGrounded && !isJumping && (!hadJumpedOnPress || jumpBufferTimer > 0)){
            jumpTimer = JUMP_TIME;
            isJumping = true;
            jumpBufferTimer = 0;
            yVel = -JUMP_SPEED;
            hadJumpedOnPress = true;
        } else if (isJumpBtnPressed && isJumping && jumpTimer > 0){
            jumpTimer--;
            yVel = -JUMP_SPEED;
        } else {
            if (isJumping && yVel < 0){
                yVel = DOWN_FACTOR * yVel;
            }
            isJumping = false;
            yVel += G;
        }
        yVel = Math.min(yVel, MAX_FALL_SPEED);
    }

    private void resolveEdgeCollisions(){
        if (x < 0){
            x = 0;
            xVel = 0;
        } else if (x > displayView.level.width - width){
            x = displayView.level.width - width;
            xVel = 0;
        }

        if (y > displayView.level.height - height){
            y = displayView.level.height - height;
            yVel = 0;
            kill();
        }
    }
}
