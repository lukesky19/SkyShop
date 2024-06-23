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
import com.github.lukesky19.skyshop.configuration.menu.MenuManager;
import com.github.lukesky19.skyshop.configuration.shop.ShopConfiguration;
import com.github.lukesky19.skyshop.configuration.shop.ShopManager;
import com.github.lukesky19.skyshop.util.PlaceholderAPIUtil;
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
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * This class is called to create a transaction inventory for a player to buy and sell items.
 */
public class TransactionGUI extends InventoryGUI {
    final SkyShop skyShop;
    final MenuManager menuManager;
    final InventoryManager inventoryManager;
    final ShopManager shopManager;
    final LocaleManager localeManager;
    final Map.Entry<String, ShopConfiguration.ShopPage> shopPageEntry;
    final ShopConfiguration.ShopEntry shopEntry;
    final String shopId;
    final Integer shopPageNum;
    final Integer transPageNum;

    /**
     * Constructor
     * @param skyShop The plugin's instance.
     * @param menuManager A MenuManager instance.
     * @param shopManager A ShopManager instance.
     * @param inventoryManager A InventoryManager instance.
     * @param localeManager A LocaleManager instance.
     * @param shopPageEntry The configuration for the page that the player came from.
     * @param shopEntry The shop entry configuration for the item the player clicked.
     * @param shopId The shop id that the player came from.
     * @param shopPageNum The shop page number that the player came from.
     * @param transPageNum The page number of the current transaction GUI.
     */
    public TransactionGUI(
            SkyShop skyShop,
            MenuManager menuManager,
            InventoryManager inventoryManager,
            ShopManager shopManager,
            LocaleManager localeManager,
            Map.Entry<String, ShopConfiguration.ShopPage> shopPageEntry,
            ShopConfiguration.ShopEntry shopEntry,
            String shopId,
            Integer shopPageNum,
            Integer transPageNum) {
        this.skyShop = skyShop;
        this.menuManager = menuManager;
        this.inventoryManager = inventoryManager;
        this.shopManager = shopManager;
        this.localeManager = localeManager;
        this.shopPageEntry = shopPageEntry;
        this.shopEntry = shopEntry;
        this.shopId = shopId;
        this.shopPageNum = shopPageNum;
        this.transPageNum = transPageNum;
        createInventory();
    }

    /**
     * A method to create the base structure of the inventory GUI.
    */
    public void createInventory() {
        int size = 54;
        Component name = MiniMessage.miniMessage().deserialize("<aqua>Transaction Menu</aqua>");
        setInventory(Bukkit.createInventory(null, size, name));
    }

    /**
     * A method to create all the buttons in the inventory GUI.
    */
    public void decorate() {
        List<Component> displayLore;
        for(int i = 0; i <= 53; i++) {
            addButton(i, new InventoryButton.Builder()
                    .setItemStack(new ItemStack(Material.GRAY_STAINED_GLASS_PANE))
                    .setItemName(MiniMessage.miniMessage().deserialize(" ").decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE))
                    .setLore(new ArrayList<>())
                    .setAction(event -> {})
                    .build());
        }

        addButton(49, (new InventoryButton.Builder())
                .setItemStack(new ItemStack(Material.BARRIER))
                .setItemName(MiniMessage.miniMessage().deserialize("<yellow>Return to the previous menu.</yellow>").decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE))
                .setLore(new ArrayList<>())
                .setAction(event -> Bukkit.getScheduler().runTaskLater(skyShop, () -> {
                    event.getWhoClicked().closeInventory(InventoryCloseEvent.Reason.OPEN_NEW);
                    inventoryManager.openGUI(new ShopGUI(skyShop, menuManager, shopManager, inventoryManager, localeManager, shopPageEntry, shopId, shopPageNum), (Player) event.getWhoClicked());
                    }, 1L))
                .build());

        switch(TransactionType.valueOf(shopEntry.type())) {
            case ITEM -> {
                addButton(22, new InventoryButton.Builder()
                        .setItemStack(new ItemStack(Material.valueOf(shopEntry.item().material())))
                        .setItemName(MiniMessage.miniMessage().deserialize(shopEntry.item().name()).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE))
                        .setLore(new ArrayList<>())
                        .setAction(event -> {
                        })
                        .build());


                if(transPageNum == 1) {
                    addButton(51, new InventoryButton.Builder()
                            .setItemStack(new ItemStack(Material.ARROW))
                            .setItemName(MiniMessage.miniMessage().deserialize("<yellow>Next Page</yellow>").decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE))
                            .setLore(new ArrayList<>())
                            .setAction(event -> Bukkit.getScheduler().runTaskLater(skyShop, () -> {
                                event.getWhoClicked().closeInventory(InventoryCloseEvent.Reason.OPEN_NEW);
                                inventoryManager.openGUI(new TransactionGUI(skyShop, menuManager, inventoryManager, shopManager, localeManager, shopPageEntry, shopEntry, shopId, shopPageNum, 2), (Player) event.getWhoClicked());
                            }, 1L))
                            .build());

                    if(shopEntry.prices().buyPrice() != -1.0) {
                        int buySlot = 10;
                        int stackSize = 1;
                        for(int j = 1; j <= 7; j++) {
                            int finalStackSize = stackSize;
                            ItemStack displayItem = new ItemStack(Material.GREEN_STAINED_GLASS_PANE);
                            displayItem.setAmount(finalStackSize);
                            double finalPrice = shopEntry.prices().buyPrice() * finalStackSize;

                            List<Component> buyLore = new ArrayList<>();
                            buyLore.add(MiniMessage.miniMessage().deserialize("<yellow>Buy Price:</yellow> <white>" + finalPrice + "</white>").decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE));

                            addButton(buySlot, new InventoryButton.Builder()
                                    .setItemStack(displayItem)
                                    .setItemName(MiniMessage.miniMessage().deserialize("<green>Buy " + finalStackSize + "</green>").decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE))
                                    .setLore(buyLore)
                                    .setAction(event -> {
                                        FormattedLocale messages = localeManager.formattedLocale();
                                        ItemStack buyItem = new ItemStack(Material.valueOf(shopEntry.item().material()));
                                        buyItem.setAmount(finalStackSize);
                                        Player player = (Player) event.getWhoClicked();

                                        if(skyShop.getEconomy().getBalance(player) >= finalPrice) {
                                            if(player.getInventory().firstEmpty() != -1) {
                                                player.getInventory().addItem(buyItem);
                                                skyShop.getEconomy().withdrawPlayer(player, finalPrice);
                                                Component buySuccess = MiniMessage.miniMessage().deserialize(messages.buySuccess(), Placeholder.parsed("amount", String.valueOf(finalStackSize)), Placeholder.parsed("item", shopEntry.item().name()), Placeholder.parsed("price", String.valueOf(finalPrice)), Placeholder.parsed("bal", String.valueOf(skyShop.getEconomy().getBalance((OfflinePlayer) event.getWhoClicked()))));
                                                event.getWhoClicked().sendMessage(messages.prefix().append(buySuccess));
                                            }
                                        } else {
                                            event.getWhoClicked().sendMessage(messages.prefix().append(messages.insufficientFunds()));
                                            Bukkit.getScheduler().runTaskLater(skyShop, () -> event.getWhoClicked().closeInventory(InventoryCloseEvent.Reason.UNLOADED), 1L);
                                        }
                                    }).build());
                            stackSize *= 2;
                            buySlot++;
                        }
                    }

                    if(shopEntry.prices().sellPrice() != -1.0) {
                        int sellSlot = 28;
                        int stackSize = 1;
                        for(int j = 1; j <= 7; j++) {
                            int finalStackSize = stackSize;
                            ItemStack displayItem = new ItemStack(Material.RED_STAINED_GLASS_PANE);
                            displayItem.setAmount(finalStackSize);
                            double finalPrice = shopEntry.prices().sellPrice() * finalStackSize;
                            List<Component> sellLore = new ArrayList<>();
                            sellLore.add(MiniMessage.miniMessage().deserialize("<yellow>Sell Price:</yellow> <white>" + finalPrice + "</white>").decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE));

                            addButton(sellSlot, new InventoryButton.Builder()
                                    .setItemStack(displayItem)
                                    .setItemName(MiniMessage.miniMessage().deserialize("<red>Sell " + stackSize + "</red>").decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE))
                                    .setLore(sellLore)
                                    .setAction(event -> {
                                        FormattedLocale messages = localeManager.formattedLocale();
                                        ItemStack sellItem = new ItemStack(Material.valueOf(shopEntry.item().material()));
                                        sellItem.setAmount(finalStackSize);
                                        Player player = (Player) event.getWhoClicked();

                                        if(player.getInventory().containsAtLeast(sellItem, finalStackSize)) {
                                            player.getInventory().removeItem(sellItem);
                                            skyShop.getEconomy().depositPlayer(player, finalPrice);

                                            Component sellSuccess = MiniMessage.miniMessage().deserialize(messages.sellSuccess(),
                                                    Placeholder.parsed("amount", String.valueOf(finalStackSize)),
                                                    Placeholder.parsed("item", shopEntry.item().name()),
                                                    Placeholder.parsed("price", String.valueOf(finalPrice)),
                                                    Placeholder.parsed("bal", String.valueOf(skyShop.getEconomy().getBalance(player))));

                                            event.getWhoClicked().sendMessage(messages.prefix().append(sellSuccess));
                                        } else {
                                            event.getWhoClicked().sendMessage(messages.prefix().append(messages.notEnoughItems()));
                                            Bukkit.getScheduler().runTaskLater(skyShop, () -> event.getWhoClicked().closeInventory(InventoryCloseEvent.Reason.UNLOADED), 1L);
                                        }
                                    }).build());
                            stackSize *= 2;
                            sellSlot++;
                        }
                    }
                }

                if(transPageNum == 2) {
                    addButton(47, new InventoryButton.Builder()
                            .setItemStack(new ItemStack(Material.ARROW))
                            .setItemName(MiniMessage.miniMessage().deserialize("<yellow>Previous Page</yellow>").decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE))
                            .setLore(new ArrayList<>())
                            .setAction(event -> Bukkit.getScheduler().runTaskLater(skyShop, () -> {
                                event.getWhoClicked().closeInventory(InventoryCloseEvent.Reason.OPEN_NEW);
                                inventoryManager.openGUI(new TransactionGUI(skyShop, menuManager, inventoryManager, shopManager, localeManager, shopPageEntry, shopEntry, shopId, shopPageNum, 1), (Player) event.getWhoClicked());
                            }, 1L))
                            .build());

                    if(shopEntry.prices().buyPrice() != -1.0) {
                        int buySlot = 9;
                        for(int j = 1; j <= 9; j++) {
                            String itemName;
                            int finalI = j;
                            ItemStack buy = new ItemStack(Material.GREEN_STAINED_GLASS_PANE);
                            buy.setAmount(finalI);
                            int stackSize = 64 * finalI;
                            double finalPrice = stackSize * shopEntry.prices().buyPrice();

                            if(finalI == 1) {
                                itemName = "<green>Buy " + finalI + " Stack</green>";
                            } else {
                                itemName = "<green>Buy " + finalI + " Stacks</green>";
                            }

                            List<Component> buyLore = new ArrayList<>();
                            buyLore.add(MiniMessage.miniMessage().deserialize("<yellow>Buy Price:</yellow> <white>" + finalPrice + "</white>").decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE));

                            addButton(buySlot, new InventoryButton.Builder()
                                    .setItemStack(buy)
                                    .setItemName(MiniMessage.miniMessage().deserialize(itemName).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE))
                                    .setLore(buyLore)
                                    .setAction(event -> {
                                        FormattedLocale messages = localeManager.formattedLocale();
                                        ItemStack buyItem = new ItemStack(Material.valueOf(shopEntry.item().material()));
                                        buyItem.setAmount(stackSize);
                                        Player player = (Player) event.getWhoClicked();

                                        if(skyShop.getEconomy().getBalance(player) >= finalPrice) {
                                            if(player.getInventory().firstEmpty() != -1) {
                                                Component buySuccess;
                                                player.getInventory().addItem(buyItem);
                                                skyShop.getEconomy().withdrawPlayer(player, finalPrice);

                                                if(finalI == 1) {
                                                    buySuccess = MiniMessage.miniMessage().deserialize(messages.buySuccess(), Placeholder.parsed("amount", finalI + " stack"), Placeholder.parsed("item", shopEntry.item().name()), Placeholder.parsed("price", String.valueOf(shopEntry.prices().buyPrice())), Placeholder.parsed("bal", String.valueOf(skyShop.getEconomy().getBalance((OfflinePlayer) event.getWhoClicked()))));
                                                } else {
                                                    buySuccess = MiniMessage.miniMessage().deserialize(messages.buySuccess(), Placeholder.parsed("amount", finalI + " stacks"), Placeholder.parsed("item", shopEntry.item().name()), Placeholder.parsed("price", String.valueOf(shopEntry.prices().buyPrice())), Placeholder.parsed("bal", String.valueOf(skyShop.getEconomy().getBalance((OfflinePlayer) event.getWhoClicked()))));
                                                }

                                                event.getWhoClicked().sendMessage(messages.prefix().append(buySuccess));
                                            } else {
                                                Component buySuccess;
                                                player.getWorld().dropItem(player.getLocation(), buyItem);

                                                if(finalI == 1) {
                                                    buySuccess = MiniMessage.miniMessage().deserialize(messages.buySuccess(), Placeholder.parsed("amount", finalI + " stack"), Placeholder.parsed("item", shopEntry.item().name()), Placeholder.parsed("price", String.valueOf(shopEntry.prices().buyPrice())), Placeholder.parsed("bal", String.valueOf(skyShop.getEconomy().getBalance((OfflinePlayer) event.getWhoClicked()))));
                                                } else {
                                                    buySuccess = MiniMessage.miniMessage().deserialize(messages.buySuccess(), Placeholder.parsed("amount", finalI + " stacks"), Placeholder.parsed("item", shopEntry.item().name()), Placeholder.parsed("price", String.valueOf(shopEntry.prices().buyPrice())), Placeholder.parsed("bal", String.valueOf(skyShop.getEconomy().getBalance((OfflinePlayer) event.getWhoClicked()))));
                                                }

                                                event.getWhoClicked().sendMessage(messages.prefix().append(buySuccess));
                                            }
                                        } else {
                                            event.getWhoClicked().sendMessage(messages.prefix().append(messages.notEnoughItems()));
                                            Bukkit.getScheduler().runTaskLater(skyShop, () -> event.getWhoClicked().closeInventory(InventoryCloseEvent.Reason.UNLOADED), 1L);
                                        }
                                    }).build());
                            buySlot++;
                        }
                    }

                    if(shopEntry.prices().sellPrice() != -1.0) {
                        int sellSlot = 27;
                        for (int j = 1; j <= 9; j++) {
                            String itemName;
                            int finalI = j;
                            ItemStack sell = new ItemStack(Material.RED_STAINED_GLASS_PANE);
                            sell.setAmount(finalI);
                            int stackSize = 64 * finalI;
                            double finalPrice = stackSize * shopEntry.prices().sellPrice();
                            
                            if(finalI == 1) {
                                itemName = "<red>Sell " + finalI + " Stack</red>";
                            } else {
                                itemName = "<red>Sell " + finalI + " Stacks</red>";
                            }

                            List<Component> sellLore = new ArrayList<>();
                            sellLore.add(MiniMessage.miniMessage().deserialize("<yellow>Sell Price:</yellow> <white>" + finalPrice + "</white>").decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE));

                            addButton(sellSlot, new InventoryButton.Builder()
                                    .setItemStack(sell)
                                    .setItemName(MiniMessage.miniMessage().deserialize(itemName).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE))
                                    .setLore(sellLore)
                                    .setAction(event -> {
                                        FormattedLocale messages = localeManager.formattedLocale();
                                        ItemStack sellItem = new ItemStack(Material.valueOf(shopEntry.item().material()));
                                        sellItem.setAmount(stackSize);
                                        Player player = (Player) event.getWhoClicked();

                                        if(player.getInventory().containsAtLeast(sellItem, stackSize)) {
                                            Component sellSuccess;
                                            player.getInventory().removeItem(sellItem);
                                            skyShop.getEconomy().depositPlayer(player, finalPrice);

                                            if(finalI == 1) {
                                                sellSuccess = MiniMessage.miniMessage().deserialize(messages.sellSuccess(), Placeholder.parsed("amount", finalI + " stack"), Placeholder.parsed("item", shopEntry.item().name()), Placeholder.parsed("price", String.valueOf(shopEntry.prices().sellPrice())), Placeholder.parsed("bal", String.valueOf(skyShop.getEconomy().getBalance((OfflinePlayer) event.getWhoClicked()))));
                                            } else {
                                                sellSuccess = MiniMessage.miniMessage().deserialize(messages.sellSuccess(), Placeholder.parsed("amount", finalI + " stacks"), Placeholder.parsed("item", shopEntry.item().name()), Placeholder.parsed("price", String.valueOf(shopEntry.prices().sellPrice())), Placeholder.parsed("bal", String.valueOf(skyShop.getEconomy().getBalance((OfflinePlayer) event.getWhoClicked()))));
                                            }

                                            event.getWhoClicked().sendMessage(messages.prefix().append(sellSuccess));
                                        } else {
                                            event.getWhoClicked().sendMessage(messages.prefix().append(messages.notEnoughItems()));
                                            Bukkit.getScheduler().runTaskLater(skyShop, () -> event.getWhoClicked().closeInventory(InventoryCloseEvent.Reason.UNLOADED), 1L);
                                        }
                                    }).build());
                            sellSlot++;
                        }
                    }
                }
            }
            case COMMAND -> {
                displayLore = new ArrayList<>();
                if(shopEntry.item().lore() != null) {
                    for(String loreLine : shopEntry.item().lore()) {
                        displayLore.add(MiniMessage.miniMessage().deserialize(loreLine, new TagResolver[]{
                                Placeholder.parsed("buy_price", String.valueOf(shopEntry.prices().buyPrice())),
                                Placeholder.parsed("sell_price", String.valueOf(shopEntry.prices().sellPrice()))
                        }).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE));
                    }
                }
                
                addButton(22, new InventoryButton.Builder()
                        .setItemStack(new ItemStack(Material.valueOf(shopEntry.item().material())))
                        .setItemName(MiniMessage.miniMessage().deserialize(shopEntry.item().name()).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE))
                        .setLore(displayLore)
                        .setAction(event -> {})
                        .build());

                if(shopEntry.prices().buyPrice() != -1.0) {
                    List<Component> buyLore = new ArrayList<>();
                    buyLore.add(MiniMessage.miniMessage().deserialize("<yellow>Buy Price:</yellow> <white>" + shopEntry.prices().buyPrice() + "</white>").decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE));

                    addButton(13, new InventoryButton.Builder()
                            .setItemStack(new ItemStack(Material.GREEN_STAINED_GLASS_PANE))
                            .setItemName(MiniMessage.miniMessage().deserialize("<green> Buy 1</green>").decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE))
                            .setLore(buyLore)
                            .setAction(event -> {
                                FormattedLocale messages = localeManager.formattedLocale();
                                Player player = (Player) event.getWhoClicked();
                                
                                if(skyShop.getEconomy().getBalance(player) >= shopEntry.prices().buyPrice()) {
                                    for (String s : shopEntry.commands().buyCommands()) {
                                        skyShop.getServer().dispatchCommand(skyShop.getServer().getConsoleSender(), PlaceholderAPIUtil.parsePlaceholders(player, s));
                                    }
                                    skyShop.getEconomy().withdrawPlayer(player, shopEntry.prices().buyPrice());
                                    
                                    Component buySuccess = MiniMessage.miniMessage().deserialize(messages.buySuccess(), 
                                            Placeholder.parsed("amount", "1"), 
                                            Placeholder.parsed("item", shopEntry.item().name()), 
                                            Placeholder.parsed("price", String.valueOf(shopEntry.prices().buyPrice())), 
                                            Placeholder.parsed("bal", String.valueOf(skyShop.getEconomy().getBalance(player))));
                                    player.sendMessage(messages.prefix().append(buySuccess));
                                } else {
                                    event.getWhoClicked().sendMessage(messages.prefix().append(messages.insufficientFunds()));
                                    Bukkit.getScheduler().runTaskLater(skyShop, () -> event.getWhoClicked().closeInventory(InventoryCloseEvent.Reason.UNLOADED), 1L);
                                }
                            }).build());
                }

                if(shopEntry.prices().sellPrice() != -1.0) {
                    List<Component> sellLore = new ArrayList<>();
                    sellLore.add(MiniMessage.miniMessage().deserialize("<yellow>Sell Price:</yellow> <white>" + shopEntry.prices().buyPrice() + "</white>").decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE));

                    addButton(31, new InventoryButton.Builder()
                            .setItemStack(new ItemStack(Material.RED_STAINED_GLASS_PANE))
                            .setItemName(MiniMessage.miniMessage().deserialize("<red>Sell 1</red>").decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE))
                            .setLore(sellLore)
                            .setAction(event -> {
                                FormattedLocale messages = localeManager.formattedLocale();
                                skyShop.getEconomy().depositPlayer((OfflinePlayer) event.getWhoClicked(), shopEntry.prices().sellPrice());
                                for (String command : shopEntry.commands().sellCommands()) {
                                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), PlaceholderAPIUtil.parsePlaceholders((Player) event.getWhoClicked(), command));
                                }
                                
                                Component buySuccess = MiniMessage.miniMessage().deserialize(messages.sellSuccess(),
                                        Placeholder.parsed("amount", "1"),
                                        Placeholder.parsed("item", shopEntry.item().name()),
                                        Placeholder.parsed("price", String.valueOf(shopEntry.prices().sellPrice())),
                                        Placeholder.parsed("bal", String.valueOf(skyShop.getEconomy().getBalance((OfflinePlayer) event.getWhoClicked()))));
                                
                                event.getWhoClicked().sendMessage(messages.prefix().append(buySuccess));
                            }).build());
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
        if(!event.getReason().equals(InventoryCloseEvent.Reason.OPEN_NEW) && !event.getReason().equals(InventoryCloseEvent.Reason.UNLOADED))
            Bukkit.getScheduler().runTaskLater(skyShop, () -> inventoryManager.openGUI(new ShopGUI(skyShop, menuManager, shopManager, inventoryManager, localeManager, shopPageEntry, shopId, shopPageNum), (Player)event.getPlayer()), 1L);
    }
}
