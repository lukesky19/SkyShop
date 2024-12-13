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
import com.github.lukesky19.skyshop.configuration.manager.LocaleManager;
import com.github.lukesky19.skyshop.configuration.record.Locale;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class SellCommand implements CommandExecutor, TabExecutor {
    private final SkyShop skyShop;
    private final LocaleManager localeManager;
    private final SkyShopAPI skyShopAPI;

    public SellCommand(SkyShop skyShop, LocaleManager localeManager, SkyShopAPI skyShopAPI) {
        this.skyShop = skyShop;
        this.localeManager = localeManager;
        this.skyShopAPI = skyShopAPI;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        Locale locale = localeManager.getLocale();

        if(sender instanceof Player player) {
            if(player.hasPermission("skyshop.commands.sell")) {
                switch (args.length) {
                    case 0 -> {
                        if (sender.hasPermission("skyshop.commands.sell.all")) {
                            skyShopAPI.sellInventory(player, player.getInventory(), true);
                            return true;
                        } else {
                            sender.sendMessage(FormatUtil.format(player, locale.prefix() + locale.noPermission()));
                            return false;
                        }
                    }

                    case 1 -> {
                        switch (args[0].toLowerCase()) {
                            case "hand" -> {
                                if (sender.hasPermission("skyshop.commands.sell.hand")) {
                                    ItemStack itemStack = player.getInventory().getItemInMainHand();
                                    int slot = player.getInventory().getHeldItemSlot();

                                    if(!itemStack.isEmpty() && !itemStack.getType().equals(Material.AIR)) {
                                        skyShopAPI.sellItemStack(player, itemStack, slot, true);
                                        return true;
                                    }

                                } else {
                                    sender.sendMessage(FormatUtil.format(player, locale.prefix() + locale.noPermission()));
                                }

                                return false;
                            }

                            case "all" -> {
                                if (sender.hasPermission("skyshop.commands.sell.all")) {
                                    skyShopAPI.sellInventory(player, player.getInventory(), true);
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

                    case 2 -> {
                        if(args[0].equalsIgnoreCase("hand")) {
                            if (args[1].equalsIgnoreCase("all")) {
                                if (sender.hasPermission("skyshop.commands.sell.hand") && sender.hasPermission("skyshop.commands.sell.hand.all")) {
                                    ItemStack itemStack = player.getInventory().getItemInMainHand();
                                    if (!itemStack.isEmpty() && !itemStack.getType().equals(Material.AIR)) {
                                        skyShopAPI.sellAllMatchingItemStack(player, itemStack, true);
                                        return true;
                                    }
                                } else {
                                    sender.sendMessage(FormatUtil.format(player, locale.prefix() + locale.noPermission()));
                                    return false;
                                }
                            } else {
                                player.sendMessage(FormatUtil.format(player, locale.prefix() + locale.unknownArgument()));
                                return false;
                            }
                        } else {
                            player.sendMessage(FormatUtil.format(player, locale.prefix() + locale.unknownArgument()));
                            return false;
                        }
                    }

                    default -> {
                        player.sendMessage(FormatUtil.format(player,locale.prefix() + locale.unknownArgument()));
                        return false;
                    }
                }
            } else {
                sender.sendMessage(FormatUtil.format(player, locale.prefix() + locale.noPermission()));
                return false;
            }
        } else {
            skyShop.getComponentLogger().info(FormatUtil.format(locale.inGameOnly()));
            return false;
        }

        return false;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        ArrayList<String> subCmds = new ArrayList<>();

        if(sender instanceof Player) {
            switch (args.length) {
                case 1 -> {
                    if(sender.hasPermission("skyshop.commands.sell.hand")) subCmds.add("hand");
                    if(sender.hasPermission("skyshop.commands.sell.all")) subCmds.add("all");
                }

                case 2 -> {
                    if(sender.hasPermission("skyshop.commands.sell.hand.all")) subCmds.add("all");
                }
            }
        }

        return subCmds;
    }
}
