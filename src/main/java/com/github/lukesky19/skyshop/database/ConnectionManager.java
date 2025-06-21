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

import com.github.lukesky19.skylib.api.database.connection.AbstractConnectionManager;
import com.github.lukesky19.skylib.libs.hikaricp.HikariConfig;
import com.github.lukesky19.skylib.libs.hikaricp.HikariDataSource;
import com.github.lukesky19.skyshop.SkyShop;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.io.File;

/**
 * This class is used to manage connections to the database.
 */
public class ConnectionManager extends AbstractConnectionManager {
    /**
     * Constructor
     * @param skyShop A {@link SkyShop} instance.
     */
    public ConnectionManager(@NotNull SkyShop skyShop) {
        super(skyShop);
    }

    /**
     * Creates the required {@link HikariConfig} to access the database and returns the {@link HikariDataSource}.
     * @param plugin The {@link Plugin} implementing and making use of this class.
     * @return A {@link HikariDataSource} object.
     */
    @Override
    protected @NotNull HikariDataSource createHikariDataSource(@NotNull Plugin plugin) {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:sqlite:" +  plugin.getDataFolder().getAbsolutePath() + File.separator + "database.db");
        config.setAutoCommit(true);
        return new HikariDataSource(config);
    }
}
