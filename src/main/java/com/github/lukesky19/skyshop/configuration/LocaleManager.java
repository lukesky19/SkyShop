/*
    SkyShop is a simple inventory based shop plugin with page support, sell commands, and error checking.
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
package com.github.lukesky19.skyshop.configuration;

import com.github.lukesky19.skylib.api.adventure.AdventureUtil;
import com.github.lukesky19.skylib.api.configurate.ConfigurationUtility;
import com.github.lukesky19.skylib.libs.configurate.ConfigurateException;
import com.github.lukesky19.skylib.libs.configurate.yaml.YamlConfigurationLoader;
import com.github.lukesky19.skyshop.SkyShop;
import com.github.lukesky19.skyshop.data.Locale;
import com.github.lukesky19.skyshop.data.Settings;
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
            "2.0.0.0",
            "<aqua><bold>SkyShop</bold></aqua><gray> ▪ </gray>",
            List.of("<aqua>SkyShop is developed by <white><bold>lukeskywlker19</bold></white>.</aqua>",
                    "<aqua>Source code is released on GitHub: <click:OPEN_URL:https://github.com/lukesky19><yellow><underlined><bold>https://github.com/lukesky19</bold></underlined></yellow></click></aqua>",
                    " ",
                    "<aqua><bold>List of Commands:</bold></aqua>",
                    "<white>/<aqua>skyshop</white>",
                    "<white>/<aqua>skyshop <yellow>help</yellow></white>",
                    "<white>/<aqua>skyshop <yellow>reload</yellow></white>",
                    "<white>/<aqua>skyshop <yellow>sellall</yellow></white>",
                    "<white>/<aqua>sell <yellow>all</yellow></white>",
                    "<white>/<aqua>sell <yellow>hand</yellow></white>",
                    "<white>/<aqua>sell <yellow>hand all</yellow></white>"),
            "<aqua>Configuration files have been reloaded.</aqua>",
            "<red>You do not have enough items to sell.</red>",
            "<red>Insufficient funds.</red>",
            "<white>Purchased <yellow><amount> <transaction_name></yellow> for <yellow><price></yellow>. Balance: <yellow><bal></yellow></white>",
            "<white>Sold <yellow><amount> <transaction_name></yellow> for <yellow><price></yellow>. Balance: <yellow><bal></yellow></white>",
            "<white>Purchased <yellow><transaction_name></yellow> for <yellow><price></yellow>. Balance: <yellow><bal></yellow>",
            "<white>Sold <yellow><transaction_name></yellow> for <yellow><price></yellow>. Balance: <yellow><bal></yellow>",
            "<white>Successfully sold all items for <yellow><price></yellow>. Updated Balance: <yellow><bal></yellow></white>",
            "<white>Unable to sell one or more items. It was added back to your inventory or dropped at your feet if full.</white>",
            "<red>This is not able to be purchased.</red>",
            "<red>This is not able to be sold.</red>",
            "<red>This command can only be ran in-game.</red>",
            "<red>Unable to open this GUI because of a configuration error.</red>",
            "<red>Unable to open the stats GUI as stats tracking is disabled.</red>");

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

            validateLocale();
        } catch (ConfigurateException e) {
            logger.error(AdventureUtil.serialize("Failed to load locale configuration. " + e.getMessage()));
        }
    }

    /**
     * Saves the default locale files that come bundled with the plugin, if they do not exist at least.
    */
    public void saveDefaultLocales() {
        Path path = Path.of(skyShop.getDataFolder() + File.separator + "locale" + File.separator + "en_US.yml");
        if (!path.toFile().exists()) {
            skyShop.saveResource("locale" + File.separator + "en_US.yml", false);
        }
    }

    /**
     * Checks if any locale strings are missing.
     * Sets locale to null if so, resulting in the default locale being used.
     */
    public void validateLocale() {
        if(locale == null) return;

        if (locale.prefix() == null
                || locale.help() == null
                || locale.configReload() == null
                || locale.notEnoughItems() == null
                || locale.insufficientFunds() == null
                || locale.buyItemSuccess() == null
                || locale.sellItemSuccess() == null
                || locale.buyCommandSuccess() == null
                || locale.sellCommandSuccess() == null
                || locale.sellallSuccess() == null
                || locale.sellallUnsellable() == null
                || locale.unbuyable() == null
                || locale.unsellable() == null
                || locale.inGameOnly() == null
                || locale.guiOpenError() == null
                || locale.statsDisabledGuiError() == null) {
            locale = null;

            skyShop.getComponentLogger().warn(AdventureUtil.serialize("Your locale configuration contains an invalid message. The default locale will be used."));
        }
    }
}
