package net.miaomoe.shizoukia.focalors.utils;

import org.bukkit.ChatColor;

public class ColorUtils {

    public static String MsgColor(String orig) {
        return ChatColor.translateAlternateColorCodes('&', orig);
    }
}
