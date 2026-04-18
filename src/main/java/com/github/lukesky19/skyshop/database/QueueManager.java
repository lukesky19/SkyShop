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

import com.github.lukesky19.skylib.common.api.database.connection.AbstractConnectionManager;
import com.github.lukesky19.skylib.common.api.database.queue.MultiThreadQueueManager;
import org.jspecify.annotations.NonNull;

/**
 * This class manages the queue for reading from and writing to the database.
 */
public class QueueManager extends MultiThreadQueueManager {
    /**
     * Constructor that takes a class that extends {@link AbstractConnectionManager}.
     * @param connectionManager A class that extends {@link AbstractConnectionManager} to use.
     */
    public QueueManager(@NonNull AbstractConnectionManager connectionManager) {
        super(connectionManager);
    }
}
