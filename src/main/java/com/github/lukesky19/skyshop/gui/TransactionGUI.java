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
import com.github.lukesky19.skyshop.SkyShop;
import com.github.lukesky19.skyshop.api.SkyShopAPI;
import com.github.lukesky19.skyshop.api.configuration.TransactionConfiguration;
import com.github.lukesky19.skyshop.api.processor.TransactionProcessor;
import com.github.lukesky19.skyshop.api.result.TransactionResult;
import com.github.lukesky19.skyshop.configuration.category.gui.CategoryConfig;
import com.github.lukesky19.skyshop.configuration.category.transaction.ItemConfiguration;
import com.github.lukesky19.skyshop.configuration.locale.Locale;
import com.github.lukesky19.skyshop.configuration.locale.LocaleManager;
import com.github.lukesky19.skyshop.configuration.sellall.SellAllConfig;
import com.github.lukesky19.skyshop.configuration.sellall.SellAllManager;
import com.github.lukesky19.skyshop.configuration.transaction.TransactionStyleConfig;
import com.github.lukesky19.skyshop.hook.HookManager;
import com.github.lukesky19.skyshop.hook.impl.EconomyHook;
import com.github.lukesky19.skyshop.hook.impl.PlayerPointsHook;
import com.github.lukesky19.skyshop.registry.RegistryManager;
import com.github.lukesky19.skyshop.util.ButtonType;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.*;
import java.util.function.Consumer;

/**
 * This class is called to create a transaction inventory for a player to buy and sell items.
 */
public class TransactionGUI extends ChestGUI<UUID> {
    private final @NotNull SkyShop skyShop;
    private final @NotNull LocaleManager localeManager;
    private final @NotNull SellAllManager sellAllManager;
    private final @NotNull RegistryManager registryManager;
    private final @NotNull HookManager hookManager;
    private final @NotNull SkyShopAPI skyShopAPI;
    private final @NotNull CategoryGUI categoryGUI;

    // Config related to the Transaction
    private final @NotNull TransactionStyleConfig transactionConfig;
    private final @NotNull String transactionStyle;
    private final @NotNull String transactionName;
    // ItemStack data
    private final @NotNull ItemStackConfig displayItemConfig;

    private final @NotNull List<TransactionConfiguration> transactionConfigurationList;

    // Price Data
    private final double buyPrice;
    private final double sellPrice;
    private final int buyPoints;
    private final int sellPoints;

    // Page Data
    private int pageNum = 0;

    /**
     * Constructor
     * @param skyShop A {@link SkyShop} instance.
     * @param guiManager An {@link IGUIManager} instance.
     * @param player The {@link Player} to create the GUI for.
     * @param localeManager A {@link LocaleManager} instance.
     * @param sellAllManager A {@link SellAllManager} instance.
     * @param registryManager A {@link RegistryManager} instance.
     * @param hookManager A {@link HookManager} instance.
     * @param skyShopAPI A {@link SkyShopAPI} instance.
     * @param categoryGUI The {@link CategoryGUI} the player came from.
     * @param transactionStyle The transaction style name. This is the {@link String} that was used to get the {@link TransactionStyleConfig}.
     * @param transactionConfig The {@link TransactionStyleConfig} to create the GUI with.
     * @param displayItemConfig The {@link ItemStackConfig} used to create the {@link ItemStack} that displays what is being purchased or sold.
     * @param priceConfig The {@link CategoryConfig.PriceConfig} for this transaction.
     * @param transactionName The name to use when displaying a successful transaction message.
     * @param transactionConfigurationList The {@link List} of {@link TransactionConfiguration}s to process.
     */
    public TransactionGUI(
            @NotNull SkyShop skyShop,
            @NotNull IGUIManager<UUID> guiManager,
            @NotNull Player player,
            @NotNull LocaleManager localeManager,
            @NotNull SellAllManager sellAllManager,
            @NotNull RegistryManager registryManager,
            @NotNull HookManager hookManager,
            @NotNull SkyShopAPI skyShopAPI,
            @NotNull CategoryGUI categoryGUI,
            @NotNull String transactionStyle,
            @NotNull TransactionStyleConfig transactionConfig,
            @NotNull ItemStackConfig displayItemConfig,
            @NotNull CategoryConfig.PriceConfig priceConfig,
            @Nullable String transactionName,
            @NotNull List<TransactionConfiguration> transactionConfigurationList) {
        super(skyShop, guiManager, player.getUniqueId(), player);

        this.skyShop = skyShop;
        this.localeManager = localeManager;
        this.sellAllManager = sellAllManager;
        this.registryManager = registryManager;
        this.hookManager = hookManager;
        this.skyShopAPI = skyShopAPI;
        this.categoryGUI = categoryGUI;
        this.transactionStyle = transactionStyle;
        this.transactionConfig = transactionConfig;
        this.displayItemConfig = displayItemConfig;
        this.buyPrice = priceConfig.buyPrice() > 0.0 ? priceConfig.buyPrice() : -1.0;
        this.sellPrice = priceConfig.sellPrice() > 0.0 ? priceConfig.sellPrice() : -1.0;
        this.buyPoints = priceConfig.buyPoints() > 0 ? priceConfig.buyPoints() : -1;
        this.sellPoints = priceConfig.sellPoints() > 0 ? priceConfig.sellPoints() : -1;
        this.transactionName = Objects.requireNonNullElse(transactionName, "");
        this.transactionConfigurationList = transactionConfigurationList;
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
     * Close the current inventory/gui with an OPEN_NEW reason and open the {@link #categoryGUI}.
     */
    @Override
    public void close() {
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            player.closeInventory(InventoryCloseEvent.Reason.OPEN_NEW);

            guiManager.removeOpenGUI(player.getUniqueId());

            categoryGUI.open();
        }, 1L);
    }

    /**
     * A method to create all the buttons in the inventory GUI.
     */
    @Override
    public boolean update() {
        Locale locale = localeManager.getConfiguration();

        // If the InventoryView was not created, log a warning and return false.
        if(inventoryView == null) {
            logger.warn(AdventureUtil.deserialize("Unable to add GUIButton ItemStacks to the InventoryView as it was not created."));
            close();
            return false;
        }

        // Get the GUI size
        int guiSize = inventoryView.getTopInventory().getSize();

        // Clear the GUI of buttons
        clearButtons();

        // Check if at least 1 page is configured.
        List<TransactionStyleConfig.PageConfig> pages = transactionConfig.gui().pages();
        if(pages.isEmpty()) {
            logger.error(AdventureUtil.deserialize("Unable to decorate the transaction GUI due to no pages configured."));
            close();
            return false;
        }

        // Get the page config
        TransactionStyleConfig.PageConfig page = pages.get(pageNum);

        // Check if at least 1 button is configured.
        List<TransactionStyleConfig.Button> entries = page.buttons();
        if(entries.isEmpty()) {
            logger.error(AdventureUtil.deserialize("Unable to decorate the transaction GUI for page " + pageNum + " due to no buttons configured."));
            close();
            return false;
        }

        for(int buttonNum = 0; buttonNum < page.buttons().size(); buttonNum++) {
            TransactionStyleConfig.Button buttonConfig = page.buttons().get(buttonNum);
            @Nullable ButtonType buttonType = buttonConfig.buttonType();

            // Handle the creation of buttons by button type.
            switch(buttonType) {
                case FILLER -> createFilterButton(guiSize, buttonConfig);

                case PREVIOUS_PAGE -> createPreviousPageButton(buttonConfig, buttonNum, buttonType);

                case NEXT_PAGE -> createNextPageButton(buttonConfig, pages.size(), buttonNum, buttonType);

                case RETURN -> createExitButton(buttonConfig, buttonNum, buttonType);

                case DISPLAY -> createButton(
                        buttonType,
                        new TransactionStyleConfig.Button(ButtonType.DISPLAY, buttonConfig.slot(), null, displayItemConfig),
                        List.of(),
                        null);

                case SELL_ALL -> createSellAllButton(buttonConfig, buttonNum, buttonType);

                case SELL_GUI -> createSellGUIButton(locale, buttonConfig, buttonNum, buttonType);

                case BUY -> createBuyButton(buttonConfig, buttonNum, buttonType);

                case SELL -> createSellButton(buttonConfig, buttonNum, buttonType);

                case DUMMY -> createButton(buttonType, buttonConfig, List.of(), null);

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
     * @param buttonConfig The {@link TransactionStyleConfig.Button} config.
     */
    private void createFilterButton(int guiSize, @NotNull TransactionStyleConfig.Button buttonConfig) {
        // Get the ItemStackConfig
        com.github.lukesky19.skylib.api.itemstack.ItemStackConfig itemConfig = buttonConfig.displayItem();

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
     * @param buttonConfig The {@link TransactionStyleConfig.Button} config.
     * @param buttonNum The button number.
     * @param buttonType The {@link ButtonType}
     */
    private void createPreviousPageButton(
            @NotNull TransactionStyleConfig.Button buttonConfig,
            int buttonNum,
            @NotNull ButtonType buttonType) {
        // Only display the previous page button if the page number is greater than or equal to 1
        if(pageNum >= 1) {
            // Check if the slot is not configured and send a warning.
            if(buttonConfig.slot() == null) {
                logger.warn(AdventureUtil.deserialize("Unable to add a previous page button due to a null slot. Button Num: " + buttonNum + " and type: " + buttonType));
                return;
            }

            createButton(buttonType, buttonConfig, List.of(), inventoryClickEvent -> {
                pageNum = pageNum - 1;
                update();
            });
        }
    }

    /**
     * Create and add a next page button.
     * @param buttonConfig The {@link TransactionStyleConfig.Button} config.
     * @param pageSize The size of the page.
     * @param buttonNum The button number.
     * @param buttonType The {@link ButtonType}
     */
    private void createNextPageButton(
            @NotNull TransactionStyleConfig.Button buttonConfig,
            int pageSize,
            int buttonNum,
            @NotNull ButtonType buttonType) {
        // Only display the next page button if another page is configured after the current
        if(pageNum < (pageSize - 1)) {
            // Check if the slot is not configured and send a warning.
            if(buttonConfig.slot() == null) {
                logger.warn(AdventureUtil.deserialize("Unable to add a next page button due to a null slot. Button Num: " + buttonNum + " and type: " + buttonType));
                return;
            }

            createButton(buttonType, buttonConfig, List.of(), inventoryClickEvent -> {
                pageNum = pageNum + 1;
                update();
            });
        }
    }

    /**
     * Create and add an exit button.
     * @param buttonConfig The {@link TransactionStyleConfig.Button} config.
     * @param buttonNum The button number.
     * @param buttonType The {@link ButtonType}
     */
    private void createExitButton(
            @NotNull TransactionStyleConfig.Button buttonConfig,
            int buttonNum,
            @NotNull ButtonType buttonType) {
        // Check if the slot is not configured and send a warning.
        if(buttonConfig.slot() == null) {
            logger.warn(AdventureUtil.deserialize("Unable to add a button due to a null slot. Button Num: " + buttonNum + " and type: " + buttonType));
            return;
        }

        createButton(buttonType, buttonConfig, List.of(), inventoryClickEvent -> close());
    }

    /**
     * Create and add a sell all button.
     * @param buttonConfig The {@link TransactionStyleConfig.Button} config.
     * @param buttonNum The button number.
     * @param buttonType The {@link ButtonType}
     */
    private void createSellAllButton(
            @NotNull TransactionStyleConfig.Button buttonConfig,
            int buttonNum,
            @NotNull ButtonType buttonType) {
        // Check if the slot is not configured and send a warning.
        if(buttonConfig.slot() == null) {
            logger.warn(AdventureUtil.deserialize("Unable to add a button due to a null slot. Button Num: " + buttonNum + " and type: " + buttonType));
            return;
        }

        createButton(buttonType, buttonConfig, List.of(), inventoryClickEvent -> {
            for(TransactionConfiguration data : transactionConfigurationList) {
                if(!(data instanceof ItemConfiguration itemConfiguration)) continue;

                ItemStackBuilder transactionItemBuilder = new ItemStackBuilder(logger);
                transactionItemBuilder.fromItemStackConfig(itemConfiguration.transactionItem(), player, null, List.of());
                Optional<ItemStack> optionalTransactionItemStack = transactionItemBuilder.buildItemStack();
                if(optionalTransactionItemStack.isEmpty()) continue;
                ItemStack transactionItemStack = optionalTransactionItemStack.get();

                skyShopAPI.sellAllMatchingItemStack(player, player.getInventory(), transactionItemStack, true, false, true);
            }
        });
    }

    /**
     * Create and add a sell GUI button.
     * @param buttonConfig The {@link TransactionStyleConfig.Button} config.
     * @param buttonNum The button number.
     * @param buttonType The {@link ButtonType}
     */
    private void createSellGUIButton(
            @NotNull Locale locale,
            @NotNull TransactionStyleConfig.Button buttonConfig,
            int buttonNum,
            @NotNull ButtonType buttonType) {
        // Check if the slot is not configured and send a warning.
        if(buttonConfig.slot() == null) {
            logger.warn(AdventureUtil.deserialize("Unable to add a button due to a null slot. Button Num: " + buttonNum + " and type: " + buttonType));
            return;
        }

        createButton(buttonType, buttonConfig, List.of(), inventoryClickEvent -> {
            SellAllConfig sellAllGuiConfig = sellAllManager.getConfiguration();
            if(sellAllGuiConfig == null) {
                logger.error(AdventureUtil.deserialize("Unable to open sellall GUI for player " + player.getName() + " due to invalid sellall config."));
                player.sendMessage(AdventureUtil.deserialize(locale.prefix() + locale.guiOpenError()));
                close();
                return;
            }
            SellAllGUI sellAllGUI = new SellAllGUI(skyShop, guiManager, sellAllGuiConfig, skyShopAPI, player);

            boolean creationResult = sellAllGUI.create();
            if(!creationResult) {
                logger.error(AdventureUtil.deserialize("Unable to create the InventoryView for the sellall GUI for player " + player.getName() + " due to a configuration error."));
                player.sendMessage(AdventureUtil.deserialize(locale.prefix() + locale.guiOpenError()));
                close();
                return;
            }

            boolean updateResult = sellAllGUI.update();
            if(!updateResult) {
                logger.error(AdventureUtil.deserialize("Unable to decorate the sellall GUI for player " + player.getName() + " due to a configuration error."));
                player.sendMessage(AdventureUtil.deserialize(locale.prefix() + locale.guiOpenError()));
                close();
                return;
            }

            boolean openResult = sellAllGUI.open();
            if(!openResult) {
                logger.error(AdventureUtil.deserialize("Unable to open the sellall GUI for player " + player.getName() + " due to a configuration error."));
                player.sendMessage(AdventureUtil.deserialize(locale.prefix() + locale.guiOpenError()));
                close();
            }
        });
    }

    /**
     * Create and add a buy button.
     * @param buttonConfig The {@link TransactionStyleConfig.Button} config.
     * @param buttonNum The button number.
     * @param buttonType The {@link ButtonType}
     */
    private void createBuyButton(
            @NotNull TransactionStyleConfig.Button buttonConfig,
            int buttonNum,
            @NotNull ButtonType buttonType) {
        if(buyPrice <= 0.0 && buyPoints <= 0) return;

        // Check if the slot is not configured and send a warning.
        if(buttonConfig.slot() == null) {
            logger.warn(AdventureUtil.deserialize("Unable to add a buy button due to a null slot. Button Num: " + buttonNum + " and type: " + buttonType));
            return;
        }

        if(buttonConfig.transactionAmount() == null || buttonConfig.transactionAmount() <= 0) {
            logger.warn(AdventureUtil.deserialize("Unable to add a buy button due to an invalid transaction amount."));
            return;
        }

        if(transactionConfigurationList.isEmpty()) {
            logger.warn(AdventureUtil.deserialize("Unable to add a buy button because the transaction configuration list is empty."));
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

        createButton(buttonType, buttonConfig, itemStackPlaceholders, inventoryClickEvent -> {
            Locale locale = localeManager.getConfiguration();
            EconomyHook economyHook = hookManager.getHook(EconomyHook.class);
            PlayerPointsHook playerPointsHook = hookManager.getHook(PlayerPointsHook.class);

            if(!validateMoneyAndPoints(locale, economyHook, playerPointsHook, player, finalBuyPrice, finalBuyPoints)) return;

            // Early checks
            for(TransactionConfiguration configuration : transactionConfigurationList) {
                @Nullable TransactionProcessor processor = registryManager.getProcessor(configuration.getId());
                if(processor == null) {
                    logger.warn(AdventureUtil.deserialize("No processor for id " + configuration.getId()));
                    player.sendMessage(AdventureUtil.deserialize(locale.prefix() + locale.transactionError()));
                    return;
                }

                TransactionResult canBuyResult = processor.canBuy(player, configuration, purchaseAmount);
                if(canBuyResult.cancelled()) return;

                if(canBuyResult.errored()) {
                    if(canBuyResult.sendErrorMessage()) {
                        logger.warn(AdventureUtil.deserialize("Pre-buy failed for a transaction with id: " + configuration.getId() + ". Error: " + canBuyResult.message()));
                        player.sendMessage(AdventureUtil.deserialize(locale.prefix() + locale.transactionError()));
                    }

                    return;
                }
            }

            // Actual buy
            for(TransactionConfiguration configuration : transactionConfigurationList) {
                @Nullable TransactionProcessor processor = registryManager.getProcessor(configuration.getId());
                if(processor == null) {
                    logger.warn(AdventureUtil.deserialize("No processor for id " + configuration.getId()));
                    player.sendMessage(AdventureUtil.deserialize(locale.prefix() + locale.transactionError()));
                    return;
                }

                TransactionResult buyResult = processor.buy(player, configuration, purchaseAmount);
                if(buyResult.errored()) {
                    if(buyResult.sendErrorMessage()) {
                        logger.warn(AdventureUtil.deserialize("Failed to process (buy) a portion of a transaction with id: " + configuration.getId() + ". Error: " + buyResult.message()));
                        player.sendMessage(AdventureUtil.deserialize(locale.prefix() + locale.transactionError()));
                    }
                }
            }

            // Remove the prices from the player's balances
            if(finalBuyPrice > 0 && economyHook.isHooked()) economyHook.removeFromBalance(player, finalBuyPrice);
            if(finalBuyPoints > 0 && playerPointsHook.isHooked()) playerPointsHook.removeFromBalance(player, finalBuyPoints);

            // Create the message placeholders
            List<TagResolver.Single> messagePlaceholders = new ArrayList<>();
            messagePlaceholders.add(Placeholder.parsed("amount", String.valueOf(purchaseAmount)));
            messagePlaceholders.add(Placeholder.parsed("transaction_name", transactionName));

            DecimalFormat df = new DecimalFormat("#.##");
            df.setRoundingMode(RoundingMode.CEILING);

            BigDecimal bigPrice = BigDecimal.valueOf(finalBuyPrice);
            String formattedSellPrice = df.format(bigPrice);
            messagePlaceholders.add(Placeholder.parsed("money", formattedSellPrice));

            BigDecimal bigBalance = BigDecimal.valueOf(economyHook.getBalance(player));
            messagePlaceholders.add(Placeholder.parsed("money_balance", df.format(bigBalance)));

            messagePlaceholders.add(Placeholder.parsed("player_points", String.valueOf(finalBuyPoints)));
            messagePlaceholders.add(Placeholder.parsed("player_points_balance", String.valueOf(playerPointsHook.getBalance(player))));

            // Send the message that the transaction was a success
            if(finalBuyPrice > 0 && finalBuyPoints > 0) {
                player.sendMessage(AdventureUtil.deserialize(player, locale.prefix() + locale.buySuccess().moneyAndPoints(), messagePlaceholders));
            } else if(finalBuyPrice > 0) {
                player.sendMessage(AdventureUtil.deserialize(player, locale.prefix() + locale.buySuccess().money(), messagePlaceholders));
            } else if(finalBuyPoints > 0) {
                player.sendMessage(AdventureUtil.deserialize(player, locale.prefix() + locale.buySuccess().points(), messagePlaceholders));
            }
        });
    }

    /**
     * Create and add a sell button.
     * @param buttonConfig The {@link TransactionStyleConfig.Button} config.
     * @param buttonNum The button number.
     * @param buttonType The {@link ButtonType}
     */
    private void createSellButton(
            @NotNull TransactionStyleConfig.Button buttonConfig,
            int buttonNum,
            @NotNull ButtonType buttonType) {
        if(sellPrice <= 0.0 && sellPoints <= 0) return;

        // Check if the slot is not configured and send a warning.
        if(buttonConfig.slot() == null) {
            logger.warn(AdventureUtil.deserialize("Unable to add a sell button due to a null slot. Button Num: " + buttonNum + " and type: " + buttonType));
            return;
        }

        // Check if the transaction amount is valid
        if(buttonConfig.transactionAmount() == null || buttonConfig.transactionAmount() <= 0) {
            logger.warn(AdventureUtil.deserialize("Unable to add a sell button due to an invalid transaction amount."));
            return;
        }

        if(transactionConfigurationList.isEmpty()) {
            logger.warn(AdventureUtil.deserialize("Unable to add a buy button because the transaction configuration list is empty."));
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

        createButton(buttonType, buttonConfig, itemStackPlaceholders, inventoryClickEvent -> {
            Locale locale = localeManager.getConfiguration();
            EconomyHook economyHook = hookManager.getHook(EconomyHook.class);
            PlayerPointsHook playerPointsHook = hookManager.getHook(PlayerPointsHook.class);

            // Early checks
            for(TransactionConfiguration configuration : transactionConfigurationList) {
                @Nullable TransactionProcessor processor = registryManager.getProcessor(configuration.getId());
                if(processor == null) {
                    logger.warn(AdventureUtil.deserialize("No processor for id " + configuration.getId()));
                    player.sendMessage(AdventureUtil.deserialize(locale.prefix() + locale.transactionError()));
                    return;
                }

                TransactionResult canSellResult = processor.canSell(player, configuration, sellAmount);
                if(canSellResult.cancelled()) return;

                if(canSellResult.errored()) {
                    if(canSellResult.sendErrorMessage()) {
                        logger.warn(AdventureUtil.deserialize("Pre-sell failed for a transaction with id: " + configuration.getId() + ". Error: " + canSellResult.message()));
                        player.sendMessage(AdventureUtil.deserialize(locale.prefix() + locale.transactionError()));
                    }

                    return;
                }
            }

            // Actual Sell
            for(TransactionConfiguration configuration : transactionConfigurationList) {
                @Nullable TransactionProcessor processor = registryManager.getProcessor(configuration.getId());
                if(processor == null) {
                    logger.warn(AdventureUtil.deserialize("No processor for id " + configuration.getId()));
                    player.sendMessage(AdventureUtil.deserialize(locale.prefix() + locale.transactionError()));
                    return;
                }

                TransactionResult sellResult = processor.sell(player, configuration, sellAmount);
                if(sellResult.errored()) {
                    if(sellResult.sendErrorMessage()) {
                        logger.warn(AdventureUtil.deserialize("Failed to process (sell) a portion of a transaction with id: " + configuration.getId() + ". Error: " + sellResult.message()));
                        player.sendMessage(AdventureUtil.deserialize(locale.prefix() + locale.transactionError()));
                    }
                }
            }

            // Add the prices from the player's balances
            if(finalSellPrice > 0 && economyHook.isHooked()) economyHook.addToBalance(player, finalSellPrice);
            if(finalSellPoints > 0 && playerPointsHook.isHooked()) playerPointsHook.addToBalance(player, finalSellPoints);

            // Create the message placeholders
            List<TagResolver.Single> messagePlaceholders = new ArrayList<>();
            messagePlaceholders.add(Placeholder.parsed("amount", String.valueOf(sellAmount)));
            messagePlaceholders.add(Placeholder.parsed("transaction_name", transactionName));

            DecimalFormat df = new DecimalFormat("#.##");
            df.setRoundingMode(RoundingMode.CEILING);

            BigDecimal bigPrice = BigDecimal.valueOf(finalSellPrice);
            String formattedSellPrice = df.format(bigPrice);
            messagePlaceholders.add(Placeholder.parsed("money", formattedSellPrice));

            BigDecimal bigBalance = BigDecimal.valueOf(economyHook.getBalance(player));
            messagePlaceholders.add(Placeholder.parsed("money_balance", df.format(bigBalance)));

            messagePlaceholders.add(Placeholder.parsed("player_points", String.valueOf(finalSellPoints)));
            messagePlaceholders.add(Placeholder.parsed("player_points_balance", String.valueOf(playerPointsHook.getBalance(player))));

            // Send the message that the transaction was a success
            if(finalSellPrice > 0 && finalSellPoints > 0) {
                player.sendMessage(AdventureUtil.deserialize(player, locale.prefix() + locale.sellSuccess().moneyAndPoints(), messagePlaceholders));
            } else if(finalSellPrice > 0) {
                player.sendMessage(AdventureUtil.deserialize(player, locale.prefix() + locale.sellSuccess().money(), messagePlaceholders));
            } else if(finalSellPoints > 0) {
                player.sendMessage(AdventureUtil.deserialize(player, locale.prefix() + locale.sellSuccess().points(), messagePlaceholders));
            }
        });
    }

    /**
     * Create and add a button.
     * @param buttonType The {@link ButtonType}.
     * @param buttonConfig The {@link TransactionStyleConfig.Button} config.
     * @param placeholders A {@link List} of {@link TagResolver.Single} for placeholders.
     * @param action A {@link Consumer} consuming an {@link InventoryClickEvent} that is used when the button is clicked.
     */
    private void createButton(
            @NotNull ButtonType buttonType,
            @NotNull TransactionStyleConfig.Button buttonConfig,
            @NotNull List<TagResolver.Single> placeholders,
            @Nullable Consumer<InventoryClickEvent> action) {
        if(buttonConfig.slot() == null) {
            logger.warn(AdventureUtil.deserialize("Unable to add a button due to a null slot. ButtonType: " + buttonType));
            return;
        }

        ItemStackBuilder itemStackBuilder = new ItemStackBuilder(logger);
        itemStackBuilder.fromItemStackConfig(buttonConfig.displayItem(), player, null, placeholders);
        Optional<ItemStack> optionalItemStack = itemStackBuilder.buildItemStack();
        optionalItemStack.ifPresent(itemStack -> {
            GUIButton.Builder guiButtonBuilder = new GUIButton.Builder();
            guiButtonBuilder.setItemStack(itemStack);
            if(action != null) guiButtonBuilder.setAction(action);

            setButton(buttonConfig.slot(), guiButtonBuilder.build());
        });
    }

    /**
     * Check if the money and points are valid and that the necessary plugins are hooked into.
     * The GUI the player is in will be closed on any error.
     * @param locale The plugin's {@link Locale}.
     * @param economyHook The plugin's {@link EconomyHook}.
     * @param playerPointsHook The player's {@link PlayerPointsHook}.
     * @param player The {@link Player}.
     * @param money The money.
     * @param points The player points.
     * @return true if money and points are valid along with the necessary hooks or false.
     */
    private boolean validateMoneyAndPoints(
            @NotNull Locale locale,
            @NotNull EconomyHook economyHook,
            @NotNull PlayerPointsHook playerPointsHook,
            @NotNull Player player,
            double money,
            int points) {
        if(money <= 0 && points <= 0) {
            logger.error(AdventureUtil.deserialize("Unable to complete the transaction because money and points are less than or equal to 0."));
            player.sendMessage(AdventureUtil.deserialize(locale.prefix() + locale.transactionError()));
            this.close();
            return false;
        }

        if(money > 0) {
            if(!economyHook.isHooked()) {
                logger.error(AdventureUtil.deserialize("Unable to complete the transaction due to no economy found."));
                player.sendMessage(AdventureUtil.deserialize(locale.prefix() + locale.transactionError()));
                this.close();
                return false;
            }

            if(economyHook.getBalance(player) < money) {
                player.sendMessage(AdventureUtil.deserialize(player, locale.prefix() + locale.insufficientMoney()));
                this.close();
                return false;
            }
        }

        if(points > 0) {
            if(!playerPointsHook.isHooked()) {
                logger.error(AdventureUtil.deserialize("Unable to complete the transaction due to no player points dependency found."));
                player.sendMessage(AdventureUtil.deserialize(locale.prefix() + locale.transactionError()));
                this.close();
                return false;
            }

            if(playerPointsHook.getBalance(player) < points) {
                player.sendMessage(AdventureUtil.deserialize(player, locale.prefix() + locale.insufficientPlayerPoints()));
                this.close();
                return false;
            }
        }

        return true;
    }
}