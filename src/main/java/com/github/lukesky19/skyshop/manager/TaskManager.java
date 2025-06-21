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
package com.github.lukesky19.skyshop.manager;

import com.github.lukesky19.skyshop.SkyShop;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * This class manages the scheduled task that saves stats to the database.
 */
public class TaskManager {
    private final @NotNull SkyShop skyShop;
    private final @NotNull StatsManager statsManager;
    private @Nullable BukkitTask saveStatsTask;

    /**
     * Constructor
     * @param skyShop A {@link SkyShop} instance.
     * @param statsManager A {@link StatsManager} instance.
     */
    public TaskManager(@NotNull SkyShop skyShop, @NotNull StatsManager statsManager) {
        this.skyShop = skyShop;
        this.statsManager = statsManager;
    }

    /**
     * Start the task that regularly saves stats to the database.
     */
    public void startSaveStatsTask() {
        saveStatsTask = skyShop.getServer().getScheduler().runTaskTimer(skyShop, statsManager::saveStats, 20L * 900, 20L * 900);
    }

    /**
     * Stop the task that regularly saves stats time to the database.
     */
    public void stopSaveStatsTask() {
        if(saveStatsTask != null && !saveStatsTask.isCancelled()) {
            saveStatsTask.cancel();
            saveStatsTask = null;
        }
    }
}
