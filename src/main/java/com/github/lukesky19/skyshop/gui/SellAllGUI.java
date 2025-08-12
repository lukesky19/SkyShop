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
import com.github.lukesky19.skyshop.data.gui.SellAllConfig;
import com.github.lukesky19.skyshop.util.ButtonType;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * This class is called to create a sellall gui for a player to sell items.
*/
public class SellAllGUI extends ChestGUI {
    private final @NotNull AbstractGUIManager guiManager;
    private final @NotNull SkyShopAPI skyShopAPI;
    private final @NotNull SellAllConfig sellAllConfig;

    /**
     * Constructor
     * @param skyShop A {@link SkyShop} instance.
     * @param guiManager A {@link AbstractGUIManager} instance.
     * @param sellAllConfig The {@link SellAllConfig} to create the GUI with.
     * @param skyShopAPI A {@link SkyShopAPI} instance.
     * @param player The {@link Player} who opened the GUI.
     */
    public SellAllGUI(
            @NotNull SkyShop skyShop,
            @NotNull AbstractGUIManager guiManager,
            @NotNull SellAllConfig sellAllConfig,
            @NotNull SkyShopAPI skyShopAPI,
            @NotNull Player player) {
        super(skyShop, guiManager, player);

        this.guiManager = guiManager;
        this.skyShopAPI = skyShopAPI;
        this.sellAllConfig = sellAllConfig;
    }

    /**
     * Create the {@link InventoryView} for this GUI.
     * @return true if created successfully, otherwise false.
     */
    public boolean create() {
        GUIType guiType = sellAllConfig.gui().guiType();
        if(guiType == null) {
            logger.warn(AdventureUtil.serialize("Unable to create the InventoryView for a ShopGUI due to an invalid GUIType"));
            return false;
        }

        String guiName = Objects.requireNonNullElse(sellAllConfig.gui().name(), "");

        return create(guiType, guiName, List.of());
    }

    /**
     * A method to create all the buttons in the inventory GUI.
     */
    @Override
    public boolean update() {
        // If the InventoryView was not created, log a warning and return false.
        if (inventoryView == null) {
            logger.warn(AdventureUtil.serialize("Unable to add GUIButton ItemStacks to the InventoryView as it was not created."));
            return false;
        }

        // Clear the GUI of buttons
        clearButtons();

        List<SellAllConfig.Button> buttonList = sellAllConfig.gui().buttons();
        for(int buttonNum = 0; buttonNum < buttonList.size(); buttonNum++) {
            SellAllConfig.Button buttonConfig = buttonList.get(buttonNum);
            ButtonType buttonType = buttonConfig.buttonType();

            // Check if the button type is null and send a warning if so, then skipping to the next button.
            if(buttonType == null) {
                logger.warn(AdventureUtil.serialize("Unable to add a button due to an invalid button type. Button Num: " + buttonNum));
                continue;
            }

            // Check if the slot is not configured and send a warning.
            if(buttonConfig.slot() == null) {
                logger.warn(AdventureUtil.serialize("Unable to add a button due to a null slot. Button Num: " + buttonNum + " and type: " + buttonType));
                continue;
            }

            switch(buttonType) {
                case RETURN -> {
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

                case DUMMY -> {
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

                default -> logger.warn(AdventureUtil.serialize("Unsupported ButtonType in the sellall GUI for " + buttonNum + " and button type " + buttonType + "."));
            }
        }

        return super.update();
    }

    @Override
    public void close() {
        // Close the GUI
        super.close();

        // The InventoryView cannot be null as the GUI can only be opened and hence closed when it is not null.
        assert inventoryView != null;

        // Sell the items in the GUI.
        sellItemsInGUI(inventoryView.getTopInventory());
    }

    @Override
    public void unload(boolean onDisable) {
        // Close the GUI
        super.unload(onDisable);

        // The InventoryView cannot be null as the GUI can only be opened and hence closed when it is not null.
        assert inventoryView != null;

        // Sell the items in the GUI.
        sellItemsInGUI(inventoryView.getTopInventory());
    }

    /**
     * Handles the closing of the inventory GUI.
     * Also handles selling of items and returning any un-sellable items.
     * @param inventoryCloseEvent The InventoryCloseEvent
     */
    @Override
    public void handleClose(@NotNull InventoryCloseEvent inventoryCloseEvent) {
        if(inventoryCloseEvent.getReason().equals(InventoryCloseEvent.Reason.UNLOADED)) return;

        // Stop tracking the GUI as being open
        guiManager.removeOpenGUI(uuid);

        // The InventoryView cannot be null as the GUI can only be opened and hence closed when it is not null.
        assert inventoryView != null;

        // Sell the items in the GUI.
        sellItemsInGUI(inventoryView.getTopInventory());
    }

    @Override
    public void handleTopDrag(@NotNull InventoryDragEvent inventoryDragEvent) {}

    @Override
    public void handleBottomDrag(@NotNull InventoryDragEvent inventoryDragEvent) {}

    @Override
    public void handleGlobalDrag(@NotNull InventoryDragEvent inventoryDragEvent) {}

    @Override
    public void handleTopClick(@NotNull InventoryClickEvent event) {
        int slot = event.getSlot();
        GUIButton button = slotButtons.get(slot);
        if(button != null) {
            event.setCancelled(true);

            button.action().accept(event);
        }
    }

    @Override
    public void handleBottomClick(@NotNull InventoryClickEvent inventoryClickEvent) {}

    @Override
    public void handleGlobalClick(@NotNull InventoryClickEvent inventoryClickEvent) {}

    private void sellItemsInGUI(@NotNull Inventory inventory) {
        // Remove any buttons so that they aren't sold or given to the player.
        clearButtons();

        // Proceed to sell any items in the inventory
        skyShopAPI.sellInventoryGUI(inventory, player, true);
    }
}

