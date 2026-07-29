package com.orbitalstrike;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class CannonCommand implements CommandExecutor {

    private final CannonManager cannonManager;

    public CannonCommand(CannonManager cannonManager) {
        this.cannonManager = cannonManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player p)) { sender.sendMessage("Players only."); return true; }
        if (!p.isOp()) { p.sendMessage(Component.text("Only operators can do this.", NamedTextColor.RED)); return true; }

        if (args.length < 1) {
            p.sendMessage(Component.text("Usage: /osc <add|remove>", NamedTextColor.RED));
            return true;
        }

        ItemStack hand = p.getInventory().getItemInMainHand();
        if (hand.getType() != org.bukkit.Material.FISHING_ROD) {
            p.sendMessage(Component.text("Hold a fishing rod first.", NamedTextColor.RED));
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "add" -> {
                cannonManager.markItem(hand);
                p.sendMessage(Component.text("This fishing rod is now the orbital strike cannon.", NamedTextColor.GREEN));
                p.sendMessage(Component.text("Right-click = orbital nuke. Shift+right-click = stab.", NamedTextColor.GRAY));
            }
            case "remove" -> {
                if (cannonManager.unmarkItem(hand)) {
                    p.sendMessage(Component.text("This rod is now a normal fishing rod again.", NamedTextColor.YELLOW));
                } else {
                    p.sendMessage(Component.text("This rod isn't marked as the cannon.", NamedTextColor.RED));
                }
            }
            default -> p.sendMessage(Component.text("Usage: /osc <add|remove>", NamedTextColor.RED));
        }
        return true;
    }
}
