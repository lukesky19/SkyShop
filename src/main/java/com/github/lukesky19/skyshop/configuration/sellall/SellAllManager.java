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
import com.github.lukesky19.skylib.api.configurate.ConfigurationUtility;
import com.github.lukesky19.skylib.libs.configurate.ConfigurateException;
import com.github.lukesky19.skylib.libs.configurate.ConfigurationNode;
import com.github.lukesky19.skylib.libs.configurate.serialize.SerializationException;
import com.github.lukesky19.skylib.libs.configurate.yaml.YamlConfigurationLoader;
import com.github.lukesky19.skyshop.SkyShop;
import com.github.lukesky19.skyshop.configuration.sellall.data.SellAllGUIConfigV1;
import com.github.lukesky19.skyshop.configuration.sellall.data.SellAllGUIConfigV2;
import com.github.lukesky19.skyshop.configuration.sellall.data.SellAllGUIConfigV3;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

import java.io.File;
import java.nio.file.Path;

/**
 * This class manages everything related to handling the plugin's sellall.yml file.
 */
public class SellAllManager {
    private final @NotNull SkyShop skyShop;
    private final @NotNull ComponentLogger logger;
    private @Nullable SellAllGUIConfigV3 configuration;

    /**
     * Constructor
     * @param skyShop A {@link SkyShop} instance.
     */
    public SellAllManager(@NotNull SkyShop skyShop) {
        this.skyShop = skyShop;
        this.logger = skyShop.getComponentLogger();
    }

    /**
     * Get the {@link SellAllGUIConfigV3} or null.
     * @return The {@link SellAllGUIConfigV3} or null.
     */
    public @Nullable SellAllGUIConfigV3 getConfiguration() {
        return configuration;
    }

    /**
     * Load the sellall GUI configuration.
     */
    public void loadConfiguration() {
        @NotNull Path configurationPath = Path.of(skyShop.getDataFolder() + File.separator + "sellall.yml");
        configuration = null;

        saveBundledConfig(configurationPath);

        YamlConfigurationLoader loader = ConfigurationUtility.getYamlConfigurationLoader(configurationPath);
        try {
            ConfigurationNode root = loader.load();
            int version = getVersion(root);

            @Nullable SellAllGUIConfigV3 sellAllConfig;
            switch(version) {
                case 3 -> {
                    sellAllConfig = root.get(SellAllGUIConfigV3.class);
                    if(sellAllConfig == null) {
                        logger.warn(AdventureUtil.deserialize("Failed to load version 3 configuration file sellall.yml. Class name: " + this.getClass().getName()));
                        return;
                    }
                }

                case 2 -> {
                    SellAllGUIConfigV2 sellAllConfigV2 = root.get(SellAllGUIConfigV2.class);
                    if(sellAllConfigV2 == null) {
                        logger.warn(AdventureUtil.deserialize("Failed to load version 2 configuration file sellall.yml. Class name: " + this.getClass().getName()));
                        return;
                    }

                    sellAllConfig = new SellAllGUIConfigV3(
                            3,
                            sellAllConfigV2.guiType(),
                            sellAllConfigV2.guiName(),
                            sellAllConfigV2.buttons().stream().map(button ->
                                    new SellAllGUIConfigV3.Button(button.buttonType(), button.slot(), button.displayItem())).toList());

                    saveConfiguration(configurationPath, sellAllConfig);
                }

                case 1 -> {
                    @Nullable SellAllGUIConfigV1 sellAllConfigV1 = root.get(SellAllGUIConfigV1.class);
                    if(sellAllConfigV1 == null) {
                        logger.warn(AdventureUtil.deserialize("Failed to load version 1 configuration file sellall.yml. Class name: " + this.getClass().getName()));
                        return;
                    }

                    sellAllConfig = new SellAllGUIConfigV3(
                            3,
                            sellAllConfigV1.gui().guiType(),
                            sellAllConfigV1.gui().name(),
                            sellAllConfigV1.gui().buttons().stream().map(button ->
                                    new SellAllGUIConfigV3.Button(button.buttonType(), button.slot(), button.displayItem())).toList());

                    saveConfiguration(configurationPath, sellAllConfig);
                }

                default -> {
                    logger.warn(AdventureUtil.deserialize("Failed to load configuration file sellall.yml due to an unsupported config version. Version: " + version + " Class name: " + this.getClass().getName()));
                    return;
                }
            }

            // Check if the configuration is invalid
            if(!validateConfiguration(sellAllConfig)) {
                logger.warn(AdventureUtil.deserialize("Sellall GUI configuration validation failed. Class name: " + this.getClass().getName()));
                return;
            }

            this.configuration = sellAllConfig;
        } catch (ConfigurateException configurateException) {
            logger.error(AdventureUtil.deserialize("Failed to load configuration. Error: " + configurateException.getMessage()));
        }
    }

    /**
     * Save the default sell all GUI configuration if it doesn't exist on the disk.
     * @param configurationPath The {@link Path} to the configuration on the disk.
     */
    public void saveBundledConfig(@NotNull Path configurationPath) {
        if(!configurationPath.toFile().exists()) skyShop.saveResource("sellall.yml", false);
    }

    /**
     * Save the configuration.
     * @param configurationPath The path to save to.
     * @param configuration The configuration.
     */
    public void saveConfiguration(@NotNull Path configurationPath, @NonNull SellAllGUIConfigV3 configuration) {
        try {
            YamlConfigurationLoader loader = ConfigurationUtility.getYamlConfigurationLoader(configurationPath);

            ConfigurationNode node = loader.createNode();

            node.set(SellAllGUIConfigV3.class, configuration);

            loader.save(node);
        } catch (ConfigurateException configurateException) {
            logger.error(AdventureUtil.deserialize("Failed to save sellall GUI configuration. Error: " + configurateException.getMessage()));
        }
    }

    /**
     * Checks if the sellall GUI configuration is valid (not null).
     * @param configuration The {@link SellAllGUIConfigV3} to validate.
     * @return true if valid, false if not.
     */
    public boolean validateConfiguration(@Nullable SellAllGUIConfigV3 configuration) {
        return configuration != null;
    }

    /**
     * Get the version number.
     * @param root The root {@link ConfigurationNode}.
     * @return The config version.
     */
    private int getVersion(@NotNull ConfigurationNode root) {
        ConfigurationNode versionNode = root.node("version");
        int version = versionNode.getInt();

        ConfigurationNode legacyVersionNode = root.node("config-version");
        @Nullable String legacyVersion = legacyVersionNode.virtual() ? null : legacyVersionNode.getString();
        if(legacyVersion != null) {
            try {
                switch (legacyVersion) {
                    case "3.0.0.0" -> {
                        versionNode.set(2);
                        version = 2;
                    }

                    case "2.0.0.0" -> {
                        versionNode.set(1);
                        version = 1;
                    }

                    default -> {
                        logger.warn(AdventureUtil.deserialize("Failed to convert String-based version to numeric version"));
                        version = 0;
                    }
                }
            } catch (SerializationException e) {
                logger.warn(AdventureUtil.deserialize("Failed to convert String-based version to numeric version"));
                version = 0;
            }
        }

        return version;
    }
}