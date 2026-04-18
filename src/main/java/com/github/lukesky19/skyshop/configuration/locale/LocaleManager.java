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
package com.github.lukesky19.skyshop.configuration.locale;

import com.github.lukesky19.skylib.common.api.adventure.AdventureUtility;
import com.github.lukesky19.skylib.common.api.configuration.abstracts.SimpleConfigManager;
import com.github.lukesky19.skylib.libs.configurate.ConfigurateException;
import com.github.lukesky19.skylib.libs.configurate.ConfigurationNode;
import com.github.lukesky19.skylib.libs.configurate.serialize.SerializationException;
import com.github.lukesky19.skylib.libs.configurate.yaml.YamlConfigurationLoader;
import com.github.lukesky19.skyshop.SkyShop;
import com.github.lukesky19.skyshop.configuration.locale.data.*;
import com.github.lukesky19.skyshop.configuration.settings.SettingsManager;
import com.github.lukesky19.skyshop.configuration.settings.data.SettingsV4;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.io.File;
import java.nio.file.Path;
import java.util.List;

/**
 * This class manages everything related to handling the plugin's locale configuration.
 */
public class LocaleManager extends SimpleConfigManager<LocaleV5> {
    private final @NonNull SettingsManager settingsManager;

    /**
     * The plugin's default locale. Used when the locale configuration is invalid.
     */
    private final @NonNull LocaleV5 DEFAULT_LOCALE = new LocaleV5(
            5,
            "<aqua><bold>SkyShop</bold></aqua><gray> ▪ </gray>",
            List.of("<aqua>SkyShop is developed by <white><bold>lukeskywlker19</bold></white>.</aqua>",
                    "<aqua>Source code is released on GitHub: <click:OPEN_URL:https://github.com/lukesky19><yellow><underlined><bold>https://github.com/lukesky19</bold></underlined></yellow></click></aqua>",
                    " ",
                    "<aqua><bold>List of Commands:</bold></aqua>",
                    "<white>/</white><aqua>shop</aqua>",
                    "<white>/</white><aqua>shop</aqua> <yellow>help</yellow>",
                    "<white>/</white><aqua>shop</aqua> <yellow>reload</yellow>",
                    "<white>/</white><aqua>shop</aqua> <yellow>sellall</yellow>",
                    "<white>/</white><aqua>shop</aqua> <yellow>stats</yellow>",
                    "<white>/</white><aqua>shop</aqua> <yellow>open <category></yellow>",
                    "<white>/</white><aqua>sell</aqua> <yellow>all</yellow>",
                    "<white>/</white><aqua>sell</aqua> <yellow>hand</yellow>",
                    "<white>/</white><aqua>sell</aqua> <yellow>hand all</yellow>"),
            "<aqua>Configuration files have been reloaded.</aqua>",
            "<red>You do not have enough items to sell.</red>",
            "<red>You lack the money to buy this item.</red>",
            "<red>You lack the player points to buy this item.</red>",
            new LocaleV5.SuccessMessages(
                    "<white>Purchased <yellow><amount> <transaction_name></yellow> for $<yellow><money></yellow> and <yellow><player_points></yellow> player points. Balance: <yellow><money_balance></yellow> Player Points: <yellow><player_points_balance></yellow></white>",
                    "<white>Purchased <yellow><amount> <transaction_name></yellow> for <yellow><money></yellow>. Balance: <yellow><money_balance></yellow></white>",
                    "<white>Purchased <yellow><amount> <transaction_name></yellow> for <yellow><player_points></yellow> player points. Player Points: <yellow><player_points_balance></yellow></white>"),
            new LocaleV5.SuccessMessages(
                    "<white>Sold <yellow><amount> <transaction_name></yellow> for $<yellow><money></yellow> and <yellow><player_points></yellow> player points. Balance: <yellow><money_balance></yellow> Player Points: <yellow><player_points_balance></yellow></white>",
                    "<white>Sold <yellow><amount> <transaction_name></yellow> for <yellow><money></yellow>. Balance: <yellow><money_balance></yellow></white>",
                    "<white>Sold <yellow><amount> <transaction_name></yellow> for <yellow><player_points></yellow> player points. Player Points: <yellow><player_points_balance></yellow></white>"),
            new LocaleV5.SuccessMessages(
                    "<white>Successfully sold all items for $<yellow><money></yellow> and <yellow><player_points></yellow> player points. Balance: <yellow><money_balance></yellow> Player Points: <yellow><player_points_balance></yellow></white>",
                    "<white>Successfully sold all items for $<yellow><money></yellow>. Updated Balance: <yellow><money_Balance></yellow></white>",
                    "<white>Successfully sold all items for <yellow><player_points></yellow> player points. Updated Balance: <yellow><player_points_balance></yellow></white>"),
            "<white>Unable to sell one or more items. It was added back to your inventory or dropped at your feet if full.</white>",
            "<red>This is not able to be purchased.</red>",
            "<red>This is not able to be sold.</red>",
            "<red>This command can only be ran in-game.</red>",
            "<red>Unable to open this GUI because of a configuration error.</red>",
            "<red>Unable to open the stats GUI as stats tracking is disabled.</red>",
            "<red>Unable to complete this transaction due to an error.</red>",
            new LocaleV5.IslandSizeMessages(
                    "<red>You must be on your island to buy or sell island size.</red>",
                    "<red>Your island is too small to sell any island size.</red>",
                    "<red>Your island is at the maximum size it can be expanded to.</red>"),
            "<red>You do not have permission to access this shop category.</red>",
            "<red>You do not have permission to access this button.</red>");
    private @Nullable LocaleV5 configuration;

    /**
     * Constructor
     * @param skyShop A {@link SkyShop} instance.
     * @param settingsManager A {@link SettingsManager} instance.
     */
    public LocaleManager(@NonNull SkyShop skyShop, @NonNull SettingsManager settingsManager) {
        super(skyShop, LocaleV5.class);

        this.settingsManager = settingsManager;
    }

    /**
     * Gets the plugin's locale if not null or the default locale otherwise.
     * @return The plugin's locale if not null or the default locale otherwise.
     */
    public @NonNull LocaleV5 getConfiguration() {
        if(configuration == null) return DEFAULT_LOCALE;
        return configuration;
    }

    /**
     * Load the locale configuration.
     */
    public void loadConfiguration() {
        configuration = null;

        saveDefaultConfiguration();

        SettingsV4 settings = settingsManager.getConfiguration();
        if(settings == null) {
            logger.warn(AdventureUtility.plain("Failed to load plugin's locale due to plugin settings being null."));
            return;
        }
        if(settings.locale() == null) {
            logger.warn(AdventureUtility.plain("Failed to load plugin's locale to use in settings.yml is null."));
            return;
        }

        String localeString = settings.locale();
        Path configurationPath = Path.of(plugin.getDirectoryFile() + File.separator + "locale" + File.separator + (localeString + ".yml"));

        YamlConfigurationLoader loader = createLoader(configurationPath);
        try {
            ConfigurationNode root = loader.load();
            int version = getVersion(root);

            LocaleV5 locale;
            switch(version) {
                case 5 -> {
                    locale = root.get(LocaleV5.class);
                    if(locale == null) {
                        logger.warn(AdventureUtility.plain("Failed to load version 5 file " + (localeString + ".yml") + ". Class name: " + this.getClass().getName()));
                        return;
                    }
                }

                case 4 -> {
                    LocaleV4 localeV4 = root.get(LocaleV4.class);
                    if(localeV4 == null) {
                        logger.warn(AdventureUtility.plain("Failed to load version 4 file " + (localeString + ".yml") + ". Class name: " + this.getClass().getName()));
                        return;
                    }

                    locale = new LocaleV5(
                            5,
                            localeV4.prefix(),
                            localeV4.help(),
                            localeV4.configReload(),
                            localeV4.notEnoughItems(),
                            localeV4.insufficientMoney(),
                            localeV4.insufficientPlayerPoints(),
                            new LocaleV5.SuccessMessages(
                                    localeV4.buySuccess().moneyAndPoints(),
                                    localeV4.buySuccess().money(),
                                    localeV4.buySuccess().points()),
                            new LocaleV5.SuccessMessages(
                                    localeV4.sellSuccess().moneyAndPoints(),
                                    localeV4.sellSuccess().money(),
                                    localeV4.sellSuccess().points()),
                            new LocaleV5.SuccessMessages(
                                    localeV4.sellallSuccess().moneyAndPoints(),
                                    localeV4.sellallSuccess().money(),
                                    localeV4.sellallSuccess().points()),
                            localeV4.sellallUnsellable(),
                            localeV4.unbuyable(),
                            localeV4.unsellable(),
                            localeV4.inGameOnly(),
                            localeV4.guiOpenError(),
                            localeV4.statsDisabledGuiError(),
                            localeV4.transactionError(),
                            new LocaleV5.IslandSizeMessages(
                                    localeV4.islandSizeMessages().notOnIsland(),
                                    localeV4.islandSizeMessages().islandTooSmall(),
                                    localeV4.islandSizeMessages().islandMaxSize()),
                            localeV4.categoryNoPermission(),
                            localeV4.buttonNoPermission());

                    saveConfiguration(configurationPath, locale);
                }

                case 3 -> {
                    LocaleV3 localeV3 = root.get(LocaleV3.class);
                    if(localeV3 == null) {
                        logger.warn(AdventureUtility.plain("Failed to load version 3 file " + (localeString + ".yml") + ". Class name: " + this.getClass().getName()));
                        return;
                    }

                    locale = new LocaleV5(
                            5,
                            localeV3.prefix(),
                            localeV3.help(),
                            localeV3.configReload(),
                            localeV3.notEnoughItems(),
                            localeV3.insufficientMoney(),
                            localeV3.insufficientPlayerPoints(),
                            new LocaleV5.SuccessMessages(
                                    localeV3.buyItemSuccess().moneyAndPoints(),
                                    localeV3.buyItemSuccess().money(),
                                    localeV3.buyItemSuccess().points()),
                            new LocaleV5.SuccessMessages(
                                    localeV3.sellItemSuccess().moneyAndPoints(),
                                    localeV3.sellItemSuccess().money(),
                                    localeV3.sellItemSuccess().points()),
                            new LocaleV5.SuccessMessages(
                                    localeV3.sellallSuccess().moneyAndPoints(),
                                    localeV3.sellallSuccess().money(),
                                    localeV3.sellallSuccess().points()),
                            localeV3.sellallUnsellable(),
                            localeV3.unbuyable(),
                            localeV3.unsellable(),
                            localeV3.inGameOnly(),
                            localeV3.guiOpenError(),
                            localeV3.statsDisabledGuiError(),
                            localeV3.transactionError(),
                            new LocaleV5.IslandSizeMessages(
                                    localeV3.islandSizeMessages().notOnIsland(),
                                    localeV3.islandSizeMessages().islandTooSmall(),
                                    localeV3.islandSizeMessages().islandMaxSize()),
                            localeV3.categoryNoPermission(),
                            localeV3.buttonNoPermission());

                    saveConfiguration(configurationPath, locale);
                }

                case 2 -> {
                    LocaleV2 localeV2 = root.get(LocaleV2.class);
                    if(localeV2 == null) {
                        logger.warn(AdventureUtility.plain("Failed to load version 2 file " + (localeString + ".yml") + ". Class name: " + this.getClass().getName()));
                        return;
                    }

                    locale = new LocaleV5(
                            5,
                            localeV2.prefix(),
                            List.of("<aqua>SkyShop is developed by <white><bold>lukeskywlker19</bold></white>.</aqua>",
                                    "<aqua>Source code is released on GitHub: <click:OPEN_URL:https://github.com/lukesky19><yellow><underlined><bold>https://github.com/lukesky19</bold></underlined></yellow></click></aqua>",
                                    " ",
                                    "<aqua><bold>List of Commands:</bold></aqua>",
                                    "<white>/</white><aqua>shop</aqua>",
                                    "<white>/</white><aqua>shop</aqua> <yellow>help</yellow>",
                                    "<white>/</white><aqua>shop</aqua> <yellow>reload</yellow>",
                                    "<white>/</white><aqua>shop</aqua> <yellow>sellall</yellow>",
                                    "<white>/</white><aqua>shop</aqua> <yellow>stats</yellow>",
                                    "<white>/</white><aqua>shop</aqua> <yellow>open <category></yellow>",
                                    "<white>/</white><aqua>sell</aqua> <yellow>all</yellow>",
                                    "<white>/</white><aqua>sell</aqua> <yellow>hand</yellow>",
                                    "<white>/</white><aqua>sell</aqua> <yellow>hand all</yellow>"),
                            localeV2.configReload(),
                            localeV2.notEnoughItems(),
                            localeV2.insufficientFunds(),
                            "<red>You lack the player points to buy this item.</red>",
                            new LocaleV5.SuccessMessages(
                                    "<white>Purchased <yellow><amount> <transaction_name></yellow> for $<yellow><money></yellow> and <yellow><player_points></yellow> player points. Balance: <yellow><money_balance></yellow> Player Points: <yellow><player_points_balance></yellow></white>",
                                    "<white>Purchased <yellow><amount> <transaction_name></yellow> for $<yellow><money></yellow>. Balance: <yellow><money_balance></yellow></white>",
                                    "<white>Purchased <yellow><amount> <transaction_name></yellow> for <yellow><player_points></yellow> player points. Player Points: <yellow><player_points_balance></yellow></white>"),
                            new LocaleV5.SuccessMessages(
                                    "<white>Sold <yellow><amount> <transaction_name></yellow> for $<yellow><money></yellow> and <yellow><player_points></yellow> player points. Balance: <yellow><money_balance></yellow> Player Points: <yellow><player_points_balance></yellow></white>",
                                    "<white>Sold <yellow><amount> <transaction_name></yellow> for $<yellow><money></yellow>. Balance: <yellow><money_balance></yellow></white>",
                                    "<white>Sold <yellow><amount> <transaction_name></yellow> for <yellow><player_points></yellow> player points. Player Points: <yellow><player_points_balance></yellow></white>"),
                            new LocaleV5.SuccessMessages(
                                    "<white>Successfully sold all items for $<yellow><money></yellow> and <yellow><player_points></yellow> player points. Balance: <yellow><money_balance></yellow> Player Points: <yellow><player_points_balance></yellow></white>",
                                    updatePlaceholders(localeV2.sellallSuccess()),
                                    "<white>Successfully sold all items for <yellow><player_points></yellow> player points. Updated Balance: <yellow><player_points_balance></yellow></white>"),
                            localeV2.sellallUnsellable(),
                            localeV2.unbuyable(),
                            localeV2.unsellable(),
                            localeV2.inGameOnly(),
                            localeV2.guiOpenError(),
                            localeV2.statsDisabledGuiError(),
                            "<red>Unable to complete this transaction due to an error.</red>",
                            new LocaleV5.IslandSizeMessages(
                                    "<red>You must be on your island to buy or sell island size.</red>",
                                    "<red>Your island is too small to sell any island size.</red>",
                                    "<red>Your island is at the maximum size it can be expanded to.</red>"),
                            "<red>You do not have permission to access this shop category.</red>",
                            "<red>You do not have permission to access this button.</red>");

                    saveConfiguration(configurationPath, locale);
                }

                case 1 -> {
                    LocaleV1 localeV1 = root.get(LocaleV1.class);
                    if(localeV1 == null) {
                        logger.warn(AdventureUtility.plain("Failed to load version 1 file " + (localeString + ".yml") + ". Class name: " + this.getClass().getName()));
                        return;
                    }

                    locale = new LocaleV5(
                            5,
                            localeV1.prefix(),
                            localeV1.help(),
                            localeV1.configReload(),
                            localeV1.notEnoughItems(),
                            localeV1.insufficientFunds(),
                            "<red>You lack the player points to buy this item.</red>",
                            new LocaleV5.SuccessMessages(
                                    "<white>Purchased <yellow><amount> <transaction_name></yellow> for $<yellow><money></yellow> and <yellow><player_points></yellow> player points. Balance: <yellow><money_balance></yellow> Player Points: <yellow><player_points_balance></yellow></white>",
                                    "<white>Purchased <yellow><amount> <transaction_name></yellow> for $<yellow><money></yellow>. Balance: <yellow><money_balance></yellow></white>",
                                    "<white>Purchased <yellow><amount> <transaction_name></yellow> for <yellow><player_points></yellow> player points. Player Points: <yellow><player_points_balance></yellow></white>"),
                            new LocaleV5.SuccessMessages(
                                    "<white>Sold <yellow><amount> <transaction_name></yellow> for $<yellow><money></yellow> and <yellow><player_points></yellow> player points. Balance: <yellow><money_balance></yellow> Player Points: <yellow><player_points_balance></yellow></white>",
                                    "<white>Sold <yellow><amount> <transaction_name></yellow> for $<yellow><money></yellow>. Balance: <yellow><money_balance></yellow></white>",
                                    "<white>Sold <yellow><amount> <transaction_name></yellow> for <yellow><player_points></yellow> player points. Player Points: <yellow><player_points_balance></yellow></white>"),
                            new LocaleV5.SuccessMessages(
                                    "<white>Successfully sold all items for $<yellow><money></yellow> and <yellow><player_points></yellow> player points. Balance: <yellow><money_balance></yellow> Player Points: <yellow><player_points_balance></yellow></white>",
                                    "<white>Successfully sold all items for $<yellow><money></yellow>. Updated Balance: <yellow><money_balance></yellow></white>",
                                    "<white>Successfully sold all items for <yellow><player_points></yellow> player points. Updated Balance: <yellow><player_points_balance></yellow></white>"),
                            localeV1.sellallUnsellable(),
                            localeV1.unbuyable(),
                            localeV1.unsellable(),
                            localeV1.inGameOnly(),
                            "<red>Unable to open this GUI because of a configuration error.</red>",
                            "<red>Unable to open the stats GUI as stats tracking is disabled.</red>",
                            "<red>Unable to complete this transaction due to an error.</red>",
                            new LocaleV5.IslandSizeMessages(
                                    "<red>You must be on your island to buy or sell island size.</red>",
                                    "<red>Your island is too small to sell any island size.</red>",
                                    "<red>Your island is at the maximum size it can be expanded to.</red>"),
                            "<red>You do not have permission to access this shop category.</red>",
                            "<red>You do not have permission to access this button.</red>");

                    saveConfiguration(configurationPath, locale);
                }

                default -> {
                    logger.warn(AdventureUtility.plain("Failed to load configuration file " + (localeString + ".yml") + " due to an unsupported config version. Version: " + version + " Class name: " + this.getClass().getName()));
                    return;
                }
            }

            // Check if the configuration is invalid
            if(!validateConfiguration(locale)) {
                logger.warn(AdventureUtility.plain("Configuration validation failed for file " + (localeString + ".yml") + ". Class name: " + this.getClass().getName()));
                return;
            }

            this.configuration = locale;
        } catch (ConfigurateException configurateException) {
            logger.error(AdventureUtility.plain("Failed to load configuration. Error: " + configurateException.getMessage()));
        }
    }

    @Override
    public @Nullable LocaleV5 migrateConfiguration(@NonNull LocaleV5 localeV5) {
        return localeV5;
    }

    /**
     * Save the default bundled locale configuration files.
     */
    @Override
    public void saveDefaultConfiguration() {
        Path path = Path.of(plugin.getDirectoryFile() + File.separator + "locale" + File.separator + "en_US.yml");
        if(!path.toFile().exists()) {
            plugin.saveResource("locale" + File.separator + "en_US.yml", false);
        }
    }

    /**
     * Save the configuration.
     * @param configurationPath The path to save to.
     * @param configuration The configuration.
     */
    public void saveConfiguration(@NonNull Path configurationPath, @NonNull LocaleV5 configuration) {
        try {
            YamlConfigurationLoader loader = createLoader(configurationPath);

            ConfigurationNode node = loader.createNode();

            node.set(LocaleV5.class, configuration);

            loader.save(node);
        } catch (ConfigurateException configurateException) {
            logger.error(AdventureUtility.plain("Failed to save locale configuration. Error: " + configurateException.getMessage()));
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
     * @param configuration The locale configuration to validate.
     * @return true if valid, false if not.
     */
    public boolean validateConfiguration(@Nullable LocaleV5 configuration) {
        if(configuration == null) return false;

        if (configuration.prefix() == null
                || configuration.help() == null
                || configuration.configReload() == null
                || configuration.notEnoughItems() == null
                || configuration.insufficientMoney() == null
                || configuration.insufficientPlayerPoints() == null
                || configuration.buySuccess().moneyAndPoints() == null
                || configuration.buySuccess().money() == null
                || configuration.buySuccess().points() == null
                || configuration.sellSuccess().moneyAndPoints() == null
                || configuration.sellSuccess().money() == null
                || configuration.sellSuccess().points() == null
                || configuration.sellallSuccess().moneyAndPoints() == null
                || configuration.sellallSuccess().money() == null
                || configuration.sellallSuccess().points() == null
                || configuration.sellallUnsellable() == null
                || configuration.unbuyable() == null
                || configuration.unsellable() == null
                || configuration.inGameOnly() == null
                || configuration.guiOpenError() == null
                || configuration.statsDisabledGuiError() == null
                || configuration.transactionError() == null
                || configuration.islandSizeMessages().notOnIsland() == null
                || configuration.islandSizeMessages().islandTooSmall() == null
                || configuration.islandSizeMessages().islandMaxSize() == null
                || configuration.categoryNoPermission() == null
                || configuration.buttonNoPermission() == null) {
            this.configuration = null;

            logger.warn(AdventureUtility.plain("Your locale configuration contains an invalid message. The default locale will be used."));
            return false;
        }

        return true;
    }

    /**
     * Get the version number.
     * @param root The root {@link ConfigurationNode}.
     * @return The config version.
     */
    private int getVersion(@NonNull ConfigurationNode root) {
        ConfigurationNode versionNode = root.node("version");
        int version = versionNode.getInt();

        ConfigurationNode legacyVersionNode = root.node("config-version");
        String legacyVersion = legacyVersionNode.virtual() ? null : legacyVersionNode.getString();
        if(legacyVersion != null) {
            try {
                switch (legacyVersion) {
                    case "3.0.0.0" -> {
                        versionNode.set(4);
                        version = 4;
                    }

                    case "2.1.0.0" -> {
                        versionNode.set(3);
                        version = 3;
                    }

                    case "2.0.0.0" -> {
                        versionNode.set(2);
                        version = 2;
                    }

                    default -> {
                        versionNode.set(1);
                        version = 1;
                    }
                }
            } catch (SerializationException e) {
                logger.warn(AdventureUtility.plain("Failed to convert String-based version to numeric version"));
                version = 0;
            }
        }

        return version;
    }
}