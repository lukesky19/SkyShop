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
package com.github.lukesky19.skyshop.configuration.category.transaction.migration;

import com.github.lukesky19.skylib.libs.configurate.objectmapping.ConfigSerializable;
import com.github.lukesky19.skyshop.api.configuration.TransactionConfiguration;
import com.github.lukesky19.skyshop.util.MultiplierType;
import org.jetbrains.annotations.Nullable;

/**
 * This record is used to migrate the prestige point multiplier data, but is not actually used.<br>
 * SkyPrestige handles the actual data format after migration.
 * @param version The data format version.
 * @param id The processor id to use.
 * @param multiplierType The {@link MultiplierType} of the purchase.
 * @param activeMultiplierPreventPurchase If the multiplier is active, prevent the purchase of another multiplier.
 * @param activeMultiplierHigherPreventPurchase If the active multiplier is higher than the one being purchased, prevent the purchase of the multiplier.
 * @param resetMultiplierTimeIfHigherMultiplier If the multiplier is higher than the active multiplier, should the current time be reset before adding time?
 * @param multiplier The multiplier to purchase.
 * @param time The multiplier time to purchase.
 * @param maxTime The maximum time to allow purchase of. If the total time will exceed this amount, the purchase won't be allowed.
 */
@ConfigSerializable
public record PrestigeMultiplierData(
        int version,
        @Nullable String id,
        @Nullable MultiplierType multiplierType,
        boolean activeMultiplierPreventPurchase,
        boolean activeMultiplierHigherPreventPurchase,
        boolean resetMultiplierTimeIfHigherMultiplier,
        @Nullable Double multiplier,
        @Nullable Long time,
        @Nullable Long maxTime) implements TransactionConfiguration {
    @Override
    public int getVersion() {
        return version;
    }

    @Override
    public @Nullable String getId() {
        return id;
    }
}
