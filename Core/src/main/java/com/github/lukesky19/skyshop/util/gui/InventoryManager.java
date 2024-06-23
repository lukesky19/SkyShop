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
package com.github.lukesky19.skyshop.util.gui;

import com.github.lukesky19.skyshop.SkyShop;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.bukkit.Bukkit;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.Inventory;

/**
 * This class supports the creation of inventory GUIs.
*/
public class InventoryManager {
    final SkyShop skyShop;
    private final Map<Inventory, InventoryHandler> activeInventories;

    public InventoryManager(SkyShop skyShop) {
        this.activeInventories = new HashMap<>();
        this.skyShop = skyShop;
    }

    public void openGUI(InventoryGUI gui, Player player) {
        registerHandledInventory(gui.getInventory(), gui);
        player.openInventory(gui.getInventory());
    }

    public void registerHandledInventory(Inventory inventory, InventoryHandler handler) {
        this.activeInventories.put(inventory, handler);
    }

    public void unregisterInventory(Inventory inventory) {
        this.activeInventories.remove(inventory);
    }

    public void clearRegisteredInventories() {
        for(Map.Entry<Inventory, InventoryHandler> entry : this.activeInventories.entrySet()) {
            List<HumanEntity> playerList = new ArrayList<>(entry.getKey().getViewers());

            for(HumanEntity player : playerList) {
                Bukkit.getScheduler().runTaskLater(this.skyShop, () -> player.closeInventory(InventoryCloseEvent.Reason.UNLOADED), 1L);
            }

            unregisterInventory(entry.getKey());
        }
    }

    public void handleClick(InventoryClickEvent event) {
        InventoryHandler handler = this.activeInventories.get(event.getInventory());
        if(handler != null) {
            handler.onClick(event);
        }
    }

    public void handleOpen(InventoryOpenEvent event) {
        InventoryHandler handler = this.activeInventories.get(event.getInventory());
        if(handler != null) {
            handler.onOpen(event);
        }
    }

    public void handleClose(InventoryCloseEvent event) {
        Inventory inventory = event.getInventory();
        InventoryHandler handler = this.activeInventories.get(inventory);
        if(handler != null) {
            handler.onClose(event);
            unregisterInventory(inventory);
        }
    }
}
