package com.moyu.dynamicTp.command;

import com.moyu.dynamicTp.service.TeleportService;
import com.moyu.dynamicTp.util.ConfigLoader;
import com.moyu.dynamicTp.util.Msg;
import org.bukkit.Bukkit;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;
import java.util.stream.Collectors;

public class MoyuDynamicTpCommand implements CommandExecutor, TabCompleter {

    private final JavaPlugin plugin;
    private final TeleportService teleportService;
    private final ConfigLoader configLoader;

    public MoyuDynamicTpCommand(JavaPlugin plugin, TeleportService teleportService, ConfigLoader configLoader) {
        this.plugin = plugin;
        this.teleportService = teleportService;
        this.configLoader = configLoader;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        // /mdt reload
        if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
            if (!sender.hasPermission("moyudynamictp.reload")) {
                sender.sendMessage(Msg.noPerm(configLoader));
                return true;
            }
            plugin.reloadConfig();
            configLoader.reload();
            sender.sendMessage(Msg.colored(configLoader.prefix() + configLoader.msgReloaded()));
            return true;
        }

        // /mdt <player> <mapping>
        if (args.length >= 2) {
            if (!sender.hasPermission("moyudynamictp.use")) {
                sender.sendMessage(Msg.noPerm(configLoader));
                return true;
            }

            Player target = Bukkit.getPlayerExact(args[0]);
            if (target == null) {
                sender.sendMessage(Msg.colored(configLoader.prefix() + configLoader.msgPlayerNotFound()));
                return true;
            }

            String mappingName = args[1];
            boolean ok = teleportService.teleportByMapping(target, mappingName);

            if (!ok) {
                // 失败原因会在 service 内部按配置提示玩家/操作者，这里不重复刷屏
                return true;
            }

            sender.sendMessage(Msg.colored(
                    configLoader.prefix() + configLoader.msgTeleported()
                            .replace("{player}", target.getName())
                            .replace("{mapping}", mappingName)
            ));
            return true;
        }

        sender.sendMessage(Msg.colored(configLoader.prefix() + "&e用法: &f/" + label + " <玩家名> <配置名> &7| &f/" + label + " reload"));
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {

        if (args.length == 1) {
            List<String> base = new ArrayList<>();
            if (sender.hasPermission("moyudynamictp.reload")) base.add("reload");
            if (sender.hasPermission("moyudynamictp.use")) {
                base.addAll(Bukkit.getOnlinePlayers().stream().map(Player::getName).collect(Collectors.toList()));
            }
            return filterPrefix(base, args[0]);
        }

        if (args.length == 2) {
            if ("reload".equalsIgnoreCase(args[0])) return Collections.emptyList();
            if (!sender.hasPermission("moyudynamictp.use")) return Collections.emptyList();

            return filterPrefix(new ArrayList<>(configLoader.getMappingNames()), args[1]);
        }

        return Collections.emptyList();
    }

    private List<String> filterPrefix(List<String> list, String prefix) {
        if (prefix == null || prefix.isEmpty()) return list;
        String p = prefix.toLowerCase(Locale.ROOT);
        List<String> out = new ArrayList<>();
        for (String s : list) {
            if (s.toLowerCase(Locale.ROOT).startsWith(p)) out.add(s);
        }
        return out;
    }
}
