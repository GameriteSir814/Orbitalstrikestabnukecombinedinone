package com.orbitalstrike;

import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CannonManager {

    private final NamespacedKey cannonKey;

    // In-memory cooldown tracking - doesn't need to survive a restart.
    private final Map<UUID, Long> nukeCooldowns = new HashMap<>();
    private final Map<UUID, Long> stabCooldowns = new HashMap<>();

    public CannonManager(OrbitalStrike plugin) {
        this.cannonKey = new NamespacedKey(plugin, "is_cannon");
    }

    public boolean markItem(ItemStack item) {
        if (item == null || item.getType().isAir() || !item.hasItemMeta()) return false;
        var meta = item.getItemMeta();
        meta.getPersistentDataContainer().set(cannonKey, PersistentDataType.BYTE, (byte) 1);
        item.setItemMeta(meta);
        return true;
    }

    public boolean unmarkItem(ItemStack item) {
        if (item == null || item.getType().isAir() || !item.hasItemMeta()) return false;
        var meta = item.getItemMeta();
        if (!meta.getPersistentDataContainer().has(cannonKey, PersistentDataType.BYTE)) return false;
        meta.getPersistentDataContainer().remove(cannonKey);
        item.setItemMeta(meta);
        return true;
    }

    public boolean isCannon(ItemStack item) {
        if (item == null || item.getType().isAir() || !item.hasItemMeta()) return false;
        return item.getItemMeta().getPersistentDataContainer().has(cannonKey, PersistentDataType.BYTE);
    }

    /** Returns seconds remaining on the nuke cooldown, or 0 if ready. */
    public long nukeCooldownRemaining(UUID uuid, long cooldownSeconds) {
        return cooldownRemaining(nukeCooldowns, uuid, cooldownSeconds);
    }

    public void triggerNukeCooldown(UUID uuid) {
        nukeCooldowns.put(uuid, System.currentTimeMillis());
    }

    /** Returns seconds remaining on the stab cooldown, or 0 if ready. */
    public long stabCooldownRemaining(UUID uuid, long cooldownSeconds) {
        return cooldownRemaining(stabCooldowns, uuid, cooldownSeconds);
    }

    public void triggerStabCooldown(UUID uuid) {
        stabCooldowns.put(uuid, System.currentTimeMillis());
    }

    private long cooldownRemaining(Map<UUID, Long> map, UUID uuid, long cooldownSeconds) {
        Long last = map.get(uuid);
        if (last == null) return 0;
        long elapsedSeconds = (System.currentTimeMillis() - last) / 1000L;
        long remaining = cooldownSeconds - elapsedSeconds;
        return Math.max(0, remaining);
    }
}
