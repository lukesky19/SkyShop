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
import com.github.lukesky19.skyshop.configuration.menu.MenuConfiguration;
import com.github.lukesky19.skyshop.configuration.menu.MenuManager;
import com.github.lukesky19.skyshop.configuration.shop.ShopManager;
import com.github.lukesky19.skyshop.util.enums.TransactionType;
import com.github.lukesky19.skyshop.util.gui.InventoryButton;
import com.github.lukesky19.skyshop.util.gui.InventoryGUI;
import com.github.lukesky19.skyshop.util.gui.InventoryManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * This class is called to create a menu inventory for a player to access the shop.
*/
public class MenuGUI extends InventoryGUI {
    final SkyShop skyShop;
    final MenuManager menuManager;
    final InventoryManager inventoryManager;
    final ShopManager shopManager;
    final LocaleManager localeManager;
    final Map.Entry<String, MenuConfiguration.MenuPage> pageEntry;
    final Integer pageNum;

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
    public MenuGUI(SkyShop skyShop, MenuManager menuManager, ShopManager shopManager, InventoryManager inventoryManager, LocaleManager localeManager, Map.Entry<String, MenuConfiguration.MenuPage> pageEntry, Integer pageNum) {
        this.skyShop = skyShop;
        this.menuManager = menuManager;
        this.shopManager = shopManager;
        this.inventoryManager = inventoryManager;
        this.localeManager = localeManager;
        this.pageEntry = pageEntry;
        this.pageNum = pageNum;

        createInventory();
    }

    /**
     * A method to create the base structure of the inventory GUI.
    */
    public void createInventory() {
        MenuConfiguration.MenuPage page = pageEntry.getValue();
        int menuSize = page.size();
        Component menuName = MiniMessage.miniMessage().deserialize(page.name());
        setInventory(Bukkit.createInventory(null, menuSize, menuName));
    }

    /**
     * A method to create all the buttons in the inventory GUI.
    */
    public void decorate() {
        MenuConfiguration.MenuPage page = pageEntry.getValue();
        for (Map.Entry<String, MenuConfiguration.MenuEntry> stringMenuEntryEntry : page.entries().entrySet()) {
            Material material;
            List<Component> lore;
            Component name;
            List<Component> list1;
            int i;
            MenuConfiguration.MenuEntry itemEntry = stringMenuEntryEntry.getValue();
            MenuConfiguration.Item item = itemEntry.item();
            TransactionType type = TransactionType.valueOf(itemEntry.type());

            switch (type) {
                case FILLER -> {
                    material = Material.valueOf(item.material());
                    name = MiniMessage.miniMessage().deserialize(item.name()).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE);
                    list1 = new ArrayList<>();
                    if (item.lore() != null) {
                        for (String loreLine : item.lore()) {
                            list1.add(MiniMessage.miniMessage().deserialize(loreLine).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE));
                        }
                    }

                    for (i = 0; i <= page.size() - 1; i++) {
                        addButton(i, (new InventoryButton.Builder())
                                .setItemStack(new ItemStack(material))
                                .setItemName(name)
                                .setLore(list1)
                                .setAction(event -> {
                                })
                                .build());
                    }
                }

                case PREVIOUS_PAGE -> {
                    lore = new ArrayList<>();
                    if (item.lore() != null) {
                        for (String loreLine : item.lore()) {
                            lore.add(MiniMessage.miniMessage().deserialize(loreLine).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE));
                        }
                    }

                    addButton(itemEntry.slot(), (new InventoryButton.Builder())
                            .setItemStack(new ItemStack(Material.valueOf(item.material())))
                            .setItemName(MiniMessage.miniMessage().deserialize(item.name()).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE))
                            .setLore(lore)
                            .setAction(event -> {
                                List<Map.Entry<String, MenuConfiguration.MenuPage>> pageList = menuManager.getMenuConfiguration().pages().entrySet().stream().toList();
                                int prevPageNum = pageNum - 1;
                                Map.Entry<String, MenuConfiguration.MenuPage> prevPage = pageList.get(prevPageNum);

                                Bukkit.getScheduler().runTaskLater(skyShop, () -> {
                                    event.getWhoClicked().closeInventory(InventoryCloseEvent.Reason.OPEN_NEW);
                                    inventoryManager.openGUI(new MenuGUI(skyShop, menuManager, shopManager, inventoryManager, localeManager, prevPage, prevPageNum), (Player) event.getWhoClicked());
                                }, 1L);
                            }).build());
                }

                case NEXT_PAGE -> {
                    lore = new ArrayList<>();
                    if (item.lore() != null) {
                        for (String loreLine : item.lore()) {
                            lore.add(MiniMessage.miniMessage().deserialize(loreLine).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE));
                        }
                    }

                    addButton(itemEntry.slot(), (new InventoryButton.Builder())
                            .setItemStack(new ItemStack(Material.valueOf(item.material())))
                            .setItemName(MiniMessage.miniMessage().deserialize(item.name()).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE))
                            .setLore(lore)
                            .setAction(event -> {
                                List<Map.Entry<String, MenuConfiguration.MenuPage>> pageList = menuManager.getMenuConfiguration().pages().entrySet().stream().toList();
                                int nextPageNum = pageNum + 1;
                                Map.Entry<String, MenuConfiguration.MenuPage> nextPage = pageList.get(nextPageNum);

                                Bukkit.getScheduler().runTaskLater(skyShop, () -> {
                                    event.getWhoClicked().closeInventory(InventoryCloseEvent.Reason.OPEN_NEW);
                                    inventoryManager.openGUI(new MenuGUI(skyShop, menuManager, shopManager, inventoryManager, localeManager, nextPage, nextPageNum), (Player) event.getWhoClicked());
                                }, 1L);
                            }).build());
                }

                case RETURN -> {
                    lore = new ArrayList<>();
                    if (item.lore() != null) {
                        for (String loreLine : item.lore()) {
                            lore.add(MiniMessage.miniMessage().deserialize(loreLine).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE));
                        }
                    }

                    addButton(itemEntry.slot(), (new InventoryButton.Builder())
                            .setItemStack(new ItemStack(Material.valueOf(item.material())))
                            .setItemName(MiniMessage.miniMessage().deserialize(item.name()).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE))
                            .setLore(lore)
                            .setAction(event -> Bukkit.getScheduler().runTaskLater(skyShop, () -> event.getWhoClicked().closeInventory(InventoryCloseEvent.Reason.OPEN_NEW), 1L))
                            .build());
                }

                case OPEN_SHOP -> {
                    lore = new ArrayList<>();
                    if (item.lore() != null) {
                        for (String loreLine : item.lore()) {
                            lore.add(MiniMessage.miniMessage().deserialize(loreLine).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE));
                        }
                    }

                    addButton(itemEntry.slot(), (new InventoryButton.Builder())
                            .setItemStack(new ItemStack(Material.valueOf(item.material())))
                            .setItemName(MiniMessage.miniMessage().deserialize(item.name()).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE))
                            .setLore(lore)
                            .setAction(event -> Bukkit.getScheduler().runTaskLater(skyShop, () -> {
                                event.getWhoClicked().closeInventory(InventoryCloseEvent.Reason.OPEN_NEW);
                                inventoryManager.openGUI(new ShopGUI(skyShop, menuManager, shopManager, inventoryManager, localeManager, shopManager.getShopConfig(itemEntry.shop()).pages().entrySet().stream().toList().getFirst(), itemEntry.shop(), 0), (Player) event.getWhoClicked());
                            }, 1L))


                            .build());
                }
            }
        }
        super.decorate();
    }
}
