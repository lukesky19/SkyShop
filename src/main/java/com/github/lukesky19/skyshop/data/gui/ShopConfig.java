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
package com.github.lukesky19.skyshop.data.gui;

import com.github.lukesky19.skylib.api.gui.GUIType;
import com.github.lukesky19.skylib.api.itemstack.ItemStackConfig;
import com.github.lukesky19.skylib.libs.configurate.objectmapping.ConfigSerializable;
import com.github.lukesky19.skyshop.gui.ShopGUI;
import com.github.lukesky19.skyshop.util.ButtonType;
import com.github.lukesky19.skyshop.util.TransactionType;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * This record contains the configuration to create a {@link ShopGUI}.
 * @param configVersion The file's config version.
 * @param gui The {@link GuiData} configuration.
 */
@ConfigSerializable
public record ShopConfig(@Nullable String configVersion, @NotNull GuiData gui) {
    /**
     * This record contains the actual configuration for creating the initial GUI.
     * @param guiType The {@link GUIType}.
     * @param name The name to display inside the GUI.
     * @param pages The {@link List} of {@link PageConfig}s.
     */
    @ConfigSerializable
    public record GuiData(@Nullable GUIType guiType, @Nullable String name, @NotNull List<@NotNull PageConfig> pages) {}

    /**
     * This record contains the configuration for individual pages.
     * @param buttons The {@link List} of {@link Button}s.
     */
    @ConfigSerializable
    public record PageConfig(@NotNull List<@NotNull Button> buttons) {}

    /**
     * This record contains the configuration to create buttons to be displayed.
     * @param buttonType The {@link ButtonType}.
     * @param slot The slot to place the button at.
     * @param displayItem The {@link ItemStackConfig} used to create the {@link ItemStack} for the button.
     * @param transactionData The {@link TransactionData} that will be used to complete a transaction.
     */
    @ConfigSerializable
    public record Button(
            @Nullable ButtonType buttonType,
            @Nullable Integer slot,
            @NotNull ItemStackConfig displayItem,
            @NotNull TransactionData transactionData) {}

    /**
     * This record contains the configuration required to complete a transaction.
     * @param transactionType The {@link TransactionType}.
     * @param transactionStyle This is a file name in SkyShop/transaction_styles
     * @param buyPrice The buy price of the item.
     * @param sellPrice The sell price of the item.
     * @param transactionName This is the text to use in the success messages when a transaction is successful.
     * @param displayItem This is the {@link ItemStackConfig} used to create the {@link ItemStack} to display what is being purchased or sold.
     * @param transactionItem The is the {@link ItemStackConfig} used to create the {@link ItemStack} that will be purchased or sold.
     * @param buyCommands A {@link List} of {@link String}s to execute in console when purchased.
     * @param sellCommands A {@link List} of {@link String}s to execute in console when sold.
     */
    @ConfigSerializable
    public record TransactionData(
            @Nullable TransactionType transactionType,
            @Nullable String transactionStyle,
            @Nullable Double buyPrice,
            @Nullable Double sellPrice,
            @Nullable String transactionName,
            @NotNull ItemStackConfig displayItem,
            @NotNull ItemStackConfig transactionItem,
            @NotNull List<String> buyCommands,
            @NotNull List<String> sellCommands) {}
}
