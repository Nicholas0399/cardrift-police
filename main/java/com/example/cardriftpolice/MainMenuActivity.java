package com.example.cardriftpolice;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainMenuActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);

        Button btnPlay = findViewById(R.id.btnPlay);
        Button btnShop = findViewById(R.id.btnShop);
        Button btnSettings = findViewById(R.id.btnSettings);
        Button btnExit = findViewById(R.id.btnExit);

        btnPlay.setOnClickListener(v -> startActivity(new Intent(this, GameActivity.class)));
        btnShop.setOnClickListener(v -> startActivity(new Intent(this, ShopActivity.class)));
        btnSettings.setOnClickListener(v -> startActivity(new Intent(this, SettingsActivity.class)));
        btnExit.setOnClickListener(v -> finishAffinity());

        // Инициализация префсов по умолчанию
        Prefs.initDefaults(this);
    }
}