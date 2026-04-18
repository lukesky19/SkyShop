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
import com.github.lukesky19.skylib.paper.api.itemstack.ItemStackConfig;
import com.github.lukesky19.skyshop.api.configuration.TransactionConfiguration;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/**
 * The configuration to buy/sell {@link ItemStack}s.
 * @param version The version of the configuration.
 * @param id The id the configuration is associated with. Used to tie it with a processor and serializer.
 * @param transactionItem The {@link ItemStackConfig} to create the {@link ItemStack}.
 * @param cacheSellPrice If the sell price should be cached for use with sell related methods.
 */
@ConfigSerializable
public record ItemConfiguration(
        int version,
        @Nullable String id,
        @NonNull ItemStackConfig transactionItem,
        boolean cacheSellPrice) implements TransactionConfiguration {
    @Override
    public int getVersion() {
        return version;
    }

    @Override
    public @Nullable String getId() {
        return id;
    }
}