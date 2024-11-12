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
import com.github.lukesky19.skylib.libs.configurate.CommentedConfigurationNode;
import com.github.lukesky19.skylib.libs.configurate.ConfigurateException;
import com.github.lukesky19.skylib.libs.configurate.yaml.YamlConfigurationLoader;
import com.github.lukesky19.skyshop.SkyShop;
import com.github.lukesky19.skyshop.configuration.record.Settings;

import java.io.File;
import java.nio.file.Path;

/**
 * This class manages everything related to handling the plugin's settings.
*/
public class SettingsManager {
    final SkyShop skyShop;
    Settings settingsConfig;

    /**
     * Constructor
     * @param skyShop The plugin's instance.
    */
    public SettingsManager(SkyShop skyShop) {
        this.skyShop = skyShop;
    }

    /**
     * A getter to get the settings configuration.
     * @return A Settings object.
    */
    public Settings getSettingsConfig() {
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

        YamlConfigurationLoader loader = ConfigurationUtility.getYamlConfigurationLoader(path);
        try {
            settingsConfig = loader.load().get(Settings.class);
        } catch (ConfigurateException e) {
            throw new RuntimeException(e);
        }

        saveExampleShop();
    }

    /**
     * This edits the settings.yml file to set `first-run` to false.
    */
    public void setFirstRunFalse() {
        Settings newSettings = new Settings(settingsConfig.configVersion(), false, settingsConfig.locale(), settingsConfig.statistics());
        settingsConfig = newSettings;

        Path path = Path.of(skyShop.getDataFolder() + File.separator + "settings.yml");
        YamlConfigurationLoader loader = ConfigurationUtility.getYamlConfigurationLoader(path);

        try {
            CommentedConfigurationNode settingsNode = loader.load();
            settingsNode.set(Settings.class, newSettings);
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
