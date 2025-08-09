package com.example.cardriftpolice.math;

public class Vec2 {
    public float x, y;
    public Vec2() { this(0,0); }
    public Vec2(float x, float y) { this.x = x; this.y = y; }
    public Vec2 set(float x, float y){ this.x=x; this.y=y; return this; }
    public Vec2 add(float dx, float dy){ x+=dx; y+=dy; return this; }
    public Vec2 add(Vec2 o){ x+=o.x; y+=o.y; return this; }
    public Vec2 sub(Vec2 o){ x-=o.x; y-=o.y; return this; }
    public Vec2 scl(float s){ x*=s; y*=s; return this; }
    public float len(){ return (float)Math.sqrt(x*x+y*y); }
    public float len2(){ return x*x+y*y; }
    public Vec2 nor(){ float l=len(); if(l>1e-6f){x/=l;y/=l;} return this; }
    public Vec2 set(Vec2 o){ x=o.x; y=o.y; return this; }
    public static float dot(Vec2 a, Vec2 b){ return a.x*b.x+a.y*b.y; }
}