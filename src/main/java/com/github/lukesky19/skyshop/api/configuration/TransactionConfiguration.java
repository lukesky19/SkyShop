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
package com.github.lukesky19.skyshop.api.configuration;

import org.jspecify.annotations.Nullable;

/**
 * This interface can be used to create a configuration class that stores the data processed once a transaction occurs.
 */
public interface TransactionConfiguration {
    /**
     * Get the version of the configuration.
     * @return The configuration version.
     */
    int getVersion();

    /**
     * Get the identifier for this configuration that the serializer and processor is associated with.
     * @return The identifier.
     */
    @Nullable String getId();
}