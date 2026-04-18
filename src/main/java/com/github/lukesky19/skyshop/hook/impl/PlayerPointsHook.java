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
package com.github.lukesky19.skyshop.hook.impl;

import com.github.lukesky19.skylib.common.api.integration.Hook;
import com.github.lukesky19.skyshop.SkyShop;
import org.black_ixx.playerpoints.PlayerPoints;
import org.black_ixx.playerpoints.PlayerPointsAPI;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.UUID;

/**
 * This class manages interfacing with PlayerPoints.
 */
public class PlayerPointsHook implements Hook {
    private final @NonNull SkyShop skyShop;
    private @Nullable PlayerPointsAPI playerPointsAPI;

    /**
     * Constructor
     * @param skyShop A {@link SkyShop} instance.
     */
    public PlayerPointsHook(@NonNull SkyShop skyShop) {
        this.skyShop = skyShop;
    }

    /**
     * Attempt to get the {@link PlayerPointsAPI}.
     */
    @Override
    public void initialize() {
        if(skyShop.getServer().getPluginManager().isPluginEnabled("PlayerPoints")) {
            this.playerPointsAPI = PlayerPoints.getInstance().getAPI();
        }
    }

    /**
     * Is the hook initialized?
     * @return true if hooked, otherwise false.
     */
    @Override
    public boolean isHooked() {
        return playerPointsAPI != null;
    }

    /**
     * Add the amount provided to the player's balance.
     * @apiNote If the PlayerPoints plugin was not hooked into, this method will do nothing. Can be checked with {@link #isHooked()}.
     * @param player The {@link Player}.
     * @param amount The amount to add.
     */
    public void addToBalance(@NonNull Player player, int amount) {
        if(playerPointsAPI == null) return;

        playerPointsAPI.give(player.getUniqueId(), amount);
    }

    /**
     * Remove the amount provided from the player's points balance.
     * This method will prevent balances from going into the negative.
     * @apiNote If the PlayerPoints plugin was not hooked into, this method will do nothing. Can be checked with {@link #isHooked()}.
     * @param player The {@link Player}.
     * @param amount The amount to remove.
     * @return The amount removed.
     */
    public double removeFromBalance(@NonNull Player player, int amount) {
        if(playerPointsAPI == null) return 0;
        UUID uuid = player.getUniqueId();

        int balance = playerPointsAPI.look(uuid);
        if(balance - amount < 0) {
            playerPointsAPI.take(uuid, balance);
            return balance;
        } else {
            playerPointsAPI.take(uuid, amount);
            return amount;
        }
    }

    /**
     * Get the player's points balance.
     * @apiNote Will always return 0 if the PlayerPoints plugin was not hooked into. Can be checked with {@link #isHooked()}.
     * @param player The {@link Player} to get the points balance for.
     * @return The player's balance or 0 if not hooked.
     */
    public double getBalance(@NonNull Player player) {
        if(playerPointsAPI == null) return 0;

        return playerPointsAPI.look(player.getUniqueId());
    }
}
