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
package com.github.lukesky19.skyshop.transaction;

import com.github.lukesky19.skylib.api.placeholderapi.PlaceholderAPIUtil;
import com.github.lukesky19.skyshop.SkyShop;
import com.github.lukesky19.skyshop.api.configuration.TransactionConfiguration;
import com.github.lukesky19.skyshop.api.event.CommandPrePurchaseEvent;
import com.github.lukesky19.skyshop.api.event.CommandPreSellEvent;
import com.github.lukesky19.skyshop.api.processor.TransactionProcessor;
import com.github.lukesky19.skyshop.api.result.TransactionResult;
import com.github.lukesky19.skyshop.configuration.category.transaction.CommandConfiguration;
import org.bukkit.Server;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Processes the {@link CommandConfiguration} transaction configuration.
 */
public class CommandDataProcessor implements TransactionProcessor {
    private final @NotNull SkyShop skyShop;

    /**
     * Constructor
     * @param skyShop A {@link SkyShop} instance.
     */
    public CommandDataProcessor(@NotNull SkyShop skyShop) {
        this.skyShop = skyShop;
    }

    /**
     * Can the player buy the commands with no errors?
     * @apiNote Prices are already checked.
     * @param player The {@link Player} buying the commands(s).
     * @param configuration The configuration to process.
     * @param amount The amount being purchased.
     * @return A {@link TransactionResult}.
     */
    @Override
    public @NotNull TransactionResult canBuy(@NotNull Player player, @NotNull TransactionConfiguration configuration, int amount) {
        if(!(configuration instanceof CommandConfiguration commandConfiguration)) return new TransactionResult("Wrong Type", true, true, false);
        if(commandConfiguration.buyCommands().isEmpty()) return new TransactionResult("Not Configured", false, false, false);

        // Parse any placeholders in the commands
        List<String> parsedCommands = commandConfiguration.buyCommands().stream()
                .map(command -> PlaceholderAPIUtil.parsePlaceholders(player, command)).toList();

        // Create and call the CommandPurchasedEvent
        CommandPrePurchaseEvent commandPurchasedEvent = new CommandPrePurchaseEvent(player, parsedCommands);
        skyShop.getServer().getPluginManager().callEvent(commandPurchasedEvent);

        // If the event was canceled, cancel the transaction.
        return !commandPurchasedEvent.isCancelled()
                ? new TransactionResult("Success", false, false, false)
                : new TransactionResult("Cancelled", false, false, true);
    }

    /**
     * Can the player sell the commands with no errors?
     * @param player The {@link Player} selling the commands(s).
     * @param configuration The configuration to process.
     * @param amount The amount being sold.
     * @return A {@link TransactionResult}.
     */
    @Override
    public @NotNull TransactionResult canSell(@NotNull Player player, @NotNull TransactionConfiguration configuration, int amount) {
        if(!(configuration instanceof CommandConfiguration commandConfiguration)) return new TransactionResult("Wrong Type", true, true, false);
        if(commandConfiguration.sellCommands().isEmpty()) return new TransactionResult("Not Configured", false, false, false);

        // Parse any placeholders in the commands
        List<String> parsedCommands = commandConfiguration.sellCommands().stream()
                .map(command -> PlaceholderAPIUtil.parsePlaceholders(player, command)).toList();

        // Create and call the CommandPreSellEvent
        CommandPreSellEvent commandPreSellEvent = new CommandPreSellEvent(player, parsedCommands);
        skyShop.getServer().getPluginManager().callEvent(commandPreSellEvent);

        // If the event was canceled, cancel the transaction.
        return !commandPreSellEvent.isCancelled()
                ? new TransactionResult("Success", false, false, false)
                : new TransactionResult("Cancelled", false, false, true);
    }

    /**
     * Process the configuration to buy the commands(s).
     * @apiNote Prices are automatically taken from the player as necessary.<br>
     * If the buying fails, the rest of the transaction will still proceed.
     * @param player The {@link Player} to process configuration for.
     * @param configuration The configuration to process.
     * @param amount The amount being purchased.
     * @return A {@link TransactionResult}.
     */
    @Override
    public @NotNull TransactionResult buy(@NotNull Player player, @NotNull TransactionConfiguration configuration, int amount) {
        if(!(configuration instanceof CommandConfiguration commandConfiguration)) return new TransactionResult("Wrong Type", true, true, false);
        if(commandConfiguration.buyCommands().isEmpty()) return new TransactionResult("Not Configured", false, false, false);

        // Parse any placeholders in the commands
        List<String> parsedCommands = commandConfiguration.buyCommands().stream()
                .map(command -> PlaceholderAPIUtil.parsePlaceholders(player, command)).toList();

        // Execute the commands for this transaction
        Server server = skyShop.getServer();
        ConsoleCommandSender commandSender = server.getConsoleSender();
        for(String command : parsedCommands) {
            for(int i = 1; i <= amount; i++) {
                server.dispatchCommand(commandSender, command);
            }
        }

        return new TransactionResult("Success", false, false, false);
    }

    /**
     * Process the configuration to sell the commands(s).
     * @apiNote Prices are automatically added to the player as necessary.<br>
     * If the selling fails, the rest of the transaction will still proceed.
     * @param player The {@link Player} to process configuration for.
     * @param configuration The configuration to process.
     * @param amount The amount being sold.
     * @return A {@link TransactionResult}.
     */
    @Override
    public @NotNull TransactionResult sell(@NotNull Player player, @NotNull TransactionConfiguration configuration, int amount) {
        if(!(configuration instanceof CommandConfiguration commandConfiguration)) return new TransactionResult("Wrong Type", true, true, false);
        if(commandConfiguration.sellCommands().isEmpty()) return new TransactionResult("Not Configured", false, false, false);

        // Parse any placeholders in the commands
        List<String> parsedCommands = commandConfiguration.sellCommands().stream()
                .map(command -> PlaceholderAPIUtil.parsePlaceholders(player, command)).toList();

        // Execute the commands for this transaction
        Server server = skyShop.getServer();
        ConsoleCommandSender commandSender = server.getConsoleSender();
        for(String command : parsedCommands) {
            for(int i = 1; i <= amount; i++) {
                server.dispatchCommand(commandSender, command);
            }
        }

        return new TransactionResult("Success", false, false, false);
    }
}