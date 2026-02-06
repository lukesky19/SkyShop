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
package com.github.lukesky19.skyshop.database.table;

import com.github.lukesky19.skylib.api.database.parameter.impl.IntegerParameter;
import com.github.lukesky19.skylib.api.database.parameter.impl.StringParameter;
import com.github.lukesky19.skylib.api.database.queue.QueueManager;
import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * This class is used to create and interface with the versions table in the database.
 */
public class VersionsTable {
    private final @NotNull QueueManager queueManager;
    private final @NotNull String tableName = "skyprestige_versions";

    /**
     * Default Constructor.
     * You should use {@link #VersionsTable(QueueManager)} instead.
     * @deprecated You should use {@link #VersionsTable(QueueManager)} instead.
     */
    @Deprecated
    public VersionsTable() {
        throw new RuntimeException("The use of the default constructor is not allowed.");
    }

    /**
     * Constructor
     * @param queueManager A {@link QueueManager} instance.
     */
    public VersionsTable(@NotNull QueueManager queueManager) {
        this.queueManager = queueManager;
    }

    /**
     * Creates the table in the database if it doesn't exist.
     * @return A {@link CompletableFuture} of type {@link Void} when complete.
     */
    public @NotNull CompletableFuture<Void> createTable() {
        String tableCreationSql = "CREATE TABLE IF NOT EXISTS " + tableName + " (" +
                "id INTEGER PRIMARY KEY, " +
                "table_id TEXT NOT NULL UNIQUE, " +
                "version INTEGER NOT NULL)";

        return queueManager.queueWriteTransaction(tableCreationSql).thenRun(() -> {});
    }

    /**
     * Update the version number for a table id.
     * @param tableId The table id.
     * @param version The version to set.
     * @return A {@link CompletableFuture} of type {@link Void} when complete.
     */
    public @NotNull CompletableFuture<Void> updateVersion(@NotNull String tableId, int version) {
        String updateSql = "INSERT INTO " + tableName + " (table_id, version) VALUES (?, ?) ON CONFLICT (table_id) DO UPDATE SET version = ?";

        StringParameter tableIdParameter = new StringParameter(tableId);
        IntegerParameter versionParameter = new IntegerParameter(version);

        return queueManager.queueWriteTransaction(updateSql, List.of(tableIdParameter, versionParameter, versionParameter)).thenRun(() -> {});
    }

    /**
     * Get the version for the table id provided.
     * @param tableId The table id.
     * @return A {@link CompletableFuture} of type {@link Integer} containing the version number. -1 is returned for no version stored.
     */
    public @NotNull CompletableFuture<Integer> getVersion(@NotNull String tableId) {
        String readSql = "SELECT version FROM " + tableName + " WHERE table_id = ?";

        StringParameter tableIdParameter = new StringParameter(tableId);

        return queueManager.queueReadTransaction(readSql, List.of(tableIdParameter), resultSet -> {
            try {
                if(resultSet.next()) {
                    return resultSet.getInt("version");
                } else {
                    return -1; // No known version
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }
}