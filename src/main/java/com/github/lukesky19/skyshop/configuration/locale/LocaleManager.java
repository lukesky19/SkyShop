/*
    SkyShop is a simple inventory based shop plugin with page support, error checking, and configuration validation.
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
package com.github.lukesky19.skyshop.configuration.locale;

import com.github.lukesky19.skyshop.SkyShop;
import com.github.lukesky19.skyshop.configuration.ConfigurationUtility;
import com.github.lukesky19.skyshop.configuration.settings.SettingsManager;
import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;

/**
 * This class manages everything related to handling the plugin's locale configuration.
 */
public class LocaleManager {
    final SkyShop skyShop;
    final SettingsManager settingsManager;
    final LocaleValidator localeValidator;
    FormattedLocale formattedLocale;
    final ConfigurationUtility configurationUtility;

    /**
     * Constructor
     * @param skyShop The plugin's instance.
     * @param configurationUtility A configuration utility instance.
     * @param settingsManager A settings manager instance.
     * @param localeValidator A locale validator instance.
    */
    public LocaleManager(SkyShop skyShop, ConfigurationUtility configurationUtility, SettingsManager settingsManager, LocaleValidator localeValidator) {
        this.skyShop = skyShop;
        this.configurationUtility = configurationUtility;
        this.settingsManager = settingsManager;
        this.localeValidator = localeValidator;
    }

    /**
     * A getter to get the formatted locale.
     * @return A FormattedLocale object.
    */
    public FormattedLocale formattedLocale() {
        return formattedLocale;
    }

    /**
     * A method to reload the plugin's locale config.
    */
    public void reload() {
        formattedLocale = null;
        if(!skyShop.isPluginEnabled()) {
            return;
        }

        copyDefaultLocales();

        String locale = settingsManager.getSettingsConfig().locale() + ".yml";
        Path path = Path.of(skyShop.getDataFolder() + File.separator + "locale" + File.separator + locale);

        LocaleConfiguration localeConfiguration = null;
        YamlConfigurationLoader loader = configurationUtility.getYamlConfigurationLoader(path);
        try {
            localeConfiguration = loader.load().get(LocaleConfiguration.class);
        } catch (ConfigurateException ignored) {}

        if(!localeValidator.isLocaleValid(localeConfiguration, locale)) {
            skyShop.setPluginState(false);
        } else {
            formattedLocale = decorateLocale(localeConfiguration);
            if(formattedLocale == null) {
                skyShop.setPluginState(false);
            }
        }
    }

    /**
     * Copies the default locale files that come bundled with the plugin, if they do not exist at least.
    */
    private void copyDefaultLocales() {
        Path path = Path.of(skyShop.getDataFolder() + File.separator + "locale" + File.separator + "en_US.yml");
        if (!path.toFile().exists()) {
            skyShop.saveResource("locale/en_US.yml", false);
        }
    }

    /**
     * Creates a FormattedLocale object that represents all the configurable plugin messages.
     * @param localeConfiguration An unformatted locale configuration.
     * @return A FormattedLocale object.
     */
    private FormattedLocale decorateLocale(LocaleConfiguration localeConfiguration) {
        if(localeConfiguration == null) return null;
        MiniMessage mm = MiniMessage.miniMessage();
        List<Component> help = new ArrayList<>();
        for(String msg : localeConfiguration.help()) {
            help.add(mm.deserialize(msg));
        }

        return new FormattedLocale(
                mm.deserialize(localeConfiguration.prefix()),
                help,
                mm.deserialize(localeConfiguration.noPermission()),
                mm.deserialize(localeConfiguration.configReload()),
                mm.deserialize(localeConfiguration.notEnoughItems()),
                mm.deserialize(localeConfiguration.insufficientFunds()),
                localeConfiguration.buySuccess(),
                localeConfiguration.sellSuccess(),
                localeConfiguration.sellallSuccess(),
                mm.deserialize(localeConfiguration.sellallUnsellable()),
                mm.deserialize(localeConfiguration.unbuyable()),
                mm.deserialize(localeConfiguration.unsellable()),
                mm.deserialize(localeConfiguration.inGameOnly()),
                mm.deserialize(localeConfiguration.unknownArgument()));
    }
}
