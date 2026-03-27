package com.moyu.dynamicTp.util;

import org.bukkit.Location;

public class Cuboid {
    public final double minX, minY, minZ;
    public final double maxX, maxY, maxZ;

    public Cuboid(double x1, double y1, double z1, double x2, double y2, double z2) {
        this.minX = Math.min(x1, x2);
        this.minY = Math.min(y1, y2);
        this.minZ = Math.min(z1, z2);
        this.maxX = Math.max(x1, x2);
        this.maxY = Math.max(y1, y2);
        this.maxZ = Math.max(z1, z2);
    }

    public boolean contains(Location loc) {
        double x = loc.getX();
        double y = loc.getY();
        double z = loc.getZ();
        return x >= minX && x <= maxX
                && y >= minY && y <= maxY
                && z >= minZ && z <= maxZ;
    }

    @Override
    public String toString() {
        return "Cuboid{" + minX + "," + minY + "," + minZ + " -> " + maxX + "," + maxY + "," + maxZ + "}";
    }
}
