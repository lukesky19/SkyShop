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
package com.github.lukesky19.skyshop.manager.config;

import com.github.lukesky19.skylib.api.adventure.AdventureUtil;
import com.github.lukesky19.skylib.api.configurate.ConfigurationUtility;
import com.github.lukesky19.skylib.libs.configurate.CommentedConfigurationNode;
import com.github.lukesky19.skylib.libs.configurate.ConfigurateException;
import com.github.lukesky19.skylib.libs.configurate.yaml.YamlConfigurationLoader;
import com.github.lukesky19.skyshop.SkyShop;
import com.github.lukesky19.skyshop.config.locale.Locale;
import com.github.lukesky19.skyshop.config.locale.Locale_2_0_0_0;
import com.github.lukesky19.skyshop.config.settings.Settings;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.nio.file.Path;
import java.util.List;

/**
 * This class manages everything related to handling the plugin's locale configuration.
 */
public class LocaleManager {
    private final @NotNull SkyShop skyShop;
    private final @NotNull SettingsManager settingsManager;
    private @Nullable Locale locale;
    /**
     * The plugin's default locale. Used when the locale configuration is invalid.
     */
    public final @NotNull Locale DEFAULT_LOCALE = new Locale(
            "2.1.0.0",
            "<aqua><bold>SkyShop</bold></aqua><gray> ▪ </gray>",
            List.of("<aqua>SkyShop is developed by <white><bold>lukeskywlker19</bold></white>.</aqua>",
                    "<aqua>Source code is released on GitHub: <click:OPEN_URL:https://github.com/lukesky19><yellow><underlined><bold>https://github.com/lukesky19</bold></underlined></yellow></click></aqua>",
                    " ",
                    "<aqua><bold>List of Commands:</bold></aqua>",
                    "<white>/</white><aqua>shop</aqua>",
                    "<white>/</white><aqua>shop</aqua> <yellow>help</yellow>",
                    "<white>/</white><aqua>shop</aqua> <yellow>reload</yellow>",
                    "<white>/</white><aqua>shop</aqua> <yellow>sellall</yellow>",
                    "<white>/</white><aqua>shop</aqua><yellow>stats</yellow>",
                    "<white>/</white><aqua>shop</aqua> <yellow>open <category></yellow>",
                    "<white>/</white><aqua>sell</aqua> <yellow>all</yellow>",
                    "<white>/</white><aqua>sell</aqua> <yellow>hand</yellow>",
                    "<white>/</white><aqua>sell</aqua> <yellow>hand all</yellow>"),
            "<aqua>Configuration files have been reloaded.</aqua>",
            "<red>You do not have enough items to sell.</red>",
            "<red>You lack the money to buy this item.</red>",
            "<red>You lack the player points to buy this item.</red>",
            new Locale.SuccessMessages(
                    "<white>Purchased <yellow><amount> <transaction_name></yellow> for $<yellow><money></yellow> and <yellow><player_points></yellow> player points. Balance: <yellow><money_balance></yellow> Player Points: <yellow><player_points_balance></yellow></white>",
                    "<white>Purchased <yellow><amount> <transaction_name></yellow> for <yellow><money></yellow>. Balance: <yellow><money_balance></yellow></white>",
                    "<white>Purchased <yellow><amount> <transaction_name></yellow> for <yellow><player_points></yellow> player points. Player Points: <yellow><player_points_balance></yellow></white>"),
            new Locale.SuccessMessages(
                    "<white>Sold <yellow><amount> <transaction_name></yellow> for $<yellow><money></yellow> and <yellow><player_points></yellow> player points. Balance: <yellow><money_balance></yellow> Player Points: <yellow><player_points_balance></yellow></white>",
                    "<white>Sold <yellow><amount> <transaction_name></yellow> for <yellow><money></yellow>. Balance: <yellow><money_balance></yellow></white>",
                    "<white>Sold <yellow><amount> <transaction_name></yellow> for <yellow><player_points></yellow> player points. Player Points: <yellow><player_points_balance></yellow></white>"),
            new Locale.SuccessMessages(
                    "<white>Purchased <yellow><transaction_name></yellow> for $<yellow><money></yellow> and <yellow><player_points></yellow> player points. Balance: <yellow><money_balance></yellow> Player Points: <yellow><player_points_balance></yellow></white>",
                    "<white>Purchased <yellow><transaction_name></yellow> for $<yellow><money></yellow>. Balance: <yellow><money_balance></yellow>",
                    "<white>Purchased <yellow><transaction_name></yellow> for <yellow><player_points></yellow> player points. Balance: <yellow><player_points_balance></yellow>"),
            new Locale.SuccessMessages(
                    "<white>Sold <yellow><transaction_name></yellow> for $<yellow><money></yellow> and <yellow><player_points></yellow> player points. Balance: <yellow><money_balance></yellow> Player Points: <yellow><player_points_balance></yellow></white>",
                    "<white>Sold <yellow><transaction_name></yellow> for $<yellow><money></yellow>. Balance: <yellow><money_balance></yellow>",
                    "<white>Sold <yellow><transaction_name></yellow> for <yellow><player_points></yellow> player points. Balance: <yellow><player_points_balance></yellow>"),
            new Locale.SuccessMessages(
                    "<white>Successfully sold all items for $<yellow><money></yellow> and <yellow><player_points></yellow> player points. Balance: <yellow><money_balance></yellow> Player Points: <yellow><player_points_balance></yellow></white>",
                    "<white>Successfully sold all items for $<yellow><money></yellow>. Updated Balance: <yellow><money_Balance></yellow></white>",
                    "<white>Successfully sold all items for <yellow><player_points></yellow> player points. Updated Balance: <yellow><player_points_balance></yellow></white>"),
            "<white>Unable to sell one or more items. It was added back to your inventory or dropped at your feet if full.</white>",
            "<red>This is not able to be purchased.</red>",
            "<red>This is not able to be sold.</red>",
            "<red>This command can only be ran in-game.</red>",
            "<red>Unable to open this GUI because of a configuration error.</red>",
            "<red>Unable to open the stats GUI as stats tracking is disabled.</red>",
            "<red>Unable to complete this transaction due to an error.</red>");

    /**
     * Constructor
     * @param skyShop A {@link SkyShop} instance.
     * @param settingsManager A {@link SettingsManager} instance.
    */
    public LocaleManager(@NotNull SkyShop skyShop, @NotNull SettingsManager settingsManager) {
        this.skyShop = skyShop;
        this.settingsManager = settingsManager;
    }

    /**
     * Gets the plugin's {@link Locale} or the {@link #DEFAULT_LOCALE} if the locale config failed to load.
     * @return A {@link Locale} record.
     */
    public @NotNull Locale getLocale() {
        if(locale == null) return DEFAULT_LOCALE;

        return locale;
    }

    /**
     * (Re-)loads the plugin's locale.
     */
    public void reload() {
        ComponentLogger logger = skyShop.getComponentLogger();
        locale = null;

        // Save the default locales
        saveDefaultLocales();

        // Don't load anything if the plugin's settings or locale option are invalid.
        Settings settings = settingsManager.getSettingsConfig();
        if(settings == null) {
            logger.warn("Failed to load locale configuration due to invalid plugin settings.");
            return;
        }
        if(settings.locale() == null) {
            logger.warn("Failed to load locale configuration due to invalid locale configured.");
            return;
        }

        // Attempt to load and validate the config. Return the locale if valid and no errors occur.
        Path path = Path.of(skyShop.getDataFolder() + File.separator + "locale" + File.separator + (settings.locale() + ".yml"));
        YamlConfigurationLoader loader = ConfigurationUtility.getYamlConfigurationLoader(path);
        try {
            locale = loader.load().get(Locale.class);

            // Migrate the locale if needed
            migrate(logger, settings.locale());

            validateLocale();
        } catch (ConfigurateException e) {
            logger.error(AdventureUtil.serialize("Failed to load locale configuration. " + e.getMessage()));
        }
    }

    /**
     * Migrate the locale to the latest version if needed.
     * @param logger The plugin's {@link ComponentLogger}.
     * @param localeName The name of the locale currently used.
     */
    private void migrate(@NotNull ComponentLogger logger, @NotNull String localeName) {
        if(locale == null) return;
        if(locale.configVersion() == null) return;

        switch(locale.configVersion()) {
            case "2.1.0.0" -> {
                // Latest version, do nothing
            }

            case "2.0.0.0" -> {
                @Nullable Locale_2_0_0_0 legacyLocale = loadLegacyLocale(localeName);
                if(legacyLocale == null) {
                    logger.warn(AdventureUtil.serialize("Unable to migrate legacy locale as it failed to load."));
                    return;
                }

                locale = new Locale(
                        "2.1.0.0",
                        locale.prefix(),
                        List.of("<aqua>SkyShop is developed by <white><bold>lukeskywlker19</bold></white>.</aqua>",
                                "<aqua>Source code is released on GitHub: <click:OPEN_URL:https://github.com/lukesky19><yellow><underlined><bold>https://github.com/lukesky19</bold></underlined></yellow></click></aqua>",
                                " ",
                                "<aqua><bold>List of Commands:</bold></aqua>",
                                "<white>/</white><aqua>shop</aqua>",
                                "<white>/</white><aqua>shop</aqua> <yellow>help</yellow>",
                                "<white>/</white><aqua>shop</aqua> <yellow>reload</yellow>",
                                "<white>/</white><aqua>shop</aqua> <yellow>sellall</yellow>",
                                "<white>/</white><aqua>shop</aqua><yellow>stats</yellow>",
                                "<white>/</white><aqua>shop</aqua> <yellow>open <category></yellow>",
                                "<white>/</white><aqua>sell</aqua> <yellow>all</yellow>",
                                "<white>/</white><aqua>sell</aqua> <yellow>hand</yellow>",
                                "<white>/</white><aqua>sell</aqua> <yellow>hand all</yellow>"),
                        locale.configReload(),
                        locale.notEnoughItems(),
                        legacyLocale.insufficientFunds(),
                        "<red>You lack the player points to buy this item.</red>",
                        new Locale.SuccessMessages(
                                "<white>Purchased <yellow><amount> <transaction_name></yellow> for $<yellow><money></yellow> and <yellow><player_points></yellow> player points. Balance: <yellow><money_balance></yellow> Player Points: <yellow><player_points_balance></yellow></white>",
                                updatePlaceholders(legacyLocale.buyItemSuccess()),
                                "<white>Purchased <yellow><amount> <transaction_name></yellow> for <yellow><player_points></yellow> player points. Player Points: <yellow><player_points_balance></yellow></white>"),
                        new Locale.SuccessMessages(
                                "<white>Sold <yellow><amount> <transaction_name></yellow> for $<yellow><money></yellow> and <yellow><player_points></yellow> player points. Balance: <yellow><money_balance></yellow> Player Points: <yellow><player_points_balance></yellow></white>",
                                updatePlaceholders(legacyLocale.sellItemSuccess()),
                                "<white>Sold <yellow><amount> <transaction_name></yellow> for <yellow><player_points></yellow> player points. Player Points: <yellow><player_points_balance></yellow></white>"),
                        new Locale.SuccessMessages(
                                "<white>Purchased <yellow><transaction_name></yellow> for $<yellow><money></yellow> and <yellow><player_points></yellow> player points. Balance: <yellow><money_balance></yellow> Player Points: <yellow><player_points_balance></yellow></white>",
                                updatePlaceholders(legacyLocale.buyCommandSuccess()),
                                "<white>Purchased <yellow><transaction_name></yellow> for <yellow><player_points></yellow> player points. Balance: <yellow><player_points_balance></yellow>"),
                        new Locale.SuccessMessages(
                                "<white>Sold <yellow><transaction_name></yellow> for $<yellow><money></yellow> and <yellow><player_points></yellow> player points. Balance: <yellow><money_balance></yellow> Player Points: <yellow><player_points_balance></yellow></white>",
                                updatePlaceholders(legacyLocale.sellCommandSuccess()),
                                "<white>Sold <yellow><transaction_name></yellow> for <yellow><player_points></yellow> player points. Balance: <yellow><player_points_balance></yellow>"),
                        new Locale.SuccessMessages(
                                "<white>Successfully sold all items for $<yellow><money></yellow> and <yellow><player_points></yellow> player points. Balance: <yellow><money_balance></yellow> Player Points: <yellow><player_points_balance></yellow></white>",
                                updatePlaceholders(legacyLocale.sellallSuccess()),
                                "<white>Successfully sold all items for <yellow><player_points></yellow> player points. Updated Balance: <yellow><player_points_balance></yellow></white>"),
                        locale.sellallUnsellable(),
                        locale.unbuyable(),
                        locale.unsellable(),
                        locale.inGameOnly(),
                        locale.guiOpenError(),
                        locale.statsDisabledGuiError(),
                        "<red>Unable to complete this transaction due to an error.</red>");

                saveLocale(logger, localeName);
            }

            default -> logger.warn(AdventureUtil.serialize("Unable to migrate the locale config as the config version is an unknown value."));
        }
    }

    /**
     * Save the current locale configuration.
     * @param logger The plugin's {@link ComponentLogger}.
     * @param localeName The name of the locale currently used.
     */
    private void saveLocale(@NotNull ComponentLogger logger, @NotNull String localeName) {
        if(locale == null) return;

        Path path = Path.of(skyShop.getDataFolder() + File.separator + "locale" + File.separator + (localeName + ".yml"));
        YamlConfigurationLoader loader = ConfigurationUtility.getYamlConfigurationLoader(path);
        try {
            CommentedConfigurationNode node = loader.createNode();

            node.set(Locale.class, locale);

            loader.save(node);
        } catch (ConfigurateException e) {
            logger.error(AdventureUtil.serialize("Failed to save locale configuration. " + e.getMessage()));
        }
    }

    /**
     * Saves the default locale files that come bundled with the plugin, if they do not exist at least.
     */
    private void saveDefaultLocales() {
        Path path = Path.of(skyShop.getDataFolder() + File.separator + "locale" + File.separator + "en_US.yml");
        if(!path.toFile().exists()) {
            skyShop.saveResource("locale" + File.separator + "en_US.yml", false);
        }
    }

    /**
     * Load the locale configuration using the 2.0.0.0 format.
     * @param localeName The name of the locale currently used.
     * @return The {@link Locale_2_0_0_0} or null.
     */
    private @Nullable Locale_2_0_0_0 loadLegacyLocale(@NotNull String localeName) {
        ComponentLogger logger = skyShop.getComponentLogger();

        // Attempt to load the legacy locale config and return the result
        Path path = Path.of(skyShop.getDataFolder() + File.separator + "locale" + File.separator + (localeName + ".yml"));
        YamlConfigurationLoader loader = ConfigurationUtility.getYamlConfigurationLoader(path);
        try {
            return loader.load().get(Locale_2_0_0_0.class);
        } catch (ConfigurateException e) {
            logger.error(AdventureUtil.serialize("Failed to load legacy locale configuration. " + e.getMessage()));
            return null;
        }
    }

    /**
     * Replace any old placeholders with their new placeholders.
     * @param message The message to replace placeholders for.
     * @return The updated message. May be null if the original message was null.
     */
    private @Nullable String updatePlaceholders(@Nullable String message) {
        if(message == null) return null;

        message = message.replace("<price>", "<money>");
        message = message.replace("<bal>", "<money_balance>");

        return message;
    }

    /**
     * Checks if any locale strings are missing.
     * Sets locale to null if so, resulting in the default locale being used.
     */
    private void validateLocale() {
        if(locale == null) return;

        if (locale.prefix() == null
                || locale.help() == null
                || locale.configReload() == null
                || locale.notEnoughItems() == null
                || locale.insufficientMoney() == null
                || locale.buyItemSuccess().moneyAndPoints() == null
                || locale.buyItemSuccess().money() == null
                || locale.buyItemSuccess().points() == null
                || locale.sellItemSuccess().moneyAndPoints() == null
                || locale.sellItemSuccess().money() == null
                || locale.sellItemSuccess().points() == null
                || locale.buyCommandSuccess().moneyAndPoints() == null
                || locale.buyCommandSuccess().money() == null
                || locale.buyCommandSuccess().points() == null
                || locale.sellCommandSuccess().moneyAndPoints() == null
                || locale.sellCommandSuccess().money() == null
                || locale.sellCommandSuccess().points() == null
                || locale.sellallSuccess().moneyAndPoints() == null
                || locale.sellallSuccess().money() == null
                || locale.sellallSuccess().points() == null
                || locale.sellallUnsellable() == null
                || locale.unbuyable() == null
                || locale.unsellable() == null
                || locale.inGameOnly() == null
                || locale.guiOpenError() == null
                || locale.statsDisabledGuiError() == null
                || locale.transactionError() == null) {
            locale = null;

            skyShop.getComponentLogger().warn(AdventureUtil.serialize("Your locale configuration contains an invalid message. The default locale will be used."));
        }
    }
}
