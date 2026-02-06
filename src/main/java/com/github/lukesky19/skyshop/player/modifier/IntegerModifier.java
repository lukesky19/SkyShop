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
package com.github.lukesky19.skyshop.player.modifier;

import java.io.Serializable;

/**
 * This class can be used for a modifier using an int.
 */
public class IntegerModifier implements Serializable {
    /**
     * The current modifier.
     */
    private int currentModifier = 0;
    /**
     * The highest or starting modifier.
     */
    private int highestModifier = 0;

    /**
     * Constructor
     */
    public IntegerModifier() {}

    /**
     * Get the current modifier.
     * @return The current modifier.
     */
    public int getCurrentModifier() {
        return currentModifier;
    }

    /**
     * Set the current modifier.
     * @param modifier The modifier to set.
     */
    public void setCurrentModifier(int modifier) {
        this.currentModifier = Math.max(0, modifier);
    }

    /**
     * Get the highest modifier.
     * @return The highest modifier.
     */
    public int getHighestModifier() {
        return highestModifier;
    }

    /**
     * Set the highest modifier.
     * @param modifier The modifier to set.
     */
    public void setHighestModifier(int modifier) {
        this.highestModifier = Math.max(0, modifier);
    }
}