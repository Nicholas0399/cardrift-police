package com.example.cardriftpolice.entities;

import android.graphics.Canvas;
import android.graphics.Paint;

public class PoliceCar extends Car {
    public float aiTimer = 0f;
    public float targetX, targetY;

    private final Paint hpPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    public PoliceCar() {
        maxSpeed = 150f;
        accel = 140f;
        grip = 0.9f;
        durability = 120f;
        width = 52; height = 26;
    }

    public void updateAI(float dt, float px, float py, float pvx, float pvy) {
        aiTimer += dt;
        // Простое преследование с упреждением
        float dx = px - pos.x + pvx * 0.6f;
        float dy = py - pos.y + pvy * 0.6f;
        float dist = (float)Math.sqrt(dx*dx + dy*dy) + 1e-5f;
        targetX = pos.x + dx;
        targetY = pos.y + dy;

        // Нормализуем вектор к цели как направление носа
        float desiredAngle = (float)Math.toDegrees(Math.atan2(dy, dx));
        float da = normalizeAngle(desiredAngle - angleDeg);
        steering = clamp(da / 60f, -1f, 1f);
        throttle = 1f;
        handbrake = Math.abs(da) > 80f; // сбросить скорость для разворота

        // Иногда выполняем таран — кратковременно урезаем сцепление
        if ((int)(aiTimer*2) % 5 == 0) {
            handbrake = true;
        }
    }

    private static float normalizeAngle(float a) {
        while (a > 180f) a -= 360f;
        while (a < -180f) a += 360f;
        return a;
    }
    private static float clamp(float v, float a, float b){ return Math.max(a, Math.min(b, v)); }

    @Override
    public void draw(Canvas c, int color) {
        super.draw(c, 0xff3f51b5);
        // мигающая “люстра”
        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        p.setColor(0x88ff0000);
        c.drawCircle(pos.x-12, pos.y-10, 4, p);
        p.setColor(0x8800aaff);
        c.drawCircle(pos.x+12, pos.y-10, 4, p);
    }
}