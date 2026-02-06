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
package com.github.lukesky19.skyshop.prices;

import com.github.lukesky19.skyshop.configuration.category.data.CategoryConfigV4;
import org.bukkit.inventory.ItemType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * This record stores the transaction id and {@link CategoryConfigV4.PriceConfig} for a given {@link ItemType}.
 * @param categoryPermission The permission required for the category the transaction is in.
 * @param transactionPermission The permission required for the transaction.
 * @param transactionId The transaction id.
 * @param priceConfig The cached {@link CategoryConfigV4.PriceConfig}
 */
public record PriceCache(
        @Nullable String categoryPermission,
        @Nullable String transactionPermission,
        @NotNull String transactionId,
        @NotNull CategoryConfigV4.PriceConfig priceConfig) {}
