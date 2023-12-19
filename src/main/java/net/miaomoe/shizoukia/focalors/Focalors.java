package net.miaomoe.shizoukia.focalors;

import net.miaomoe.shizoukia.focalors.utils.BanWave;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
//比较难绷的事我没抄明白kautoban 这个主类现在非常的乱我只能说
public final class Focalors extends JavaPlugin {
    public final List<OfflinePlayer> banWave = new CopyOnWriteArrayList<>();
    @Override
    public void reloadConfig() {
        super.reloadConfig();
    }
    public int banWaveMin = 10;
    public int banWaveMax = 30;
    public int broadcastDelay = 0;
    public final Random random = new Random();
    public Focalors plugin;
    BanWave punish = new BanWave();
    public final static BukkitScheduler scheduler = Bukkit.getServer().getScheduler();

    public FileConfiguration config = this.getConfig();
    public String PREFIX = "&b&lFocalors &7» &r";
    public List<Pattern> REGEX = new ArrayList<>();

    public final String PERMISSION_NOTIFY = "focalors.notify";

    public static BukkitTask task = null;

    public int totalBanned = 0;
    public static Focalors INSTANCE;

    void loadRegex(FileConfiguration config) {
        this.REGEX.clear();
        final Logger logger = this.getLogger();
        final List<String> list = config.getStringList("auto-ban.regex");
        if (list.isEmpty()) {
            logger.log(Level.WARNING, "No any auto-ban name regex found.");
            return;
        }
        for (String raw : list) {
            try {
                final Pattern regex = Pattern.compile(raw);
                REGEX.add(regex);
                final String log = "Regex " + raw + " is successfully added to auto-ban regex.";
                logger.log(Level.INFO, log);
            } catch (PatternSyntaxException exception) {
                logger.log(Level.WARNING, "Failed to parse regex: {raw}".replace("{raw}", raw), exception);
            }
        }
        final String msg = "Loaded " + REGEX.size() + " auto-ban rules.";
        logger.log(Level.INFO, msg);
    }
    @Override
    public void onEnable() {
        plugin = this;
        saveConfig();
        // Plugin startup logic
        getLogger().info("[Focalors] Enabled! Version: " + getDescription().getVersion() + " By Shizoukia");
        getCommand("focalors").setExecutor(new FocalorsCommands(this));
    }
    @Override
    public void onLoad() {
        INSTANCE = this;
    }
    @Override
    public void onDisable() {
        // Plugin shutdown logic
        getLogger().info("[Focalors] Disabled! Version: " + getDescription().getVersion() + " By Shizoukia");
    }
}
