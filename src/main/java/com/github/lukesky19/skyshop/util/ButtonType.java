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
package com.github.lukesky19.skyshop.util;

import com.github.lukesky19.skyshop.gui.SellAllGUI;
import com.github.lukesky19.skyshop.gui.ShopGUI;
import com.github.lukesky19.skyshop.gui.TransactionGUI;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

/**
 * This enum is used for different actions a GUIButton can do.
 */
public enum ButtonType {
    /**
     * This type is used to identify the configuration for the filler buttons.
     */
    FILLER,
    /**
     * This type is used to identify the configuration for the previous page button.
     */
    PREVIOUS_PAGE,
    /**
     * This type is used to identify the configuration for the next page button.
     */
    NEXT_PAGE,
    /**
     * This type is used to identify the configuration for the return or exit button.
     */
    RETURN,
    /**
     * This type is used to identify the configuration for the button to open a {@link ShopGUI}.
     */
    OPEN_SHOP,
    /**
     * This type is used to identify the configuration for the button to open the {@link TransactionGUI}.
     */
    TRANSACTION,
    /**
     * This type is used to identify the configuration for the button to buy something.
     */
    BUY,
    /**
     * This type is used to identify the configuration for the button to sell something.
     */
    SELL,
    /**
     * This type is used to identify the configuration for the button that displays what is being purchased or sold.
     */
    DISPLAY,
    /**
     * This type is used to identify the configuration for the button that sells all {@link ItemStack}s inside the {@link Player}'s {@link Inventory}.
     */
    SELL_ALL,
    /**
     * This type is used to identify the configuration for the button that opens the {@link SellAllGUI}.
     */
    SELL_GUI,
    /**
     * This type is used to identify configuration for dummy buttons. This button type is similar to FILLER, but is only for a single slot.
     */
    DUMMY
}
