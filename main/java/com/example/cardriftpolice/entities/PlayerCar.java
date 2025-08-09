package com.example.cardriftpolice.entities;

import com.example.cardriftpolice.shop.CarCatalog;
import com.example.cardriftpolice.shop.CarSpec;
import com.example.cardriftpolice.shop.UpgradeState;

public class PlayerCar extends Car {

    public PlayerCar(CarSpec spec, UpgradeState up) {
        maxSpeed = spec.baseMaxSpeed * up.engineMul();
        accel    = spec.baseAccel    * up.engineMul();
        grip     = Math.min(0.98f, spec.baseGrip * up.gripMul() * up.tiresMul());
        durability = spec.baseDurability * 1.0f;
        width = 56; height = 28;
    }
}