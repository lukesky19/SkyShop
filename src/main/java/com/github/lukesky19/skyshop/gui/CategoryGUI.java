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
import com.github.lukesky19.skyshop.SkyShopAPI;
import com.github.lukesky19.skyshop.config.gui.CategoryConfig;
import com.github.lukesky19.skyshop.config.gui.TransactionConfig;
import com.github.lukesky19.skyshop.config.locale.Locale;
import com.github.lukesky19.skyshop.manager.HookManager;
import com.github.lukesky19.skyshop.manager.StatsManager;
import com.github.lukesky19.skyshop.manager.TransactionManager;
import com.github.lukesky19.skyshop.manager.config.CategoryConfigManager;
import com.github.lukesky19.skyshop.manager.config.LocaleManager;
import com.github.lukesky19.skyshop.manager.config.SellAllManager;
import com.github.lukesky19.skyshop.manager.config.TransactionConfigManager;
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

import java.util.*;

/**
 * This class is called to create a gui for a player to access a shop category.
 * This could be a navigation category, a shop category, or both mixed together.
 */
public class CategoryGUI extends ChestGUI<UUID> {
    private final @NotNull SkyShop skyShop;
    private final @NotNull LocaleManager localeManager;
    private final @NotNull CategoryConfigManager categoryConfigManager;
    private final @NotNull TransactionConfigManager transactionConfigManager;
    private final @NotNull TransactionManager transactionManager;
    private final @NotNull SellAllManager sellAllManager;
    private final @Nullable StatsManager statsManager;
    private final @NotNull HookManager hookManager;
    private final @NotNull SkyShopAPI skyShopAPI;

    private int pageNum = 0;
    private boolean isOpen = false;
    private final @Nullable CategoryGUI previousGUI;
    private final @NotNull String shopName;
    private final @NotNull CategoryConfig categoryConfig;

    /**
     * Constructor
     *
     * @param skyShop A {@link SkyShop} instance.
     * @param guiManager An {@link IGUIManager} instance.
     * @param player The {@link Player} viewing the GUI/Inventory.
     * @param localeManager A {@link LocaleManager} instance.
     * @param categoryConfigManager A {@link CategoryConfigManager} instance.
     * @param transactionConfigManager A {@link TransactionConfigManager} instance.
     * @param transactionManager A {@link TransactionManager} instance.
     * @param sellAllManager A {@link SellAllManager} instance.
     * @param statsManager A {@link StatsManager} instance.
     * @param hookManager A {@link HookManager} instance.
     * @param skyShopAPI A {@link SkyShopAPI} instance.
     * @param previousGUI The {@link CategoryGUI} the player opened this GUI/Inventory from.
     * @param categoryConfig The {@link CategoryConfig} associated with the GUI/Inventory being created.
     * @param shopName The name of the shop for this GUI.
     */
    public CategoryGUI(
            @NotNull SkyShop skyShop,
            @NotNull IGUIManager<UUID> guiManager,
            @NotNull Player player,
            @NotNull LocaleManager localeManager,
            @NotNull CategoryConfigManager categoryConfigManager,
            @NotNull TransactionConfigManager transactionConfigManager,
            @NotNull TransactionManager transactionManager,
            @NotNull SellAllManager sellAllManager,
            @Nullable StatsManager statsManager,
            @NotNull HookManager hookManager,
            @NotNull SkyShopAPI skyShopAPI,
            @Nullable CategoryGUI previousGUI,
            @NotNull CategoryConfig categoryConfig,
            @NotNull String shopName) {
        super(skyShop, guiManager, player.getUniqueId(), player);

        this.skyShop = skyShop;
        this.localeManager = localeManager;
        this.categoryConfigManager = categoryConfigManager;
        this.transactionConfigManager = transactionConfigManager;
        this.transactionManager = transactionManager;
        this.sellAllManager = sellAllManager;
        this.statsManager = statsManager;
        this.hookManager = hookManager;
        this.skyShopAPI = skyShopAPI;
        this.previousGUI = previousGUI;
        this.categoryConfig = categoryConfig;
        this.shopName = shopName;
    }

    /**
     * Create the {@link InventoryView} for this GUI.
     * @return true if created successfully, otherwise false.
     */
    public boolean create() {
        GUIType guiType = categoryConfig.gui().guiType();
        if(guiType == null) {
            logger.warn(AdventureUtil.deserialize("Unable to create the InventoryView for a ShopGUI due to an invalid GUIType"));
            return false;
        }

        String guiName = Objects.requireNonNullElse(categoryConfig.gui().name(), "");

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
     * Close the current inventory/gui with an OPEN_NEW reason and open the {@link #previousGUI}.
     */
    @Override
    public void close() {
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            isOpen = false;

            player.closeInventory(InventoryCloseEvent.Reason.OPEN_NEW);

            guiManager.removeOpenGUI(player.getUniqueId());

            if(previousGUI != null) previousGUI.open();
        }, 1L);
    }

    /**
     * Close the current inventory/gui with an UNLOADED reason without opening any previous GUIs.
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
     * @return true if successful, otherwise false.
     */
    @Override
    public boolean update() {
        Locale locale = localeManager.getLocale();

        // If the InventoryView was not created, log a warning and return false.
        if(inventoryView == null) {
            logger.warn(AdventureUtil.deserialize("Unable to add GUIButton ItemStacks to the InventoryView as it was not created."));
            return false;
        }

        // Get the GUI size
        int guiSize = inventoryView.getTopInventory().getSize();

        // Clear the GUI of buttons
        clearButtons();

        // Check if at least 1 page is configured.
        List<CategoryConfig.PageConfig> pages = categoryConfig.gui().pages();
        if(pages.isEmpty()) {
            logger.error(AdventureUtil.deserialize("Unable to decorate the shop GUI for file " + shopName + ".yml due to no pages configured."));
            return false;
        }

        // Get the page config
        CategoryConfig.PageConfig page = pages.get(pageNum);

        // Check if at least 1 button is configured.
        List<CategoryConfig.ButtonConfig> entries  = page.buttons();
        if(entries.isEmpty()) {
            logger.error(AdventureUtil.deserialize("Unable to decorate the shop GUI for page " + pageNum + " and file " + shopName + ".yml due to no buttons configured."));
            return false;
        }

        for(int buttonNum = 0; buttonNum < page.buttons().size(); buttonNum++) {
            CategoryConfig.ButtonConfig buttonConfig = page.buttons().get(buttonNum);
            ButtonType buttonType = buttonConfig.buttonType();

            // Check if the button type is null and send a warning if so, then skipping to the next button.
            if(buttonType == null) {
                logger.warn(AdventureUtil.deserialize("Unable to add a button due to an invalid button type. Button Num: " + buttonNum));
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
                        logger.warn(AdventureUtil.deserialize("Unable to add a button due to a null slot. Button Num: " + buttonNum + " and type: " + buttonConfig.buttonType()));
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
                                if(buttonConfig.permission() != null) {
                                    if(!player.hasPermission(buttonConfig.permission())) {
                                        player.sendMessage(AdventureUtil.deserialize(locale.prefix() + locale.buttonNoPermission()));
                                        return;
                                    }
                                }

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
                        logger.warn(AdventureUtil.deserialize("Unable to add a button due to a null slot. Button Num: " + buttonNum + " and type: " + buttonConfig.buttonType()));
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
                                if(buttonConfig.permission() != null) {
                                    if(!player.hasPermission(buttonConfig.permission())) {
                                        player.sendMessage(AdventureUtil.deserialize(locale.prefix() + locale.buttonNoPermission()));
                                        return;
                                    }
                                }

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
                        logger.warn(AdventureUtil.deserialize("Unable to add a button due to a null slot. Button Num: " + buttonNum + " and type: " + buttonConfig.buttonType()));
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
                            if(buttonConfig.permission() != null) {
                                if(!player.hasPermission(buttonConfig.permission())) {
                                    player.sendMessage(AdventureUtil.deserialize(locale.prefix() + locale.buttonNoPermission()));
                                    return;
                                }
                            }

                            close();
                        });

                        setButton(buttonConfig.slot(), guiButtonBuilder.build());
                    });
                }

                case TRANSACTION -> {
                    // Check if the slot is not configured and send a warning.
                    if(buttonConfig.slot() == null) {
                        logger.warn(AdventureUtil.deserialize("Unable to add a button due to a null slot. Button Num: " + buttonNum + " and type: " + buttonConfig.buttonType()));
                        continue;
                    }

                    CategoryConfig.TransactionData transactionData = buttonConfig.transactionData();
                    if(transactionData == null) continue;
                    TransactionType transactionType = transactionData.transactionType();
                    if(transactionType == null) continue;

                    // Create the ItemStackBuilder and pass the ItemStackConfig.
                    ItemStackBuilder itemStackBuilder = new ItemStackBuilder(logger);
                    itemStackBuilder.fromItemStackConfig(buttonConfig.displayItem(), player, null, getPricePlaceholders(transactionData.prices()));

                    // If an ItemStack was created, create the GUIButton and add it to the GUI.
                    Optional<ItemStack> optionalItemStack = itemStackBuilder.buildItemStack();
                    optionalItemStack.ifPresent(itemStack -> {
                        GUIButton.Builder guiButtonBuilder = new GUIButton.Builder();
                        guiButtonBuilder.setItemStack(itemStack);
                        guiButtonBuilder.setAction(event -> {
                            if(buttonConfig.permission() != null) {
                                if(!player.hasPermission(buttonConfig.permission())) {
                                    player.sendMessage(AdventureUtil.deserialize(locale.prefix() + locale.buttonNoPermission()));
                                    return;
                                }
                            }

                            // Get the transaction style name and check if it is null
                            String transactionStyle = transactionData.transactionStyle();
                            if(transactionStyle == null) {
                                logger.error(AdventureUtil.deserialize("Unable to open transaction GUI for player " + player.getName() + " due to an invalid transaction style."));
                                player.sendMessage(AdventureUtil.deserialize(locale.prefix() + locale.guiOpenError()));
                                if(isOpen) close();
                                return;
                            }

                            String transactionName = transactionData.transactionName();
                            if(transactionName == null) {
                                logger.error(AdventureUtil.deserialize("Unable to open transaction GUI for player " + player.getName() + " due to an invalid transaction name."));
                                player.sendMessage(AdventureUtil.deserialize(locale.prefix() + locale.guiOpenError()));
                                if(isOpen) close();
                                return;
                            }

                            // Get the TransactionConfig for the transaction style and check if it is valid
                            @NotNull Optional<TransactionConfig> optionalTransactionConfig = transactionConfigManager.getTransactionConfig(transactionStyle);
                            if(optionalTransactionConfig.isEmpty()) {
                                logger.error(AdventureUtil.deserialize("Unable to open transaction GUI for player " + player.getName() + " due to no transaction style config found for " + transactionStyle + "."));
                                player.sendMessage(AdventureUtil.deserialize(locale.prefix() + locale.guiOpenError()));
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
                                    transactionManager,
                                    skyShopAPI,
                                    this,
                                    transactionType,
                                    transactionStyle,
                                    transactionConfig,
                                    transactionData.displayItem(),
                                    transactionData.transactionItem(),
                                    transactionData.prices(),
                                    transactionName,
                                    transactionData.buyCommands(),
                                    transactionData.sellCommands(),
                                    transactionData.islandSize());

                            boolean creationResult = transactionGUI.create();
                            if(!creationResult) {
                                logger.error(AdventureUtil.deserialize("Unable to create the InventoryView for the transaction GUI for player " + player.getName() + " due to a configuration error."));
                                player.sendMessage(AdventureUtil.deserialize(locale.prefix() + locale.guiOpenError()));
                                if(isOpen) close();
                                return;
                            }

                            boolean updateFuture = transactionGUI.update();
                            if(!updateFuture) {
                                logger.error(AdventureUtil.deserialize("Unable to decorate the transaction GUI for player " + player.getName() + " due to a configuration error."));
                                player.sendMessage(AdventureUtil.deserialize(locale.prefix() + locale.guiOpenError()));
                                if(isOpen) close();
                                return;
                            }

                            boolean openResult = transactionGUI.open();
                            if(!openResult) {
                                logger.error(AdventureUtil.deserialize("Unable to open the transaction GUI for player " + player.getName() + " due to a configuration error."));
                                player.sendMessage(AdventureUtil.deserialize(locale.prefix() + locale.guiOpenError()));
                                if(isOpen) close();
                            }
                        });

                        setButton(buttonConfig.slot(), guiButtonBuilder.build());
                    });
                }

                case OPEN_SHOP -> {
                    // Check if the slot is not configured and send a warning.
                    if(buttonConfig.slot() == null) {
                        logger.warn(AdventureUtil.deserialize("Unable to add a button due to a null slot. Button Num: " + buttonNum + " and type: " + buttonConfig.buttonType()));
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
                            if(buttonConfig.permission() != null) {
                                if(!player.hasPermission(buttonConfig.permission())) {
                                    player.sendMessage(AdventureUtil.deserialize(locale.prefix() + locale.buttonNoPermission()));
                                    return;
                                }
                            }

                            String shopName = buttonConfig.shopName();
                            if(shopName == null) {
                                logger.error(AdventureUtil.deserialize("Unable to open shop GUI for player " + player.getName() + " due to no configured shop name."));
                                return;
                            }

                            @NotNull Optional<CategoryConfig> optionalCategoryConfig = categoryConfigManager.getCategoryConfig(shopName);
                            if(optionalCategoryConfig.isEmpty()) {
                                logger.error(AdventureUtil.deserialize("Unable to open shop GUI " + shopName + " for player " + player.getName() + " due to no configuration found for shop name " + shopName + "."));
                                player.sendMessage(AdventureUtil.deserialize(locale.prefix() + locale.guiOpenError()));
                                if(isOpen) close();
                                return;
                            }

                            CategoryConfig categoryConfig = optionalCategoryConfig.get();
                            if(categoryConfig.permission() != null) {
                                if(!player.hasPermission(categoryConfig.permission())) {
                                    player.sendMessage(AdventureUtil.deserialize(locale.prefix() + locale.categoryNoPermission()));
                                    return;
                                }
                            }

                            CategoryGUI categoryGUI = new CategoryGUI(skyShop, guiManager, player, localeManager, categoryConfigManager, transactionConfigManager, transactionManager, sellAllManager, statsManager, hookManager, skyShopAPI, this, categoryConfig, shopName);

                            boolean creationResult = categoryGUI.create();
                            if(!creationResult) {
                                logger.error(AdventureUtil.deserialize("Unable to create the InventoryView for the GUI " + shopName + " for player " + player.getName() + " due to a configuration error."));
                                player.sendMessage(AdventureUtil.deserialize(locale.prefix() + locale.guiOpenError()));
                                if(isOpen) close();
                                return;
                            }

                            boolean updateResult = categoryGUI.update();
                            if(!updateResult) {
                                logger.error(AdventureUtil.deserialize("Unable to decorate the GUI " + shopName + " for player " + player.getName() + " due to a configuration error."));
                                player.sendMessage(AdventureUtil.deserialize(locale.prefix() + locale.guiOpenError()));
                                if(isOpen) close();
                                return;
                            }

                            boolean openResult = categoryGUI.open();
                            if(!openResult) {
                                logger.error(AdventureUtil.deserialize("Unable to open the GUI " + shopName + " for player " + player.getName() + " due to a configuration error."));
                                player.sendMessage(AdventureUtil.deserialize(locale.prefix() + locale.guiOpenError()));
                                if(isOpen) close();
                            }
                        });

                        setButton(buttonConfig.slot(), guiButtonBuilder.build());
                    });
                }

                case DUMMY -> {
                    // Check if the slot is not configured and send a warning.
                    if(buttonConfig.slot() == null) {
                        logger.warn(AdventureUtil.deserialize("Unable to add a button due to a null slot. Button Num: " + buttonNum + " and type: " + buttonConfig.buttonType()));
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

                        setButton(buttonConfig.slot(), guiButtonBuilder.build());
                    });
                }

                default -> logger.error(AdventureUtil.deserialize("Unsupported ButtonType for " + buttonNum + " on page " + pageNum + " and file " + shopName + ".yml due to no buttons configured."));
            }
        }

        return super.update();
    }

    @Override
    public void handleClose(@NotNull InventoryCloseEvent inventoryCloseEvent) {
        if(inventoryCloseEvent.getReason().equals(InventoryCloseEvent.Reason.UNLOADED) || inventoryCloseEvent.getReason().equals(InventoryCloseEvent.Reason.OPEN_NEW)) return;

        guiManager.removeOpenGUI(uuid);

        if(previousGUI != null) previousGUI.open();
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
     * Get the placeholder list to use for displaying prices.
     * @param priceConfig The {@link CategoryConfig.PriceConfig}.
     * @return A {@link List} of {@link TagResolver.Single}
     */
    private @NotNull List<TagResolver.Single> getPricePlaceholders(@NotNull CategoryConfig.PriceConfig priceConfig) {
        List<TagResolver.Single> placeholderList = new ArrayList<>();

        double buyPrice = priceConfig.buyPrice();
        double sellPrice = priceConfig.sellPrice();
        int buyPoints = priceConfig.buyPoints();
        int sellPoints = priceConfig.sellPoints();

        placeholderList.add(Placeholder.parsed("buy_price", String.valueOf(buyPrice)));
        placeholderList.add(Placeholder.parsed("sell_price", String.valueOf(sellPrice)));
        placeholderList.add(Placeholder.parsed("buy_points", String.valueOf(buyPoints)));
        placeholderList.add(Placeholder.parsed("sell_points", String.valueOf(sellPoints)));

        return placeholderList;
    }
}
