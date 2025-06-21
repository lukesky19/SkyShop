/*
    SkyShop is a simple inventory based shop plugin with page support, sell commands, and error checking.
    Copyright (C) 2024 lukeskywlker19

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

import com.github.lukesky19.skylib.api.adventure.AdventureUtil;
import com.github.lukesky19.skylib.api.gui.AbstractGUIManager;
import com.github.lukesky19.skylib.api.gui.GUIButton;
import com.github.lukesky19.skylib.api.gui.GUIType;
import com.github.lukesky19.skylib.api.gui.abstracts.ChestGUI;
import com.github.lukesky19.skylib.api.itemstack.ItemStackBuilder;
import com.github.lukesky19.skylib.api.itemstack.ItemStackConfig;
import com.github.lukesky19.skylib.api.placeholderapi.PlaceholderAPIUtil;
import com.github.lukesky19.skylib.api.player.PlayerUtil;
import com.github.lukesky19.skyshop.SkyShop;
import com.github.lukesky19.skyshop.SkyShopAPI;
import com.github.lukesky19.skyshop.configuration.LocaleManager;
import com.github.lukesky19.skyshop.configuration.SellAllManager;
import com.github.lukesky19.skyshop.data.Locale;
import com.github.lukesky19.skyshop.data.gui.SellAllConfig;
import com.github.lukesky19.skyshop.data.gui.TransactionConfig;
import com.github.lukesky19.skyshop.event.CommandPurchasedEvent;
import com.github.lukesky19.skyshop.event.CommandSoldEvent;
import com.github.lukesky19.skyshop.event.ItemPurchasedEvent;
import com.github.lukesky19.skyshop.event.ItemSoldEvent;
import com.github.lukesky19.skyshop.manager.StatsManager;
import com.github.lukesky19.skyshop.util.ButtonType;
import com.github.lukesky19.skyshop.util.TransactionType;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.Server;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ItemType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/**
 * This class is called to create a transaction inventory for a player to buy and sell items.
 */
public class TransactionGUI extends ChestGUI {
    private final @NotNull SkyShop skyShop;
    private final @NotNull LocaleManager localeManager;
    private final @NotNull SellAllManager sellAllManager;
    private final @Nullable StatsManager statsManager;
    private final @NotNull SkyShopAPI skyShopAPI;
    private final @NotNull ShopGUI shopGUI;

    // Config related to the Transaction
    private final @NotNull TransactionType transactionType;
    private final @NotNull String transactionStyle;
    private final @NotNull TransactionConfig transactionConfig;
    private final @NotNull ItemStackConfig displayItemConfig;
    private final @NotNull ItemStackConfig transactionItemConfig;
    private final @Nullable Double buyPrice;
    private final @Nullable Double sellPrice;
    private final @NotNull String transactionName;
    private final @NotNull List<String> buyCommands;
    private final @NotNull List<String> sellCommands;

    private int pageNum = 0;
    private boolean isOpen = false;

    /**
     * Constructor
     * @param skyShop A {@link SkyShop} instance.
     * @param guiManager An {@link AbstractGUIManager} instance.
     * @param player The {@link Player} to create the GUI for.
     * @param localeManager A {@link SkyShop} instance.
     * @param sellAllManager A {@link LocaleManager} instance.
     * @param statsManager A {@link SellAllManager} instance.
     * @param skyShopAPI A {@link SkyShopAPI} instance.
     * @param shopGUI The {@link ShopGUI} the player came from.
     * @param transactionType The {@link TransactionType}.
     * @param transactionStyle The transaction style name. This is the {@link String} that was used to get the {@link TransactionConfig}.
     * @param transactionConfig The {@link TransactionConfig} to create the GUI with.
     * @param displayItemConfig The {@link ItemStackConfig} used to create the {@link ItemStack} that displays what is being purchased or sold.
     * @param transactionItemConfig The {@link ItemStackConfig} used to create the {@link ItemStack} that will be purchased or sold.
     * @param buyPrice The price take from the {@link Player} to buy the {@link ItemStack} or execute the buy commands.
     * @param sellPrice The price to give the {@link Player} to sell the {@link ItemStack} or execute the sell commands.
     * @param transactionName The name to use when displaying a successful transaction message.
     * @param buyCommands A {@link List} of {@link String} containing the commands to execute in console when a successful buy transaction is made.
     * @param sellCommands A {@link List} of {@link String} containing the commands to execute in console when a successful sell transaction is made.
     */
    public TransactionGUI(
            @NotNull SkyShop skyShop,
            @NotNull AbstractGUIManager guiManager,
            @NotNull Player player,
            @NotNull LocaleManager localeManager,
            @NotNull SellAllManager sellAllManager,
            @Nullable StatsManager statsManager,
            @NotNull SkyShopAPI skyShopAPI,
            @NotNull ShopGUI shopGUI,
            @NotNull TransactionType transactionType,
            @NotNull String transactionStyle,
            @NotNull TransactionConfig transactionConfig,
            @NotNull ItemStackConfig displayItemConfig,
            @NotNull ItemStackConfig transactionItemConfig,
            @Nullable Double buyPrice,
            @Nullable Double sellPrice,
            @Nullable String transactionName,
            @NotNull List<String> buyCommands,
            @NotNull List<String> sellCommands) {
        super(skyShop, guiManager, player);

        this.skyShop = skyShop;
        this.localeManager = localeManager;
        this.sellAllManager = sellAllManager;
        this.statsManager = statsManager;
        this.skyShopAPI = skyShopAPI;
        this.shopGUI = shopGUI;
        this.transactionType = transactionType;
        this.transactionStyle = transactionStyle;
        this.transactionConfig = transactionConfig;
        this.displayItemConfig = displayItemConfig;
        this.transactionItemConfig = transactionItemConfig;
        this.buyPrice = buyPrice;
        this.sellPrice = sellPrice;
        this.buyCommands = buyCommands;
        this.sellCommands = sellCommands;
        this.transactionName = Objects.requireNonNullElse(transactionName, "");
    }

    /**
     * Create the {@link InventoryView} for this GUI.
     * @return true if created successfully, otherwise false.
     */
    public boolean create() {
        GUIType guiType = transactionConfig.gui().guiType();
        if(guiType == null) {
            logger.warn(AdventureUtil.serialize("Unable to create the InventoryView for a ShopGUI due to an invalid GUIType"));
            return false;
        }

        String guiName = Objects.requireNonNullElse(transactionConfig.gui().name(), "");

        return create(guiType, guiName);
    }

    /**
     * Set the {@link #isOpen} boolean to true and run the super method.
     * @return true if opened successfully, otherwise false.
     */
    @Override
    public boolean open() {
        isOpen = true;

        return super.open();
    }

    /**
     * Close the current inventory/gui with an OPEN_NEW reason and open the {@link #shopGUI}.
     */
    @Override
    public void close() {
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            isOpen = false;

            player.closeInventory(InventoryCloseEvent.Reason.OPEN_NEW);

            guiManager.removeOpenGUI(player.getUniqueId());

            shopGUI.open();
        }, 1L);
    }

    /**
     * Close the current inventory/gui with an UNLOADED reason.
     * @param onDisable Is the plugin being disabled?
     */
    @Override
    public void unload(boolean onDisable) {
        if(!onDisable) {
            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                isOpen = false;

                player.closeInventory(InventoryCloseEvent.Reason.UNLOADED);

                guiManager.removeOpenGUI(player.getUniqueId());
            }, 1L);
        } else {
            isOpen = false;

            player.closeInventory(InventoryCloseEvent.Reason.UNLOADED);

            guiManager.removeOpenGUI(player.getUniqueId());
        }
    }

    /**
     * A method to create all the buttons in the inventory GUI.
     */
    @Override
    public @NotNull CompletableFuture<Boolean> update() {
        Locale locale = localeManager.getLocale();

        // If the InventoryView was not created, log a warning and return false.
        if (inventoryView == null) {
            logger.warn(AdventureUtil.serialize("Unable to add GUIButton ItemStacks to the InventoryView as it was not created."));
            if(isOpen) close();
            return CompletableFuture.completedFuture(false);
        }

        // Get the GUI size
        int guiSize = inventoryView.getTopInventory().getSize();

        // Clear the GUI of buttons
        clearButtons();

        // Check if at least 1 page is configured.
        List<TransactionConfig.PageConfig> pages = transactionConfig.gui().pages();
        if(pages.isEmpty()) {
            logger.error(AdventureUtil.serialize("Unable to decorate the transaction GUI due to no pages configured."));
            return CompletableFuture.completedFuture(false);
        }

        // Get the page config
        TransactionConfig.PageConfig page = pages.get(pageNum);

        // Check if at least 1 button is configured.
        List<TransactionConfig.Button> entries = page.buttons();
        if (entries.isEmpty()) {
            logger.error(AdventureUtil.serialize("Unable to decorate the transaction GUI for page " + pageNum + " due to no buttons configured."));
            if(isOpen) close();
            return CompletableFuture.completedFuture(false);
        }

        for(int buttonNum = 0; buttonNum < page.buttons().size(); buttonNum++) {
            TransactionConfig.Button buttonConfig = page.buttons().get(buttonNum);
            ButtonType buttonType = buttonConfig.buttonType();

            // Check if the button type is null and send a warning if so, then skipping to the next button.
            if(buttonType == null) {
                logger.warn(AdventureUtil.serialize("Unable to add a button due to an invalid button type. Button Num: " + buttonNum));
                continue;
            }

            // Handle the creation of buttons by button type.
            switch (buttonType) {
                case FILLER -> {
                    // Get the ItemStackConfig
                    ItemStackConfig itemConfig = buttonConfig.displayItem();

                    // Create the ItemStackBuilder and pass the ItemStackConfig.
                    ItemStackBuilder itemStackBuilder = new ItemStackBuilder(logger);
                    itemStackBuilder.fromItemStackConfig(itemConfig, player, null, List.of());

                    // If an ItemStack was created, create the GUIButton and add it to the GUI.
                    Optional<ItemStack> optionalItemStack = itemStackBuilder.buildItemStack();
                    optionalItemStack.ifPresent(itemStack -> {
                        GUIButton.Builder guiButtonBuilder = new GUIButton.Builder();
                        guiButtonBuilder.setItemStack(itemStack);

                        GUIButton fillerButton = guiButtonBuilder.build();

                        for (int i = 0; i <= (guiSize - 1); i++) {
                            setButton(i, fillerButton);
                        }
                    });
                }

                case PREVIOUS_PAGE -> {
                    // Only display the previous page button if the page number is greater than or equal to 1
                    if (pageNum >= 1) {
                        // Check if the slot is not configured and send a warning.
                        if(buttonConfig.slot() == null) {
                            logger.warn(AdventureUtil.serialize("Unable to add a button due to a null slot. Button Num: " + buttonNum + " and type: " + buttonType));
                            continue;
                        }

                        // Get the ItemStackConfig
                        ItemStackConfig itemConfig = buttonConfig.displayItem();

                        // Create the ItemStackBuilder and pass the ItemStackConfig.
                        ItemStackBuilder itemStackBuilder = new ItemStackBuilder(logger);
                        itemStackBuilder.fromItemStackConfig(itemConfig, player, null, List.of());

                        // If an ItemStack was created, create the GUIButton and add it to the GUI.
                        Optional<ItemStack> optionalItemStack = itemStackBuilder.buildItemStack();
                        optionalItemStack.ifPresent(itemStack -> {
                            GUIButton.Builder guiButtonBuilder = new GUIButton.Builder();
                            guiButtonBuilder.setItemStack(itemStack);
                            guiButtonBuilder.setAction(event -> {
                                pageNum = pageNum - 1;
                                update();
                            });

                            setButton(buttonConfig.slot(), guiButtonBuilder.build());
                        });
                    }
                }

                case NEXT_PAGE -> {
                    // Only display the next page button if another page is configured after the current
                    if(pageNum < (pages.size() - 1)) {
                        // Check if the slot is not configured and send a warning.
                        if(buttonConfig.slot() == null) {
                            logger.warn(AdventureUtil.serialize("Unable to add a button due to a null slot. Button Num: " + buttonNum + " and type: " + buttonType));
                            continue;
                        }

                        // Get the ItemStackConfig
                        ItemStackConfig itemConfig = buttonConfig.displayItem();

                        // Create the ItemStackBuilder and pass the ItemStackConfig.
                        ItemStackBuilder itemStackBuilder = new ItemStackBuilder(logger);
                        itemStackBuilder.fromItemStackConfig(itemConfig, player, null, List.of());

                        // If an ItemStack was created, create the GUIButton and add it to the GUI.
                        Optional<ItemStack> optionalItemStack = itemStackBuilder.buildItemStack();
                        optionalItemStack.ifPresent(itemStack -> {
                            GUIButton.Builder guiButtonBuilder = new GUIButton.Builder();
                            guiButtonBuilder.setItemStack(itemStack);
                            guiButtonBuilder.setAction(event -> {
                                pageNum = pageNum + 1;
                                update();
                            });

                            setButton(buttonConfig.slot(), guiButtonBuilder.build());
                        });
                    }
                }

                case RETURN -> {
                    // Check if the slot is not configured and send a warning.
                    if(buttonConfig.slot() == null) {
                        logger.warn(AdventureUtil.serialize("Unable to add a button due to a null slot. Button Num: " + buttonNum + " and type: " + buttonType));
                        continue;
                    }

                    // Get the ItemStackConfig
                    ItemStackConfig itemConfig = buttonConfig.displayItem();

                    // Create the ItemStackBuilder and pass the ItemStackConfig.
                    ItemStackBuilder itemStackBuilder = new ItemStackBuilder(logger);
                    itemStackBuilder.fromItemStackConfig(itemConfig, player, null, List.of());

                    // If an ItemStack was created, create the GUIButton and add it to the GUI.
                    Optional<ItemStack> optionalItemStack = itemStackBuilder.buildItemStack();
                    optionalItemStack.ifPresent(itemStack -> {
                        GUIButton.Builder guiButtonBuilder = new GUIButton.Builder();
                        guiButtonBuilder.setItemStack(itemStack);
                        guiButtonBuilder.setAction(event -> close());

                        setButton(buttonConfig.slot(), guiButtonBuilder.build());
                    });
                }

                case DISPLAY -> {
                    // Check if the slot is not configured and send a warning.
                    if(buttonConfig.slot() == null) {
                        logger.warn(AdventureUtil.serialize("Unable to add a button due to a null slot. Button Num: " + buttonNum + " and type: " + buttonType));
                        continue;
                    }

                    // Create the ItemStackBuilder and pass the ItemStackConfig.
                    ItemStackBuilder itemStackBuilder = new ItemStackBuilder(logger);
                    itemStackBuilder.fromItemStackConfig(displayItemConfig, player, null, List.of());

                    // If an ItemStack was created, create the GUIButton and add it to the GUI.
                    Optional<ItemStack> optionalItemStack = itemStackBuilder.buildItemStack();
                    optionalItemStack.ifPresent(itemStack -> {
                        GUIButton.Builder guiButtonBuilder = new GUIButton.Builder();
                        guiButtonBuilder.setItemStack(itemStack);
                        guiButtonBuilder.setAction(event -> close());

                        setButton(buttonConfig.slot(), guiButtonBuilder.build());
                    });
                }

                case SELL_ALL -> {
                    // Check if the slot is not configured and send a warning.
                    if(buttonConfig.slot() == null) {
                        logger.warn(AdventureUtil.serialize("Unable to add a button due to a null slot. Button Num: " + buttonNum + " and type: " + buttonType));
                        continue;
                    }

                    // Get the ItemStackConfig
                    ItemStackConfig itemConfig = buttonConfig.displayItem();

                    // Create the ItemStackBuilder and pass the ItemStackConfig.
                    ItemStackBuilder itemStackBuilder = new ItemStackBuilder(logger);
                    itemStackBuilder.fromItemStackConfig(itemConfig, player, null, List.of());

                    // If an ItemStack was created, create the GUIButton and add it to the GUI.
                    Optional<ItemStack> optionalItemStack = itemStackBuilder.buildItemStack();
                    optionalItemStack.ifPresent(itemStack -> {
                        GUIButton.Builder guiButtonBuilder = new GUIButton.Builder();
                        guiButtonBuilder.setItemStack(itemStack);
                        guiButtonBuilder.setAction(event -> {
                            ItemStackBuilder transactionItemBuilder = new ItemStackBuilder(logger);
                            transactionItemBuilder.fromItemStackConfig(transactionItemConfig, player, null, List.of());
                            Optional<ItemStack> optionalTransactionItemStack = transactionItemBuilder.buildItemStack();
                            if(optionalTransactionItemStack.isEmpty()) return;

                            ItemStack transactionItemStack = optionalTransactionItemStack.get();

                            skyShopAPI.sellAllMatchingItemStack(player, transactionItemStack, true);
                        });

                        setButton(buttonConfig.slot(), guiButtonBuilder.build());
                    });
                }

                case SELL_GUI -> {
                    // Check if the slot is not configured and send a warning.
                    if(buttonConfig.slot() == null) {
                        logger.warn(AdventureUtil.serialize("Unable to add a button due to a null slot. Button Num: " + buttonNum + " and type: " + buttonType));
                        continue;
                    }

                    // Get the ItemStackConfig
                    ItemStackConfig itemConfig = buttonConfig.displayItem();

                    // Create the ItemStackBuilder and pass the ItemStackConfig.
                    ItemStackBuilder itemStackBuilder = new ItemStackBuilder(logger);
                    itemStackBuilder.fromItemStackConfig(itemConfig, player, null, List.of());

                    // If an ItemStack was created, create the GUIButton and add it to the GUI.
                    Optional<ItemStack> optionalItemStack = itemStackBuilder.buildItemStack();
                    optionalItemStack.ifPresent(itemStack -> {
                        GUIButton.Builder guiButtonBuilder = new GUIButton.Builder();
                        guiButtonBuilder.setItemStack(itemStack);
                        guiButtonBuilder.setAction(event -> {
                            @NotNull Optional<@NotNull SellAllConfig> optionalGUIConfig = sellAllManager.getSellAllGuiConfig();
                            if(optionalGUIConfig.isEmpty()) {
                                logger.error(AdventureUtil.serialize("Unable to open sellall GUI for player " + player.getName() + " due to invalid sellall config."));
                                player.sendMessage(AdventureUtil.serialize(locale.prefix() + locale.guiOpenError()));
                                if(isOpen) close();
                                return;
                            }

                            SellAllConfig sellAllGuiConfig = optionalGUIConfig.get();
                            SellAllGUI sellAllGUI = new SellAllGUI(skyShop, guiManager, sellAllGuiConfig, skyShopAPI, player);

                            boolean creationResult = sellAllGUI.create();
                            if(!creationResult) {
                                logger.error(AdventureUtil.serialize("Unable to create the InventoryView for the sellall GUI for player " + player.getName() + " due to a configuration error."));
                                player.sendMessage(AdventureUtil.serialize(locale.prefix() + locale.guiOpenError()));
                                if(isOpen) close();
                                return;
                            }

                            // This method is completed sync, the api returns a CompletableFuture for supporting plugins with async requirements.
                            @NotNull CompletableFuture<Boolean> updateFuture = sellAllGUI.update();
                            try {
                                if(!updateFuture.get()) {
                                    logger.error(AdventureUtil.serialize("Unable to decorate the sellall GUI for player " + player.getName() + " due to a configuration error."));
                                    player.sendMessage(AdventureUtil.serialize(locale.prefix() + locale.guiOpenError()));
                                    if(isOpen) close();
                                    return;
                                }
                            } catch (InterruptedException | ExecutionException e) {
                                logger.error(AdventureUtil.serialize("Unable to decorate the sellall GUI for player " + player.getName() + " due to a configuration error. " + e.getMessage()));
                                player.sendMessage(AdventureUtil.serialize(locale.prefix() + locale.guiOpenError()));
                                if(isOpen) close();
                                return;
                            }

                            boolean openResult = sellAllGUI.open();
                            if(!openResult) {
                                logger.error(AdventureUtil.serialize("Unable to open the sellall GUI for player " + player.getName() + " due to a configuration error."));
                                player.sendMessage(AdventureUtil.serialize(locale.prefix() + locale.guiOpenError()));
                                if(isOpen) close();
                            }
                        });

                        setButton(buttonConfig.slot(), guiButtonBuilder.build());
                    });
                }

                case BUY -> {
                    // Check if the slot is not configured and send a warning.
                    if(buttonConfig.slot() == null) {
                        logger.warn(AdventureUtil.serialize("Unable to add a button due to a null slot. Button Num: " + buttonNum + " and type: " + buttonType));
                        continue;
                    }

                    if(buyPrice != null && buyPrice > 0.0) {
                        // Get the ItemStackConfig
                        ItemStackConfig itemConfig = buttonConfig.displayItem();
                        if(buttonConfig.transactionAmount() == null || buttonConfig.transactionAmount() <= 0) {
                            logger.warn(AdventureUtil.serialize("Unable to add a buy button due to an invalid transaction amount."));
                            continue;
                        }

                        // Get the amount to purchase
                        int purchaseAmount = buttonConfig.transactionAmount();
                        // Calculate the buy price
                        double price = buyPrice * purchaseAmount;

                        // Create the ItemStack placeholders
                        List<TagResolver.Single> itemStackPlaceholders = new ArrayList<>();
                        itemStackPlaceholders.add(Placeholder.parsed("buy_price", String.valueOf(price)));
                        itemStackPlaceholders.add(Placeholder.parsed("amount", String.valueOf(purchaseAmount)));

                        // Create the ItemStackBuilder and pass the ItemStackConfig.
                        ItemStackBuilder itemStackBuilder = new ItemStackBuilder(logger);
                        itemStackBuilder.fromItemStackConfig(itemConfig, player, null, itemStackPlaceholders);

                        // If an ItemStack was created, create the GUIButton and add it to the GUI.
                        Optional<ItemStack> optionalItemStack = itemStackBuilder.buildItemStack();
                        optionalItemStack.ifPresent(itemStack -> {
                            GUIButton.Builder guiButtonBuilder = new GUIButton.Builder();
                            guiButtonBuilder.setItemStack(itemStack);

                            if(transactionType.equals(TransactionType.ITEM)) {
                                guiButtonBuilder.setAction(inventoryClickEvent -> buyItem(purchaseAmount, price));
                            } else if(transactionType.equals(TransactionType.COMMAND)) {
                                guiButtonBuilder.setAction(inventoryClickEvent -> buyCommand(purchaseAmount, price));
                            }

                            setButton(buttonConfig.slot(), guiButtonBuilder.build());
                        });
                    }
                }

                case SELL -> {
                    // Check if the slot is not configured and send a warning.
                    if(buttonConfig.slot() == null) {
                        logger.warn(AdventureUtil.serialize("Unable to add a button due to a null slot. Button Num: " + buttonNum + " and type: " + buttonType));
                        continue;
                    }

                    if(sellPrice != null && sellPrice > 0.0) {
                        // Get the ItemStackConfig
                        ItemStackConfig itemConfig = buttonConfig.displayItem();
                        if(buttonConfig.transactionAmount() == null || buttonConfig.transactionAmount() <= 0) {
                            logger.warn(AdventureUtil.serialize("Unable to add a sell button due to an invalid transaction amount."));
                            continue;
                        }
                        // Get the amount to sell
                        int sellAmount = buttonConfig.transactionAmount();
                        // Calculate the sell price
                        double price = sellPrice * sellAmount;

                        // Create the ItemStack placeholders
                        List<TagResolver.Single> itemStackPlaceholders = new ArrayList<>();
                        itemStackPlaceholders.add(Placeholder.parsed("sell_price", String.valueOf(price)));
                        itemStackPlaceholders.add(Placeholder.parsed("amount", String.valueOf(sellAmount)));

                        // Create the ItemStackBuilder and pass the ItemStackConfig.
                        ItemStackBuilder itemStackBuilder = new ItemStackBuilder(logger);
                        itemStackBuilder.fromItemStackConfig(itemConfig, player, null, itemStackPlaceholders);

                        // If an ItemStack was created, create the GUIButton and add it to the GUI.
                        Optional<ItemStack> optionalItemStack = itemStackBuilder.buildItemStack();
                        optionalItemStack.ifPresent(itemStack -> {
                            GUIButton.Builder guiButtonBuilder = new GUIButton.Builder();
                            guiButtonBuilder.setItemStack(itemStack);

                            if(transactionType.equals(TransactionType.ITEM)) {
                                guiButtonBuilder.setAction(inventoryClickEvent -> sellItem(sellAmount, price));
                            } else if(transactionType.equals(TransactionType.COMMAND)) {
                                guiButtonBuilder.setAction(inventoryClickEvent -> sellCommand(sellAmount, price));
                            }

                            setButton(buttonConfig.slot(), guiButtonBuilder.build());
                        });
                    }
                }

                default -> logger.warn(AdventureUtil.serialize("Unsupported ButtonType in the transaction GUI for " + buttonNum + " on page " + pageNum + " and style " + transactionStyle + "."));
            }
        }

        return super.update();
    }

    /**
     * Re-open the ShopGUI if the reason the inventory closed was not OPEN_NEW or UNLOADED.
     * @param inventoryCloseEvent InventoryCloseEvent
     */
    @Override
    public void handleClose(@NotNull InventoryCloseEvent inventoryCloseEvent) {
        if(inventoryCloseEvent.getReason().equals(InventoryCloseEvent.Reason.UNLOADED) || inventoryCloseEvent.getReason().equals(InventoryCloseEvent.Reason.OPEN_NEW)) return;

        guiManager.removeOpenGUI(uuid);

        shopGUI.open();
    }

    @Override
    public void handleBottomDrag(@NotNull InventoryDragEvent inventoryDragEvent) {}

    @Override
    public void handleGlobalDrag(@NotNull InventoryDragEvent inventoryDragEvent) {}

    @Override
    public void handleBottomClick(@NotNull InventoryClickEvent inventoryClickEvent) {}

    @Override
    public void handleGlobalClick(@NotNull InventoryClickEvent inventoryClickEvent) {}

    /**
     * This method contains the logic to purchase an item.
     * @param amount The amount being purchased.
     * @param price The price of the item being purchased.
     */
    private void buyItem(int amount, double price) {
        Locale locale = localeManager.getLocale();

        // If the player doesn't have enough money, cancel the purchase.
        if(skyShop.getEconomy().getBalance(player) < price) {
            player.sendMessage(AdventureUtil.serialize(player, locale.prefix() + locale.insufficientFunds()));
            close();
            return;
        }

        // Create the ItemStack that will be given to the player on successful purchase.
        ItemStackBuilder itemStackBuilder = new ItemStackBuilder(logger);
        itemStackBuilder.fromItemStackConfig(transactionItemConfig, player, null, List.of());
        Optional<ItemStack> optionalItemStack = itemStackBuilder.buildItemStack();
        if(optionalItemStack.isPresent()) {
            ItemStack buyItem = optionalItemStack.get();
            buyItem.setAmount(amount);
            ItemType itemType = buyItem.getType().asItemType();
            if(itemType == null) return; // This should never be null, but just in-case return if such a case occurs.

            // Create and call the ItemPurchasedEvent
            ItemPurchasedEvent itemPurchasedEvent = new ItemPurchasedEvent(buyItem);
            skyShop.getServer().getPluginManager().callEvent(itemPurchasedEvent);

            // If the event was cancelled, cancel the purchase.
            if(itemPurchasedEvent.isCancelled()) return;

            // Remove the price from the player's balance.
            skyShop.getEconomy().withdrawPlayer(player, price);

            // Give the player the ItemStack they purchased.
            PlayerUtil.giveItem(player.getInventory(), buyItem, amount, player.getLocation());

            // Create the DecimalFormat that will be used to format the price and player's balance
            DecimalFormat df = new DecimalFormat("#.##");
            df.setRoundingMode(RoundingMode.CEILING);

            // Format the price
            BigDecimal bigPrice = BigDecimal.valueOf(price);
            String formattedPrice = df.format(bigPrice);

            // Format the player's balance
            BigDecimal bigBalance = BigDecimal.valueOf(skyShop.getEconomy().getBalance(player));
            String bal = df.format(bigBalance);

            // Create the necessary placeholders
            List<TagResolver.Single> successPlaceholders = new ArrayList<>();
            successPlaceholders.add(Placeholder.parsed("amount", String.valueOf(amount)));
            successPlaceholders.add(Placeholder.parsed("transaction_name", transactionName));
            successPlaceholders.add(Placeholder.parsed("price", formattedPrice));
            successPlaceholders.add(Placeholder.parsed("bal", bal));

            // Send the message that the transaction was a success
            player.sendMessage(AdventureUtil.serialize(player, locale.prefix() + locale.buyItemSuccess(), successPlaceholders));

            // Increment stats if statsManager is not null
            if(statsManager != null) statsManager.incrementAmountPurchased(itemType, amount);
        } else {
            logger.warn(AdventureUtil.serialize("An item failed to be purchased as the ItemStack failed to be created."));
        }
    }

    /**
     * This method contains the logic to sell an item.
     * @param amount The amount being sold.
     * @param price The price of the item being sold.
     */
    private void sellItem(int amount, double price) {
        Locale locale = localeManager.getLocale();

        // Create the ItemStack that will be taken from the player if they have enough of said ItemStack.
        ItemStackBuilder itemStackBuilder = new ItemStackBuilder(logger);
        itemStackBuilder.fromItemStackConfig(transactionItemConfig, player, null, List.of());
        Optional<ItemStack> optionalItemStack = itemStackBuilder.buildItemStack();
        if(optionalItemStack.isPresent()) {
            ItemStack sellItem = optionalItemStack.get();
            sellItem.setAmount(amount);
            ItemType itemType = sellItem.getType().asItemType();
            if(itemType == null) return; // This should never be null, but just in-case return if such a case occurs.

            if(!player.getInventory().containsAtLeast(sellItem, amount)) {
                player.sendMessage(AdventureUtil.serialize(player, locale.prefix() + locale.notEnoughItems()));
                close();
                return;
            }

            // Create and call the ItemSoldEvent
            ItemSoldEvent itemSoldEvent = new ItemSoldEvent(sellItem);
            skyShop.getServer().getPluginManager().callEvent(itemSoldEvent);

            // If the event was cancelled, cancel the purchase.
            if(itemSoldEvent.isCancelled()) return;

            // Remove the sold item from the player's inventory.
            player.getInventory().removeItem(sellItem);
            // Deposit the value of the item to the player's balance.
            skyShop.getEconomy().depositPlayer(player, price);

            // Create the DecimalFormat that will be used to format the price and player's balance
            DecimalFormat df = new DecimalFormat("#.##");
            df.setRoundingMode(RoundingMode.CEILING);

            // Format the price
            BigDecimal bigPrice = BigDecimal.valueOf(price);
            String formattedPrice = df.format(bigPrice);

            // Format the player's balance
            BigDecimal bigBalance = BigDecimal.valueOf(skyShop.getEconomy().getBalance(player));
            String bal = df.format(bigBalance);

            // Create the necessary placeholders
            List<TagResolver.Single> successPlaceholders = new ArrayList<>();
            successPlaceholders.add(Placeholder.parsed("amount", String.valueOf(amount)));
            successPlaceholders.add(Placeholder.parsed("transaction_name", transactionName));
            successPlaceholders.add(Placeholder.parsed("price", formattedPrice));
            successPlaceholders.add(Placeholder.parsed("bal", bal));

            player.sendMessage(AdventureUtil.serialize(player, locale.prefix() + locale.sellItemSuccess(), successPlaceholders));

            // Increment stats if statsManager is not null
            if(statsManager != null) statsManager.incrementAmountSold(itemType, amount);
        } else {
            logger.warn(AdventureUtil.serialize("An item failed to be sold as the ItemStack failed to be created."));
        }
    }

    /**
     * This method contains the logic to purchase a command.
     * @param amount The amount being purchased.
     * @param price The price of the item being purchased.
     */
    private void buyCommand(int amount, double price) {
        Locale locale = localeManager.getLocale();

        // If the player doesn't have enough money, cancel the purchase.
        if(skyShop.getEconomy().getBalance(player) < price) {
            player.sendMessage(AdventureUtil.serialize(player, locale.prefix() + locale.insufficientFunds()));
            close();
            return;
        }

        // Create and call the CommandPurchasedEvent
        CommandPurchasedEvent commandPurchasedEvent = new CommandPurchasedEvent(buyCommands);
        skyShop.getServer().getPluginManager().callEvent(commandPurchasedEvent);

        // If the event was cancelled, cancel the purchase.
        if(commandPurchasedEvent.isCancelled()) return;

        // Remove the price from the player's balance.
        skyShop.getEconomy().withdrawPlayer(player, price);

        // Execute the commands for this transaction
        Server server = skyShop.getServer();
        ConsoleCommandSender commandSender = server.getConsoleSender();
        for(String command : buyCommands) {
            for(int i = 1; i <= amount; i++) {
                server.dispatchCommand(commandSender, PlaceholderAPIUtil.parsePlaceholders(player, command));
            }
        }

        // Create the DecimalFormat that will be used to format the price and player's balance
        DecimalFormat df = new DecimalFormat("#.##");
        df.setRoundingMode(RoundingMode.CEILING);

        // Format the price
        BigDecimal bigPrice = BigDecimal.valueOf(price);
        String formattedPrice = df.format(bigPrice);

        // Format the player's balance
        BigDecimal bigBalance = BigDecimal.valueOf(skyShop.getEconomy().getBalance(player));
        String bal = df.format(bigBalance);

        // Create the necessary placeholders
        List<TagResolver.Single> successPlaceholders = new ArrayList<>();
        successPlaceholders.add(Placeholder.parsed("amount", String.valueOf(amount)));
        successPlaceholders.add(Placeholder.parsed("transaction_name", transactionName));
        successPlaceholders.add(Placeholder.parsed("price", formattedPrice));
        successPlaceholders.add(Placeholder.parsed("bal", bal));

        // Send the message that the transaction was a success
        player.sendMessage(AdventureUtil.serialize(player, locale.prefix() + locale.buyCommandSuccess(), successPlaceholders));
    }

    /**
     * This method contains the logic to purchase a command.
     * @param amount The amount being purchased.
     * @param price The price of the item being purchased.
     */
    private void sellCommand(int amount, double price) {
        Locale locale = localeManager.getLocale();

        // Create and call the CommandSoldEvent
        CommandSoldEvent commandSoldEvent = new CommandSoldEvent(sellCommands);
        skyShop.getServer().getPluginManager().callEvent(commandSoldEvent);

        // If the event was cancelled, cancel the transaction.
        if(commandSoldEvent.isCancelled()) return;

        // Deposit the price into the player's balance.
        skyShop.getEconomy().depositPlayer(player, price);

        // Execute the commands for this transaction
        Server server = skyShop.getServer();
        ConsoleCommandSender commandSender = skyShop.getServer().getConsoleSender();
        for(String command : sellCommands) {
            for(int i = 1; i <= amount; i++) {
                server.dispatchCommand(commandSender, PlaceholderAPIUtil.parsePlaceholders(player, command));
            }
        }

        // Create the DecimalFormat that will be used to format the price and player's balance
        DecimalFormat df = new DecimalFormat("#.##");
        df.setRoundingMode(RoundingMode.CEILING);

        // Format the price
        BigDecimal bigPrice = BigDecimal.valueOf(price);
        String formattedPrice = df.format(bigPrice);

        // Format the player's balance
        BigDecimal bigBalance = BigDecimal.valueOf(skyShop.getEconomy().getBalance(player));
        String bal = df.format(bigBalance);

        // Create the necessary placeholders
        List<TagResolver.Single> successPlaceholders = new ArrayList<>();
        successPlaceholders.add(Placeholder.parsed("amount", String.valueOf(amount)));
        successPlaceholders.add(Placeholder.parsed("transaction_name", transactionName));
        successPlaceholders.add(Placeholder.parsed("price", formattedPrice));
        successPlaceholders.add(Placeholder.parsed("bal", bal));

        // Send the message that the transaction was a success
        player.sendMessage(AdventureUtil.serialize(player, locale.prefix() + locale.sellCommandSuccess(), successPlaceholders));
    }
}
