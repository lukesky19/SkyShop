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
package com.github.lukesky19.skyshop.commands;

import com.github.lukesky19.skylib.api.adventure.AdventureUtil;
import com.github.lukesky19.skyshop.SkyShop;
import com.github.lukesky19.skyshop.SkyShopAPI;
import com.github.lukesky19.skyshop.commands.arguments.HelpCommand;
import com.github.lukesky19.skyshop.commands.arguments.ReloadCommand;
import com.github.lukesky19.skyshop.commands.arguments.SellAllCommand;
import com.github.lukesky19.skyshop.commands.arguments.StatsCommand;
import com.github.lukesky19.skyshop.configuration.*;
import com.github.lukesky19.skyshop.data.Locale;
import com.github.lukesky19.skyshop.data.gui.MenuConfig;
import com.github.lukesky19.skyshop.gui.GUIManager;
import com.github.lukesky19.skyshop.gui.MenuGUI;
import com.github.lukesky19.skyshop.manager.StatsManager;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

/**
 * This class is used to create the main skyshop command.
 */
public class SkyShopCommand {
    private final @NotNull SkyShop skyShop;
    private final @NotNull LocaleManager localeManager;
    private final @NotNull MenuManager menuManager;
    private final @NotNull ShopManager shopManager;
    private final @NotNull TransactionManager transactionManager;
    private final @NotNull SellAllManager sellAllManager;
    private final @Nullable StatsManager statsManager;
    private final @NotNull GUIManager guiManager;
    private final @NotNull SkyShopAPI skyShopAPI;

    /**
     * Constructor
     * @param skyShop A {@link SkyShop} instance
     * @param guiManager A {@link GUIManager} instance.
     * @param localeManager A {@link LocaleManager} instance.
     * @param menuManager A {@link MenuManager} instance.
     * @param shopManager A {@link ShopManager} instance.
     * @param transactionManager A {@link TransactionManager} instance.
     * @param sellAllManager A {@link SellAllManager} instance.
     * @param statsManager A {@link StatsManager} instance.
     * @param skyShopAPI A {@link SkyShopAPI} instance.
     */
    public SkyShopCommand(
            @NotNull SkyShop skyShop,
            @NotNull GUIManager guiManager,
            @NotNull LocaleManager localeManager,
            @NotNull MenuManager menuManager,
            @NotNull ShopManager shopManager,
            @NotNull TransactionManager transactionManager,
            @NotNull SellAllManager sellAllManager,
            @Nullable StatsManager statsManager,
            @NotNull SkyShopAPI skyShopAPI) {
        this.skyShop = skyShop;
        this.localeManager = localeManager;
        this.menuManager = menuManager;
        this.shopManager = shopManager;
        this.transactionManager = transactionManager;
        this.sellAllManager = sellAllManager;
        this.statsManager = statsManager;
        this.guiManager = guiManager;
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
            Locale locale = localeManager.getLocale();
            ComponentLogger logger = skyShop.getComponentLogger();

            if (ctx.getSource().getSender() instanceof Player player) {
                Optional<MenuConfig> optionalMenuConfig = menuManager.getMenuConfig();
                if(optionalMenuConfig.isPresent()) {
                    MenuConfig menuConfig = optionalMenuConfig.get();
                    MenuGUI menuGUI = new MenuGUI(skyShop, guiManager, player, localeManager, shopManager, transactionManager, sellAllManager, statsManager, skyShopAPI, menuConfig);

                    boolean creationResult = menuGUI.create();
                    if(!creationResult) {
                        logger.error(AdventureUtil.serialize("Unable to create the InventoryView for the menu GUI for player " + player.getName() + " due to a configuration error."));
                        player.sendMessage(AdventureUtil.serialize(locale.prefix() + locale.guiOpenError()));
                        return 0;
                    }

                    boolean updateResult = menuGUI.update();
                    if(!updateResult) {
                        logger.error(AdventureUtil.serialize("Unable to decorate the menu GUI for player " + player.getName() + " due to a configuration error."));
                        player.sendMessage(AdventureUtil.serialize(locale.prefix() + locale.guiOpenError()));
                        return 0;
                    }

                    boolean openResult = menuGUI.open();
                    if(!openResult) {
                        logger.error(AdventureUtil.serialize("Unable to open the menu GUI for player " + player.getName() + " due to a configuration error."));
                        player.sendMessage(AdventureUtil.serialize(locale.prefix() + locale.guiOpenError()));
                        return 0;
                    }

                    return 1;
                } else {
                    player.sendMessage(AdventureUtil.serialize(locale.prefix() + locale.guiOpenError()));

                    return 0;
                }
            } else {
                skyShop.getComponentLogger().info(AdventureUtil.serialize(locale.inGameOnly()));

                return 0;
            }
        });

        HelpCommand helpCommand = new HelpCommand(skyShop, localeManager);
        ReloadCommand reloadCommand = new ReloadCommand(skyShop, localeManager);
        SellAllCommand sellAllCommand = new SellAllCommand(skyShop, localeManager, guiManager, sellAllManager, skyShopAPI);
        StatsCommand statsCommand = new StatsCommand(skyShop, localeManager, guiManager, statsManager);

        builder.then(helpCommand.createCommand());
        builder.then(reloadCommand.createCommand());
        builder.then(sellAllCommand.createCommand());
        builder.then(statsCommand.createCommand());

        return builder.build();
    }
}
