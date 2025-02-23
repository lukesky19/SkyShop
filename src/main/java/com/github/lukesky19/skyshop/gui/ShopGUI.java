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
import com.github.lukesky19.skylib.gui.GUIType;
import com.github.lukesky19.skylib.gui.abstracts.ChestGUI;
import com.github.lukesky19.skyshop.SkyShop;
import com.github.lukesky19.skyshop.SkyShopAPI;
import com.github.lukesky19.skyshop.configuration.manager.LocaleManager;
import com.github.lukesky19.skyshop.configuration.manager.SellAllManager;
import com.github.lukesky19.skyshop.configuration.manager.SettingsManager;
import com.github.lukesky19.skyshop.configuration.manager.TransactionManager;
import com.github.lukesky19.skyshop.configuration.record.GUI;
import com.github.lukesky19.skyshop.configuration.record.Locale;
import com.github.lukesky19.skyshop.enums.ActionType;
import com.github.lukesky19.skyshop.manager.StatsDatabaseManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * This class is called to create a shop inventory for a player to access an individual shop category.
*/
public class ShopGUI extends ChestGUI {
    private final SkyShop skyShop;
    private final SettingsManager settingsManager;
    private final LocaleManager localeManager;
    private final TransactionManager transactionManager;
    private final StatsDatabaseManager statsDatabaseManager;
    private final SkyShopAPI skyShopAPI;
    private final SellAllManager sellAllManager;
    private final GUIManager guiManager;
    private final MenuGUI menuGUI;
    private int pageNum;
    private final Player player;
    private final GUI shopConfig;

    /**
     * Constructor
     * @param skyShop The plugin's instance.
     * @param settingsManager A SettingsManager instance.
     * @param localeManager A LocaleManager instance.
     * @param transactionManager A TransactionManager instance.
     * @param statsDatabaseManager A StatsDatabaseManager instance.
     * @param skyShopAPI A SkyShopAPI instance.
     * @param sellAllManager A SellAllManager instance.
     * @param guiManager A GUIManager instance.
     * @param menuGUI The MenuGUI the player opened this GUI/Inventory from.
     * @param pageNum The page number associated with the GUI/Inventory being created.
     * @param shopConfig The ShopConfig associated with the GUI/Inventory being created.
     * @param player The player viewing the GUI/Inventory.
     */
    public ShopGUI(
            SkyShop skyShop,
            SettingsManager settingsManager,
            LocaleManager localeManager,
            TransactionManager transactionManager,
            StatsDatabaseManager statsDatabaseManager,
            SkyShopAPI skyShopAPI,
            SellAllManager sellAllManager,
            GUIManager guiManager,
            MenuGUI menuGUI,
            int pageNum,
            GUI shopConfig,
            Player player) {
        this.skyShop = skyShop;
        this.settingsManager = settingsManager;
        this.localeManager = localeManager;
        this.transactionManager = transactionManager;
        this.statsDatabaseManager = statsDatabaseManager;
        this.skyShopAPI = skyShopAPI;
        this.sellAllManager = sellAllManager;
        this.guiManager = guiManager;
        this.menuGUI = menuGUI;
        this.pageNum = pageNum;
        this.player = player;
        this.shopConfig = shopConfig;

        GUIType type = GUIType.getType(shopConfig.gui().guiType());
        if(type == null) {
            throw new RuntimeException("Invalid GUIType");
        }

        String guiName = "";
        if(shopConfig.gui().name() != null) guiName = shopConfig.gui().name();

        create(player, type, guiName, null);

        update();
    }

    /**
     * A method to create all the buttons in the inventory GUI.
    */
    @Override
    public void update() {
        final ComponentLogger logger = skyShop.getComponentLogger();
        final Locale locale = localeManager.getLocale();

        int guiSize = getInventory().getSize();

        // Clear the GUI of buttons
        clearButtons();

        // Create the placeholders list for errors with the config file associated with this GUI
        List<TagResolver.Single> errorPlaceholders = new ArrayList<>(List.of(Placeholder.parsed("file", "menu.yml")));

        // Check if at least 1 page is configured.
        Map<Integer, GUI.Page> pages = shopConfig.gui().pages();
        if(pages == null || pages.isEmpty()) {
            logger.error(FormatUtil.format(locale.noPagesFound(), errorPlaceholders));
            return;
        }

        // Get the page config.
        GUI.Page page = pages.get(pageNum);

        // Add the page number to the placeholders.
        errorPlaceholders.add(Placeholder.parsed("page", String.valueOf(pageNum)));

        // Check if at least 1 entry is configured.
        Map<Integer, GUI.Entry> entries  = page.entries();
        if(entries == null || entries.isEmpty()) {
            logger.error(FormatUtil.format(locale.noEntriesFound(), errorPlaceholders));
            return;
        }

        // Loop through the entries to populate the GUI
        for(Map.Entry<Integer, GUI.Entry> itemEntry : page.entries().entrySet()) {
            // Entry num (key)
            int entryNum = itemEntry.getKey();
            // Entry config (value)
            GUI.Entry entryConfig = itemEntry.getValue();
            // Entry type / ActionType
            ActionType type = ActionType.getActionType(entryConfig.type());
            // Item config
            GUI.Item itemConfig = entryConfig.item();

            // Add the entry number to the placeholders.
            errorPlaceholders.add(Placeholder.parsed("entry", String.valueOf(entryNum)));

            // Create the placeholders list with the associated buy and sell prices for the item.
            List<TagResolver.Single> pricePlaceholders = List.of(
                    Placeholder.parsed("buy_price", String.valueOf(entryConfig.prices().buyPrice())),
                    Placeholder.parsed("sell_price", String.valueOf(entryConfig.prices().sellPrice())));

            // Parse and format the lore of the item.
            List<Component> loreList = itemConfig.lore().stream()
                    .map(loreLine -> FormatUtil.format(player, loreLine, pricePlaceholders))
                    .toList();

            // Create the GUIButton based on the ActionType
            switch (type) {
                case FILLER -> {
                    Material material = Material.getMaterial(itemConfig.material());
                    if (material != null) {
                        for (int i = 0; i <= guiSize - 1; i++) {
                            GUIButton.Builder builder = new GUIButton.Builder();

                            ItemStack itemStack = ItemStack.of(material);
                            ItemMeta meta = itemStack.getItemMeta();

                            if(itemConfig.name() != null) {
                                meta.displayName(FormatUtil.format(itemConfig.name()));
                            }

                            meta.lore(loreList);

                            itemStack.setItemMeta(meta);

                            builder.setItemStack(itemStack);

                            setButton(i, builder.build());
                        }
                    } else {
                        logger.error(FormatUtil.format(locale.skippingEntryInvalidMaterial(), errorPlaceholders));
                    }
                }

                case PREVIOUS_PAGE -> {
                    if (shopConfig.gui().pages().get(pageNum - 1) != null) {
                        Material material = Material.getMaterial(itemConfig.material());
                        if (material != null) {
                            GUIButton.Builder builder = new GUIButton.Builder();

                            ItemStack itemStack = ItemStack.of(material);
                            ItemMeta meta = itemStack.getItemMeta();

                            if(itemConfig.name() != null) {
                                meta.displayName(FormatUtil.format(itemConfig.name()));
                            }

                            meta.lore(loreList);

                            itemStack.setItemMeta(meta);

                            builder.setItemStack(itemStack);

                            builder.setAction(event -> {
                                pageNum = pageNum - 1;
                                update();
                            });

                            setButton(entryConfig.slot(), builder.build());
                        }
                    } else {
                        logger.error(FormatUtil.format(locale.skippingEntryInvalidMaterial(), errorPlaceholders));
                    }
                }

                case NEXT_PAGE -> {
                    if (shopConfig.gui().pages().get(pageNum + 1) != null) {
                        Material material = Material.getMaterial(itemConfig.material());
                        if (material != null) {
                            GUIButton.Builder builder = new GUIButton.Builder();

                            ItemStack itemStack = ItemStack.of(material);
                            ItemMeta meta = itemStack.getItemMeta();

                            if(itemConfig.name() != null) {
                                meta.displayName(FormatUtil.format(itemConfig.name()));
                            }

                            meta.lore(loreList);

                            itemStack.setItemMeta(meta);

                            builder.setItemStack(itemStack);

                            builder.setAction(event -> {
                                pageNum = pageNum + 1;
                                update();
                            });

                            setButton(entryConfig.slot(), builder.build());
                        }
                    } else {
                        logger.error(FormatUtil.format(locale.skippingEntryInvalidMaterial(), errorPlaceholders));
                    }
                }

                case RETURN -> {
                    Material material = Material.getMaterial(itemConfig.material());
                    if (material != null) {
                        GUIButton.Builder builder = new GUIButton.Builder();

                        ItemStack itemStack = ItemStack.of(material);
                        ItemMeta meta = itemStack.getItemMeta();

                        if(itemConfig.name() != null) {
                            meta.displayName(FormatUtil.format(itemConfig.name()));
                        }

                        meta.lore(loreList);

                        itemStack.setItemMeta(meta);

                        builder.setItemStack(itemStack);

                        builder.setAction(event -> close(skyShop, player));

                        setButton(entryConfig.slot(), builder.build());
                    } else {
                        logger.error(FormatUtil.format(locale.skippingEntryInvalidMaterial(), errorPlaceholders));
                    }
                }

                case ITEM, COMMAND -> {
                    Material material = Material.getMaterial(itemConfig.material());
                    if (material != null) {
                        GUIButton.Builder builder = new GUIButton.Builder();

                        ItemStack itemStack = ItemStack.of(material);
                        ItemMeta meta = itemStack.getItemMeta();

                        if(itemConfig.name() != null) {
                            meta.displayName(FormatUtil.format(itemConfig.name()));
                        }

                        meta.lore(loreList);

                        itemStack.setItemMeta(meta);

                        builder.setItemStack(itemStack);

                        builder.setAction(event -> {
                            TransactionGUI gui = new TransactionGUI(skyShop, settingsManager, localeManager, transactionManager, statsDatabaseManager, skyShopAPI, sellAllManager, guiManager, this, entryConfig, itemConfig, type, 0, player);

                            skyShop.getServer().getScheduler().runTaskLater(skyShop, () ->
                                    player.closeInventory(InventoryCloseEvent.Reason.OPEN_NEW), 1L);

                            guiManager.removeOpenGUI(player.getUniqueId());

                            gui.open(skyShop, player);
                        });

                        setButton(entryConfig.slot(), builder.build());
                    } else {
                        logger.error(FormatUtil.format(locale.skippingEntryInvalidMaterial(), errorPlaceholders));
                    }
                }

                case null -> logger.warn(FormatUtil.format(locale.skippingEntryInvalidType(), errorPlaceholders));

                default -> logger.warn(FormatUtil.format(locale.skippingEntryTypeNotAllowed(), errorPlaceholders));
            }
        }

        super.update();
    }

    @Override
    public void open(@NotNull Plugin plugin, @NotNull Player player) {
        super.open(plugin, player);

        guiManager.addOpenGUI(player.getUniqueId(), this);
    }

    @Override
    public void close(@NotNull Plugin plugin, @NotNull Player player) {
        UUID uuid = player.getUniqueId();

        plugin.getServer().getScheduler().runTaskLater(plugin, () ->
                player.closeInventory(InventoryCloseEvent.Reason.OPEN_NEW), 1L);

        guiManager.removeOpenGUI(uuid);

        menuGUI.update();

        menuGUI.open(plugin, player);

        guiManager.addOpenGUI(uuid, menuGUI);
    }

    @Override
    public void unload(@NotNull Plugin plugin, @NotNull Player player, boolean onDisable) {
        UUID uuid = player.getUniqueId();

        player.closeInventory(InventoryCloseEvent.Reason.UNLOADED);

        guiManager.removeOpenGUI(uuid);
    }

    @Override
    public void handleClose(@NotNull InventoryCloseEvent inventoryCloseEvent) {
        if(inventoryCloseEvent.getReason().equals(InventoryCloseEvent.Reason.UNLOADED) || inventoryCloseEvent.getReason().equals(InventoryCloseEvent.Reason.OPEN_NEW)) return;

        Player player = (Player) inventoryCloseEvent.getPlayer();
        UUID uuid = player.getUniqueId();

        guiManager.removeOpenGUI(uuid);

        menuGUI.update();

        menuGUI.open(skyShop, player);

        guiManager.addOpenGUI(uuid, menuGUI);
    }
}
