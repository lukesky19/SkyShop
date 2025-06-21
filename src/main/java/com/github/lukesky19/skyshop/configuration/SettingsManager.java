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

import com.github.lukesky19.skylib.api.configurate.ConfigurationUtility;
import com.github.lukesky19.skylib.libs.configurate.CommentedConfigurationNode;
import com.github.lukesky19.skylib.libs.configurate.ConfigurateException;
import com.github.lukesky19.skylib.libs.configurate.yaml.YamlConfigurationLoader;
import com.github.lukesky19.skyshop.SkyShop;
import com.github.lukesky19.skyshop.data.Settings;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.nio.file.Path;

/**
 * This class manages everything related to handling the plugin's settings.
*/
public class SettingsManager {
    private final @NotNull SkyShop skyShop;
    private @Nullable Settings settingsConfig;

    /**
     * Constructor
     * @param skyShop A {@link SkyShop} instance.
    */
    public SettingsManager(@NotNull SkyShop skyShop) {
        this.skyShop = skyShop;
    }

    /**
     * Get the plugin's {@link Settings}.
     * @return The plugin's {@link Settings}.
    */
    public @Nullable Settings getSettingsConfig() {
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
    }

    /**
     * This edits the settings.yml file to set `first-run` to false.
    */
    public void setFirstRunFalse() {
        if(settingsConfig == null) return;

        Settings newSettings = new Settings(settingsConfig.configVersion(), settingsConfig.locale(), false, settingsConfig.statistics());
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
}
