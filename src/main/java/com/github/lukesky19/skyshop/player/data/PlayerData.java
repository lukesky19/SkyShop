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
package com.github.lukesky19.skyshop.player.data;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * This class stores player data.
 */
public class PlayerData {
    private int version = 1;
    private @NotNull Map<String, PlayerModifiers> playerPricesMap = new HashMap<>();

    /**
     * Constructor
     */
    public PlayerData() {}

    /**
     * Get the version.
     * @return The version number.
     */
    public int getVersion() {
        return version;
    }

    /**
     * Set the version.
     * @param version The version number.
     */
    public void setVersion(int version) {
        this.version = version;
    }

    /**
     * Get the {@link PlayerModifiers} for the transaction id.
     * @param transactionId The id.
     * @return The {@link PlayerModifiers}.
     */
    public @NotNull PlayerModifiers getPlayerPrices(@NotNull String transactionId) {
        return playerPricesMap.computeIfAbsent(transactionId.toLowerCase(), PlayerModifiers::new);
    }

    /**
     * Set the {@link Map} mapping transaction ids as {@link String} to {@link PlayerModifiers}.
     * @param map A {@link Map} mapping transaction ids as {@link String} to {@link PlayerModifiers}.
     */
    public void setPlayerPricesMap(@NotNull Map<String, PlayerModifiers> map) {
        playerPricesMap = map;
    }

    /**
     * Get the {@link Map} mapping transaction ids as {@link String} to {@link PlayerModifiers}.
     * @return The {@link Map} mapping transaction ids as {@link String} to {@link PlayerModifiers}.
     */
    public @NotNull Map<String, PlayerModifiers> getPlayerPricesMap() {
        return playerPricesMap;
    }

    /**
     * Get a {@link Set} of all {@link PlayerModifiers} the player has.
     * @return A {@link Set} of {@link PlayerModifiers}.
     */
    public @NotNull Set<PlayerModifiers> getPlayerPrices() {
        return new HashSet<>(playerPricesMap.values());
    }
}