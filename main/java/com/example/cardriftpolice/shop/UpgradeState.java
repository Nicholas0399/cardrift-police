package com.example.cardriftpolice.shop;

public class UpgradeState {
    public int engineLvl;
    public int gripLvl;
    public int tiresLvl;

    public UpgradeState(int e, int g, int t) {
        engineLvl = e; gripLvl = g; tiresLvl = t;
    }

    public float engineMul() { return 1f + 0.08f * engineLvl; }
    public float gripMul()   { return 1f + 0.06f * gripLvl; }
    public float tiresMul()  { return 1f + 0.05f * tiresLvl; }
}