package com.moyu.dynamicTp.util;

public class Vec2 {
    public final double x;
    public final double z;

    public Vec2(double x, double z) {
        this.x = x;
        this.z = z;
    }

    public double length() {
        return Math.sqrt(x * x + z * z);
    }

    public Vec2 multiply(double s) {
        return new Vec2(x * s, z * s);
    }

    public Vec2 rotate(double rad) {
        double cos = Math.cos(rad);
        double sin = Math.sin(rad);
        double nx = x * cos - z * sin;
        double nz = x * sin + z * cos;
        return new Vec2(nx, nz);
    }
}
