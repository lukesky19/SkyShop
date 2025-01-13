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
import com.github.lukesky19.skylib.format.PlaceholderAPIUtil;
import com.github.lukesky19.skylib.gui.GUIButton;
import com.github.lukesky19.skylib.gui.InventoryGUI;
import com.github.lukesky19.skylib.player.PlayerUtil;
import com.github.lukesky19.skyshop.SkyShop;
import com.github.lukesky19.skyshop.SkyShopAPI;
import com.github.lukesky19.skyshop.configuration.manager.LocaleManager;
import com.github.lukesky19.skyshop.configuration.manager.SellAllManager;
import com.github.lukesky19.skyshop.configuration.manager.TransactionManager;
import com.github.lukesky19.skyshop.configuration.record.GUI;
import com.github.lukesky19.skyshop.configuration.record.Locale;
import com.github.lukesky19.skyshop.configuration.record.Transaction;
import com.github.lukesky19.skyshop.enums.ActionType;
import com.github.lukesky19.skyshop.event.CommandPurchasedEvent;
import com.github.lukesky19.skyshop.event.CommandSoldEvent;
import com.github.lukesky19.skyshop.event.ItemPurchasedEvent;
import com.github.lukesky19.skyshop.event.ItemSoldEvent;
import com.github.lukesky19.skyshop.manager.StatsDatabaseManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * This class is called to create a transaction inventory for a player to buy and sell items.
 */
public class TransactionGUI extends InventoryGUI {
    private final SkyShop skyShop;
    private final LocaleManager localeManager;
    private final StatsDatabaseManager statsDatabaseManager;
    private final SkyShopAPI skyShopAPI;
    private final SellAllManager sellAllManager;
    private final ShopGUI shopGUI;
    private final GUI.Entry shopEntry;
    private final GUI.Item shopItem;
    private final double buyPrice;
    private final double sellPrice;
    private final ActionType type;
    private final List<String> buyCommands;
    private final List<String> sellCommands;
    private Integer pageNum;
    private final Player player;
    private final LinkedHashMap<Integer, Transaction.Page> pages;
    private Transaction.Page pageConfig;

    /**
     * Constructor
     * @param skyShop The plugin's instance.
     * @param localeManager A LocaleManager instance.
     * @param shopGUI The shop that the player came from.
     * @param pageNum The page number of the current transaction GUI.
     */
    public TransactionGUI(
            SkyShop skyShop,
            LocaleManager localeManager,
            TransactionManager transactionManager,
            StatsDatabaseManager statsDatabaseManager, SkyShopAPI skyShopAPI, SellAllManager sellAllManager,
            ShopGUI shopGUI,
            GUI.Entry shopEntry,
            GUI.Item shopItem,
            ActionType type,
            Integer pageNum,
            Player player) {
        this.skyShop = skyShop;
        this.localeManager = localeManager;
        this.statsDatabaseManager = statsDatabaseManager;
        this.skyShopAPI = skyShopAPI;
        this.sellAllManager = sellAllManager;
        this.shopGUI = shopGUI;
        this.shopEntry = shopEntry;
        this.shopItem = shopItem;
        this.buyPrice = shopEntry.prices().buyPrice();
        this.sellPrice = shopEntry.prices().sellPrice();
        this.type = type;
        this.buyCommands = shopEntry.commands().buyCommands();
        this.sellCommands = shopEntry.commands().sellCommands();
        this.pageNum = pageNum;
        this.player = player;

        Transaction transactionConfig = transactionManager.getTransactionGuiConfig();

        if(type.equals(ActionType.ITEM)) {
            this.pages = transactionConfig.items();
        } else {
            this.pages = transactionConfig.commands();
        }

        // Get the page config
        this.pageConfig = pages.get(pageNum);

        createInventory();
        decorate();
    }

    /**
     * A method to create the base structure of the inventory GUI.
    */
    public void createInventory() {
        final ComponentLogger logger = skyShop.getComponentLogger();
        final Locale locale = localeManager.getLocale();
        int invSize = pageConfig.size();

        // Verify GUI Size
        if(invSize % 9 != 0 || invSize < 9 || invSize > 54) {
            List<TagResolver.Single> placeholders = List.of(Placeholder.parsed("file", "transaction.yml"));

            player.sendMessage(FormatUtil.format(locale.prefix() + locale.guiOpenError(), placeholders));

            logger.error(FormatUtil.format(locale.guiInvalidSize(), placeholders));

            Bukkit.getScheduler().runTaskLater(skyShop, () -> player.closeInventory(InventoryCloseEvent.Reason.UNLOADED), 1L);

            return;
        }

        if(pageConfig.name() != null) {
            Component menuName = FormatUtil.format(player, pageConfig.name());
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

        List<TagResolver.Single> errorPlaceholders = new ArrayList<>(List.of(Placeholder.parsed("file", "transaction.yml")));

        pageConfig = pages.get(pageNum);

        // Add the page number to the placeholders.
        errorPlaceholders.add(Placeholder.parsed("page", String.valueOf(pageNum)));

        clearButtons();

        Map<Integer, Transaction.Entry> entries = pageConfig.entries();
        if(entries == null || entries.isEmpty()) {
            logger.error(FormatUtil.format(locale.noEntriesFound(), errorPlaceholders));
            return;
        }

        for(Map.Entry<Integer, Transaction.Entry> itemEntry : pageConfig.entries().entrySet()) {
            int entryNum = itemEntry.getKey();
            Transaction.Entry entryConfig = itemEntry.getValue();
            ActionType transactionType = ActionType.getActionType(entryConfig.type());
            Transaction.Item itemConfig = entryConfig.item();

            List<TagResolver.Single> pricePlaceholders = List.of(
                    Placeholder.parsed("buy_price", String.valueOf(shopEntry.prices().buyPrice())),
                    Placeholder.parsed("sell_price", String.valueOf(shopEntry.prices().sellPrice())));

            // Add the entry number to the placeholders.
            errorPlaceholders.add(Placeholder.parsed("entry", String.valueOf(entryNum)));

            List<Component> loreList = entryConfig.item().lore().stream()
                    .map(loreLine -> FormatUtil.format(player, loreLine, pricePlaceholders))
                    .toList();

            switch(transactionType) {
                case FILLER -> {
                    Material material = Material.getMaterial(itemConfig.material());
                    if(material != null) {
                        for (int i = 0; i <= pageConfig.size() - 1; i++) {
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
                        logger.error(FormatUtil.format(locale.skippingEntryInvalidMaterial(), errorPlaceholders));
                    }
                }

                case DISPLAY -> {
                    Material material = Material.getMaterial(shopItem.material());
                    if(material != null) { // The material should never be null at this point
                        GUIButton.Builder builder = new GUIButton.Builder();

                        builder.setMaterial(material);

                        String name = shopItem.name();
                        if (name != null) {
                            builder.setItemName(FormatUtil.format(player, name));
                        }

                        List<Component> displayLore = shopItem.lore().stream()
                                .map(loreLine -> FormatUtil.format(player, loreLine, pricePlaceholders))
                                .toList();

                        builder.setLore(displayLore);

                        setButton(entryConfig.slot(), builder.build());
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

                        builder.setAction(event -> {
                            closeInventory(skyShop, player);
                            shopGUI.openInventory(skyShop, player);
                        });

                        setButton(entryConfig.slot(), builder.build());
                    } else {
                        logger.error(FormatUtil.format(locale.skippingEntryInvalidMaterial(), errorPlaceholders));
                    }
                }

                case NEXT_PAGE -> {
                    if(pages.get(pageNum + 1) != null) {
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
                        logger.error(FormatUtil.format(locale.skippingEntryInvalidMaterial(), errorPlaceholders));
                    }
                }

                case PREVIOUS_PAGE -> {
                    if(pages.get(pageNum - 1) != null) {
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
                        logger.error(FormatUtil.format(locale.skippingEntryInvalidMaterial(), errorPlaceholders));
                    }
                }

                case SELL_GUI -> {
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
                            closeInventory(skyShop, player);
                            new SellAllGUI(skyShop, localeManager, sellAllManager, skyShopAPI, player).openInventory(skyShop, player);
                        });

                        setButton(entryConfig.slot(), builder.build());
                    } else {
                        logger.error(FormatUtil.format(locale.skippingEntryInvalidMaterial(), errorPlaceholders));
                    }
                }

                case SELL_ALL -> {
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
                            Material transactionMaterial = Material.getMaterial(shopItem.material());
                            if(transactionMaterial != null) {
                                ItemStack transactionItemStack = new ItemStack(transactionMaterial);
                                skyShopAPI.sellAllMatchingItemStack(player, transactionItemStack, true);
                            }
                        });

                        setButton(entryConfig.slot(), builder.build());
                    } else {
                        logger.error(FormatUtil.format(locale.skippingEntryInvalidMaterial(), errorPlaceholders));
                    }
                }

                case BUY -> {
                    if(buyPrice >= 0.0) {
                        int amount = entryConfig.amount();
                        double price = buyPrice * amount;

                        List<TagResolver.Single> buyPlaceholders = new ArrayList<>();
                        buyPlaceholders.add(Placeholder.parsed("buy_price", String.valueOf(price)));
                        buyPlaceholders.add(Placeholder.parsed("amount", String.valueOf(amount)));

                        List<Component> buyLore = itemConfig.lore().stream().map(loreLine ->
                                FormatUtil.format(player, loreLine, buyPlaceholders)).toList();

                        Material material = Material.getMaterial(itemConfig.material());
                        if(material != null) {
                            GUIButton.Builder builder = new GUIButton.Builder();

                            builder.setMaterial(material);

                            String name = itemConfig.name();
                            if (name != null) {
                                builder.setItemName(FormatUtil.format(name, buyPlaceholders));
                            }

                            builder.setLore(buyLore);

                            builder.setAction(event -> {
                                if(type.equals(ActionType.ITEM)) {
                                    buyItem(shopItem.material(), amount, price);
                                }

                                if(type.equals(ActionType.COMMAND)) {
                                    buyCommand(amount, price);
                                }
                            });

                            setButton(entryConfig.slot(), builder.build());
                        } else {
                            logger.error(FormatUtil.format(locale.skippingEntryInvalidMaterial(), errorPlaceholders));
                        }
                    }
                }

                case SELL -> {
                    if(sellPrice >= 0.0) {
                        int amount = entryConfig.amount();
                        double price = sellPrice * amount;

                        List<TagResolver.Single> sellPlaceholders = new ArrayList<>();
                        sellPlaceholders.add(Placeholder.parsed("sell_price", String.valueOf(price)));
                        sellPlaceholders.add(Placeholder.parsed("amount", String.valueOf(amount)));

                        List<Component> sellLore = itemConfig.lore().stream().map(loreLine ->
                                FormatUtil.format(player, loreLine, sellPlaceholders)).toList();

                        Material material = Material.getMaterial(itemConfig.material());
                        if(material != null) {
                            GUIButton.Builder builder = new GUIButton.Builder();

                            builder.setMaterial(material);

                            String name = itemConfig.name();
                            if (name != null) {
                                builder.setItemName(FormatUtil.format(name, sellPlaceholders));
                            }

                            builder.setLore(sellLore);

                            builder.setAction(event -> {
                                if (type.equals(ActionType.ITEM)) {
                                    sellItem(shopItem.material(), amount, price);
                                }

                                if (type.equals(ActionType.COMMAND)) {
                                    sellCommand(amount, price);
                                }
                            });

                            setButton(entryConfig.slot(), builder.build());
                        } else {
                            logger.error(FormatUtil.format(locale.skippingEntryInvalidMaterial(), errorPlaceholders));
                        }
                    }
                }

                case null -> logger.warn(FormatUtil.format(locale.skippingEntryInvalidType(), errorPlaceholders));

                default -> logger.warn(FormatUtil.format(locale.skippingEntryTypeNotAllowed(), errorPlaceholders));
            }
        }

        super.decorate();
    }

    /**
     * Close the inventory with an OPEN_NEW reason.
     * @param plugin The Plugin closing the inventory.
     * @param player The Player to close the inventory for.
     */
    @Override
    public void closeInventory(@NotNull Plugin plugin, @NotNull Player player) {
        Bukkit.getScheduler().runTaskLater(plugin, () -> player.closeInventory(InventoryCloseEvent.Reason.OPEN_NEW), 1L);
    }

    /**
     * Re-open the ShopGUI if the reason the inventory closed was not OPEN_NEW or UNLOADED.
     * @param event InventoryCloseEvent
     */
    @Override
    public void handleClose(InventoryCloseEvent event) {
        if(!event.getReason().equals(InventoryCloseEvent.Reason.OPEN_NEW) && !event.getReason().equals(InventoryCloseEvent.Reason.UNLOADED)) {
            Player player = (Player) event.getPlayer();
            shopGUI.openInventory(skyShop, player);
        }
    }

    /**
     * This method contains the logic to purchase an item.
     * @param materialName The name of the material being purchased.
     * @param amount The amount being purchased.
     * @param price The price of the item being purchased.
     */
    private void buyItem(String materialName, int amount, double price) {
        Locale locale = localeManager.getLocale();
        Material material = Material.getMaterial(materialName);
        if(material == null) return;

        ItemStack buyItem = new ItemStack(material);
        buyItem.setAmount(amount);

        if (skyShop.getEconomy().getBalance(player) >= price) {
            ItemPurchasedEvent itemPurchasedEvent = new ItemPurchasedEvent(buyItem);
            skyShop.getServer().getPluginManager().callEvent(itemPurchasedEvent);

            if (!itemPurchasedEvent.isCancelled()) {
                skyShop.getEconomy().withdrawPlayer(player, price);

                PlayerUtil.giveItem(player.getInventory(), buyItem, amount, player.getLocation());

                List<TagResolver.Single> successPlaceholders = new ArrayList<>();
                successPlaceholders.add(Placeholder.parsed("amount", String.valueOf(amount)));
                successPlaceholders.add(Placeholder.parsed("item", shopItem.name()));
                successPlaceholders.add(Placeholder.parsed("price", String.valueOf(price)));
                successPlaceholders.add(Placeholder.parsed("bal", String.valueOf(skyShop.getEconomy().getBalance(player))));

                player.sendMessage(FormatUtil.format(player, locale.prefix() + locale.buySuccess(), successPlaceholders));

                if (statsDatabaseManager != null) {
                    skyShop.getServer().getScheduler().runTaskAsynchronously(skyShop, () -> {
                        try {
                            statsDatabaseManager.updateMaterial(buyItem.getType().toString(), amount, 0);
                        } catch (SQLException e) {
                            throw new RuntimeException(e);
                        }
                    });
                }
            }
        } else {
            player.sendMessage(FormatUtil.format(player, locale.prefix() + locale.insufficientFunds()));
            Bukkit.getScheduler().runTaskLater(skyShop, () -> player.closeInventory(InventoryCloseEvent.Reason.UNLOADED), 1L);
        }
    }

    /**
     * This method contains the logic to sell an item.
     * @param materialName The name of the material being sold.
     * @param amount The amount being sold.
     * @param price The price of the item being sold.
     */
    private void sellItem(String materialName, int amount, double price) {
        Locale locale = localeManager.getLocale();
        Material material = Material.getMaterial(materialName);
        if(material == null) return;

        ItemStack sellItem = new ItemStack(material);
        sellItem.setAmount(amount);

        if (player.getInventory().containsAtLeast(sellItem, amount)) {
            ItemSoldEvent itemSoldEvent = new ItemSoldEvent(sellItem);
            skyShop.getServer().getPluginManager().callEvent(itemSoldEvent);

            if (!itemSoldEvent.isCancelled()) {
                player.getInventory().removeItem(sellItem);
                skyShop.getEconomy().depositPlayer(player, price);

                List<TagResolver.Single> successPlaceholders = new ArrayList<>();
                successPlaceholders.add(Placeholder.parsed("amount", String.valueOf(amount)));
                successPlaceholders.add(Placeholder.parsed("item", shopItem.name()));
                successPlaceholders.add(Placeholder.parsed("price", String.valueOf(price)));
                successPlaceholders.add(Placeholder.parsed("bal", String.valueOf(skyShop.getEconomy().getBalance(player))));

                player.sendMessage(FormatUtil.format(player, locale.prefix() + locale.sellSuccess(), successPlaceholders));

                if (statsDatabaseManager != null) {
                    skyShop.getServer().getScheduler().runTaskAsynchronously(skyShop, () -> {
                        try {
                            statsDatabaseManager.updateMaterial(sellItem.getType().toString(), 0, amount);
                        } catch (SQLException e) {
                            throw new RuntimeException(e);
                        }
                    });
                }
            }
        } else {
            player.sendMessage(FormatUtil.format(player, locale.prefix() + locale.notEnoughItems()));
            Bukkit.getScheduler().runTaskLater(skyShop, () -> player.closeInventory(InventoryCloseEvent.Reason.UNLOADED), 1L);
        }
    }

    /**
     * This method contains the logic to purchase a command.
     * @param amount The amount being purchased.
     * @param price The price of the item being purchased.
     */
    private void buyCommand(int amount, double price) {
        Locale locale = localeManager.getLocale();

        if (skyShop.getEconomy().getBalance(player) >= price) {
            CommandPurchasedEvent commandPurchasedEvent = new CommandPurchasedEvent(buyCommands);
            skyShop.getServer().getPluginManager().callEvent(commandPurchasedEvent);

            if (!commandPurchasedEvent.isCancelled()) {
                for (String command : buyCommands) {
                    for (int i = 1; i <= amount; i++) {
                        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), PlaceholderAPIUtil.parsePlaceholders(player, command));
                    }
                }

                skyShop.getEconomy().withdrawPlayer(player, price);

                List<TagResolver.Single> successPlaceholders = new ArrayList<>();
                successPlaceholders.add(Placeholder.parsed("amount", String.valueOf(amount)));
                successPlaceholders.add(Placeholder.parsed("item", shopItem.name()));
                successPlaceholders.add(Placeholder.parsed("price", String.valueOf(price)));
                successPlaceholders.add(Placeholder.parsed("bal", String.valueOf(skyShop.getEconomy().getBalance(player))));

                player.sendMessage(FormatUtil.format(player, locale.prefix() + locale.buySuccess(), successPlaceholders));
            }
        } else {
            player.sendMessage(FormatUtil.format(player, locale.prefix() + locale.insufficientFunds()));
            Bukkit.getScheduler().runTaskLater(skyShop, () -> player.closeInventory(InventoryCloseEvent.Reason.UNLOADED), 1L);
        }
    }

    /**
     * This method contains the logic to purchase a command.
     * @param amount The amount being purchased.
     * @param price The price of the item being purchased.
     */
    private void sellCommand(int amount, double price) {
        Locale locale = localeManager.getLocale();

        CommandSoldEvent commandSoldEvent = new CommandSoldEvent(sellCommands);
        skyShop.getServer().getPluginManager().callEvent(commandSoldEvent);

        if (!commandSoldEvent.isCancelled()) {
            skyShop.getEconomy().depositPlayer(player, price);
            for (String command : sellCommands) {
                for (int i = 1; i <= amount; i++) {
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), PlaceholderAPIUtil.parsePlaceholders(player, command));
                }
            }

            List<TagResolver.Single> successPlaceholders = new ArrayList<>();
            successPlaceholders.add(Placeholder.parsed("amount", String.valueOf(amount)));
            successPlaceholders.add(Placeholder.parsed("item", shopItem.name()));
            successPlaceholders.add(Placeholder.parsed("price", String.valueOf(price)));
            successPlaceholders.add(Placeholder.parsed("bal", String.valueOf(skyShop.getEconomy().getBalance(player))));

            player.sendMessage(FormatUtil.format(player, locale.prefix() + locale.sellSuccess(), successPlaceholders));
        }
    }
}
