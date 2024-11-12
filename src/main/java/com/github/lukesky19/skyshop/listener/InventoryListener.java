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
package com.github.lukesky19.skyshop.listener;

import com.github.lukesky19.skyshop.SkyShop;
import com.github.lukesky19.skyshop.gui.MenuGUI;
import com.github.lukesky19.skyshop.gui.SellAllGUI;
import com.github.lukesky19.skyshop.gui.ShopGUI;
import com.github.lukesky19.skyshop.gui.TransactionGUI;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;

/**
 * This class listens for when a plugin GUI is clicked or closed.
 */
public class InventoryListener implements Listener {
    final SkyShop skyShop;

    /**
     * Constructor
     * @param skyShop The plugin's instance.
     */
    public InventoryListener(SkyShop skyShop) {
        this.skyShop = skyShop;
    }

    /**
     * When an inventory is clicked, check if the Inventory is a GUI created by the plugin.
     * If so, call the handleClick method for the specific GUI.
     * @param event InventoryClickEvent
     */
    @EventHandler
    public void onClick(InventoryClickEvent event) {
        Inventory inventory = event.getClickedInventory();
        if(inventory == null) return;

        if(inventory.getHolder(false) instanceof MenuGUI menuGUI) {
            menuGUI.handleClick(event);
        }

        if(inventory.getHolder(false) instanceof ShopGUI shopGUI) {
            shopGUI.handleClick(event);
        }

        if(inventory.getHolder(false) instanceof TransactionGUI transactionGUI) {
            transactionGUI.handleClick(event);
        }

        if(inventory.getHolder(false) instanceof SellAllGUI SellAllGUI) {
            SellAllGUI.handleClick(event);
        }
    }

    /**
     * When an inventory is closed, check if the inventory is a GUI created by the plugin.
     * If so, call the handleClose method for the specific GUI.
     * @param event InventoryCloseEvent
     */
    @EventHandler
    public void onClose(InventoryCloseEvent event) {
        Inventory inventory = event.getInventory();

        if(inventory.getHolder(false) instanceof MenuGUI menuGUI) {
            menuGUI.handleClose(event);
        }

        if(inventory.getHolder(false) instanceof ShopGUI shopGUI) {
            shopGUI.handleClose(event);
        }

        if(inventory.getHolder(false) instanceof TransactionGUI transactionGUI) {
            transactionGUI.handleClose(event);
        }

        if(inventory.getHolder(false) instanceof SellAllGUI sellAllGUI) {
            sellAllGUI.handleClose(event);
        }
    }
}
