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
package com.github.lukesky19.skyshop.manager;

import com.github.lukesky19.skylib.api.adventure.AdventureUtil;
import com.github.lukesky19.skylib.api.gui.interfaces.BaseGUI;
import com.github.lukesky19.skylib.api.itemstack.ItemStackBuilder;
import com.github.lukesky19.skylib.api.itemstack.ItemStackConfig;
import com.github.lukesky19.skylib.api.placeholderapi.PlaceholderAPIUtil;
import com.github.lukesky19.skylib.api.player.PlayerUtil;
import com.github.lukesky19.skyshop.SkyShop;
import com.github.lukesky19.skyshop.config.locale.Locale;
import com.github.lukesky19.skyshop.config.settings.Settings;
import com.github.lukesky19.skyshop.event.CommandPurchasedEvent;
import com.github.lukesky19.skyshop.event.CommandSoldEvent;
import com.github.lukesky19.skyshop.event.ItemPurchasedEvent;
import com.github.lukesky19.skyshop.event.ItemSoldEvent;
import com.github.lukesky19.skyshop.gui.TransactionGUI;
import com.github.lukesky19.skyshop.hook.impl.BentoBoxHook;
import com.github.lukesky19.skyshop.hook.impl.EconomyHook;
import com.github.lukesky19.skyshop.hook.impl.PlayerPointsHook;
import com.github.lukesky19.skyshop.manager.config.LocaleManager;
import com.github.lukesky19.skyshop.manager.config.SettingsManager;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.Server;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ItemType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import world.bentobox.bentobox.database.objects.Island;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * This class manages the processing of transactions from {@link TransactionGUI}s.
 */
public class TransactionManager {
    private final @NotNull SkyShop skyShop;
    private final @NotNull ComponentLogger logger;
    private final @NotNull SettingsManager settingsManager;
    private final @NotNull LocaleManager localeManager;
    private final @NotNull HookManager hookManager;
    private final @Nullable StatsManager statsManager;

    /**
     * Constructor
     * @param skyShop A {@link SkyShop} instance.
     * @param settingsManager A {@link SettingsManager} instance.
     * @param localeManager A {@link LocaleManager} instance.
     * @param hookManager A {@link HookManager} instance.
     * @param statsManager A {@link StatsManager} instance or null.
     */
    public TransactionManager(
            @NotNull SkyShop skyShop,
            @NotNull SettingsManager settingsManager,
            @NotNull LocaleManager localeManager,
            @NotNull HookManager hookManager,
            @Nullable StatsManager statsManager) {
        this.skyShop = skyShop;
        this.logger = skyShop.getComponentLogger();
        this.settingsManager = settingsManager;
        this.localeManager = localeManager;
        this.hookManager = hookManager;
        this.statsManager = statsManager;
    }

    /**
     * Initiate the buying of an item.
     * @param player The {@link Player} involved.
     * @param gui The {@link BaseGUI} involved.
     * @param transactionItemConfig The {@link ItemStackConfig} for the item involved.
     * @param transactionName The transaction name.
     * @param amount The amount involved.
     * @param money The money.
     * @param points The player points.
     */
    public void buyItem(
            @NotNull Player player,
            @NotNull BaseGUI<UUID> gui,
            @NotNull ItemStackConfig transactionItemConfig,
            @NotNull String transactionName,
            int amount,
            double money,
            int points) {
        Locale locale = localeManager.getLocale();
        EconomyHook economyHook = hookManager.getHook(EconomyHook.class);
        PlayerPointsHook playerPointsHook = hookManager.getHook(PlayerPointsHook.class);

        // Validate the money and points
        if(!validateMoneyAndPoints(locale, economyHook, playerPointsHook, player, gui, money, points)) return;

        // Build the ItemStack that will be given to the player on successful purchase.
        @Nullable ItemStack buyItem = buildItemStack(locale, player, gui, transactionItemConfig, amount);
        if(buyItem == null) return;

        // Create and call the ItemPurchasedEvent
        ItemPurchasedEvent itemPurchasedEvent = new ItemPurchasedEvent(player, buyItem);
        skyShop.getServer().getPluginManager().callEvent(itemPurchasedEvent);
        // If the event was cancelled, cancel the purchase.
        if(itemPurchasedEvent.isCancelled()) return;

        // Remove the prices from the player's balances
        if(money > 0) economyHook.removeFromBalance(player, money);
        if(points > 0) playerPointsHook.removeFromBalance(player, points);

        // Give the player the ItemStack they purchased.
        PlayerUtil.giveItem(player.getInventory(), buyItem, amount, player.getLocation());

        // Create the necessary placeholders
        List<TagResolver.Single> successPlaceholders = buildPlaceholders(economyHook, playerPointsHook, player, transactionName, amount, money, points);

        // Send the message that the transaction was a success
        if(money > 0 && points > 0) {
            player.sendMessage(AdventureUtil.deserialize(player, locale.prefix() + locale.buyItemSuccess().moneyAndPoints(), successPlaceholders));
        } else if(money > 0) {
            player.sendMessage(AdventureUtil.deserialize(player, locale.prefix() + locale.buyItemSuccess().money(), successPlaceholders));
        } else { // Points only
            player.sendMessage(AdventureUtil.deserialize(player, locale.prefix() + locale.buyItemSuccess().points(), successPlaceholders));
        }

        // Increment stats if statsManager is not null
        ItemType itemType = buyItem.getType().asItemType();
        if(itemType == null) return;
        if(statsManager != null) statsManager.incrementAmountPurchased(itemType, amount);
    }

    /**
     * Initiate the selling of an item.
     * @param player The {@link Player} involved.
     * @param gui The {@link BaseGUI} involved.
     * @param transactionItemConfig The {@link ItemStackConfig} for the item involved.
     * @param transactionName The transaction name.
     * @param amount The amount involved.
     * @param money The money.
     * @param points The player points.
     */
    public void sellItem(
            @NotNull Player player,
            @NotNull BaseGUI<UUID> gui,
            @NotNull ItemStackConfig transactionItemConfig,
            @NotNull String transactionName,
            int amount,
            double money,
            int points) {
        Locale locale = localeManager.getLocale();
        EconomyHook economyHook = hookManager.getHook(EconomyHook.class);
        PlayerPointsHook playerPointsHook = hookManager.getHook(PlayerPointsHook.class);

        // Validate the money and points
        if(!validateMoneyAndPoints(locale, economyHook, playerPointsHook, player, gui, money, points)) return;

        // Create the ItemStack that will be taken from the player if they have enough of said ItemStack.
        @Nullable ItemStack sellItem = buildItemStack(locale, player, gui, transactionItemConfig, amount);
        if(sellItem == null) return;

        // Check if the player has the required amount to sell
        if(!player.getInventory().containsAtLeast(sellItem, amount)) {
            player.sendMessage(AdventureUtil.deserialize(player, locale.prefix() + locale.notEnoughItems()));
            gui.close();
            return;
        }

        // Create and call the ItemSoldEvent
        ItemSoldEvent itemSoldEvent = new ItemSoldEvent(player, sellItem);
        skyShop.getServer().getPluginManager().callEvent(itemSoldEvent);
        // If the event was cancelled, cancel the transaction.
        if(itemSoldEvent.isCancelled()) return;

        // Remove the sold item from the player's inventory.
        player.getInventory().removeItem(sellItem);

        // Add the prices to the player's balances
        if(money > 0) economyHook.addToBalance(player, money);
        if(points > 0) playerPointsHook.addToBalance(player, points);

        // Create the necessary placeholders
        List<TagResolver.Single> successPlaceholders = buildPlaceholders(economyHook, playerPointsHook, player, transactionName, amount, money, points);

        // Send the message that the transaction was a success
        if(money > 0 && points > 0) {
            player.sendMessage(AdventureUtil.deserialize(player, locale.prefix() + locale.sellItemSuccess().moneyAndPoints(), successPlaceholders));
        } else if(money > 0) {
            player.sendMessage(AdventureUtil.deserialize(player, locale.prefix() + locale.sellItemSuccess().money(), successPlaceholders));
        } else { // Points only
            player.sendMessage(AdventureUtil.deserialize(player, locale.prefix() + locale.sellItemSuccess().points(), successPlaceholders));
        }

        // Increment stats if statsManager is not null
        ItemType itemType = sellItem.getType().asItemType();
        if(itemType == null) return;
        if(statsManager != null) statsManager.incrementAmountSold(itemType, amount);
    }

    /**
     * Initiate the buying of commands.
     * @param player The {@link Player} involved.
     * @param gui The {@link BaseGUI} involved.
     * @param transactionName The transaction name.
     * @param commands The {@link List} of {@link String}s for the commands involved.
     * @param amount The amount involved.
     * @param money The money.
     * @param points The player points.
     */
    public void buyCommand(
            @NotNull Player player,
            @NotNull BaseGUI<UUID> gui,
            @NotNull String transactionName,
            @NotNull List<String> commands,
            int amount,
            double money,
            int points) {
        Locale locale = localeManager.getLocale();
        EconomyHook economyHook = hookManager.getHook(EconomyHook.class);
        PlayerPointsHook playerPointsHook = hookManager.getHook(PlayerPointsHook.class);

        // Validate the money and points
        if(!validateMoneyAndPoints(locale, economyHook, playerPointsHook, player, gui, money, points)) return;

        // Parse any placeholders in the commands
        List<String> parsedCommands = commands.stream().map(command -> PlaceholderAPIUtil.parsePlaceholders(player, command)).toList();

        // Create and call the CommandPurchasedEvent
        CommandPurchasedEvent commandPurchasedEvent = new CommandPurchasedEvent(player, parsedCommands);
        skyShop.getServer().getPluginManager().callEvent(commandPurchasedEvent);
        // If the event was cancelled, cancel the transaction.
        if(commandPurchasedEvent.isCancelled()) return;

        // Remove the prices from the player's balances
        if(money > 0) economyHook.removeFromBalance(player, money);
        if(points > 0) playerPointsHook.removeFromBalance(player, points);

        // Execute the commands for this transaction
        Server server = skyShop.getServer();
        ConsoleCommandSender commandSender = server.getConsoleSender();
        for(String command : parsedCommands) {
            for(int i = 1; i <= amount; i++) {
                server.dispatchCommand(commandSender, command);
            }
        }

        // Create the necessary placeholders
        List<TagResolver.Single> successPlaceholders = buildPlaceholders(economyHook, playerPointsHook, player, transactionName, amount, money, points);

        // Send the message that the transaction was a success
        if(money > 0 && points > 0) {
            player.sendMessage(AdventureUtil.deserialize(player, locale.prefix() + locale.buyCommandSuccess().moneyAndPoints(), successPlaceholders));
        } else if(money > 0) {
            player.sendMessage(AdventureUtil.deserialize(player, locale.prefix() + locale.buyCommandSuccess().money(), successPlaceholders));
        } else { // Points only
            player.sendMessage(AdventureUtil.deserialize(player, locale.prefix() + locale.buyCommandSuccess().points(), successPlaceholders));
        }
    }

    /**
     * Initiate the selling of commands.
     * @param player The {@link Player} involved.
     * @param gui The {@link BaseGUI} involved.
     * @param transactionName The transaction name.
     * @param commands The {@link List} of {@link String}s for the commands involved.
     * @param amount The amount involved.
     * @param money The money.
     * @param points The player points.
     */
    public void sellCommand(
            @NotNull Player player,
            @NotNull BaseGUI<UUID> gui,
            @NotNull String transactionName,
            @NotNull List<String> commands,
            int amount,
            double money,
            int points) {
        Locale locale = localeManager.getLocale();
        EconomyHook economyHook = hookManager.getHook(EconomyHook.class);
        PlayerPointsHook playerPointsHook = hookManager.getHook(PlayerPointsHook.class);

        // Validate the money and points
        if(!validateMoneyAndPoints(locale, economyHook, playerPointsHook, player, gui, money, points)) return;

        // Parse any placeholders in the commands
        List<String> parsedCommands = commands.stream().map(command -> PlaceholderAPIUtil.parsePlaceholders(player, command)).toList();

        // Create and call the CommandSoldEvent
        CommandSoldEvent commandSoldEvent = new CommandSoldEvent(player, parsedCommands);
        skyShop.getServer().getPluginManager().callEvent(commandSoldEvent);
        // If the event was cancelled, cancel the transaction.
        if(commandSoldEvent.isCancelled()) return;

        // Add the prices to the player's balances
        if(money > 0) economyHook.addToBalance(player, money);
        if(points > 0) playerPointsHook.addToBalance(player, points);

        // Execute the commands for this transaction
        Server server = skyShop.getServer();
        ConsoleCommandSender commandSender = skyShop.getServer().getConsoleSender();
        for(String command : parsedCommands) {
            for(int i = 1; i <= amount; i++) {
                server.dispatchCommand(commandSender, command);
            }
        }

        // Create the necessary placeholders
        List<TagResolver.Single> successPlaceholders = buildPlaceholders(economyHook, playerPointsHook, player, transactionName, amount, money, points);

        // Send the message that the transaction was a success
        if(money > 0 && points > 0) {
            player.sendMessage(AdventureUtil.deserialize(player, locale.prefix() + locale.sellCommandSuccess().moneyAndPoints(), successPlaceholders));
        } else if(money > 0) {
            player.sendMessage(AdventureUtil.deserialize(player, locale.prefix() + locale.sellCommandSuccess().money(), successPlaceholders));
        } else { // Points only
            player.sendMessage(AdventureUtil.deserialize(player, locale.prefix() + locale.sellCommandSuccess().points(), successPlaceholders));
        }
    }

    /**
     * Initiate the buying of island size.
     * @param player The {@link Player} involved.
     * @param gui The {@link BaseGUI} involved.
     * @param transactionName The transaction name.
     * @param islandSize The island size.
     * @param amount The amount involved.
     * @param money The money involved.
     * @param points The player points involved.
     */
    public void buyIslandSize(
            @NotNull Player player,
            @NotNull BaseGUI<UUID> gui,
            @NotNull String transactionName,
            @Nullable Integer islandSize,
            int amount,
            double money,
            int points) {
        if(islandSize == null || islandSize <= 0) return;
        Locale locale = localeManager.getLocale();
        @Nullable Settings settings = settingsManager.getConfiguration();
        if(settings == null || settings.islandSizeLimit() == null) {
            logger.error(AdventureUtil.deserialize("Unable to complete the transaction because the plugin's settings are invalid."));
            player.sendMessage(AdventureUtil.deserialize(locale.prefix() + locale.transactionError()));
            gui.close();
            return;
        }
        BentoBoxHook bentoBoxHook = hookManager.getHook(BentoBoxHook.class);
        EconomyHook economyHook = hookManager.getHook(EconomyHook.class);
        PlayerPointsHook playerPointsHook = hookManager.getHook(PlayerPointsHook.class);

        // Check if BentoBox is hooked into
        if(!bentoBoxHook.isHooked()) {
            logger.error(AdventureUtil.deserialize("Unable to complete the transaction because BentoBox is not hooked into."));
            player.sendMessage(AdventureUtil.deserialize(locale.prefix() + locale.transactionError()));
            gui.close();
            return;
        }

        // Get and validate the island
        @Nullable Island island = bentoBoxHook.getIslandAtLocation(player.getLocation());
        if(island == null) {
            player.sendMessage(AdventureUtil.deserialize(locale.prefix() + locale.notOnIsland()));
            gui.close();
            return;
        }

        // Check if the island is at the max configured size
        if(island.getProtectionRange() >= settings.islandSizeLimit()) {
            player.sendMessage(AdventureUtil.deserialize(locale.prefix() + locale.islandMaxSize()));
            gui.close();
            return;
        }

        // Validate the money and points
        if(!validateMoneyAndPoints(locale, economyHook, playerPointsHook, player, gui, money, points)) return;

        // Remove the prices from the player's balances
        if(money > 0) economyHook.removeFromBalance(player, money);
        if(points > 0) playerPointsHook.removeFromBalance(player, points);

        // Add the island size
        bentoBoxHook.addIslandSize(player.getUniqueId(), island, islandSize, settings.islandSizeLimit());

        // Create the necessary placeholders
        List<TagResolver.Single> successPlaceholders = buildPlaceholders(economyHook, playerPointsHook, player, transactionName, amount, money, points);

        // Send the message that the transaction was a success
        if(money > 0 && points > 0) {
            player.sendMessage(AdventureUtil.deserialize(player, locale.prefix() + locale.otherBuySuccess().moneyAndPoints(), successPlaceholders));
        } else if(money > 0) {
            player.sendMessage(AdventureUtil.deserialize(player, locale.prefix() + locale.otherBuySuccess().money(), successPlaceholders));
        } else { // Points only
            player.sendMessage(AdventureUtil.deserialize(player, locale.prefix() + locale.otherBuySuccess().points(), successPlaceholders));
        }
    }

    /**
     * Initiate the selling of island size.
     * @param player The {@link Player} involved.
     * @param gui The {@link BaseGUI} involved.
     * @param transactionName The transaction name.
     * @param islandSize The island size.
     * @param amount The amount involved.
     * @param money The money involved.
     * @param points The player points involved.
     */
    public void sellIslandSize(
            @NotNull Player player,
            @NotNull BaseGUI<UUID> gui,
            @NotNull String transactionName,
            @Nullable Integer islandSize,
            int amount,
            double money,
            int points) {
        if(islandSize == null || islandSize <= 0) return;
        Locale locale = localeManager.getLocale();
        BentoBoxHook bentoBoxHook = hookManager.getHook(BentoBoxHook.class);
        EconomyHook economyHook = hookManager.getHook(EconomyHook.class);
        PlayerPointsHook playerPointsHook = hookManager.getHook(PlayerPointsHook.class);

        // Check if BentoBox is hooked into
        if(!bentoBoxHook.isHooked()) {
            logger.error(AdventureUtil.deserialize("Unable to complete the transaction because BentoBox is not hooked into."));
            player.sendMessage(AdventureUtil.deserialize(locale.prefix() + locale.transactionError()));
            gui.close();
            return;
        }

        // Get and validate the island
        @Nullable Island island = bentoBoxHook.getIslandAtLocation(player.getLocation());
        if(island == null) {
            player.sendMessage(AdventureUtil.deserialize(locale.prefix() + locale.notOnIsland()));
            gui.close();
            return;
        }

        // Validate the money and points
        if(!validateMoneyAndPoints(locale, economyHook, playerPointsHook, player, gui, money, points)) return;

        // Validate that the player's island is large enough
        if(island.getProtectionRange() <= islandSize) {
            player.sendMessage(AdventureUtil.deserialize(locale.prefix() + locale.islandTooSmall()));
            gui.close();
            return;
        }

        // Add the prices from the player's balances
        if(money > 0) economyHook.addToBalance(player, money);
        if(points > 0) playerPointsHook.addToBalance(player, points);

        // Remove the island size
        bentoBoxHook.removeIslandSize(player.getUniqueId(), island, islandSize);

        // Create the necessary placeholders
        List<TagResolver.Single> successPlaceholders = buildPlaceholders(economyHook, playerPointsHook, player, transactionName, amount, money, points);

        // Send the message that the transaction was a success
        if(money > 0 && points > 0) {
            player.sendMessage(AdventureUtil.deserialize(player, locale.prefix() + locale.otherSellSuccess().moneyAndPoints(), successPlaceholders));
        } else if(money > 0) {
            player.sendMessage(AdventureUtil.deserialize(player, locale.prefix() + locale.otherSellSuccess().money(), successPlaceholders));
        } else { // Points only
            player.sendMessage(AdventureUtil.deserialize(player, locale.prefix() + locale.otherSellSuccess().points(), successPlaceholders));
        }
    }

    /**
     * Check if the money and points are valid and that the necessary plugins are hooked into.
     * The GUI the player is in will be closed on any error.
     * @param locale The plugin's {@link Locale}.
     * @param economyHook The plugin's {@link EconomyHook}.
     * @param playerPointsHook The player's {@link PlayerPointsHook}.
     * @param player The {@link Player}.
     * @param gui The {@link BaseGUI}.
     * @param money The money.
     * @param points The player points.
     * @return true if money and points are valid along with the necessary hooks or false.
     */
    private boolean validateMoneyAndPoints(
            @NotNull Locale locale,
            @NotNull EconomyHook economyHook,
            @NotNull PlayerPointsHook playerPointsHook,
            @NotNull Player player,
            @NotNull BaseGUI<UUID> gui,
            double money,
            int points) {
        if(money > 0 && points > 0) {
            logger.error(AdventureUtil.deserialize("Unable to complete the transaction because money and points are less than or equal to 0."));
            player.sendMessage(AdventureUtil.deserialize(locale.prefix() + locale.transactionError()));
            gui.close();
            return false;
        }

        if(money > 0) {
            if(!economyHook.isHooked()) {
                logger.error(AdventureUtil.deserialize("Unable to complete the transaction due to no economy found."));
                player.sendMessage(AdventureUtil.deserialize(locale.prefix() + locale.transactionError()));
                gui.close();
                return false;
            }

            if(economyHook.getBalance(player) < money) {
                player.sendMessage(AdventureUtil.deserialize(player, locale.prefix() + locale.insufficientMoney()));
                gui.close();
                return false;
            }
        }

        if(points > 0) {
            if(!playerPointsHook.isHooked()) {
                logger.error(AdventureUtil.deserialize("Unable to complete the transaction due to no player points dependency found."));
                player.sendMessage(AdventureUtil.deserialize(locale.prefix() + locale.transactionError()));
                gui.close();
                return false;
            }

            if(playerPointsHook.getBalance(player) < points) {
                player.sendMessage(AdventureUtil.deserialize(player, locale.prefix() + locale.insufficientPlayerPoints()));
                gui.close();
                return false;
            }
        }

        return true;
    }

    /**
     * Create an {@link ItemStack} from the {@link ItemStackConfig}.
     * @param locale The plugin's {@link Locale}.
     * @param player The {@link Player} involved in the transaction.
     * @param gui The {@link BaseGUI} involved in the transaction.
     * @param itemStackConfig The {@link ItemStackConfig} for the transaction ItemStack.
     * @param transactionAmount The amount of items the ItemStack should contain for this transaction.
     * @return An {@link ItemStack} or null if creation failed.
     */
    private @Nullable ItemStack buildItemStack(
            @NotNull Locale locale,
            @NotNull Player player,
            @NotNull BaseGUI<UUID> gui,
            @NotNull ItemStackConfig itemStackConfig,
            int transactionAmount) {
        ItemStackBuilder itemStackBuilder = new ItemStackBuilder(logger);
        itemStackBuilder.fromItemStackConfig(itemStackConfig, player, null, List.of());
        itemStackBuilder.setAmount(transactionAmount);

        Optional<ItemStack> optionalItemStack = itemStackBuilder.buildItemStack();
        if(optionalItemStack.isEmpty()) {
            logger.error(AdventureUtil.deserialize("Unable create the ItemStack for a transaction due to invalid transaction item config."));
            player.sendMessage(AdventureUtil.deserialize(locale.prefix() + locale.transactionError()));
            gui.close();
            return null;
        }

        return optionalItemStack.get();
    }

    /**
     * Create the list of placeholders for success messages.
     * @param economyHook The {@link EconomyHook}.
     * @param playerPointsHook The {@link PlayerPointsHook}.
     * @param player The {@link Player}.
     * @param transactionName The transaction name.
     * @param amount The amount.
     * @param money The money.
     * @param points The player points.
     * @return A {@link List} of {@link TagResolver.Single}.
     */
    private @NotNull List<TagResolver.Single> buildPlaceholders(
            @NotNull EconomyHook economyHook,
            @NotNull PlayerPointsHook playerPointsHook,
            @NotNull Player player,
            @NotNull String transactionName,
            int amount,
            double money,
            int points) {
        List<TagResolver.Single> placeholders = new ArrayList<>();
        placeholders.add(Placeholder.parsed("amount", String.valueOf(amount)));
        placeholders.add(Placeholder.parsed("transaction_name", transactionName));

        if(money > 0 && points > 0) {
            DecimalFormat df = new DecimalFormat("#.##");
            df.setRoundingMode(RoundingMode.CEILING);

            BigDecimal bigPrice = BigDecimal.valueOf(money);
            String formattedSellPrice = df.format(bigPrice);
            placeholders.add(Placeholder.parsed("money", formattedSellPrice));

            BigDecimal bigBalance = BigDecimal.valueOf(economyHook.getBalance(player));
            placeholders.add(Placeholder.parsed("money_balance", df.format(bigBalance)));

            placeholders.add(Placeholder.parsed("player_points", String.valueOf(points)));
            placeholders.add(Placeholder.parsed("player_points_balance", String.valueOf(playerPointsHook.getBalance(player))));
        } else if(money > 0) {
            DecimalFormat df = new DecimalFormat("#.##");
            df.setRoundingMode(RoundingMode.CEILING);

            BigDecimal bigPrice = BigDecimal.valueOf(money);
            String formattedSellPrice = df.format(bigPrice);
            placeholders.add(Placeholder.parsed("money", formattedSellPrice));

            BigDecimal bigBalance = BigDecimal.valueOf(economyHook.getBalance(player));
            placeholders.add(Placeholder.parsed("money_balance", df.format(bigBalance)));

            placeholders.add(Placeholder.parsed("player_points", "0"));
            placeholders.add(Placeholder.parsed("player_points_balance", "0"));
        } else if(points > 0) {
            placeholders.add(Placeholder.parsed("player_points", String.valueOf(points)));
            placeholders.add(Placeholder.parsed("player_points_balance", String.valueOf(playerPointsHook.getBalance(player))));

            placeholders.add(Placeholder.parsed("money", "0"));
            placeholders.add(Placeholder.parsed("money_balance", "0"));
        }

        return placeholders;
    }
}