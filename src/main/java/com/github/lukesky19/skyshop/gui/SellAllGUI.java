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
import com.github.lukesky19.skyshop.configuration.locale.FormattedLocale;
import com.github.lukesky19.skyshop.configuration.locale.LocaleManager;
import com.github.lukesky19.skyshop.configuration.menu.MenuConfiguration;
import com.github.lukesky19.skyshop.configuration.menu.MenuManager;
import com.github.lukesky19.skyshop.configuration.shop.ShopConfiguration;
import com.github.lukesky19.skyshop.configuration.shop.ShopManager;
import com.github.lukesky19.skyshop.util.enums.TransactionType;
import com.github.lukesky19.skyshop.util.gui.InventoryGUI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.Map;

/**
 * This class is called to create a sellall inventory for a player to sell items.
*/
public class SellAllGUI extends InventoryGUI {
    final SkyShop skyShop;
    final LocaleManager localeManager;
    final MenuManager menuManager;
    final ShopManager shopManager;

    /**
     * Constructor
     * @param skyShop The plugin's instance.
     * @param menuManager A MenuManager instance.
     * @param shopManager A ShopManager instance.
     * @param localeManager A LocaleManager instance.
    */
    public SellAllGUI(SkyShop skyShop, MenuManager menuManager, ShopManager shopManager, LocaleManager localeManager) {
        this.skyShop = skyShop;
        this.menuManager = menuManager;
        this.shopManager = shopManager;
        this.localeManager = localeManager;

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
        FormattedLocale messages = localeManager.formattedLocale();
        double money = 0.0;

        for (Map.Entry<String, MenuConfiguration.MenuPage> menuPageEntry : menuManager.getMenuConfiguration().pages().entrySet()) {
            for (Map.Entry<String, MenuConfiguration.MenuEntry> menuEntryEntry : menuPageEntry.getValue().entries().entrySet()) {
                for (Map.Entry<String, ShopConfiguration.ShopPage> shopPageEntry : shopManager.getShopConfig(menuEntryEntry.getValue().shop()).pages().entrySet()) {
                    for (Map.Entry<String, ShopConfiguration.ShopEntry> shopEntryEntry : shopPageEntry.getValue().entries().entrySet()) {
                        for (ItemStack item : event.getInventory().getContents()) {
                            if (item != null
                                    && TransactionType.valueOf(shopEntryEntry.getValue().type()).equals(TransactionType.ITEM)
                                    && item.getType().equals(Material.valueOf(shopEntryEntry.getValue().item().material()))) {
                                money = money + (shopEntryEntry.getValue().prices().sellPrice() * item.getAmount());
                                event.getInventory().removeItem(item);
                            }
                        }
                    }
                }
            }
        }

        if (!Arrays.stream(event.getInventory().getContents()).toList().isEmpty()) {
            for (ItemStack item : event.getInventory().getContents()) {
                if (item != null && item.getType() != Material.AIR) {
                    event.getPlayer().sendMessage(messages.prefix().append(messages.sellallUnsellable()));

                    if (event.getPlayer().getInventory().firstEmpty() != -1) {
                        event.getPlayer().getInventory().addItem(item);
                    } else {
                        event.getPlayer().getWorld().dropItem(event.getPlayer().getLocation(), item);
                    }

                    event.getInventory().removeItem(item);
                }
            }
        }

        if (money != 0.0) {
            skyShop.getEconomy().depositPlayer((OfflinePlayer) event.getPlayer(), money);
            Component sellAllSuccess = MiniMessage.miniMessage().deserialize(messages.sellallSuccess(),
                    Placeholder.parsed("price", String.valueOf(money)),
                    Placeholder.parsed("bal", String.valueOf(skyShop.getEconomy().getBalance((OfflinePlayer) event.getPlayer()))));
            event.getPlayer().sendMessage(messages.prefix().append(sellAllSuccess));

        }
    }
}
