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
package com.github.lukesky19.skyshop.transaction.processor;

import com.github.lukesky19.skylib.paper.api.adventure.PaperAdventureUtility;
import com.github.lukesky19.skylib.paper.api.itemstack.ItemStackBuilder;
import com.github.lukesky19.skylib.paper.api.itemstack.ItemStackConfig;
import com.github.lukesky19.skylib.paper.api.player.PlayerUtil;
import com.github.lukesky19.skyshop.SkyShop;
import com.github.lukesky19.skyshop.api.configuration.TransactionConfiguration;
import com.github.lukesky19.skyshop.api.event.ItemPrePurchaseEvent;
import com.github.lukesky19.skyshop.api.event.ItemPreSellEvent;
import com.github.lukesky19.skyshop.api.processor.TransactionProcessor;
import com.github.lukesky19.skyshop.api.result.TransactionResult;
import com.github.lukesky19.skyshop.configuration.category.transaction.ItemConfiguration;
import com.github.lukesky19.skyshop.configuration.locale.LocaleManager;
import com.github.lukesky19.skyshop.configuration.locale.data.LocaleV5;
import com.github.lukesky19.skyshop.stats.StatsManager;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ItemType;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Optional;

/**
 * This class processes {@link ItemConfiguration} for buying/selling items.
 */
public class ItemStackProcessor implements TransactionProcessor {
    private final @NonNull SkyShop skyShop;
    private final @NonNull ComponentLogger logger;
    private final @NonNull LocaleManager localeManager;
    private final @Nullable StatsManager statsManager;

    /**
     * Constructor
     * @param skyShop A {@link SkyShop} instance.
     * @param localeManager A {@link LocaleManager} instance.
     * @param statsManager A {@link StatsManager} instance.
     */
    public ItemStackProcessor(
            @NonNull SkyShop skyShop,
            @NonNull LocaleManager localeManager,
            @Nullable StatsManager statsManager) {
        this.skyShop = skyShop;
        this.logger = skyShop.getComponentLogger();
        this.localeManager = localeManager;
        this.statsManager = statsManager;
    }

    /**
     * Can the player buy the item with no errors?
     * @apiNote Prices are already checked.
     * @param player The {@link Player} buying the item(s).
     * @param configuration The configuration to process.
     * @param amount The amount being purchased.
     * @return A {@link TransactionResult}.
     */
    @Override
    public @NonNull TransactionResult canBuy(@NonNull Player player, @NonNull TransactionConfiguration configuration, int amount) {
        if(!(configuration instanceof ItemConfiguration itemConfiguration)) return new TransactionResult("Wrong Type", true, true, false);
        ItemStackConfig itemStackConfig = itemConfiguration.transactionItem();
        if(itemStackConfig.itemType() == null) return new TransactionResult("Not Configured", false, false, false);

        ItemStackBuilder itemStackBuilder = new ItemStackBuilder(logger);
        itemStackBuilder.fromItemStackConfig(itemStackConfig, player, List.of());
        itemStackBuilder.setAmount(amount);

        Optional<ItemStack> optionalItemStack = itemStackBuilder.buildItemStack();
        if(optionalItemStack.isEmpty()) return new TransactionResult("Invalid Configuration", true, true, false);
        ItemStack buyItem = optionalItemStack.get();
        buyItem.setAmount(amount);

        // Create and call the ItemPrePurchaseEvent
        ItemPrePurchaseEvent itemPrePurchaseEvent = new ItemPrePurchaseEvent(player, buyItem);
        skyShop.getServer().getPluginManager().callEvent(itemPrePurchaseEvent);

        // If the event was canceled, don't allow the purchase.
        return !itemPrePurchaseEvent.isCancelled()
                ? new TransactionResult("Success", false, false, false)
                : new TransactionResult("Cancelled", false, false, true);
    }

    /**
     * Can the player sell the item with no errors?
     * @param player The {@link Player} selling the item(s).
     * @param configuration The configuration to process.
     * @param amount The amount being sold.
     * @return A {@link TransactionResult}.
     */
    @Override
    public @NonNull TransactionResult canSell(@NonNull Player player, @NonNull TransactionConfiguration configuration, int amount) {
        if(!(configuration instanceof ItemConfiguration itemConfiguration)) return new TransactionResult("Wrong Type", true, true, false);
        ItemStackConfig itemStackConfig = itemConfiguration.transactionItem();
        if(itemStackConfig.itemType() == null) return new TransactionResult("Not Configured", false, false, false);
        LocaleV5 locale = localeManager.getConfiguration();

        // Create the ItemStack that will be taken from the player if they have enough of said ItemStack.
        ItemStackBuilder itemStackBuilder = new ItemStackBuilder(logger);
        itemStackBuilder.fromItemStackConfig(itemStackConfig, player, List.of());

        Optional<ItemStack> optionalItemStack = itemStackBuilder.buildItemStack();
        if(optionalItemStack.isEmpty()) return new TransactionResult("Invalid Configuration", true, true, false);
        ItemStack sellItem = optionalItemStack.get();
        sellItem.setAmount(amount);

        // Check if the player has the required amount to sell
        if(!player.getInventory().containsAtLeast(sellItem, amount)) {
            player.sendMessage(PaperAdventureUtility.deserialize(player, locale.prefix() + locale.notEnoughItems()));
            return new TransactionResult("Insufficient Resources", true, false, false);
        }

        // Create and call the ItemSoldEvent
        ItemPreSellEvent itemSoldEvent = new ItemPreSellEvent(player, sellItem);
        skyShop.getServer().getPluginManager().callEvent(itemSoldEvent);
        // If the event was canceled, cancel the transaction.
        return !itemSoldEvent.isCancelled()
                ? new TransactionResult("Success", false, false, false)
                : new TransactionResult("Cancelled", false, false, true);
    }

    /**
     * Process the configuration to buy the item(s).
     * @apiNote Prices are automatically taken from the player as necessary.<br>
     * If the buying fails, the rest of the transaction will still proceed.
     * @param player The {@link Player} to process data for.
     * @param configuration The configuration to process.
     * @param amount The amount being purchased.
     * @return A {@link TransactionResult}.
     */
    @Override
    public @NonNull TransactionResult buy(@NonNull Player player, @NonNull TransactionConfiguration configuration, int amount) {
        if(!(configuration instanceof ItemConfiguration itemConfiguration)) return new TransactionResult("Wrong Type", true, true, false);
        ItemStackConfig itemStackConfig = itemConfiguration.transactionItem();
        if(itemStackConfig.itemType() == null) return new TransactionResult("Not Configured", false, false, false);

        // Build the ItemStack that will be given to the player on successful purchase.
        ItemStackBuilder itemStackBuilder = new ItemStackBuilder(logger);
        itemStackBuilder.fromItemStackConfig(itemStackConfig, player, List.of());

        Optional<ItemStack> optionalItemStack = itemStackBuilder.buildItemStack();
        if(optionalItemStack.isEmpty()) return new TransactionResult("Invalid Configuration", true, true, false);
        ItemStack buyItem = optionalItemStack.get();
        buyItem.setAmount(amount);

        // Give the player the ItemStack they purchased.
        PlayerUtil.giveItem(player.getInventory(), buyItem, amount, player.getLocation());

        // Increment stats if statsManager is not null
        ItemType itemType = buyItem.getType().asItemType();
        if(itemType != null && statsManager != null) {
            statsManager.incrementAmountPurchased(itemType, amount);
        }

        return new TransactionResult("Success", false, false, false);
    }

    /**
     * Process the configuration to sell the item(s).
     * @apiNote Prices are automatically added to the player as necessary.<br>
     * If the selling fails, the rest of the transaction will still proceed.
     * @param player The {@link Player} to process data for.
     * @param configuration The configuration to process.
     * @param amount The amount being sold.
     * @return A {@link TransactionResult}.
     */
    @Override
    public @NonNull TransactionResult sell(@NonNull Player player, @NonNull TransactionConfiguration configuration, int amount) {
        if(!(configuration instanceof ItemConfiguration itemConfiguration)) return new TransactionResult("Wrong Type", true, true, false);
        ItemStackConfig itemStackConfig = itemConfiguration.transactionItem();
        if(itemStackConfig.itemType() == null) return new TransactionResult("Not Configured", false, false, false);

        // Create the ItemStack that will be taken from the player if they have enough of said ItemStack.
        ItemStackBuilder itemStackBuilder = new ItemStackBuilder(logger);
        itemStackBuilder.fromItemStackConfig(itemStackConfig, player, List.of());

        Optional<ItemStack> optionalItemStack = itemStackBuilder.buildItemStack();
        if(optionalItemStack.isEmpty()) return new TransactionResult("Invalid Configuration", true, true, false);
        ItemStack sellItem = optionalItemStack.get();
        sellItem.setAmount(amount);

        // Remove the sold item from the player's inventory.
        player.getInventory().removeItem(sellItem);

        // Increment stats if statsManager is not null
        ItemType itemType = sellItem.getType().asItemType();
        if(itemType != null && statsManager != null) {
            statsManager.incrementAmountSold(itemType, amount);
        }

        return new TransactionResult("Success", false, false, false);
    }
}