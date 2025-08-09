package com.example.cardriftpolice.world;

import android.graphics.RectF;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class MapGenerator {
    private final OpenSimplexNoise noise;
    private final long seed;
    private final Map<Long, Chunk> cache = new HashMap<>();
    private final Random rnd;

    public MapGenerator(long seed) {
        this.seed = seed;
        this.noise = new OpenSimplexNoise(seed);
        this.rnd = new Random(seed);
    }

    private static long key(int cx, int cy) {
        return (((long)cx) << 32) ^ (cy & 0xffffffffL);
    }

    public Chunk getChunk(int cx, int cy) {
        long k = key(cx, cy);
        Chunk ch = cache.get(k);
        if (ch != null) return ch;
        ch = new Chunk(cx, cy);
        genChunk(ch);
        cache.put(k, ch);
        return ch;
    }

    private void genChunk(Chunk ch) {
        float baseX = ch.cx * Chunk.W;
        float baseY = ch.cy * Chunk.H;
        for (int x=0;x<Chunk.W;x++){
            for (int y=0;y<Chunk.H;y++){
                double nx = (baseX + x) * 0.05;
                double ny = (baseY + y) * 0.05;
                double v = noise.eval(nx, ny);
                if (v < -0.3) ch.biome[x][y] = Biome.BEACH;
                else if (v < -0.05) ch.biome[x][y] = Biome.DESERT;
                else if (v < 0.25) ch.biome[x][y] = Biome.VILLAGE;
                else if (v < 0.55) ch.biome[x][y] = Biome.FOREST;
                else ch.biome[x][y] = Biome.CITY;
            }
        }
        // Препятствия по биому (фиксированно по seed+coords)
        Random r = new Random(seed ^ (ch.cx*73856093L) ^ (ch.cy*19349663L));
        int obstacles = 10 + r.nextInt(10);
        for (int i=0;i<obstacles;i++){
            int tx = r.nextInt(Chunk.W);
            int ty = r.nextInt(Chunk.H);
            Biome b = ch.biome[tx][ty];
            if (b == Biome.BEACH) continue;
            float x = (ch.cx*Chunk.W + tx) * Chunk.TILE;
            float y = (ch.cy*Chunk.H + ty) * Chunk.TILE;
            float w = 30 + r.nextInt(30);
            float h = 30 + r.nextInt(30);
            ch.obstacles.add(new RectF(x+10, y+10, x+10+w, y+10+h));
        }
    }
}