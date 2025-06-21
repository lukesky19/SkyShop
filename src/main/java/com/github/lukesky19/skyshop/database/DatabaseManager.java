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

import com.github.lukesky19.skylib.api.database.AbstractDatabaseManager;
import com.github.lukesky19.skyshop.SkyShop;
import org.jetbrains.annotations.NotNull;

/**
 * This class manages access to database tables, in this case just {@link StatsTable}.
 */
public class DatabaseManager extends AbstractDatabaseManager {
    private final @NotNull StatsTable statsTable;

    /**
     * Get the {@link StatsTable} table.
     * @return A {@link StatsTable}
     */
    public @NotNull StatsTable getStatsTable() {
        return statsTable;
    }

    /**
     * Constructor
     * Initializes the {@link ConnectionManager}, {@link QueueManager}, and any tables.
     * @param skyShop A {@link SkyShop instance.}
     * @param connectionManager A {@link ConnectionManager} instance.
     * @param queueManager A {@link QueueManager} instance.
     */
    public DatabaseManager(@NotNull SkyShop skyShop, @NotNull ConnectionManager connectionManager, @NotNull QueueManager queueManager) {
        super(connectionManager, queueManager);

        statsTable = new StatsTable(skyShop, queueManager);
        statsTable.createTable();
    }
}