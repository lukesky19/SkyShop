/*
    SkyShop is a simple inventory based shop plugin with page support, sell commands, and error checking.
    Copyright (C) 2024  lukeskywlker19

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
package com.github.lukesky19.skyshop.enums;

import org.jetbrains.annotations.Nullable;

/**
 * This enum is used for different actions a GUIButton can do.
 */
public enum ActionType {
    FILLER,
    PREVIOUS_PAGE,
    NEXT_PAGE,
    RETURN,
    OPEN_SHOP,
    ITEM,
    COMMAND,
    BUY,
    SELL,
    DISPLAY;

    @Nullable
    public static ActionType getActionType(String type) {
        try {
           return ActionType.valueOf(type);
        } catch (IllegalArgumentException ignored) {
            return null;
        }
    }
}
