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
package com.github.lukesky19.skyshop.configuration.category.transaction;

import com.github.lukesky19.skylib.libs.configurate.objectmapping.ConfigSerializable;
import com.github.lukesky19.skyshop.api.configuration.TransactionConfiguration;
import org.jetbrains.annotations.Nullable;

/**
 * The configuration to buy/sell island size.
 * @param version The version of the configuration.
 * @param id The id the configuration is associated with. Used to tie it with a processor and serializer.
 * @param setIslandSize Whether the island size should be set or added to/removed from.
 * @param buyAmount The island size to purchase.
 * @param sellAmount The island size to sell.
 */
@ConfigSerializable
public record IslandSizeConfiguration(
        int version,
        @Nullable String id,
        boolean setIslandSize,
        @Nullable Integer buyAmount,
        @Nullable Integer sellAmount) implements TransactionConfiguration {
    @Override
    public int getVersion() {
        return version;
    }

    @Override
    public @Nullable String getId() {
        return id;
    }
}