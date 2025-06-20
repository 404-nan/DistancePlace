package net.lulg.distanceplace;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;

public class DistancePlace extends JavaPlugin implements CommandExecutor, TabCompleter {
    private static DistancePlace instance;

    private int defaultReach;
    private final Map<UUID, Integer> reachMap = new HashMap<>();
    private final Map<UUID, Integer> speedMap = new HashMap<>(); // ms
    private final Map<UUID, Boolean> modeMap = new HashMap<>();
    private final Map<UUID, BukkitTask> taskMap = new HashMap<>();

    @Override
    public void onLoad() {
        instance = this;
    }

    public static DistancePlace getInstance(){
        return instance;
    }

    @Override
    public void onEnable() {
        saveDefaultConfig();
        defaultReach = getConfig().getInt("maxDistance", 30);
        getServer().getPluginManager().registerEvents(new PlacementListener(this), this);
        PluginCommand cmd = getCommand("reach");
        if (cmd != null) {
            cmd.setExecutor(this);
            cmd.setTabCompleter(this);
        }
    }

    @Override
    public void onDisable() {
        for (BukkitTask task : taskMap.values()) {
            task.cancel();
        }
        taskMap.clear();
        org.bukkit.event.HandlerList.unregisterAll(this);
    }

    /* getters / setters */
    public int getReach(Player p) {
        return reachMap.getOrDefault(p.getUniqueId(), defaultReach);
    }
    public void setReach(Player p, int v) { reachMap.put(p.getUniqueId(), v); }
    public void resetReach(Player p) { reachMap.remove(p.getUniqueId()); }

    public int getSpeed(Player p) { return speedMap.getOrDefault(p.getUniqueId(), -1); }
    public void setSpeed(Player p, int ms) { speedMap.put(p.getUniqueId(), ms); }

    public boolean isModeOn(Player p) { return modeMap.getOrDefault(p.getUniqueId(), true); }
    public void setMode(Player p, boolean on) { modeMap.put(p.getUniqueId(), on); }

    public BukkitTask getTask(Player p) { return taskMap.get(p.getUniqueId()); }
    public void setTask(Player p, BukkitTask t) { taskMap.put(p.getUniqueId(), t); }
    public void clearTask(Player p){
        BukkitTask task = taskMap.remove(p.getUniqueId());
        if(task != null) task.cancel();
    }

    private int msToTicks(int ms){
        return Math.max(1, ms / 50);
    }

    /* command handler */
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if(!(sender instanceof Player)){ sender.sendMessage("Only players"); return true; }
        Player p = (Player) sender;
        if(args.length == 0){
            sender.sendMessage("§eUsage: /reach <blocks>|reset|status|speed|mode");
            return true;
        }
        switch(args[0].toLowerCase()){
            case "reset" -> { resetReach(p); sender.sendMessage("§aReach reset to default"); return true; }
            case "status" -> { sendStatus(p); return true; }
            case "speed" -> { return handleSpeed(p, Arrays.copyOfRange(args,1,args.length)); }
            case "mode" -> { return handleMode(p, Arrays.copyOfRange(args,1,args.length)); }
            default -> {
                try{
                    int d = Integer.parseInt(args[0]);
                    setReach(p, d);
                    sender.sendMessage("§aReach set to " + d + " blocks");
                }catch(NumberFormatException ex){
                    sender.sendMessage("§cNot a number");
                }
                return true;
            }
        }
    }

    private boolean handleSpeed(Player p, String[] args){
        if(args.length==0){ p.sendMessage("§eUsage: /reach speed <ms>|off|status"); return true; }
        switch(args[0].toLowerCase()){
            case "off" -> { setSpeed(p, -1); clearTask(p); p.sendMessage("§aAuto place off"); }
            case "status" -> {
                int s=getSpeed(p); p.sendMessage("§7Speed: §a"+(s<0?"off":s+"ms"));
            }
            default -> {
                try{
                    int ms=Integer.parseInt(args[0]);
                    setSpeed(p, ms);
                    p.sendMessage("§aAuto place speed set to "+ms+"ms");
                }catch(NumberFormatException ex){
                    p.sendMessage("§cNot a number");
                }
            }
        }
        return true;
    }

    private boolean handleMode(Player p, String[] args){
        if(args.length==0){ p.sendMessage("§eUsage: /reach mode on|off|status"); return true; }
        switch(args[0].toLowerCase()){
            case "on" -> { setMode(p,true); p.sendMessage("§aMode ON"); }
            case "off" -> { setMode(p,false); p.sendMessage("§aMode OFF"); }
            case "status" -> p.sendMessage("§7Mode: §a"+(isModeOn(p)?"on":"off"));
            default -> p.sendMessage("§cUnknown");
        }
        return true;
    }

    private void sendStatus(Player p){
        p.sendMessage("§7Reach: §a"+getReach(p));
        int s=getSpeed(p); p.sendMessage("§7Speed: §a"+(s<0?"off":s+"ms"));
        p.sendMessage("§7Mode: §a"+(isModeOn(p)?"on":"off"));
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        if(args.length==1) return List.of("reset","status","speed","mode","30","50","100");
        if(args.length==2 && args[0].equalsIgnoreCase("speed")) return List.of("off","100","200","500","status");
        if(args.length==2 && args[0].equalsIgnoreCase("mode")) return List.of("on","off","status");
        return List.of();
    }

    /* helper to start task */
    public void startAutoTask(Player p){
        clearTask(p);
        int ms=getSpeed(p);
        if(ms<0) return;
        BukkitTask task=getServer().getScheduler().runTaskTimer(this,()->{
            PlacementListener.attemptPlace(p,this);
        },msToTicks(ms),msToTicks(ms));
        setTask(p,task);
    }
}
