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
package com.github.lukesky19.skyshop.configuration.locale.data;

import com.github.lukesky19.skylib.libs.configurate.objectmapping.ConfigSerializable;

import java.util.List;

/**
 * The version 1 format of the locale configuration for migration purposes only.
 * Note: The first version did not have a proper config version defined.
 * @param prefix The plugin's prefix.
 * @param help The plugin's help message. A {@link List} of {@link String}s.
 * @param noPermission The message sent when a player lacks permission for a command.
 * @param configReload The message sent when the plugin is reloaded.
 * @param notEnoughItems The message sent to the player when they lack the items to sell.
 * @param insufficientFunds The message sent to the player when they lack the funds to buy an item or command.
 * @param buySuccess The message when the player successfully completes buy transaction.
 * @param sellSuccess The message when the player successfully completes sell transaction.
 * @param sellallSuccess The message sent to the player when they successfully sell all items.
 * @param sellallUnsellable The message sent to the player when an item is unsellable.
 * @param unbuyable The message sent to the player when an item is unbuyable.
 * @param unsellable The message sent to the player when an item is unsellable.
 * @param inGameOnly The message sent in console when a command is in-game only.
 * @param unknownArgument The message sent when an unknown argument is sent for a command.
 */
@Deprecated(since = "2.0.0.0")
@ConfigSerializable
public record LocaleV1(
        String prefix,
        List<String> help,
        String noPermission,
        String configReload,
        String notEnoughItems,
        String insufficientFunds,
        String buySuccess,
        String sellSuccess,
        String sellallSuccess,
        String sellallUnsellable,
        String unbuyable,
        String unsellable,
        String inGameOnly,
        String unknownArgument) {}