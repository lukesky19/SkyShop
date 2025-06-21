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
package com.github.lukesky19.skyshop.data;

import com.github.lukesky19.skylib.libs.configurate.objectmapping.ConfigSerializable;

import java.util.List;

/**
 * The plugin's locale configuration.
 * @param configVersion The configuration's version.
 * @param prefix The plugin's prefix/
 * @param help The plugin's help message. A {@link List} of {@link String}s.
 * @param configReload The message sent when the plugin reloads.
 * @param notEnoughItems The message sent to the player when they lack the items to sell.
 * @param insufficientFunds The message sent to the player when they lack the funds to buy an item or command.
 * @param buyItemSuccess The message when the player successfully completes buying an item.
 * @param sellItemSuccess The message when the player successfully completes selling an item.
 * @param buyCommandSuccess The message when the player successfully completes buying a command.
 * @param sellCommandSuccess The message when the player successfully completes selling a command.
 * @param sellallSuccess The message sent to the player when they successfully sell all items.
 * @param sellallUnsellable The message sent to the player when an item is unsellable.
 * @param unbuyable The message sent to the player when an item is unbuyable.
 * @param unsellable The message sent to the player when an item is unsellable.
 * @param inGameOnly The message sent in console when a command is in-game only.
 * @param guiOpenError The message sent to the player when a GUI fails to open.
 * @param statsDisabledGuiError The message sent to the player when stats tracking is disabled, and they try to open the stats GUI.
 */
@ConfigSerializable
public record Locale(
        String configVersion,
        String prefix,
        List<String> help,
        String configReload,
        String notEnoughItems,
        String insufficientFunds,
        String buyItemSuccess,
        String sellItemSuccess,
        String buyCommandSuccess,
        String sellCommandSuccess,
        String sellallSuccess,
        String sellallUnsellable,
        String unbuyable,
        String unsellable,
        String inGameOnly,
        String guiOpenError,
        String statsDisabledGuiError) {
}
