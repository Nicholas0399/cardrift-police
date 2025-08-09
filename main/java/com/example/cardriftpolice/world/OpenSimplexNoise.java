package com.example.cardriftpolice.world;

// Минимальная реализация OpenSimplex Noise 2D
public class OpenSimplexNoise {
    private static final double STRETCH_2D = -0.211324865405187;    // (1/Math.sqrt(2+1)-1)/2
    private static final double SQUISH_2D = 0.366025403784439;      // (Math.sqrt(2+1)-1)/2
    private static final double NORM_2D = 47;
    private short[] perm;

    public OpenSimplexNoise(long seed) {
        perm = new short[256];
        short[] source = new short[256];
        for(short i=0;i<256;i++) source[i]=i;
        for(int i=255;i>=0;i--){
            seed = seed * 6364136223846793005L + 1442695040888963407L;
            int r = (int)((seed + 31) % (i+1));
            if (r<0) r += (i+1);
            perm[i] = source[r];
            source[r] = source[i];
        }
    }

    private static double extrapolate2D(short[] perm, int xsb, int ysb, double dx, double dy) {
        int index = perm[(perm[xsb & 0xFF] + ysb) & 0xFF] & 0x0E;
        double value = 0;
        switch (index) {
            case 0:  value = ( dx + dy); break;
            case 2:  value = (-dx + dy); break;
            case 4:  value = ( dx - dy); break;
            case 6:  value = (-dx - dy); break;
            case 8:  value = ( dx + 0);  break;
            case 10: value = (-dx + 0);  break;
            case 12: value = ( 0 + dy);  break;
            case 14: value = ( 0 - dy);  break;
        }
        return value;
    }

    public double eval(double x, double y) {
        double stretchOffset = (x + y) * STRETCH_2D;
        double xs = x + stretchOffset;
        double ys = y + stretchOffset;

        int xsb = fastFloor(xs);
        int ysb = fastFloor(ys);

        double squishOffset = (xsb + ysb) * SQUISH_2D;
        double dx0 = x - (xsb + squishOffset);
        double dy0 = y - (ysb + squishOffset);

        double xins = xs - xsb;
        double yins = ys - ysb;

        double inSum = xins + yins;

        double dx_ext, dy_ext;
        int xsv_ext, ysv_ext;

        double value = 0;

        double dx1 = dx0 - 1 - SQUISH_2D;
        double dy1 = dy0 - 0 - SQUISH_2D;
        double attn1 = 2 - dx1*dx1 - dy1*dy1;
        if (attn1 > 0) {
            attn1 *= attn1;
            value += attn1 * attn1 * extrapolate2D(perm, xsb + 1, ysb, dx1, dy1);
        }

        double dx2 = dx0 - 0 - SQUISH_2D;
        double dy2 = dy0 - 1 - SQUISH_2D;
        double attn2 = 2 - dx2*dx2 - dy2*dy2;
        if (attn2 > 0) {
            attn2 *= attn2;
            value += attn2 * attn2 * extrapolate2D(perm, xsb, ysb + 1, dx2, dy2);
        }

        if (inSum <= 1) {
            double zins = 1 - inSum;
            if (zins > xins || zins > yins) {
                if (xins > yins) {
                    xsv_ext = xsb + 1;
                    ysv_ext = ysb - 1;
                    dx_ext = dx0 - 1;
                    dy_ext = dy0 + 1;
                } else {
                    xsv_ext = xsb - 1;
                    ysv_ext = ysb + 1;
                    dx_ext = dx0 + 1;
                    dy_ext = dy0 - 1;
                }
            } else {
                xsv_ext = xsb + 1;
                ysv_ext = ysb + 1;
                dx_ext = dx0 - 1 - 2 * SQUISH_2D;
                dy_ext = dy0 - 1 - 2 * SQUISH_2D;
            }
        } else {
            double zins = 2 - inSum;
            if (zins < xins || zins < yins) {
                if (xins > yins) {
                    xsv_ext = xsb + 2;
                    ysv_ext = ysb + 0;
                    dx_ext = dx0 - 2 - 2 * SQUISH_2D;
                    dy_ext = dy0 + 0 - 2 * SQUISH_2D;
                } else {
                    xsv_ext = xsb + 0;
                    ysv_ext = ysb + 2;
                    dx_ext = dx0 + 0 - 2 * SQUISH_2D;
                    dy_ext = dy0 - 2 - 2 * SQUISH_2D;
                }
            } else {
                xsv_ext = xsb;
                ysv_ext = ysb;
                dx_ext = dx0;
                dy_ext = dy0;
            }
            xsb += 1;
            ysb += 1;
            double dx3 = dx0 - 1 - 2 * SQUISH_2D;
            double dy3 = dy0 - 1 - 2 * SQUISH_2D;
            double attn3 = 2 - dx3*dx3 - dy3*dy3;
            if (attn3 > 0) {
                attn3 *= attn3;
                value += attn3 * attn3 * extrapolate2D(perm, xsb, ysb, dx3, dy3);
            }
        }

        double attn_ext = 2 - dx_ext*dx_ext - dy_ext*dy_ext;
        if (attn_ext > 0) {
            attn_ext *= attn_ext;
            value += attn_ext * attn_ext * extrapolate2D(perm, xsv_ext, ysv_ext, dx_ext, dy_ext);
        }

        return value / NORM_2D;
    }

    private static int fastFloor(double x) {
        int xi = (int)x;
        return x < xi ? xi - 1 : xi;
    }
}