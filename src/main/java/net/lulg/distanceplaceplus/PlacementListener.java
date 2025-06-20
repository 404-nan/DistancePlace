package net.lulg.distanceplaceplus;

import org.bukkit.Bukkit;
import org.bukkit.Material;
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

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

public class PlacementListener implements Listener {

    private final DistancePlacePlusPlugin plugin;
    private static final Set<Material> FORBIDDEN = EnumSet.of(
            Material.CHEST, Material.TRAPPED_CHEST, Material.ENDER_CHEST,
            Material.OAK_SIGN, Material.OAK_WALL_SIGN,
            Material.SPRUCE_SIGN, Material.SPRUCE_WALL_SIGN,
            Material.BIRCH_SIGN, Material.BIRCH_WALL_SIGN,
            Material.JUNGLE_SIGN, Material.JUNGLE_WALL_SIGN,
            Material.ACACIA_SIGN, Material.ACACIA_WALL_SIGN,
            Material.DARK_OAK_SIGN, Material.DARK_OAK_WALL_SIGN,
            Material.CRIMSON_SIGN, Material.CRIMSON_WALL_SIGN,
            Material.WARPED_SIGN, Material.WARPED_WALL_SIGN,
            Material.BREWING_STAND, Material.ANVIL,
            Material.CHIPPED_ANVIL, Material.DAMAGED_ANVIL
    );

    public PlacementListener(DistancePlacePlusPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onInteract(PlayerInteractEvent e) {
        if (e.getHand() != EquipmentSlot.HAND) return;
        if (e.getAction() != Action.RIGHT_CLICK_AIR && e.getAction() != Action.RIGHT_CLICK_BLOCK)
            return;

        Player player = e.getPlayer();
        DistancePlacePlusPlugin.PlayerSettings ps = plugin.getSettings(player);
        ps.lastInteract = System.currentTimeMillis();

        if (!ps.mode) return;

        e.setCancelled(true);
        attemptPlace(player, ps.distance);
    }

    static void attemptPlace(Player p, int max) {
        ItemStack hand = p.getInventory().getItemInMainHand();
        if (hand == null || hand.getType() == Material.AIR || !hand.getType().isBlock()) return;

        List<Block> chain = p.getLastTwoTargetBlocks(null, max);
        if (chain.size() < 2) return;
        Block wall = chain.get(0);
        Block air = chain.get(1);

        if (!air.getType().isAir()) return;
        if (FORBIDDEN.contains(wall.getType())) return;

        BlockState before = air.getState();
        BlockPlaceEvent ev = new BlockPlaceEvent(air, before, wall, hand.clone(), p, true, EquipmentSlot.HAND);
        Bukkit.getPluginManager().callEvent(ev);
        if (ev.isCancelled()) return;

        air.setType(hand.getType(), false);
        switch (p.getGameMode()) {
            case SURVIVAL, ADVENTURE -> hand.setAmount(hand.getAmount() - 1);
            default -> {
            }
        }
    }
}
