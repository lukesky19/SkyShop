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
package com.github.lukesky19.skyshop.gui;

import com.github.lukesky19.skylib.format.FormatUtil;
import com.github.lukesky19.skylib.gui.GUIButton;
import com.github.lukesky19.skylib.gui.InventoryGUI;
import com.github.lukesky19.skyshop.SkyShop;
import com.github.lukesky19.skyshop.SkyShopAPI;
import com.github.lukesky19.skyshop.configuration.manager.*;
import com.github.lukesky19.skyshop.configuration.record.GUI;
import com.github.lukesky19.skyshop.configuration.record.Locale;
import com.github.lukesky19.skyshop.enums.ActionType;
import com.github.lukesky19.skyshop.manager.StatsDatabaseManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryCloseEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * This class is called to create a menu inventory for a player to access the shop.
*/
public class MenuGUI extends InventoryGUI {
    private final SkyShop skyShop;
    private final SettingsManager settingsManager;
    private final ShopManager shopManager;
    private final LocaleManager localeManager;
    private final TransactionManager transactionManager;
    private final StatsDatabaseManager statsDatabaseManager;
    private final SkyShopAPI skyShopAPI;
    private final SellAllManager sellAllManager;
    private Integer pageNum;
    private final Player player;
    private final GUI menuConfig;

    /**
     * Constructor
     * @param skyShop The plugin's instance.
     * @param settingsManager A SettingsManager instance.
     * @param menuManager A MenuManager instance.
     * @param shopManager A ShopManager instance.
     * @param localeManager A LocaleManager instance.
     * @param transactionManager A TransactionManager instance.
     * @param statsDatabaseManager A StatsDatabaseManager instance.
     * @param skyShopAPI A SkyShopAPI instance.
     * @param sellAllManager A SellAllManager instance.
     * @param pageNum The page number associated with the GUI/Inventory being created.
     * @param player The player viewing the GUI/Inventory.
     */
    public MenuGUI(
            SkyShop skyShop,
            SettingsManager settingsManager,
            MenuManager menuManager,
            ShopManager shopManager,
            LocaleManager localeManager,
            TransactionManager transactionManager,
            StatsDatabaseManager statsDatabaseManager,
            SkyShopAPI skyShopAPI,
            SellAllManager sellAllManager,
            Integer pageNum,
            Player player) {
        this.skyShop = skyShop;
        this.settingsManager = settingsManager;
        this.shopManager = shopManager;
        this.localeManager = localeManager;
        this.transactionManager = transactionManager;
        this.statsDatabaseManager = statsDatabaseManager;
        this.skyShopAPI = skyShopAPI;
        this.sellAllManager = sellAllManager;
        this.pageNum = pageNum;
        this.player = player;
        menuConfig = menuManager.getMenuConfig();

        createInventory();
        decorate();
    }

    /**
     * A method to create the base structure of the inventory GUI.
    */
    public void createInventory() {
        final ComponentLogger logger = skyShop.getComponentLogger();
        final Locale locale = localeManager.getLocale();
        int invSize = menuConfig.gui().size();

        // Verify GUI Size
        if(invSize % 9 != 0 || invSize < 9 || invSize > 54) {
            List<TagResolver.Single> placeholders = List.of(Placeholder.parsed("file", "menu.yml"));

            player.sendMessage(FormatUtil.format(locale.prefix() + locale.guiOpenError(), placeholders));

            logger.error(FormatUtil.format(locale.guiInvalidSize(), placeholders));

            Bukkit.getScheduler().runTaskLater(skyShop, () -> player.closeInventory(InventoryCloseEvent.Reason.UNLOADED), 1L);

            return;
        }

        // Create the Inventory based on whether a name is configured or not
        if(menuConfig.gui().name() != null) {
            Component menuName = FormatUtil.format(player, menuConfig.gui().name());
            setInventory(skyShop.getServer().createInventory(this, invSize, menuName));
        } else {
            setInventory(skyShop.getServer().createInventory(this, invSize));
        }
    }

    /**
     * A method to create all the buttons in the inventory GUI.
    */
    public void decorate() {
        final ComponentLogger logger = skyShop.getComponentLogger();
        final Locale locale = localeManager.getLocale();

        // Clear the GUI of buttons
        clearButtons();

        // Create the placeholders list for errors with the config file associated with this GUI
        List<TagResolver.Single> placeholders = new ArrayList<>(List.of(Placeholder.parsed("file", "menu.yml")));

        // Check if at least 1 page is configured.
        Map<Integer, GUI.Page> pages = menuConfig.gui().pages();
        if(pages == null || pages.isEmpty()) {
            logger.error(FormatUtil.format(locale.noPagesFound(), placeholders));
            return;
        }

        // Get the page config
        GUI.Page page = pages.get(pageNum);

        // Add the page number to the placeholders.
        placeholders.add(Placeholder.parsed("page", String.valueOf(pageNum)));

        // Check if at least 1 entry is configured.
        Map<Integer, GUI.Entry> entries  = page.entries();
        if(entries == null || entries.isEmpty()) {
            logger.error(FormatUtil.format(locale.noEntriesFound(), placeholders));
            return;
        }

        // Loop through entries to populate the GUI
        for (Map.Entry<Integer, GUI.Entry> itemEntry : page.entries().entrySet()) {
            // Entry num (key)
            int entryNum = itemEntry.getKey();
            // Entry config (value)
            GUI.Entry entryConfig = itemEntry.getValue();
            // Entry type / ActionType
            ActionType type = ActionType.getActionType(entryConfig.type());
            // Item config
            GUI.Item itemConfig = entryConfig.item();

            // Add the entry number to the placeholders.
            placeholders.add(Placeholder.parsed("entry", String.valueOf(entryNum)));

            // Parse and format the lore of the item.
            List<Component> loreList = itemConfig.lore().stream()
                    .map(loreLine -> FormatUtil.format(player, loreLine))
                    .toList();

            // Create the GUIButton based on the ActionType
            switch (type) {
                case FILLER -> {
                    Material material = Material.getMaterial(itemConfig.material());
                    if(material != null) {
                        for (int i = 0; i <= menuConfig.gui().size() - 1; i++) {
                            GUIButton.Builder builder = new GUIButton.Builder();

                            builder.setMaterial(material);

                            String name = itemConfig.name();
                            if (name != null) {
                                builder.setItemName(FormatUtil.format(player, name));
                            }

                            builder.setLore(loreList);

                            setButton(i, builder.build());
                        }
                    } else {
                        logger.error(FormatUtil.format(locale.skippingEntryInvalidMaterial(), placeholders));
                    }
                }

                case PREVIOUS_PAGE -> {
                    if(menuConfig.gui().pages().get(pageNum - 1) != null) {
                        Material material = Material.getMaterial(itemConfig.material());
                        if(material != null) {
                            GUIButton.Builder builder = new GUIButton.Builder();

                            builder.setMaterial(material);

                            String name = itemConfig.name();
                            if (name != null) {
                                builder.setItemName(FormatUtil.format(player, name));
                            }

                            builder.setLore(loreList);

                            builder.setAction(event -> {
                                pageNum = pageNum - 1;
                                decorate();
                            });

                            setButton(entryConfig.slot(), builder.build());
                        }
                    } else {
                        logger.error(FormatUtil.format(locale.skippingEntryInvalidMaterial(), placeholders));
                    }
                }

                case NEXT_PAGE -> {
                    if(menuConfig.gui().pages().get(pageNum + 1) != null) {
                        Material material = Material.getMaterial(itemConfig.material());
                        if(material != null) {
                            GUIButton.Builder builder = new GUIButton.Builder();

                            builder.setMaterial(material);

                            String name = itemConfig.name();
                            if (name != null) {
                                builder.setItemName(FormatUtil.format(player, name));
                            }

                            builder.setLore(loreList);

                            builder.setAction(event -> {
                                pageNum = pageNum + 1;
                                decorate();
                            });

                            setButton(entryConfig.slot(), builder.build());
                        }
                    } else {
                        logger.error(FormatUtil.format(locale.skippingEntryInvalidMaterial(), placeholders));
                    }
                }

                case RETURN -> {
                    Material material = Material.getMaterial(itemConfig.material());
                    if(material != null) {
                        GUIButton.Builder builder = new GUIButton.Builder();

                        builder.setMaterial(material);

                        String name = itemConfig.name();
                        if (name != null) {
                            builder.setItemName(FormatUtil.format(player, name));
                        }

                        builder.setLore(loreList);

                        builder.setAction(event -> closeInventory(skyShop, player));

                        setButton(entryConfig.slot(), builder.build());
                    } else {
                        logger.error(FormatUtil.format(locale.skippingEntryInvalidMaterial(), placeholders));
                    }
                }

                case OPEN_SHOP -> {
                    Material material = Material.getMaterial(itemConfig.material());
                    if(material != null) {
                        GUIButton.Builder builder = new GUIButton.Builder();

                        builder.setMaterial(material);

                        String name = itemConfig.name();
                        if (name != null) {
                            builder.setItemName(FormatUtil.format(player, name));
                        }

                        builder.setLore(loreList);

                        builder.setAction(event -> Bukkit.getScheduler().runTaskLater(skyShop, () -> {
                            GUI shopConfig = shopManager.getShopConfig(entryConfig.shop());

                            if(shopConfig != null) {
                                ShopGUI gui = new ShopGUI(skyShop, settingsManager, localeManager, transactionManager, statsDatabaseManager, skyShopAPI, sellAllManager, this, pageNum, entryConfig.shop(), shopConfig, player);
                                closeInventory(skyShop, player);
                                gui.openInventory(skyShop, player);
                            } else {
                                player.sendMessage(FormatUtil.format(locale.prefix() + locale.guiOpenError()));
                                closeInventory(skyShop, player);
                            }
                        }, 1L));

                        setButton(entryConfig.slot(), builder.build());
                    } else {
                        logger.error(FormatUtil.format(locale.skippingEntryInvalidMaterial(), placeholders));
                    }
                }

                case null -> logger.warn(FormatUtil.format(locale.skippingEntryInvalidType(), placeholders));

                default -> logger.warn(FormatUtil.format(locale.skippingEntryTypeNotAllowed(), placeholders));
            }
        }

        super.decorate();
    }
}
