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
package com.github.lukesky19.skyshop;

import com.github.lukesky19.skylib.format.FormatUtil;
import com.github.lukesky19.skylib.player.PlayerUtil;
import com.github.lukesky19.skyshop.configuration.manager.LocaleManager;
import com.github.lukesky19.skyshop.configuration.manager.ShopManager;
import com.github.lukesky19.skyshop.configuration.record.Locale;
import com.github.lukesky19.skyshop.manager.StatsDatabaseManager;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class SkyShopAPI {
    private final SkyShop skyShop;
    private final LocaleManager localeManager;
    private final ShopManager shopManager;
    private final StatsDatabaseManager statsDatabaseManager;

    public SkyShopAPI(
            SkyShop skyShop,
            LocaleManager localeManager,
            ShopManager shopManager,
            StatsDatabaseManager statsDatabaseManager) {
        this.skyShop = skyShop;
        this.localeManager = localeManager;
        this.shopManager = shopManager;
        this.statsDatabaseManager = statsDatabaseManager;
    }

    /**
     * Sells all possible items in an inventory GUI, if a sell price is configured for that item at least once.
     * Any remaining items that weren't sold will be returned to the player's inventory.
     * @param inventory An inventory containing items.
     * @param player The player selling items.
     */
    public void sellInventoryGUI(@NotNull Inventory inventory, Player player) {
        Locale locale = localeManager.getLocale();
        double money = 0.0;

        for(ItemStack item : inventory.getContents()) {
            if (item != null && item.getType() != Material.AIR) {
                Double price = shopManager.getMaterialSellPrice(item.getType());
                if (price != null && price > 0.0) {
                    money = money + (price * item.getAmount());
                    inventory.removeItem(item);

                    if (statsDatabaseManager != null) {
                        skyShop.getServer().getScheduler().runTaskAsynchronously(skyShop, () -> {
                            try {
                                statsDatabaseManager.updateMaterial(item.getType().toString(), 0, item.getAmount());
                            } catch (SQLException e) {
                                throw new RuntimeException(e);
                            }
                        });
                    }
                }
            }
        }

        boolean sent = false;
        if(!Arrays.stream(inventory.getContents()).toList().isEmpty()) {
            for(ItemStack item : inventory.getContents()) {
                if(item != null && item.getType() != Material.AIR) {
                    // Add the unsold item to the player's inventory.
                    PlayerUtil.giveItem(player, item, item.getAmount());

                    // Remove the item from the inventory GUI
                    inventory.removeItem(item);

                    // Only send the unsellable message once as to not spam the player
                    if(!sent) {
                        player.sendMessage(FormatUtil.format(player, locale.prefix() + locale.sellallUnsellable()));
                        sent = true;
                    }
                }
            }
        }

        if(money > 0.0) {
            skyShop.getEconomy().depositPlayer(player, money);

            List<TagResolver.Single> placeholders = new ArrayList<>();
            placeholders.add(Placeholder.parsed("price", String.valueOf(money)));
            placeholders.add(Placeholder.parsed("bal", String.valueOf(skyShop.getEconomy().getBalance(player))));

            player.sendMessage(FormatUtil.format(player, locale.prefix() + locale.sellallSuccess(), placeholders));
        }
    }

    /**
     * Sells all possible items inside a Player's inventory if a sell price is configured for that item at least once.
     * This will remove any and all items if the item type (Material) has a configured sell price that is > 0.0.
     * If an item has some custom data, the item may not be sold.
     * Any items that weren't sold will remain in player's inventory.
     * @param inventory The player's inventory containing items.
     * @param player The Player to pay for the items sold.
     */
    public void sellPlayerInventory(Inventory inventory, Player player) {
        Locale locale = localeManager.getLocale();
        double money = 0.0;

        for(ItemStack item : inventory.getContents()) {
            if (item != null && item.getType() != Material.AIR) {
                Double price = shopManager.getMaterialSellPrice(item.getType());
                if (price != null && price > 0.0) {
                    // This will remove the items before the total money is calculated, but it will return the final total at the end.
                    inventory.removeItemAnySlot(item);

                    money = money + (price * item.getAmount());

                    if (statsDatabaseManager != null) {
                        skyShop.getServer().getScheduler().runTaskAsynchronously(skyShop, () -> {
                            try {
                                statsDatabaseManager.updateMaterial(item.getType().toString(), 0, item.getAmount());
                            } catch (SQLException e) {
                                throw new RuntimeException(e);
                            }
                        });
                    }
                }
            }
        }

        if(money > 0.0) {
            skyShop.getEconomy().depositPlayer(player, money);

            List<TagResolver.Single> placeholders = new ArrayList<>();
            placeholders.add(Placeholder.parsed("price", String.valueOf(money)));
            placeholders.add(Placeholder.parsed("bal", String.valueOf(skyShop.getEconomy().getBalance(player))));

            player.sendMessage(FormatUtil.format(player, locale.prefix() + locale.sellallSuccess(), placeholders));
        }
    }

    /**
     * <p>Sells all possible items inside the given inventory if a sell price is configured for that item at least once.</p>
     * <p>This will remove any and all items if the item type (Material) has a configured sell price that is > 0.0.</p>
     * <p>If an item has some custom data, the item may not be sold.</p>
     * <p>Any remaining items that weren't sold will be left inside the inventory.</p>
     * <p>Do not use this method if the inventory is one created by a plugin (i.e., a GUI) or a player's inventory.</p>
     * <p>Use {@link #sellInventoryGUI(Inventory, Player)} for GUIs and {@link #sellPlayerInventory(Inventory, Player)} for player inventories.</p>
     * @param inventory The player's inventory containing items.
     * @param player The Player to pay for the items sold.
     */
    public void sellInventory(Player player, Inventory inventory) {
        double money = 0.0;

        for(ItemStack item : inventory.getContents()) {
            if (item != null && item.getType() != Material.AIR) {
                Double price = shopManager.getMaterialSellPrice(item.getType());
                if (price != null && price > 0.0) {
                    money = money + (price * item.getAmount());
                    inventory.removeItem(item);

                    if (statsDatabaseManager != null) {
                        skyShop.getServer().getScheduler().runTaskAsynchronously(skyShop, () -> {
                            try {
                                statsDatabaseManager.updateMaterial(item.getType().toString(), 0, item.getAmount());
                            } catch (SQLException e) {
                                throw new RuntimeException(e);
                            }
                        });
                    }
                }
            }
        }

        if(money > 0.0) {
            skyShop.getEconomy().depositPlayer(player, money);
        }
    }

    /**
     * <p>Sells the given ItemStack if a sell price is configured for that item at least once.</p>
     * <p>If the ItemStack could not be sold, it will be left inside the Inventory.</p>
     * @param itemStack The ItemStack to sell.
     * @param player The Player to pay for the items sold.
     */
    public void sellItemStack(Player player, Inventory inventory, ItemStack itemStack, int slot) {
        Locale locale = localeManager.getLocale();
        double money = 0.0;

        Double price = shopManager.getMaterialSellPrice(itemStack.getType());
        if (price != null && price > 0.0) {
            inventory.clear(slot);

            money = money + (price * itemStack.getAmount());

            if (statsDatabaseManager != null) {
                skyShop.getServer().getScheduler().runTaskAsynchronously(skyShop, () -> {
                    try {
                        statsDatabaseManager.updateMaterial(itemStack.getType().toString(), 0, itemStack.getAmount());
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                });
            }
        }  else {
            player.sendMessage(FormatUtil.format(player, locale.prefix() + locale.unsellable()));
            return;
        }

        if(money > 0.0) {
            skyShop.getEconomy().depositPlayer(player, money);

            List<TagResolver.Single> placeholders = new ArrayList<>();
            placeholders.add(Placeholder.parsed("price", String.valueOf(money)));
            placeholders.add(Placeholder.parsed("bal", String.valueOf(skyShop.getEconomy().getBalance(player))));

            player.sendMessage(FormatUtil.format(player, locale.prefix() + locale.sellallSuccess(), placeholders));
        }
    }

    /**
     * <p>Sells all possible items matching the given ItemStack inside the given inventory if a sell price is configured for that item at least once.</p>
     * <p>This will remove any and all items matching the type (Material) if it has a configured sell price that is > 0.0.</p>
     * <p>If an item has some custom data, the item may not be sold.</p>
     * <p>Any remaining items that weren't sold will be left inside the Inventory.</p>
     * @param player The Player to pay for the items sold.
     * @param inventory The Inventory to sell items for.
     * @param itemStack The ItemStack that matches the items being sold
     */
    public void sellAllMatchingItemStack(Player player, Inventory inventory, ItemStack itemStack) {
        Locale locale = localeManager.getLocale();
        double money = 0.0;

        List<ItemStack> itemStacks = Arrays.stream(player.getInventory().getContents())
                .filter(Objects::nonNull)
                .filter(item -> item.isSimilar(itemStack))
                .toList();

        for(ItemStack item : itemStacks) {
            if (item != null && item.getType() != Material.AIR) {
                Double price = shopManager.getMaterialSellPrice(item.getType());
                if (price != null && price > 0.0) {
                    // This will remove the items before the total money is calculated, but it will return the final total at the end.
                    inventory.removeItemAnySlot(item);
                    money = money + (price * item.getAmount());

                    if (statsDatabaseManager != null) {
                        skyShop.getServer().getScheduler().runTaskAsynchronously(skyShop, () -> {
                            try {
                                statsDatabaseManager.updateMaterial(item.getType().toString(), 0, item.getAmount());
                            } catch (SQLException e) {
                                throw new RuntimeException(e);
                            }
                        });
                    }
                } else {
                    player.sendMessage(FormatUtil.format(player, locale.prefix() + locale.unsellable()));
                    return;
                }
            }
        }

        if(money > 0.0) {
            skyShop.getEconomy().depositPlayer(player, money);

            List<TagResolver.Single> placeholders = new ArrayList<>();
            placeholders.add(Placeholder.parsed("price", String.valueOf(money)));
            placeholders.add(Placeholder.parsed("bal", String.valueOf(skyShop.getEconomy().getBalance(player))));

            player.sendMessage(FormatUtil.format(player, locale.prefix() + locale.sellallSuccess(), placeholders));
        }
    }
}