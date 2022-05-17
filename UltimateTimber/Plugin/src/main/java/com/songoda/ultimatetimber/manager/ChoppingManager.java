package com.songoda.ultimatetimber.manager;

import com.songoda.core.compatibility.CompatibleHand;
import com.songoda.ultimatetimber.UltimateTimber;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class ChoppingManager extends Manager {

    private final Set<UUID> disabledPlayers;
    private final Map<UUID, Boolean> cooldownedPlayers;
    private boolean useCooldown;
    private int cooldownAmount;


    public ChoppingManager(UltimateTimber ultimateTimber) {
        super(ultimateTimber);
        this.disabledPlayers = new HashSet<>();
        this.cooldownedPlayers = new HashMap<>();
    }

    @Override
    public void reload() {
        this.useCooldown = ConfigurationManager.Setting.PLAYER_TREE_TOPPLE_COOLDOWN.getBoolean();
        this.cooldownAmount = ConfigurationManager.Setting.PLAYER_TREE_TOPPLE_COOLDOWN_LENGTH.getInt();
    }

    @Override
    public void disable() {
        this.disabledPlayers.clear();
        this.cooldownedPlayers.clear();
    }

    /**
     * Toggles a player's chopping status
     *
     * @param player The player to toggle
     * @return True if the player has chopping enabled, or false if they have it disabled
     */
    public boolean togglePlayer(Player player) {
        if (this.disabledPlayers.contains(player.getUniqueId())) {
            this.disabledPlayers.remove(player.getUniqueId());
            return true;
        } else {
            this.disabledPlayers.add(player.getUniqueId());
            return false;
        }
    }

    /**
     * Checks if a player has chopping enabled
     *
     * @param player The player to check
     * @return True if the player has chopping enabled, or false if they have it disabled
     */
    public boolean isChopping(Player player) {
        return !this.disabledPlayers.contains(player.getUniqueId());
    }

    /**
     * Sets a player into cooldown
     *
     * @param player The player to cooldown
     * @param tool
     */
    public void cooldownPlayer(Player player, ItemStack tool) {
        if (!this.useCooldown || player.hasPermission("ultimatetimber.bypasscooldown"))
            return;

        TreeDefinitionManager treeDefinitionManager = UltimateTimber.getInstance().getTreeDefinitionManager();

        this.cooldownedPlayers.put(player.getUniqueId(), false);

        int cooldownAmount = -1;

        if (treeDefinitionManager.isOverrideTreeToppleCooldown(tool)) {
            cooldownAmount = treeDefinitionManager.getAxeTreeToppleCooldown(tool);
        }

        if (cooldownAmount < 0) {
            cooldownAmount = this.cooldownAmount;
        }

        Bukkit.getScheduler().scheduleSyncDelayedTask(UltimateTimber.getInstance(), () ->
                this.cooldownedPlayers.remove(player.getUniqueId()), cooldownAmount * 20L);
    }

    /**
     * Checks if a player is in cooldown
     *
     * @param player The player to check
     * @return True if the player can topple trees, otherwise false
     */
    public boolean isInCooldown(Player player) {
        boolean cooldowned = this.useCooldown && this.cooldownedPlayers.containsKey(player.getUniqueId());
        if (cooldowned && !this.cooldownedPlayers.get(player.getUniqueId())) {
            this.plugin.getLocale().getMessage("event.on.cooldown").sendPrefixedMessage(player);
            this.cooldownedPlayers.replace(player.getUniqueId(), true);
        }
        return cooldowned;
    }

}
