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
import com.github.lukesky19.skylib.api.format.FormatUtil;
import com.github.lukesky19.skylib.api.gui.GUIButton;
import com.github.lukesky19.skylib.api.gui.abstracts.ChestGUI;
import com.github.lukesky19.skylib.api.itemstack.ItemStackBuilder;
import com.github.lukesky19.skyshop.SkyShop;
import com.github.lukesky19.skyshop.data.TransactionStats;
import com.github.lukesky19.skyshop.manager.StatsManager;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ItemType;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * This class is used to view the stats for how often an item has been purchased or sold.
 */
public class StatsGUI extends ChestGUI {
    private final @NotNull SkyShop skyShop;
    private final @NotNull GUIManager guiManager;
    private final @NotNull StatsManager statsManager;
    private int pageNum = 0;

    /**
     * Constructor
     * @param skyShop A {@link SkyShop} instance.
     * @param guiManager A {@link GUIManager} instance.
     * @param statsManager A {@link StatsManager} instance.
     * @param player A {@link Player} the GUI is for.
     */
    public StatsGUI(@NotNull SkyShop skyShop, @NotNull GUIManager guiManager, @NotNull StatsManager statsManager, @NotNull Player player) {
        super(skyShop, guiManager, player);

        this.skyShop = skyShop;
        this.guiManager = guiManager;
        this.statsManager = statsManager;
    }

    /**
     * Add the buttons to the GUI.
     * While this method returns a {@link CompletableFuture}, this method executes synchronously.
     * @return A {@link CompletableFuture} containing a {@link Boolean}. true if successful, otherwise false.
     */
    @Override
    public @NotNull CompletableFuture<Boolean> update() {
        Map<ItemType, TransactionStats> statsMap = statsManager.getStatsMap();

        createFillerButtons();
        createReturnButton();
        createNextPageButton(statsMap);
        createPrevPageButton();
        createStatsButtons(statsMap);

        return super.update();
    }

    /**
     * Handles when the GUI is closed.
     * @param inventoryCloseEvent An {@link InventoryCloseEvent}
     */
    @Override
    public void handleClose(@NotNull InventoryCloseEvent inventoryCloseEvent) {
        if(inventoryCloseEvent.getReason().equals(InventoryCloseEvent.Reason.UNLOADED) || inventoryCloseEvent.getReason().equals(InventoryCloseEvent.Reason.OPEN_NEW)) return;

        guiManager.removeOpenGUI(uuid);
    }

    /**
     * Handles when the player's inventory is dragged.
     * @param inventoryDragEvent An {@link InventoryDragEvent}
     */
    @Override
    public void handleBottomDrag(@NotNull InventoryDragEvent inventoryDragEvent) {
        inventoryDragEvent.setCancelled(true);
    }

    /**
     * Handles when the top or bottom inventory is dragged.
     * @param inventoryDragEvent An {@link InventoryDragEvent}
     */
    @Override
    public void handleGlobalDrag(@NotNull InventoryDragEvent inventoryDragEvent) {}

    /**
     * Handles when the player's inventory is clicked.
     * @param inventoryClickEvent An {@link InventoryClickEvent}
     */
    @Override
    public void handleBottomClick(@NotNull InventoryClickEvent inventoryClickEvent) {
        inventoryClickEvent.setCancelled(true);
    }

    /**
     * Handles when the top or bottom inventory is clicked.
     * @param inventoryClickEvent An {@link InventoryClickEvent}
     */
    @Override
    public void handleGlobalClick(@NotNull InventoryClickEvent inventoryClickEvent) {}

    /**
     * Create the {@link GUIButton}s for filler buttons.
     */
    private void createFillerButtons() {
        ItemStackBuilder fillerBuilder = new ItemStackBuilder(skyShop.getComponentLogger());
        fillerBuilder.setItemType(ItemType.GRAY_STAINED_GLASS_PANE);
        fillerBuilder.setAmount(1);
        fillerBuilder.setName(AdventureUtil.serialize(" "));
        Optional<@NotNull ItemStack> optionalFillerStack = fillerBuilder.buildItemStack();
        optionalFillerStack.ifPresent(itemStack -> {
            if(inventoryView == null) return;
            int guiSize = inventoryView.getTopInventory().getSize();

            GUIButton.Builder fillerGuiButtonBuilder = new GUIButton.Builder();
            fillerGuiButtonBuilder.setItemStack(itemStack);
            GUIButton fillerGuiButton = fillerGuiButtonBuilder.build();

            for(int i = 0; i < guiSize; i++) {
                setButton(i, fillerGuiButton);
            }
        });
    }

    /**
     * Create the exit or return button.
     */
    private void createReturnButton() {
        ItemStackBuilder itemStackBuilder = new ItemStackBuilder(skyShop.getComponentLogger());
        itemStackBuilder.setItemType(ItemType.BARRIER);
        itemStackBuilder.setAmount(1);
        itemStackBuilder.setName(AdventureUtil.serialize("<red>Click to exit the menu.</red>"));
        Optional<@NotNull ItemStack> optionalFillerStack = itemStackBuilder.buildItemStack();
        optionalFillerStack.ifPresent(itemStack -> {
            if(inventoryView == null) return;

            GUIButton.Builder guiButtonBuilder = new GUIButton.Builder();
            guiButtonBuilder.setItemStack(itemStack);
            GUIButton guiButton = guiButtonBuilder.build();

            setButton(49, guiButton);
        });
    }

    /**
     * Create the next page button if there is a next page.
     * @param statsMap A {@link Map} mapping {@link ItemType}s to {@link TransactionStats}.
     */
    private void createNextPageButton(@NotNull Map<ItemType, TransactionStats> statsMap) {
        int statsCount = statsMap.size();
        int statsPerPage = 27;
        int maxPages = statsCount / statsPerPage;
        if(pageNum >= maxPages) return;

        ItemStackBuilder itemStackBuilder = new ItemStackBuilder(skyShop.getComponentLogger());
        itemStackBuilder.setItemType(ItemType.ARROW);
        itemStackBuilder.setAmount(1);
        itemStackBuilder.setName(AdventureUtil.serialize("<red>Click to go to the next page.</red>"));
        Optional<@NotNull ItemStack> optionalFillerStack = itemStackBuilder.buildItemStack();
        optionalFillerStack.ifPresent(itemStack -> {
            if(inventoryView == null) return;

            GUIButton.Builder guiButtonBuilder = new GUIButton.Builder();
            guiButtonBuilder.setItemStack(itemStack);
            guiButtonBuilder.setAction(inventoryClickEvent -> {
                pageNum++;
                this.update();
            });
            GUIButton guiButton = guiButtonBuilder.build();

            setButton(51, guiButton);
        });
    }

    /**
     * Create the previous page button if there is a previous page.
     */
    private void createPrevPageButton() {
        if(pageNum <= 0) return;

        ItemStackBuilder itemStackBuilder = new ItemStackBuilder(skyShop.getComponentLogger());
        itemStackBuilder.setItemType(ItemType.ARROW);
        itemStackBuilder.setAmount(1);
        itemStackBuilder.setName(AdventureUtil.serialize("<red>Click to go to the previous page.</red>"));
        Optional<@NotNull ItemStack> optionalFillerStack = itemStackBuilder.buildItemStack();
        optionalFillerStack.ifPresent(itemStack -> {
            if(inventoryView == null) return;

            GUIButton.Builder guiButtonBuilder = new GUIButton.Builder();
            guiButtonBuilder.setItemStack(itemStack);
            guiButtonBuilder.setAction(inventoryClickEvent -> {
                pageNum--;
                this.update();
            });
            GUIButton guiButton = guiButtonBuilder.build();

            setButton(47, guiButton);
        });
    }

    /**
     * Create the buttons that display the actual stats.
     * @param statsMap A {@link Map} mapping {@link ItemType}s to {@link TransactionStats}.
     */
    private void createStatsButtons(@NotNull Map<ItemType, TransactionStats> statsMap) {
        int index = pageNum * 27 + (pageNum >= 1 ? 1 : 0);
        List<Map.Entry<ItemType, TransactionStats>> list = new ArrayList<>(statsMap.entrySet());

        for(int amountAdded = 0; amountAdded < 27;) {
            if(index >= statsMap.size()) break;

            Map.Entry<ItemType, TransactionStats> entry = list.get(index);
            ItemType itemType = entry.getKey();
            TransactionStats transactionStats = entry.getValue();

            List<Component> loreList = List.of(
                    AdventureUtil.serialize("<yellow>Total Amount Purchased:</yellow> " + transactionStats.getAmountPurchased()),
                    AdventureUtil.serialize("<yellow>Total Amount Sold:</yellow> " + transactionStats.getAmountSold())
            );

            ItemStackBuilder itemStackBuilder = new ItemStackBuilder(skyShop.getComponentLogger());
            itemStackBuilder.setItemType(ItemType.ARROW);
            itemStackBuilder.setAmount(1);
            itemStackBuilder.setName(AdventureUtil.serialize("<yellow>Transaction Stats For </yellow>" + FormatUtil.formatItemTypeName(itemType)));
            itemStackBuilder.setLore(loreList);

            Optional<@NotNull ItemStack> optionalItemStack = itemStackBuilder.buildItemStack();
            if(optionalItemStack.isPresent()) {
                ItemStack itemStack = optionalItemStack.get();

                GUIButton.Builder guiButtonBuilder = new GUIButton.Builder();
                guiButtonBuilder.setItemStack(itemStack);
                GUIButton guiButton = guiButtonBuilder.build();

                setButton(getSlot(amountAdded), guiButton);

                amountAdded++;
            }

            index++;
        }
    }

    /**
     * Maps the current number of stats added to the appropriate slot to use when adding a button.
     * @param amountAdded The number of stats added to the GUI.
     * @return The slot to use.
     * @throws RuntimeException if the amountAdded provided is > 27
     */
    private int getSlot(int amountAdded) {
        return switch (amountAdded) {
            case 0 -> 10;
            case 1 -> 11;
            case 2 -> 12;
            case 3 -> 13;
            case 4 -> 14;
            case 5 -> 15;
            case 6 -> 16;
            case 7 -> 19;
            case 8 -> 20;
            case 9 -> 21;
            case 10 -> 22;
            case 11 -> 23;
            case 12 -> 24;
            case 13 -> 25;
            case 14 -> 28;
            case 15 -> 29;
            case 16 -> 30;
            case 17 -> 31;
            case 18 -> 32;
            case 19 -> 33;
            case 20 -> 34;
            case 21 -> 37;
            case 22 -> 38;
            case 23 -> 39;
            case 24 -> 40;
            case 25 -> 41;
            case 26 -> 42;
            case 27 -> 43;
            default -> throw new RuntimeException("Unable to map " + amountAdded + " to a slot.");
        };
    }
}
