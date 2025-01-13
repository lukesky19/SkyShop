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
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * This class manages everything related to handling the skyshop command.
*/
public class SkyShopCommand implements CommandExecutor, TabCompleter {
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
     * Contains all the logic for the skyshop/shop command.
     * Includes reloading the plugin, the help message, opening the shop, and opening the sellall inventory.
     * @param sender The sender of the command.
     * @param command The actual command object. (Not Used)
     * @param label The alias of the sent command. (Not Used)
     * @param args Any arguments with the sent command.
     * @return true if succeeds, false if not.
     */
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        Locale locale = localeManager.getLocale();

        if(sender instanceof Player player) {
            if(player.hasPermission("skyshop.commands.skyshop")) {
                switch(args.length) {
                    case 0 -> {
                        if(sender.hasPermission("skyshop.commands.skyshop.shop")) {
                            if(menuManager.getMenuConfig() != null) {
                                MenuGUI gui = new MenuGUI(skyShop, menuManager, shopManager, localeManager, transactionManager, statsDatabaseManager, skyShopAPI, sellAllManager, 0, player);
                                gui.openInventory(skyShop, player);
                                return true;
                            } else {
                                player.sendMessage(FormatUtil.format(locale.prefix() + locale.guiOpenError()));

                                return false;
                            }
                        } else {
                            sender.sendMessage(FormatUtil.format(player, locale.prefix() + locale.noPermission()));
                            return false;
                        }
                    }

                    case 1 -> {
                        switch(args[0].toLowerCase()) {
                            case "help" -> {
                                if(sender.hasPermission("skyshop.commands.skyshop.help")) {
                                    for(String msg : locale.help()) {
                                        player.sendMessage(FormatUtil.format(player, msg));
                                    }
                                    return true;
                                } else {
                                    sender.sendMessage(FormatUtil.format(player, locale.prefix() + locale.noPermission()));
                                    return false;
                                }
                            }

                            case "reload" -> {
                                if(player.hasPermission("skyshop.commands.skyshop.reload")) {
                                    skyShop.reload();
                                    sender.sendMessage(FormatUtil.format(player, locale.prefix() + locale.configReload()));
                                    return true;
                                } else {
                                    sender.sendMessage(FormatUtil.format(player, locale.prefix() + locale.noPermission()));
                                    return false;
                                }
                            }

                            case "sellall" -> {
                                if(sender.hasPermission("skyshop.commands.skyshop.sellall")) {
                                    SellAllGUI gui = new SellAllGUI(skyShop, localeManager, sellAllManager, skyShopAPI, player);
                                    gui.openInventory(skyShop, player);
                                    return true;
                                } else {
                                    sender.sendMessage(FormatUtil.format(player, locale.prefix() + locale.noPermission()));
                                    return false;
                                }
                            }

                            default -> {
                                player.sendMessage(FormatUtil.format(player,locale.prefix() + locale.unknownArgument()));

                                return false;
                            }
                        }
                    }

                    default -> {
                        player.sendMessage(FormatUtil.format(player,locale.prefix() + locale.unknownArgument()));

                        return false;
                    }
                }
            }
        } else {
            ComponentLogger logger = skyShop.getComponentLogger();

            switch(args.length) {
                case 0 -> {
                    logger.info(FormatUtil.format(locale.inGameOnly()));

                    return false;
                }

                case 1 -> {
                    switch (args[0].toLowerCase()) {
                        case "help" -> {
                            locale.help().forEach(msg -> logger.info(FormatUtil.format(msg)));

                            return true;
                        }

                        case "reload" -> {
                            logger.info(FormatUtil.format(locale.configReload()));

                            return true;
                        }

                         case "sellall" -> {
                             logger.info(FormatUtil.format(locale.inGameOnly()));

                             return false;
                         }

                        default -> {
                            logger.info(FormatUtil.format(locale.unknownArgument()));

                            return false;
                        }
                    }
                }

                default -> {
                    logger.info(FormatUtil.format(locale.unknownArgument()));

                    return false;
                }
            }
        }

        return false;
    }

    /**
     * The tab completion logic for the skyshop/shop command.
     * @param sender The sender of the command.
     * @param command The command object. (Not Used)
     * @param label The command label. (Not Used)
     * @param args The arguments sent with the command.
     * @return A list of commands that can be tab-completed based on permissions and sender.
     */
    @Nullable
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if(args.length == 1) {
            ArrayList<String> subCmds = new ArrayList<>();
            if(sender instanceof Player) {
                if(sender.hasPermission("skyshop.commands.skyshop.help")) subCmds.add("help");
                if(sender.hasPermission("skyshop.commands.skyshop.reload")) subCmds.add("reload");
                if(sender.hasPermission("skyshop.commands.skyshop.sellall")) subCmds.add("sellall");
            } else {
                subCmds.add("help");
                subCmds.add("reload");
            }
            return subCmds;
        }

        return Collections.emptyList();
    }
}
