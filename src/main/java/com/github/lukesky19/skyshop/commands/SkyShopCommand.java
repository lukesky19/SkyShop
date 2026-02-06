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
package com.github.lukesky19.skyshop.commands;

import com.github.lukesky19.skylib.api.adventure.AdventureUtil;
import com.github.lukesky19.skylib.api.gui.impl.UUIDGUIManager;
import com.github.lukesky19.skyshop.SkyShop;
import com.github.lukesky19.skyshop.api.SkyShopAPI;
import com.github.lukesky19.skyshop.commands.arguments.*;
import com.github.lukesky19.skyshop.configuration.category.CategoryConfigManager;
import com.github.lukesky19.skyshop.configuration.category.data.CategoryConfigV4;
import com.github.lukesky19.skyshop.configuration.locale.LocaleManager;
import com.github.lukesky19.skyshop.configuration.locale.data.LocaleV5;
import com.github.lukesky19.skyshop.configuration.sellall.SellAllManager;
import com.github.lukesky19.skyshop.configuration.transaction.TransactionGUIConfigManager;
import com.github.lukesky19.skyshop.gui.CategoryGUI;
import com.github.lukesky19.skyshop.hook.HookManager;
import com.github.lukesky19.skyshop.player.PlayerDataManager;
import com.github.lukesky19.skyshop.player.data.PlayerData;
import com.github.lukesky19.skyshop.registry.RegistryManager;
import com.github.lukesky19.skyshop.stats.StatsManager;
import com.github.lukesky19.skyshop.transaction.TransactionManager;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * This class is used to create the main skyshop command.
 */
public class SkyShopCommand {
    private final @NotNull SkyShop skyShop;
    private final @NotNull LocaleManager localeManager;
    private final @NotNull CategoryConfigManager categoryConfigManager;
    private final @NotNull TransactionGUIConfigManager transactionStyleConfigManager;
    private final @NotNull RegistryManager registryManager;
    private final @NotNull SellAllManager sellAllManager;
    private final @Nullable StatsManager statsManager;
    private final @NotNull HookManager hookManager;
    private final @NotNull PlayerDataManager playerDataManager;
    private final @NotNull TransactionManager transactionManager;
    private final @NotNull UUIDGUIManager guiManager;
    private final @NotNull SkyShopAPI skyShopAPI;

    /**
     * Constructor
     * @param skyShop A {@link SkyShop} instance
     * @param guiManager A {@link UUIDGUIManager} instance.
     * @param localeManager A {@link LocaleManager} instance.
     * @param categoryConfigManager A {@link CategoryConfigManager} instance.
     * @param transactionStyleConfigManager A {@link TransactionGUIConfigManager} instance.
     * @param registryManager A {@link RegistryManager} instance.
     * @param sellAllManager A {@link SellAllManager} instance.
     * @param statsManager A {@link StatsManager} instance.
     * @param hookManager A {@link HookManager} instance.
     * @param playerDataManager A {@link PlayerDataManager} instance.
     * @param transactionManager A {@link TransactionManager} instance.
     * @param skyShopAPI A {@link SkyShopAPI} instance.
     */
    public SkyShopCommand(
            @NotNull SkyShop skyShop,
            @NotNull UUIDGUIManager guiManager,
            @NotNull LocaleManager localeManager,
            @NotNull CategoryConfigManager categoryConfigManager,
            @NotNull TransactionGUIConfigManager transactionStyleConfigManager,
            @NotNull RegistryManager registryManager,
            @NotNull SellAllManager sellAllManager,
            @Nullable StatsManager statsManager,
            @NotNull HookManager hookManager,
            @NotNull PlayerDataManager playerDataManager,
            @NotNull TransactionManager transactionManager,
            @NotNull SkyShopAPI skyShopAPI) {
        this.skyShop = skyShop;
        this.localeManager = localeManager;
        this.categoryConfigManager = categoryConfigManager;
        this.transactionStyleConfigManager = transactionStyleConfigManager;
        this.registryManager = registryManager;
        this.sellAllManager = sellAllManager;
        this.statsManager = statsManager;
        this.guiManager = guiManager;
        this.hookManager = hookManager;
        this.playerDataManager = playerDataManager;
        this.transactionManager = transactionManager;
        this.skyShopAPI = skyShopAPI;
    }

    /**
     * Builds a {@link LiteralCommandNode} of type {@link CommandSourceStack} for the skyshop command.
     * @return A {@link LiteralCommandNode} of type {@link CommandSourceStack} representing the skyshop command.
     */
    public @NotNull LiteralCommandNode<CommandSourceStack> createCommand() {
        LiteralArgumentBuilder<CommandSourceStack> builder = Commands.literal("skyshop");
        builder.requires(ctx -> ctx.getSender().hasPermission("skyshop.commands.skyshop"));
        builder.executes(ctx -> {
            LocaleV5 locale = localeManager.getConfiguration();
            ComponentLogger logger = skyShop.getComponentLogger();

            if(ctx.getSource().getSender() instanceof Player player) {
                @Nullable PlayerData playerData = playerDataManager.getPlayerData(player.getUniqueId());
                if(playerData == null) {
                    logger.error(AdventureUtil.deserialize("Unable to create the InventoryView for the menu GUI for player " + player.getName() + " due to invalid player data."));
                    player.sendMessage(AdventureUtil.deserialize(locale.prefix() + locale.guiOpenError()));
                    return 0;
                }

                CategoryConfigV4 menuConfig = categoryConfigManager.getConfiguration("menu");
                if(menuConfig != null) {
                    CategoryGUI menuGUI = new CategoryGUI(skyShop, guiManager, player, playerData, localeManager, categoryConfigManager, transactionStyleConfigManager, registryManager, sellAllManager, statsManager, hookManager, transactionManager, skyShopAPI, null, menuConfig, "menu");

                    boolean creationResult = menuGUI.create();
                    if(!creationResult) {
                        logger.error(AdventureUtil.deserialize("Unable to create the InventoryView for the menu GUI for player " + player.getName() + " due to a configuration error."));
                        player.sendMessage(AdventureUtil.deserialize(locale.prefix() + locale.guiOpenError()));
                        return 0;
                    }

                    boolean updateResult = menuGUI.update();
                    if(!updateResult) {
                        logger.error(AdventureUtil.deserialize("Unable to decorate the menu GUI for player " + player.getName() + " due to a configuration error."));
                        player.sendMessage(AdventureUtil.deserialize(locale.prefix() + locale.guiOpenError()));
                        return 0;
                    }

                    boolean openResult = menuGUI.open();
                    if(!openResult) {
                        logger.error(AdventureUtil.deserialize("Unable to open the menu GUI for player " + player.getName() + " due to a configuration error."));
                        player.sendMessage(AdventureUtil.deserialize(locale.prefix() + locale.guiOpenError()));
                        return 0;
                    }

                    return 1;
                } else {
                    player.sendMessage(AdventureUtil.deserialize(locale.prefix() + locale.guiOpenError()));

                    return 0;
                }
            } else {
                skyShop.getComponentLogger().info(AdventureUtil.deserialize(locale.inGameOnly()));

                return 0;
            }
        });

        HelpCommand helpCommand = new HelpCommand(skyShop, localeManager);
        OpenCommand openCommand = new OpenCommand(skyShop, guiManager, localeManager, categoryConfigManager, transactionStyleConfigManager, registryManager, sellAllManager, statsManager, hookManager, playerDataManager, transactionManager, skyShopAPI);
        ReloadCommand reloadCommand = new ReloadCommand(skyShop, localeManager);
        SellAllCommand sellAllCommand = new SellAllCommand(skyShop, localeManager, guiManager, sellAllManager, skyShopAPI);
        StatsCommand statsCommand = new StatsCommand(skyShop, localeManager, guiManager, statsManager);

        builder.then(helpCommand.createCommand());
        builder.then(openCommand.createCommand());
        builder.then(reloadCommand.createCommand());
        builder.then(sellAllCommand.createCommand());
        builder.then(statsCommand.createCommand());

        return builder.build();
    }
}
