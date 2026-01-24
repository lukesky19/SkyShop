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
package com.github.lukesky19.skyshop.configuration.category;

import com.github.lukesky19.skylib.api.adventure.AdventureUtil;
import com.github.lukesky19.skylib.api.common.abstracts.config.KeyValueConfigManager;
import com.github.lukesky19.skylib.libs.configurate.ConfigurateException;
import com.github.lukesky19.skylib.libs.configurate.ConfigurationNode;
import com.github.lukesky19.skylib.libs.configurate.serialize.SerializationException;
import com.github.lukesky19.skylib.libs.configurate.yaml.NodeStyle;
import com.github.lukesky19.skylib.libs.configurate.yaml.YamlConfigurationLoader;
import com.github.lukesky19.skyshop.SkyShop;
import com.github.lukesky19.skyshop.api.configuration.TransactionConfiguration;
import com.github.lukesky19.skyshop.api.serializer.Serializer;
import com.github.lukesky19.skyshop.configuration.category.gui.CategoryConfig;
import com.github.lukesky19.skyshop.configuration.category.gui.CategoryConfigV2;
import com.github.lukesky19.skyshop.configuration.category.serializer.migration.MultiplierConfigurationSerializer;
import com.github.lukesky19.skyshop.configuration.category.transaction.CommandConfiguration;
import com.github.lukesky19.skyshop.configuration.category.transaction.IslandSizeConfiguration;
import com.github.lukesky19.skyshop.configuration.category.transaction.ItemConfiguration;
import com.github.lukesky19.skyshop.configuration.category.transaction.migration.PrestigeMultiplierData;
import com.github.lukesky19.skyshop.configuration.settings.Settings;
import com.github.lukesky19.skyshop.configuration.settings.SettingsManager;
import com.github.lukesky19.skyshop.configuration.type_serializer.TransactionConfigurationSerializer;
import com.github.lukesky19.skyshop.prices.PriceManager;
import com.github.lukesky19.skyshop.registry.RegistryManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

/**
 * This class manages everything related to handling the plugin's category config files.
*/
public class CategoryConfigManager extends KeyValueConfigManager<String, CategoryConfig> {
    private final @NotNull SettingsManager settingsManager;
    private final @NotNull PriceManager priceManager;
    private final @NotNull RegistryManager registryManager;

    private final @NotNull TransactionConfigurationSerializer serializer;

    /**
     * Constructor
     * @param skyShop A {@link SkyShop} instance.
     * @param settingsManager A {@link SettingsManager} instance.
     * @param priceManager A {@link PriceManager} instance.
     * @param registryManager A {@link RegistryManager} instance.
    */
    public CategoryConfigManager(
            @NotNull SkyShop skyShop,
            @NotNull SettingsManager settingsManager,
            @NotNull PriceManager priceManager,
            @NotNull RegistryManager registryManager) {
        super(skyShop);

        this.settingsManager = settingsManager;
        this.priceManager = priceManager;
        this.registryManager = registryManager;

        this.serializer = new TransactionConfigurationSerializer(registryManager);
    }

    /**
     * Get a {@link Set} of {@link String}s for the known category config ids loaded.
     * @return A {@link Set} of {@link String}s for the known category config ids loaded.
     */
    public @NotNull Set<@NotNull String> getCategoryIds() {
        return dataMap.keySet();
    }

    /**
     * Load all category gui configuration files in SkyShop/category
     */
    @Override
    public void loadConfigurations() {
        // Clear cached sell prices.
        priceManager.clearCache();

        // Clear the current loaded configurations
        dataMap.clear();

        // Save bundled config if first run
        saveBundledConfig();

        // Create the path to the category directory.
        Path categoryPath = Path.of(plugin.getDataFolder() + File.separator + "category");

        // Walk through all files
        try(Stream<Path> stream = Files.walk(categoryPath)) {
            // Loop through each path in the stream
            for(Path path : stream.toList()) {
                // Ignore directories
                if(path.toFile().isDirectory()) continue;

                // Get the category name, which is the file name without the extension
                String fileNameWithoutExtension = getFileNameWithoutExtension(path);

                // Attempt to load the configuration
                loadConfiguration(fileNameWithoutExtension, CategoryConfig.class, path);
            }
        } catch (IOException e) {
            logger.error(AdventureUtil.deserialize("Failed to load category configuration files. " + e.getMessage()));
        }
    }

    @Override
    public void loadConfiguration(@NonNull String identifier, @NotNull Class<CategoryConfig> configClass, @NotNull Path configurationPath) {
        YamlConfigurationLoader loader = YamlConfigurationLoader.builder()
                .nodeStyle(NodeStyle.BLOCK)
                .path(configurationPath)
                .indent(4)
                .defaultOptions(opts ->
                        opts.serializers(build ->
                                build.registerExact(TransactionConfiguration.class, serializer)))
                .build();

        try {
            ConfigurationNode root = loader.load();
            ConfigurationNode versionNode = root.node("config-version");
            @Nullable String configVersion = versionNode.virtual() ? null : versionNode.getString();

            @Nullable CategoryConfig categoryConfig;
            switch(configVersion) {
                case "3.0.0.0" -> {
                    categoryConfig = root.get(CategoryConfig.class);
                    if(categoryConfig == null) {
                        logger.warn(AdventureUtil.deserialize("Failed to load version 3.0.0.0 or newer configuration file " + (identifier + ".yml") + ". Class name: " + this.getClass().getName()));
                        return;
                    }
                }

                case "2.1.0.0", "2.0.0.0" -> {
                    @Nullable CategoryConfigV2 categoryConfigV2 = root.get(CategoryConfigV2.class);
                    if(categoryConfigV2 == null) {
                        logger.warn(AdventureUtil.deserialize("Failed to load version 2.0.0.0 or 2.1.0.0 configuration file " + (identifier + ".yml") + ". Class name: " + this.getClass().getName()));
                        return;
                    }

                    // Migrate V2 config to V3
                    categoryConfig = migrateV2Configuration(categoryConfigV2);

                    // Store the existing serializer if any
                    String id = "skyprestige:multiplier";
                    @Nullable Serializer existingSerializer = registryManager.getSerializer(id);

                    // Save updated configuration
                    try {
                        // Temporarily overwrite the serializer for prestige multiplier for migration purposes
                        registryManager.registerSerializer(id, new MultiplierConfigurationSerializer());

                        YamlConfigurationLoader yamlConfigurationLoader = YamlConfigurationLoader.builder()
                                .nodeStyle(NodeStyle.BLOCK)
                                .path(configurationPath)
                                .indent(4)
                                .defaultOptions(opts ->
                                        opts.serializers(build ->
                                                build.registerExact(TransactionConfiguration.class, serializer)))
                                .build();

                        ConfigurationNode node = yamlConfigurationLoader.createNode();

                        node.set(configClass, categoryConfig);

                        yamlConfigurationLoader.save(node);

                        // Revert temporarily overwritten serializer
                        if(existingSerializer != null) {
                            registryManager.registerSerializer(id, existingSerializer);
                        } else {
                            registryManager.unregisterSerializer(id);
                        }
                    } catch(SerializationException e) {
                        // Revert temporarily overwritten serializer
                        if(existingSerializer != null) {
                            registryManager.registerSerializer(id, existingSerializer);
                        } else {
                            registryManager.unregisterSerializer(id);
                        }

                        logger.warn(AdventureUtil.deserialize("Failed to save migrated configuration (version 2 -> 3) configuration file " + (identifier + ".yml") + ". Class name: " + this.getClass().getName() + ". Error: " + e.getMessage()));
                        return;
                    }

                    // Re-load configuration from disk
                    // This is required so that the prestige multiplier transaction configuration uses the SkyPrestige class and not the migration class from SkyShop
                    loadConfiguration(identifier, configClass, configurationPath);

                    return;
                }

                case null -> {
                    logger.warn(AdventureUtil.deserialize("Failed to load configuration file " + (identifier + ".yml") + " due to a null config version. Class name: " + this.getClass().getName()));
                    return;
                }

                default -> {
                    logger.warn(AdventureUtil.deserialize("Failed to load configuration file " + (identifier + ".yml") + " due to an unsupported config version. Class name: " + this.getClass().getName()));
                    return;
                }
            }

            // Migrate configuration
            @Nullable CategoryConfig migratedConfiguration = migrateConfiguration(categoryConfig);
            // If migration failed, return
            if(migratedConfiguration == null) {
                logger.warn(AdventureUtil.deserialize("Configuration migration failed for file " + (identifier + ".yml") + ". Class name: " + this.getClass().getName()));
                return;
            }

            // Check if the configuration is invalid
            if(!validateConfiguration(categoryConfig)) {
                logger.warn(AdventureUtil.deserialize("Configuration validation failed for file " + (identifier + ".yml") + ". Class name: " + this.getClass().getName()));
                return;
            }

            // Save the migrated configuration if different
            if(categoryConfig != migratedConfiguration) {
                saveConfiguration(configClass, configurationPath, migratedConfiguration);
            }

            // Store the configuration
            setData(identifier, migratedConfiguration);
        } catch (ConfigurateException configurateException) {
            logger.error(AdventureUtil.deserialize("Failed to load the configuration. Error: " + configurateException.getMessage()));
        }
    }

    @Override
    public void saveConfiguration(@NotNull Class<CategoryConfig> configClass, @NotNull Path configurationPath, @NonNull CategoryConfig configuration) {
        try {
            YamlConfigurationLoader yamlConfigurationLoader = YamlConfigurationLoader.builder()
                    .nodeStyle(NodeStyle.BLOCK)
                    .path(configurationPath)
                    .indent(4)
                    .defaultOptions(opts ->
                            opts.serializers(build ->
                                    build.registerExact(TransactionConfiguration.class, serializer)))
                    .build();

            ConfigurationNode node = yamlConfigurationLoader.createNode();

            node.set(configClass, configuration);

            yamlConfigurationLoader.save(node);
        } catch (ConfigurateException configurateException) {
            logger.error(AdventureUtil.deserialize("Failed to save configuration. Error: " + configurateException.getMessage()));
        }
    }

    /**
     * Save the example category configuration files if they don't exist.
     * Will only save if {@link Settings#firstRun()} is true.
     */
    @Override
    public void saveBundledConfig() {
        Settings settings = settingsManager.getConfiguration();
        if(settings == null) return;
        if(!settings.firstRun()) return;

        Path exampleMenuPath = Path.of(plugin.getDataFolder() + File.separator + "category" + File.separator + "menu.yml");
        Path exampleShopPath = Path.of(plugin.getDataFolder() + File.separator + "category" + File.separator + "example.yml");

        // Save the example menu category if it doesn't exist
        if(!exampleMenuPath.toFile().exists()) {
            plugin.saveResource("category" + File.separator + "menu.yml", false);
        }

        // Save the example shop category if it doesn't exist
        if(!exampleShopPath.toFile().exists()) {
            plugin.saveResource("category" + File.separator + "example.yml", false);
        }

        settingsManager.setFirstRunFalse();
    }

    @Override
    public @Nullable CategoryConfig migrateConfiguration(@NonNull CategoryConfig configuration) {
        switch(configuration.configVersion()) {
            case "3.0.0.0" -> {
                // Latest Version, do nothing
                return configuration;
            }

            case "2.1.0.0", "2.0.0.0" -> {
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
    public boolean validateConfiguration(@Nullable CategoryConfig configuration) {
        return configuration != null;
    }

    @NotNull
    private CategoryConfig migrateV2Configuration(@NotNull CategoryConfigV2 categoryConfigV2) {
        List<CategoryConfig.PageConfig> pageConfigList = new ArrayList<>();
        categoryConfigV2.gui().pages().forEach(pageConfig -> {
            List<CategoryConfig.ButtonConfig> buttonConfigList = new ArrayList<>();
            pageConfig.buttons().forEach(buttonConfig -> {
                CategoryConfigV2.TransactionData legacyTransData = buttonConfig.transactionData();
                CategoryConfig.TransactionData transactionData = new CategoryConfig.TransactionData(
                        legacyTransData.transactionStyle(),
                        legacyTransData.transactionName(),
                        new CategoryConfig.PriceConfig(
                                legacyTransData.prices().buyPrice(),
                                legacyTransData.prices().sellPrice(),
                                legacyTransData.prices().buyPoints(),
                                legacyTransData.prices().sellPoints()),
                        legacyTransData.displayItem(),
                        getTransactionConfigurations(buttonConfig.transactionData())
                );

                buttonConfigList.add(new CategoryConfig.ButtonConfig(
                        buttonConfig.buttonType(),
                        buttonConfig.slot(),
                        buttonConfig.shopName(),
                        buttonConfig.permission(),
                        buttonConfig.displayItem(),
                        transactionData));
            });
            pageConfigList.add(new CategoryConfig.PageConfig(buttonConfigList));
        });

        return new CategoryConfig("3.0.0.0",
                categoryConfigV2.permission(),
                categoryConfigV2.gui().guiType(),
                categoryConfigV2.gui().name(),
                pageConfigList);
    }

    @NotNull
    private List<TransactionConfiguration> getTransactionConfigurations(@NotNull CategoryConfigV2.TransactionData legacyTransactionData) {
        List<TransactionConfiguration> transactionConfigurations = new ArrayList<>();
        transactionConfigurations.add(new ItemConfiguration(1, "skyshop:item", legacyTransactionData.transactionItem(), true));
        transactionConfigurations.add(new CommandConfiguration(1, "skyshop:commands", legacyTransactionData.buyCommands(), legacyTransactionData.sellCommands()));
        transactionConfigurations.add(new IslandSizeConfiguration(1, "skyshop:island_size", legacyTransactionData.islandSize().setIslandSize(), legacyTransactionData.islandSize().buyAmount(), legacyTransactionData.islandSize().sellAmount()));

        if(legacyTransactionData.prestigeMultiplier().multiplierType() != null
                && legacyTransactionData.prestigeMultiplier().multiplier() != null
                && legacyTransactionData.prestigeMultiplier().time() != null
                && legacyTransactionData.prestigeMultiplier().maxTime() != null)
            transactionConfigurations.add(new PrestigeMultiplierData(
                    1,
                    "skyprestige:multiplier",
                    legacyTransactionData.prestigeMultiplier().multiplierType(),
                    legacyTransactionData.prestigeMultiplier().activeMultiplierPreventPurchase(),
                    legacyTransactionData.prestigeMultiplier().activeMultiplierHigherPreventPurchase(),
                    legacyTransactionData.prestigeMultiplier().resetMultiplierTimeIfHigherMultiplier(),
                    legacyTransactionData.prestigeMultiplier().multiplier(),
                    legacyTransactionData.prestigeMultiplier().time(),
                    legacyTransactionData.prestigeMultiplier().maxTime()));
        return transactionConfigurations;
    }

    /**
     * Get the file name from a {@link Path} without the file extension.
     * @param path The {@link Path} to a file. You should ensure the {@link Path} actually points to a file.
     * @return A {@link String} containing the file name.
     * @throws RuntimeException if the {@link Path} is not a file.
     */
    private @NotNull String getFileNameWithoutExtension(@NotNull Path path) {
        if(!path.toFile().isFile()) throw new RuntimeException("Path does not point to a file.");

        String fileName = path.getFileName().toString();

        int lastDotIndex = fileName.lastIndexOf('.');

        if(lastDotIndex == -1) return fileName;

        return fileName.substring(0, lastDotIndex);
    }
}