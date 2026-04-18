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
package com.github.lukesky19.skyshop.task;

import com.github.lukesky19.skylib.paper.api.gui.impl.UUIDGUIManager;
import com.github.lukesky19.skyshop.SkyShop;
import com.github.lukesky19.skyshop.player.PlayerDataManager;
import com.github.lukesky19.skyshop.stats.StatsManager;
import com.github.lukesky19.skyshop.task.tasks.CooldownTask;
import org.bukkit.scheduler.BukkitTask;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/**
 * This class manages the scheduled task that saves stats to the database.
 */
public class TaskManager {
    private final @NonNull SkyShop skyShop;
    private final @NonNull PlayerDataManager playerDataManager;
    private final @NonNull UUIDGUIManager guiManager;
    private final @Nullable StatsManager statsManager;
    private @Nullable BukkitTask saveStatsTask;
    private @Nullable BukkitTask cooldownTask;

    /**
     * Constructor
     * @param skyShop A {@link SkyShop} instance.
     * @param playerDataManager A {@link PlayerDataManager} instance.
     * @param guiManager A {@link UUIDGUIManager} instance.
     * @param statsManager A {@link StatsManager} instance.
     */
    public TaskManager(
            @NonNull SkyShop skyShop,
            @NonNull PlayerDataManager playerDataManager,
            @NonNull UUIDGUIManager guiManager,
            @Nullable StatsManager statsManager) {
        this.skyShop = skyShop;
        this.playerDataManager = playerDataManager;
        this.guiManager = guiManager;
        this.statsManager = statsManager;
    }

    /**
     * Start all tasks.
     */
    public void startTasks() {
        stopTasks();

        startCooldownTask();
        startSaveStatsTask();
    }

    /**
     * Stop all tasks.
     */
    public void stopTasks() {
        stopCooldownTask();
        stopSaveStatsTask();
    }

    /**
     * Start the task that regularly saves stats to the database.
     */
    private void startSaveStatsTask() {
        if(statsManager == null) return;

        saveStatsTask = skyShop.getServer().getScheduler().runTaskTimer(skyShop, statsManager::saveStats, 20L * 900, 20L * 900);
    }

    /**
     * Stop the task that regularly saves stats time to the database.
     */
    private void stopSaveStatsTask() {
        if(saveStatsTask != null) {
            if(!saveStatsTask.isCancelled()) {
                saveStatsTask.cancel();
            }

            saveStatsTask = null;
        }
    }

    private void startCooldownTask() {
        cooldownTask = new CooldownTask(playerDataManager, guiManager).runTaskTimer(skyShop, 20L, 20L);
    }

    private void stopCooldownTask() {
        if(cooldownTask != null) {
            if(!cooldownTask.isCancelled()) {
                cooldownTask.cancel();
            }

            cooldownTask = null;
        }
    }
}
