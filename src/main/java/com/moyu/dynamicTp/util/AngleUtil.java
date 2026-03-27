package com.moyu.dynamicTp.util;

public class AngleUtil {

    /** 把角度压到 [-180, 180) 更稳定 */
    public static double wrapDegrees(double deg) {
        double d = deg;
        while (d >= 180.0) d -= 360.0;
        while (d < -180.0) d += 360.0;
        return d;
    }

    /** MC pitch 通常 [-90, 90] */
    public static double clampPitch(double pitch) {
        if (pitch > 90.0) return 90.0;
        if (pitch < -90.0) return -90.0;
        return pitch;
    }
}
