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
package com.github.lukesky19.skyshop.player.countdown;

import java.io.Serializable;

/**
 * This class holds the data for a countdown.
 */
public class Cooldown implements Serializable {
    /**
     * The current cooldown seconds.
     */
    private int cooldownSeconds = 0;
    /**
     * The highest or starting cooldown seconds.
     */
    private int highestCooldownSeconds = 0;
    /**
     * The interval to update modifiers at.
     */
    private int updateInterval = 0;
    /**
     * The number of seconds elapsed.
     */
    private int secondsElapsed = 0;

    /**
     * Constructor
     */
    public Cooldown() {}

    /**
     * Get the current cooldown seconds.
     * @return The current cooldown seconds.
     */
    public int getCooldownSeconds() {
        return cooldownSeconds;
    }

    /**
     * Set the current cooldown seconds.
     * @param cooldownSeconds The cooldown seconds to set.
     */
    public void setCooldownSeconds(int cooldownSeconds) {
        this.cooldownSeconds = Math.max(0, cooldownSeconds);
    }

    /**
     * Remove the cooldown seconds from the current cooldown.
     * @param cooldownSeconds The cooldown seconds to remove.
     */
    public void removeCooldownSeconds(int cooldownSeconds) {
        if(cooldownSeconds <= 0) return;

        this.cooldownSeconds = Math.max(0, this.cooldownSeconds - cooldownSeconds);
    }

    /**
     * Get the highest cooldown seconds. This would be where the countdown started at.
     * @return The highest cooldown in seconds.
     */
    public int getHighestCooldownSeconds() {
        return highestCooldownSeconds;
    }

    /**
     * Set the highest cooldown seconds. This would be where the countdown started at.
     * @param highestCooldownSeconds The highest cooldown in seconds.
     */
    public void setHighestCooldownSeconds(int highestCooldownSeconds) {
        this.highestCooldownSeconds = Math.max(0, highestCooldownSeconds);
    }

    /**
     * Get the update interval. This can be used with time elapsed.
     * @return The update interval.
     */
    public int getUpdateInterval() {
        return updateInterval;
    }

    /**
     * Set the update interval.
     * @param updateInterval The update interval.
     */
    public void setUpdateInterval(int updateInterval) {
        this.updateInterval = Math.max(0, updateInterval);
    }

    /**
     * Get the seconds elapsed.
     * @return The seconds elapsed.
     */
    public int getSecondsElapsed() {
        return secondsElapsed;
    }

    /**
     * Set the seconds elapsed.
     * @param secondsElapsed The seconds elapsed.
     */
    public void setSecondsElapsed(int secondsElapsed) {
        this.secondsElapsed = Math.max(0, secondsElapsed);
    }

    /**
     * Add the seconds to the seconds elapsed.
     * @param secondsElapsed The seconds elapsed.
     */
    public void addSecondsElapsed(int secondsElapsed) {
        if(secondsElapsed <= 0) return;

        this.secondsElapsed += secondsElapsed;
    }
}