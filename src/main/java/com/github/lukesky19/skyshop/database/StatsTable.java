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
package com.github.lukesky19.skyshop.database;

import com.github.lukesky19.skylib.api.database.parameter.Parameter;
import com.github.lukesky19.skylib.api.database.parameter.impl.LongParameter;
import com.github.lukesky19.skylib.api.database.parameter.impl.NamespacedKeyParameter;
import com.github.lukesky19.skylib.api.registry.RegistryUtil;
import com.github.lukesky19.skyshop.SkyShop;
import com.github.lukesky19.skyshop.data.TransactionStats;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import org.bukkit.inventory.ItemType;
import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.CompletableFuture;

/**
 * This table manages the creation, saving, and loading of transaction statistics for {@link ItemType}s.
 */
public class StatsTable {
    private final @NotNull SkyShop skyShop;
    private final @NotNull QueueManager queueManager;
    private final @NotNull String tableName = "stats";

    /**
     * Constructor
     * @param skyShop A {@link SkyShop} instance.
     * @param queueManager A {@link QueueManager} instance.
     */
    public StatsTable(@NotNull SkyShop skyShop, @NotNull QueueManager queueManager) {
        this.skyShop = skyShop;
        this.queueManager = queueManager;
    }

    /**
     * Create the table if it doesn't exist to store transaction stats for {@link ItemType}s.
     */
    public void createTable() {
        String tableCreationSql = "CREATE TABLE IF NOT EXISTS " + tableName + " (" +
                "item_type TEXT PRIMARY KEY UNIQUE, " +
                "buy LONG NOT NULL DEFAULT 0, " +
                "sell LONG NOT NULL DEFAULT 0, " +
                "last_updated LONG NOT NULL DEFAULT 0)";
        String indexCreationSql = "CREATE INDEX IF NOT EXISTS idx_item_types ON " + tableName + "(item_type);";

        queueManager.queueBulkWriteTransaction(List.of(tableCreationSql, indexCreationSql));
    }

    /**
     * Saves a {@link Map} mapping an {@link ItemType} to {@link TransactionStats} to the database.
     * @param stats A {@link Map} mapping an {@link ItemType} to {@link TransactionStats}.
     * @return A {@link CompletableFuture} containing a {@link List} of {@link Boolean} where true if successful, otherwise false for each entry in the provided Map.
     */
    public @NotNull CompletableFuture<@NotNull List<@NotNull Boolean>> saveStats(@NotNull Map<ItemType, TransactionStats> stats) {
        String updateSql = "INSERT INTO " + tableName + " (item_type, buy, sell, last_updated) VALUES (?, ?, ?, ?) ON CONFLICT (item_type) DO UPDATE SET buy = ?, sell = ?, last_updated = ? WHERE last_updated < ?";
        Map<String, List<Parameter<?>>> sqlStatementsAndParameters = new HashMap<>();

        stats.forEach(((itemType, transactionStats) -> {
            LongParameter timestampParameter = new LongParameter(System.currentTimeMillis());
            NamespacedKeyParameter itemTypeParameter = new NamespacedKeyParameter(itemType.getKey());
            LongParameter buyParameter = new LongParameter(transactionStats.getAmountPurchased());
            LongParameter sellParameter = new LongParameter(transactionStats.getAmountSold());

            sqlStatementsAndParameters.put(updateSql, List.of(itemTypeParameter, buyParameter, sellParameter, timestampParameter, buyParameter, sellParameter, timestampParameter, timestampParameter));
        }));

        return queueManager.queueBulkWriteTransaction(sqlStatementsAndParameters).thenApply(list -> {
            List<Boolean> results = new ArrayList<>();

            list.forEach(rowsUpdated -> {
                if(rowsUpdated > 0) {
                    results.add(true);
                } else  {
                    results.add(false);
                }
            });

            return results;
        });
    }

    /**
     * Loads all stats stored in the database.
     * @return A {@link CompletableFuture} containing a {@link Map} mapping {@link ItemType} to {@link TransactionStats}.
     */
    public @NotNull CompletableFuture<@NotNull Map<@NotNull ItemType, @NotNull TransactionStats>> loadStats() {
        String selectSql = "SELECT item_type, buy, sell FROM " + tableName + " WHERE last_updated < ?";

        LongParameter timestampParameter = new LongParameter(System.currentTimeMillis());

        return queueManager.queueReadTransaction(selectSql, List.of(timestampParameter), resultSet -> {
            ComponentLogger logger = skyShop.getComponentLogger();
            Map<@NotNull ItemType, @NotNull TransactionStats> transactionStatsMap = new HashMap<>();

            try {
                while(resultSet.next()) {
                    String itemTypeName = resultSet.getString("item_type");
                    long buy = resultSet.getLong("buy");
                    long sell = resultSet.getLong("sell");

                    Optional<ItemType> optionalItemType = RegistryUtil.getItemType(logger, itemTypeName);
                    if(optionalItemType.isEmpty()) continue;

                    TransactionStats transactionStats = new TransactionStats(buy, sell);
                    transactionStatsMap.put(optionalItemType.get(), transactionStats);
                }

                return transactionStatsMap;
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }
}
