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

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import net.kyori.adventure.text.Component;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

/**
 * This class supports the creation of inventory GUIs.
*/
public class InventoryButton {
    private final ItemStack itemStack;
    private final Component itemName;
    private final List<Component> lore;
    private final Consumer<InventoryClickEvent> action;
  
    public ItemStack itemStack() {
    return this.itemStack;
  }
    public Component itemName() {
    return this.itemName;
  }
    public List<Component> lore() {
    return this.lore;
  }
    public Consumer<InventoryClickEvent> action() {
    return this.action;
  }
  
    public InventoryButton(Builder builder) {
        this.itemStack = builder.itemStack;
        this.itemName = builder.itemName;
        this.lore = builder.lore;
        this.action = builder.action;
    }
  
    public static class Builder {
        private ItemStack itemStack;
        private Component itemName;
        private List<Component> lore = new ArrayList<>();
        private Consumer<InventoryClickEvent> action;

        public Builder setItemStack(ItemStack itemStack) {
            this.itemStack = itemStack;
            return this;
        }

        public Builder setItemName(Component itemName) {
            this.itemName = itemName;
            return this;
        }

        public Builder setLore(List<Component> lore) {
            this.lore = lore;
            return this;
        }

        public Builder setAction(Consumer<InventoryClickEvent> action) {
            this.action = action;
            return this;
        }

        public InventoryButton build() {
            return new InventoryButton(this);
        }
    }
}
