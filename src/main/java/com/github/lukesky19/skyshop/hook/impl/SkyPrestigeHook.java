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

import com.github.lukesky19.skyPrestige.SkyPrestigeAPI;
import com.github.lukesky19.skyshop.SkyShop;
import com.github.lukesky19.skyshop.hook.Hook;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import world.bentobox.bentobox.database.objects.Island;

/**
 * This class manages interfacing with the SkyPrestige plugin.
 */
public class SkyPrestigeHook implements Hook {
    private final @NotNull SkyShop skyShop;
    private @Nullable SkyPrestigeAPI skyPrestigeAPI;

    /**
     * Constructor
     * @param skyShop A {@link SkyShop} instance.
     */
    public SkyPrestigeHook(@NotNull SkyShop skyShop) {
        this.skyShop = skyShop;
    }

    /**
     * Attempt to get the {@link Economy} from Vault.
     */
    @Override
    public void initialize() {
        if(skyShop.getServer().getPluginManager().isPluginEnabled("SkyPrestige")) {
            RegisteredServiceProvider<SkyPrestigeAPI> rsp = skyShop.getServer().getServicesManager().getRegistration(SkyPrestigeAPI.class);
            if(rsp != null) {
                this.skyPrestigeAPI = rsp.getProvider();
            }
        }
    }

    /**
     * Is the hook initialized?
     * @return true if hooked, otherwise false.
     */
    @Override
    public boolean isHooked() {
        return skyPrestigeAPI != null;
    }

    /**
     * Get the server prestige points multiplier.
     * @return The server prestige points multiplier or 0.0 if not hooked into.
     */
    public double getServerMultiplier() {
        if(skyPrestigeAPI == null) return 0.0;

        return skyPrestigeAPI.getServerMultiplier();
    }

    /**
     * Get the server prestige points multiplier time.
     * @return The server prestige points multiplier time or 0 if not hooked into.
     */
    public long getServerMultiplierTime() {
        if(skyPrestigeAPI == null) return 0;

        return skyPrestigeAPI.getServerMultiplierTime();
    }

    /**
     * Get the island's prestige points multiplier.
     * @param island The {@link Island}.
     * @return The island's prestige points multiplier or 0.0 if not hooked into.
     */
    public double getIslandMultiplier(@NotNull Island island) {
        if(skyPrestigeAPI == null) return 0.0;

        return skyPrestigeAPI.getIslandMultiplier(island);
    }

    /**
     * Get the island's prestige points multiplier time.
     * @param island The {@link Island}.
     * @return The island's prestige points multiplier time or 0 if not hooked into.
     */
    public long getIslandMultiplierTime(@NotNull Island island) {
        if(skyPrestigeAPI == null) return 0;

        return skyPrestigeAPI.getIslandMultiplierTime(island);
    }

    /**
     * Update the server multiplier.
     * @param multiplier The multiplier or null.
     * @param time The multiplier time or null.
     * @return true if successful, false if not.
     */
    public boolean setServerMultiplier(@Nullable Double multiplier, @Nullable Long time) {
        if(skyPrestigeAPI == null) return false;
        if(multiplier == null && time == null) return false;

        if(multiplier != null && time != null) {
            return skyPrestigeAPI.setServerMultiplier(multiplier, time, true);
        } else if(multiplier != null) {
            return skyPrestigeAPI.setServerMultiplier(multiplier, true);
        } else {
            return skyPrestigeAPI.setServerMultiplier(time, true);
        }
    }

    /**
     * Update the server multiplier.
     * @param island The {@link Island}.
     * @param multiplier The multiplier or null.
     * @param time The multiplier time or null.
     * @return true if successful, false if not.
     */
    public boolean setIslandMultiplier(@NotNull Island island, @Nullable Double multiplier, @Nullable Long time) {
        if(skyPrestigeAPI == null) return false;
        if(multiplier == null && time == null) return false;

        if(multiplier != null && time != null) {
            return skyPrestigeAPI.setIslandMultiplier(island, multiplier, time, true);
        } else if(multiplier != null) {
            return skyPrestigeAPI.setIslandMultiplier(island, multiplier, true);
        } else {
            return skyPrestigeAPI.setIslandMultiplier(island, time, true);
        }
    }
}
