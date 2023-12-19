package net.miaomoe.shizoukia.focalors.utils;

import net.miaomoe.shizoukia.focalors.Focalors;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitScheduler;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;

import static net.miaomoe.shizoukia.focalors.Focalors.task;

// 这个 BanWave 类用于处理 BanWave 相关的逻辑
public class BanWave {
    public final Focalors plugin = Focalors.INSTANCE;
    public final List<OfflinePlayer> banWave = new CopyOnWriteArrayList<>(); // 使用 Focalors 类中的 banWave
    public final BukkitScheduler scheduler = Bukkit.getServer().getScheduler();
    private final String PERMISSION_NOTIFY = "focalors.banwave.notify";
    public final AtomicBoolean banWaveActive = new AtomicBoolean(false);

    // 在 ban 方法中，确保使用 Focalors 类中的 banWave
    public void ban(Player player) {
        scheduler.runTaskAsynchronously(plugin, (() -> {
            for (OfflinePlayer orig : plugin.banWave) {
                if (player.getUniqueId() == orig.getUniqueId()) {
                    return;
                }
            }
            plugin.banWave.add(player);
            broadcast("&7[player] is added to BanWave.".replace("[player]", player.getName()), PERMISSION_NOTIFY, true);
            if (!banWaveActive.get()) {
                if (task != null) {
                    task.cancel();
                }
                task = null;
                final long seconds = plugin.random.nextInt((plugin.banWaveMax - plugin.banWaveMin) + 1) + (long) plugin.banWaveMin;
                broadcast("&7Triggered BanWave. They will be execute after " + seconds + " seconds.", PERMISSION_NOTIFY, true);
                task = scheduler.runTaskLaterAsynchronously(Focalors.INSTANCE, (this::executeBanWave), (seconds * 20L));
                banWaveActive.set(true);
            }
        }));
    }

    // 在 executeBanWave 方法中，确保使用 Focalors 类中的 banWave
    public void executeBanWave() {
        final FileConfiguration config = plugin.getConfig();
        broadcast("&cBanWave is incoming...", PERMISSION_NOTIFY, true);
        broadcast(config.getStringList("broadcast"), "focalors.banwave.broadcast", false);
        if (task != null) {
            task.cancel();
            task = null;
        }
        scheduler.runTaskLaterAsynchronously(plugin, (() -> {
            int count = 0;
            for (OfflinePlayer p : plugin.banWave) { // 使用 Focalors 类中的 banWave
                if (p.isOnline()) {
                    final Player a = p.getPlayer();
                    if (a.hasPermission("focalors.banwave.bypass")) {
                        broadcast("&b" + p.getName() + " has escaped BanWave because they has bypass permission.", PERMISSION_NOTIFY, true);
                        continue;
                    }
                } else {
                    broadcast("&c" + p.getName() + " is offline.", PERMISSION_NOTIFY, true);
                }
                count++;
                plugin.totalBanned++;
                final String command = p.isOnline() ? "fts punishban " + p.getName() + " &7[GN]" : "tempban -s " + p.getName() + " 7d &cFocalors Cheat Trial &7[GN]";
                scheduler.runTask(plugin, (() -> Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), command)));
            }
            plugin.banWave.clear(); // 使用 Focalors 类中的 banWave
            broadcast("&7BanWave banned " + count + " player(s) on this time.", PERMISSION_NOTIFY, true);
            banWaveActive.set(false);
        }), plugin.broadcastDelay * 20L);
    }

    // 在 broadcast 方法中，确保使用 Focalors 类中的 PREFIX
    public void broadcast(String message, String permission, boolean prefix) {
        if (message.isEmpty()) {
            return;
        }
        final String format = ColorUtils.MsgColor((prefix ? plugin.PREFIX : "") + message);
        Bukkit.getLogger().log(Level.INFO, format);
        for (Player player : Bukkit.getServer().getOnlinePlayers()) {
            if (player.hasPermission(permission)) {
                player.sendMessage(format);
            }
        }
    }

    // 在 broadcast(List<String> message, String permission, boolean prefix) 方法中，确保使用 Focalors 类中的 PREFIX
    public void broadcast(List<String> message, String permission, boolean prefix) {
        if (message.isEmpty()) {
            return;
        }
        for (String string : message) {
            broadcast(string, permission, prefix);
        }
    }
}
