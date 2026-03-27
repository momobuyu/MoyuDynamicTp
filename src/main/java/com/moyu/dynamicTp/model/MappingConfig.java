package com.moyu.dynamicTp.model;

import com.moyu.dynamicTp.util.Cuboid;

public class MappingConfig {

    private final String name;
    private final String world; // 同一个世界

    private final Anchor location1A;
    private final Anchor location1B;
    private final Anchor location2A;
    private final Anchor location2B;

    private final Cuboid region1;
    private final Cuboid region2;

    private final boolean enableScale;

    public MappingConfig(String name,
                         String world,
                         Anchor location1A, Anchor location1B,
                         Anchor location2A, Anchor location2B,
                         Cuboid region1, Cuboid region2,
                         boolean enableScale) {
        this.name = name;
        this.world = world;
        this.location1A = location1A;
        this.location1B = location1B;
        this.location2A = location2A;
        this.location2B = location2B;
        this.region1 = region1;
        this.region2 = region2;
        this.enableScale = enableScale;
    }

    public String getName() { return name; }
    public String getWorld() { return world; }

    public Anchor getLocation1A() { return location1A; }
    public Anchor getLocation1B() { return location1B; }
    public Anchor getLocation2A() { return location2A; }
    public Anchor getLocation2B() { return location2B; }

    public Cuboid getRegion1() { return region1; }
    public Cuboid getRegion2() { return region2; }

    public boolean isEnableScale() { return enableScale; }
}
