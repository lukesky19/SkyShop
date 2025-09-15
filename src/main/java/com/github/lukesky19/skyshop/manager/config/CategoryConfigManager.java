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
package com.github.lukesky19.skyshop.manager.config;

import com.github.lukesky19.skylib.api.adventure.AdventureUtil;
import com.github.lukesky19.skylib.api.configurate.ConfigurationUtility;
import com.github.lukesky19.skylib.libs.configurate.ConfigurateException;
import com.github.lukesky19.skylib.libs.configurate.yaml.YamlConfigurationLoader;
import com.github.lukesky19.skyshop.SkyShop;
import com.github.lukesky19.skyshop.config.gui.CategoryConfig;
import com.github.lukesky19.skyshop.config.settings.Settings;
import com.github.lukesky19.skyshop.manager.PriceManager;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

/**
 * This class manages everything related to handling the plugin's category config files.
*/
public class CategoryConfigManager {
    private final @NotNull SkyShop skyShop;
    private final @NotNull SettingsManager settingsManager;
    private final @NotNull PriceManager priceManager;
    private final @NotNull Map<@NotNull String, @NotNull CategoryConfig> categoryConfigMap = new HashMap<>();

    /**
     * Constructor
     * @param skyShop A {@link SkyShop} instance.
     * @param settingsManager A {@link SettingsManager} instance.
     * @param priceManager A {@link PriceManager} instance.
    */
    public CategoryConfigManager(
            @NotNull SkyShop skyShop,
            @NotNull SettingsManager settingsManager,
            @NotNull PriceManager priceManager) {
        this.skyShop = skyShop;
        this.settingsManager = settingsManager;
        this.priceManager = priceManager;
    }

    /**
     * Get the {@link CategoryConfig} for the provided category id.
     * @param categoryId The category id to get the configuration for.
     * @return An {@link Optional} containing {@link CategoryConfig} for the provided category id. Will be empty if no {@link CategoryConfig} exists for that id.
     */
    public @NotNull Optional<CategoryConfig> getCategoryConfig(@NotNull String categoryId) {
        return Optional.ofNullable(categoryConfigMap.get(categoryId));
    }

    /**
     * Get a {@link Set} of {@link String}s for the known category config ids loaded.
     * @return A {@link Set} of {@link String}s for the known category config ids loaded.
     */
    public @NotNull Set<@NotNull String> getCategoryIds() {
        return categoryConfigMap.keySet();
    }

    /**
     * A method to reload the plugin's category config files.
    */
    public void reload() {
        ComponentLogger logger = skyShop.getComponentLogger();

        // Clear cached sell prices.
        priceManager.clearCache();

        // Clear the current loaded configurations
        categoryConfigMap.clear();

        // Save example config
        saveDefaultConfig();

        // Create the path to the category directory.
        Path categoryPath = Path.of(skyShop.getDataFolder() + File.separator + "category");

        // Walk through all files
        try(Stream<Path> stream = Files.walk(categoryPath)) {
            // Filter to only include files and then attempt to load the configuration
            stream.filter(Files::isRegularFile).forEach(path -> {
                // Get the file name with the extension
                String fileNameWithExtension = path.getFileName().toString();

                // Attempt to load the config
                YamlConfigurationLoader loader = ConfigurationUtility.getYamlConfigurationLoader(path);
                try {
                    // Load the config
                    CategoryConfig categoryConfig = loader.load().get(CategoryConfig.class);
                    // If non-null, store the config and attempt to cache the sell prices.
                    if(categoryConfig != null) {
                        // Get the category name, which is the file name without the extension
                        String fileNameWithoutExtension = getFileNameWithoutExtension(path);
                        // Store the category configuration
                        categoryConfigMap.put(fileNameWithoutExtension, categoryConfig);

                        // Cache sell prices for the category configuration
                        priceManager.cacheCategorySellPrices(categoryConfig);
                    } else {
                        logger.warn(AdventureUtil.serialize("Failed to load " + fileNameWithExtension + " configuration."));
                    }
                } catch (ConfigurateException e) {
                    logger.warn(AdventureUtil.serialize("Failed to load " + fileNameWithExtension + " configuration. " + e.getMessage()));
                }
            });
        } catch (IOException e) {
            logger.error(AdventureUtil.serialize("Failed to load category configuration files. " + e.getMessage()));
        }
    }

    /**
     * Save the example category configuration files if they don't exist.
     * Will only save if {@link Settings#firstRun()} is true.
     */
    private void saveDefaultConfig() {
        Settings settings = settingsManager.getSettingsConfig();
        if(settings == null) return;
        if(!settings.firstRun()) return;

        Path exampleMenuPath = Path.of(skyShop.getDataFolder() + File.separator + "category" + File.separator + "menu.yml");
        Path exampleShopPath = Path.of(skyShop.getDataFolder() + File.separator + "category" + File.separator + "example.yml");

        // Save the example menu category if it doesn't exist
        if(!exampleMenuPath.toFile().exists()) {
            skyShop.saveResource("category/menu.yml", false);
        }

        // Save the example shop category if it doesn't exist
        if(!exampleShopPath.toFile().exists()) {
            skyShop.saveResource("category/example.yml", false);
        }

        settingsManager.setFirstRunFalse();
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
