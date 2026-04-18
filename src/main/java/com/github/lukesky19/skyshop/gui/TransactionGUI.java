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

import com.github.lukesky19.skylib.common.api.adventure.AdventureUtility;
import com.github.lukesky19.skylib.paper.api.gui.GUIButton;
import com.github.lukesky19.skylib.paper.api.gui.GUIType;
import com.github.lukesky19.skylib.paper.api.gui.interfaces.IGUIManager;
import com.github.lukesky19.skylib.paper.api.gui.templates.ChestGUI;
import com.github.lukesky19.skylib.paper.api.itemstack.ItemStackBuilder;
import com.github.lukesky19.skylib.paper.api.itemstack.ItemStackConfig;
import com.github.lukesky19.skyshop.SkyShop;
import com.github.lukesky19.skyshop.api.SkyShopAPI;
import com.github.lukesky19.skyshop.api.configuration.TransactionConfiguration;
import com.github.lukesky19.skyshop.configuration.category.data.CategoryConfigV4;
import com.github.lukesky19.skyshop.configuration.category.transaction.ItemConfiguration;
import com.github.lukesky19.skyshop.configuration.locale.LocaleManager;
import com.github.lukesky19.skyshop.configuration.locale.data.LocaleV5;
import com.github.lukesky19.skyshop.configuration.sellall.SellAllManager;
import com.github.lukesky19.skyshop.configuration.sellall.data.SellAllGUIConfigV3;
import com.github.lukesky19.skyshop.configuration.transaction.TransactionGUIConfigManager;
import com.github.lukesky19.skyshop.configuration.transaction.data.TransactionGUIConfigV3;
import com.github.lukesky19.skyshop.player.data.PlayerData;
import com.github.lukesky19.skyshop.transaction.TransactionManager;
import com.github.lukesky19.skyshop.util.ButtonType;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.*;
import java.util.function.Consumer;

/**
 * This class is called to create a transaction inventory for a player to buy and sell items.
 */
public class TransactionGUI extends ChestGUI<UUID> {
    private final @NonNull SkyShop skyShop;
    private final @NonNull LocaleManager localeManager;
    private final @NonNull SellAllManager sellAllManager;
    private final @NonNull SkyShopAPI skyShopAPI;
    private final @NonNull CategoryGUI categoryGUI;
    private final @NonNull TransactionManager transactionManager;

    private final @NonNull PlayerData playerData;

    // Config related to the Transaction
    private final CategoryConfigV4.@NonNull TransactionData transactionData;
    private final @Nullable TransactionGUIConfigV3 transactionStyleConfig;

    // Price config
    private final CategoryConfigV4.@NonNull PriceConfig priceConfig;

    // Display item config being purchased
    private final @NonNull ItemStackConfig displayItemConfig;

    // Page Data
    private int pageNum = 0;

    /**
     * Constructor
     * @param skyShop A {@link SkyShop} instance.
     * @param guiManager An {@link IGUIManager} instance.
     * @param player The {@link Player} to create the GUI for.
     * @param playerData The player's {@link PlayerData}.
     * @param localeManager A {@link LocaleManager} instance.
     * @param sellAllManager A {@link SellAllManager} instance.
     * @param transactionStyleConfigManager A {@link TransactionGUIConfigManager} instance.
     * @param skyShopAPI A {@link SkyShopAPI} instance.
     * @param transactionManager A {@link TransactionManager} instance.
     * @param categoryGUI The {@link CategoryGUI} the player came from.
     * @param transactionData The {@link CategoryConfigV4.TransactionData}.
     * @param displayItemConfig The {@link ItemStackConfig} used to create the {@link ItemStack} that displays what is being purchased or sold.
     * @param priceConfig The {@link CategoryConfigV4.PriceConfig} for this transaction.
     */
    public TransactionGUI(
            @NonNull SkyShop skyShop,
            @NonNull IGUIManager<UUID> guiManager,
            @NonNull Player player,
            @NonNull PlayerData playerData,
            @NonNull LocaleManager localeManager,
            @NonNull SellAllManager sellAllManager,
            @NonNull TransactionGUIConfigManager transactionStyleConfigManager,
            @NonNull SkyShopAPI skyShopAPI,
            @NonNull TransactionManager transactionManager,
            @NonNull CategoryGUI categoryGUI,
            CategoryConfigV4.@NonNull TransactionData transactionData,
            @NonNull ItemStackConfig displayItemConfig,
            CategoryConfigV4.@NonNull PriceConfig priceConfig) {
        super(skyShop, guiManager, player.getUniqueId(), player);
        this.playerData = playerData;

        // Classes
        this.skyShop = skyShop;
        this.localeManager = localeManager;
        this.sellAllManager = sellAllManager;
        this.skyShopAPI = skyShopAPI;
        this.transactionManager = transactionManager;

        // Previous GUI
        this.categoryGUI = categoryGUI;


        // Data
        LocaleV5 locale = localeManager.getConfiguration();
        this.transactionData = transactionData;
        this.displayItemConfig = displayItemConfig;
        this.priceConfig = priceConfig;
        this.transactionStyleConfig = transactionStyleConfigManager.getTransactionConfig(transactionData.transactionStyle());

        // Validation
        if(transactionStyleConfig == null) {
            player.sendMessage(AdventureUtility.deserialize(locale.prefix() + locale.guiOpenError()));
            throw new RuntimeException("No transaction style found for transaction style " + transactionData.transactionStyle());
        }
    }

    /**
     * Create the {@link InventoryView} for this GUI.
     * @return true if created successfully, otherwise false.
     */
    public boolean create() {
        if(transactionStyleConfig == null) {
            logger.warn(AdventureUtility.plain("Unable to create the InventoryView for a transaction GUI due to invalid style config for " + transactionData.transactionStyle()));
            return false;
        }

        GUIType guiType = transactionStyleConfig.guiType();
        if(guiType == null) {
            logger.warn(AdventureUtility.plain("Unable to create the InventoryView for a transaction GUI due to an invalid GUIType"));
            return false;
        }

        String guiName = Objects.requireNonNullElse(transactionStyleConfig.guiName(), "");

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

            categoryGUI.refresh();

            categoryGUI.open();
        }, 1L);
    }

    /**
     * A method to create all the buttons in the inventory GUI.
     */
    @Override
    public boolean update() {
        // If the InventoryView was not created, log a warning and return false.
        if(inventoryView == null) {
            logger.warn(AdventureUtility.plain("Unable to add GUIButton ItemStacks to the InventoryView as it was not created."));
            close();
            return false;
        }

        if(transactionStyleConfig == null) {
            logger.warn(AdventureUtility.plain("Unable to update the InventoryView for a transaction GUI due to invalid style config for " + transactionData.transactionStyle()));
            close();
            return false;
        }

        // Get the GUI size
        int guiSize = inventoryView.getTopInventory().getSize();

        // Clear the GUI of buttons
        clearButtons();

        // Check if at least 1 page is configured.
        List<TransactionGUIConfigV3.PageConfig> pages = transactionStyleConfig.pages();
        if(pages.isEmpty()) {
            logger.error(AdventureUtility.plain("Unable to decorate the transaction GUI due to no pages configured."));
            close();
            return false;
        }

        // Get the page config
        TransactionGUIConfigV3.PageConfig page = pages.get(pageNum);

        // Check if at least 1 button is configured.
        List<TransactionGUIConfigV3.Button> entries = page.buttons();
        if(entries.isEmpty()) {
            logger.error(AdventureUtility.plain("Unable to decorate the transaction GUI for page " + pageNum + " due to no buttons configured."));
            close();
            return false;
        }

        LocaleV5 locale = localeManager.getConfiguration();
        for(int buttonNum = 0; buttonNum < page.buttons().size(); buttonNum++) {
            TransactionGUIConfigV3.Button buttonConfig = page.buttons().get(buttonNum);
            ButtonType buttonType = buttonConfig.buttonType();

            // Handle the creation of buttons by button type.
            switch(buttonType) {
                case FILLER -> createFilterButton(guiSize, buttonConfig);

                case PREVIOUS_PAGE -> createPreviousPageButton(buttonConfig, buttonNum, buttonType);

                case NEXT_PAGE -> createNextPageButton(buttonConfig, pages.size(), buttonNum, buttonType);

                case RETURN -> createExitButton(buttonConfig, buttonNum, buttonType);

                case DISPLAY -> createButton(
                        buttonType,
                        new TransactionGUIConfigV3.Button(ButtonType.DISPLAY, buttonConfig.slot(), null, displayItemConfig),
                        List.of(),
                        null);

                case SELL_ALL -> createSellAllButton(buttonConfig, buttonNum, buttonType);

                case SELL_GUI -> createSellGUIButton(locale, buttonConfig, buttonNum, buttonType);

                case BUY -> createBuyButton(buttonConfig, buttonNum, buttonType);

                case SELL -> createSellButton(buttonConfig, buttonNum, buttonType);

                case DUMMY -> createButton(buttonType, buttonConfig, List.of(), null);

                case null -> logger.warn(AdventureUtility.plain("Unable to add a button due to an invalid button type. Button Num: " + buttonNum));

                default -> logger.warn(AdventureUtility.plain("Unsupported ButtonType in the transaction GUI for " + buttonNum + " on page " + pageNum + " and style " + transactionData.transactionStyle() + "."));
            }
        }

        return super.update();
    }

    /**
     * Open the previous CategoryGUI if the reason the inventory closed was not OPEN_NEW or UNLOADED.
     * @param inventoryCloseEvent InventoryCloseEvent
     */
    @Override
    public void handleClose(@NonNull InventoryCloseEvent inventoryCloseEvent) {
        if(inventoryCloseEvent.getReason().equals(InventoryCloseEvent.Reason.UNLOADED) || inventoryCloseEvent.getReason().equals(InventoryCloseEvent.Reason.OPEN_NEW)) return;

        guiManager.removeOpenGUI(uuid);

        categoryGUI.open();
    }

    /**
     * Handle when the bottom part of the inventory is dragged. Does nothing.
     * @param inventoryDragEvent An {@link InventoryDragEvent}.
     */
    @Override
    public void handleBottomDrag(@NonNull InventoryDragEvent inventoryDragEvent) {}

    /**
     * Handle when any part of the inventory is dragged. Does nothing.
     * @param inventoryDragEvent An {@link InventoryDragEvent}.
     */
    @Override
    public void handleGlobalDrag(@NonNull InventoryDragEvent inventoryDragEvent) {}

    /**
     * Handle when the bottom part of the inventory is clicked. Does nothing.
     * @param inventoryClickEvent An {@link InventoryClickEvent}.
     */
    @Override
    public void handleBottomClick(@NonNull InventoryClickEvent inventoryClickEvent) {}

    /**
     * Handle when any part of the inventory is clicked. Does nothing.
     * @param inventoryClickEvent An {@link InventoryClickEvent}.
     */
    @Override
    public void handleGlobalClick(@NonNull InventoryClickEvent inventoryClickEvent) {}

    /**
     * Create and add filler buttons.
     * @param guiSize The size of the GUI.
     * @param buttonConfig The {@link TransactionGUIConfigV3.Button} config.
     */
    private void createFilterButton(int guiSize, TransactionGUIConfigV3.@NonNull Button buttonConfig) {
        // Get the ItemStackConfig
        ItemStackConfig itemConfig = buttonConfig.displayItem();

        // Create the ItemStackBuilder and pass the ItemStackConfig.
        ItemStackBuilder itemStackBuilder = new ItemStackBuilder(logger);
        itemStackBuilder.fromItemStackConfig(itemConfig, player, List.of());

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
     * @param buttonConfig The {@link TransactionGUIConfigV3.Button} config.
     * @param buttonNum The button number.
     * @param buttonType The {@link ButtonType}
     */
    private void createPreviousPageButton(
            TransactionGUIConfigV3.@NonNull Button buttonConfig,
            int buttonNum,
            @NonNull ButtonType buttonType) {
        // Only display the previous page button if the page number is greater than or equal to 1
        if(pageNum >= 1) {
            // Check if the slot is not configured and send a warning.
            if(buttonConfig.slot() == null) {
                logger.warn(AdventureUtility.plain("Unable to add a previous page button due to a null slot. Button Num: " + buttonNum + " and type: " + buttonType));
                return;
            }

            createButton(buttonType, buttonConfig, List.of(), _ -> {
                pageNum = pageNum - 1;
                update();
            });
        }
    }

    /**
     * Create and add a next page button.
     * @param buttonConfig The {@link TransactionGUIConfigV3.Button} config.
     * @param pageSize The size of the page.
     * @param buttonNum The button number.
     * @param buttonType The {@link ButtonType}
     */
    private void createNextPageButton(
            TransactionGUIConfigV3.@NonNull Button buttonConfig,
            int pageSize,
            int buttonNum,
            @NonNull ButtonType buttonType) {
        // Only display the next page button if another page is configured after the current
        if(pageNum < (pageSize - 1)) {
            // Check if the slot is not configured and send a warning.
            if(buttonConfig.slot() == null) {
                logger.warn(AdventureUtility.plain("Unable to add a next page button due to a null slot. Button Num: " + buttonNum + " and type: " + buttonType));
                return;
            }

            createButton(buttonType, buttonConfig, List.of(), _ -> {
                pageNum = pageNum + 1;
                update();
            });
        }
    }

    /**
     * Create and add an exit button.
     * @param buttonConfig The {@link TransactionGUIConfigV3.Button} config.
     * @param buttonNum The button number.
     * @param buttonType The {@link ButtonType}
     */
    private void createExitButton(
            TransactionGUIConfigV3.@NonNull Button buttonConfig,
            int buttonNum,
            @NonNull ButtonType buttonType) {
        // Check if the slot is not configured and send a warning.
        if(buttonConfig.slot() == null) {
            logger.warn(AdventureUtility.plain("Unable to add a button due to a null slot. Button Num: " + buttonNum + " and type: " + buttonType));
            return;
        }

        createButton(buttonType, buttonConfig, List.of(), _ -> close());
    }

    /**
     * Create and add a sell all button.
     * @param buttonConfig The {@link TransactionGUIConfigV3.Button} config.
     * @param buttonNum The button number.
     * @param buttonType The {@link ButtonType}
     */
    private void createSellAllButton(
            TransactionGUIConfigV3.@NonNull Button buttonConfig,
            int buttonNum,
            @NonNull ButtonType buttonType) {
        // Check if the slot is not configured and send a warning.
        if(buttonConfig.slot() == null) {
            logger.warn(AdventureUtility.plain("Unable to add a button due to a null slot. Button Num: " + buttonNum + " and type: " + buttonType));
            return;
        }

        createButton(buttonType, buttonConfig, List.of(), _ -> {
            for(TransactionConfiguration data : transactionData.transactionList()) {
                if(!(data instanceof ItemConfiguration itemConfiguration)) continue;

                ItemStackBuilder transactionItemBuilder = new ItemStackBuilder(logger);
                transactionItemBuilder.fromItemStackConfig(itemConfiguration.transactionItem(), player, List.of());
                Optional<ItemStack> optionalTransactionItemStack = transactionItemBuilder.buildItemStack();
                if(optionalTransactionItemStack.isEmpty()) continue;
                ItemStack transactionItemStack = optionalTransactionItemStack.get();

                skyShopAPI.sellAllMatchingItemStack(player, player.getInventory(), transactionItemStack, true, false, true);
            }
        });
    }

    /**
     * Create and add a sell GUI button.
     * @param buttonConfig The {@link TransactionGUIConfigV3.Button} config.
     * @param buttonNum The button number.
     * @param buttonType The {@link ButtonType}
     */
    private void createSellGUIButton(
            @NonNull LocaleV5 locale,
            TransactionGUIConfigV3.@NonNull Button buttonConfig,
            int buttonNum,
            @NonNull ButtonType buttonType) {
        // Check if the slot is not configured and send a warning.
        if(buttonConfig.slot() == null) {
            logger.warn(AdventureUtility.plain("Unable to add a button due to a null slot. Button Num: " + buttonNum + " and type: " + buttonType));
            return;
        }

        createButton(buttonType, buttonConfig, List.of(), _ -> {
            SellAllGUIConfigV3 sellAllGuiConfig = sellAllManager.getConfiguration();
            if(sellAllGuiConfig == null) {
                logger.error(AdventureUtility.plain("Unable to open sellall GUI for player " + player.getName() + " due to invalid sellall config."));
                player.sendMessage(AdventureUtility.deserialize(locale.prefix() + locale.guiOpenError()));
                close();
                return;
            }
            SellAllGUI sellAllGUI = new SellAllGUI(skyShop, guiManager, sellAllGuiConfig, skyShopAPI, player);

            boolean creationResult = sellAllGUI.create();
            if(!creationResult) {
                logger.error(AdventureUtility.plain("Unable to create the InventoryView for the sellall GUI for player " + player.getName() + " due to a configuration error."));
                player.sendMessage(AdventureUtility.deserialize(locale.prefix() + locale.guiOpenError()));
                close();
                return;
            }

            boolean updateResult = sellAllGUI.update();
            if(!updateResult) {
                logger.error(AdventureUtility.plain("Unable to decorate the sellall GUI for player " + player.getName() + " due to a configuration error."));
                player.sendMessage(AdventureUtility.deserialize(locale.prefix() + locale.guiOpenError()));
                close();
                return;
            }

            boolean openResult = sellAllGUI.open();
            if(!openResult) {
                logger.error(AdventureUtility.plain("Unable to open the sellall GUI for player " + player.getName() + " due to a configuration error."));
                player.sendMessage(AdventureUtility.deserialize(locale.prefix() + locale.guiOpenError()));
                close();
            }
        });
    }

    /**
     * Create and add a buy button.
     * @param buttonConfig The {@link TransactionGUIConfigV3.Button} config.
     * @param buttonNum The button number.
     * @param buttonType The {@link ButtonType}
     */
    private void createBuyButton(
            TransactionGUIConfigV3.@NonNull Button buttonConfig,
            int buttonNum,
            @NonNull ButtonType buttonType) {
        if(priceConfig.buyMoney() <= 0.0 && priceConfig.buyPoints() <= 0) return;

        // Check if the slot is not configured and send a warning.
        if(buttonConfig.slot() == null) {
            logger.warn(AdventureUtility.plain("Unable to add a buy button due to a null slot. Button Num: " + buttonNum + " and type: " + buttonType));
            return;
        }

        if(buttonConfig.transactionAmount() == null || buttonConfig.transactionAmount() <= 0) {
            logger.warn(AdventureUtility.plain("Unable to add a buy button due to an invalid transaction amount."));
            return;
        }

        if(transactionData.transactionList().isEmpty()) {
            logger.warn(AdventureUtility.plain("Unable to add a buy button because the transaction configuration list is empty."));
            return;
        }

        // Get the amount to purchase
        int purchaseAmount = buttonConfig.transactionAmount();

        // Calculate price data
        TransactionManager.PriceData priceData = transactionManager.calculateBuyPrices(playerData, transactionData.transactionId(),  priceConfig, purchaseAmount);

        // Create the ItemStack placeholders
        DecimalFormat decimalFormat = new DecimalFormat("#.##");
        decimalFormat.setRoundingMode(RoundingMode.CEILING);

        List<TagResolver.Single> itemStackPlaceholders = new ArrayList<>();
        itemStackPlaceholders.add(Placeholder.parsed("buy_price", decimalFormat.format(BigDecimal.valueOf(priceData.money()))));
        itemStackPlaceholders.add(Placeholder.parsed("buy_points", String.valueOf(priceData.points())));
        itemStackPlaceholders.add(Placeholder.parsed("amount", String.valueOf(purchaseAmount)));

        createButton(buttonType, buttonConfig, itemStackPlaceholders, _ ->
                transactionManager.buy(player, playerData, this, transactionData, priceConfig, priceData, purchaseAmount));
    }

    /**
     * Create and add a sell button.
     * @param buttonConfig The {@link TransactionGUIConfigV3.Button} config.
     * @param buttonNum The button number.
     * @param buttonType The {@link ButtonType}
     */
    private void createSellButton(
            TransactionGUIConfigV3.@NonNull Button buttonConfig,
            int buttonNum,
            @NonNull ButtonType buttonType) {
        if(priceConfig.sellMoney() <= 0.0 && priceConfig.sellPoints() <= 0) return;

        // Check if the slot is not configured and send a warning.
        if(buttonConfig.slot() == null) {
            logger.warn(AdventureUtility.plain("Unable to add a sell button due to a null slot. Button Num: " + buttonNum + " and type: " + buttonType));
            return;
        }

        // Check if the transaction amount is valid
        if(buttonConfig.transactionAmount() == null || buttonConfig.transactionAmount() <= 0) {
            logger.warn(AdventureUtility.plain("Unable to add a sell button due to an invalid transaction amount."));
            return;
        }

        if(transactionData.transactionList().isEmpty()) {
            logger.warn(AdventureUtility.plain("Unable to add a buy button because the transaction configuration list is empty."));
            return;
        }

        // Get the amount to sell
        int sellAmount = buttonConfig.transactionAmount();

        // Calculate price data
        TransactionManager.PriceData priceData = transactionManager.calculateSellPrices(playerData, transactionData.transactionId(), priceConfig, sellAmount);

        // Create the ItemStack placeholders
        List<TagResolver.Single> itemStackPlaceholders = new ArrayList<>();
        itemStackPlaceholders.add(Placeholder.parsed("sell_price", String.valueOf(priceData.money())));
        itemStackPlaceholders.add(Placeholder.parsed("sell_points", String.valueOf(priceData.points())));
        itemStackPlaceholders.add(Placeholder.parsed("amount", String.valueOf(sellAmount)));

        createButton(buttonType, buttonConfig, itemStackPlaceholders, _ ->
                transactionManager.sell(player, playerData, this, transactionData, priceConfig, priceData, sellAmount));
    }

    /**
     * Create and add a button.
     * @param buttonType The {@link ButtonType}.
     * @param buttonConfig The {@link TransactionGUIConfigV3.Button} config.
     * @param placeholders A {@link List} of {@link TagResolver.Single} for placeholders.
     * @param action A {@link Consumer} consuming an {@link InventoryClickEvent} that is used when the button is clicked.
     */
    private void createButton(
            @NonNull ButtonType buttonType,
            TransactionGUIConfigV3.@NonNull Button buttonConfig,
            @NonNull List<TagResolver.Single> placeholders,
            @Nullable Consumer<InventoryClickEvent> action) {
        if(buttonConfig.slot() == null) {
            logger.warn(AdventureUtility.plain("Unable to add a button due to a null slot. ButtonType: " + buttonType));
            return;
        }

        ItemStackBuilder itemStackBuilder = new ItemStackBuilder(logger);
        itemStackBuilder.fromItemStackConfig(buttonConfig.displayItem(), player, placeholders);
        Optional<ItemStack> optionalItemStack = itemStackBuilder.buildItemStack();
        optionalItemStack.ifPresent(itemStack -> {
            GUIButton.Builder guiButtonBuilder = new GUIButton.Builder();
            guiButtonBuilder.setItemStack(itemStack);
            if(action != null) guiButtonBuilder.setAction(action);

            setButton(buttonConfig.slot(), guiButtonBuilder.build());
        });
    }
}