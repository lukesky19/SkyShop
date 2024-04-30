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

import java.util.HashMap;
import java.util.Map;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

/**
 * This class supports the creation of inventory GUIs.
*/
public abstract class InventoryGUI implements InventoryHandler {
    private Inventory inventory;
    private final Map<Integer, InventoryButton> buttonMap = new HashMap<>();
  
    public final Inventory getInventory() {
        return this.inventory;
    }
  
    public final void setInventory(Inventory inventory) {
        this.inventory = inventory;
    }
  
    public final void addButton(int slot, InventoryButton button) {
        this.buttonMap.put(slot, button);
    }
  
    public void decorate() {
        this.buttonMap.forEach((slot, button) -> {
            ItemStack icon = button.itemStack();
            ItemMeta iconMeta = icon.getItemMeta();
            iconMeta.displayName(button.itemName());
            iconMeta.lore(button.lore());
            icon.setItemMeta(iconMeta);
            this.inventory.setItem(slot, icon);
        });
    }

    public void onClick(InventoryClickEvent event) {
        event.setCancelled(true);
        int slot = event.getSlot();
        InventoryButton button = this.buttonMap.get(slot);
        if (button != null) {
            button.action().accept(event);
        }
    }

    public void onOpen(InventoryOpenEvent event) {
        decorate();
    }
  
    public void onClose(InventoryCloseEvent event) {}
  
    public abstract void createInventory();
}
