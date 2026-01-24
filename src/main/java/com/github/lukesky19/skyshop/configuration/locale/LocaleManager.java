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

import com.github.lukesky19.skylib.api.adventure.AdventureUtil;
import com.github.lukesky19.skylib.api.common.abstracts.SkyPlugin;
import com.github.lukesky19.skylib.api.common.abstracts.config.SimpleConfigManager;
import com.github.lukesky19.skylib.api.configurate.ConfigurationUtility;
import com.github.lukesky19.skylib.libs.configurate.ConfigurateException;
import com.github.lukesky19.skylib.libs.configurate.ConfigurationNode;
import com.github.lukesky19.skylib.libs.configurate.yaml.YamlConfigurationLoader;
import com.github.lukesky19.skyshop.configuration.settings.Settings;
import com.github.lukesky19.skyshop.configuration.settings.SettingsManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

import java.io.File;
import java.nio.file.Path;
import java.util.List;

/**
 * This class manages everything related to handling the plugin's locale configuration.
 */
public class LocaleManager extends SimpleConfigManager<Locale> {
    private final @NotNull SettingsManager settingsManager;
    /**
     * The plugin's default locale. Used when the locale configuration is invalid.
     */
    public final @NotNull Locale DEFAULT_LOCALE = new Locale(
            "3.0.0.0",
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
            new Locale.SuccessMessages(
                    "<white>Purchased <yellow><amount> <transaction_name></yellow> for $<yellow><money></yellow> and <yellow><player_points></yellow> player points. Balance: <yellow><money_balance></yellow> Player Points: <yellow><player_points_balance></yellow></white>",
                    "<white>Purchased <yellow><amount> <transaction_name></yellow> for <yellow><money></yellow>. Balance: <yellow><money_balance></yellow></white>",
                    "<white>Purchased <yellow><amount> <transaction_name></yellow> for <yellow><player_points></yellow> player points. Player Points: <yellow><player_points_balance></yellow></white>"),
            new Locale.SuccessMessages(
                    "<white>Sold <yellow><amount> <transaction_name></yellow> for $<yellow><money></yellow> and <yellow><player_points></yellow> player points. Balance: <yellow><money_balance></yellow> Player Points: <yellow><player_points_balance></yellow></white>",
                    "<white>Sold <yellow><amount> <transaction_name></yellow> for <yellow><money></yellow>. Balance: <yellow><money_balance></yellow></white>",
                    "<white>Sold <yellow><amount> <transaction_name></yellow> for <yellow><player_points></yellow> player points. Player Points: <yellow><player_points_balance></yellow></white>"),
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
            "<red>Unable to complete this transaction due to an error.</red>",
            new Locale.IslandSizeMessages(
                    "<red>You must be on your island to buy or sell island size.</red>",
                    "<red>Your island is too small to sell any island size.</red>",
                    "<red>Your island is at the maximum size it can be expanded to.</red>"),
            "<red>You do not have permission to access this shop category.</red>",
            "<red>You do not have permission to access this button.</red>");

    /**
     * Constructor
     * @param plugin A {@link SkyPlugin}.
     * @param settingsManager A {@link SettingsManager} instance.
     */
    public LocaleManager(@NotNull SkyPlugin plugin, @NotNull SettingsManager settingsManager) {
        super(plugin, Locale.class);
        this.settingsManager = settingsManager;
    }

    /**
     * Gets the plugin's locale if not null or the default locale otherwise.
     * @return The plugin's locale if not null or the default locale otherwise.
     */
    @Override
    public @NotNull Locale getConfiguration() {
        if(configuration == null) return DEFAULT_LOCALE;
        return configuration;
    }

    @Override
    public void loadConfiguration() {
        Settings settings = settingsManager.getConfiguration();
        if(settings == null) {
            logger.warn(AdventureUtil.deserialize("Failed to load plugin's locale due to plugin settings being null."));
            return;
        }
        if(settings.locale() == null) {
            logger.warn(AdventureUtil.deserialize("Failed to load plugin's locale to use in settings.yml is null."));
            return;
        }

        String localeString = settings.locale();
        Path path = Path.of(plugin.getDataFolder() + File.separator + "locale" + File.separator + (localeString + ".yml"));
        setConfigurationPath(path);

        configuration = null;
        if(configurationPath == null) {
            logger.warn(AdventureUtil.deserialize("Unable to load configuration because the configuration path was not set."));
            return;
        }

        if(!configurationPath.toFile().exists()) {
            saveBundledConfig();
        }

        YamlConfigurationLoader loader = ConfigurationUtility.getYamlConfigurationLoader(configurationPath);
        try {
            ConfigurationNode root = loader.load();
            ConfigurationNode versionNode = root.node("config-version");
            @Nullable String configVersion = versionNode.virtual() ? null : versionNode.getString();

            @Nullable Locale locale;
            switch(configVersion) {
                case "3.0.0.0" -> {
                    locale = root.get(Locale.class);
                    if(locale == null) {
                        logger.warn(AdventureUtil.deserialize("Failed to load version 3.0.0.0 or newer configuration file " + (localeString + ".yml") + ". Class name: " + this.getClass().getName()));
                        return;
                    }
                }

                case "2.1.0.0" -> {
                    @Nullable Locale_2_1_0_0 locale_2_1_0_0 = root.get(Locale_2_1_0_0.class);
                    if(locale_2_1_0_0 == null) {
                        logger.warn(AdventureUtil.deserialize("Failed to load version 2.1.0.0 configuration file " + (localeString + ".yml") + ". Class name: " + this.getClass().getName()));
                        return;
                    }

                    locale = new Locale(
                            "3.0.0.0",
                            locale_2_1_0_0.prefix(),
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
                            locale_2_1_0_0.configReload(),
                            locale_2_1_0_0.notEnoughItems(),
                            locale_2_1_0_0.insufficientMoney(),
                            locale_2_1_0_0.insufficientPlayerPoints(),
                            new Locale.SuccessMessages(
                                    locale_2_1_0_0.buyItemSuccess().moneyAndPoints(),
                                    locale_2_1_0_0.buyItemSuccess().money(),
                                    locale_2_1_0_0.buyItemSuccess().points()),
                            new Locale.SuccessMessages(
                                    locale_2_1_0_0.sellItemSuccess().moneyAndPoints(),
                                    locale_2_1_0_0.sellItemSuccess().money(),
                                    locale_2_1_0_0.sellItemSuccess().points()),
                            new Locale.SuccessMessages(
                                    locale_2_1_0_0.sellallSuccess().moneyAndPoints(),
                                    locale_2_1_0_0.sellallSuccess().money(),
                                    locale_2_1_0_0.sellallSuccess().points()),
                            locale_2_1_0_0.sellallUnsellable(),
                            locale_2_1_0_0.unbuyable(),
                            locale_2_1_0_0.unsellable(),
                            locale_2_1_0_0.inGameOnly(),
                            locale_2_1_0_0.guiOpenError(),
                            locale_2_1_0_0.statsDisabledGuiError(),
                            locale_2_1_0_0.transactionError(),
                            new Locale.IslandSizeMessages(
                                    locale_2_1_0_0.islandSizeMessages().notOnIsland(),
                                    locale_2_1_0_0.islandSizeMessages().islandTooSmall(),
                                    locale_2_1_0_0.islandSizeMessages().islandMaxSize()),
                            locale_2_1_0_0.categoryNoPermission(),
                            locale_2_1_0_0.buttonNoPermission());

                    // Save updated configuration
                    saveConfiguration(locale);

                    this.configuration = locale;

                    return;
                }

                case "2.0.0.0" -> {
                    @Nullable Locale_2_0_0_0 locale_2_0_0_0 = root.get(Locale_2_0_0_0.class);
                    if(locale_2_0_0_0 == null) {
                        logger.warn(AdventureUtil.deserialize("Failed to load version 2.0.0.0 configuration file " + (localeString + ".yml") + ". Class name: " + this.getClass().getName()));
                        return;
                    }

                    locale = new Locale(
                            "3.0.0.0",
                            locale_2_0_0_0.prefix(),
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
                            locale_2_0_0_0.configReload(),
                            locale_2_0_0_0.notEnoughItems(),
                            locale_2_0_0_0.insufficientFunds(),
                            "<red>You lack the player points to buy this item.</red>",
                            new Locale.SuccessMessages(
                                    "<white>Purchased <yellow><amount> <transaction_name></yellow> for $<yellow><money></yellow> and <yellow><player_points></yellow> player points. Balance: <yellow><money_balance></yellow> Player Points: <yellow><player_points_balance></yellow></white>",
                                    "<white>Purchased <yellow><amount> <transaction_name></yellow> for $<yellow><money></yellow>. Balance: <yellow><money_balance></yellow></white>",
                                    "<white>Purchased <yellow><amount> <transaction_name></yellow> for <yellow><player_points></yellow> player points. Player Points: <yellow><player_points_balance></yellow></white>"),
                            new Locale.SuccessMessages(
                                    "<white>Sold <yellow><amount> <transaction_name></yellow> for $<yellow><money></yellow> and <yellow><player_points></yellow> player points. Balance: <yellow><money_balance></yellow> Player Points: <yellow><player_points_balance></yellow></white>",
                                    "<white>Sold <yellow><amount> <transaction_name></yellow> for $<yellow><money></yellow>. Balance: <yellow><money_balance></yellow></white>",
                                    "<white>Sold <yellow><amount> <transaction_name></yellow> for <yellow><player_points></yellow> player points. Player Points: <yellow><player_points_balance></yellow></white>"),
                            new Locale.SuccessMessages(
                                    "<white>Successfully sold all items for $<yellow><money></yellow> and <yellow><player_points></yellow> player points. Balance: <yellow><money_balance></yellow> Player Points: <yellow><player_points_balance></yellow></white>",
                                    updatePlaceholders(locale_2_0_0_0.sellallSuccess()),
                                    "<white>Successfully sold all items for <yellow><player_points></yellow> player points. Updated Balance: <yellow><player_points_balance></yellow></white>"),
                            locale_2_0_0_0.sellallUnsellable(),
                            locale_2_0_0_0.unbuyable(),
                            locale_2_0_0_0.unsellable(),
                            locale_2_0_0_0.inGameOnly(),
                            locale_2_0_0_0.guiOpenError(),
                            locale_2_0_0_0.statsDisabledGuiError(),
                            "<red>Unable to complete this transaction due to an error.</red>",
                            new Locale.IslandSizeMessages(
                                    "<red>You must be on your island to buy or sell island size.</red>",
                                    "<red>Your island is too small to sell any island size.</red>",
                                    "<red>Your island is at the maximum size it can be expanded to.</red>"),
                            "<red>You do not have permission to access this shop category.</red>",
                            "<red>You do not have permission to access this button.</red>");

                    // Save updated configuration
                    saveConfiguration(locale);

                    this.configuration = locale;

                    return;
                }

                case null -> {
                    logger.warn(AdventureUtil.deserialize("Failed to load configuration file " + (localeString + ".yml") + " due to a null config version. Class name: " + this.getClass().getName()));
                    return;
                }

                default -> {
                    logger.warn(AdventureUtil.deserialize("Failed to load configuration file " + (localeString + ".yml") + " due to an unsupported config version. Class name: " + this.getClass().getName()));
                    return;
                }
            }

            // Migrate configuration
            @Nullable Locale migratedLocale = migrateConfiguration(locale);
            // If migration failed, return
            if(migratedLocale == null) {
                logger.warn(AdventureUtil.deserialize("Configuration migration failed for file " + (localeString + ".yml") + ". Class name: " + this.getClass().getName()));
                return;
            }

            // Check if the configuration is invalid
            if(!validateConfiguration(migratedLocale)) {
                logger.warn(AdventureUtil.deserialize("Configuration validation failed for file " + (localeString + ".yml") + ". Class name: " + this.getClass().getName()));
                return;
            }

            // Save the migrated configuration if different
            if(migratedLocale != locale) {
                saveConfiguration(migratedLocale);
            }

            this.configuration = migratedLocale;
        } catch (ConfigurateException configurateException) {
            logger.error(AdventureUtil.deserialize("Failed to load configuration. Error: " + configurateException.getMessage()));
        }
    }

    @Override
    public void saveBundledConfig() {
        Path path = Path.of(plugin.getDataFolder() + File.separator + "locale" + File.separator + "en_US.yml");
        if(!path.toFile().exists()) {
            plugin.saveResource("locale" + File.separator + "en_US.yml", false);
        }
    }

    @Override
    public @Nullable Locale migrateConfiguration(@NonNull Locale configuration) {
        switch(configuration.configVersion()) {
            case "3.0.0.0" -> {
                // Latest Version, do nothing
                return configuration;
            }

            case "2.1.0.0", "2.0.0.0" -> {
                logger.warn(AdventureUtil.deserialize("Version 2 configuration cannot be migrated by this method. Class name: " + this.getClass().getName()));
                logger.info(AdventureUtil.deserialize("It should of been migrated before this point."));
                return null;
            }

            case null -> {
                logger.warn(AdventureUtil.deserialize("Failed to migrate configuration due to a null config version. Class name: " + this.getClass().getName()));
                return null;
            }

            default -> {
                logger.warn(AdventureUtil.deserialize("Failed to migrate configuration due to an unsupported config version. Class name: " + this.getClass().getName()));
                return null;
            }
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
    @Override
    public boolean validateConfiguration(@Nullable Locale configuration) {
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

            logger.warn(AdventureUtil.deserialize("Your locale configuration contains an invalid message. The default locale will be used."));
            return false;
        }

        return true;
    }
}