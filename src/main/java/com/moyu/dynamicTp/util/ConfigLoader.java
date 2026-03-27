package com.moyu.dynamicTp.util;

import com.moyu.dynamicTp.model.Anchor;
import com.moyu.dynamicTp.model.MappingConfig;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class ConfigLoader {

    private final JavaPlugin plugin;
    private final Map<String, MappingConfig> mappings = new HashMap<>();

    private String prefix;
    private String noPerm;
    private String playerNotFound;
    private String mappingNotFound;
    private String reloaded;
    private String teleported;
    private String notInWorld;

    public ConfigLoader(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void reload() {
        mappings.clear();

        prefix = plugin.getConfig().getString("messages.prefix", "&7[&bMoyuDynamicTp&7] ");
        noPerm = plugin.getConfig().getString("messages.no_permission", "&c你没有权限。");
        playerNotFound = plugin.getConfig().getString("messages.player_not_found", "&c找不到该玩家。");
        mappingNotFound = plugin.getConfig().getString("messages.mapping_not_found", "&c找不到配置 &e{mapping}&c。");
        reloaded = plugin.getConfig().getString("messages.reloaded", "&a配置已重载。");
        teleported = plugin.getConfig().getString("messages.teleported", "&a已将 &e{player}&a 无缝传送到 &e{mapping}&a 对应区域。");
        notInWorld = plugin.getConfig().getString("messages.not_in_world", "&c你不在任意配置区域内。");

        ConfigurationSection root = plugin.getConfig().getConfigurationSection("mappings");
        if (root == null) {
            plugin.getLogger().warning("[MoyuDynamicTp] config.yml 缺少 mappings 根节点。");
            return;
        }

        for (String name : root.getKeys(false)) {
            ConfigurationSection s = root.getConfigurationSection(name);
            if (s == null) continue;

            // 同世界模式
            String world = s.getString("world", "world");
            boolean enableScale = s.getBoolean("enableScale", true);

            Anchor l1a = parseAnchor(s.getString("location1A"));
            Anchor l1b = parseAnchor(s.getString("location1B"));
            Anchor l2a = parseAnchor(s.getString("location2A"));
            Anchor l2b = parseAnchor(s.getString("location2B"));

            if (l1a == null || l1b == null || l2a == null || l2b == null) {
                plugin.getLogger().warning("[MoyuDynamicTp] Mapping '" + name + "' has invalid anchors, skipped.");
                plugin.getLogger().warning("[MoyuDynamicTp]  location1A=" + s.getString("location1A"));
                plugin.getLogger().warning("[MoyuDynamicTp]  location1B=" + s.getString("location1B"));
                plugin.getLogger().warning("[MoyuDynamicTp]  location2A=" + s.getString("location2A"));
                plugin.getLogger().warning("[MoyuDynamicTp]  location2B=" + s.getString("location2B"));
                continue;
            }

            // ✅ 兼容：region1/region2 优先按 section(pos1/pos2) 读，读不到就回退按字符串读
            Cuboid r1 = parseRegionFlexible(s, "region1");
            Cuboid r2 = parseRegionFlexible(s, "region2");

            if (r1 == null || r2 == null) {
                plugin.getLogger().warning("[MoyuDynamicTp] Mapping '" + name + "' has invalid region1/region2 cuboid, skipped.");
                // 打印出真实读到的内容，方便你立刻定位缩进/符号问题
                plugin.getLogger().warning("[MoyuDynamicTp]  region1.section=" + (s.getConfigurationSection("region1") != null));
                plugin.getLogger().warning("[MoyuDynamicTp]  region2.section=" + (s.getConfigurationSection("region2") != null));
                plugin.getLogger().warning("[MoyuDynamicTp]  region1.pos1=" + safeSecGet(s.getConfigurationSection("region1"), "pos1"));
                plugin.getLogger().warning("[MoyuDynamicTp]  region1.pos2=" + safeSecGet(s.getConfigurationSection("region1"), "pos2"));
                plugin.getLogger().warning("[MoyuDynamicTp]  region2.pos1=" + safeSecGet(s.getConfigurationSection("region2"), "pos1"));
                plugin.getLogger().warning("[MoyuDynamicTp]  region2.pos2=" + safeSecGet(s.getConfigurationSection("region2"), "pos2"));
                plugin.getLogger().warning("[MoyuDynamicTp]  region1.raw=" + s.getString("region1"));
                plugin.getLogger().warning("[MoyuDynamicTp]  region2.raw=" + s.getString("region2"));
                continue;
            }

            MappingConfig cfg = new MappingConfig(
                    name,
                    world,
                    l1a, l1b,
                    l2a, l2b,
                    r1, r2,
                    enableScale
            );

            mappings.put(name, cfg);
        }

        plugin.getLogger().info("[MoyuDynamicTp] Loaded mappings: " + mappings.keySet());
    }

    private String safeSecGet(ConfigurationSection sec, String path) {
        if (sec == null) return null;
        return sec.getString(path);
    }

    /**
     * 锚点格式：x,y,z,yaw,pitch（允许空格/中文逗号，允许多余段数，取前5段）
     */
    private Anchor parseAnchor(String raw) {
        try {
            double[] nums = parseNumbers(raw);
            if (nums.length < 5) return null;
            return new Anchor(nums[0], nums[1], nums[2], (float) nums[3], (float) nums[4]);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * region 支持两种写法：
     * A) region1:
     *      pos1: "x,y,z"
     *      pos2: "x,y,z"
     *
     * B) region1: "x1,y1,z1,x2,y2,z2"（旧写法回退兼容）
     *
     * 同时：如果 pos 里写成了 "x,y,z,yaw,pitch" 也允许，自动取前三段。
     */
    private Cuboid parseRegionFlexible(ConfigurationSection mappingSec, String key) {
        try {
            // 先按 section 读
            ConfigurationSection sec = mappingSec.getConfigurationSection(key);
            if (sec != null) {
                double[] p1 = parseNumbers(sec.getString("pos1"));
                double[] p2 = parseNumbers(sec.getString("pos2"));
                if (p1.length >= 3 && p2.length >= 3) {
                    return new Cuboid(p1[0], p1[1], p1[2], p2[0], p2[1], p2[2]);
                }
                return null;
            }

            // 再按字符串回退（防 tab 缩进导致 section 读不到）
            String raw = mappingSec.getString(key);
            if (raw == null) return null;
            double[] nums = parseNumbers(raw);
            if (nums.length >= 6) {
                return new Cuboid(nums[0], nums[1], nums[2], nums[3], nums[4], nums[5]);
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 把字符串里的数字解析出来：
     * - 兼容中文逗号 ，
     * - 兼容空格分隔
     * - 兼容多余字段（yaw/pitch等）
     */
    private double[] parseNumbers(String raw) {
        if (raw == null) return new double[0];

        // 标准化：中文逗号 -> 英文逗号；多个空白 -> 单空格
        String s = raw.trim()
                .replace('，', ',')
                .replace('−', '-') // 有些输入法会给 unicode minus
                .replaceAll("\\s+", " ");

        // 允许用逗号或空格混合分隔
        String[] parts = s.split("[, ]+");
        double[] out = new double[parts.length];
        int n = 0;
        for (String p : parts) {
            if (p.isEmpty()) continue;
            out[n++] = Double.parseDouble(p.trim());
        }

        // 截断为实际长度
        double[] r = new double[n];
        System.arraycopy(out, 0, r, 0, n);
        return r;
    }

    public MappingConfig getMapping(String name) {
        return mappings.get(name);
    }

    public Set<String> getMappingNames() {
        return mappings.keySet();
    }

    public String prefix() { return prefix; }
    public String msgNoPerm() { return noPerm; }
    public String msgPlayerNotFound() { return playerNotFound; }
    public String msgMappingNotFound() { return mappingNotFound; }
    public String msgReloaded() { return reloaded; }
    public String msgTeleported() { return teleported; }
    public String msgNotInWorld() { return notInWorld; }
}
