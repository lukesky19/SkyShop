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
package com.github.lukesky19.skyshop.configuration.category.gui;

import com.github.lukesky19.skylib.api.gui.GUIType;
import com.github.lukesky19.skylib.api.itemstack.ItemStackConfig;
import com.github.lukesky19.skylib.libs.configurate.objectmapping.ConfigSerializable;
import com.github.lukesky19.skyshop.api.configuration.TransactionConfiguration;
import com.github.lukesky19.skyshop.util.ButtonType;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * This record contains the configuration to create a category GUIs.
 * @param configVersion The file's config version.
 * @param permission The permission required to access the category.
 * @param guiType The {@link GUIType}.
 * @param guiName The name to display inside the GUI.
 * @param pages The {@link List} of {@link CategoryConfig.PageConfig}s.
 */
@ConfigSerializable
public record CategoryConfig(
        @Nullable String configVersion,
        @Nullable String permission,
        @Nullable GUIType guiType,
        @Nullable String guiName,
        @NotNull List<@NotNull PageConfig> pages) {
    /**
     * This record contains the configuration for individual pages.
     * @param buttons The {@link List} of {@link ButtonConfig}s.
     */
    @ConfigSerializable
    public record PageConfig(@NotNull List<@NotNull ButtonConfig> buttons) {}

    /**
     * This record contains the configuration to create buttons to be displayed.
     * @param buttonType The {@link ButtonType}.
     * @param slot The slot to place the button at.
     * @param shopName If the {@link ButtonType} is that of OPEN_SHOP, this is the shop name to open. This name corresponds to a file in {@code SkyShop/categories}.
     * @param permission The permission the player needs to see/access this button.
     * @param displayItem The {@link ItemStackConfig} used to create the {@link ItemStack} for the button.
     * @param transactionData The {@link TransactionData} that will be used to complete a transaction.
     */
    @ConfigSerializable
    public record ButtonConfig(
            @Nullable ButtonType buttonType,
            @Nullable Integer slot,
            @Nullable String shopName,
            @Nullable String permission,
            @NotNull ItemStackConfig displayItem,
            @Nullable TransactionData transactionData) {}
    /**
     * This record contains the configuration required to complete a transaction.
     * @param transactionStyle This is a file name in SkyShop/transaction_styles
     * @param transactionName This is the text to use in the success messages when a transaction is successful.
     * @param prices The {@link PriceConfig} for the transaction.
     * @param displayItem This {@link ItemStackConfig} used to create the {@link ItemStack} to display what is being purchased or sold.
     * @param transactionList A {@link List} of {@link TransactionConfiguration}s to process.
     */
    @ConfigSerializable
    public record TransactionData(
            @Nullable String transactionStyle,
            @Nullable String transactionName,
            @NotNull PriceConfig prices,
            @NotNull ItemStackConfig displayItem,
            @NotNull List<TransactionConfiguration> transactionList) {}
    /**
     * The price configuration for a transaction.
     * @param buyPrice The buy price of the item.
     * @param sellPrice The sell price of the item.
     * @param buyPoints The player points required to buy an item.
     * @param sellPoints The player points given when selling an item.
     */
    @ConfigSerializable
    public record PriceConfig(
            double buyPrice,
            double sellPrice,
            int buyPoints,
            int sellPoints) {}
}