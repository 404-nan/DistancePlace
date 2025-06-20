
package net.lulg.distanceplace;

import org.bukkit.command.*;
import org.bukkit.plugin.java.JavaPlugin;
import java.util.List;

public class DistancePlacePlugin extends JavaPlugin implements TabCompleter, CommandExecutor {

    @Override
    public void onEnable(){
        saveDefaultConfig();
        getServer().getPluginManager().registerEvents(new PlacementListener(this), this);
        PluginCommand cmd = getCommand("distanceplace");
        if(cmd != null){
            cmd.setExecutor(this);
            cmd.setTabCompleter(this);
        }
    }

    /* Command: /distanceplace setdistance <n> | help */
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args){
        if(!sender.hasPermission("distanceplace.admin")){
            sender.sendMessage("§cYou don't have permission.");
            return true;
        }
        if(args.length == 0 || args[0].equalsIgnoreCase("help")){
            sender.sendMessage("§eUsage: /distanceplace setdistance <blocks>");
            sender.sendMessage("§7Current distance: §a" + getConfig().getInt("maxDistance"));
            return true;
        }
        if(args[0].equalsIgnoreCase("setdistance") && args.length == 2){
            try{
                int dist = Integer.parseInt(args[1]);
                getConfig().set("maxDistance", dist);
                saveConfig();
                sender.sendMessage("§aMax distance set to " + dist);
            }catch(NumberFormatException ex){
                sender.sendMessage("§cNot a number.");
            }
            return true;
        }
        sender.sendMessage("§cUnknown sub-command. /distanceplace help");
        return true;
    }

    /* Tab completions */
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args){
        if(args.length == 1) return List.of("setdistance","help");
        if(args.length == 2 && args[0].equalsIgnoreCase("setdistance"))
            return List.of("30","50","100");
        return List.of();
    }
}
