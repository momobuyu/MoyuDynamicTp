package com.moyu.dynamicTp.model;

/**
 * 代表一个锚点：x,y,z,yaw,pitch
 */
public class Anchor {
    public final double x;
    public final double y;
    public final double z;
    public final float yaw;
    public final float pitch;

    public Anchor(double x, double y, double z, float yaw, float pitch) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.yaw = yaw;
        this.pitch = pitch;
    }

    public org.bukkit.Location toBukkitLocation(org.bukkit.World world) {
        return new org.bukkit.Location(world, x, y, z, yaw, pitch);
    }

    @Override
    public String toString() {
        return "Anchor{" + x + "," + y + "," + z + "," + yaw + "," + pitch + "}";
    }
}
