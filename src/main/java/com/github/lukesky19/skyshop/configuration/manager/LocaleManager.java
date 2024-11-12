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
package com.github.lukesky19.skyshop.configuration.manager;

import com.github.lukesky19.skylib.config.ConfigurationUtility;
import com.github.lukesky19.skylib.libs.configurate.ConfigurateException;
import com.github.lukesky19.skylib.libs.configurate.yaml.YamlConfigurationLoader;
import com.github.lukesky19.skyshop.SkyShop;
import com.github.lukesky19.skyshop.configuration.record.Locale;
import com.github.lukesky19.skyshop.configuration.record.Settings;

import java.io.File;
import java.nio.file.Path;
import java.util.List;

/**
 * This class manages everything related to handling the plugin's locale configuration.
 */
public class LocaleManager {
    private final SkyShop skyShop;
    private final SettingsManager settingsManager;
    private Locale locale;
    public final Locale DEFAULT_LOCALE = new Locale(
            "2.0.0",
            "<gray>[</gray><aqua><bold>SkyShop</bold></aqua><gray>]</gray> ",
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
            "<red>You do not have permission for this command.</red>",
            "<aqua>Configuration files have been reloaded.</aqua>",
            "<red>You do not have enough items to sell.</red>",
            "<red>Insufficient funds.</red>",
            "<white>Purchased <yellow><amount> <item></yellow> for <yellow><price></yellow>. Balance: <yellow><bal></yellow></white>",
            "<white>Sold <yellow><amount> <item></yellow> for <yellow><price></yellow>. Balance: <yellow><bal></yellow></white>",
            "<white>Successfully sold all items for <yellow><price></yellow>. Updated Balance: <yellow><bal></yellow></white>",
            "<white>Unable to sell one or more items. It was added back to your inventory or dropped at your feet if full.</white>",
            "<red>This item is not able to be purchased.</red>",
            "<red>This item is not able to be sold.</red>",
            "<red>This command can only be ran in-game.</red>",
            "<red>One or more of the arguments sent is not recognized.</red>",
            "<red>Unable to open this gui because it's configuration does not exist, failed to load, or is otherwise invalid.</red>",
            "<red>The size of the gui is invalid for <white><file></white>. It must be a multiple of 9 while also being greater than or equal to 9 and less than or equal to 54.</red>",
            "<red>The size of the gui is invalid for page <white><page></white> in <white><file></white>. It must be a multiple of 9 while also being greater than or equal to 9 and less than or equal to 54.</red>",
            "<yellow>Skipping entry <white><entry></white> for page <white><page></white> in <white><file></white> due to an invalid type.</yellow>",
            "<yellow>Skipping entry <white><entry></white> for page <white><page></white> in <white><file></white> as the type is not allowed in this context.</yellow>",
            "<red>Skipping entry <white><entry></white> for page <white><page></white> in <white><file></white> due to an invalid material.</red>",
            "<red>No pages were found in <white><file></white>.</red>",
            "<red>No entries were found for page <white><page></white> in file <white><file></white>.</red>");

    /**
     * Constructor
     * @param skyShop The plugin's instance.
     * @param settingsManager A settings manager instance.
    */
    public LocaleManager(SkyShop skyShop, SettingsManager settingsManager) {
        this.skyShop = skyShop;
        this.settingsManager = settingsManager;
    }

    /**
     * Gets the plugin's locale or default locale.
     * If the plugin's locale config failed to load, the default locale will be provided.
     * @return A Locale object.
    */
    public Locale getLocale() {
        if(locale == null) return DEFAULT_LOCALE;

        return locale;
    }

    /**
     * A method to reload the plugin's locale config.
    */
    public void reload() {
        final Settings settings = settingsManager.getSettingsConfig();

        locale = null;

        copyDefaultLocales();

        String localeString = settings.locale();

        if(localeString != null) {
            Path path = Path.of(skyShop.getDataFolder() + File.separator + "locale" + File.separator + (localeString + ".yml"));

            YamlConfigurationLoader loader = ConfigurationUtility.getYamlConfigurationLoader(path);
            try {
                locale = loader.load().get(Locale.class);

                checkLocale();
            } catch (ConfigurateException e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * Copies the default locale files that come bundled with the plugin, if they do not exist at least.
    */
    private void copyDefaultLocales() {
        Path path = Path.of(skyShop.getDataFolder() + File.separator + "locale" + File.separator + "en_US.yml");
        if (!path.toFile().exists()) {
            skyShop.saveResource("locale" + File.separator + "en_US.yml", false);
        }
    }

    /**
     * Checks if any locale strings are missing.
     * Sets locale to null if so, resulting in the default locale being used.
     */
    private void checkLocale() {
        if (locale.prefix() == null
                || locale.help() == null
                || locale.noPermission() == null
                || locale.configReload() == null
                || locale.notEnoughItems() == null
                || locale.insufficientFunds() == null
                || locale.buySuccess() == null
                || locale.sellSuccess() == null
                || locale.sellallSuccess() == null
                || locale.sellallUnsellable() == null
                || locale.unbuyable() == null
                || locale.unsellable() == null
                || locale.inGameOnly() == null
                || locale.unknownArgument() == null
                || locale.guiOpenError() == null
                || locale.guiInvalidSize() == null
                || locale.guiInvalidPageSize() == null
                || locale.skippingEntryInvalidType() == null
                || locale.skippingEntryTypeNotAllowed() == null
                || locale.skippingEntryInvalidMaterial() == null
                || locale.noPagesFound() == null
                || locale.noEntriesFound() == null) {
            locale = null;
        }
    }
}
