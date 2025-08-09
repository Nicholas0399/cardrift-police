package com.example.cardriftpolice;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.cardriftpolice.shop.UpgradeState;

public class Prefs {
    private static final String P = "prefs_cardrift";
    private static final String K_COINS = "coins";
    private static final String K_SELECTED_CAR = "sel_car";
    private static final String K_UNLOCK_PREFIX = "u_";
    private static final String K_UPGRADE_PREFIX = "up_";
    private static final String K_COLOR_PREFIX = "col_";
    private static final String K_CONTROL = "control";
    private static final String K_QUALITY = "quality";

    public static final int CTRL_TOUCH = 0;
    public static final int CTRL_TILT = 1;
    public static final int Q_LOW = 0, Q_MED = 1, Q_HIGH = 2;

    public static final int[] CAR_COLORS = new int[]{
            0xffe53935, 0xff1e88e5, 0xff43a047, 0xffffb300, 0xff8e24aa, 0xffffffff
    };

    public static void initDefaults(Context c) {
        SharedPreferences sp = c.getSharedPreferences(P, Context.MODE_PRIVATE);
        if (!sp.contains(K_COINS)) {
            sp.edit().putLong(K_COINS, 0).apply();
        }
        if (!sp.contains(K_SELECTED_CAR)) {
            sp.edit().putString(K_SELECTED_CAR, "classic").apply();
            unlockCar(c, "classic");
        }
        if (!sp.contains(K_CONTROL)) {
            sp.edit().putInt(K_CONTROL, CTRL_TOUCH).apply();
        }
        if (!sp.contains(K_QUALITY)) {
            sp.edit().putInt(K_QUALITY, Q_MED).apply();
        }
    }

    public static long getCoins(Context c) {
        return c.getSharedPreferences(P, Context.MODE_PRIVATE).getLong(K_COINS, 0);
    }

    public static void addCoins(Context c, long delta) {
        SharedPreferences sp = c.getSharedPreferences(P, Context.MODE_PRIVATE);
        sp.edit().putLong(K_COINS, Math.max(0, sp.getLong(K_COINS, 0)+delta)).apply();
    }

    public static boolean isCarUnlocked(Context c, String id) {
        return c.getSharedPreferences(P, Context.MODE_PRIVATE).getBoolean(K_UNLOCK_PREFIX+id, false);
    }

    public static void unlockCar(Context c, String id) {
        c.getSharedPreferences(P, Context.MODE_PRIVATE).edit().putBoolean(K_UNLOCK_PREFIX+id, true).apply();
    }

    public static String getSelectedCarId(Context c) {
        return c.getSharedPreferences(P, Context.MODE_PRIVATE).getString(K_SELECTED_CAR, "classic");
    }

    public static void setSelectedCarId(Context c, String id) {
        c.getSharedPreferences(P, Context.MODE_PRIVATE).edit().putString(K_SELECTED_CAR, id).apply();
    }

    public static UpgradeState getUpgrade(Context c, String id) {
        SharedPreferences sp = c.getSharedPreferences(P, Context.MODE_PRIVATE);
        int e = sp.getInt(K_UPGRADE_PREFIX+id+"_e", 0);
        int g = sp.getInt(K_UPGRADE_PREFIX+id+"_g", 0);
        int t = sp.getInt(K_UPGRADE_PREFIX+id+"_t", 0);
        return new UpgradeState(e,g,t);
    }

    public static void setUpgrade(Context c, String id, UpgradeState u) {
        c.getSharedPreferences(P, Context.MODE_PRIVATE).edit()
                .putInt(K_UPGRADE_PREFIX+id+"_e", u.engineLvl)
                .putInt(K_UPGRADE_PREFIX+id+"_g", u.gripLvl)
                .putInt(K_UPGRADE_PREFIX+id+"_t", u.tiresLvl)
                .apply();
    }

    public static int getCarColorIndex(Context c, String id) {
        return c.getSharedPreferences(P, Context.MODE_PRIVATE).getInt(K_COLOR_PREFIX+id, 0);
    }
    public static void setCarColorIndex(Context c, String id, int idx) {
        c.getSharedPreferences(P, Context.MODE_PRIVATE).edit().putInt(K_COLOR_PREFIX+id, idx).apply();
    }

    public static int getControlMode(Context c) {
        return c.getSharedPreferences(P, Context.MODE_PRIVATE).getInt(K_CONTROL, CTRL_TOUCH);
    }
    public static void setControlMode(Context c, int mode) {
        c.getSharedPreferences(P, Context.MODE_PRIVATE).edit().putInt(K_CONTROL, mode).apply();
    }

    public static int getQuality(Context c) {
        return c.getSharedPreferences(P, Context.MODE_PRIVATE).getInt(K_QUALITY, Q_MED);
    }
    public static void setQuality(Context c, int q) {
        c.getSharedPreferences(P, Context.MODE_PRIVATE).edit().putInt(K_QUALITY, q).apply();
    }
}