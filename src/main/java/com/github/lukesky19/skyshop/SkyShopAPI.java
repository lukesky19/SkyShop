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
import com.github.lukesky19.skyshop.event.ItemSoldEvent;
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
import java.util.List;

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
     * @param message Should the sell success or unsellable message from SkyShop be sent?
     */
    public boolean sellInventoryGUI(@NotNull Inventory inventory, Player player, boolean message) {
        Locale locale = localeManager.getLocale();
        double money = 0.0;

        boolean sent = false;
        for(int i = 0; i <= inventory.getSize() - 1; i++) {
            ItemStack item = inventory.getItem(i);

            if (item != null && item.getType() != Material.AIR) {
                Double price = shopManager.getMaterialSellPrice(item.getType());
                if (price != null && price > 0.0) {
                    ItemSoldEvent itemSoldEvent = new ItemSoldEvent(item);
                    skyShop.getServer().getPluginManager().callEvent(itemSoldEvent);
                    if (!itemSoldEvent.isCancelled()) {
                        inventory.clear(i);

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
                        // Give the player the item back
                        PlayerUtil.giveItem(player.getInventory(), item, item.getAmount(), player.getLocation());

                        // Remove the item from the inventory GUI
                        inventory.clear(i);
                    }
                } else {
                    // Give the player the item that cannot be sold back
                    PlayerUtil.giveItem(player.getInventory(), item, item.getAmount(), player.getLocation());

                    // Remove the item from the inventory GUI
                    inventory.clear(i);

                    // Only send the unsellable message once as to not spam the player
                    if(!sent) {
                        if(message) {
                            player.sendMessage(FormatUtil.format(player, locale.prefix() + locale.sellallUnsellable()));
                            sent = true;
                        }
                    }
                }
            }
        }

        if(money > 0.0) {
            skyShop.getEconomy().depositPlayer(player, money);

            if(message) {
                List<TagResolver.Single> placeholders = new ArrayList<>();
                placeholders.add(Placeholder.parsed("price", String.valueOf(money)));
                placeholders.add(Placeholder.parsed("bal", String.valueOf(skyShop.getEconomy().getBalance(player))));

                player.sendMessage(FormatUtil.format(player, locale.prefix() + locale.sellallSuccess(), placeholders));
            }

            return true;
        }

        return false;
    }

    /**
     * <p>Sells all possible items inside the given inventory if a sell price is configured for that item at least once.</p>
     * <p>This will remove any and all items if the item type (Material) has a configured sell price that is > 0.0.</p>
     * <p>If an item has some custom data, the item may not be sold.</p>
     * <p>Any remaining items that weren't sold will be left inside the inventory.</p>
     * <p>Do not use this method if the inventory is one created by a plugin (i.e., a GUI).</p>
     * <p>Use {@link #sellInventoryGUI(Inventory, Player, boolean)} for GUIs.</p>
     * <p>Use {@link #sellPlayerInventory(Player, Inventory, boolean)} for Player Inventories.</p>
     * @param inventory The player's inventory containing items.
     * @param player The Player to pay for the items sold.
     * @param message Should the sell success message from SkyShop be sent?
     * @return true if at least one item was sold, else false
     */
    public boolean sellInventory(Player player, Inventory inventory, boolean message) {
        Locale locale = localeManager.getLocale();
        double money = 0.0;

        for(int i = 0; i <= inventory.getSize() - 1; i++) {
            ItemStack item = inventory.getItem(i);

            if (item != null && item.getType() != Material.AIR) {
                Double price = shopManager.getMaterialSellPrice(item.getType());
                if (price != null && price > 0.0) {
                    ItemSoldEvent itemSoldEvent = new ItemSoldEvent(item);
                    skyShop.getServer().getPluginManager().callEvent(itemSoldEvent);

                    if (!itemSoldEvent.isCancelled()) {
                        inventory.clear(i);

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
        }

        if(money > 0.0) {
            skyShop.getEconomy().depositPlayer(player, money);

            if(message) {
                List<TagResolver.Single> placeholders = new ArrayList<>();
                placeholders.add(Placeholder.parsed("price", String.valueOf(money)));
                placeholders.add(Placeholder.parsed("bal", String.valueOf(skyShop.getEconomy().getBalance(player))));

                player.sendMessage(FormatUtil.format(player, locale.prefix() + locale.sellallSuccess(), placeholders));
            }

            return true;
        }

        return false;
    }

    /**
     * <p>Sells all possible items inside the given inventory if a sell price is configured for that item at least once.</p>
     * <p>This will remove any and all items if the item type (Material) has a configured sell price that is > 0.0.</p>
     * <p>If an item has some custom data, the item may not be sold.</p>
     * <p>Any remaining items that weren't sold will be left inside the inventory.</p>
     * <p>This method will ignore armor slots. You can use {@link #sellInventory(Player, Inventory, boolean)} for a method that sells armor slots.</p>
     * <p>Do not use this method if the inventory is one created by a plugin (i.e., a GUI).</p>
     * <p>Use {@link #sellInventoryGUI(Inventory, Player, boolean)} for GUIs.</p>
     * <p>Use {@link #sellInventory(Player, Inventory, boolean)} for any other Inventories.</p>
     * @param inventory The player's inventory containing items.
     * @param player The Player to pay for the items sold.
     * @param message Should the sell success message from SkyShop be sent?
     * @return true if at least one item was sold, else false
     */
    public boolean sellPlayerInventory(Player player, Inventory inventory, boolean message) {
        Locale locale = localeManager.getLocale();
        double money = 0.0;

        for(int i = 0; i <= inventory.getSize() - 1; i++) {
            // Ignore armor slots
            if(i >= 36 && i <= 39) continue;

            ItemStack item = inventory.getItem(i);

            if (item != null && item.getType() != Material.AIR) {
                Double price = shopManager.getMaterialSellPrice(item.getType());
                if (price != null && price > 0.0) {
                    ItemSoldEvent itemSoldEvent = new ItemSoldEvent(item);
                    skyShop.getServer().getPluginManager().callEvent(itemSoldEvent);

                    if (!itemSoldEvent.isCancelled()) {
                        inventory.clear(i);

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
        }

        if(money > 0.0) {
            skyShop.getEconomy().depositPlayer(player, money);

            if(message) {
                List<TagResolver.Single> placeholders = new ArrayList<>();
                placeholders.add(Placeholder.parsed("price", String.valueOf(money)));
                placeholders.add(Placeholder.parsed("bal", String.valueOf(skyShop.getEconomy().getBalance(player))));

                player.sendMessage(FormatUtil.format(player, locale.prefix() + locale.sellallSuccess(), placeholders));
            }

            return true;
        }

        return false;
    }

    /**
     * <p>Sells the given ItemStack in the player's inventory if a sell price is configured for that item at least once.</p>
     * <p>If the ItemStack could not be sold, it will be left inside the Inventory.</p>
     * @param itemStack The ItemStack to sell.
     * @param player The Player to pay for the items sold.
     * @param message Should the sell success or unsellable message from SkyShop be sent?
     */
    public boolean sellItemStack(Player player, ItemStack itemStack, int slot, boolean message) {
        Locale locale = localeManager.getLocale();
        Inventory inventory = player.getInventory();
        Double price = shopManager.getMaterialSellPrice(itemStack.getType());

        if (price != null && price > 0.0) {
            ItemSoldEvent itemSoldEvent = new ItemSoldEvent(itemStack);
            skyShop.getServer().getPluginManager().callEvent(itemSoldEvent);

            if (!itemSoldEvent.isCancelled()) {
                inventory.clear(slot);

                double money = price * itemStack.getAmount();

                skyShop.getEconomy().depositPlayer(player, money);

                if(message) {
                    List<TagResolver.Single> placeholders = new ArrayList<>();
                    placeholders.add(Placeholder.parsed("price", String.valueOf(money)));
                    placeholders.add(Placeholder.parsed("bal", String.valueOf(skyShop.getEconomy().getBalance(player))));

                    player.sendMessage(FormatUtil.format(player, locale.prefix() + locale.sellallSuccess(), placeholders));
                }

                if (statsDatabaseManager != null) {
                    skyShop.getServer().getScheduler().runTaskAsynchronously(skyShop, () -> {
                        try {
                            statsDatabaseManager.updateMaterial(itemStack.getType().toString(), 0, itemStack.getAmount());
                        } catch (SQLException e) {
                            throw new RuntimeException(e);
                        }
                    });
                }

                return true;
            }
        } else {
            if(message) {
                player.sendMessage(FormatUtil.format(player, locale.prefix() + locale.unsellable()));
            }
        }

        return false;
    }

    /**
     * <p>Sells all possible items matching the given ItemStack inside the player's inventory if a sell price is configured for that item at least once.</p>
     * <p>This will remove any and all items matching the type (Material) if it has a configured sell price that is > 0.0.</p>
     * <p>If an item has some custom data, the item may not be sold.</p>
     * <p>Any remaining items that weren't sold will be left inside the Inventory.</p>
     * @param player The Player to pay for the items sold.
     * @param itemStack The ItemStack that matches the items being sold
     * @param message Should the sell success or unsellable message from SkyShop be sent?
     * @return true if at least one item was sold, else false
     */
    public boolean sellAllMatchingItemStack(Player player, ItemStack itemStack, boolean message) {
        Locale locale = localeManager.getLocale();
        Inventory inventory = player.getInventory();
        Double price = shopManager.getMaterialSellPrice(itemStack.getType());
        double money = 0.0;

        if (price == null || price <= 0.0) {
            if (message) {
                player.sendMessage(FormatUtil.format(player, locale.prefix() + locale.unsellable()));
            }

            return false;
        }

        for(int i = 0; i <= inventory.getSize() - 1; i++) {
            // Ignore armor slots
            if(i >= 36 && i <= 39) continue;

            ItemStack inventoryItem = inventory.getItem(i);

            if (inventoryItem != null && inventoryItem.getType() != Material.AIR) {
                if(inventoryItem.getType().equals(itemStack.getType())) {
                    ItemSoldEvent itemSoldEvent = new ItemSoldEvent(inventoryItem);
                    skyShop.getServer().getPluginManager().callEvent(itemSoldEvent);

                    if (!itemSoldEvent.isCancelled()) {
                        inventory.clear(i);

                        money = money + (price * inventoryItem.getAmount());

                        if (statsDatabaseManager != null) {
                            skyShop.getServer().getScheduler().runTaskAsynchronously(skyShop, () -> {
                                try {
                                    statsDatabaseManager.updateMaterial(inventoryItem.getType().toString(), 0, inventoryItem.getAmount());
                                } catch (SQLException e) {
                                    throw new RuntimeException(e);
                                }
                            });
                        }
                    }
                }
            }
        }

        if(money > 0.0) {
            skyShop.getEconomy().depositPlayer(player, money);

            if(message) {
                List<TagResolver.Single> placeholders = new ArrayList<>();
                placeholders.add(Placeholder.parsed("price", String.valueOf(money)));
                placeholders.add(Placeholder.parsed("bal", String.valueOf(skyShop.getEconomy().getBalance(player))));

                player.sendMessage(FormatUtil.format(player, locale.prefix() + locale.sellallSuccess(), placeholders));
            }

            return true;
        }

        return false;
    }
}