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
package com.github.lukesky19.skyshop;

import com.github.lukesky19.skylib.api.adventure.AdventureUtil;
import com.github.lukesky19.skylib.api.format.FormatUtil;
import com.github.lukesky19.skylib.api.player.PlayerUtil;
import com.github.lukesky19.skyshop.config.locale.Locale;
import com.github.lukesky19.skyshop.data.PriceCache;
import com.github.lukesky19.skyshop.event.ItemSoldEvent;
import com.github.lukesky19.skyshop.hook.impl.EconomyHook;
import com.github.lukesky19.skyshop.hook.impl.PlayerPointsHook;
import com.github.lukesky19.skyshop.manager.HookManager;
import com.github.lukesky19.skyshop.manager.PriceManager;
import com.github.lukesky19.skyshop.manager.StatsManager;
import com.github.lukesky19.skyshop.manager.config.LocaleManager;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ItemType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * This class provides methods to sell items using the priceConfig configured in SkyShop.
 */
public class SkyShopAPI {
    private final @NotNull SkyShop skyShop;
    private final @NotNull LocaleManager localeManager;
    private final @NotNull PriceManager priceManager;
    private final @Nullable StatsManager statsManager;
    private final @NotNull HookManager hookManager;

    /**
     * Constructor
     * @param skyShop A {@link SkyShop} instance.
     * @param localeManager A {@link LocaleManager} instance.
     * @param priceManager A {@link PriceManager} instance.
     * @param statsManager A {@link StatsManager} instance.
     * @param hookManager A {@link HookManager} instance.
     */
    public SkyShopAPI(
            @NotNull SkyShop skyShop,
            @NotNull LocaleManager localeManager,
            @NotNull PriceManager priceManager,
            @Nullable StatsManager statsManager,
            @NotNull HookManager hookManager) {
        this.skyShop = skyShop;
        this.localeManager = localeManager;
        this.priceManager = priceManager;
        this.statsManager = statsManager;
        this.hookManager = hookManager;
    }

    /**
     * Sell the {@link ItemStack} for the {@link Player} provided.
     * @param player The {@link Player} selling the item.
     * @param itemStack The {@link ItemStack} being sold.
     * @param slot The slot where the ItemStack is inside the player's inventory.
     * @param message Whether the player should be messaged on successful selling.
     * @return true if sold successfully, otherwise false.
     */
    public boolean sellItemStack(@NotNull Player player, @NotNull ItemStack itemStack, int slot, boolean message) {
        Locale locale = localeManager.getLocale();
        // Get the player's inventory
        Inventory inventory = player.getInventory();

        // Get the ItemStack's ItemType
        @Nullable ItemType itemType = itemStack.getType().asItemType();
        // If the ItemStack is empty (air or stack size of 0), return
        if(itemStack.isEmpty()) return false;
        // If the ItemType is null, return
        if(itemType == null) return false;
        String transactionName = FormatUtil.formatItemTypeName(itemType);
        int stackSize = itemStack.getAmount();

        @Nullable PriceCache priceCache = priceManager.getCachedPrice(itemType);
        if(priceCache == null || (priceCache.money() == 0 && priceCache.points() == 0)) {
            if(message) {
                player.sendMessage(AdventureUtil.serialize(player, locale.prefix() + locale.unsellable()));
            }

            return false;
        }

        EconomyHook economyHook = hookManager.getHook(EconomyHook.class);
        PlayerPointsHook playerPointsHook = hookManager.getHook(PlayerPointsHook.class);

        double finalSellPrice = priceCache.money() * stackSize;
        int finalSellPoints = priceCache.points() * stackSize;

        if(finalSellPrice > 0.0 && finalSellPoints > 0) {
            ItemSoldEvent itemSoldEvent = new ItemSoldEvent(itemStack);
            skyShop.getServer().getPluginManager().callEvent(itemSoldEvent);
            if(itemSoldEvent.isCancelled()) return false;

            inventory.clear(slot);

            processSinglePaymentAndMessage(locale, economyHook, playerPointsHook, player, transactionName, stackSize, finalSellPrice, finalSellPoints, message);

            if(statsManager != null) statsManager.incrementAmountSold(itemType, itemStack.getAmount());

            return true;
        } else if(finalSellPrice > 0.0) {
            ItemSoldEvent itemSoldEvent = new ItemSoldEvent(itemStack);
            skyShop.getServer().getPluginManager().callEvent(itemSoldEvent);
            if(itemSoldEvent.isCancelled()) return false;

            inventory.clear(slot);

            processSinglePaymentAndMessage(locale, economyHook, playerPointsHook, player, transactionName, stackSize, finalSellPrice, -1, message);

            if(statsManager != null) statsManager.incrementAmountSold(itemType, itemStack.getAmount());

            return true;
        } else if(finalSellPoints > 0) {
            ItemSoldEvent itemSoldEvent = new ItemSoldEvent(itemStack);
            skyShop.getServer().getPluginManager().callEvent(itemSoldEvent);
            if(itemSoldEvent.isCancelled()) return false;

            inventory.clear(slot);

            processSinglePaymentAndMessage(locale, economyHook, playerPointsHook, player, transactionName, stackSize, -1, finalSellPoints, message);

            if(statsManager != null) statsManager.incrementAmountSold(itemType, itemStack.getAmount());

            return true;
        } else {
            if(message) {
                player.sendMessage(AdventureUtil.serialize(player, locale.prefix() + locale.unsellable()));
            }

            return false;
        }
    }

    /**
     * Sell all the {@link ItemStack}s inside the provided {@link Inventory} for the {@link Player} provided.
     * @param player The {@link Player} selling the item.
     * @param sellInventory The {@link Inventory} being sold.
     * @param ignoreArmorSlots Whether armor slots should be ignored or not.
     * @param returnUnsoldToPlayer Whether any unsold items should be returned to the player or not.
     * @param message Whether the player should be messaged on successful selling.
     * @return true if sold successfully, otherwise false.
     */
    public boolean sellInventory(@NotNull Player player, @NotNull Inventory sellInventory, boolean ignoreArmorSlots, boolean returnUnsoldToPlayer, boolean message) {
        Locale locale = localeManager.getLocale();

        double[] totalPrices = getTotalPrices(locale, player, sellInventory, null, ignoreArmorSlots, returnUnsoldToPlayer, message);
        double money = totalPrices[0];
        int points = (int) totalPrices[1];
        if(money <= 0 && points <= 0) return false;

        EconomyHook economyHook = hookManager.getHook(EconomyHook.class);
        PlayerPointsHook playerPointsHook = hookManager.getHook(PlayerPointsHook.class);

        processBulkPaymentAndMessage(locale, economyHook, playerPointsHook, player, money, points, message);

        return true;
    }

    /**
     * Sell all the {@link ItemStack}s inside the provided {@link Inventory} for the {@link Player} provided that match the provided {@link ItemStack}.
     * @param player The {@link Player} selling the item.
     * @param sellInventory The {@link Inventory} being sold.
     * @param matchingStack The {@link ItemStack} to compare {@link ItemStack}s in the inventory to sell.
     * @param ignoreArmorSlots Whether armor slots should be ignored or not.
     * @param returnUnsoldToPlayer Whether any unsold items should be returned to the player or not.
     * @param message Whether the player should be messaged on successful selling.
     * @return true if sold successfully, otherwise false.
     */
    public boolean sellAllMatchingItemStack(
            @NotNull Player player,
            @NotNull Inventory sellInventory,
            @NotNull ItemStack matchingStack,
            boolean ignoreArmorSlots,
            boolean returnUnsoldToPlayer,
            boolean message) {
        Locale locale = localeManager.getLocale();

        if(matchingStack.isEmpty()) return false;
        ItemType matchingType = matchingStack.getType().asItemType();
        if(matchingType == null) return false;

        double[] totalPrices = getTotalPrices(locale, player, sellInventory, matchingType, ignoreArmorSlots, returnUnsoldToPlayer, message);
        double money = totalPrices[0];
        int points = (int) totalPrices[1];
        if(money <= 0 && points <= 0) return false;

        EconomyHook economyHook = hookManager.getHook(EconomyHook.class);
        PlayerPointsHook playerPointsHook = hookManager.getHook(PlayerPointsHook.class);

        processBulkPaymentAndMessage(locale, economyHook, playerPointsHook, player, money, points, message);

        return true;
    }

    /**
     * Get the total prices for all items in the provided {@link Inventory}.
     * @param locale The plugin's {@link Locale}.
     * @param player The {@link Player}.
     * @param sellInventory The {@link Inventory} being sold.
     * @param matchingType The {@link ItemType} to match or null to ignore matching.
     * @param ignoreArmorSlots Whether armor slots should be ignored or not.
     * @param returnUnsoldToPlayer Whether any unsold items should be returned to the player or not.
     * @param message Whether the player should be messaged on successful selling.
     * @return A double array where the first number is the money as a double and the 2nd number is the player points as an integer (cast to int).
     */
    private double[] getTotalPrices(
            @NotNull Locale locale,
            @NotNull Player player,
            @NotNull Inventory sellInventory,
            @Nullable ItemType matchingType,
            boolean ignoreArmorSlots,
            boolean returnUnsoldToPlayer,
            boolean message) {
        @NotNull Inventory playerInventory = player.getInventory();
        @NotNull Location playerLocation = player.getLocation();

        double money = 0.0;
        int points = 0;

        boolean sent = false;
        for(int i = 0; i < sellInventory.getSize(); i++) {
            if(ignoreArmorSlots && i >= 36 && i <= 39) continue;

            ItemStack invStack = sellInventory.getItem(i);
            if(invStack == null || invStack.isEmpty()) continue;
            ItemType invType = invStack.getType().asItemType();
            if(invType == null) continue;

            // Check if matchingType is provided and if it matches the invType
            if(matchingType != null && !invType.equals(matchingType)) {
                if(!sent && message) {
                    player.sendMessage(AdventureUtil.serialize(player, locale.prefix() + locale.sellallUnsellable()));
                    sent = true;
                }

                if(returnUnsoldToPlayer) {
                    // Remove the ItemStack from the source Inventory
                    sellInventory.clear(i);

                    // Return the unsold item to the player's inventory
                    PlayerUtil.giveItem(playerInventory, invStack, invStack.getAmount(), playerLocation);
                }

                continue;
            }

            int stackSize = invStack.getAmount();
            @Nullable PriceCache priceCache = priceManager.getCachedPrice(invType);
            if(priceCache == null) {
                if(!sent && message) {
                    player.sendMessage(AdventureUtil.serialize(player, locale.prefix() + locale.sellallUnsellable()));
                    sent = true;
                }

                if(returnUnsoldToPlayer) {
                    // Remove the ItemStack from the source Inventory
                    sellInventory.clear(i);

                    // Return the unsold item to the player's inventory
                    PlayerUtil.giveItem(playerInventory, invStack, invStack.getAmount(), playerLocation);
                }

                continue;
            }

            if(priceCache.money() > 0 && priceCache.points() > 0) {
                ItemSoldEvent itemSoldEvent = new ItemSoldEvent(invStack);
                skyShop.getServer().getPluginManager().callEvent(itemSoldEvent);
                if(itemSoldEvent.isCancelled()) continue;

                sellInventory.clear(i);

                money += priceCache.money() * stackSize;
                points += priceCache.points() * stackSize;
            } else if(priceCache.money() > 0) {
                ItemSoldEvent itemSoldEvent = new ItemSoldEvent(invStack);
                skyShop.getServer().getPluginManager().callEvent(itemSoldEvent);
                if(itemSoldEvent.isCancelled()) continue;

                sellInventory.clear(i);

                money += priceCache.money() * stackSize;
            } else if(priceCache.points() > 0) {
                ItemSoldEvent itemSoldEvent = new ItemSoldEvent(invStack);
                skyShop.getServer().getPluginManager().callEvent(itemSoldEvent);
                if(itemSoldEvent.isCancelled()) continue;

                sellInventory.clear(i);

                points += priceCache.points() * stackSize;
            } else {
                if(!sent && message) {
                    player.sendMessage(AdventureUtil.serialize(player, locale.prefix() + locale.sellallUnsellable()));
                    sent = true;
                }

                if(returnUnsoldToPlayer) {
                    // Remove the ItemStack from the source Inventory
                    sellInventory.clear(i);

                    // Return the unsold item to the player's inventory
                    PlayerUtil.giveItem(playerInventory, invStack, invStack.getAmount(), playerLocation);
                }
            }
        }

        return new double[]{money, points};
    }

    /**
     * Process a single payment and send the appropriate message.
     * @param locale The plugin's {@link Locale}.
     * @param economyHook The {@link EconomyHook}.
     * @param playerPointsHook The {@link PlayerPointsHook}.
     * @param player The {@link Player}.
     * @param money The money.
     * @param points The player points.
     * @param message Whether the player should be messaged on successful selling.
     */
    private void processSinglePaymentAndMessage(
            @NotNull Locale locale,
            @NotNull EconomyHook economyHook,
            @NotNull PlayerPointsHook playerPointsHook,
            @NotNull Player player,
            @NotNull String transactionName,
            int amount,
            double money,
            int points,
            boolean message) {
        if(money <= 0 && points <= 0) return;

        givePayment(economyHook, playerPointsHook, player, money, points);

        if(message) sendSingleItemSoldPlayerMessage(locale, economyHook, playerPointsHook, player, transactionName, amount, money, points);
    }

    /**
     * Process a bulk payment and send the appropriate message.
     * @param locale The plugin's {@link Locale}.
     * @param economyHook The {@link EconomyHook}.
     * @param playerPointsHook The {@link PlayerPointsHook}.
     * @param player The {@link Player}.
     * @param money The money.
     * @param points The player points.
     * @param message Whether the player should be messaged on successful selling.
     */
    private void processBulkPaymentAndMessage(
            @NotNull Locale locale,
            @NotNull EconomyHook economyHook,
            @NotNull PlayerPointsHook playerPointsHook,
            @NotNull Player player,
            double money,
            int points,
            boolean message) {
        givePayment(economyHook, playerPointsHook, player, money, points);

        if(message) sendSellAllPlayerMessage(locale, economyHook, playerPointsHook, player, money, points);
    }

    /**
     * Give the money and player points to the player.
     * @param economyHook The {@link EconomyHook}.
     * @param playerPointsHook The {@link PlayerPointsHook}.
     * @param player The {@link Player}.
     * @param money The money to give. Use anything less than or equal to 0.0 to give nothing.
     * @param points The player points to give. Use anything less than or equal to 0 to give nothing.
     */
    private void givePayment(
            @NotNull EconomyHook economyHook,
            @NotNull PlayerPointsHook playerPointsHook,
            @NotNull Player player,
            double money,
            int points) {
        if(economyHook.isHooked() && money > 0.0) {
            economyHook.addToBalance(player, money);
        }

        if(playerPointsHook.isHooked() && points > 0) {
            playerPointsHook.addToBalance(player, points);
        }
    }

    /**
     * Send the success message.
     * @param locale The plugin's {@link Locale}.
     * @param economyHook The {@link EconomyHook}.
     * @param playerPointsHook The {@link PlayerPointsHook}.
     * @param player The {@link Player} to send the message to.
     * @param transactionName The transaction name.
     * @param amount The amount sold.
     * @param money The money given.
     * @param points The player points given.
     */
    private void sendSingleItemSoldPlayerMessage(
            @NotNull Locale locale,
            @NotNull EconomyHook economyHook,
            @NotNull PlayerPointsHook playerPointsHook,
            @NotNull Player player,
            @NotNull String transactionName,
            int amount,
            double money,
            int points) {
        List<TagResolver.Single> placeholders = buildPlaceholders(economyHook, playerPointsHook, player, transactionName, amount, money, points);

        if(money > 0.0 && points > 0) {
            player.sendMessage(AdventureUtil.serialize(player, locale.prefix() + locale.sellItemSuccess().moneyAndPoints(), placeholders));
        } else if(money > 0.0) {
            player.sendMessage(AdventureUtil.serialize(player, locale.prefix() + locale.sellItemSuccess().money(), placeholders));
        } else if(points > 0) {
            player.sendMessage(AdventureUtil.serialize(player, locale.prefix() + locale.sellItemSuccess().points(), placeholders));
        }
    }

    /**
     * Send the success message.
     * @param locale The plugin's {@link Locale}.
     * @param economyHook The {@link EconomyHook}.
     * @param playerPointsHook The {@link PlayerPointsHook}.
     * @param player The {@link Player} to send the message to.
     * @param money The money given.
     * @param points The player points given.
     */
    private void sendSellAllPlayerMessage(
            @NotNull Locale locale,
            @NotNull EconomyHook economyHook,
            @NotNull PlayerPointsHook playerPointsHook,
            @NotNull Player player,
            double money,
            int points) {
        List<TagResolver.Single> placeholders = buildPlaceholders(economyHook, playerPointsHook, player, money, points);

        if(money > 0 && points > 0) {
            player.sendMessage(AdventureUtil.serialize(player, locale.prefix() + locale.sellallSuccess().moneyAndPoints(), placeholders));
        } else if(money > 0) {
            player.sendMessage(AdventureUtil.serialize(player, locale.prefix() + locale.sellallSuccess().money(), placeholders));
        } else if(points > 0) {
            player.sendMessage(AdventureUtil.serialize(player, locale.prefix() + locale.sellallSuccess().points(), placeholders));
        }
    }

    /**
     * Create the list of placeholders for success messages.
     * @param economyHook The {@link EconomyHook}.
     * @param playerPointsHook The {@link PlayerPointsHook}.
     * @param player The {@link Player}.
     * @param money The money.
     * @param points The player points.
     * @return A {@link List} of {@link TagResolver.Single}.
     */
    private List<TagResolver.Single> buildPlaceholders(
            @NotNull EconomyHook economyHook,
            @NotNull PlayerPointsHook playerPointsHook,
            @NotNull Player player,
            double money,
            int points) {
        List<TagResolver.Single> placeholders = new ArrayList<>();

        if(money > 0.0 && points > 0) {
            DecimalFormat df = new DecimalFormat("#.##");
            df.setRoundingMode(RoundingMode.CEILING);

            BigDecimal bigPrice = BigDecimal.valueOf(money);
            String formattedSellPrice = df.format(bigPrice);
            placeholders.add(Placeholder.parsed("money", formattedSellPrice));

            BigDecimal bigBalance = BigDecimal.valueOf(economyHook.getBalance(player));
            placeholders.add(Placeholder.parsed("money_balance", df.format(bigBalance)));

            placeholders.add(Placeholder.parsed("player_points", String.valueOf(points)));
            placeholders.add(Placeholder.parsed("player_points_balance", String.valueOf(playerPointsHook.getBalance(player))));
        } else if(money > 0.0) {
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

    /**
     * Create the list of placeholders for success messages.
     * @param economyHook The {@link EconomyHook}.
     * @param playerPointsHook The {@link PlayerPointsHook}.
     * @param player The {@link Player}.
     * @param money The money.
     * @param points The player points.
     * @return A {@link List} of {@link TagResolver.Single}.
     */
    private List<TagResolver.Single> buildPlaceholders(
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

        if(money > 0.0 && points > 0) {
            DecimalFormat df = new DecimalFormat("#.##");
            df.setRoundingMode(RoundingMode.CEILING);

            BigDecimal bigPrice = BigDecimal.valueOf(money);
            String formattedSellPrice = df.format(bigPrice);
            placeholders.add(Placeholder.parsed("money", formattedSellPrice));

            BigDecimal bigBalance = BigDecimal.valueOf(economyHook.getBalance(player));
            placeholders.add(Placeholder.parsed("money_balance", df.format(bigBalance)));

            placeholders.add(Placeholder.parsed("player_points", String.valueOf(points)));
            placeholders.add(Placeholder.parsed("player_points_balance", String.valueOf(playerPointsHook.getBalance(player))));
        } else if(money > 0.0) {
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

    // ###############################################################
    // Below are Legacy API methods. They just wrap the newer methods.
    // ###############################################################

    /**
     * Sells all possible items in an inventory GUI, if a sell price is configured for that item at least once.
     * Any remaining items that weren't sold will be returned to the player's inventory.
     * @param inventory An inventory containing items.
     * @param player The player selling items.
     * @param message Should the sell success or unsellable message from SkyShop be sent?
     * @return true if successful, otherwise false.
     * @deprecated You should use {@link #sellInventory(Player, Inventory, boolean, boolean, boolean)} instead. This method just runs that method.
     */
    @Deprecated(since = "2.1.0.0")
    public boolean sellInventoryGUI(@NotNull Inventory inventory, Player player, boolean message) {
        return sellInventory(player, inventory, false, true, message);
    }

    /**
     * <p>Sells all possible items inside the given inventory if a sell price is configured for that item at least once.</p>
     * <p>This will remove any and all items if the {@link ItemType} has a configured sell price that is > 0.0.</p>
     * <p>Any remaining items that weren't sold will be left inside the inventory.</p>
     * @param inventory The player's inventory containing items.
     * @param player The Player to pay for the items sold.
     * @param message Should the sell success message from SkyShop be sent?
     * @return true if at least one item was sold, else false
     * @deprecated You should use {@link #sellInventory(Player, Inventory, boolean, boolean, boolean)} instead. This method just runs that method.
     */
    @Deprecated(since = "2.1.0.0")
    public boolean sellInventory(@NotNull Player player, @NotNull Inventory inventory, boolean message) {
        return sellInventory(player, inventory, false, false, message);
    }

    /**
     * <p>Sells all possible items inside the given inventory if a sell price is configured for that item at least once.</p>
     * <p>This will remove any and all items if the {@link ItemType} has a configured sell price that is > 0.0.</p>
     * <p>Any remaining items that weren't sold will be left inside the inventory.</p>
     * <p>This method will ignore armor slots. You can use {@link #sellInventory(Player, Inventory, boolean)} for a method that sells armor slots.</p>
     * @param inventory The player's inventory containing items.
     * @param player The Player to pay for the items sold.
     * @param message Should the sell success message from SkyShop be sent?
     * @return true if at least one item was sold, else false
     * @deprecated You should use {@link #sellInventory(Player, Inventory, boolean, boolean, boolean)} instead. This method just runs that method.
     */
    @Deprecated(since = "2.1.0.0")
    public boolean sellPlayerInventory(Player player, Inventory inventory, boolean message) {
        return sellInventory(player, inventory, true, false, message);
    }

    /**
     * <p>Sells all possible items matching the given ItemStack inside the player's inventory if a sell price is configured for that item at least once.</p>
     * <p>This will remove any and all items matching the type (Material) if it has a configured sell price that is > 0.0.</p>
     * <p>Any remaining items that weren't sold will be left inside the Inventory.</p>
     * @param player The Player to pay for the items sold.
     * @param itemStack The ItemStack that matches the items being sold
     * @param message Should the sell success or unsellable message from SkyShop be sent?
     * @return true if at least one item was sold, else false
     * @deprecated You should use {@link #sellAllMatchingItemStack(Player, Inventory, ItemStack, boolean, boolean, boolean)} instead. This method just runs that method.
     */
    @Deprecated(since = "2.1.0.0")
    public boolean sellAllMatchingItemStack(@NotNull Player player, @NotNull ItemStack itemStack, boolean message) {
        return sellAllMatchingItemStack(player, player.getInventory(), itemStack, true, false, message);
    }
}