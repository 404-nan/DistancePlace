
package net.lulg.distanceplace;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;
import org.bukkit.event.*;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class PlacementListener implements Listener {
    private final DistancePlacePlugin plugin;
    public PlacementListener(DistancePlacePlugin p){plugin=p;}

    @EventHandler(priority=EventPriority.HIGH, ignoreCancelled=true)
    public void onClick(PlayerInteractEvent e){
        if(e.getHand()!=EquipmentSlot.HAND) return;
        if(e.getAction()!=Action.RIGHT_CLICK_AIR && e.getAction()!=Action.RIGHT_CLICK_BLOCK) return;

        Player p=e.getPlayer();
        ItemStack hand=p.getInventory().getItemInMainHand();
        if(hand==null||!hand.getType().isBlock()) return;

        int max=plugin.getConfig().getInt("maxDistance",30);
        List<Block> chain=p.getLastTwoTargetBlocks(null,max);
        if(chain.size()<2) return;
        Block wall=chain.get(0);
        Block air=chain.get(1);
        if(!air.getType().isAir()) return;

        BlockState before=air.getState();
        BlockPlaceEvent ev=new BlockPlaceEvent(air,before,wall,hand.clone(),p,true,EquipmentSlot.HAND);
        Bukkit.getPluginManager().callEvent(ev);
        if(ev.isCancelled()) return;

        air.setType(hand.getType(),false);
        switch(p.getGameMode()){
            case SURVIVAL,ADVENTURE -> hand.setAmount(hand.getAmount()-1);
            default -> {}
        }
    }
}
