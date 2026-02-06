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
import com.github.lukesky19.skylib.libs.configurate.ConfigurateException;
import com.github.lukesky19.skylib.libs.configurate.ConfigurationNode;
import com.github.lukesky19.skylib.libs.configurate.serialize.SerializationException;
import com.github.lukesky19.skylib.libs.configurate.yaml.NodeStyle;
import com.github.lukesky19.skylib.libs.configurate.yaml.YamlConfigurationLoader;
import com.github.lukesky19.skyshop.SkyShop;
import com.github.lukesky19.skyshop.api.configuration.TransactionConfiguration;
import com.github.lukesky19.skyshop.api.serializer.Serializer;
import com.github.lukesky19.skyshop.configuration.category.data.CategoryConfigV2;
import com.github.lukesky19.skyshop.configuration.category.data.CategoryConfigV3;
import com.github.lukesky19.skyshop.configuration.category.data.CategoryConfigV4;
import com.github.lukesky19.skyshop.configuration.category.serializer.migration.MultiplierConfigurationSerializer;
import com.github.lukesky19.skyshop.configuration.category.transaction.CommandConfiguration;
import com.github.lukesky19.skyshop.configuration.category.transaction.IslandSizeConfiguration;
import com.github.lukesky19.skyshop.configuration.category.transaction.ItemConfiguration;
import com.github.lukesky19.skyshop.configuration.category.transaction.migration.PrestigeMultiplierData;
import com.github.lukesky19.skyshop.configuration.settings.SettingsManager;
import com.github.lukesky19.skyshop.configuration.settings.data.SettingsV4;
import com.github.lukesky19.skyshop.configuration.util.TransactionConfigurationSerializer;
import com.github.lukesky19.skyshop.prices.PriceManager;
import com.github.lukesky19.skyshop.registry.RegistryManager;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Stream;

/**
 * This class manages everything related to handling the plugin's category config files.
 */
public class CategoryConfigManager {
    private final @NotNull SkyShop skyShop;
    private final @NotNull ComponentLogger logger;
    private final @NotNull SettingsManager settingsManager;
    private final @NotNull PriceManager priceManager;
    private final @NotNull RegistryManager registryManager;
    private final @NotNull TransactionConfigurationSerializer serializer;

    private final @NotNull Map<String, CategoryConfigV4> configMap = new HashMap<>();

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
        this.skyShop = skyShop;
        this.logger = skyShop.getComponentLogger();
        this.settingsManager = settingsManager;
        this.priceManager = priceManager;
        this.registryManager = registryManager;
        this.serializer = new TransactionConfigurationSerializer(registryManager);
    }

    /**
     * Get the {@link CategoryConfigV4} for the id provided.
     * @param categoryId The id.
     * @return The {@link CategoryConfigV4} or null.
     */
    public @Nullable CategoryConfigV4 getConfiguration(@NotNull String categoryId) {
        return configMap.get(categoryId);
    }

    /**
     * Get a {@link Set} of {@link String}s for the known category config ids loaded.
     * @return A {@link Set} of {@link String}s for the known category config ids loaded.
     */
    public @NotNull Set<@NotNull String> getCategoryIds() {
        return configMap.keySet();
    }

    /**
     * Load all category gui configuration files in SkyShop/category
     */
    public void loadConfigurations() {
        priceManager.clearCache();
        configMap.clear();

        saveBundledConfig();

        Path categoryPath = Path.of(skyShop.getDataFolder() + File.separator + "category");

        try(Stream<Path> stream = Files.walk(categoryPath)) {
            stream.filter(path -> !path.toFile().isDirectory()).forEach(path -> {
                String identifier = getFileNameWithoutExtension(path);

                loadConfiguration(identifier, path);
            });
        } catch (IOException e) {
            logger.error(AdventureUtil.deserialize("Failed to load category configuration files. " + e.getMessage()));
        }
    }

    /**
     * Load the configuration.
     * @param identifier The config version identifier.
     * @param configurationPath The configuration path.
     */
    public void loadConfiguration(@NotNull String identifier, @NotNull Path configurationPath) {
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
            int version = getVersion(root);

            @Nullable CategoryConfigV4 categoryConfig;
            switch(version) {
                case 4 -> {
                    categoryConfig = root.get(CategoryConfigV4.class);
                    if(categoryConfig == null) {
                        logger.warn(AdventureUtil.deserialize("Failed to load version 4 configuration file " + (identifier + ".yml") + ". Class name: " + this.getClass().getName()));
                        return;
                    }
                }

                case 3 -> { // Former: 3.0.0.0
                    CategoryConfigV3 categoryConfigV3 = root.get(CategoryConfigV3.class);
                    if(categoryConfigV3 == null) {
                        logger.warn(AdventureUtil.deserialize("Failed to load version 3 configuration file " + (identifier + ".yml") + ". Class name: " + this.getClass().getName()));
                        return;
                    }

                    categoryConfig = versionThreeToFour(identifier, categoryConfigV3);

                    saveConfiguration(configurationPath, categoryConfig);
                }

                // 2.0.0.0 is version 1 because for some reason I used 2.0.0.0 when converting shop files to category files.
                // So technically, 2.0.0.0 is the first version for category files.
                case 2, 1 -> { // 2 = Former 2.1.0.0, 1 = Former 2.0.0.0
                    CategoryConfigV2 categoryConfigV2 = root.get(CategoryConfigV2.class);
                    if(categoryConfigV2 == null) {
                        logger.warn(AdventureUtil.deserialize("Failed to load version 1 or 2 configuration file " + (identifier + ".yml") + ". Class name: " + this.getClass().getName()));
                        return;
                    }

                    // Migrate V2 config to V4
                    categoryConfig = versionTwoToFour(identifier, categoryConfigV2);

                    // Store the existing serializer if any
                    String id = "skyprestige:multiplier";
                    @Nullable Serializer existingSerializer = registryManager.getSerializer(id);

                    // Temporarily overwrite the serializer for prestige multiplier for migration purposes
                    registryManager.registerSerializer(id, new MultiplierConfigurationSerializer());

                    // Save the configuration
                    saveConfiguration(configurationPath, categoryConfig);

                    // Revert temporarily overwritten serializer
                    if(existingSerializer != null) {
                        registryManager.registerSerializer(id, existingSerializer);
                    } else {
                        registryManager.unregisterSerializer(id);
                    }

                    // Re-load configuration from disk
                    // This is required so that the prestige multiplier transaction configuration uses the SkyPrestige class and not the migration class from SkyShop
                    loadConfiguration(identifier, configurationPath);

                    return;
                }

                default -> {
                    logger.warn(AdventureUtil.deserialize("Failed to load configuration file " + (identifier + ".yml") + " due to an unsupported config version. Version: " + version + ". Class name: " + this.getClass().getName()));
                    return;
                }
            }

            // Check if the configuration is invalid
            if(!validateConfiguration(categoryConfig)) {
                logger.warn(AdventureUtil.deserialize("Configuration validation failed for file " + (identifier + ".yml") + ". Class name: " + this.getClass().getName()));
                return;
            }

            // Cache sell prices
            priceManager.cacheCategorySellPrices(categoryConfig);

            // Store the configuration
            configMap.put(identifier, categoryConfig);
        } catch (ConfigurateException configurateException) {
            logger.error(AdventureUtil.deserialize("Failed to load the configuration. Error: " + configurateException.getMessage()));
        }
    }

    /**
     * Save the configuration.
     * @param configurationPath The path to save to.
     * @param configuration The configuration.
     */
    public void saveConfiguration(@NotNull Path configurationPath, @NonNull CategoryConfigV4 configuration) {
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

            node.set(CategoryConfigV4.class, configuration);

            yamlConfigurationLoader.save(node);
        } catch (ConfigurateException configurateException) {
            logger.error(AdventureUtil.deserialize("Failed to save configuration. Error: " + configurateException.getMessage()));
        }
    }

    /**
     * Save the example category configuration files if they don't exist.
     * Will only save if {@link SettingsV4#firstRun()} is true.
     */
    public void saveBundledConfig() {
        SettingsV4 settings = settingsManager.getConfiguration();
        if(settings == null) return;
        if(!settings.firstRun()) return;

        Path exampleMenuPath = Path.of(skyShop.getDataFolder() + File.separator + "category" + File.separator + "menu.yml");
        Path exampleShopPath = Path.of(skyShop.getDataFolder() + File.separator + "category" + File.separator + "example.yml");

        // Save the example menu category if it doesn't exist
        if(!exampleMenuPath.toFile().exists()) {
            skyShop.saveResource("category" + File.separator + "menu.yml", false);
        }

        // Save the example shop category if it doesn't exist
        if(!exampleShopPath.toFile().exists()) {
            skyShop.saveResource("category" + File.separator + "example.yml", false);
        }

        settingsManager.setFirstRunFalse();
    }

    /**
     * Validate the configuration.
     * @param configuration The configuration.
     * @return true if valid, false if not.
     */
    public boolean validateConfiguration(@Nullable CategoryConfigV4 configuration) {
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
                    case "3.1.0.0" -> {
                        versionNode.set(4);
                        version = 4;
                    }

                    case "3.0.0.0" -> {
                        versionNode.set(3);
                        version = 3;
                    }

                    case "2.1.0.0" -> {
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
                logger.warn(AdventureUtil.deserialize("Failed to convert String-based version to numeric version"));
                version = 0;
            }
        }

        return version;
    }

    /**
     * Converts {@link CategoryConfigV2} configuration to {@link CategoryConfigV4}.
     * @param identifier The configuration identifier.
     * @param categoryConfigV2 The {@link CategoryConfigV2}.
     * @return The {@link CategoryConfigV4}.
     */
    private @NotNull CategoryConfigV4 versionTwoToFour(@NotNull String identifier, @NotNull CategoryConfigV2 categoryConfigV2) {
        List<CategoryConfigV3.PageConfig> pageConfigList = new ArrayList<>();

        for(CategoryConfigV2.PageConfig pageConfig : categoryConfigV2.gui().pages()) {
            List<CategoryConfigV3.ButtonConfig> buttonConfigList = new ArrayList<>();
            for(CategoryConfigV2.ButtonConfig buttonConfig : pageConfig.buttons()) {
                CategoryConfigV2.TransactionData legacyTransactionData = buttonConfig.transactionData();
                CategoryConfigV3.TransactionData transactionData = new CategoryConfigV3.TransactionData(
                        legacyTransactionData.transactionStyle(),
                        legacyTransactionData.transactionName(),
                        new CategoryConfigV3.PriceConfig(
                                legacyTransactionData.prices().buyPrice(),
                                legacyTransactionData.prices().sellPrice(),
                                legacyTransactionData.prices().buyPoints(),
                                legacyTransactionData.prices().sellPoints()),
                        legacyTransactionData.displayItem(),
                        getTransactionConfigurations(buttonConfig.transactionData())
                );

                buttonConfigList.add(new CategoryConfigV3.ButtonConfig(
                        buttonConfig.buttonType(),
                        buttonConfig.slot(),
                        buttonConfig.shopName(),
                        buttonConfig.permission(),
                        buttonConfig.displayItem(),
                        transactionData));
            }

            pageConfigList.add(new CategoryConfigV3.PageConfig(buttonConfigList));
        }

        CategoryConfigV3 categoryConfigV3 = new CategoryConfigV3(
                "3.0.0.0",
                categoryConfigV2.permission(),
                categoryConfigV2.gui().guiType(),
                categoryConfigV2.gui().name(),
                pageConfigList);

        return versionThreeToFour(identifier, categoryConfigV3);
    }

    /**
     * Converts version three to version four (latest).
     * @return The {@link CategoryConfigV4}.
     */
    private @NotNull CategoryConfigV4 versionThreeToFour(@NotNull String identifier, @NotNull CategoryConfigV3 categoryConfigV3) {
        int pageNum = 0;
        List<CategoryConfigV4.PageConfig> pageConfigList = new ArrayList<>();
        for(CategoryConfigV3.PageConfig pageConfig : categoryConfigV3.pages()) {
            List<CategoryConfigV4.ButtonConfig> buttonConfigList = new ArrayList<>();

            int buttonNum = 0;
            for(CategoryConfigV3.ButtonConfig buttonConfig : pageConfig.buttons()) {
                CategoryConfigV3.TransactionData legacyTransactionData = buttonConfig.transactionData();
                CategoryConfigV4.TransactionData transactionData = legacyTransactionData != null ?
                        new CategoryConfigV4.TransactionData(
                                identifier + ":" + pageNum + ":" + buttonNum,
                                legacyTransactionData.transactionStyle(),
                                legacyTransactionData.transactionName(),
                                new CategoryConfigV4.PriceConfig(
                                        new CategoryConfigV4.PriceModifier(
                                                -1,
                                                -1,
                                                -1,
                                                -1,
                                                -1),
                                        new CategoryConfigV4.PriceModifier(
                                                -1,
                                                -1,
                                                -1,
                                                -1,
                                                -1),
                                        legacyTransactionData.prices().buyPrice(),
                                        legacyTransactionData.prices().buyPoints(),
                                        legacyTransactionData.prices().sellPrice(),
                                        legacyTransactionData.prices().sellPoints()),
                                legacyTransactionData.displayItem(),
                                legacyTransactionData.transactionList())
                        : null;

                buttonConfigList.add(new CategoryConfigV4.ButtonConfig(
                        buttonConfig.buttonType(),
                        buttonConfig.slot(),
                        buttonConfig.shopName(),
                        buttonConfig.permission(),
                        buttonConfig.displayItem(),
                        transactionData));

                buttonNum++;
            }

            pageConfigList.add(new CategoryConfigV4.PageConfig(buttonConfigList));
        }

        return new CategoryConfigV4(
                4,
                categoryConfigV3.permission(),
                categoryConfigV3.guiType(),
                categoryConfigV3.guiName(),
                pageConfigList);
    }

    /**
     * Get the {@link List} of {@link TransactionConfiguration}, converting the version 2 format to the version 3+ format.
     * @param legacyTransactionData The {@link CategoryConfigV2.TransactionData}.
     * @return The {@link List} of {@link TransactionConfiguration}.
     */
    private @NotNull List<TransactionConfiguration> getTransactionConfigurations(@NotNull CategoryConfigV2.TransactionData legacyTransactionData) {
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