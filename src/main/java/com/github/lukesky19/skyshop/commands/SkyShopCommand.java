/*
    SkyShop is a simple inventory based shop plugin with page support, error checking, and configuration validation.
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

import com.github.lukesky19.skyshop.SkyShop;
import com.github.lukesky19.skyshop.configuration.locale.LocaleManager;
import com.github.lukesky19.skyshop.configuration.menu.MenuConfiguration;
import com.github.lukesky19.skyshop.configuration.menu.MenuManager;
import com.github.lukesky19.skyshop.configuration.shop.ShopManager;
import com.github.lukesky19.skyshop.gui.MenuGUI;
import com.github.lukesky19.skyshop.gui.SellAllGUI;
import com.github.lukesky19.skyshop.util.gui.InventoryManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
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
import java.util.Map;

/**
 * This class manages everything related to handling the skyshop command.
*/
public class SkyShopCommand implements CommandExecutor, TabCompleter {
    final SkyShop skyShop;
    final MenuManager menuManager;
    final ShopManager shopManager;
    final LocaleManager localeManager;
    final InventoryManager inventoryManager;

    /**
     * Constructor
     * @param skyShop The plugin instance.
     * @param menuManager The menu manager instance.
     * @param shopManager The shop manager instance.
     * @param localeManager The locale manager instance.
     * @param inventoryManager The inventory manager instance.
     */
    public SkyShopCommand(
            SkyShop skyShop,
            MenuManager menuManager,
            ShopManager shopManager,
            LocaleManager localeManager,
            InventoryManager inventoryManager) {
        this.skyShop = skyShop;
        this.menuManager = menuManager;
        this.shopManager = shopManager;
        this.inventoryManager = inventoryManager;
        this.localeManager = localeManager;
    }

    /**
     * Contains all the logic for the skyshop/shop command.
     * Includes reloading the plugin, the help message, opening the shop, and opening the sellall inventory.
     * @param commandSender The sender of the command.
     * @param command The actual command object. (Not Used)
     * @param label The alias of the sent command. (Not Used)
     * @param args Any arguments with the sent command.
     * @return true if succeeds, false if not.
     */
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        switch(args.length) {
            case 0 -> {
                if(commandSender instanceof Player player) {
                    if(commandSender.hasPermission("skyshop.commands.shop")) {
                        if(!skyShop.isPluginEnabled()) {
                            commandSender.sendMessage(MiniMessage.miniMessage().deserialize("<gray>[</gray> <aqua>SkyShop <gray>]</gray> <red>The plugin is currently is soft-disabled due to a configuration error.</red>"));
                            commandSender.sendMessage(MiniMessage.miniMessage().deserialize("<gray>[</gray> <aqua>SkyShop</aqua> <gray>]</gray> <red>Please report this to your server's system administrator.</red>"));
                            return false;
                        }

                        List<Map.Entry<String, MenuConfiguration.MenuPage>> pageList = menuManager.getMenuConfiguration().pages().entrySet().stream().toList();
                        Map.Entry<String, MenuConfiguration.MenuPage> entry = pageList.get(0);

                        inventoryManager.openGUI(new MenuGUI(skyShop, menuManager, shopManager, inventoryManager, localeManager, entry, 0), player);
                        return true;
                    }
                } else {
                    if(!skyShop.isPluginEnabled()) {
                        skyShop.getComponentLogger().warn(MiniMessage.miniMessage().deserialize("<gray>[</gray> <aqua>SkyShop <gray>]</gray> <red>The plugin has been soft-disabled due to a configuration error.</red>"));
                        skyShop.getComponentLogger().warn(MiniMessage.miniMessage().deserialize("<gray>[</gray> <aqua>SkyShop</aqua> <gray>]</gray> <red>Please check your server's console for more information.</red>"));
                        return false;
                    }

                    skyShop.getComponentLogger().info(localeManager.formattedLocale().inGameOnly());
                    return false;
                }
            }

            case 1 -> {
                switch(args[0].toLowerCase()) {
                    case "help" -> {
                        if(commandSender instanceof Player player) {
                            if(commandSender.hasPermission("skyshop.commands.help")) {
                                if(!skyShop.isPluginEnabled()) {
                                    player.sendMessage(MiniMessage.miniMessage().deserialize("<gray>[</gray> <aqua>SkyShop <gray>]</gray> <red>The plugin is currently is soft-disabled due to a configuration error.</red>"));
                                    player.sendMessage(MiniMessage.miniMessage().deserialize("<gray>[</gray> <aqua>SkyShop</aqua> <gray>]</gray> <red>Please report this to your server's system administrator.</red>"));
                                    return false;
                                }

                                for(Component msg : localeManager.formattedLocale().help()) {
                                    player.sendMessage(msg);
                                }
                                return true;
                            }
                        } else {
                            if(!skyShop.isPluginEnabled()) {
                                skyShop.getComponentLogger().warn(MiniMessage.miniMessage().deserialize("<gray>[</gray> <aqua>SkyShop <gray>]</gray> <red>The plugin has been soft-disabled due to a configuration error.</red>"));
                                skyShop.getComponentLogger().warn(MiniMessage.miniMessage().deserialize("<gray>[</gray> <aqua>SkyShop</aqua> <gray>]</gray> <red>Please check your server's console for more information.</red>"));
                                return false;
                            }

                            for(Component msg : localeManager.formattedLocale().help()) {
                                skyShop.getComponentLogger().info(msg);
                            }
                            return true;
                        }
                    }

                    case "reload" -> {
                        if(commandSender instanceof Player player) {
                            if(player.hasPermission("skyshop.commands.reload")) {
                                skyShop.reload();
                                if(!skyShop.isPluginEnabled()) {
                                    commandSender.sendMessage(MiniMessage.miniMessage().deserialize("<gray>[</gray> <aqua>SkyShop <gray>]</gray> <red>The plugin has been soft-disabled due to a configuration error.</red>"));
                                    commandSender.sendMessage(MiniMessage.miniMessage().deserialize("<gray>[</gray> <aqua>SkyShop</aqua> <gray>]</gray> <red>Please check your server's console for more information.</red>"));
                                    return false;
                                }

                                player.sendMessage(localeManager.formattedLocale().prefix().append(localeManager.formattedLocale().configReload()));
                                return true;
                            }
                        } else {
                            skyShop.reload();
                            if(!skyShop.isPluginEnabled()) {
                                skyShop.getComponentLogger().warn(MiniMessage.miniMessage().deserialize("<gray>[</gray> <aqua>SkyShop <gray>]</gray> <red>The plugin has been soft-disabled due to a configuration error.</red>"));
                                skyShop.getComponentLogger().warn(MiniMessage.miniMessage().deserialize("<gray>[</gray> <aqua>SkyShop</aqua> <gray>]</gray> <red>Please check your server's console for more information.</red>"));
                                return false;
                            }

                            skyShop.getComponentLogger().info(localeManager.formattedLocale().prefix().append(localeManager.formattedLocale().configReload()));
                            return true;
                        }
                    }

                    case "sellall" -> {
                        if(commandSender instanceof Player player) {
                            if (commandSender.hasPermission("skyshop.commands.sellall")) {
                                if(!skyShop.isPluginEnabled()) {
                                    commandSender.sendMessage(MiniMessage.miniMessage().deserialize("<gray>[</gray> <aqua>SkyShop <gray>]</gray> <red>The plugin is currently is soft-disabled due to a configuration error.</red>"));
                                    commandSender.sendMessage(MiniMessage.miniMessage().deserialize("<gray>[</gray> <aqua>SkyShop</aqua> <gray>]</gray> <red>Please report this to your server's system administrator.</red>"));
                                    return false;
                                }

                                inventoryManager.openGUI(new SellAllGUI(skyShop, menuManager, shopManager, localeManager), player);
                                return true;
                            }
                        } else {
                            if(!skyShop.isPluginEnabled()) {
                                skyShop.getComponentLogger().warn(MiniMessage.miniMessage().deserialize("<gray>[</gray> <aqua>SkyShop <gray>]</gray> <red>The plugin has been soft-disabled due to a configuration error.</red>"));
                                skyShop.getComponentLogger().warn(MiniMessage.miniMessage().deserialize("<gray>[</gray> <aqua>SkyShop</aqua> <gray>]</gray> <red>Please check your server's console for more information.</red>"));
                                return false;
                            }

                            skyShop.getComponentLogger().info(localeManager.formattedLocale().inGameOnly());
                        }
                    }

                    default -> {
                        if(commandSender instanceof Player player) {
                            if(!skyShop.isPluginEnabled()) {
                                player.sendMessage(MiniMessage.miniMessage().deserialize("<gray>[</gray> <aqua>SkyShop <gray>]</gray> <red>The plugin is currently is soft-disabled due to a configuration error.</red>"));
                                player.sendMessage(MiniMessage.miniMessage().deserialize("<gray>[</gray> <aqua>SkyShop</aqua> <gray>]</gray> <red>Please report this to your server's system administrator.</red>"));
                                return false;
                            }

                            player.sendMessage(localeManager.formattedLocale().unknownArgument());
                        } else {
                            if(!skyShop.isPluginEnabled()) {
                                skyShop.getComponentLogger().warn(MiniMessage.miniMessage().deserialize("<gray>[</gray> <aqua>SkyShop <gray>]</gray> <red>The plugin has been soft-disabled due to a configuration error.</red>"));
                                skyShop.getComponentLogger().warn(MiniMessage.miniMessage().deserialize("<gray>[</gray> <aqua>SkyShop</aqua> <gray>]</gray> <red>Please check your server's console for more information.</red>"));
                                return false;
                            }

                            skyShop.getComponentLogger().info(localeManager.formattedLocale().unknownArgument());
                        }
                        return false;
                    }
                }
            }

            default -> {
                if(commandSender instanceof Player player) {
                    if(!skyShop.isPluginEnabled()) {
                        player.sendMessage(MiniMessage.miniMessage().deserialize("<gray>[</gray> <aqua>SkyShop <gray>]</gray> <red>The plugin is currently is soft-disabled due to a configuration error.</red>"));
                        player.sendMessage(MiniMessage.miniMessage().deserialize("<gray>[</gray> <aqua>SkyShop</aqua> <gray>]</gray> <red>Please report this to your server's system administrator.</red>"));
                        return false;
                    }

                    player.sendMessage(localeManager.formattedLocale().unknownArgument());
                } else {
                    if(!skyShop.isPluginEnabled()) {
                        skyShop.getComponentLogger().warn(MiniMessage.miniMessage().deserialize("<gray>[</gray> <aqua>SkyShop <gray>]</gray> <red>The plugin has been soft-disabled due to a configuration error.</red>"));
                        skyShop.getComponentLogger().warn(MiniMessage.miniMessage().deserialize("<gray>[</gray> <aqua>SkyShop</aqua> <gray>]</gray> <red>Please check your server's console for more information.</red>"));
                        return false;
                    }

                    skyShop.getComponentLogger().info(localeManager.formattedLocale().unknownArgument());
                }
                return false;
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
                if(sender.hasPermission("skyshop.commands.help")) subCmds.add("help");
                if(sender.hasPermission("skyshop.commands.reload")) subCmds.add("reload");
                if(sender.hasPermission("skyshop.commands.sellall")) subCmds.add("sellall");
            } else {
                subCmds.add("help");
                subCmds.add("reload");
            }
            return subCmds;
        }
        return Collections.emptyList();
    }
}
