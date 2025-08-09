package com.example.cardriftpolice.shop;

public class CarSpec {
    public final String id;
    public final String title;
    public final long price;
    public final float baseMaxSpeed;
    public final float baseAccel;
    public final float baseGrip;
    public final float baseDurability;

    public CarSpec(String id, String title, long price,
                   float baseMaxSpeed, float baseAccel, float baseGrip, float baseDurability) {
        this.id = id;
        this.title = title;
        this.price = price;
        this.baseMaxSpeed = baseMaxSpeed;
        this.baseAccel = baseAccel;
        this.baseGrip = baseGrip;
        this.baseDurability = baseDurability;
    }
}