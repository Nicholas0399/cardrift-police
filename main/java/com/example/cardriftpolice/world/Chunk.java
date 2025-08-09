package com.example.cardriftpolice.world;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;

import java.util.ArrayList;
import java.util.Random;

public class Chunk {
    public static final int TILE = 64; // px
    public static final int W = 16; // tiles
    public static final int H = 16;

    public final int cx, cy;
    public final Biome[][] biome = new Biome[W][H];
    public final ArrayList<RectF> obstacles = new ArrayList<>();
    private final Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);

    public Chunk(int cx, int cy) {
        this.cx = cx; this.cy = cy;
    }

    public void draw(Canvas c, float ox, float oy) {
        // ox, oy — мировые координаты верхнего-левого угла видимой области
        float startX = cx * W * TILE;
        float startY = cy * H * TILE;

        for (int x=0;x<W;x++){
            for (int y=0;y<H;y++){
                p.setColor(colorFor(biome[x][y]));
                float left = startX + x*TILE - ox;
                float top  = startY + y*TILE - oy;
                c.drawRect(left, top, left+TILE, top+TILE, p);
                // Простые “дороги” в городе
                if (biome[x][y]==Biome.CITY && (x%4==0 || y%4==0)) {
                    p.setColor(0xff606060);
                    c.drawRect(left, top, left+TILE, top+TILE, p);
                }
            }
        }
        p.setColor(0xff333333);
        for (RectF r : obstacles) {
            c.drawRect(r.left - ox, r.top - oy, r.right - ox, r.bottom - oy, p);
        }
    }

    private int colorFor(Biome b) {
        switch (b) {
            case CITY: return 0xff2b2b2b;
            case DESERT: return 0xffc8b068;
            case FOREST: return 0xff2e6b2f;
            case BEACH: return 0xffd9c49e;
            case VILLAGE: return 0xff7b6436;
        }
        return 0xff000000;
    }
}