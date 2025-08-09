package com.example.cardriftpolice.core;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;


import com.example.cardriftpolice.entities.Car;
import com.example.cardriftpolice.entities.PlayerCar;
import com.example.cardriftpolice.entities.PoliceCar;
import com.example.cardriftpolice.math.Vec2;
import com.example.cardriftpolice.shop.CarCatalog;
import com.example.cardriftpolice.shop.CarSpec;
import com.example.cardriftpolice.shop.UpgradeState;
import com.example.cardriftpolice.world.Biome;
import com.example.cardriftpolice.world.Chunk;
import com.example.cardriftpolice.world.MapGenerator;
import com.example.cardriftpolice.Prefs;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

public class GameView extends SurfaceView implements SurfaceHolder.Callback {

    private GameThread thread;
    private final Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);

    private final MapGenerator world;
    private final PlayerCar player;
    private final List<PoliceCar> cops = new ArrayList<>();

    private final Random rnd = new Random(1337);
    private float camX = 0, camY = 0;
    private long lastMs = 0;

    // управление
    private int pointerIdLeft = -1, pointerIdRight = -1;
    private float leftX = 0, leftY = 0, rightX = 0, rightY = 0;
    private boolean pressedLeft = false, pressedRight = false;
    private boolean pause = false;
    private boolean gameOver = false;

    // HUD/метрики
    private float wanted = 0f; // 0..100
    private float damage = 0f; // урон игрока
    private long runCoins = 0;
    private float sessionTime = 0f;

    public GameView(Context ctx) {
        super(ctx);
        getHolder().addCallback(this);
        setFocusable(true);

        long seed = System.currentTimeMillis();
        world = new MapGenerator(seed);

        String id = Prefs.getSelectedCarId(ctx);
        CarSpec spec = CarCatalog.byId(id);
        UpgradeState up = Prefs.getUpgrade(ctx, id);
        player = new PlayerCar(spec, up);
        int colorIndex = Prefs.getCarColorIndex(ctx, id);
        playerColor = Prefs.CAR_COLORS[colorIndex];

        // стартовая позиция
        player.pos.set(1000, 1000);
        camX = player.pos.x - 640;
        camY = player.pos.y - 360;
    }

    private int playerColor = Color.WHITE;

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        thread = new GameThread(getHolder());
        thread.running = true;
        thread.start();
        lastMs = System.currentTimeMillis();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        if (thread != null) {
            thread.running = false;
            try { thread.join(); } catch (InterruptedException ignored) {}
        }
    }

    @Override public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {}

    public void onPauseGame() { pause = true; }
    public void onResumeGame() { pause = false; }

    public boolean togglePause() {
        if (gameOver) return false;
        pause = !pause;
        return true;
    }

    private class GameThread extends Thread {
        private final SurfaceHolder sh;
        volatile boolean running = false;

        GameThread(SurfaceHolder sh) { this.sh = sh; }

        @Override
        public void run() {
            while (running) {
                long now = System.currentTimeMillis();
                float dt = Math.min(0.033f, (now - lastMs) / 1000f);
                lastMs = now;

                if (!pause && !gameOver) update(dt);

                Canvas c = sh.lockCanvas();
                if (c != null) {
                    try { drawGame(c); } finally { sh.unlockCanvasAndPost(c); }
                }

                try { Thread.sleep(5); } catch (InterruptedException ignored) {}
            }
        }
    }

    private void update(float dt) {
        sessionTime += dt;

        // управление игроком
        applyTouchControls(dt);

        // физика
        player.update(dt);

        // камера плавно следует
        camX += (player.pos.x - camX - getWidth() * 0.5f) * 0.08f;
        camY += (player.pos.y - camY - getHeight() * 0.5f) * 0.08f;

        // полиция
        updateWanted(dt);
        spawnPoliceIfNeeded();
        for (PoliceCar pc : cops) {
            pc.updateAI(dt, player.pos.x, player.pos.y, player.vel.x, player.vel.y);
            pc.update(dt);
        }

        // столкновения с препятствиями
        collideWithWorld(player);
        for (PoliceCar pc : cops) collideWithWorld(pc);

        // столкновения с полицией
        Iterator<PoliceCar> it = cops.iterator();
        while (it.hasNext()) {
            PoliceCar pc = it.next();
            collideCars(player, pc);
            if (pc.durability <= 0f) it.remove();
        }

        // урон и гейм-овер
        if (damage >= player.durability) {
            gameOver = true;
            // монеты за катку: дрифт-очки / 5 + бонус
            runCoins = Math.round(player.driftPoints / 5f + wanted);
            Prefs.addCoins(getContext(), runCoins);
        }
    }

    private void updateWanted(float dt) {
        // растём когда активно дрифтуем, падаем со временем
        if (player.driftSlip > 0.25f) wanted += dt * Math.min(3f, player.driftMultiplier);
        else wanted -= dt * 1f;
        wanted = Math.max(0f, Math.min(100f, wanted));
    }

    private void spawnPoliceIfNeeded() {
        int stars = (int)(wanted / 20f); // 0..5
        int wantCount = Math.min(1 + stars, 6);
        while (cops.size() < wantCount) {
            PoliceCar pc = new PoliceCar();
            // заспавнить у края экрана
            float spawnDist = 700 + rnd.nextInt(400);
            double a = rnd.nextFloat() * Math.PI * 2;
            pc.pos.set(player.pos.x + (float)Math.cos(a)*spawnDist, player.pos.y + (float)Math.sin(a)*spawnDist);
            pc.angleDeg = (float)Math.toDegrees(a + Math.PI);
            cops.add(pc);
        }
    }

    private void collideWithWorld(Car c) {
        // проверяем чанки вокруг
        int minCX = (int)Math.floor((c.pos.x - 200) / (Chunk.W * Chunk.TILE));
        int maxCX = (int)Math.floor((c.pos.x + 200) / (Chunk.W * Chunk.TILE));
        int minCY = (int)Math.floor((c.pos.y - 200) / (Chunk.H * Chunk.TILE));
        int maxCY = (int)Math.floor((c.pos.y + 200) / (Chunk.H * Chunk.TILE));

        RectF b = c.bounds();
        for (int cx=minCX; cx<=maxCX; cx++){
            for (int cy=minCY; cy<=maxCY; cy++){
                Chunk ch = world.getChunk(cx, cy);
                for (RectF r : ch.obstacles) {
                    if (RectF.intersects(b, r)) {
                        // простое отталкивание
                        float overlapL = b.right - r.left;
                        float overlapR = r.right - b.left;
                        float overlapT = b.bottom - r.top;
                        float overlapB = r.bottom - b.top;
                        float minOverlap = Math.min(Math.min(overlapL, overlapR), Math.min(overlapT, overlapB));

                        if (minOverlap == overlapL) c.pos.x -= overlapL;
                        else if (minOverlap == overlapR) c.pos.x += overlapR;
                        else if (minOverlap == overlapT) c.pos.y -= overlapT;
                        else c.pos.y += overlapB;

                        // гашение скорости и урон
                        c.vel.scl(0.5f);
                        if (c == player) damage += 6f; else c.durability -= 8f;
                        b = c.bounds();
                    }
                }
            }
        }
    }

    private void collideCars(Car a, Car b) {
        RectF A = a.bounds(); RectF B = b.bounds();
        if (RectF.intersects(A, B)) {
            // обмен импульсами
            float dx = (a.pos.x - b.pos.x);
            float dy = (a.pos.y - b.pos.y);
            float d2 = dx*dx + dy*dy + 1e-3f;
            float inv = 1f / (float)Math.sqrt(d2);
            float nx = dx * inv, ny = dy * inv;

            float sep = 10f;
            a.pos.add(nx*sep, ny*sep);
            b.pos.add(-nx*sep, -ny*sep);

            float push = 120f;
            a.vel.add(nx*push, ny*push);
            b.vel.add(-nx*push, -ny*push);

            damage += 5f; // игроку больнее
            b.durability -= 4f;
            wanted = Math.min(100f, wanted + 2f);
        }
    }

    private void drawGame(Canvas c) {
        c.drawColor(0xff0a0a0a);

        // Мир
        drawWorld(c);

        // Полиция
        for (PoliceCar pc : cops) pc.draw(c, 0xff3f51b5);

        // Игрок
        player.draw(c, playerColor);

        // HUD
        drawHUD(c);

        // Пауза/гейм-овер
        if (pause) drawPause(c);
        if (gameOver) drawGameOver(c);
    }

    private void drawWorld(Canvas c) {
        int w = getWidth(), h = getHeight();
        int minCX = (int)Math.floor((camX) / (Chunk.W * Chunk.TILE));
        int maxCX = (int)Math.floor((camX + w) / (Chunk.W * Chunk.TILE));
        int minCY = (int)Math.floor((camY) / (Chunk.H * Chunk.TILE));
        int maxCY = (int)Math.floor((camY + h) / (Chunk.H * Chunk.TILE));

        for (int cx=minCX-1; cx<=maxCX+1; cx++){
            for (int cy=minCY-1; cy<=maxCY+1; cy++){
                Chunk ch = world.getChunk(cx, cy);
                ch.draw(c, camX, camY);
            }
        }
    }

    private void drawHUD(Canvas c) {
        p.setColor(Color.WHITE);
        p.setTextSize(28f);

        float spd = (float)Math.sqrt(player.vel.x*player.vel.x + player.vel.y*player.vel.y);

        c.drawText(String.format("Скорость: %.0f", spd), 20, 40, p);
        c.drawText(String.format("Дрифт: %.0f  x%.1f", player.driftPoints, player.driftMultiplier), 20, 80, p);
        c.drawText(String.format("Розыск: %d★", (int)(wanted/20f)), 20, 120, p);
        c.drawText(String.format("Урон: %.0f/%.0f", damage, player.durability), 20, 160, p);

        // Кнопка паузы
        p.setColor(0x66ffffff);
        c.drawRect(getWidth()-90, 20, getWidth()-20, 80, p);
        p.setColor(Color.WHITE);
        c.drawRect(getWidth()-78, 28, getWidth()-66, 72, p);
        c.drawRect(getWidth()-48, 28, getWidth()-36, 72, p);

        // Контролы
        drawControls(c);
    }

    private void drawControls(Canvas c) {
        // левый стик — рулёжка
        float cx = 140, cy = getHeight()-140, r = 100;
        p.setColor(0x33ffffff);
        c.drawCircle(cx, cy, r, p);
        p.setColor(0x88ffffff);
        float sx = pressedLeft ? leftX : cx, sy = pressedLeft ? leftY : cy;
        c.drawCircle(sx, sy, 28, p);

        // правый — газ/ручник
        float rx = getWidth()-140, ry = getHeight()-140;
        p.setColor(0x33ffffff);
        c.drawCircle(rx, ry, r, p);
        p.setColor(pressedRight ? 0x88ff5555 : 0x88ffffff);
        float tx = pressedRight ? rightX : rx, ty = pressedRight ? rightY : ry;
        c.drawCircle(tx, ty, 28, p);

        // подписи
        p.setColor(0x99ffffff);
        p.setTextSize(20f);
        c.drawText("Руль", cx-24, cy + r + 24, p);
        c.drawText("Газ/Ручник", rx-48, ry + r + 24, p);
    }

    private void drawPause(Canvas c) {
        p.setColor(0x99000000);
        c.drawRect(0,0,getWidth(),getHeight(),p);
        p.setColor(Color.WHITE);
        p.setTextSize(42f);
        c.drawText("Пауза", getWidth()/2f - 60, getHeight()/2f - 40, p);
        p.setTextSize(28f);
        c.drawText("Нажми кнопку Паузы снова, чтобы продолжить", getWidth()/2f - 360, getHeight()/2f + 10, p);
    }

    private void drawGameOver(Canvas c) {
        p.setColor(0xCC000000);
        c.drawRect(0,0,getWidth(),getHeight(),p);
        p.setColor(Color.WHITE);
        p.setTextSize(46f);
        c.drawText("Конец заезда", getWidth()/2f - 140, getHeight()/2f - 80, p);
        p.setTextSize(28f);
        c.drawText(String.format("Дрифт-очки: %.0f", player.driftPoints), getWidth()/2f - 120, getHeight()/2f - 30, p);
        c.drawText(String.format("Монеты: +%d", runCoins), getWidth()/2f - 120, getHeight()/2f + 10, p);
        c.drawText("Нажми Назад для выхода в меню", getWidth()/2f - 200, getHeight()/2f + 60, p);
    }

    private void applyTouchControls(float dt) {
        // steering: от -1 до 1 в зависимости от отклонения стика от центра
        float cx = 140, cy = getHeight()-140, r = 100;
        if (pressedLeft) {
            float dx = (leftX - cx) / r;
            dx = Math.max(-1f, Math.min(1f, dx));
            player.steering = dx;
        } else {
            player.steering = 0f;
        }

        float rx = getWidth()-140, ry = getHeight()-140;
        if (pressedRight) {
            // газ по вертикали: выше — больше газ
            float dy = (ry - rightY) / 100f; // нормализация
            dy = Math.max(0f, Math.min(1f, dy));
            player.throttle = dy;
            // если сильно вбок — ручник
            player.handbrake = Math.abs(rightX - rx) > 35;
        } else {
            player.throttle = 0f;
            player.handbrake = false;
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        int action = e.getActionMasked();
        int idx = e.getActionIndex();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_POINTER_DOWN: {
                int id = e.getPointerId(idx);
                float x = e.getX(idx), y = e.getY(idx);
                if (x < getWidth()/2f && pointerIdLeft==-1) {
                    pointerIdLeft = id; pressedLeft = true; leftX = x; leftY = y;
                } else if (pointerIdRight==-1) {
                    pointerIdRight = id; pressedRight = true; rightX = x; rightY = y;
                    // если тыкнули по кнопке паузы — ставим на паузу
                    if (x > getWidth()-90 && y < 80) pause = !pause;
                }
                break;
            }
            case MotionEvent.ACTION_MOVE: {
                for (int i=0;i<e.getPointerCount();i++){
                    int id = e.getPointerId(i);
                    float x = e.getX(i), y = e.getY(i);
                    if (id == pointerIdLeft) { leftX = x; leftY = y; }
                    if (id == pointerIdRight) { rightX = x; rightY = y; }
                }
                break;
            }
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_POINTER_UP:
            case MotionEvent.ACTION_CANCEL: {
                int id = e.getPointerId(idx);
                if (id == pointerIdLeft) { pointerIdLeft=-1; pressedLeft=false; }
                if (id == pointerIdRight) { pointerIdRight=-1; pressedRight=false; }
                break;
            }
        }
        return true;
    }
}