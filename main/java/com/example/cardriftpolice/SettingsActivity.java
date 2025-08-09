package com.example.cardriftpolice;

import android.app.Activity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;

public class SettingsActivity extends Activity {

    RadioGroup rgCtrl, rgQuality;
    RadioButton rbTouch, rbTilt, rbLow, rbMedium, rbHigh;
    Button btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        rgCtrl = findViewById(R.id.rgControl);
        rgQuality = findViewById(R.id.rgQuality);
        rbTouch = findViewById(R.id.rbTouch);
        rbTilt = findViewById(R.id.rbTilt);
        rbLow = findViewById(R.id.rbLow);
        rbMedium = findViewById(R.id.rbMedium);
        rbHigh = findViewById(R.id.rbHigh);
        btnBack = findViewById(R.id.btnBack);

        // load
        int control = Prefs.getControlMode(this);
        if (control == Prefs.CTRL_TOUCH) rbTouch.setChecked(true);
        else rbTilt.setChecked(true);

        int q = Prefs.getQuality(this);
        if (q == Prefs.Q_LOW) rbLow.setChecked(true);
        else if (q == Prefs.Q_HIGH) rbHigh.setChecked(true);
        else rbMedium.setChecked(true);

        rgCtrl.setOnCheckedChangeListener((g, id) -> {
            if (id == R.id.rbTouch) Prefs.setControlMode(this, Prefs.CTRL_TOUCH);
            else Prefs.setControlMode(this, Prefs.CTRL_TILT);
        });

        rgQuality.setOnCheckedChangeListener((g, id) -> {
            if (id == R.id.rbLow) Prefs.setQuality(this, Prefs.Q_LOW);
            else if (id == R.id.rbHigh) Prefs.setQuality(this, Prefs.Q_HIGH);
            else Prefs.setQuality(this, Prefs.Q_MED);
        });

        btnBack.setOnClickListener(v -> finish());
    }
}