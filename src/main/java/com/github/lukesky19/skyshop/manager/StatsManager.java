/*
    SkyShop is a simple inventory based shop plugin with page support, sell commands, and error checking.
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
package com.github.lukesky19.skyshop.manager;

import com.github.lukesky19.skylib.api.adventure.AdventureUtil;
import com.github.lukesky19.skylib.api.format.FormatUtil;
import com.github.lukesky19.skyshop.data.TransactionStats;
import com.github.lukesky19.skyshop.database.DatabaseManager;
import com.github.lukesky19.skyshop.database.StatsTable;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import org.bukkit.inventory.ItemType;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * This class manages statistics for the amount an {@link ItemType} has been purchased or sold.
 */
public class StatsManager {
    private final @NotNull ComponentLogger logger;
    private final @NotNull DatabaseManager databaseManager;
    private final @NotNull Map<ItemType, TransactionStats> statsMap = new HashMap<>();

    /**
     * Default Constructor
     * You should not use this constructor and instead use {@link StatsManager#StatsManager(ComponentLogger, DatabaseManager)}.
     */
    @Deprecated
    public StatsManager() {
        throw new RuntimeException("The use of the default constructor is not allowed.");
    }

    /**
     * Constructor
     * @param logger The plugin's {@link ComponentLogger}.
     * @param databaseManager A {@link DatabaseManager} instance.
     */
    public StatsManager(@NotNull ComponentLogger logger, @NotNull DatabaseManager databaseManager) {
        this.logger = logger;
        this.databaseManager = databaseManager;
    }

    /**
     * Get the {@link Map} mapping {@link ItemType}s to {@link TransactionStats}.
     * @return A {@link Map} mapping {@link ItemType}s to {@link TransactionStats}
     */
    public @NotNull Map<ItemType, TransactionStats> getStatsMap() {
        return statsMap;
    }

    /**
     * Loads all stats from the database.
     * Running this method twice will result in data loss.
     */
    public void loadStats() {
        StatsTable statsTable = databaseManager.getStatsTable();

        statsMap.clear();

        statsTable.loadStats().thenAccept(statsMap::putAll);
    }

    /**
     * Saves all stats from the database.
     * @return A {@link CompletableFuture} containing a {@link List} of {@link Boolean}s. The list will contain false if any data failed to save.
     */
    public @NotNull CompletableFuture<@NotNull List<@NotNull Boolean>> saveStats() {
        StatsTable statsTable = databaseManager.getStatsTable();

        return statsTable.saveStats(statsMap);
    }

    /**
     * Get the stats associated with the {@link ItemType}.
     * @param itemType The {@link ItemType} to get stats for.
     * @return An {@link Optional} containing {@link TransactionStats}.
     * If no stats exist for the {@link ItemType}, the {@link Optional} will be empty.
     */
    public @NotNull Optional<TransactionStats> getTransactionStats(@NotNull ItemType itemType) {
        return Optional.ofNullable(statsMap.get(itemType));
    }

    /**
     * Increments the amount of items purchased for an {@link ItemType}.
     * @param itemType The {@link ItemType}
     * @param incrementAmount The amount of items purchased.
     */
    public void incrementAmountPurchased(@NotNull ItemType itemType, long incrementAmount) {
        if(incrementAmount <= 0) {
            logger.warn(AdventureUtil.serialize("Unable to increment the amount purchased for " + FormatUtil.formatItemTypeName(itemType) + ". The increment amount must be greater than 0."));
            return;
        }

        Optional<TransactionStats> optionalTransactionStats = getTransactionStats(itemType);
        TransactionStats transactionStats;
        if(optionalTransactionStats.isPresent()) {
            transactionStats = optionalTransactionStats.get();

            transactionStats.incrementAmountPurchased(incrementAmount);
        } else {
            transactionStats = new TransactionStats(incrementAmount, 0);

            statsMap.put(itemType, transactionStats);
        }
    }

    /**
     * Increments the amount of items sold for an {@link ItemType}.
     * @param itemType The {@link ItemType}
     * @param incrementAmount The amount of items sold.
     */
    public void incrementAmountSold(@NotNull ItemType itemType, long incrementAmount) {
        if(incrementAmount <= 0) {
            logger.warn(AdventureUtil.serialize("Unable to increment the amount sold for " + FormatUtil.formatItemTypeName(itemType) + ". The increment amount must be greater than 0."));
            return;
        }

        Optional<TransactionStats> optionalTransactionStats = getTransactionStats(itemType);
        TransactionStats transactionStats;
        if(optionalTransactionStats.isPresent()) {
            transactionStats = optionalTransactionStats.get();

            transactionStats.incrementAmountSold(incrementAmount);
        } else {
            transactionStats = new TransactionStats(0, incrementAmount);

            statsMap.put(itemType, transactionStats);
        }
    }
}
