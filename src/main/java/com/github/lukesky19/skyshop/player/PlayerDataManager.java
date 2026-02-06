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
package com.github.lukesky19.skyshop.player;

import com.github.lukesky19.skyshop.SkyShop;
import com.github.lukesky19.skyshop.database.DatabaseManager;
import com.github.lukesky19.skyshop.database.table.PlayerDataTable;
import com.github.lukesky19.skyshop.player.data.PlayerData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * This class manages {@link PlayerData}.
 */
public class PlayerDataManager {
    private final @NotNull SkyShop skyShop;
    private final @NotNull PlayerDataTable playerDataTable;
    private final @NotNull Map<UUID, PlayerData> playerDataMap = new HashMap<>();

    /**
     * Constructor
     * @param skyShop A {@link SkyShop} instance.
     * @param databaseManager A {@link DatabaseManager} instance.
     */
    public PlayerDataManager(
            @NotNull SkyShop skyShop,
            @NotNull DatabaseManager databaseManager) {
        this.skyShop = skyShop;
        this.playerDataTable = databaseManager.getPlayerDataTable();
    }

    /**
     * Get the {@link PlayerData} for the player id.
     * @param playerId The player's {@link UUID}.
     * @return The {@link PlayerData} or null.
     */
    public @Nullable PlayerData getPlayerData(@NotNull UUID playerId) {
        return playerDataMap.get(playerId);
    }

    /**
     * Get the {@link Map} mapping {@link UUID}s to {@link PlayerData}.
     * @return The {@link Map} mapping {@link UUID}s to {@link PlayerData}.
     */
    public @NotNull Map<UUID, PlayerData> getPlayerData() {
        return playerDataMap;
    }

    /**
     * Load the player data for the player id.
     * @param playerId The player's {@link UUID}.
     */
    public void loadPlayerData(@NotNull UUID playerId) {
        @NotNull CompletableFuture<PlayerData> future = playerDataTable.loadPlayerData(playerId, new PlayerData());
        future.thenAccept(playerData -> playerDataMap.put(playerId, playerData));
    }

    /**
     * Load the player data for all online players.
     */
    public void loadPlayerData() {
        skyShop.getServer().getOnlinePlayers().forEach(player -> loadPlayerData(player.getUniqueId()));
    }

    /**
     * Save and unload the player data for the player.
     * @param playerId The player's {@link UUID}.
     * @return A {@link CompletableFuture} of type {@link Void} when complete.
     */
    public @NotNull CompletableFuture<Void> unloadPlayerData(@NotNull UUID playerId) {
        @Nullable PlayerData playerData = playerDataMap.get(playerId);
        if(playerData == null) return CompletableFuture.completedFuture(null);

        return playerDataTable.savePlayerData(playerId, playerData).thenAccept(v -> playerDataMap.remove(playerId));
    }

    /**
     * Save and unload all player data.
     * @return A {@link CompletableFuture} of type {@link Void} when complete.
     */
    public @NotNull CompletableFuture<Void> unloadPlayerData() {
        return playerDataTable.savePlayerData(playerDataMap).thenAccept(results -> playerDataMap.clear());
    }
}