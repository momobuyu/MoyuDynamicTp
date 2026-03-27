package com.moyu.dynamicTp.util;

import org.bukkit.ChatColor;

public class Msg {

    public static String colored(String s) {
        return ChatColor.translateAlternateColorCodes('&', s);
    }

    public static String noPerm(ConfigLoader loader) {
        return colored(loader.prefix() + loader.msgNoPerm());
    }
}
