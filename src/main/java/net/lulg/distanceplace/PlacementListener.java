package net.lulg.distanceplace;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class PlacementListener implements Listener {
    private final DistancePlace plugin;
    public PlacementListener(DistancePlace p){this.plugin=p;}

    @EventHandler(priority=EventPriority.HIGH, ignoreCancelled=true)
    public void onClick(PlayerInteractEvent e){
        if(e.getHand()!=EquipmentSlot.HAND) return;
        Player p=e.getPlayer();
        if(e.getAction()==Action.LEFT_CLICK_AIR || e.getAction()==Action.LEFT_CLICK_BLOCK){
            plugin.clearTask(p);
            return;
        }
        if(e.getAction()!=Action.RIGHT_CLICK_AIR && e.getAction()!=Action.RIGHT_CLICK_BLOCK) return;
        if(!plugin.isModeOn(p)) return;
        attemptPlace(p, plugin);
        if(plugin.getSpeed(p)>=0) plugin.startAutoTask(p);
    }

    public static void attemptPlace(Player p, DistancePlace plugin){
        ItemStack hand=p.getInventory().getItemInMainHand();
        if(hand==null || !hand.getType().isBlock()) return;
        int max=plugin.getReach(p);
        List<Block> chain=p.getLastTwoTargetBlocks(null,max);
        if(chain.size()<2) return;
        Block wall=chain.get(0);
        Block air=chain.get(1);
        if(!air.getType().isAir()) return;
        if(wall.getType().isInteractable()) return;

        BlockState before=air.getState();
        BlockPlaceEvent ev=new BlockPlaceEvent(air,before,wall,hand.clone(),p,true,EquipmentSlot.HAND);
        Bukkit.getPluginManager().callEvent(ev);
        if(ev.isCancelled()) return;

        air.setType(hand.getType(),false);
        if(p.getGameMode()==GameMode.SURVIVAL || p.getGameMode()==GameMode.ADVENTURE){
            hand.setAmount(hand.getAmount()-1);
        }
    }
}
