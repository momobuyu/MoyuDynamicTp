package com.moyu.dynamicTp.service;

import com.moyu.dynamicTp.model.Anchor;
import com.moyu.dynamicTp.model.MappingConfig;
import com.moyu.dynamicTp.util.AngleUtil;
import com.moyu.dynamicTp.util.ConfigLoader;
import com.moyu.dynamicTp.util.Msg;
import com.moyu.dynamicTp.util.Vec2;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 同一世界内双向无缝映射：
 * - 玩家在 region1 内：按 1A/1B -> 2A/2B
 * - 玩家在 region2 内：按 2A/2B -> 1A/1B
 */
public class TeleportService {

    private final JavaPlugin plugin;
    private final ConfigLoader loader;

    // 预留缓存（以后做 move 自动触发可用）
    private final Map<UUID, Long> playerCache = new ConcurrentHashMap<>();

    public TeleportService(JavaPlugin plugin, ConfigLoader loader) {
        this.plugin = plugin;
        this.loader = loader;
    }

    public void clearPlayerCache(UUID uuid) {
        playerCache.remove(uuid);
    }

    public boolean teleportByMapping(Player player, String mappingName) {
        MappingConfig mapping = loader.getMapping(mappingName);
        if (mapping == null) {
            player.sendMessage(Msg.colored(loader.prefix()
                    + loader.msgMappingNotFound().replace("{mapping}", mappingName)));
            return false;
        }

        World world = Bukkit.getWorld(mapping.getWorld());
        if (world == null) {
            player.sendMessage(Msg.colored(loader.prefix() + "&c配置 world 不存在，请检查 config.yml"));
            return false;
        }

        if (!player.getWorld().equals(world)) {
            player.sendMessage(Msg.colored(loader.prefix() + "&c你不在配置指定的 world：&e" + mapping.getWorld()));
            return false;
        }

        Location from = player.getLocation();

        boolean inR1 = mapping.getRegion1().contains(from);
        boolean inR2 = mapping.getRegion2().contains(from);

        // ✅ 用区域边界判定方向
        final boolean forward;
        if (inR1 && !inR2) {
            forward = true;   // 1 -> 2
        } else if (inR2 && !inR1) {
            forward = false;  // 2 -> 1
        } else if (inR1) {
            // 同时落在两个区域（通常是你 cuboid 配得重叠了）
            player.sendMessage(Msg.colored(loader.prefix() + "&c你同时处于 region1 与 region2 内，请检查两块区域是否重叠。"));
            return false;
        } else {
            player.sendMessage(Msg.colored(loader.prefix() + loader.msgNotInWorld()));
            return false;
        }

        Location to = mapLocationBidirectional(from, mapping, forward, world);
        player.teleport(to);
        return true;
    }

    /**
     * forward=true:  1 -> 2
     * forward=false: 2 -> 1
     *
     * XZ：旋转 +（可选）缩放 + 平移
     * Y：相对高度平移
     * 视角：Yaw 随旋转角 + 常量偏移；Pitch 常量偏移
     */
    private Location mapLocationBidirectional(Location from, MappingConfig m, boolean forward, World world) {

        Anchor srcA = forward ? m.getLocation1A() : m.getLocation2A();
        Anchor srcB = forward ? m.getLocation1B() : m.getLocation2B();
        Anchor dstA = forward ? m.getLocation2A() : m.getLocation1A();
        Anchor dstB = forward ? m.getLocation2B() : m.getLocation1B();

        Location a1 = srcA.toBukkitLocation(world);
        Location b1 = srcB.toBukkitLocation(world);
        Location a2 = dstA.toBukkitLocation(world);
        Location b2 = dstB.toBukkitLocation(world);

        Vec2 v1 = new Vec2(b1.getX() - a1.getX(), b1.getZ() - a1.getZ());
        Vec2 v2 = new Vec2(b2.getX() - a2.getX(), b2.getZ() - a2.getZ());

        double len1 = v1.length();
        double len2 = v2.length();
        if (len1 < 1e-6 || len2 < 1e-6) {
            return a2.clone();
        }

        double ang1 = Math.atan2(v1.z, v1.x);
        double ang2 = Math.atan2(v2.z, v2.x);
        double rotRad = ang2 - ang1;
        double rotDeg = Math.toDegrees(rotRad);

        double scale = 1.0;
        if (m.isEnableScale()) {
            scale = len2 / len1;
        }

        Vec2 rp = new Vec2(from.getX() - a1.getX(), from.getZ() - a1.getZ());
        Vec2 rp2 = rp.rotate(rotRad).multiply(scale);

        double newX = a2.getX() + rp2.x;
        double newZ = a2.getZ() + rp2.z;
        double newY = from.getY() + (a2.getY() - a1.getY());

        double yawConstA = AngleUtil.wrapDegrees(dstA.yaw - (srcA.yaw + rotDeg));
        double yawConstB = AngleUtil.wrapDegrees(dstB.yaw - (srcB.yaw + rotDeg));
        double yawConst = AngleUtil.wrapDegrees((yawConstA + yawConstB) / 2.0);

        double pitchConstA = (dstA.pitch - srcA.pitch);
        double pitchConstB = (dstB.pitch - srcB.pitch);
        double pitchConst = (pitchConstA + pitchConstB) / 2.0;

        float newYaw = (float) AngleUtil.wrapDegrees(from.getYaw() + rotDeg + yawConst);
        float newPitch = (float) AngleUtil.clampPitch(from.getPitch() + pitchConst);

        return new Location(world, newX, newY, newZ, newYaw, newPitch);
    }
}
