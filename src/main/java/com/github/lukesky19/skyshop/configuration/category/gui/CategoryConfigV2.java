package com.github.lukesky19.skyshop.configuration.category.gui;

import com.github.lukesky19.skylib.api.gui.GUIType;
import com.github.lukesky19.skylib.api.itemstack.ItemStackConfig;
import com.github.lukesky19.skylib.libs.configurate.objectmapping.ConfigSerializable;
import com.github.lukesky19.skyshop.util.ButtonType;
import com.github.lukesky19.skyshop.util.MultiplierType;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * This record contains the configuration for version 2.0.0.0 and 2.1.0.0.
 * @param configVersion The file's config version.
 * @param permission The permission required to access the category.
 * @param gui The {@link GuiData} configuration.
 */
@ConfigSerializable
public record CategoryConfigV2(@Nullable String configVersion, @Nullable String permission, @NotNull GuiData gui) {
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
     * @param buttons The {@link List} of {@link ButtonConfig}s.
     */
    @ConfigSerializable
    public record PageConfig(@NotNull List<@NotNull ButtonConfig> buttons) {}

    /**
     * This record contains the configuration to create buttons to be displayed.
     * @param buttonType The {@link ButtonType}.
     * @param slot The slot to place the button at.
     * @param shopName If the {@link ButtonType} is that of OPEN_SHOP, this is the shop name to open. This name corresponds to a file in {@code SkyShop/categories}.
     * @param displayItem The {@link ItemStackConfig} used to create the {@link ItemStack} for the button.
     * @param transactionData The {@link TransactionData} that will be used to complete a transaction.
     * @param permission The permission the player needs to see/access this button.
     */
    @ConfigSerializable
    public record ButtonConfig(
            @Nullable ButtonType buttonType,
            @Nullable Integer slot,
            @Nullable String shopName,
            @NotNull ItemStackConfig displayItem,
            @NotNull TransactionData transactionData,
            @Nullable String permission) {}
    /**
     * This record contains the configuration required to complete a transaction.
     * @param transactionStyle This is a file name in SkyShop/transaction_styles
     * @param transactionName This is the text to use in the success messages when a transaction is successful.
     * @param prices The {@link PriceConfig} for the transaction.
     * @param displayItem This {@link ItemStackConfig} used to create the {@link ItemStack} to display what is being purchased or sold.
     * @param transactionItem The {@link ItemStackConfig} used to create the {@link ItemStack} that will be purchased or sold.
     * @param buyCommands A {@link List} of {@link String}s to execute in console when purchased.
     * @param sellCommands A {@link List} of {@link String}s to execute in console when sold.
     * @param islandSize The island size data when a transaction occurs.
     * @param prestigeMultiplier The prestige multiplier data when a transaction occurs.
     */
    @ConfigSerializable
    public record TransactionData(
            @Nullable String transactionStyle,
            @Nullable String transactionName,
            @NotNull PriceConfig prices,
            @NotNull ItemStackConfig displayItem,
            @NotNull ItemStackConfig transactionItem,
            @NotNull List<String> buyCommands,
            @NotNull List<String> sellCommands,
            @NotNull IslandSizeData islandSize,
            @NotNull PrestigeMultiplierData prestigeMultiplier) {}

    /**
     * This record stores the data for an island size transactions.
     * @param setIslandSize Should the values here when purchased or sold set the island size to the exact values?
     * @param buyAmount The amount of island size to purchase.
     * @param sellAmount The amount of island size to sell.
     */
    @ConfigSerializable
    public record IslandSizeData(
            boolean setIslandSize,
            @Nullable Integer buyAmount,
            @Nullable Integer sellAmount) {}

    /**
     * This record stores the data for a prestige multiplier purchase.
     * @param multiplierType The {@link MultiplierType} of the purchase.
     * @param activeMultiplierPreventPurchase If the multiplier is active, prevent the purchase of another multiplier.
     * @param activeMultiplierHigherPreventPurchase If the active multiplier is higher than the one being purchased, prevent the purchase of the multiplier.
     * @param resetMultiplierTimeIfHigherMultiplier If the multiplier is higher than the active multiplier, should the current time be reset before adding time?
     * @param multiplier The multiplier to purchase.
     * @param time The multiplier time to purchase.
     * @param maxTime The maximum time to allow purchase of. If the total time will exceed this amount, the purchase won't be allowed.
     */
    @ConfigSerializable
    public record PrestigeMultiplierData(
            @Nullable MultiplierType multiplierType,
            boolean activeMultiplierPreventPurchase,
            boolean activeMultiplierHigherPreventPurchase,
            boolean resetMultiplierTimeIfHigherMultiplier,
            @Nullable Double multiplier,
            @Nullable Long time,
            @Nullable Long maxTime) {}

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