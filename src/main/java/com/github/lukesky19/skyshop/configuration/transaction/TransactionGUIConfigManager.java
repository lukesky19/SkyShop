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
package com.github.lukesky19.skyshop.configuration.transaction;

import com.github.lukesky19.skylib.common.api.adventure.AdventureUtility;
import com.github.lukesky19.skylib.common.api.configuration.abstracts.KeyValueConfigManager;
import com.github.lukesky19.skylib.libs.configurate.ConfigurateException;
import com.github.lukesky19.skylib.libs.configurate.ConfigurationNode;
import com.github.lukesky19.skylib.libs.configurate.serialize.SerializationException;
import com.github.lukesky19.skylib.libs.configurate.yaml.YamlConfigurationLoader;
import com.github.lukesky19.skyshop.SkyShop;
import com.github.lukesky19.skyshop.configuration.transaction.data.TransactionGUIConfigV1;
import com.github.lukesky19.skyshop.configuration.transaction.data.TransactionGUIConfigV2;
import com.github.lukesky19.skyshop.configuration.transaction.data.TransactionGUIConfigV3;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

/**
 * This class manages everything related to handling the plugin's transaction GUI config files.
 */
public class TransactionGUIConfigManager extends KeyValueConfigManager<String, TransactionGUIConfigV3> {

    /**
     * Constructor
     * @param skyShop A {@link SkyShop instance}
     */
    public TransactionGUIConfigManager(@NonNull SkyShop skyShop) {
        super(skyShop);
    }

    /**
     * Get the {@link TransactionGUIConfigV3} for the provided shop id.
     * @param transactionStyle The id of the transaction style to get the configuration for. The transaction style is just the file name without the file extension.
     * @return The {@link TransactionGUIConfigV3} for the provided shop id or null if no {@link TransactionGUIConfigV3} exists for that id.
     */
    public @Nullable TransactionGUIConfigV3 getTransactionConfig(@Nullable String transactionStyle) {
        if(transactionStyle == null) return null;
        return getConfiguration(transactionStyle);
    }

    /**
     * Load all transaction gui configuration files in SkyShop/category
     */
    public void loadConfigurations() {
        dataMap.clear();

        saveDefaultConfiguration();

        Path categoryPath = Path.of(plugin.getDirectoryFile() + File.separator + "transaction_styles");

        try(Stream<Path> stream = Files.walk(categoryPath)) {
            stream.filter(path -> !path.toFile().isDirectory()).forEach(path -> {
                String identifier = getFileNameWithoutExtension(path);

                loadConfiguration(identifier, path);
            });
        } catch (IOException e) {
            logger.error(AdventureUtility.plain("Failed to load transaction GUI configuration files. " + e.getMessage()));
        }
    }

    /**
     * Load the configuration.
     * @param identifier The config version identifier.
     * @param configurationPath The configuration path.
     */
    public void loadConfiguration(@NonNull String identifier, @NonNull Path configurationPath) {
        YamlConfigurationLoader loader = createLoader(configurationPath);

        try {
            ConfigurationNode root = loader.load();
            int version = getVersion(root);

            TransactionGUIConfigV3 transactionGUIConfig;
            switch(version) {
                case 3 -> {
                    transactionGUIConfig = root.get(TransactionGUIConfigV3.class);
                    if(transactionGUIConfig == null) {
                        logger.warn(AdventureUtility.plain("Failed to load version 3 configuration file " + (identifier + ".yml") + ". Class name: " + this.getClass().getName()));
                        return;
                    }
                }

                case 2 -> {
                    TransactionGUIConfigV2 transactionGUIConfigV2 = root.get(TransactionGUIConfigV2.class);
                    if(transactionGUIConfigV2 == null) {
                        logger.warn(AdventureUtility.plain("Failed to load version 2 configuration file " + (identifier + ".yml") + ". Class name: " + this.getClass().getName()));
                        return;
                    }

                    transactionGUIConfig = new TransactionGUIConfigV3(
                            3,
                            transactionGUIConfigV2.guiType(),
                            transactionGUIConfigV2.guiName(),
                            transactionGUIConfigV2.pages().stream().map(pageConfig ->
                                    new TransactionGUIConfigV3.PageConfig(pageConfig.buttons().stream()
                                            .map(button -> new TransactionGUIConfigV3.Button(button.buttonType(),
                                                    button.slot(), button.transactionAmount(), button.displayItem()))
                                            .toList())).toList());

                    saveConfiguration(configurationPath, transactionGUIConfig);
                }

                case 1 -> {
                    TransactionGUIConfigV1 transactionGUIConfigV1 = root.get(TransactionGUIConfigV1.class);
                    if(transactionGUIConfigV1 == null) {
                        logger.warn(AdventureUtility.plain("Failed to load version 1 configuration file " + (identifier + ".yml") + ". Class name: " + this.getClass().getName()));
                        return;
                    }

                    transactionGUIConfig = new TransactionGUIConfigV3(
                            3,
                            transactionGUIConfigV1.gui().guiType(),
                            transactionGUIConfigV1.gui().name(),
                            transactionGUIConfigV1.gui().pages().stream().map(pageConfig ->
                                    new TransactionGUIConfigV3.PageConfig(pageConfig.buttons().stream()
                                            .map(button -> new TransactionGUIConfigV3.Button(button.buttonType(),
                                                    button.slot(), button.transactionAmount(), button.displayItem()))
                                            .toList())).toList());

                    saveConfiguration(configurationPath, transactionGUIConfig);
                }

                default -> {
                    logger.warn(AdventureUtility.plain("Failed to load configuration file " + (identifier + ".yml") + " due to an unsupported config version. Version: " + version + ". Class name: " + this.getClass().getName()));
                    return;
                }
            }

            // Check if the configuration is invalid
            if(!validateConfiguration(transactionGUIConfig)) {
                logger.warn(AdventureUtility.plain("Configuration validation failed for file " + (identifier + ".yml") + ". Class name: " + this.getClass().getName()));
                return;
            }

            dataMap.put(identifier, transactionGUIConfig);
        } catch (ConfigurateException configurateException) {
            logger.error(AdventureUtility.plain("Failed to load the configuration. Error: " + configurateException.getMessage()));
        }
    }

    /**
     * Save the configuration.
     * @param configurationPath The path to save to.
     * @param configuration The configuration.
     */
    public void saveConfiguration(@NonNull Path configurationPath, @NonNull TransactionGUIConfigV3 configuration) {
        try {
            YamlConfigurationLoader loader = createLoader(configurationPath);

            ConfigurationNode node = loader.createNode();

            node.set(TransactionGUIConfigV3.class, configuration);

            loader.save(node);
        } catch (ConfigurateException configurateException) {
            logger.error(AdventureUtility.plain("Failed to save transaction GUI configuration. Error: " + configurateException.getMessage()));
        }
    }

    /**
     * Validate the configuration.
     * @param configuration The configuration.
     * @return true if valid, false if not.
     */
    public boolean validateConfiguration(@Nullable TransactionGUIConfigV3 configuration) {
        return configuration != null;
    }

    /**
     * Save the default transaction GUI configurations if they do not exist.
     */
    @Override
    public void saveDefaultConfiguration() {
        Path itemsStylePath = Path.of(plugin.getDirectoryFile() + File.separator + "transaction_styles" + File.separator + "items.yml");
        Path singleCommandStylePath = Path.of(plugin.getDirectoryFile() + File.separator + "transaction_styles" + File.separator + "single_command.yml");

        if(!itemsStylePath.toFile().exists()) plugin.saveResource("transaction_styles/items.yml", false);
        if(!singleCommandStylePath.toFile().exists()) plugin.saveResource("transaction_styles/single_command.yml", false);
    }

    @Override
    protected @Nullable TransactionGUIConfigV3 migrateConfiguration(@NonNull TransactionGUIConfigV3 transactionGUIConfigV3) {
        return transactionGUIConfigV3;
    }

    /**
     * Get the file name from a {@link Path} without the file extension.
     * @param path The {@link Path} to a file. You should ensure the {@link Path} actually points to a file.
     * @return A {@link String} containing the file name.
     * @throws RuntimeException if the {@link Path} is not a file.
     */
    private @NonNull String getFileNameWithoutExtension(@NonNull Path path) {
        if(!path.toFile().isFile()) throw new RuntimeException("Path does not point to a file.");

        String fileName = path.getFileName().toString();

        int lastDotIndex = fileName.lastIndexOf('.');

        if(lastDotIndex == -1) return fileName;

        return fileName.substring(0, lastDotIndex);
    }

    /**
     * Get the version number.
     * @param root The root {@link ConfigurationNode}.
     * @return The config version.
     */
    private int getVersion(@NonNull ConfigurationNode root) {
        ConfigurationNode versionNode = root.node("version");
        int version = versionNode.getInt();

        ConfigurationNode legacyVersionNode = root.node("config-version");
        String legacyVersion = legacyVersionNode.virtual() ? null : legacyVersionNode.getString();
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
                        versionNode.set(0);
                        version = 0;
                    }
                }
            } catch (SerializationException e) {
                logger.warn(AdventureUtility.plain("Failed to convert String-based version to numeric version"));
                version = 0;
            }
        }

        return version;
    }
}