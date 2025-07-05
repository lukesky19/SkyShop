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
import com.github.lukesky19.skylib.api.gui.GUIButton;
import com.github.lukesky19.skylib.api.gui.GUIType;
import com.github.lukesky19.skylib.api.gui.abstracts.ChestGUI;
import com.github.lukesky19.skylib.api.itemstack.ItemStackBuilder;
import com.github.lukesky19.skylib.api.itemstack.ItemStackConfig;
import com.github.lukesky19.skyshop.SkyShop;
import com.github.lukesky19.skyshop.SkyShopAPI;
import com.github.lukesky19.skyshop.configuration.LocaleManager;
import com.github.lukesky19.skyshop.configuration.SellAllManager;
import com.github.lukesky19.skyshop.configuration.ShopManager;
import com.github.lukesky19.skyshop.configuration.TransactionManager;
import com.github.lukesky19.skyshop.data.Locale;
import com.github.lukesky19.skyshop.data.gui.MenuConfig;
import com.github.lukesky19.skyshop.data.gui.SellAllConfig;
import com.github.lukesky19.skyshop.data.gui.ShopConfig;
import com.github.lukesky19.skyshop.manager.StatsManager;
import com.github.lukesky19.skyshop.util.ButtonType;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * This class creates the GUI to access different shop categories.
*/
public class MenuGUI extends ChestGUI {
    private final @NotNull SkyShop skyShop;
    private final @NotNull LocaleManager localeManager;
    private final @NotNull ShopManager shopManager;
    private final @NotNull TransactionManager transactionManager;
    private final @NotNull SellAllManager sellAllManager;
    private final @Nullable StatsManager statsManager;
    private final @NotNull SkyShopAPI skyShopAPI;

    private final @NotNull MenuConfig menuConfig;
    private int pageNum = 0;
    private boolean isOpen = false;

    /**
     * Constructor
     * @param skyShop A {@link SkyShop} instance.
     * @param guiManager A {@link GUIManager} instance.
     * @param player The {@link Player} viewing the GUI/Inventory.
     * @param localeManager A {@link LocaleManager} instance.
     * @param shopManager A {@link ShopManager} instance.
     * @param transactionManager A {@link TransactionManager} instance.
     * @param sellAllManager A {@link SellAllManager} instance.
     * @param statsManager A {@link StatsManager} instance.
     * @param skyShopAPI A {@link SkyShopAPI} instance.
     * @param menuConfig The {@link MenuConfig} to create this GUI with.
     */
    public MenuGUI(
            @NotNull SkyShop skyShop,
            @NotNull GUIManager guiManager,
            @NotNull Player player,
            @NotNull LocaleManager localeManager,
            @NotNull ShopManager shopManager,
            @NotNull TransactionManager transactionManager,
            @NotNull SellAllManager sellAllManager,
            @Nullable StatsManager statsManager,
            @NotNull SkyShopAPI skyShopAPI,
            @NotNull MenuConfig menuConfig) {
        super(skyShop, guiManager, player);

        this.skyShop = skyShop;
        this.localeManager = localeManager;
        this.shopManager = shopManager;
        this.transactionManager = transactionManager;
        this.sellAllManager = sellAllManager;
        this.statsManager = statsManager;
        this.skyShopAPI = skyShopAPI;
        this.menuConfig = menuConfig;
    }

    /**
     * Create the {@link InventoryView} for this GUI.
     * @return true if created successfully, otherwise false.
     */
    public boolean create() {
        GUIType guiType = menuConfig.gui().guiType();
        if(guiType == null) {
            logger.warn(AdventureUtil.serialize("Unable to create the InventoryView for the MenuGUI due to an invalid GUIType"));
            return false;
        }

        String guiName = Objects.requireNonNullElse(menuConfig.gui().name(), "");

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
     * Set the isOpen boolean to false and run the super method.
     */
    @Override
    public void close() {
        isOpen = false;

        super.close();
    }

    /**
     * Set the isOpen boolean to false and run the super method.
     * @param onDisable Is the plugin being disabled?
     */
    @Override
    public void unload(boolean onDisable) {
        isOpen = false;

        super.unload(onDisable);
    }

    /**
     * A method to create all the buttons in the inventory GUI.
     * @return true if successful, otherwise false.
     */
    @Override
    public boolean update() {
        Locale locale = localeManager.getLocale();

        // If the InventoryView was not created, log a warning and return false.
        if(inventoryView == null) {
            logger.warn(AdventureUtil.serialize("Unable to add GUIButton ItemStacks to the InventoryView as it was not created."));
            return false;
        }

        // Get the GUI size
        int guiSize = inventoryView.getTopInventory().getSize();

        // Clear the GUI of buttons
        clearButtons();

        // Check if at least 1 page is configured.
        List<MenuConfig.PageConfig> pages = menuConfig.gui().pages();
        if(pages.isEmpty()) {
            logger.error(AdventureUtil.serialize("Unable to decorate the menu GUI due to no pages configured."));
            return false;
        }

        // Get the page config
        MenuConfig.PageConfig page = pages.get(pageNum);

        // Check if at least 1 button is configured.
        List<MenuConfig.Button> entries  = page.buttons();
        if(entries.isEmpty()) {
            logger.error(AdventureUtil.serialize("Unable to decorate the menu GUI for page " + pageNum + " due to no buttons configured."));
            return false;
        }

        // Loop through buttons to populate the GUI
        for(int buttonNum = 0; buttonNum < page.buttons().size(); buttonNum++) {
            MenuConfig.Button buttonConfig = page.buttons().get(buttonNum);
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

                case OPEN_SHOP -> {
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
                        guiButtonBuilder.setAction(event -> {
                            String shopName = buttonConfig.shopName();
                            if(shopName == null) {
                                logger.error(AdventureUtil.serialize("Unable to open shop GUI for player " + player.getName() + " due to no configured shop name."));
                                return;
                            }

                            @NotNull Optional<ShopConfig> optionalShopConfig = shopManager.getShopConfig(shopName);
                            if(optionalShopConfig.isEmpty()) {
                                logger.error(AdventureUtil.serialize("Unable to open shop GUI " + shopName + " for player " + player.getName() + " due to no configuration found for shop name " + shopName + "."));
                                player.sendMessage(AdventureUtil.serialize(locale.prefix() + locale.guiOpenError()));
                                if(isOpen) close();
                                return;
                            }

                            ShopConfig shopConfig = optionalShopConfig.get();
                            ShopGUI shopGUI = new ShopGUI(skyShop, guiManager, player, localeManager, transactionManager, sellAllManager, statsManager, skyShopAPI,  this, shopConfig, shopName);

                            boolean creationResult = shopGUI.create();
                            if(!creationResult) {
                                logger.error(AdventureUtil.serialize("Unable to create the InventoryView for the shop GUI " + shopName + " for player " + player.getName() + " due to a configuration error."));
                                player.sendMessage(AdventureUtil.serialize(locale.prefix() + locale.guiOpenError()));
                                if(isOpen) close();
                                return;
                            }

                            boolean updateResult = shopGUI.update();
                            if(!updateResult) {
                                logger.error(AdventureUtil.serialize("Unable to decorate the shop GUI " + shopName + " for player " + player.getName() + " due to a configuration error."));
                                player.sendMessage(AdventureUtil.serialize(locale.prefix() + locale.guiOpenError()));
                                if(isOpen) close();
                                return;
                            }

                            boolean openResult = shopGUI.open();
                            if(!openResult) {
                                logger.error(AdventureUtil.serialize("Unable to open the shop GUI " + shopName + " for player " + player.getName() + " due to a configuration error."));
                                player.sendMessage(AdventureUtil.serialize(locale.prefix() + locale.guiOpenError()));
                                if(isOpen) close();
                            }
                        });

                        setButton(buttonConfig.slot(), guiButtonBuilder.build());
                    });
                }

                case SELL_GUI -> {
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
                        guiButtonBuilder.setAction(event -> {
                            @NotNull Optional<@NotNull SellAllConfig> optionalSellAllConfig = sellAllManager.getSellAllGuiConfig();
                            if(optionalSellAllConfig.isEmpty()) {
                                logger.error(AdventureUtil.serialize("Unable to open sellall GUI for player " + player.getName() + " due to invalid sellall config."));
                                player.sendMessage(AdventureUtil.serialize(locale.prefix() + locale.guiOpenError()));
                                if(isOpen) close();
                                return;
                            }

                            SellAllConfig sellAllConfig = optionalSellAllConfig.get();
                            SellAllGUI sellAllGUI = new SellAllGUI(skyShop, guiManager, sellAllConfig, skyShopAPI, player);

                            boolean creationResult = sellAllGUI.create();
                            if(!creationResult) {
                                logger.error(AdventureUtil.serialize("Unable to create the InventoryView for the sellall GUI for player " + player.getName() + " due to a configuration error."));
                                player.sendMessage(AdventureUtil.serialize(locale.prefix() + locale.guiOpenError()));
                                if(isOpen) close();
                                return;
                            }

                            boolean updateResult = sellAllGUI.update();
                            if(!updateResult) {
                                logger.error(AdventureUtil.serialize("Unable to decorate the sellall GUI for player " + player.getName() + " due to a configuration error."));
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

                default -> logger.warn(AdventureUtil.serialize("Unsupported ButtonType in the menu GUI for " + buttonNum + " on page " + pageNum + "."));
            }
        }

        return super.update();
    }

    @Override
    public void handleClose(@NotNull InventoryCloseEvent inventoryCloseEvent) {
        if(inventoryCloseEvent.getReason().equals(InventoryCloseEvent.Reason.UNLOADED) || inventoryCloseEvent.getReason().equals(InventoryCloseEvent.Reason.OPEN_NEW)) return;

        guiManager.removeOpenGUI(uuid);
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