package com.example.cardriftpolice;

import com.example.cardriftpolice.core.GameView;
import android.app.Activity;
import android.os.Bundle;

public class GameActivity extends Activity {

    private GameView gameView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        gameView = new GameView(this);
        setContentView(gameView);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (gameView != null) gameView.onPauseGame();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (gameView != null) gameView.onResumeGame();
    }

    @Override
    public void onBackPressed() {
        if (gameView != null && gameView.togglePause()) return;
        super.onBackPressed();
    }
}