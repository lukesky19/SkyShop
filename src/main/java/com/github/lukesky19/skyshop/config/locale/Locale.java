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
package com.github.lukesky19.skyshop.config.locale;

import com.github.lukesky19.skylib.libs.configurate.objectmapping.ConfigSerializable;

import java.util.List;

/**
 * The plugin's locale configuration.
 * @param configVersion The configuration's version.
 * @param prefix The plugin's prefix/
 * @param help The plugin's help message. A {@link List} of {@link String}s.
 * @param configReload The message sent when the plugin reloads.
 * @param notEnoughItems The message sent to the player when they lack the items to sell.
 * @param insufficientMoney The message sent to the player when they lack the funds to buy an item or command.
 * @param insufficientPlayerPoints The message sent to the player when they lack the player points to buy an item or command.
 * @param buyItemSuccess The message when the player successfully completes buying an item.
 * @param sellItemSuccess The message when the player successfully completes selling an item.
 * @param buyCommandSuccess The message when the player successfully completes buying a command.
 * @param sellCommandSuccess The message when the player successfully completes selling a command.
 * @param otherBuySuccess The message sent when a player successfully completes a non-item and non-command buy transaction.
 * @param otherSellSuccess The message sent when a player successfully completes a non-item and non-command sell transaction.
 * @param sellallSuccess The message sent to the player when they successfully sell all items.
 * @param sellallUnsellable The message sent to the player when an item is unsellable.
 * @param unbuyable The message sent to the player when an item is unbuyable.
 * @param unsellable The message sent to the player when an item is unsellable.
 * @param inGameOnly The message sent in console when a command is in-game only.
 * @param guiOpenError The message sent to the player when a GUI fails to open.
 * @param statsDisabledGuiError The message sent to the player when stats tracking is disabled, and they try to open the stats GUI.
 * @param transactionError The message sent to the player when buying or selling fails due to an error.
 * @param islandSizeMessages The messages related to buying or selling island size.
 * @param prestigeMultiplierMessages The messages related to buying or selling a prestige points multiplier.
 * @param categoryNoPermission The message sent to a player when they don't have permission to access a shop category.
 * @param buttonNoPermission The message sent to a player when they don't have permission to use a button.
 */
@ConfigSerializable
public record Locale(
        String configVersion,
        String prefix,
        List<String> help,
        String configReload,
        String notEnoughItems,
        String insufficientMoney,
        String insufficientPlayerPoints,
        SuccessMessages buyItemSuccess,
        SuccessMessages sellItemSuccess,
        SuccessMessages buyCommandSuccess,
        SuccessMessages sellCommandSuccess,
        SuccessMessages otherBuySuccess,
        SuccessMessages otherSellSuccess,
        SuccessMessages sellallSuccess,
        String sellallUnsellable,
        String unbuyable,
        String unsellable,
        String inGameOnly,
        String guiOpenError,
        String statsDisabledGuiError,
        String transactionError,
        IslandSizeMessages islandSizeMessages,
        PrestigeMultiplierMessages prestigeMultiplierMessages,
        String categoryNoPermission,
        String buttonNoPermission) {
    /**
     * This record contains the messages used when an item or command is purchased or sold successfully.
     * @param moneyAndPoints The message sent when an item or command is purchased ors old for money and points.
     * @param money The message sent when an item or command is purchased ors old for money only.
     * @param points The message sent when an item or command is purchased ors old for points only.
     */
    @ConfigSerializable
    public record SuccessMessages(
            String moneyAndPoints,
            String money,
            String points) {}
    /**
     * This record contains messages related to island size expansion purchases.
     * @param notOnIsland The message sent to a player when they are not on their island to buy or sell island size.
     * @param islandTooSmall The message sent to a player when their island is too small to sell a portion of their island size.
     * @param islandMaxSize The message sent to a player when their island is at the maximum size and an expansion can be purchased.
     */
    @ConfigSerializable
    public record IslandSizeMessages(
            String notOnIsland,
            String islandTooSmall,
            String islandMaxSize) {}
    /**
     * This record contains messages related to buy or sell a prestige point multiplier.
     * @param notOnIsland The message sent to a player when they are not on their island.
     * @param multiplierActive The message sent to a player when a multiplier is active and the player isn't allowed to buy another because of it.
     * @param higherMultiplierActive The message sent to a player when a higher multiplier is active and the player isn't allowed to buy another because of it.
     * @param multiplierTimeMax The message sent to a player when a multiplier is at the maximum time allowed.
     */
    @ConfigSerializable
    public record PrestigeMultiplierMessages(
            String notOnIsland,
            String multiplierActive,
            String higherMultiplierActive,
            String multiplierTimeMax) {}
}
