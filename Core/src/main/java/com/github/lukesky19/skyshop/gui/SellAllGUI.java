/*
    SkyShop is a simple inventory based shop plugin with page support, error checking, and configuration validation.
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
package com.github.lukesky19.skyshop.gui;

import com.github.lukesky19.skyshop.SkyShop;
import com.github.lukesky19.skyshop.api.SkyShopAPI;
import com.github.lukesky19.skyshop.util.gui.InventoryGUI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;

/**
 * This class is called to create a sellall inventory for a player to sell items.
*/
public class SellAllGUI extends InventoryGUI {
    final SkyShopAPI skyShopAPI;

    /**
     * Constructor
     * @param skyShopAPI The plugin's API.
    */
    public SellAllGUI(SkyShopAPI skyShopAPI) {
        this.skyShopAPI = skyShopAPI;

        createInventory();
    }

    /**
     * A method to create the base structure of the inventory GUI.
    */
    public void createInventory() {
        int size = 54;
        Component name = MiniMessage.miniMessage().deserialize("<green>SellAll Menu</green>");
        setInventory(Bukkit.createInventory(null, size, name));
    }

    /**
     * A method to create all the buttons in the inventory GUI.
    */
    public void decorate() {}

    public void onClick(InventoryClickEvent event) {}

    /**
     * Handles the closing of the inventory GUI.
     * Also handles selling of items and returning any un-sellable items.
     * @param event The InventoryCloseEvent
    */
    public void onClose(InventoryCloseEvent event) {
        if(event.getPlayer() instanceof Player player) {
            skyShopAPI.sell(event.getInventory(), player);
        }
    }
}
