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
import com.github.lukesky19.skyshop.SkyShop;
import com.github.lukesky19.skyshop.SkyShopAPI;
import com.github.lukesky19.skyshop.configuration.LocaleManager;
import com.github.lukesky19.skyshop.configuration.SellAllManager;
import com.github.lukesky19.skyshop.configuration.TransactionManager;
import com.github.lukesky19.skyshop.data.Locale;
import com.github.lukesky19.skyshop.data.gui.ShopConfig;
import com.github.lukesky19.skyshop.data.gui.TransactionConfig;
import com.github.lukesky19.skyshop.manager.StatsManager;
import com.github.lukesky19.skyshop.util.ButtonType;
import com.github.lukesky19.skyshop.util.TransactionType;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/**
 * This class is called to create a shop gui for a player to access an individual shop category.
*/
public class ShopGUI extends ChestGUI {
    private final @NotNull SkyShop skyShop;
    private final @NotNull LocaleManager localeManager;
    private final @NotNull TransactionManager transactionManager;
    private final @NotNull SellAllManager sellAllManager;
    private final @Nullable StatsManager statsManager;
    private final @NotNull SkyShopAPI skyShopAPI;

    private int pageNum = 0;
    private boolean isOpen = false;
    private final @NotNull MenuGUI menuGUI;
    private final @NotNull String shopName;
    private final @NotNull ShopConfig shopConfig;

    /**
     * Constructor
     * @param skyShop A {@link SkyShop} instance.
     * @param guiManager An {@link AbstractGUIManager} instance.
     * @param player The {@link Player} viewing the GUI/Inventory.
     * @param localeManager A {@link LocaleManager} instance.
     * @param transactionManager A {@link TransactionManager} instance.
     * @param sellAllManager A {@link SellAllManager} instance.
     * @param statsManager A {@link StatsManager} instance.
     * @param skyShopAPI A {@link SkyShopAPI} instance.
     * @param menuGUI The {@link MenuGUI} the player opened this GUI/Inventory from.
     * @param shopConfig The {@link ShopConfig} associated with the GUI/Inventory being created.
     * @param shopName The name of the shop for this GUI.
     */
    public ShopGUI(
            @NotNull SkyShop skyShop,
            @NotNull AbstractGUIManager guiManager,
            @NotNull Player player,
            @NotNull LocaleManager localeManager,
            @NotNull TransactionManager transactionManager,
            @NotNull SellAllManager sellAllManager,
            @Nullable StatsManager statsManager,
            @NotNull SkyShopAPI skyShopAPI,
            @NotNull MenuGUI menuGUI,
            @NotNull ShopConfig shopConfig,
            @NotNull String shopName) {
        super(skyShop, guiManager, player);

        this.skyShop = skyShop;
        this.localeManager = localeManager;
        this.transactionManager = transactionManager;
        this.sellAllManager = sellAllManager;
        this.statsManager = statsManager;
        this.skyShopAPI = skyShopAPI;
        this.menuGUI = menuGUI;
        this.shopConfig = shopConfig;
        this.shopName = shopName;
    }

    /**
     * Create the {@link InventoryView} for this GUI.
     * @return true if created successfully, otherwise false.
     */
    public boolean create() {
        GUIType guiType = shopConfig.gui().guiType();
        if(guiType == null) {
            logger.warn(AdventureUtil.serialize("Unable to create the InventoryView for a ShopGUI due to an invalid GUIType"));
            return false;
        }

        String guiName = Objects.requireNonNullElse(shopConfig.gui().name(), "");

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
     * Close the current inventory/gui with an OPEN_NEW reason and open the {@link #menuGUI}.
     */
    @Override
    public void close() {
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            isOpen = false;

            player.closeInventory(InventoryCloseEvent.Reason.OPEN_NEW);

            guiManager.removeOpenGUI(player.getUniqueId());

            menuGUI.open();
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
     * Take the mapping of slots to {@link GUIButton}s in {@link #slotButtons} and add the {@link ItemStack}s to the GUI.
     * While this method returns a {@link CompletableFuture}, the actual code is executed asynchronously.
     * @return A {@link CompletableFuture} containing a {@link Boolean} where true was successful, otherwise false.
     */
    @Override
    public @NotNull CompletableFuture<Boolean> update() {
        Locale locale = localeManager.getLocale();

        // If the InventoryView was not created, log a warning and return false.
        if(inventoryView == null) {
            logger.warn(AdventureUtil.serialize("Unable to add GUIButton ItemStacks to the InventoryView as it was not created."));
            return CompletableFuture.completedFuture(false);
        }

        // Get the GUI size
        int guiSize = inventoryView.getTopInventory().getSize();

        // Clear the GUI of buttons
        clearButtons();

        // Check if at least 1 page is configured.
        List<ShopConfig.PageConfig> pages = shopConfig.gui().pages();
        if(pages.isEmpty()) {
            logger.error(AdventureUtil.serialize("Unable to decorate the shop GUI for file " + shopName + ".yml due to no pages configured."));
            return CompletableFuture.completedFuture(false);
        }

        // Get the page config
        ShopConfig.PageConfig page = pages.get(pageNum);

        // Check if at least 1 button is configured.
        List<ShopConfig.Button> entries  = page.buttons();
        if(entries.isEmpty()) {
            logger.error(AdventureUtil.serialize("Unable to decorate the shop GUI for page " + pageNum + " and file " + shopName + ".yml due to no buttons configured."));
            return CompletableFuture.completedFuture(false);
        }

        for(int buttonNum = 0; buttonNum < page.buttons().size(); buttonNum++) {
            ShopConfig.Button buttonConfig = page.buttons().get(buttonNum);
            ButtonType buttonType = buttonConfig.buttonType();

            // Check if the button type is null and send a warning if so, then skipping to the next button.
            if(buttonType == null) {
                logger.warn(AdventureUtil.serialize("Unable to add a button due to an invalid button type. Button Num: " + buttonNum));
                continue;
            }

            // Handle the creation of buttons by button type.
            switch(buttonType) {
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
                    // Check if the slot is not configured and send a warning.
                    if(buttonConfig.slot() == null) {
                        logger.warn(AdventureUtil.serialize("Unable to add a button due to a null slot. Button Num: " + buttonNum + " and type: " + buttonConfig.buttonType()));
                        continue;
                    }

                    // Only display the previous page button if the page number is greater than or equal to 1
                    if (pageNum >= 1) {
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
                    // Check if the slot is not configured and send a warning.
                    if(buttonConfig.slot() == null) {
                        logger.warn(AdventureUtil.serialize("Unable to add a button due to a null slot. Button Num: " + buttonNum + " and type: " + buttonConfig.buttonType()));
                        continue;
                    }

                    // Only display the next page button if another page is configured after the current
                    if(pageNum < (pages.size() - 1)) {
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
                        logger.warn(AdventureUtil.serialize("Unable to add a button due to a null slot. Button Num: " + buttonNum + " and type: " + buttonConfig.buttonType()));
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

                case TRANSACTION -> {
                    // Check if the slot is not configured and send a warning.
                    if(buttonConfig.slot() == null) {
                        logger.warn(AdventureUtil.serialize("Unable to add a button due to a null slot. Button Num: " + buttonNum + " and type: " + buttonConfig.buttonType()));
                        continue;
                    }

                    ShopConfig.TransactionData transactionData = buttonConfig.transactionData();
                    TransactionType transactionType = transactionData.transactionType();
                    if(transactionType == null) continue;

                    // Get the ItemStackConfig
                    ItemStackConfig itemConfig = buttonConfig.displayItem();

                    // Create price placeholders
                    Double buyPrice = transactionData.buyPrice();
                    Double sellPrice = transactionData.sellPrice();
                    List<TagResolver.Single> pricePlaceholders = new ArrayList<>();
                    if(buyPrice != null) pricePlaceholders.add(Placeholder.parsed("buy_price", String.valueOf(buyPrice)));
                    if(sellPrice != null) pricePlaceholders.add(Placeholder.parsed("sell_price", String.valueOf(sellPrice)));

                    // Create the ItemStackBuilder and pass the ItemStackConfig.
                    ItemStackBuilder itemStackBuilder = new ItemStackBuilder(logger);
                    itemStackBuilder.fromItemStackConfig(itemConfig, player, null, pricePlaceholders);

                    // If an ItemStack was created, create the GUIButton and add it to the GUI.
                    Optional<ItemStack> optionalItemStack = itemStackBuilder.buildItemStack();
                    optionalItemStack.ifPresent(itemStack -> {
                        GUIButton.Builder guiButtonBuilder = new GUIButton.Builder();
                        guiButtonBuilder.setItemStack(itemStack);
                        guiButtonBuilder.setAction(event -> {
                            // Get the transaction style name and check if it is null
                            String transactionStyle = transactionData.transactionStyle();
                            if(transactionStyle == null) {
                                logger.error(AdventureUtil.serialize("Unable to open transaction GUI for player " + player.getName() + " due to an invalid transaction style."));
                                player.sendMessage(AdventureUtil.serialize(locale.prefix() + locale.guiOpenError()));
                                if(isOpen) close();
                                return;
                            }

                            String transactionName = transactionData.transactionName();
                            if(transactionName == null) {
                                logger.error(AdventureUtil.serialize("Unable to open transaction GUI for player " + player.getName() + " due to an invalid transaction name."));
                                player.sendMessage(AdventureUtil.serialize(locale.prefix() + locale.guiOpenError()));
                                if(isOpen) close();
                                return;
                            }

                            // Get the TransactionConfig for the transaction style and check if it is valid
                            @NotNull Optional<TransactionConfig> optionalTransactionConfig = transactionManager.getTransactionConfig(transactionStyle);
                            if(optionalTransactionConfig.isEmpty()) {
                                logger.error(AdventureUtil.serialize("Unable to open transaction GUI for player " + player.getName() + " due to no transaction style config found for " + transactionStyle + "."));
                                player.sendMessage(AdventureUtil.serialize(locale.prefix() + locale.guiOpenError()));
                                if(isOpen) close();
                                return;
                            }
                            TransactionConfig transactionConfig = optionalTransactionConfig.get();

                            TransactionGUI transactionGUI = new TransactionGUI(
                                    skyShop,
                                    guiManager,
                                    player,
                                    localeManager,
                                    sellAllManager,
                                    statsManager,
                                    skyShopAPI,
                                    this,
                                    transactionType,
                                    transactionStyle,
                                    transactionConfig,
                                    transactionData.displayItem(),
                                    transactionData.transactionItem(),
                                    buyPrice,
                                    sellPrice,
                                    transactionName,
                                    transactionData.buyCommands(),
                                    transactionData.sellCommands());

                            boolean creationResult = transactionGUI.create();
                            if(!creationResult) {
                                logger.error(AdventureUtil.serialize("Unable to create the InventoryView for the transaction GUI for player " + player.getName() + " due to a configuration error."));
                                player.sendMessage(AdventureUtil.serialize(locale.prefix() + locale.guiOpenError()));
                                if(isOpen) close();
                                return;
                            }

                            // This method is completed sync, the api returns a CompletableFuture for supporting plugins with async requirements.
                            @NotNull CompletableFuture<Boolean> updateFuture = transactionGUI.update();
                            try {
                                boolean updateResult = updateFuture.get();

                                if(!updateResult) {
                                    logger.error(AdventureUtil.serialize("Unable to decorate the transaction GUI for player " + player.getName() + " due to a configuration error."));
                                    player.sendMessage(AdventureUtil.serialize(locale.prefix() + locale.guiOpenError()));
                                    if(isOpen) close();
                                    return;
                                }
                            } catch (InterruptedException | ExecutionException e) {
                                logger.error(AdventureUtil.serialize("Unable to decorate the transaction GUI for player " + player.getName() + " due to a configuration error. " + e.getMessage()));
                                player.sendMessage(AdventureUtil.serialize(locale.prefix() + locale.guiOpenError()));
                                if(isOpen) close();
                                return;
                            }

                            boolean openResult = transactionGUI.open();
                            if(!openResult) {
                                logger.error(AdventureUtil.serialize("Unable to open the transaction GUI for player " + player.getName() + " due to a configuration error."));
                                player.sendMessage(AdventureUtil.serialize(locale.prefix() + locale.guiOpenError()));
                                if(isOpen) close();
                            }
                        });

                        setButton(buttonConfig.slot(), guiButtonBuilder.build());
                    });
                }

                default -> logger.error(AdventureUtil.serialize("Unsupported ButtonType for " + buttonNum + " on page " + pageNum + " and file " + shopName + ".yml due to no buttons configured."));
            }
        }

        return super.update();
    }

    @Override
    public void handleClose(@NotNull InventoryCloseEvent inventoryCloseEvent) {
        if(inventoryCloseEvent.getReason().equals(InventoryCloseEvent.Reason.UNLOADED) || inventoryCloseEvent.getReason().equals(InventoryCloseEvent.Reason.OPEN_NEW)) return;

        guiManager.removeOpenGUI(uuid);

        menuGUI.open();
    }

    @Override
    public void handleBottomDrag(@NotNull InventoryDragEvent inventoryDragEvent) {}

    @Override
    public void handleGlobalDrag(@NotNull InventoryDragEvent inventoryDragEvent) {}

    @Override
    public void handleBottomClick(@NotNull InventoryClickEvent inventoryClickEvent) {}

    @Override
    public void handleGlobalClick(@NotNull InventoryClickEvent inventoryClickEvent) {}
}
