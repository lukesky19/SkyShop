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

import com.github.lukesky19.skylib.gui.abstracts.ChestGUI;
import com.github.lukesky19.skyshop.gui.GUIManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.PlayerInventory;

import java.util.UUID;

/**
 * This class listens for when a plugin GUI is clicked or closed.
 */
public class InventoryListener implements Listener {
    private final GUIManager guiManager;

    public InventoryListener(GUIManager guiManager) {
        this.guiManager = guiManager;
    }

    /**
     * When an inventory is clicked, check if the Inventory is a GUI created by the plugin.
     * If so, call the handleClick method for the specific GUI.
     * @param inventoryClickEvent InventoryClickEvent
     */
    @EventHandler
    public void onClick(InventoryClickEvent inventoryClickEvent) {
        UUID uuid = inventoryClickEvent.getWhoClicked().getUniqueId();
        Inventory inventory = inventoryClickEvent.getClickedInventory();

        ChestGUI gui = guiManager.getOpenGUI(uuid);
        if(gui == null) return;

        gui.handleGlobalClick(inventoryClickEvent);

        if(inventory instanceof PlayerInventory) {
            gui.handleBottomClick(inventoryClickEvent);
        } else {
            gui.handleTopClick(inventoryClickEvent);
        }
    }

    /**
     * When an inventory is dragged, check if the Inventory is a GUI created by the plugin.
     * If so, call the handleDrag method for the specific GUI.
     * @param inventoryDragEvent InventoryClickEvent
     */
    @EventHandler
    public void onDrag(InventoryDragEvent inventoryDragEvent) {
        UUID uuid = inventoryDragEvent.getWhoClicked().getUniqueId();
        Inventory inventory = inventoryDragEvent.getInventory();

        ChestGUI gui = guiManager.getOpenGUI(uuid);
        if(gui == null) return;

        gui.handleGlobalDrag(inventoryDragEvent);

        if(inventory instanceof PlayerInventory) {
            gui.handleBottomDrag(inventoryDragEvent);
        } else {
            gui.handleTopDrag(inventoryDragEvent);
        }
    }

    /**
     * When an inventory is closed, check if the inventory is a GUI created by the plugin.
     * If so, call the handleClose method for the specific GUI.
     * @param inventoryCloseEvent InventoryCloseEvent
     */
    @EventHandler
    public void onClose(InventoryCloseEvent inventoryCloseEvent) {
        UUID uuid = inventoryCloseEvent.getPlayer().getUniqueId();

        ChestGUI gui = guiManager.getOpenGUI(uuid);

        if (gui != null) {
            gui.handleClose(inventoryCloseEvent);
        }
    }
}
