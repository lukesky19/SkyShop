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
package com.github.lukesky19.skyshop.database;

import com.github.lukesky19.skylib.common.api.database.AbstractDatabaseManager;
import com.github.lukesky19.skyshop.SkyShop;
import com.github.lukesky19.skyshop.database.table.PlayerDataTable;
import com.github.lukesky19.skyshop.database.table.StatsTable;
import com.github.lukesky19.skyshop.database.table.VersionsTable;
import org.jspecify.annotations.NonNull;

/**
 * This class manages access to database tables, in this case just {@link StatsTable}.
 */
public class DatabaseManager extends AbstractDatabaseManager {
    private final @NonNull StatsTable statsTable;
    private final @NonNull PlayerDataTable playerDataTable;

    /**
     * Get the {@link StatsTable} table.
     * @return A {@link StatsTable}
     */
    public @NonNull StatsTable getStatsTable() {
        return statsTable;
    }

    /**
     * Get the {@link PlayerDataTable} table.
     * @return A {@link PlayerDataTable}
     */
    public @NonNull PlayerDataTable getPlayerDataTable() {
        return playerDataTable;
    }

    /**
     * Constructor
     * Initializes the {@link ConnectionManager}, {@link QueueManager}, and any tables.
     * @param skyShop A {@link SkyShop instance.}
     * @param connectionManager A {@link ConnectionManager} instance.
     * @param queueManager A {@link QueueManager} instance.
     */
    public DatabaseManager(@NonNull SkyShop skyShop, @NonNull ConnectionManager connectionManager, @NonNull QueueManager queueManager) {
        super(connectionManager, queueManager);

        VersionsTable versionsTable = new VersionsTable(queueManager);
        versionsTable.createTable();

        statsTable = new StatsTable(skyShop, queueManager, versionsTable);
        statsTable.createTable();

        playerDataTable = new PlayerDataTable(queueManager, versionsTable);
        playerDataTable.createTable();
    }
}