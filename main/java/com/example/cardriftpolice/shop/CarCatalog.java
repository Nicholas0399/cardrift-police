package com.example.cardriftpolice.shop;

public class CarCatalog {
    public static final CarSpec CLASSIC = new CarSpec("classic", "Классика", 0, 120f, 120f, 0.90f, 100f);
    public static final CarSpec MUSCLE = new CarSpec("muscle", "Маслкар", 1200, 150f, 140f, 0.82f, 120f);
    public static final CarSpec SPORT = new CarSpec("sport", "Спорт", 2400, 180f, 170f, 0.88f, 100f);
    public static final CarSpec DRIFT = new CarSpec("drift", "Дрифт-спец", 3600, 170f, 160f, 0.78f, 110f);

    public static final CarSpec[] CAR_LIST = new CarSpec[] { CLASSIC, MUSCLE, SPORT, DRIFT };

    public static CarSpec byId(String id) {
        for (CarSpec c : CAR_LIST) if (c.id.equals(id)) return c;
        return CLASSIC;
    }
}