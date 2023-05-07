package com.tomer.platformaker.game;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;

import com.tomer.platformaker.R;

import java.util.HashMap;

public class GameActivity extends AppCompatActivity {

    private GameView gameView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // making it full screen
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_game);
        getSupportActionBar().hide();
        gameView = findViewById(R.id.view_game);
        new GameController(
                findViewById(R.id.button_jump),
                findViewById(R.id.button_left),
                findViewById(R.id.button_right)
        );
        gameView.activity = this;
    }

    @Override
    protected void onResume() {
        super.onResume();
        Intent intent = getIntent();
        gameView.restart((HashMap<String, Object>) intent.getSerializableExtra("level"));
    }

    public void showPauseScreen(View view) {
        gameView.thread.pause = true;
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View menu = getLayoutInflater().inflate(R.layout.menu_pause, null);
        builder.setView(menu);
        AlertDialog dialog = builder.show();
        dialog.getWindow().setBackgroundDrawableResource(R.color.transparant_black);
//        RatingBar ratingBar = menu.findViewById(R.id.rating_pause);
//        ratingBar.setRating(gameView.level.userRating/2f);
//        ratingBar.setOnRatingBarChangeListener((ratingBar1, rating, fromUser) -> {
//            gameView.level.userRating = (int) (rating * 2);
//        });
        menu.findViewById(R.id.button_pause_home).setOnClickListener(v -> {
            dialog.dismiss();
            gameView.stopThread();
            finish();
        });
        menu.findViewById(R.id.button_pause_continue).setOnClickListener(v -> dialog.dismiss());
        menu.findViewById(R.id.button_pause_restart).setOnClickListener(v -> {
            gameView.player.restart();
            dialog.dismiss();
        });
        menu.findViewById(R.id.button_pause_zoom_in).setOnClickListener(v -> {
            gameView.zoomIn();
            gameView.invalidate();
        });
        menu.findViewById(R.id.button_pause_zoom_out).setOnClickListener(v -> {
            gameView.zoomOut();
            gameView.invalidate();
        });
        dialog.setOnDismissListener(v -> gameView.thread.pause = false);
    }

    public synchronized void showWinScreen() {
        runOnUiThread(() -> {
            gameView.thread.pause = true;
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            View menu = getLayoutInflater().inflate(R.layout.menu_win, null);
            builder.setView(menu);
            AlertDialog dialog = builder.show();
            dialog.getWindow().setBackgroundDrawableResource(R.color.transparant_black);
//            RatingBar ratingBar = menu.findViewById(R.id.rating_win);
//            ratingBar.setRating(gameView.level.userRating/2f);
//            ratingBar.setOnRatingBarChangeListener((ratingBar1, rating, fromUser) -> {
//                gameView.level.userRating = (int) (rating * 2);
//            });
            menu.findViewById(R.id.button_win_home).setOnClickListener(v -> {
                dialog.dismiss();
                gameView.stopThread();
                finish();
            });
            menu.findViewById(R.id.button_win_continue).setOnClickListener(v -> dialog.dismiss());
            menu.findViewById(R.id.button_win_replay).setOnClickListener(v -> {
                gameView.player.restart();
                dialog.dismiss();
            });
            dialog.setOnDismissListener(v -> gameView.thread.pause = false);
        });
    }
}