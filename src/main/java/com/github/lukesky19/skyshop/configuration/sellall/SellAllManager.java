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
package com.github.lukesky19.skyshop.configuration.sellall;

import com.github.lukesky19.skylib.api.adventure.AdventureUtil;
import com.github.lukesky19.skylib.api.common.abstracts.config.SimpleConfigManager;
import com.github.lukesky19.skylib.api.configurate.ConfigurationUtility;
import com.github.lukesky19.skylib.libs.configurate.ConfigurateException;
import com.github.lukesky19.skylib.libs.configurate.ConfigurationNode;
import com.github.lukesky19.skylib.libs.configurate.yaml.YamlConfigurationLoader;
import com.github.lukesky19.skyshop.SkyShop;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * This class manages everything related to handling the plugin's sellall.yml file.
 */
public class SellAllManager extends SimpleConfigManager<SellAllConfig> {
    /**
     * Constructor
     * @param skyShop A {@link SkyShop} instance.
     */
    public SellAllManager(@NotNull SkyShop skyShop) {
        super(skyShop, Path.of(skyShop.getDataFolder() + File.separator + "sellall.yml"), SellAllConfig.class);
    }

    @Override
    public void loadConfiguration() {
        configuration = null;
        if(configurationPath == null) {
            logger.warn(AdventureUtil.deserialize("Unable to load configuration because the configuration path was not set."));
            return;
        }

        saveBundledConfig();

        YamlConfigurationLoader loader = ConfigurationUtility.getYamlConfigurationLoader(configurationPath);
        try {
            ConfigurationNode root = loader.load();
            ConfigurationNode versionNode = root.node("config-version");
            @Nullable String configVersion = versionNode.virtual() ? null : versionNode.getString();

            @Nullable SellAllConfig sellAllConfig;
            switch(configVersion) {
                case "3.0.0.0" -> {
                    sellAllConfig = root.get(SellAllConfig.class);
                    if(sellAllConfig == null) {
                        logger.warn(AdventureUtil.deserialize("Failed to load version 3.0.0.0 or newer configuration file sellall.yml. Class name: " + this.getClass().getName()));
                        return;
                    }
                }

                case "2.0.0.0" -> {
                    @Nullable SellAllConfigV2 sellAllConfigV2 = root.get(SellAllConfigV2.class);
                    if(sellAllConfigV2 == null) {
                        logger.warn(AdventureUtil.deserialize("Failed to load version 2.1.0.0 configuration file sellall.yml. Class name: " + this.getClass().getName()));
                        return;
                    }

                    List<SellAllConfig.Button> buttonList = new ArrayList<>();
                    sellAllConfigV2.gui().buttons().forEach(button ->
                            buttonList.add(new SellAllConfig.Button(button.buttonType(), button.slot(), button.displayItem())));

                    sellAllConfig = new SellAllConfig(
                            sellAllConfigV2.configVersion(),
                            sellAllConfigV2.gui().guiType(),
                            sellAllConfigV2.gui().name(),
                            buttonList);

                    // Save updated configuration
                    saveConfiguration(sellAllConfig);

                    this.configuration = sellAllConfig;

                    return;
                }

                case null -> {
                    logger.warn(AdventureUtil.deserialize("Failed to load configuration file sellall.yml due to a null config version. Class name: " + this.getClass().getName()));
                    return;
                }

                default -> {
                    logger.warn(AdventureUtil.deserialize("Failed to load configuration file sellall.yml due to an unsupported config version. Class name: " + this.getClass().getName()));
                    return;
                }
            }

            // Migrate configuration
            @Nullable SellAllConfig migratedConfiguration = migrateConfiguration(sellAllConfig);
            // If migration failed, return
            if(migratedConfiguration == null) {
                logger.warn(AdventureUtil.deserialize("Configuration migration failed for file sellall.yml. Class name: " + this.getClass().getName()));
                return;
            }

            // Check if the configuration is invalid
            if(!validateConfiguration(migratedConfiguration)) {
                logger.warn(AdventureUtil.deserialize("Configuration validation failed. Class name: " + this.getClass().getName()));
                return;
            }

            // Save the migrated configuration if different
            if(migratedConfiguration != sellAllConfig) {
                saveConfiguration(migratedConfiguration);
            }

            this.configuration = migratedConfiguration;
        } catch (ConfigurateException configurateException) {
            logger.error(AdventureUtil.deserialize("Failed to load configuration. Error: " + configurateException.getMessage()));
        }
    }

    @Override
    public void saveBundledConfig() {
        if(configurationPath == null) return;
        if(!configurationPath.toFile().exists()) plugin.saveResource("sellall.yml", false);
    }

    @Override
    public @Nullable SellAllConfig migrateConfiguration(@NonNull SellAllConfig configuration) {
        switch(configuration.configVersion()) {
            case "3.0.0.0" -> {
                // Latest Version, do nothing
                return configuration;
            }

            case "2.0.0.0" -> {
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

    @Override
    public boolean validateConfiguration(@Nullable SellAllConfig configuration) {
        return configuration != null;
    }
}