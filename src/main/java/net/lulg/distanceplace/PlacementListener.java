package net.lulg.distanceplace;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Event;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class PlacementListener implements Listener {
    private final DistancePlace plugin;
    public PlacementListener(DistancePlace p){this.plugin=p;}

    // Bukkit fires RIGHT_CLICK_AIR / LEFT_CLICK_AIR already "cancelled", so ignoreCancelled=true
    // would drop every click into the air (the main use case). Check useItemInHand instead.
    @EventHandler(priority=EventPriority.HIGH)
    public void onClick(PlayerInteractEvent e){
        if(e.getHand()!=EquipmentSlot.HAND) return;
        if(e.useItemInHand()==Event.Result.DENY) return;
        Player p=e.getPlayer();
        if(e.getAction()==Action.LEFT_CLICK_AIR || e.getAction()==Action.LEFT_CLICK_BLOCK){
            plugin.clearTask(p);
            return;
        }
        if(e.getAction()!=Action.RIGHT_CLICK_AIR && e.getAction()!=Action.RIGHT_CLICK_BLOCK) return;
        if(!plugin.isModeOn(p)) return;
        if(attemptPlace(p, plugin)){
            // We already placed the block; stop vanilla from placing/consuming a second one.
            e.setUseItemInHand(Event.Result.DENY);
            e.setUseInteractedBlock(Event.Result.DENY);
        }
        if(plugin.getSpeed(p)>=0) plugin.startAutoTask(p);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e){
        plugin.forget(e.getPlayer());
    }

    public static boolean attemptPlace(Player p, DistancePlace plugin){
        ItemStack hand=p.getInventory().getItemInMainHand();
        if(hand==null || hand.getAmount()<=0 || !hand.getType().isBlock() || hand.getType().isAir()) return false;
        int max=plugin.getReach(p);
        // The target block is the LAST element; the one before it is the air block in front of it.
        List<Block> chain=p.getLastTwoTargetBlocks(null,max);
        if(chain.size()<2) return false;
        Block before=chain.get(0);
        Block target=chain.get(1);
        Block air;
        Block wall;
        if(target.getType().isAir()){
            // Nothing within reach: place in mid-air at the reach distance (previous behaviour).
            if(!plugin.isAirPlaceEnabled()) return false;
            air=target;
            wall=before;
        }else{
            // Place against the block being looked at, like vanilla but from further away.
            if(!before.getType().isAir()) return false;
            if(target.getType().isInteractable()) return false;
            air=before;
            wall=target;
        }

        BlockState replaced=air.getState();
        BlockPlaceEvent ev=new BlockPlaceEvent(air,replaced,wall,hand.clone(),p,true,EquipmentSlot.HAND);
        Bukkit.getPluginManager().callEvent(ev);
        if(ev.isCancelled() || !ev.canBuild()) return false;

        air.setType(hand.getType(),false);
        if(p.getGameMode()==GameMode.SURVIVAL || p.getGameMode()==GameMode.ADVENTURE){
            hand.setAmount(hand.getAmount()-1);
        }
        return true;
    }
}
