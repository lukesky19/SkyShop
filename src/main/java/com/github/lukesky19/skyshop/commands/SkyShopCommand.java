/*
    SkyShop is a simple inventory based shop plugin with page support, sell commands, and error checking.
    Copyright (C) 2024  lukeskywlker19

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

import com.github.lukesky19.skylib.format.FormatUtil;
import com.github.lukesky19.skyshop.SkyShop;
import com.github.lukesky19.skyshop.SkyShopAPI;
import com.github.lukesky19.skyshop.configuration.manager.*;
import com.github.lukesky19.skyshop.configuration.record.Locale;
import com.github.lukesky19.skyshop.gui.MenuGUI;
import com.github.lukesky19.skyshop.gui.SellAllGUI;
import com.github.lukesky19.skyshop.manager.StatsDatabaseManager;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import org.bukkit.entity.Player;

public class SkyShopCommand {
    private final SkyShop skyShop;
    private final MenuManager menuManager;
    private final ShopManager shopManager;
    private final LocaleManager localeManager;
    private final TransactionManager transactionManager;
    private final SellAllManager sellAllManager;
    private final StatsDatabaseManager statsDatabaseManager;
    private final SkyShopAPI skyShopAPI;

    /**
     * Constructor
     * @param skyShop The plugin instance.
     * @param menuManager A MenuManager instance.
     * @param shopManager A ShopManager instance.
     * @param localeManager A LocaleManager instance.
     * @param transactionManager A TransactionManager instance.
     * @param sellAllManager A SellAllManager instance.
     * @param skyShopAPI The SkyShopAPI
     */
    public SkyShopCommand(
            SkyShop skyShop,
            MenuManager menuManager,
            ShopManager shopManager,
            LocaleManager localeManager,
            TransactionManager transactionManager,
            SellAllManager sellAllManager,
            StatsDatabaseManager statsDatabaseManager,
            SkyShopAPI skyShopAPI) {
        this.skyShop = skyShop;
        this.menuManager = menuManager;
        this.shopManager = shopManager;
        this.localeManager = localeManager;
        this.transactionManager = transactionManager;
        this.sellAllManager = sellAllManager;
        this.statsDatabaseManager = statsDatabaseManager;
        this.skyShopAPI = skyShopAPI;
    }

    /**
     * Builds a {@literal LiteralCommandNode<CommandSourceStack>} for the/skyshop command to be registered through the Lifecycle API.
     * @return A {@literal LiteralCommandNode<CommandSourceStack>} representing the /skyshop command.
     */
    public LiteralCommandNode<CommandSourceStack> createCommand() {
        LiteralArgumentBuilder<CommandSourceStack> builder = Commands.literal("skyshop")
                .requires(ctx -> ctx.getSender().hasPermission("skyshop.commands.skyshop"))
                .executes(ctx -> {
                    Locale locale = localeManager.getLocale();

                    if (ctx.getSource().getSender() instanceof Player player) {
                        if(menuManager.getMenuConfig() != null) {
                            MenuGUI gui = new MenuGUI(skyShop, menuManager, shopManager, localeManager, transactionManager, statsDatabaseManager, skyShopAPI, sellAllManager, 0, player);

                            gui.openInventory(skyShop, player);

                            return 1;
                        } else {
                            player.sendMessage(FormatUtil.format(locale.prefix() + locale.guiOpenError()));

                            return 0;
                        }
                    } else {
                        skyShop.getComponentLogger().info(FormatUtil.format(locale.inGameOnly()));

                        return 0;
                    }
                });

        builder.then(Commands.literal("help")
                .requires(ctx -> ctx.getSender().hasPermission("skyshop.commands.skyshop.help"))
                .executes(ctx -> {
                    Locale locale = localeManager.getLocale();

                    if(ctx.getSource().getSender() instanceof Player player) {
                        for(String msg : locale.help()) {
                            player.sendMessage(FormatUtil.format(player, msg));
                        }
                    } else {
                        for(String msg : locale.help()) {
                            skyShop.getComponentLogger().info(FormatUtil.format(msg));
                        }
                    }

                    return 1;
                })
        );

        builder.then(Commands.literal("reload")
                .requires(ctx -> ctx.getSender().hasPermission("skyshop.commands.skyshop.reload"))
                .executes(ctx -> {
                    skyShop.reload();

                    Locale locale = localeManager.getLocale();

                    if(ctx.getSource().getSender() instanceof Player player) {
                        player.sendMessage(FormatUtil.format(player, locale.prefix() + locale.configReload()));
                    } else {
                        skyShop.getComponentLogger().info(FormatUtil.format(locale.configReload()));
                    }

                    return 1;
                })
        );

        builder.then(Commands.literal("sellall")
                .requires(ctx -> ctx.getSender().hasPermission("skyshop.commands.skyshop.sellall"))
                .executes(ctx -> {
                    Locale locale = localeManager.getLocale();

                    if (ctx.getSource().getSender() instanceof Player player) {
                        if(menuManager.getMenuConfig() != null) {
                            SellAllGUI gui = new SellAllGUI(skyShop, localeManager, sellAllManager, skyShopAPI, player);

                            gui.openInventory(skyShop, player);

                            return 1;
                        } else {
                            player.sendMessage(FormatUtil.format(locale.prefix() + locale.guiOpenError()));

                            return 0;
                        }
                    } else {
                        skyShop.getComponentLogger().info(FormatUtil.format(locale.inGameOnly()));

                        return 0;
                    }
                })
        );

        return builder.build();
    }
}
