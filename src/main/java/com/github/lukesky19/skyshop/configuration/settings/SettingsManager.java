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
package com.github.lukesky19.skyshop.configuration.settings;

import com.github.lukesky19.skylib.common.api.adventure.AdventureUtility;
import com.github.lukesky19.skylib.common.api.configuration.abstracts.SimpleConfigManager;
import com.github.lukesky19.skylib.libs.configurate.ConfigurateException;
import com.github.lukesky19.skylib.libs.configurate.ConfigurationNode;
import com.github.lukesky19.skylib.libs.configurate.serialize.SerializationException;
import com.github.lukesky19.skylib.libs.configurate.yaml.YamlConfigurationLoader;
import com.github.lukesky19.skyshop.SkyShop;
import com.github.lukesky19.skyshop.configuration.settings.data.SettingsV1;
import com.github.lukesky19.skyshop.configuration.settings.data.SettingsV2;
import com.github.lukesky19.skyshop.configuration.settings.data.SettingsV3;
import com.github.lukesky19.skyshop.configuration.settings.data.SettingsV4;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.io.File;
import java.nio.file.Path;

/**
 * This class manages everything related to handling the plugin's settings.
*/
public class SettingsManager extends SimpleConfigManager<SettingsV4> {
    /**
     * Constructor
     * @param skyShop A {@link SkyShop} instance.
    */
    public SettingsManager(@NonNull SkyShop skyShop) {
        super(skyShop, Path.of(skyShop.getDataFolder() + File.separator + "settings.yml"), SettingsV4.class);
    }

    /**
     * Get the {@link SettingsV4} or null.
     * @return The {@link SettingsV4} or null.
     */
    public @Nullable SettingsV4 getConfiguration() {
        return configuration;
    }

    /**
     * Load the settings configuration.
     */
    public void loadConfiguration() {
        configuration = null;
        if(configurationPath == null) return;

        saveDefaultConfiguration();

        YamlConfigurationLoader loader = createLoader(configurationPath);
        try {
            ConfigurationNode root = loader.load();
            int version = getVersion(root);

            SettingsV4 settings;
            switch(version) {
                case 4 -> {
                    settings = root.get(SettingsV4.class);
                    if(settings == null) {
                        logger.warn(AdventureUtility.plain("Failed to load version 4 configuration file settings.yml. Class name: " + this.getClass().getName()));
                        return;
                    }
                }

                case 3 -> {
                    SettingsV3 settingsV3 = root.get(SettingsV3.class);
                    if(settingsV3 == null) {
                        logger.warn(AdventureUtility.plain("Failed to load version 3 configuration file settings.yml. Class name: " + this.getClass().getName()));
                        return;
                    }

                    settings = new SettingsV4(
                            4,
                            settingsV3.locale(),
                            settingsV3.firstRun(),
                            settingsV3.statistics(),
                            settingsV3.islandSizeLimit());

                    saveConfiguration(settings);
                }

                case 2 -> {
                    SettingsV2 settingsV2 = root.get(SettingsV2.class);
                    if(settingsV2 == null) {
                        logger.warn(AdventureUtility.plain("Failed to load version 2 configuration file settings.yml. Class name: " + this.getClass().getName()));
                        return;
                    }

                    settings = new SettingsV4(
                            4,
                            settingsV2.locale(),
                            settingsV2.firstRun(),
                            settingsV2.statistics(),
                            100);

                    saveConfiguration(settings);
                }

                case 1 -> {
                    SettingsV1 settingsV1 = root.get(SettingsV1.class);
                    if(settingsV1 == null) {
                        logger.warn(AdventureUtility.plain("Failed to load version 1 configuration file settings.yml. Class name: " + this.getClass().getName()));
                        return;
                    }

                    settings = new SettingsV4(
                            4,
                            settingsV1.locale(),
                            settingsV1.firstRun(),
                            false,
                            100);

                    saveConfiguration(settings);
                }

                default -> {
                    logger.warn(AdventureUtility.plain("Failed to load configuration file sellall.yml due to an unsupported config version. Version: " + version + " Class name: " + this.getClass().getName()));
                    return;
                }
            }

            // Check if the configuration is invalid
            if(!validateConfiguration(settings)) {
                logger.warn(AdventureUtility.plain("Settings configuration validation failed. Class name: " + this.getClass().getName()));
                return;
            }

            this.configuration = settings;
        } catch (ConfigurateException configurateException) {
            logger.error(AdventureUtility.plain("Failed to load configuration. Error: " + configurateException.getMessage()));
        }
    }

    /**
     * Save the default settings configuration if it doesn't exist on the disk.
     */
    @Override
    public void saveDefaultConfiguration() {
        if(configurationPath == null) return;
        if(!configurationPath.toFile().exists()) plugin.saveResource("settings.yml", false);
    }

    /**
     * No migration because version 4 is the latest version. Returns the passed settings.
     * @param settingsV4 The {@link SettingsV4} to migrate.
     * @return The passed settings.
     */
    @Override
    public @Nullable SettingsV4 migrateConfiguration(@NonNull SettingsV4 settingsV4) {
        return settingsV4;
    }

    /**
     * Save the configuration.
     * @param configuration The configuration.
     */
    public void saveConfiguration(@NonNull SettingsV4 configuration) {
        if(configurationPath == null) return;
        try {
            YamlConfigurationLoader loader = createLoader(configurationPath);

            ConfigurationNode node = loader.createNode();

            node.set(SettingsV4.class, configuration);

            loader.save(node);
        } catch (ConfigurateException configurateException) {
            logger.error(AdventureUtility.plain("Failed to save settings configuration. Error: " + configurateException.getMessage()));
        }
    }

    /**
     * Checks if the settings configuration is valid (not null).
     * @param configuration The {@link SettingsV4} to validate.
     * @return true if valid, false if not.
     */
    public boolean validateConfiguration(@Nullable SettingsV4 configuration) {
        return configuration != null;
    }

    /**
     * This edits the settings.yml file to set `first-run` to false.
    */
    public void setFirstRunFalse() {
        if(configuration == null) return;

        configuration = new SettingsV4(configuration.version(), configuration.locale(), false, configuration.statistics(), configuration.islandSizeLimit());

        saveConfiguration(configuration);
    }

    /**
     * Get the version number.
     * @param root The root {@link ConfigurationNode}.
     * @return The config version.
     */
    private int getVersion(@NonNull ConfigurationNode root) {
        ConfigurationNode versionNode = root.node("version");
        int version = versionNode.getInt();

        if(version == 0) {
            ConfigurationNode legacyVersionNode = root.node("config-version");
            String legacyVersion = legacyVersionNode.virtual() ? null : legacyVersionNode.getString();
            try {
                switch (legacyVersion) {
                    case "2.1.0.0" -> {
                        versionNode.set(3);
                        version = 3;
                    }

                    case "2.0.0.0" -> {
                        versionNode.set(2);
                        version = 2;
                    }

                    // Settings version 1.0.0.0 didn't have a defined config version
                    case null -> {
                        versionNode.set(1);
                        version = 1;
                    }

                    default -> logger.warn(AdventureUtility.plain("Failed to convert String-based version to numeric version"));
                }
            } catch (SerializationException e) {
                logger.warn(AdventureUtility.plain("Failed to convert String-based version to numeric version"));
            }
        }

        return version;
    }
}