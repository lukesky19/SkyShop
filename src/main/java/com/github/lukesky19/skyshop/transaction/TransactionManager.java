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

import com.github.lukesky19.skylib.common.api.adventure.AdventureUtility;
import com.github.lukesky19.skylib.paper.api.adventure.PaperAdventureUtility;
import com.github.lukesky19.skyshop.SkyShop;
import com.github.lukesky19.skyshop.api.configuration.TransactionConfiguration;
import com.github.lukesky19.skyshop.api.processor.TransactionProcessor;
import com.github.lukesky19.skyshop.api.result.TransactionResult;
import com.github.lukesky19.skyshop.configuration.category.data.CategoryConfigV4;
import com.github.lukesky19.skyshop.configuration.locale.LocaleManager;
import com.github.lukesky19.skyshop.configuration.locale.data.LocaleV5;
import com.github.lukesky19.skyshop.gui.TransactionGUI;
import com.github.lukesky19.skyshop.hook.HookManager;
import com.github.lukesky19.skyshop.hook.impl.EconomyHook;
import com.github.lukesky19.skyshop.hook.impl.PlayerPointsHook;
import com.github.lukesky19.skyshop.player.data.PlayerData;
import com.github.lukesky19.skyshop.player.data.PlayerModifiers;
import com.github.lukesky19.skyshop.registry.RegistryManager;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.List;
import java.util.Objects;

/**
 * This class manages the facilitation of transactions.
 */
public class TransactionManager {
    private final @NonNull ComponentLogger logger;
    private final @NonNull LocaleManager localeManager;
    private final @NonNull RegistryManager registryManager;
    private final @NonNull HookManager hookManager;

    /**
     * Constructor
     * @param skyShop A {@link SkyShop} instance.
     * @param localeManager A {@link LocaleManager} instance.
     * @param registryManager A {@link RegistryManager} instance.
     * @param hookManager A {@link HookManager} instance.
     */
    public TransactionManager(
            @NonNull SkyShop skyShop,
            @NonNull LocaleManager localeManager,
            @NonNull RegistryManager registryManager,
            @NonNull HookManager hookManager) {
        this.logger = skyShop.getComponentLogger();
        this.localeManager = localeManager;
        this.registryManager = registryManager;
        this.hookManager = hookManager;
    }

    /**
     * Attempt to complete a buy transaction.
     * @param player The {@link Player} involved in the transaction.
     * @param playerData The player's {@link PlayerData}.
     * @param gui The {@link TransactionGUI} the player is viewing.
     * @param transactionData The {@link CategoryConfigV4.TransactionData}.
     * @param priceConfig The {@link CategoryConfigV4.PriceConfig}.
     * @param priceData The {@link PriceData}.
     * @param amount The amount being purchased.
     * @return true if successful, false if not.
     */
    public boolean buy(
            @NonNull Player player,
            @NonNull PlayerData playerData,
            @NonNull TransactionGUI gui,
            CategoryConfigV4.@NonNull TransactionData transactionData,
            CategoryConfigV4.@NonNull PriceConfig priceConfig,
            @NonNull PriceData priceData,
            int amount) {
        LocaleV5 locale = localeManager.getConfiguration();
        EconomyHook economyHook = hookManager.getHook(EconomyHook.class);
        PlayerPointsHook playerPointsHook = hookManager.getHook(PlayerPointsHook.class);

        if(!validateMoneyAndPoints(locale, economyHook, playerPointsHook, player, priceData.money(), priceData.points())) return false;
        
        if(isTransactionDisallowed(true, locale, player, transactionData.transactionList(), amount)) return false;

        applyTransaction(true, locale, player, transactionData.transactionList(), amount);

        // Remove the prices from the player's balances
        if(priceData.money() > 0 && economyHook.isHooked()) economyHook.removeFromBalance(player, priceData.money());
        if(priceData.points() > 0 && playerPointsHook.isHooked()) playerPointsHook.removeFromBalance(player, priceData.points());

        // Create the message placeholders
        List<TagResolver.Single> messagePlaceholders = createPlaceholders(player, economyHook, playerPointsHook, transactionData, priceData, amount);

        // Send the message that the transaction was a success
        if(priceData.money() > 0 && priceData.points() > 0) {
            player.sendMessage(PaperAdventureUtility.deserialize(player, locale.prefix() + locale.buySuccess().moneyAndPoints(), messagePlaceholders));
        } else if(priceData.money() > 0) {
            player.sendMessage(PaperAdventureUtility.deserialize(player, locale.prefix() + locale.buySuccess().money(), messagePlaceholders));
        } else if(priceData.points() > 0) {
            player.sendMessage(PaperAdventureUtility.deserialize(player, locale.prefix() + locale.buySuccess().points(), messagePlaceholders));
        }

        // Update player price modifiers
        updatePlayerPrices(true, playerData, transactionData.transactionId(), priceConfig, priceData, amount);

        // Refresh GUI
        gui.refresh();

        return true;
    }

    /**
     * Attempt to complete a sell transaction.
     * @param player The {@link Player} involved in the transaction.
     * @param playerData The player's {@link PlayerData}.
     * @param gui The {@link TransactionGUI} the player is viewing.
     * @param transactionData The {@link CategoryConfigV4.TransactionData}.
     * @param priceConfig The {@link CategoryConfigV4.PriceConfig}.
     * @param priceData The {@link PriceData}.
     * @param amount The amount being sold.
     * @return true if successful, false if not.
     */
    public boolean sell(
            @NonNull Player player,
            @NonNull PlayerData playerData,
            @NonNull TransactionGUI gui,
            CategoryConfigV4.@NonNull TransactionData transactionData,
            CategoryConfigV4.@NonNull PriceConfig priceConfig,
            @NonNull PriceData priceData,
            int amount) {
        LocaleV5 locale = localeManager.getConfiguration();
        EconomyHook economyHook = hookManager.getHook(EconomyHook.class);
        PlayerPointsHook playerPointsHook = hookManager.getHook(PlayerPointsHook.class);

        if(isTransactionDisallowed(false, locale, player, transactionData.transactionList(), amount)) return false;

        applyTransaction(false, locale, player, transactionData.transactionList(), amount);

        // Add the prices from the player's balances
        if(priceData.money() > 0 && economyHook.isHooked()) {
            economyHook.addToBalance(player, priceData.money());
        }
        if(priceData.points() > 0 && playerPointsHook.isHooked()) {
            playerPointsHook.addToBalance(player, priceData.points());
        }

        // Create the message placeholders
        List<TagResolver.Single> messagePlaceholders = createPlaceholders(player, economyHook, playerPointsHook, transactionData, priceData, amount);

        // Send the message that the transaction was a success
        if(priceData.money > 0 && priceData.points > 0) {
            player.sendMessage(PaperAdventureUtility.deserialize(player, locale.prefix() + locale.sellSuccess().moneyAndPoints(), messagePlaceholders));
        } else if(priceData.money > 0) {
            player.sendMessage(PaperAdventureUtility.deserialize(player, locale.prefix() + locale.sellSuccess().money(), messagePlaceholders));
        } else if(priceData.points > 0) {
            player.sendMessage(PaperAdventureUtility.deserialize(player, locale.prefix() + locale.sellSuccess().points(), messagePlaceholders));
        }

        // Update player price modifiers
        updatePlayerPrices(false, playerData, transactionData.transactionId(), priceConfig, priceData, amount);

        // Refresh GUI
        gui.refresh();

        return true;
    }

    /**
     * Check if the money and points are valid and that the necessary plugins are hooked into.
     * The GUI the player is in will be closed on any error.
     * @param locale The plugin's {@link LocaleV5}.
     * @param economyHook The plugin's {@link EconomyHook}.
     * @param playerPointsHook The player's {@link PlayerPointsHook}.
     * @param player The {@link Player}.
     * @param money The money.
     * @param points The player points.
     * @return true if money and points are valid along with the necessary hooks or false.
     */
    private boolean validateMoneyAndPoints(
            @NonNull LocaleV5 locale,
            @NonNull EconomyHook economyHook,
            @NonNull PlayerPointsHook playerPointsHook,
            @NonNull Player player,
            double money,
            int points) {
        if(money <= 0 && points <= 0) {
            logger.error(AdventureUtility.plain("Unable to complete the transaction because money and points are less than or equal to 0."));
            player.sendMessage(AdventureUtility.deserialize(locale.prefix() + locale.transactionError()));
            return false;
        }

        if(money > 0) {
            if(!economyHook.isHooked()) {
                logger.error(AdventureUtility.plain("Unable to complete the transaction due to no economy found."));
                player.sendMessage(AdventureUtility.deserialize(locale.prefix() + locale.transactionError()));
                return false;
            }

            if(economyHook.getBalance(player) < money) {
                player.sendMessage(PaperAdventureUtility.deserialize(player, locale.prefix() + locale.insufficientMoney()));
                return false;
            }
        }

        if(points > 0) {
            if(!playerPointsHook.isHooked()) {
                logger.error(AdventureUtility.plain("Unable to complete the transaction due to no player points dependency found."));
                player.sendMessage(AdventureUtility.deserialize(locale.prefix() + locale.transactionError()));
                return false;
            }

            if(playerPointsHook.getBalance(player) < points) {
                player.sendMessage(PaperAdventureUtility.deserialize(player, locale.prefix() + locale.insufficientPlayerPoints()));
                return false;
            }
        }

        return true;
    }

    /**
     * Check if the transaction can be completed.
     * @param purchase Is this transaction for a purchase or selling?
     * @param locale The plugin's {@link LocaleV5}.
     * @param player The {@link Player} involved in the transaction.
     * @param transactionList The {@link List} of {@link TransactionConfiguration}s.
     * @param amount The amount involved in the transaction.
     * @return true or false.
     */
    private boolean isTransactionDisallowed(
            boolean purchase,
            @NonNull LocaleV5 locale,
            @NonNull Player player,
            @NonNull List<TransactionConfiguration> transactionList,
            int amount) {
        for(TransactionConfiguration configuration : transactionList) {
            TransactionProcessor processor = registryManager.getProcessor(configuration.getId());
            if(processor == null) {
                logger.warn(AdventureUtility.plain("No processor for id " + configuration.getId()));
                player.sendMessage(AdventureUtility.deserialize(locale.prefix() + locale.transactionError()));
                return true;
            }

            TransactionResult result;
            if(purchase) {
                result = processor.canBuy(player, configuration, amount);
            } else {
                result = processor.canSell(player, configuration, amount);
            }
            if(result.cancelled()) return true;

            if(result.errored()) {
                if(result.sendErrorMessage()) {
                    logger.warn(AdventureUtility.plain("Early checks failed for a " + (purchase ? "buy" : "sell") + " transaction with id: " + configuration.getId() + ". Error: " + result.message()));
                    player.sendMessage(AdventureUtility.deserialize(locale.prefix() + locale.transactionError()));
                }

                return true;
            }
        }

        return false;
    }

    /**
     * Apply the configurations to complete the transaction for the player.
     * @param purchase Is this transaction for a purchase or selling?
     * @param locale The plugin's {@link LocaleV5}.
     * @param player The {@link Player} involved in the transaction.
     * @param transactionList The {@link List} of {@link TransactionConfiguration}s.
     * @param amount The amount involved in the transaction.
     */
    private void applyTransaction(
            boolean purchase,
            @NonNull LocaleV5 locale,
            @NonNull Player player,
            @NonNull List<TransactionConfiguration> transactionList,
            int amount) {
        for(TransactionConfiguration configuration : transactionList) {
            TransactionProcessor processor = registryManager.getProcessor(configuration.getId());
            if(processor != null) {
                TransactionResult result;
                if(purchase) {
                    result = processor.buy(player, configuration, amount);
                } else {
                    result = processor.sell(player, configuration, amount);
                }

                if(result.errored()) {
                    if(result.sendErrorMessage()) {
                        logger.warn(AdventureUtility.plain("Failed to process a portion of a " + (purchase ? "buy" : "sell") + " transaction with id: " + configuration.getId() + ". Error: " + result.message()));
                        player.sendMessage(AdventureUtility.deserialize(locale.prefix() + locale.transactionError()));
                    }
                }
            } else {
                logger.warn(AdventureUtility.plain("No processor for id " + configuration.getId()));
                player.sendMessage(AdventureUtility.deserialize(locale.prefix() + locale.transactionError()));
            }
        }
    }

    /**
     * Create the {@link List} of {@link TagResolver.Single} used in transaction success messages.
     * @param player The {@link Player} involved in the transaction.
     * @param economyHook The {@link EconomyHook}.
     * @param playerPointsHook The {@link PlayerPointsHook}.
     * @param transactionData The {@link CategoryConfigV4.TransactionData}.
     * @param priceData The {@link PriceData}.
     * @param amount The amount.
     * @return A {@link List} of {@link TagResolver.Single}.
     */
    private @NonNull List<TagResolver.Single> createPlaceholders(
            @NonNull Player player,
            @NonNull EconomyHook economyHook,
            @NonNull PlayerPointsHook playerPointsHook,
            CategoryConfigV4.@NonNull TransactionData transactionData,
            @NonNull PriceData priceData,
            int amount) {
        DecimalFormat decimalFormat = new DecimalFormat("#.##");
        decimalFormat.setRoundingMode(RoundingMode.CEILING);

        return List.of(
                Placeholder.parsed("transaction_name", Objects.requireNonNullElse(transactionData.transactionName(), "")),
                Placeholder.parsed("amount", String.valueOf(amount)),
                Placeholder.parsed("money", decimalFormat.format(BigDecimal.valueOf(priceData.money()))),
                Placeholder.parsed("money_balance", decimalFormat.format(BigDecimal.valueOf(economyHook.getBalance(player)))),
                Placeholder.parsed("player_points", String.valueOf(priceData.points())),
                Placeholder.parsed("player_points_balance", String.valueOf(playerPointsHook.getBalance(player))));
    }

    /**
     * Update the {@link PlayerModifiers}.
     * @param purchase Was the transaction for a purchase or selling?
     * @param playerData The {@link PlayerData}.
     * @param transactionId The transaction id.
     * @param priceConfig The {@link CategoryConfigV4.PriceConfig}.
     * @param priceData The {@link PriceData}.
     * @param amount The amount purchased or sold.
     */
    public void updatePlayerPrices(
            boolean purchase,
            @NonNull PlayerData playerData,
            @Nullable String transactionId,
            CategoryConfigV4.@NonNull PriceConfig priceConfig,
            @NonNull PriceData priceData,
            int amount) {
        if(transactionId == null) return;

        PlayerModifiers playerPrices = playerData.getPlayerPrices(transactionId);

        if(priceData.updatePrices()) {
            if(purchase) {
                CategoryConfigV4.PriceModifier priceModifier = priceConfig.buyModifier();

                playerPrices.setBuyMoneyModifier(priceModifier.moneyModifier());
                playerPrices.setHighestBuyMoneyModifier(priceModifier.moneyModifier());
                playerPrices.setBuyPointsModifier(priceModifier.pointsModifier());
                playerPrices.setHighestBuyPointsModifier(priceModifier.pointsModifier());
                playerPrices.setBuyCooldownSeconds(priceModifier.cooldownSeconds());
                playerPrices.setHighestBuyCooldown(priceModifier.cooldownSeconds());
                playerPrices.setBuyUpdateInterval(priceModifier.updateIntervalSeconds());

                playerPrices.resetBuyAmount();
            } else {
                CategoryConfigV4.PriceModifier priceModifier = priceConfig.sellModifier();

                playerPrices.setSellMoneyModifier(priceModifier.moneyModifier());
                playerPrices.setHighestSellMoneyModifier(priceModifier.moneyModifier());
                playerPrices.setSellPointsModifier(priceModifier.pointsModifier());
                playerPrices.setHighestSellPointsModifier(priceModifier.pointsModifier());
                playerPrices.setSellCooldownSeconds(priceModifier.cooldownSeconds());
                playerPrices.setHighestSellCooldown(priceModifier.cooldownSeconds());
                playerPrices.setSellUpdateInterval(priceModifier.updateIntervalSeconds());

                playerPrices.resetSellAmount();
            }
        } else if(priceData.updateAmount()) {
            if(purchase) {
                playerPrices.incrementBuyAmount(amount);
            } else {
                playerPrices.incrementSellAmount(amount);
            }
        }
    }

    /**
     * Calculate the effective buy prices.
     * @param playerData The {@link PlayerData}.
     * @param transactionId The transaction id.
     * @param priceConfig The {@link CategoryConfigV4.PriceConfig}
     * @param purchaseAmount The amount purchased
     * @return The {@link PriceData}.
     */
    public @NonNull PriceData calculateBuyPrices(
            @NonNull PlayerData playerData,
            @Nullable String transactionId,
            CategoryConfigV4.@NonNull PriceConfig priceConfig,
            int purchaseAmount) {
        double buyMoney = 0.0;
        int buyPoints = 0;
        boolean updatePrices = false;
        boolean updateAmount = false;

        if(transactionId != null) {
            CategoryConfigV4.PriceModifier priceModifier = priceConfig.buyModifier();

            if(priceModifier.cooldownSeconds() > 0 && priceModifier.countBeforeModifier() > 0) {
                PlayerModifiers playerPrices = playerData.getPlayerPrices(transactionId);

                if(priceConfig.buyMoney() > 0) {
                    if(playerPrices.getBuyMoneyModifier() > 0.0) {
                        buyMoney = (priceConfig.buyMoney() + playerPrices.getBuyMoneyModifier()) * purchaseAmount;
                    } else if(playerPrices.getBuyAmount() + purchaseAmount >= priceModifier.countBeforeModifier()) {
                        int amountBeforeChange = priceModifier.countBeforeModifier() - playerPrices.getBuyAmount();
                        int amountAfterChange = (playerPrices.getBuyAmount() + purchaseAmount) - priceModifier.countBeforeModifier();

                        buyMoney = (priceConfig.buyMoney() * amountBeforeChange) + ((priceConfig.buyMoney() + priceModifier.moneyModifier()) * amountAfterChange);

                        updatePrices = true;
                    } else {
                        buyMoney = priceConfig.buyMoney() * purchaseAmount;

                        updateAmount = true;
                    }
                }

                if(priceConfig.buyPoints() > 0) {
                    if(playerPrices.getBuyPointsModifier() > 0) {
                        buyPoints = (priceConfig.buyPoints() + playerPrices.getBuyPointsModifier()) * purchaseAmount;
                    } else if(playerPrices.getBuyAmount() + purchaseAmount >= priceModifier.countBeforeModifier()) {
                        int amountBeforeChange = priceModifier.countBeforeModifier() - playerPrices.getBuyAmount();
                        int amountAfterChange = (playerPrices.getBuyAmount() + purchaseAmount) - priceModifier.countBeforeModifier();

                        buyPoints = (priceConfig.buyPoints() * amountBeforeChange) + ((priceConfig.buyPoints() + priceModifier.pointsModifier()) * amountAfterChange);

                        updatePrices = true;
                    } else {
                        buyPoints = priceConfig.buyPoints() * purchaseAmount;

                        updateAmount = true;
                    }
                }
            } else {
                if(priceConfig.buyMoney() > 0) {
                    buyMoney = priceConfig.buyMoney() * purchaseAmount;
                }

                if(priceConfig.buyPoints() > 0) {
                    buyPoints = priceConfig.buyPoints() * purchaseAmount;
                }
            }
        } else {
            if(priceConfig.buyMoney() > 0) {
                buyMoney = priceConfig.buyMoney() * purchaseAmount;
            }

            if(priceConfig.buyPoints() > 0) {
                buyPoints = priceConfig.buyPoints() * purchaseAmount;
            }
        }

        return new PriceData(updatePrices, updateAmount, buyMoney, buyPoints);
    }

    /**
     * Calculate the effective sell prices.
     * @param playerData The {@link PlayerData}.
     * @param transactionId The transaction id.
     * @param priceConfig The {@link CategoryConfigV4.PriceConfig}
     * @param sellAmount The amount sold.
     * @return The {@link PriceData}.
     */
    public @NonNull PriceData calculateSellPrices(
            @NonNull PlayerData playerData,
            @Nullable String transactionId,
            CategoryConfigV4.@NonNull PriceConfig priceConfig,
            int sellAmount) {
        double sellMoney = 0.0;
        int sellPoints = 0;
        boolean updatePrices = false;
        boolean updateAmount = false;

        if(transactionId != null) {
            CategoryConfigV4.PriceModifier priceModifier = priceConfig.sellModifier();

            if(priceModifier.cooldownSeconds() > 0 && priceModifier.countBeforeModifier() > 0) {
                PlayerModifiers playerPrices = playerData.getPlayerPrices(transactionId);

                if(priceConfig.sellMoney() > 0) {
                    if(playerPrices.getSellMoneyModifier() > 0.0) {
                        sellMoney = (priceConfig.sellMoney() - playerPrices.getSellMoneyModifier()) * sellAmount;
                    } else if(playerPrices.getSellAmount() + sellAmount >= priceModifier.countBeforeModifier()) {
                        int amountBeforeChange = priceModifier.countBeforeModifier() - playerPrices.getSellAmount();
                        int amountAfterChange = (playerPrices.getSellAmount() + sellAmount) - priceModifier.countBeforeModifier();

                        sellMoney = (priceConfig.sellMoney() * amountBeforeChange) + ((priceConfig.sellMoney() - priceModifier.moneyModifier()) * amountAfterChange);

                        updatePrices = true;
                    } else {
                        sellMoney = priceConfig.sellMoney() * sellAmount;

                        updateAmount = true;
                    }
                }

                if(priceConfig.sellPoints() > 0) {
                    if(playerPrices.getSellPointsModifier() > 0) {
                        sellPoints = (priceConfig.sellPoints() + playerPrices.getSellPointsModifier()) * sellAmount;
                    } else if(playerPrices.getSellAmount() + sellAmount >= priceModifier.countBeforeModifier()) {
                        int amountBeforeChange = priceModifier.countBeforeModifier() - playerPrices.getSellAmount();
                        int amountAfterChange = (playerPrices.getSellAmount() + sellAmount) - priceModifier.countBeforeModifier();

                        sellPoints = (priceConfig.sellPoints() * amountBeforeChange) + ((priceConfig.sellPoints() + priceModifier.pointsModifier()) * amountAfterChange);

                        updatePrices = true;
                    } else {
                        sellPoints = priceConfig.sellPoints() * sellAmount;

                        updateAmount = true;
                    }
                }
            } else {
                if(priceConfig.sellMoney() > 0) {
                    sellMoney = priceConfig.sellMoney() * sellAmount;
                }

                if(priceConfig.sellPoints() > 0) {
                    sellPoints = priceConfig.sellPoints() * sellAmount;
                }
            }
        } else {
            if(priceConfig.sellMoney() > 0) {
                sellMoney = priceConfig.sellMoney() * sellAmount;
            }

            if(priceConfig.sellPoints() > 0) {
                sellPoints = priceConfig.sellPoints() * sellAmount;
            }
        }

        return new PriceData(updatePrices, updateAmount, sellMoney, sellPoints);
    }

    /**
     * This record holds the calculated price data for a transaction.
     * @param updatePrices If player prices should be updated.
     * @param updateAmount If the player transaction count should be updated.
     * @param money The money.
     * @param points The player points.
     */
    public record PriceData(boolean updatePrices, boolean updateAmount, double money, int points) {}
}