package net.lulg.distanceplaceplus;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.StringUtil;

import java.util.*;

public class DistancePlacePlusPlugin extends JavaPlugin implements CommandExecutor, TabCompleter {

    static class PlayerSettings {
        int distance = 30;
        int speed = 0; // ms, 0 = off
        boolean mode = false;
        long lastInteract = 0;
        long lastPlace = 0;
    }

    private final Map<UUID, PlayerSettings> settings = new HashMap<>();
    private BukkitTask loopTask;

    public PlayerSettings getSettings(Player p) {
        return settings.computeIfAbsent(p.getUniqueId(), k -> new PlayerSettings());
    }

    @Override
    public void onEnable() {
        saveDefaultConfig();
        getServer().getPluginManager().registerEvents(new PlacementListener(this), this);
        PluginCommand cmd = getCommand("reach");
        if (cmd != null) {
            cmd.setExecutor(this);
            cmd.setTabCompleter(this);
        }
        loopTask = Bukkit.getScheduler().runTaskTimer(this, () -> {
            long now = System.currentTimeMillis();
            for (Player p : Bukkit.getOnlinePlayers()) {
                PlayerSettings ps = getSettings(p);
                if (!ps.mode || ps.speed <= 0) continue;
                if (now - ps.lastInteract > 300) continue; // not holding
                if (now - ps.lastPlace < ps.speed) continue;
                PlacementListener.attemptPlace(p, ps.distance);
                ps.lastPlace = now;
            }
        }, 1L, 1L);
    }

    @Override
    public void onDisable() {
        if (loopTask != null) loopTask.cancel();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Only players");
            return true;
        }
        Player p = (Player) sender;
        PlayerSettings ps = getSettings(p);
        if (args.length == 0) {
            sender.sendMessage("/reach <blocks|status|reset|speed|mode>");
            return true;
        }
        switch (args[0].toLowerCase()) {
            case "reset":
                ps.distance = 30;
                sender.sendMessage("Distance reset to 30");
                break;
            case "status":
                sender.sendMessage("Mode: " + (ps.mode ? "ON" : "OFF") + ", Distance: " + ps.distance + ", Speed: " + (ps.speed <= 0 ? "OFF" : ps.speed + "ms"));
                break;
            case "speed":
                if (args.length == 1 || args[1].equalsIgnoreCase("status")) {
                    sender.sendMessage("Speed: " + (ps.speed <= 0 ? "OFF" : ps.speed + "ms"));
                } else if (args[1].equalsIgnoreCase("off")) {
                    ps.speed = 0;
                    sender.sendMessage("Auto place OFF");
                } else {
                    try {
                        int ms = Integer.parseInt(args[1]);
                        if (ms < 50) ms = 50;
                        ps.speed = ms;
                        sender.sendMessage("Auto place speed set to " + ms + "ms");
                    } catch (NumberFormatException e) {
                        sender.sendMessage("Not a number");
                    }
                }
                break;
            case "mode":
                if (args.length == 1 || args[1].equalsIgnoreCase("status")) {
                    sender.sendMessage("Mode: " + (ps.mode ? "ON" : "OFF"));
                } else if (args[1].equalsIgnoreCase("on")) {
                    ps.mode = true;
                    sender.sendMessage("Distance place mode ON");
                } else if (args[1].equalsIgnoreCase("off")) {
                    ps.mode = false;
                    sender.sendMessage("Distance place mode OFF");
                } else {
                    sender.sendMessage("/reach mode <on|off|status>");
                }
                break;
            default:
                try {
                    int dist = Integer.parseInt(args[0]);
                    ps.distance = dist;
                    sender.sendMessage("Distance set to " + dist);
                } catch (NumberFormatException e) {
                    sender.sendMessage("Usage: /reach <blocks|reset|status|speed|mode>");
                }
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return StringUtil.copyPartialMatches(args[0], Arrays.asList("reset", "status", "speed", "mode"), new ArrayList<>());
        }
        if (args.length >= 2 && args[0].equalsIgnoreCase("speed")) {
            if (args.length == 2) {
                return StringUtil.copyPartialMatches(args[1], Arrays.asList("off", "status", "100", "200"), new ArrayList<>());
            }
        }
        if (args.length >= 2 && args[0].equalsIgnoreCase("mode")) {
            if (args.length == 2) {
                return StringUtil.copyPartialMatches(args[1], Arrays.asList("on", "off", "status"), new ArrayList<>());
            }
        }
        return Collections.emptyList();
    }
}
