package com.moyu.dynamicTp;

import com.moyu.dynamicTp.command.MoyuDynamicTpCommand;
import com.moyu.dynamicTp.listener.PlayerCacheListener;
import com.moyu.dynamicTp.service.TeleportService;
import com.moyu.dynamicTp.util.ConfigLoader;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class MoyuDynamicTpPlugin extends JavaPlugin {

    private ConfigLoader configLoader;
    private TeleportService teleportService;

    @Override
    public void onEnable() {
        // 1) 生成默认配置
        saveDefaultConfig();

        // 2) 加载配置到内存
        this.configLoader = new ConfigLoader(this);
        this.configLoader.reload();

        // 3) 核心服务
        this.teleportService = new TeleportService(this, configLoader);

        // 4) 注册命令 + Tab
        MoyuDynamicTpCommand cmd = new MoyuDynamicTpCommand(this, teleportService, configLoader);
        getCommand("moyudynamictp").setExecutor(cmd);
        getCommand("moyudynamictp").setTabCompleter(cmd);

        // 5) 注册监听器（用于清理缓存/示范扩展点）
        Bukkit.getPluginManager().registerEvents(new PlayerCacheListener(teleportService), this);

        getLogger().info("MoyuDynamicTp enabled.");
    }

    @Override
    public void onDisable() {
        getLogger().info("MoyuDynamicTp disabled.");
    }

    public ConfigLoader getConfigLoader() {
        return configLoader;
    }
}
