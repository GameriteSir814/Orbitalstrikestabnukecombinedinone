package com.orbitalstrike;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

import java.util.concurrent.ThreadLocalRandom;

public class CannonListener implements Listener {

    private final OrbitalStrike plugin;
    private final CannonManager cannonManager;

    public CannonListener(OrbitalStrike plugin, CannonManager cannonManager) {
        this.plugin = plugin;
        this.cannonManager = cannonManager;
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent e) {
        // Avoid double-firing (Bukkit can fire this once per hand for some interactions).
        if (e.getHand() != EquipmentSlot.HAND) return;
        if (e.getAction() != Action.RIGHT_CLICK_AIR && e.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        ItemStack item = e.getItem();
        if (!cannonManager.isCannon(item)) return;

        // This is the marked cannon - fully replace the normal fishing rod behavior.
        // setCancelled alone isn't always enough for items with special right-click
        // behavior (fishing rods, bows, food) - explicitly deny the item-use action too.
        e.setCancelled(true);
        e.setUseItemInHand(org.bukkit.event.Event.Result.DENY);
        e.setUseInteractedBlock(org.bukkit.event.Event.Result.DENY);

        Player p = e.getPlayer();
        if (p.isSneaking()) {
            fireStab(p);
        } else {
            fireNuke(p);
        }
    }

    private void fireNuke(Player p) {
        double maxRange = plugin.getConfig().getDouble("nuke.max-range", 150);
        Location target = findAimedLocation(p, maxRange);

        int count = plugin.getConfig().getInt("nuke.tnt-count", 500);
        double spread = plugin.getConfig().getDouble("nuke.spread-radius", 20);
        double dropHeight = plugin.getConfig().getDouble("nuke.drop-height", 30);
        int fuseTicks = plugin.getConfig().getInt("nuke.fuse-ticks", 40);
        int batchSize = plugin.getConfig().getInt("nuke.spawn-batch-size", 25);

        p.sendMessage(Component.text("Orbital strike incoming...", NamedTextColor.RED));
        p.getWorld().playSound(target, Sound.ENTITY_WARDEN_SONIC_CHARGE, 3f, 0.6f);

        dropTntBarrage(p, target, count, spread, dropHeight, fuseTicks, batchSize);
    }

    private void fireStab(Player p) {
        double maxRange = plugin.getConfig().getDouble("stab.max-range", 30);
        Location target = findAimedLocation(p, maxRange);

        int count = plugin.getConfig().getInt("stab.tnt-count", 30);
        double spread = plugin.getConfig().getDouble("stab.spread-radius", 1.5);
        double dropHeight = plugin.getConfig().getDouble("stab.drop-height", 20);
        int fuseTicks = plugin.getConfig().getInt("stab.fuse-ticks", 30);
        int batchSize = plugin.getConfig().getInt("stab.spawn-batch-size", 30);

        p.getWorld().playSound(p.getLocation(), Sound.ITEM_TRIDENT_HIT, 2f, 1.4f);

        dropTntBarrage(p, target, count, spread, dropHeight, fuseTicks, batchSize);
    }

    /**
     * Spawns `count` primed TNT entities above `center`, scattered within
     * `spreadRadius` blocks of it, falling naturally under gravity. Spawning
     * is staggered across ticks (batchSize per tick) instead of all at once,
     * since instantiating hundreds of entities in a single tick is a real
     * lag/crash risk, especially on a free/shared host.
     */
    private void dropTntBarrage(Player source, Location center, int count, double spreadRadius,
                                 double dropHeight, int fuseTicks, int batchSize) {
        World world = center.getWorld();
        if (world == null) return;

        new org.bukkit.scheduler.BukkitRunnable() {
            int spawnedCount = 0;

            @Override
            public void run() {
                int toSpawn = Math.min(batchSize, count - spawnedCount);
                for (int i = 0; i < toSpawn; i++) {
                    double offsetX = (ThreadLocalRandom.current().nextDouble() * 2 - 1) * spreadRadius;
                    double offsetZ = (ThreadLocalRandom.current().nextDouble() * 2 - 1) * spreadRadius;
                    Location spawnAt = center.clone().add(offsetX, dropHeight, offsetZ);

                    TNTPrimed tnt = world.spawn(spawnAt, TNTPrimed.class);
                    tnt.setFuseTicks(fuseTicks);
                    tnt.setSource(source); // so kills/credit correctly attribute to the player who fired
                }
                spawnedCount += toSpawn;

                if (spawnedCount >= count) {
                    this.cancel();
                }
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }

    /** Finds where the player is aiming: the block they're looking at, or a point in open space if nothing's hit. */
    private Location findAimedLocation(Player p, double maxRange) {
        RayTraceResult blockHit = p.rayTraceBlocks(maxRange);
        if (blockHit != null && blockHit.getHitPosition() != null) {
            return blockHit.getHitPosition().toLocation(p.getWorld());
        }
        Vector direction = p.getEyeLocation().getDirection().normalize().multiply(maxRange);
        return p.getEyeLocation().add(direction);
    }
}
