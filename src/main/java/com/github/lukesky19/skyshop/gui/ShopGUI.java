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
import com.github.lukesky19.skyshop.configuration.locale.LocaleManager;
import com.github.lukesky19.skyshop.configuration.menu.MenuManager;
import com.github.lukesky19.skyshop.configuration.shop.ShopConfiguration;
import com.github.lukesky19.skyshop.configuration.shop.ShopManager;
import com.github.lukesky19.skyshop.util.enums.TransactionType;
import com.github.lukesky19.skyshop.util.gui.InventoryButton;
import com.github.lukesky19.skyshop.util.gui.InventoryGUI;
import com.github.lukesky19.skyshop.util.gui.InventoryManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * This class is called to create a shop inventory for a player to access an individual shop category.
*/
public class ShopGUI extends InventoryGUI {
    final SkyShop skyShop;
    final MenuManager menuManager;
    final InventoryManager inventoryManager;
    final ShopManager shopManager;
    final LocaleManager localeManager;
    final Map.Entry<String, ShopConfiguration.ShopPage> pageEntry;
    final String shopId;
    final int pageNum;

    /**
     * Constructor
     * @param skyShop The plugin's instance.
     * @param menuManager A MenuManager instance.
     * @param shopManager A ShopManager instance.
     * @param inventoryManager A InventoryManager instance.
     * @param localeManager A LocaleManager instance.
     * @param pageEntry The page configuration to create the Inventory based on.
     * @param pageNum The page number associated with the Inventory being created.
    */
    public ShopGUI(
            SkyShop skyShop,
            MenuManager menuManager,
            ShopManager shopManager,
            InventoryManager inventoryManager,
            LocaleManager localeManager,
            Map.Entry<String, ShopConfiguration.ShopPage> pageEntry,
            String shopId,
            int pageNum) {
        this.skyShop = skyShop;
        this.menuManager = menuManager;
        this.shopManager = shopManager;
        this.inventoryManager = inventoryManager;
        this.localeManager = localeManager;
        this.pageEntry = pageEntry;
        this.shopId = shopId;
        this.pageNum = pageNum;

        createInventory();
    }

    /**
     * A method to create the base structure of the inventory GUI.
    */
    public void createInventory() {
        ShopConfiguration.ShopPage shopPage = pageEntry.getValue();
        int shopSize = shopPage.size();
        Component shopName = MiniMessage.miniMessage().deserialize(shopPage.name());
        setInventory(Bukkit.createInventory(null, shopSize, shopName));
        decorate();
    }

    /**
     * A method to create all the buttons in the inventory GUI.
    */
    public void decorate() {
        ShopConfiguration.ShopPage shopPage = pageEntry.getValue();
        for (Map.Entry<String, ShopConfiguration.ShopEntry> stringShopEntryEntry : shopPage.entries().entrySet()) {
            Material material;
            List<Component> loreList;
            Component name;
            int i;
            ShopConfiguration.ShopEntry entry = stringShopEntryEntry.getValue();

            ShopConfiguration.Item item = entry.item();
            switch (TransactionType.valueOf(entry.type())) {
                case FILLER -> {
                    material = Material.valueOf(item.material());
                    name = MiniMessage.miniMessage().deserialize(item.name()).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE);
                    loreList = new ArrayList<>();
                    if (item.lore() != null) {
                        for (String loreLine : item.lore()) {
                            loreList.add(MiniMessage.miniMessage().deserialize(loreLine).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE));
                        }
                    }

                    for (i = 0; i <= shopPage.size() - 1; i++) {
                        addButton(i, (new InventoryButton.Builder())
                                .setItemStack(new ItemStack(material)).setItemName(name)
                                .setLore(loreList)
                                .setAction(event -> {
                                })
                                .build());
                    }
                }

                case PREVIOUS_PAGE -> {
                    loreList = new ArrayList<>();
                    if (item.lore() != null) {
                        for (String loreLine : item.lore()) {
                            loreList.add(MiniMessage.miniMessage().deserialize(loreLine).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE));
                        }
                    }

                    addButton(entry.slot(), new InventoryButton.Builder()
                            .setItemStack(new ItemStack(Material.valueOf(item.material())))
                            .setItemName(MiniMessage.miniMessage().deserialize(item.name()).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE))
                            .setLore(loreList)
                            .setAction(event -> {
                                List<Map.Entry<String, ShopConfiguration.ShopPage>> pageList = shopManager.getShopConfig(shopId).pages().entrySet().stream().toList();
                                int prevPageNum = pageNum - 1;
                                Map.Entry<String, ShopConfiguration.ShopPage> prevPage = pageList.get(prevPageNum);
                                Bukkit.getScheduler().runTaskLater(skyShop, () -> {
                                    event.getWhoClicked().closeInventory(InventoryCloseEvent.Reason.OPEN_NEW);
                                    inventoryManager.openGUI(new ShopGUI(skyShop, menuManager, shopManager, inventoryManager, localeManager, prevPage, shopId, prevPageNum), (Player) event.getWhoClicked());
                                }, 1L);
                            }).build());
                }

                case NEXT_PAGE -> {
                    loreList = new ArrayList<>();
                    if (item.lore() != null) {
                        for (String loreLine : item.lore()) {
                            loreList.add(MiniMessage.miniMessage().deserialize(loreLine).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE));
                        }
                    }

                    addButton(entry.slot(), new InventoryButton.Builder()
                            .setItemStack(new ItemStack(Material.valueOf(item.material())))
                            .setItemName(MiniMessage.miniMessage().deserialize(item.name()).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE))
                            .setLore(loreList)
                            .setAction(event -> {
                                List<Map.Entry<String, ShopConfiguration.ShopPage>> pageList = shopManager.getShopConfig(shopId).pages().entrySet().stream().toList();
                                int nextPageNum = pageNum + 1;
                                Map.Entry<String, ShopConfiguration.ShopPage> nextPage = pageList.get(nextPageNum);
                                Bukkit.getScheduler().runTaskLater(skyShop, () -> {
                                    event.getWhoClicked().closeInventory(InventoryCloseEvent.Reason.OPEN_NEW);
                                    inventoryManager.openGUI(new ShopGUI(skyShop, menuManager, shopManager, inventoryManager, localeManager, nextPage, shopId, nextPageNum), (Player) event.getWhoClicked());
                                }, 1L);
                            }).build());
                }

                case RETURN -> {
                    loreList = new ArrayList<>();
                    if (item.lore() != null) {
                        for (String loreLine : item.lore()) {
                            loreList.add(MiniMessage.miniMessage().deserialize(loreLine).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE));
                        }
                    }

                    addButton(entry.slot(), (new InventoryButton.Builder())
                            .setItemStack(new ItemStack(Material.valueOf(item.material())))
                            .setItemName(MiniMessage.miniMessage().deserialize(item.name()).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE))
                            .setLore(loreList)
                            .setAction(event -> Bukkit.getScheduler().runTaskLater(skyShop, () -> {
                                event.getWhoClicked().closeInventory(InventoryCloseEvent.Reason.OPEN_NEW);
                                inventoryManager.openGUI(new MenuGUI(skyShop, menuManager, shopManager, inventoryManager, localeManager, menuManager.getMenuConfiguration().pages().entrySet().stream().toList().getFirst(), 0), (Player) event.getWhoClicked());
                            }, 1L))
                            .build());
                }

                case ITEM, COMMAND -> {
                    loreList = new ArrayList<>();
                    if (item.lore() != null) {
                        for (String loreLine : item.lore()) {
                            loreList.add(MiniMessage.miniMessage().deserialize(loreLine, new TagResolver[]{
                                    Placeholder.parsed("buy_price", String.valueOf(entry.prices().buyPrice())),
                                    Placeholder.parsed("sell_price", String.valueOf(entry.prices().sellPrice()))
                            }).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE));
                        }
                    }

                    addButton(entry.slot(), new InventoryButton.Builder()
                            .setItemStack(new ItemStack(Material.valueOf(item.material())))
                            .setItemName(MiniMessage.miniMessage().deserialize(item.name()).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE))
                            .setLore(loreList)
                            .setAction(event -> Bukkit.getScheduler().runTaskLater(skyShop, () -> {
                                event.getWhoClicked().closeInventory(InventoryCloseEvent.Reason.OPEN_NEW);
                                inventoryManager.openGUI(new TransactionGUI(skyShop, menuManager, inventoryManager, shopManager, localeManager, pageEntry, entry, shopId, pageNum, 1), (Player) event.getWhoClicked());
                            }, 1L))
                            .build());
                }
            }
        }

        super.decorate();
    }

    /**
     * Handles the closing of the inventory GUI.
     * @param event The InventoryCloseEvent
    */
    public void onClose(InventoryCloseEvent event) {
        if (!event.getReason().equals(InventoryCloseEvent.Reason.OPEN_NEW) && !event.getReason().equals(InventoryCloseEvent.Reason.UNLOADED))
            Bukkit.getScheduler().runTaskLater(skyShop, () -> inventoryManager.openGUI(new MenuGUI(skyShop, menuManager, shopManager, inventoryManager, localeManager, menuManager.getMenuConfiguration().pages().entrySet().stream().toList().getFirst(), 0), (Player)event.getPlayer()), 1L);
    }
}
