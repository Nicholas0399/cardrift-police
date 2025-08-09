package com.example.cardriftpolice.entities;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;

import com.example.cardriftpolice.math.Vec2;

public class Car {
    public final Vec2 pos = new Vec2();
    public final Vec2 vel = new Vec2();
    public float angleDeg = 0f;
    public float width = 52, height = 26;
    public float accel = 140f;      // сила разгона (пикс./с^2)
    public float maxSpeed = 160f;   // макс. скорость (пикс./с)
    public float grip = 0.85f;      // сцепление (0..1)
    public float durability = 100f; // “хп”
    public float steering = 0f;     // -1..1
    public float throttle = 0f;     // 0..1
    public boolean handbrake = false;

    // дрифт метрики
    public float driftSlip = 0f;
    public float driftPoints = 0f;
    public float driftMultiplier = 1f;
    private float comboTimer = 0f;

    protected final Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);

    public void update(float dt) {
        // направление носа машины
        float angRad = (float)Math.toRadians(angleDeg);
        float nx = (float)Math.cos(angRad);
        float ny = (float)Math.sin(angRad);

        // Продольная и поперечная составляющие скорости
        float vLong = vel.x * nx + vel.y * ny;
        float vLat  = -vel.x * ny + vel.y * nx;

        // Тяга/тормоз
        float engine = throttle * accel;
        vLong += engine * dt;
        // Ограничение максималки
        float sign = Math.signum(vLong);
        vLong = Math.min(Math.abs(vLong), maxSpeed) * sign;

        // Сцепление: чем меньше grip или на ручнике — тем больше боковое скольжение
        float latFriction = handbrake ? (0.98f - grip) * 0.6f : (0.98f - grip) * 1.6f;
        vLat -= vLat * (latFriction) * dt * 10f;

        // Сопротивление воздуха/качение
        vLong -= vLong * 0.5f * dt;

        // Снова в мировые
        vel.x = vLong * nx - vLat * ny;
        vel.y = vLong * ny + vLat * nx;

        // Поворот
        angleDeg += steering * (handbrake ? 120f : 80f) * dt * (0.5f + Math.min(1f, Math.abs(vLong)/maxSpeed));

        // Обновление позиции
        pos.add(vel.x * dt, vel.y * dt);

        // Дрифт скоринг
        float spd = (float)Math.sqrt(vel.x*vel.x + vel.y*vel.y);
        if (spd > 10f) {
            float vdx = vel.x / spd, vdy = vel.y / spd;
            float dot = vdx * nx + vdy * ny;
            float slip = (float)Math.acos(Math.max(-1, Math.min(1, dot))); // 0..pi
            driftSlip = slip;
            if (slip > 0.25f) {
                float base = (float)(slip * spd * dt * 10);
                driftMultiplier = Math.min(8f, driftMultiplier + dt * 0.5f);
                driftPoints += base * driftMultiplier;
                comboTimer = 2f; // 2 сек на поддержание комбо
            } else {
                comboTimer -= dt;
                if (comboTimer <= 0f) {
                    driftMultiplier = 1f;
                    comboTimer = 0f;
                }
            }
        } else {
            driftSlip = 0f;
            comboTimer -= dt;
            if (comboTimer <= 0f) driftMultiplier = 1f;
        }
    }

    public RectF bounds(){
        return new RectF(pos.x - width/2f, pos.y - height/2f, pos.x + width/2f, pos.y + height/2f);
    }

    public void draw(Canvas c, int color) {
        p.setColor(color);
        float ang = (float)Math.toRadians(angleDeg);
        c.save();
        c.translate(pos.x, pos.y);
        c.rotate(angleDeg);
        c.drawRoundRect(-width/2f, -height/2f, width/2f, height/2f, 6, 6, p);
        // капот
        p.setColor(0x55ffffff);
        c.drawRect(0, -height/2f, width/2f, height/2f, p);
        c.restore();
    }
}