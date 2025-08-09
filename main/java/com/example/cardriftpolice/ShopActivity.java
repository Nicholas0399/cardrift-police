package com.example.cardriftpolice;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.cardriftpolice.shop.CarCatalog;
import com.example.cardriftpolice.shop.CarSpec;
import com.example.cardriftpolice.shop.UpgradeState;

public class ShopActivity extends Activity {

    LinearLayout list;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shop);
        list = findViewById(R.id.list);
        render();
    }

    private void render() {
        list.removeAllViews();

        long coins = Prefs.getCoins(this);

        TextView coinsView = new TextView(this);
        coinsView.setText("Монеты: " + coins);
        coinsView.setTextColor(Color.WHITE);
        coinsView.setTextSize(18f);
        coinsView.setPadding(8, 8, 8, 16);
        list.addView(coinsView);

        for (CarSpec spec : CarCatalog.CAR_LIST) {
            LinearLayout card = new LinearLayout(this);
            card.setOrientation(LinearLayout.VERTICAL);
            card.setPadding(16,16,16,16);
            card.setBackgroundColor(Color.parseColor("#1b1b1b"));
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            lp.bottomMargin = 16;
            card.setLayoutParams(lp);

            TextView title = new TextView(this);
            title.setText(spec.title + "  (" + spec.price + " мон.)");
            title.setTextColor(Color.WHITE);
            title.setTextSize(18f);
            card.addView(title);

            TextView stats = new TextView(this);
            stats.setText(String.format("Макс.скорость: %.0f | Ускорение: %.1f | Сцепление: %.2f | Прочность: %.0f",
                    spec.baseMaxSpeed, spec.baseAccel, spec.baseGrip, spec.baseDurability));
            stats.setTextColor(Color.LTGRAY);
            stats.setTextSize(14f);
            stats.setPadding(0, 6, 0, 6);
            card.addView(stats);

            LinearLayout btnRow = new LinearLayout(this);
            btnRow.setOrientation(LinearLayout.HORIZONTAL);
            btnRow.setGravity(Gravity.START);

            boolean unlocked = Prefs.isCarUnlocked(this, spec.id);
            String selected = Prefs.getSelectedCarId(this);
            boolean isSelected = unlocked && spec.id.equals(selected);

            Button action = new Button(this);
            if (!unlocked) {
                action.setText("Купить");
                action.setOnClickListener(v -> {
                    long c = Prefs.getCoins(this);
                    if (c >= spec.price) {
                        Prefs.addCoins(this, -spec.price);
                        Prefs.unlockCar(this, spec.id);
                        Prefs.setSelectedCarId(this, spec.id);
                        render();
                    } else {
                        action.setText("Нужно " + (spec.price - c));
                    }
                });
            } else if (!isSelected) {
                action.setText("Выбрать");
                action.setOnClickListener(v -> {
                    Prefs.setSelectedCarId(this, spec.id);
                    render();
                });
            } else {
                action.setText("Выбрано");
                action.setEnabled(false);
            }
            btnRow.addView(action);

            if (unlocked) {
                // Кнопки апгрейдов
                Button upEngine = new Button(this);
                Button upGrip = new Button(this);
                Button upTires = new Button(this);

                upEngine.setText("Двигатель +");
                upGrip.setText("Сцепление +");
                upTires.setText("Шины +");

                upEngine.setOnClickListener(v -> {
                    UpgradeState u = Prefs.getUpgrade(this, spec.id);
                    int price = 200 + u.engineLvl * 200;
                    long c = Prefs.getCoins(this);
                    if (c >= price) {
                        Prefs.addCoins(this, -price);
                        u.engineLvl++;
                        Prefs.setUpgrade(this, spec.id, u);
                        render();
                    } else upEngine.setText("Нужно " + (price - c));
                });

                upGrip.setOnClickListener(v -> {
                    UpgradeState u = Prefs.getUpgrade(this, spec.id);
                    int price = 150 + u.gripLvl * 200;
                    long c = Prefs.getCoins(this);
                    if (c >= price) {
                        Prefs.addCoins(this, -price);
                        u.gripLvl++;
                        Prefs.setUpgrade(this, spec.id, u);
                        render();
                    } else upGrip.setText("Нужно " + (price - c));
                });

                upTires.setOnClickListener(v -> {
                    UpgradeState u = Prefs.getUpgrade(this, spec.id);
                    int price = 120 + u.tiresLvl * 160;
                    long c = Prefs.getCoins(this);
                    if (c >= price) {
                        Prefs.addCoins(this, -price);
                        u.tiresLvl++;
                        Prefs.setUpgrade(this, spec.id, u);
                        render();
                    } else upTires.setText("Нужно " + (price - c));
                });

                btnRow.addView(upEngine);
                btnRow.addView(upGrip);
                btnRow.addView(upTires);

                // Цвета
                LinearLayout colors = new LinearLayout(this);
                colors.setOrientation(LinearLayout.HORIZONTAL);
                colors.setPadding(0,8,0,0);
                for (int i = 0; i < Prefs.CAR_COLORS.length; i++) {
                    final int colorIndex = i;
                    View swatch = new View(this);
                    LinearLayout.LayoutParams cp = new LinearLayout.LayoutParams(80, 40);
                    cp.rightMargin = 8;
                    swatch.setLayoutParams(cp);
                    swatch.setBackgroundColor(Prefs.CAR_COLORS[i]);
                    swatch.setOnClickListener(v -> {
                        Prefs.setCarColorIndex(this, spec.id, colorIndex);
                        render();
                    });
                    colors.addView(swatch);
                }
                card.addView(colors);
            }

            card.addView(btnRow);
            list.addView(card);
        }

        Button back = new Button(this);
        back.setText("Назад");
        back.setOnClickListener(v -> finish());
        list.addView(back);
    }
}