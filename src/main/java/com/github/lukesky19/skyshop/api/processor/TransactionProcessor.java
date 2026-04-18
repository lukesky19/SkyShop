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
package com.github.lukesky19.skyshop.api.processor;

import com.github.lukesky19.skyshop.api.configuration.TransactionConfiguration;
import com.github.lukesky19.skyshop.api.result.TransactionResult;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NonNull;

/**
 * This interface can be used to create a processor that is used to process a {@link TransactionConfiguration}.
 */
public interface TransactionProcessor {
    /**
     * Can the player buy?
     * @apiNote Prices are already checked.
     * @param player The {@link Player}.
     * @param configuration The {@link TransactionConfiguration} to process.
     * @param amount The amount being purchased.
     * @return A {@link TransactionResult}.
     */
    @NonNull TransactionResult canBuy(
            @NonNull Player player,
            @NonNull TransactionConfiguration configuration,
            int amount);

    /**
     * Can the player sell?
     * @param player The {@link Player}.
     * @param configuration The {@link TransactionConfiguration} to process.
     * @param amount The amount being sold.
     * @return A {@link TransactionResult}.
     */
    @NonNull TransactionResult canSell(
            @NonNull Player player,
            @NonNull TransactionConfiguration configuration,
            int amount);

    /**
     * Process the buying.
     * @param player The {@link Player} to process configuration for.
     * @param configuration The configuration to process.
     * @param amount The amount being purchased.
     * @return A {@link TransactionResult}.
     */
    @NonNull TransactionResult buy(
            @NonNull Player player,
            @NonNull TransactionConfiguration configuration,
            int amount);

    /**
     * Process the selling.
     * @param player The {@link Player} to process configuration for.
     * @param configuration The configuration to process.
     * @param amount The amount being sold.
     * @return A {@link TransactionResult}.
     */
    @NonNull TransactionResult sell(
            @NonNull Player player,
            @NonNull TransactionConfiguration configuration,
            int amount);
}
