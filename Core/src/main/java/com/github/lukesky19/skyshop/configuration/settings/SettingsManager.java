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
package com.github.lukesky19.skyshop.configuration.settings;

import com.github.lukesky19.skyshop.SkyShop;
import com.github.lukesky19.skyshop.configuration.ConfigurationUtility;

import java.io.File;
import java.nio.file.Path;

import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;

/**
 * This class manages everything related to handling the plugin's settings.
*/
public class SettingsManager {
    final SkyShop skyShop;
    final SettingsValidator settingsValidator;
    final ConfigurationUtility configurationUtility;
    SettingsConfiguration settingsConfig;

    /**
     * Constructor
     * @param skyShop The plugin's instance.
     * @param settingsValidator A SettingsValidator instance.
     * @param configurationUtility A ConfigurationUtility instance.
    */
    public SettingsManager(SkyShop skyShop, SettingsValidator settingsValidator, ConfigurationUtility configurationUtility) {
        this.skyShop = skyShop;
        this.settingsValidator = settingsValidator;
        this.configurationUtility = configurationUtility;
    }

    /**
     * A getter to get the settings configuration.
     * @return A SettingsConfiguration object.
    */
    public SettingsConfiguration getSettingsConfig() {
        return settingsConfig;
    }

    /**
     * A method to reload the plugin's settings config.
    */
    public void reload() {
        settingsConfig = null;
        Path path = Path.of(skyShop.getDataFolder() + File.separator + "settings.yml");
        if(!path.toFile().exists()) {
            skyShop.saveResource("settings.yml", false);
        }

        YamlConfigurationLoader loader = configurationUtility.getYamlConfigurationLoader(path);
        try {
            settingsConfig = loader.load().get(SettingsConfiguration.class);
        } catch (ConfigurateException ignored) {}

        if(!settingsValidator.isSettingsValid(settingsConfig)) {
            skyShop.setPluginState(false);
        }

        saveExampleShop();
    }

    /**
     * This edits the settings.yml file to set `first-run` to false.
    */
    public void setFirstRunFalse() {
        SettingsConfiguration newSettings = new SettingsConfiguration(false, settingsConfig.debug(), settingsConfig.locale());
        settingsConfig = newSettings;

        Path path = Path.of(skyShop.getDataFolder() + File.separator + "settings.yml");
        YamlConfigurationLoader loader = configurationUtility.getYamlConfigurationLoader(path);

        try {
            CommentedConfigurationNode settingsNode = loader.load();
            settingsNode.set(SettingsConfiguration.class, newSettings);
            loader.save(settingsNode);
        } catch (ConfigurateException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Saves the example shop file if it does not exist.
     * Only does so if first-run in settings.yml is true.
     */
    public void saveExampleShop() {
        Path path = Path.of(skyShop.getDataFolder() + File.separator + "shops" + File.separator + "example.yml");
        if(!path.toFile().exists()) {
            skyShop.saveResource("shops" + File.separator + "example.yml", false);
            setFirstRunFalse();
        }
    }
}
