/*
    SkyShop is a GUI shop plugin with sell commands, a sell GUI, nested categories, page support, and error checking.
    Copyright (C) 2024 lukeskywlker19

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as published
    by the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <https://www.gnu.org/licenses/>.
*/
package com.github.lukesky19.skyshop.listener;

import com.github.lukesky19.skylib.common.api.adventure.AdventureUtility;
import com.github.lukesky19.skyshop.SkyShop;
import com.github.lukesky19.skyshop.player.PlayerDataManager;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jspecify.annotations.NonNull;

/**
 * This class listens for when a player quits and then saves and unloads their player data.
 */
public class PlayerQuitListener implements Listener {
    private final @NonNull ComponentLogger logger;
    private final @NonNull PlayerDataManager playerDataManager;

    /**
     * Constructor
     * @param skyShop A {@link SkyShop} instance.
     * @param playerDataManager A {@link PlayerDataManager} instance.
     */
    public PlayerQuitListener(@NonNull SkyShop skyShop, @NonNull PlayerDataManager playerDataManager) {
        this.logger = skyShop.getComponentLogger();
        this.playerDataManager = playerDataManager;
    }

    /**
     * When a player quits, save and unload their player data.
     * @param playerQuitEvent A {@link PlayerQuitEvent}.
     */
    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onPlayerQuit(PlayerQuitEvent playerQuitEvent) {
        Player player = playerQuitEvent.getPlayer();

        playerDataManager.unloadPlayerData(player.getUniqueId()).thenAccept(_ ->
                logger.info(AdventureUtility.plain("Saved player data for player " + player.getName())))
                .exceptionally(ex -> {
                    logger.warn(AdventureUtility.plain("Failed to save player data for player " + player.getName() + ". Error: " + ex.getMessage()));
                    return null;
                });
    }
}
