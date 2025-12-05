/*
    SkyShop is a GUI shop plugin with sell commands, a sell GUI, nested categories, page support, and error checking.
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
import com.github.lukesky19.skylib.api.gui.GUIButton;
import com.github.lukesky19.skylib.api.gui.GUIType;
import com.github.lukesky19.skylib.api.gui.interfaces.IGUIManager;
import com.github.lukesky19.skylib.api.gui.templates.ChestGUI;
import com.github.lukesky19.skylib.api.itemstack.ItemStackBuilder;
import com.github.lukesky19.skylib.api.itemstack.ItemStackConfig;
import com.github.lukesky19.skylib.api.placeholderapi.PlaceholderAPIUtil;
import com.github.lukesky19.skylib.api.player.PlayerUtil;
import com.github.lukesky19.skyshop.SkyShop;
import com.github.lukesky19.skyshop.SkyShopAPI;
import com.github.lukesky19.skyshop.config.gui.CategoryConfig;
import com.github.lukesky19.skyshop.config.gui.SellAllConfig;
import com.github.lukesky19.skyshop.config.gui.TransactionConfig;
import com.github.lukesky19.skyshop.config.locale.Locale;
import com.github.lukesky19.skyshop.event.CommandPurchasedEvent;
import com.github.lukesky19.skyshop.event.CommandSoldEvent;
import com.github.lukesky19.skyshop.event.ItemPurchasedEvent;
import com.github.lukesky19.skyshop.event.ItemSoldEvent;
import com.github.lukesky19.skyshop.hook.impl.EconomyHook;
import com.github.lukesky19.skyshop.hook.impl.PlayerPointsHook;
import com.github.lukesky19.skyshop.manager.HookManager;
import com.github.lukesky19.skyshop.manager.StatsManager;
import com.github.lukesky19.skyshop.manager.config.LocaleManager;
import com.github.lukesky19.skyshop.manager.config.SellAllManager;
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
import java.util.*;

/**
 * This class is called to create a transaction inventory for a player to buy and sell items.
 */
public class TransactionGUI extends ChestGUI<UUID> {
    private final @NotNull SkyShop skyShop;
    private final @NotNull LocaleManager localeManager;
    private final @NotNull SellAllManager sellAllManager;
    private final @Nullable StatsManager statsManager;
    private final @NotNull HookManager hookManager;
    private final @NotNull SkyShopAPI skyShopAPI;
    private final @NotNull CategoryGUI categoryGUI;

    // Config related to the Transaction
    private final @NotNull TransactionConfig transactionConfig;
    private final @NotNull TransactionType transactionType;
    private final @NotNull String transactionStyle;
    private final @NotNull String transactionName;
    // ItemStack data
    private final @NotNull ItemStackConfig displayItemConfig;
    private final @NotNull ItemStackConfig transactionItemConfig;
    // Command Data
    private final @NotNull List<String> buyCommands;
    private final @NotNull List<String> sellCommands;
    // Price Data
    private final double buyPrice;
    private final double sellPrice;
    private final int buyPoints;
    private final int sellPoints;

    // Page Data
    private int pageNum = 0;
    private boolean isOpen = false;

    /**
     * Constructor
     * @param skyShop A {@link SkyShop} instance.
     * @param guiManager An {@link IGUIManager} instance.
     * @param player The {@link Player} to create the GUI for.
     * @param localeManager A {@link SkyShop} instance.
     * @param sellAllManager A {@link LocaleManager} instance.
     * @param statsManager A {@link SellAllManager} instance.
     * @param hookManager A {@link HookManager} instance.
     * @param skyShopAPI A {@link SkyShopAPI} instance.
     * @param categoryGUI The {@link CategoryGUI} the player came from.
     * @param transactionType The {@link TransactionType}.
     * @param transactionStyle The transaction style name. This is the {@link String} that was used to get the {@link TransactionConfig}.
     * @param transactionConfig The {@link TransactionConfig} to create the GUI with.
     * @param displayItemConfig The {@link ItemStackConfig} used to create the {@link ItemStack} that displays what is being purchased or sold.
     * @param transactionItemConfig The {@link ItemStackConfig} used to create the {@link ItemStack} that will be purchased or sold.
     * @param priceConfig The {@link CategoryConfig.PriceConfig} for this transaction.
     * @param transactionName The name to use when displaying a successful transaction message.
     * @param buyCommands A {@link List} of {@link String} containing the commands to execute in console when a successful buy transaction is made.
     * @param sellCommands A {@link List} of {@link String} containing the commands to execute in console when a successful sell transaction is made.
     */
    public TransactionGUI(
            @NotNull SkyShop skyShop,
            @NotNull IGUIManager<UUID> guiManager,
            @NotNull Player player,
            @NotNull LocaleManager localeManager,
            @NotNull SellAllManager sellAllManager,
            @Nullable StatsManager statsManager,
            @NotNull HookManager hookManager,
            @NotNull SkyShopAPI skyShopAPI,
            @NotNull CategoryGUI categoryGUI,
            @NotNull TransactionType transactionType,
            @NotNull String transactionStyle,
            @NotNull TransactionConfig transactionConfig,
            @NotNull ItemStackConfig displayItemConfig,
            @NotNull ItemStackConfig transactionItemConfig,
            @NotNull CategoryConfig.PriceConfig priceConfig,
            @Nullable String transactionName,
            @NotNull List<String> buyCommands,
            @NotNull List<String> sellCommands) {
        super(skyShop, guiManager, player.getUniqueId(), player);

        this.skyShop = skyShop;
        this.localeManager = localeManager;
        this.sellAllManager = sellAllManager;
        this.statsManager = statsManager;
        this.hookManager = hookManager;
        this.skyShopAPI = skyShopAPI;
        this.categoryGUI = categoryGUI;
        this.transactionType = transactionType;
        this.transactionStyle = transactionStyle;
        this.transactionConfig = transactionConfig;
        this.displayItemConfig = displayItemConfig;
        this.transactionItemConfig = transactionItemConfig;
        this.buyPrice = priceConfig.buyPrice() > 0.0 ? priceConfig.buyPrice() : -1.0;
        this.sellPrice = priceConfig.sellPrice() > 0.0 ? priceConfig.sellPrice() : -1.0;
        this.buyPoints = priceConfig.buyPoints() > 0 ? priceConfig.buyPoints() : -1;
        this.sellPoints = priceConfig.sellPoints() > 0 ? priceConfig.sellPoints() : -1;
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
            logger.warn(AdventureUtil.deserialize("Unable to create the InventoryView for a ShopGUI due to an invalid GUIType"));
            return false;
        }

        String guiName = Objects.requireNonNullElse(transactionConfig.gui().name(), "");

        return create(guiType, guiName, List.of());
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
     * Close the current inventory/gui with an OPEN_NEW reason and open the {@link #categoryGUI}.
     */
    @Override
    public void close() {
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            isOpen = false;

            player.closeInventory(InventoryCloseEvent.Reason.OPEN_NEW);

            guiManager.removeOpenGUI(player.getUniqueId());

            categoryGUI.open();
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
    public boolean update() {
        Locale locale = localeManager.getLocale();

        // If the InventoryView was not created, log a warning and return false.
        if(inventoryView == null) {
            logger.warn(AdventureUtil.deserialize("Unable to add GUIButton ItemStacks to the InventoryView as it was not created."));
            if(isOpen) close();
            return false;
        }

        // Get the GUI size
        int guiSize = inventoryView.getTopInventory().getSize();

        // Clear the GUI of buttons
        clearButtons();

        // Check if at least 1 page is configured.
        List<TransactionConfig.PageConfig> pages = transactionConfig.gui().pages();
        if(pages.isEmpty()) {
            logger.error(AdventureUtil.deserialize("Unable to decorate the transaction GUI due to no pages configured."));
            if(isOpen) close();
            return false;
        }

        // Get the page config
        TransactionConfig.PageConfig page = pages.get(pageNum);

        // Check if at least 1 button is configured.
        List<TransactionConfig.Button> entries = page.buttons();
        if(entries.isEmpty()) {
            logger.error(AdventureUtil.deserialize("Unable to decorate the transaction GUI for page " + pageNum + " due to no buttons configured."));
            if(isOpen) close();
            return false;
        }

        for(int buttonNum = 0; buttonNum < page.buttons().size(); buttonNum++) {
            TransactionConfig.Button buttonConfig = page.buttons().get(buttonNum);
            @Nullable ButtonType buttonType = buttonConfig.buttonType();

            // Handle the creation of buttons by button type.
            switch(buttonType) {
                case FILLER -> createFilterButton(guiSize, buttonConfig);

                case PREVIOUS_PAGE -> createPreviousPageButton(buttonConfig, buttonNum, buttonType);

                case NEXT_PAGE -> createNextPageButton(buttonConfig, pages.size(), buttonNum, buttonType);

                case RETURN -> createExitButton(buttonConfig, buttonNum, buttonType);

                case DISPLAY -> createDisplayButton(buttonConfig, buttonNum, buttonType);

                case SELL_ALL -> createSellAllButton(buttonConfig, buttonNum, buttonType);

                case SELL_GUI -> createSellGUIButton(locale, buttonConfig, buttonNum, buttonType);

                case BUY -> createBuyButton(buttonConfig, buttonNum, buttonType);

                case SELL -> createSellButton(buttonConfig, buttonNum, buttonType);

                case DUMMY -> createDummyButton(buttonConfig, buttonNum, buttonType);

                case null -> logger.warn(AdventureUtil.deserialize("Unable to add a button due to an invalid button type. Button Num: " + buttonNum));

                default -> logger.warn(AdventureUtil.deserialize("Unsupported ButtonType in the transaction GUI for " + buttonNum + " on page " + pageNum + " and style " + transactionStyle + "."));
            }
        }

        return super.update();
    }

    /**
     * Open the previous CategoryGUI if the reason the inventory closed was not OPEN_NEW or UNLOADED.
     * @param inventoryCloseEvent InventoryCloseEvent
     */
    @Override
    public void handleClose(@NotNull InventoryCloseEvent inventoryCloseEvent) {
        if(inventoryCloseEvent.getReason().equals(InventoryCloseEvent.Reason.UNLOADED) || inventoryCloseEvent.getReason().equals(InventoryCloseEvent.Reason.OPEN_NEW)) return;

        guiManager.removeOpenGUI(uuid);

        categoryGUI.open();
    }

    /**
     * Handle when the bottom part of the inventory is dragged. Does nothing.
     * @param inventoryDragEvent An {@link InventoryDragEvent}.
     */
    @Override
    public void handleBottomDrag(@NotNull InventoryDragEvent inventoryDragEvent) {}

    /**
     * Handle when any part of the inventory is dragged. Does nothing.
     * @param inventoryDragEvent An {@link InventoryDragEvent}.
     */
    @Override
    public void handleGlobalDrag(@NotNull InventoryDragEvent inventoryDragEvent) {}

    /**
     * Handle when the bottom part of the inventory is clicked. Does nothing.
     * @param inventoryClickEvent An {@link InventoryClickEvent}.
     */
    @Override
    public void handleBottomClick(@NotNull InventoryClickEvent inventoryClickEvent) {}

    /**
     * Handle when any part of the inventory is clicked. Does nothing.
     * @param inventoryClickEvent An {@link InventoryClickEvent}.
     */
    @Override
    public void handleGlobalClick(@NotNull InventoryClickEvent inventoryClickEvent) {}

    /**
     * Create and add filler buttons.
     * @param guiSize The size of the GUI.
     * @param buttonConfig The {@link TransactionConfig.Button} config.
     */
    private void createFilterButton(
            int guiSize,
            @NotNull TransactionConfig.Button buttonConfig) {
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

            for(int i = 0; i <= (guiSize - 1); i++) {
                setButton(i, fillerButton);
            }
        });
    }

    /**
     * Create and add a previous page button.
     * @param buttonConfig The {@link TransactionConfig.Button} config.
     * @param buttonNum The button number.
     * @param buttonType The {@link ButtonType}
     */
    private void createPreviousPageButton(
            @NotNull TransactionConfig.Button buttonConfig,
            int buttonNum,
            @NotNull ButtonType buttonType) {
        // Only display the previous page button if the page number is greater than or equal to 1
        if(pageNum >= 1) {
            // Check if the slot is not configured and send a warning.
            if(buttonConfig.slot() == null) {
                logger.warn(AdventureUtil.deserialize("Unable to add a button due to a null slot. Button Num: " + buttonNum + " and type: " + buttonType));
                return;
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

    /**
     * Create and add a next page button.
     * @param buttonConfig The {@link TransactionConfig.Button} config.
     * @param pageSize The size of the page.
     * @param buttonNum The button number.
     * @param buttonType The {@link ButtonType}
     */
    private void createNextPageButton(
            @NotNull TransactionConfig.Button buttonConfig,
            int pageSize,
            int buttonNum,
            @NotNull ButtonType buttonType) {
        // Only display the next page button if another page is configured after the current
        if(pageNum < (pageSize - 1)) {
            // Check if the slot is not configured and send a warning.
            if(buttonConfig.slot() == null) {
                logger.warn(AdventureUtil.deserialize("Unable to add a button due to a null slot. Button Num: " + buttonNum + " and type: " + buttonType));
                return;
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

    /**
     * Create and add an exit button.
     * @param buttonConfig The {@link TransactionConfig.Button} config.
     * @param buttonNum The button number.
     * @param buttonType The {@link ButtonType}
     */
    private void createExitButton(
            @NotNull TransactionConfig.Button buttonConfig,
            int buttonNum,
            @NotNull ButtonType buttonType) {
        // Check if the slot is not configured and send a warning.
        if(buttonConfig.slot() == null) {
            logger.warn(AdventureUtil.deserialize("Unable to add a button due to a null slot. Button Num: " + buttonNum + " and type: " + buttonType));
            return;
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

    /**
     * Create and add a display button.
     * @param buttonConfig The {@link TransactionConfig.Button} config.
     * @param buttonNum The button number.
     * @param buttonType The {@link ButtonType}
     */
    private void createDisplayButton(
            @NotNull TransactionConfig.Button buttonConfig,
            int buttonNum,
            @NotNull ButtonType buttonType) {
        // Check if the slot is not configured and send a warning.
        if(buttonConfig.slot() == null) {
            logger.warn(AdventureUtil.deserialize("Unable to add a button due to a null slot. Button Num: " + buttonNum + " and type: " + buttonType));
            return;
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

    /**
     * Create and add a sell all button.
     * @param buttonConfig The {@link TransactionConfig.Button} config.
     * @param buttonNum The button number.
     * @param buttonType The {@link ButtonType}
     */
    private void createSellAllButton(
            @NotNull TransactionConfig.Button buttonConfig,
            int buttonNum,
            @NotNull ButtonType buttonType) {
        // Check if the slot is not configured and send a warning.
        if(buttonConfig.slot() == null) {
            logger.warn(AdventureUtil.deserialize("Unable to add a button due to a null slot. Button Num: " + buttonNum + " and type: " + buttonType));
            return;
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

                skyShopAPI.sellAllMatchingItemStack(player, player.getInventory(), transactionItemStack, true, false, true);
            });

            setButton(buttonConfig.slot(), guiButtonBuilder.build());
        });
    }

    /**
     * Create and add a sell GUI button.
     * @param buttonConfig The {@link TransactionConfig.Button} config.
     * @param buttonNum The button number.
     * @param buttonType The {@link ButtonType}
     */
    private void createSellGUIButton(
            @NotNull Locale locale,
            @NotNull TransactionConfig.Button buttonConfig,
            int buttonNum,
            @NotNull ButtonType buttonType) {
        // Check if the slot is not configured and send a warning.
        if(buttonConfig.slot() == null) {
            logger.warn(AdventureUtil.deserialize("Unable to add a button due to a null slot. Button Num: " + buttonNum + " and type: " + buttonType));
            return;
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
                    logger.error(AdventureUtil.deserialize("Unable to open sellall GUI for player " + player.getName() + " due to invalid sellall config."));
                    player.sendMessage(AdventureUtil.deserialize(locale.prefix() + locale.guiOpenError()));
                    if(isOpen) close();
                    return;
                }

                SellAllConfig sellAllGuiConfig = optionalGUIConfig.get();
                SellAllGUI sellAllGUI = new SellAllGUI(skyShop, guiManager, sellAllGuiConfig, skyShopAPI, player);

                boolean creationResult = sellAllGUI.create();
                if(!creationResult) {
                    logger.error(AdventureUtil.deserialize("Unable to create the InventoryView for the sellall GUI for player " + player.getName() + " due to a configuration error."));
                    player.sendMessage(AdventureUtil.deserialize(locale.prefix() + locale.guiOpenError()));
                    if(isOpen) close();
                    return;
                }

                boolean updateResult = sellAllGUI.update();
                if(!updateResult) {
                    logger.error(AdventureUtil.deserialize("Unable to decorate the sellall GUI for player " + player.getName() + " due to a configuration error."));
                    player.sendMessage(AdventureUtil.deserialize(locale.prefix() + locale.guiOpenError()));
                    if(isOpen) close();
                    return;
                }

                boolean openResult = sellAllGUI.open();
                if(!openResult) {
                    logger.error(AdventureUtil.deserialize("Unable to open the sellall GUI for player " + player.getName() + " due to a configuration error."));
                    player.sendMessage(AdventureUtil.deserialize(locale.prefix() + locale.guiOpenError()));
                    if(isOpen) close();
                }
            });

            setButton(buttonConfig.slot(), guiButtonBuilder.build());
        });
    }

    /**
     * Create and add a buy button.
     * @param buttonConfig The {@link TransactionConfig.Button} config.
     * @param buttonNum The button number.
     * @param buttonType The {@link ButtonType}
     */
    private void createBuyButton(
            @NotNull TransactionConfig.Button buttonConfig,
            int buttonNum,
            @NotNull ButtonType buttonType) {
        if(buyPrice <= 0.0 && buyPoints <= 0) return;

        // Check if the slot is not configured and send a warning.
        if(buttonConfig.slot() == null) {
            logger.warn(AdventureUtil.deserialize("Unable to add a buy button due to a null slot. Button Num: " + buttonNum + " and type: " + buttonType));
            return;
        }

        ItemStackConfig itemConfig = buttonConfig.displayItem();
        if(buttonConfig.transactionAmount() == null || buttonConfig.transactionAmount() <= 0) {
            logger.warn(AdventureUtil.deserialize("Unable to add a buy button due to an invalid transaction amount."));
            return;
        }

        // Get the amount to purchase
        int purchaseAmount = buttonConfig.transactionAmount();
        // Calculate the final prices
        double finalBuyPrice = buyPrice > 0 ? buyPrice * purchaseAmount : -1;
        int finalBuyPoints = buyPoints > 0 ? buyPoints * purchaseAmount : -1;

        // Create the ItemStack placeholders
        List<TagResolver.Single> itemStackPlaceholders = new ArrayList<>();
        itemStackPlaceholders.add(Placeholder.parsed("buy_price", String.valueOf(finalBuyPrice)));
        itemStackPlaceholders.add(Placeholder.parsed("buy_points", String.valueOf(finalBuyPoints)));
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
                guiButtonBuilder.setAction(inventoryClickEvent -> buyItem(purchaseAmount, finalBuyPrice, finalBuyPoints));
            } else if(transactionType.equals(TransactionType.COMMAND)) {
                guiButtonBuilder.setAction(inventoryClickEvent -> buyCommand(purchaseAmount, finalBuyPrice, finalBuyPoints));
            }

            setButton(buttonConfig.slot(), guiButtonBuilder.build());
        });
    }

    /**
     * Create and add a sell button.
     * @param buttonConfig The {@link TransactionConfig.Button} config.
     * @param buttonNum The button number.
     * @param buttonType The {@link ButtonType}
     */
    private void createSellButton(
            @NotNull TransactionConfig.Button buttonConfig,
            int buttonNum,
            @NotNull ButtonType buttonType) {
        if(sellPrice <= 0.0 && sellPoints <= 0) return;

        // Check if the slot is not configured and send a warning.
        if(buttonConfig.slot() == null) {
            logger.warn(AdventureUtil.deserialize("Unable to add a sell button due to a null slot. Button Num: " + buttonNum + " and type: " + buttonType));
            return;
        }

        // Get the ItemStackConfig
        ItemStackConfig itemConfig = buttonConfig.displayItem();
        if(buttonConfig.transactionAmount() == null || buttonConfig.transactionAmount() <= 0) {
            logger.warn(AdventureUtil.deserialize("Unable to add a sell button due to an invalid transaction amount."));
            return;
        }

        // Get the amount to sell
        int sellAmount = buttonConfig.transactionAmount();
        // Calculate the final prices
        double finalSellPrice = sellPrice > 0 ? sellPrice * sellAmount : -1;
        int finalSellPoints = sellPoints > 0 ? sellPoints * sellAmount : -1;

        // Create the ItemStack placeholders
        List<TagResolver.Single> itemStackPlaceholders = new ArrayList<>();
        itemStackPlaceholders.add(Placeholder.parsed("sell_price", String.valueOf(finalSellPrice)));
        itemStackPlaceholders.add(Placeholder.parsed("sell_points", String.valueOf(finalSellPoints)));
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
                guiButtonBuilder.setAction(inventoryClickEvent -> sellItem(sellAmount, finalSellPrice, finalSellPoints));
            } else if(transactionType.equals(TransactionType.COMMAND)) {
                guiButtonBuilder.setAction(inventoryClickEvent -> sellCommand(sellAmount, finalSellPrice, finalSellPoints));
            }

            setButton(buttonConfig.slot(), guiButtonBuilder.build());
        });
    }

    /**
     * Create and add a dummy button.
     * @param buttonConfig The {@link TransactionConfig.Button} config.
     * @param buttonNum The button number.
     * @param buttonType The {@link ButtonType}
     */
    private void createDummyButton(
            @NotNull TransactionConfig.Button buttonConfig,
            int buttonNum,
            @NotNull ButtonType buttonType) {
        // Check if the slot is not configured and send a warning.
        if(buttonConfig.slot() == null) {
            logger.warn(AdventureUtil.deserialize("Unable to add a dummy button due to a null slot. Button Num: " + buttonNum + " and type: " + buttonType));
            return;
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

            setButton(buttonConfig.slot(), guiButtonBuilder.build());
        });
    }

    /**
     * Initiate the buying of an item.
     * @param amount The amount involved.
     * @param money The money.
     * @param points The player points.
     */
    private void buyItem(int amount, double money, int points) {
        if(money < 0 && points < 0) return;
        Locale locale = localeManager.getLocale();
        EconomyHook economyHook = hookManager.getHook(EconomyHook.class);
        PlayerPointsHook playerPointsHook = hookManager.getHook(PlayerPointsHook.class);

        if(money > 0) {
            if(!economyHook.isHooked()) {
                logger.error(AdventureUtil.deserialize("Unable to buy this item due to no economy found."));
                player.sendMessage(AdventureUtil.deserialize(locale.prefix() + locale.transactionError()));
                close();
                return;
            }

            if(economyHook.getBalance(player) < money) {
                player.sendMessage(AdventureUtil.deserialize(player, locale.prefix() + locale.insufficientMoney()));
                close();
                return;
            }
        }

        if(points > 0) {
            if(!playerPointsHook.isHooked()) {
                logger.error(AdventureUtil.deserialize("Unable to buy this item due to no player points dependency found."));
                player.sendMessage(AdventureUtil.deserialize(locale.prefix() + locale.transactionError()));
                close();
                return;
            }
        }

        // Create the ItemStackBuilder and pass the config to use to create the ItemStack.
        ItemStackBuilder itemStackBuilder = new ItemStackBuilder(logger);
        itemStackBuilder.fromItemStackConfig(transactionItemConfig, player, null, List.of());

        // Build the ItemStack that will be given to the player on successful purchase.
        Optional<ItemStack> optionalItemStack = itemStackBuilder.buildItemStack();
        if(optionalItemStack.isEmpty()) {
            logger.error(AdventureUtil.deserialize("Unable to buy item due the transaction ItemStack being failed to be created from the transaction item config."));
            player.sendMessage(AdventureUtil.deserialize(locale.prefix() + locale.transactionError()));
            close();
            return;
        }

        // Get the ItemStack and set the proper amount
        ItemStack buyItem = optionalItemStack.get();
        buyItem.setAmount(amount);
        ItemType itemType = buyItem.getType().asItemType();
        if(itemType == null) return;

        // Create and call the ItemPurchasedEvent
        ItemPurchasedEvent itemPurchasedEvent = new ItemPurchasedEvent(buyItem);
        skyShop.getServer().getPluginManager().callEvent(itemPurchasedEvent);
        // If the event was cancelled, cancel the purchase.
        if(itemPurchasedEvent.isCancelled()) return;

        // Remove the prices from the player's balances
        if(money > 0) economyHook.removeFromBalance(player, money);
        if(points > 0) playerPointsHook.removeFromBalance(player, points);

        // Give the player the ItemStack they purchased.
        PlayerUtil.giveItem(player.getInventory(), buyItem, amount, player.getLocation());

        // Create the necessary placeholders
        List<TagResolver.Single> successPlaceholders = buildPlaceholders(economyHook, playerPointsHook, player, transactionName, amount, money, points);

        // Send the message that the transaction was a success
        if(money > 0 && points > 0) {
            player.sendMessage(AdventureUtil.deserialize(player, locale.prefix() + locale.buyItemSuccess().moneyAndPoints(), successPlaceholders));
        } else if(money > 0) {
            player.sendMessage(AdventureUtil.deserialize(player, locale.prefix() + locale.buyItemSuccess().money(), successPlaceholders));
        } else { // Points only
            player.sendMessage(AdventureUtil.deserialize(player, locale.prefix() + locale.buyItemSuccess().points(), successPlaceholders));
        }

        // Increment stats if statsManager is not null
        if(statsManager != null) statsManager.incrementAmountPurchased(itemType, amount);
    }

    /**
     * Initiate the selling of an item.
     * @param amount The amount involved.
     * @param money The money.
     * @param points The player points.
     */
    private void sellItem(int amount, double money, int points) {
        if(money < 0 && points < 0) return;
        Locale locale = localeManager.getLocale();
        EconomyHook economyHook = hookManager.getHook(EconomyHook.class);
        PlayerPointsHook playerPointsHook = hookManager.getHook(PlayerPointsHook.class);

        // Create the ItemStack that will be taken from the player if they have enough of said ItemStack.
        ItemStackBuilder itemStackBuilder = new ItemStackBuilder(logger);
        itemStackBuilder.fromItemStackConfig(transactionItemConfig, player, null, List.of());

        // Build the ItemStack that will be taken to the player on successful selling.
        Optional<ItemStack> optionalItemStack = itemStackBuilder.buildItemStack();
        if(optionalItemStack.isEmpty()) {
            logger.error(AdventureUtil.deserialize("Unable to sell this item due the transaction ItemStack being failed to be created from the transaction item config."));
            player.sendMessage(AdventureUtil.deserialize(locale.prefix() + locale.transactionError()));
            close();
            return;
        }

        // Get the ItemStack and set the proper amount
        ItemStack sellItem = optionalItemStack.get();
        sellItem.setAmount(amount);
        ItemType itemType = sellItem.getType().asItemType();
        if(itemType == null) return;

        // Check if the player has the required amount to sell
        if(!player.getInventory().containsAtLeast(sellItem, amount)) {
            player.sendMessage(AdventureUtil.deserialize(player, locale.prefix() + locale.notEnoughItems()));
            close();
            return;
        }

        // Create and call the ItemSoldEvent
        ItemSoldEvent itemSoldEvent = new ItemSoldEvent(sellItem);
        skyShop.getServer().getPluginManager().callEvent(itemSoldEvent);
        // If the event was cancelled, cancel the transaction.
        if(itemSoldEvent.isCancelled()) return;

        // Remove the sold item from the player's inventory.
        player.getInventory().removeItem(sellItem);

        // Add the prices to the player's balances
        if(money > 0) economyHook.addToBalance(player, money);
        if(points > 0) playerPointsHook.addToBalance(player, points);

        // Create the necessary placeholders
        List<TagResolver.Single> successPlaceholders = buildPlaceholders(economyHook, playerPointsHook, player, transactionName, amount, money, points);

        // Send the message that the transaction was a success
        if(money > 0 && points > 0) {
            player.sendMessage(AdventureUtil.deserialize(player, locale.prefix() + locale.sellItemSuccess().moneyAndPoints(), successPlaceholders));
        } else if(money > 0) {
            player.sendMessage(AdventureUtil.deserialize(player, locale.prefix() + locale.sellItemSuccess().money(), successPlaceholders));
        } else { // Points only
            player.sendMessage(AdventureUtil.deserialize(player, locale.prefix() + locale.sellItemSuccess().points(), successPlaceholders));
        }

        // Increment stats if statsManager is not null
        if(statsManager != null) statsManager.incrementAmountSold(itemType, amount);
    }

    /**
     * Initiate the buying of a command.
     * @param amount The amount involved.
     * @param money The money.
     * @param points The player points.
     */
    private void buyCommand(int amount, double money, int points) {
        if(money < 0 && points < 0) return;
        Locale locale = localeManager.getLocale();
        EconomyHook economyHook = hookManager.getHook(EconomyHook.class);
        PlayerPointsHook playerPointsHook = hookManager.getHook(PlayerPointsHook.class);

        if(money > 0) {
            if(!economyHook.isHooked()) {
                logger.error(AdventureUtil.deserialize("Unable to buy these command(s) due to no economy found."));
                player.sendMessage(AdventureUtil.deserialize(locale.prefix() + locale.transactionError()));
                close();
                return;
            }

            if(economyHook.getBalance(player) < money) {
                player.sendMessage(AdventureUtil.deserialize(player, locale.prefix() + locale.insufficientMoney()));
                close();
                return;
            }
        }

        if(points > 0) {
            if(!playerPointsHook.isHooked()) {
                logger.error(AdventureUtil.deserialize("Unable to buy these command(s) due to no player points dependency found."));
                player.sendMessage(AdventureUtil.deserialize(locale.prefix() + locale.transactionError()));
                close();
                return;
            }

            if(playerPointsHook.getBalance(player) < points) {
                player.sendMessage(AdventureUtil.deserialize(player, locale.prefix() + locale.insufficientPlayerPoints()));
                close();
                return;
            }
        }

        // Create and call the CommandPurchasedEvent
        CommandPurchasedEvent commandPurchasedEvent = new CommandPurchasedEvent(buyCommands);
        skyShop.getServer().getPluginManager().callEvent(commandPurchasedEvent);
        // If the event was cancelled, cancel the purchase.
        if(commandPurchasedEvent.isCancelled()) return;

        // Remove the prices from the player's balances
        if(money > 0) economyHook.removeFromBalance(player, money);
        if(points > 0) playerPointsHook.removeFromBalance(player, points);

        // Execute the commands for this transaction
        Server server = skyShop.getServer();
        ConsoleCommandSender commandSender = server.getConsoleSender();
        for(String command : buyCommands) {
            for(int i = 1; i <= amount; i++) {
                server.dispatchCommand(commandSender, PlaceholderAPIUtil.parsePlaceholders(player, command));
            }
        }

        // Create the necessary placeholders
        List<TagResolver.Single> successPlaceholders = buildPlaceholders(economyHook, playerPointsHook, player, transactionName, amount, money, points);

        // Send the message that the transaction was a success
        if(money > 0 && points > 0) {
            player.sendMessage(AdventureUtil.deserialize(player, locale.prefix() + locale.buyCommandSuccess().moneyAndPoints(), successPlaceholders));
        } else if(money > 0) {
            player.sendMessage(AdventureUtil.deserialize(player, locale.prefix() + locale.buyCommandSuccess().money(), successPlaceholders));
        } else { // Points only
            player.sendMessage(AdventureUtil.deserialize(player, locale.prefix() + locale.buyCommandSuccess().points(), successPlaceholders));
        }
    }

    /**
     * Initiate the selling of a command.
     * @param amount The amount involved.
     * @param money The money.
     * @param points The player points.
     */
    private void sellCommand(int amount, double money, int points) {
        if(money < 0 && points < 0) return;
        Locale locale = localeManager.getLocale();
        EconomyHook economyHook = hookManager.getHook(EconomyHook.class);
        PlayerPointsHook playerPointsHook = hookManager.getHook(PlayerPointsHook.class);

        // Create and call the CommandSoldEvent
        CommandSoldEvent commandSoldEvent = new CommandSoldEvent(sellCommands);
        skyShop.getServer().getPluginManager().callEvent(commandSoldEvent);
        // If the event was cancelled, cancel the transaction.
        if(commandSoldEvent.isCancelled()) return;

        // Add the prices to the player's balances
        if(money > 0) economyHook.addToBalance(player, money);
        if(points > 0) playerPointsHook.addToBalance(player, points);

        // Execute the commands for this transaction
        Server server = skyShop.getServer();
        ConsoleCommandSender commandSender = skyShop.getServer().getConsoleSender();
        for(String command : sellCommands) {
            for(int i = 1; i <= amount; i++) {
                server.dispatchCommand(commandSender, PlaceholderAPIUtil.parsePlaceholders(player, command));
            }
        }

        // Create the necessary placeholders
        List<TagResolver.Single> successPlaceholders = buildPlaceholders(economyHook, playerPointsHook, player, transactionName, amount, money, points);

        // Send the message that the transaction was a success
        if(money > 0 && points > 0) {
            player.sendMessage(AdventureUtil.deserialize(player, locale.prefix() + locale.sellCommandSuccess().moneyAndPoints(), successPlaceholders));
        } else if(money > 0) {
            player.sendMessage(AdventureUtil.deserialize(player, locale.prefix() + locale.sellCommandSuccess().money(), successPlaceholders));
        } else { // Points only
            player.sendMessage(AdventureUtil.deserialize(player, locale.prefix() + locale.sellCommandSuccess().points(), successPlaceholders));
        }
    }

    /**
     * Create the list of placeholders for success messages.
     * @param economyHook The {@link EconomyHook}.
     * @param playerPointsHook The {@link PlayerPointsHook}.
     * @param player The {@link Player}.
     * @param transactionName The transaction name.
     * @param amount The amount.
     * @param money The money.
     * @param points The player points.
     * @return A {@link List} of {@link TagResolver.Single}.
     */
    private @NotNull List<TagResolver.Single> buildPlaceholders(
            @NotNull EconomyHook economyHook,
            @NotNull PlayerPointsHook playerPointsHook,
            @NotNull Player player,
            @NotNull String transactionName,
            int amount,
            double money,
            int points) {
        List<TagResolver.Single> placeholders = new ArrayList<>();
        placeholders.add(Placeholder.parsed("amount", String.valueOf(amount)));
        placeholders.add(Placeholder.parsed("transaction_name", transactionName));

        if(money > 0 && points > 0) {
            DecimalFormat df = new DecimalFormat("#.##");
            df.setRoundingMode(RoundingMode.CEILING);

            BigDecimal bigPrice = BigDecimal.valueOf(money);
            String formattedSellPrice = df.format(bigPrice);
            placeholders.add(Placeholder.parsed("money", formattedSellPrice));

            BigDecimal bigBalance = BigDecimal.valueOf(economyHook.getBalance(player));
            placeholders.add(Placeholder.parsed("money_balance", df.format(bigBalance)));

            placeholders.add(Placeholder.parsed("player_points", String.valueOf(points)));
            placeholders.add(Placeholder.parsed("player_points_balance", String.valueOf(playerPointsHook.getBalance(player))));
        } else if(money > 0) {
            DecimalFormat df = new DecimalFormat("#.##");
            df.setRoundingMode(RoundingMode.CEILING);

            BigDecimal bigPrice = BigDecimal.valueOf(money);
            String formattedSellPrice = df.format(bigPrice);
            placeholders.add(Placeholder.parsed("money", formattedSellPrice));

            BigDecimal bigBalance = BigDecimal.valueOf(economyHook.getBalance(player));
            placeholders.add(Placeholder.parsed("money_balance", df.format(bigBalance)));

            placeholders.add(Placeholder.parsed("player_points", "0"));
            placeholders.add(Placeholder.parsed("player_points_balance", "0"));
        } else if(points > 0) {
            placeholders.add(Placeholder.parsed("player_points", String.valueOf(points)));
            placeholders.add(Placeholder.parsed("player_points_balance", String.valueOf(playerPointsHook.getBalance(player))));

            placeholders.add(Placeholder.parsed("money", "0"));
            placeholders.add(Placeholder.parsed("money_balance", "0"));
        }

        return placeholders;
    }
}
