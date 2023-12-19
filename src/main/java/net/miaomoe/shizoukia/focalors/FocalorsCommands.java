package net.miaomoe.shizoukia.focalors;

import net.miaomoe.shizoukia.focalors.utils.BanWave;
import net.miaomoe.shizoukia.focalors.utils.ColorUtils;
import net.minecraft.server.v1_8_R3.IChatBaseComponent;
import net.minecraft.server.v1_8_R3.PacketPlayOutTitle;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;


public final class FocalorsCommands implements CommandExecutor {
    private Focalors plugin;

    public final List<OfflinePlayer> banWave = new CopyOnWriteArrayList<>();
    public BanWave banWaveHandler;
    public final AtomicBoolean banWaveActive = new AtomicBoolean(false);

    public FocalorsCommands(Focalors plugin) {
        this.plugin = plugin;
    }
    public static StringBuilder argsBuilder(int startIndex, String[] args) {
        StringBuilder message = new StringBuilder();
        if (args != null && args.length - 2 >= startIndex) {
            for (int i = startIndex; i < args.length - 1; i++) {
                message.append(args[i]).append(" ");
            }
        }
        message.append(args != null ? args[args.length - 1] : message);
        return message;
    }

    public static final CommandSender CONSOLE = Bukkit.getConsoleSender();
    public static final String unknown = "Unknown command. Type \"/help\" for help.";

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        this.banWaveHandler = new BanWave();
        final String SYNTAX = plugin.PREFIX + "&c/kab <add/remove/list/reload/now/clear> [Player]";
        BanWave punish = new BanWave();
        if (args.length > 0) {
            if ("help".equalsIgnoreCase(args[0])) {
                String helptext2 = "\n&b&lFocalors Help:\n&7» &b/focalors help &fto get this help\n&7» &b/focalors punishban &fto ban a player use focalors\n&7» &b/focalors punishwipe &fto wipe a player use focalors\n&7» &b/focalors banwave &fto ban a player use focalors banwave\n&f&l   ";
                sender.sendMessage(ColorUtils.MsgColor(helptext2));
            } else {
                if ("punishban".equalsIgnoreCase(args[0])) {
                    if (args.length < 2) {
                        sender.sendMessage("§cUsage: /fs punishban <player>");
                    } else {
                        if (!sender.hasPermission("focalors.punishmanager")) {
                            sender.sendMessage(ColorUtils.MsgColor(unknown));
                        } else {
                            final Player targetPlayer;
                            targetPlayer = Bukkit.getPlayer(args[1]);
                            final String banreason;
                            try {
                                banreason = String.valueOf(argsBuilder(2, args));
                                if (targetPlayer.getName() == null || banreason == null || banreason.isEmpty()) {
                                    throw new NullPointerException();
                                }
                            } catch (IndexOutOfBoundsException | NullPointerException e) {
                                sender.sendMessage(ColorUtils.MsgColor("&b&lFocalors &7» &cPlease select a reason"));
                                return true;
                            }

                            Bukkit.getServer().broadcastMessage(
                                    ColorUtils.MsgColor("\n&f&l» §c§l一名玩家因违反游戏规则被移出游戏！\n&f&l» §b§l使用/report来举报违反游戏规则的玩家！\n&f&l   ")
                            );
                            targetPlayer.getWorld().strikeLightningEffect(targetPlayer.getLocation());
                            final String banCommand = "litebans:ipban [player] 7d [reason]"
                                    .replace("[player]", targetPlayer.getName())
                                    .replace("[reason]", ColorUtils.MsgColor("&cFocalors Cheat Trial " + banreason));
                            if (targetPlayer.hasPermission("focalors.debug.notpunish")) {
                                String bantheme = ColorUtils.MsgColor("&cFocalors Cheat Trial §r") + ColorUtils.MsgColor(banreason);
                                IChatBaseComponent chatTitle = IChatBaseComponent.ChatSerializer.a("{\"text\": \"" + "§f " + "\"}");
                                IChatBaseComponent chatSubtitle = IChatBaseComponent.ChatSerializer.a("{\"text\": \"" + bantheme + "\"}");
                                PacketPlayOutTitle titlePacket = new PacketPlayOutTitle(PacketPlayOutTitle.EnumTitleAction.TITLE, chatTitle);
                                PacketPlayOutTitle subtitlePacket = new PacketPlayOutTitle(PacketPlayOutTitle.EnumTitleAction.SUBTITLE, chatSubtitle);
                                PacketPlayOutTitle lengthPacket = new PacketPlayOutTitle(10, 40, 10);

                                ((CraftPlayer) targetPlayer).getHandle().playerConnection.sendPacket(titlePacket);
                                ((CraftPlayer) targetPlayer).getHandle().playerConnection.sendPacket(subtitlePacket);
                                ((CraftPlayer) targetPlayer).getHandle().playerConnection.sendPacket(lengthPacket);
                            } else {
                                Bukkit.getServer().dispatchCommand(CONSOLE, banCommand);
                            }
                        }
                    }

                } else if ("punishwipe".equalsIgnoreCase(args[0])) {
                    if (!sender.hasPermission("focalors.punishmanager")) {
                        sender.sendMessage(ColorUtils.MsgColor(unknown));
                    } else {
                        if (args.length < 4) {
                            sender.sendMessage(ColorUtils.MsgColor("&cUsage: /punishwipe <player> <reason>"));
                        } else {
                            OfflinePlayer targetPlayer2 = Bukkit.getOfflinePlayer(args[1]);
                            boolean playeronline = targetPlayer2.isOnline();
                            String wipereason = args[2];
                            String wipetype = args[3];
                            if (wipereason.isEmpty()) {
                                sender.sendMessage("原因呢?");
                            } else {
                                if (wipetype.isEmpty()) {
                                    sender.sendMessage("你是否要封禁此玩家? yes/no");
                                } else {
                                    if (playeronline) {
                                        Player targetPlayer = (Player) targetPlayer2;
                                        final String wipeWarn = "\n&f&l» &c&l您的账户部分游戏数据已被我们重置\n"
                                                + "&f&l» &6&l原因: &f&l[reason]\n".replace("[reason]", wipereason)
                                                + "&f&l» §b§l请遵守我们的游戏规则 不要使用违规增益\n&f&l» "
                                                + "详见: &ehttps://www.miaomoe.net/rules \n&f&l   ";
                                        targetPlayer.sendMessage(ColorUtils.MsgColor(wipeWarn)); //[OWNER] hypixel: we need talk
                                        // 我他妈是穷举大神
                                        String[] subCommands = {
                                                "nodebuff", "boxing", "debuff", "builduhc", "sumo", "combo", "gapple",
                                                "soup", "bridge", "spleef", "archer", "sg", "classic", "vanilla", "pearlfight",
                                                "bedfight", "invaded", "skywars", "battlerush", "parkour", "wizard", "finaluhc"
                                        };

                                        for (String subCommand : subCommands) {
                                            String wipecommand = "eloset " + targetPlayer.getName() + " " + subCommand + " 1000";
                                            Bukkit.dispatchCommand(CONSOLE, wipecommand);
                                        }
//                                Bukkit.getServer().dispatchCommand(CONSOLE, "eloset " + targetPlayer + " nodebuff 1000");
                                        // 小比崽子给我死
                                        if (wipetype.equalsIgnoreCase("yes")) {
                                            String wipeban = "litebans:ipban " + targetPlayer.getName() + " 30d" + " &cIllegal to get stats &7[B]";
                                            System.out.println("DEBUG RUN COMMAND: " + wipeban);
                                            Bukkit.getServer().dispatchCommand(CONSOLE, wipeban);
                                        } else { // 我操 你他吗给我输入了什么玩意？？？
                                            sender.sendMessage(ColorUtils.MsgColor("已提交至控制台执行 如需要执行封禁玩家 请再次输入命令并在原因后加入 yes"));
                                        }
                                    } else {
                                        OfflinePlayer targetPlayerOffline = Bukkit.getOfflinePlayer(args[1]);
                                        // 我他妈是穷举大神
                                        String[] subCommands = {
                                                "nodebuff", "boxing", "debuff", "builduhc", "sumo", "combo", "gapple",
                                                "soup", "bridge", "spleef", "archer", "sg", "classic", "vanilla", "pearlfight",
                                                "bedfight", "invaded", "skywars", "battlerush", "parkour", "wizard", "finaluhc"
                                        };

                                        for (String subCommand : subCommands) {
                                            String wipecommand = "eloset " + targetPlayerOffline.getName() + " " + subCommand + " 1000";
                                            Bukkit.dispatchCommand(CONSOLE, wipecommand);
                                        }
//                                Bukkit.getServer().dispatchCommand(CONSOLE, "eloset " + targetPlayer + " nodebuff 1000");
                                        // 小比崽子给我死
                                        if (wipetype.equalsIgnoreCase("yes")) {
                                            String wipeban = "litebans:ban " + targetPlayerOffline.getName() + " 3d" + " &cIllegal to get stats &7[B]";
                                            System.out.println("DEBUG RUN COMMAND: " + wipeban);
                                            Bukkit.getServer().dispatchCommand(CONSOLE, wipeban);
                                        } else { // 我操 你他吗给我输入了什么玩意？？？
                                            sender.sendMessage(ColorUtils.MsgColor("已提交至控制台执行 如需要执行封禁玩家 请再次输入命令并在原因后加入 yes"));
                                        }
                                    }
                                }
                            }
                        }
                    }
                } else if ("banwave".equalsIgnoreCase(args[0])) {
                    if (!sender.hasPermission("focalors.banwave")) {
                        sender.sendMessage(ColorUtils.MsgColor(unknown));
                    } else {
                        if (args.length < 2) {
                            sender.sendMessage(ColorUtils.MsgColor("&cUsage: /fts <add/remove/list/reload/now/clear> [Player]"));
                        } else {
                            switch (args[1]) {
                                case "add":
                                    if (!sender.hasPermission("focalors.command.add")) {
                                        sender.sendMessage(unknown);
                                        return false;
                                    }
                                    try {
                                        final Player target = plugin.getServer().getPlayer(args[2]);
                                        if (target == null) {
                                            sender.sendMessage(ColorUtils.MsgColor(plugin.PREFIX + "&cPlayer not found."));
                                            return false;
                                        }
                                        if (banWave.contains(target)) {
                                            sender.sendMessage(ColorUtils.MsgColor(plugin.PREFIX + "&cThis player is already in list!"));
                                            return false;
                                        }
                                        punish.ban(target);
                                    } catch (IndexOutOfBoundsException e) {
                                        sender.sendMessage(ColorUtils.MsgColor(SYNTAX));
                                        return false;
                                    } catch (NullPointerException e) {
                                        sender.sendMessage(ColorUtils.MsgColor(plugin.PREFIX + "&cCannot found this player."));
                                        return false;
                                    }
                                    break;
                                case "remove":
                                    if (!sender.hasPermission("focalors.banwave.remove")) {
                                        sender.sendMessage(ColorUtils.MsgColor("&cNo permission."));
                                        return false;
                                    }
                                    try {
                                        final String target = args[2];
                                        OfflinePlayer a = null;
                                        for (OfflinePlayer p : banWave) {
                                            if (p.getName().equals(target)) {
                                                a = p;
                                                break;
                                            }
                                        }
                                        if (a == null) {
                                            sender.sendMessage(ColorUtils.MsgColor(plugin.PREFIX + "&cTarget is not in BanWave list!"));
                                            return false;
                                        }
                                        banWave.remove(a);
                                        sender.sendMessage(ColorUtils.MsgColor(plugin.PREFIX + "&aRemove successfully."));
                                    } catch (IndexOutOfBoundsException e) {
                                        sender.sendMessage(ColorUtils.MsgColor(SYNTAX));
                                        return false;
                                    }
                                    break;
                                case "list":
                                    if (!sender.hasPermission("focalors.banwave.list")) {
                                        sender.sendMessage(ColorUtils.MsgColor("&cNo permission."));
                                        return false;
                                    }
                                    if (banWave.isEmpty()) {
                                        sender.sendMessage(ColorUtils.MsgColor(plugin.PREFIX + "&bNo any player will be ban."));
                                        sender.sendMessage(ColorUtils.MsgColor(plugin.PREFIX + "&bAlready banned on this session: " + plugin.totalBanned));
                                        return false;
                                    }
                                    final StringBuilder sb = new StringBuilder(plugin.PREFIX + "&bPlayers will be ban: ");
                                    for (OfflinePlayer p : banWave) {
                                        sb.append("&7").append(p.getName());
                                        if (banWave.get(banWave.size() - 1) != p) {
                                            sb.append("&8, ");
                                        }
                                    }
                                    sb.append("\n").append(plugin.PREFIX).append("&bAlready banned on this session: ").append(plugin.totalBanned);
                                    sender.sendMessage(ColorUtils.MsgColor(sb.toString()));
                                    break;
                                case "now":
                                    if (!sender.hasPermission("focalors.banwave.now")) {
                                        sender.sendMessage(ColorUtils.MsgColor("&cNo permission."));
                                        return false;
                                    }
                                    if (Focalors.task == null) {
                                        sender.sendMessage(ColorUtils.MsgColor(plugin.PREFIX + "&cNo pending task will be execute."));
                                    } else {
                                        sender.sendMessage(ColorUtils.MsgColor(plugin.PREFIX + "&dYou executed task now."));
                                        punish.executeBanWave();
                                    }
                                    break;
                                case "clear":
                                    if (!sender.hasPermission("focalors.banwave.clear")) {
                                        sender.sendMessage(ColorUtils.MsgColor("&cNo permission."));
                                        return false;
                                    }
                                    sender.sendMessage(ColorUtils.MsgColor(plugin.PREFIX + "&aRemoved all players in BanWave."));
                                    banWave.clear();
                                    if (Focalors.task != null) {
                                        Focalors.task.cancel();
                                        Focalors.task = null;
                                    }
                                    break;
                                default:
                                    sender.sendMessage(ColorUtils.MsgColor(SYNTAX));
                                    return false;
                            }
                            return false;
                        }
                    }
                } else if ("badwordwarn".equalsIgnoreCase(args[0])) {
                    if (!sender.hasPermission("focalors.badwordwarn")) {
                        sender.sendMessage(ColorUtils.MsgColor(unknown));
                    } else {
                        if (args.length < 2) {
                            sender.sendMessage(ColorUtils.MsgColor("&cUsage: /fts badwordwarn"));
                        } else {
                            final Player targetPlayer;
                            targetPlayer = Bukkit.getPlayer(args[1]);
                            final String badwordwarnreason = ColorUtils.MsgColor("&b&l=========================================\n"
                                    + "&f&l»&eWe have blocked some of your messages\n"
                                    + "&f&l»&eThese messages may have broken our rules\n&f&l»"
                                    + "&eWe prohibit posting inappropriate comments"
                                    +"\n&b&l=========================================\n");
                            targetPlayer.sendMessage(badwordwarnreason);
                        }
                    }
                } else if ("reload".equalsIgnoreCase(args[0])) {
                    if (!sender.hasPermission("focalors.reload")) {
                        sender.sendMessage(ColorUtils.MsgColor(unknown));
                    } else {
                        if (args.length < 2) {
                            sender.sendMessage(ColorUtils.MsgColor("&cUsage: /fts reload"));
                        } else {
                            plugin.reloadConfig();
                            final FileConfiguration config = plugin.getConfig();
                            plugin.config = config;
                            plugin.PREFIX = config.getString("prefix");
                            plugin.banWaveMin = config.getInt("ban-wave.min");
                            plugin.banWaveMax = config.getInt("ban-wave.max");
                            plugin.broadcastDelay = config.getInt("ban-wave.broadcast-delay");
                            plugin.loadRegex(config);
                        }
                    }
                } /*start a new chuck*/else if ("badwordwarn".equalsIgnoreCase(args[0])) {
                    if (!sender.hasPermission("focalors.badwordwarn")) {
                        sender.sendMessage(ColorUtils.MsgColor(unknown));
                    } else {
                        if (args.length < 2) {
                            sender.sendMessage(ColorUtils.MsgColor("&cUsage: /fts badwordwarn"));
                        } else {
                            final Player targetPlayer;
                            targetPlayer = Bukkit.getPlayer(args[1]);
                            final String badwordwarnreason = ColorUtils.MsgColor("&b&l=========================================\n"
                                    + "&f&l»&eWe have blocked some of your messages\n"
                                    + "&f&l»&eThese messages may have broken our rules\n&f&l»"
                                    + "&eWe prohibit posting inappropriate comments"
                                    +"\n&b&l=========================================\n");
                            targetPlayer.sendMessage(badwordwarnreason);
                        }
                    }
                } /*end a chuck*/
            }
        } else {
                String helptext = "\n&b&lFocalors Trial System\n&7» &fUsage &b/focalors help &f to help\n&fVersion: &e" + plugin.getDescription().getVersion() + " &fBy Shizoukia\n&f&l   ";
                sender.sendMessage(ColorUtils.MsgColor(helptext));
            }

            return true;
        }

    }

