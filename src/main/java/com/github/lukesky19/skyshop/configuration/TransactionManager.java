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

import com.github.lukesky19.skylib.api.adventure.AdventureUtil;
import com.github.lukesky19.skylib.api.configurate.ConfigurationUtility;
import com.github.lukesky19.skylib.libs.configurate.ConfigurateException;
import com.github.lukesky19.skylib.libs.configurate.yaml.YamlConfigurationLoader;
import com.github.lukesky19.skyshop.SkyShop;
import com.github.lukesky19.skyshop.data.gui.ShopConfig;
import com.github.lukesky19.skyshop.data.gui.TransactionConfig;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * This class manages everything related to handling the plugin's transaction config files.
 */
public class TransactionManager {
    private final @NotNull SkyShop skyShop;
    private final @NotNull Map<String, TransactionConfig> transactionConfigurations = new HashMap<>();

    /**
     * Constructor
     * @param skyShop A {@link SkyShop instance}
     */
    public TransactionManager(@NotNull SkyShop skyShop) {
        this.skyShop = skyShop;
    }

    /**
     * Get the {@link TransactionConfig} for the provided shop id.
     * @param transactionStyle The id of the transaction style to get the configuration for. The transaction style is just the file name without the file extension.
     * @return An {@link Optional} containing {@link ShopConfig} for the provided shop id. Will be empty if no {@link ShopConfig} exists for that id.
     */
    public @NotNull Optional<TransactionConfig> getTransactionConfig(@NotNull String transactionStyle) {
        return Optional.ofNullable(transactionConfigurations.get(transactionStyle));
    }

    /**
     * A method to reload the plugin's transaction config.
     */
    public void reload() {
        ComponentLogger logger = skyShop.getComponentLogger();

        // Clear the current loaded configurations
        transactionConfigurations.clear();

        // Save default configuration that doesn't exist
        saveDefaultConfig();

        // Create the path to the transaction_styles directory.
        Path transactionsPath = Path.of(skyShop.getDataFolder() + File.separator + "transaction_styles");

        // Walk through all files
        try(Stream<Path> stream = Files.walk(transactionsPath)) {
            // Filter to only include files and then attempt to load the configuration
            stream.filter(Files::isRegularFile).forEach(path -> {
                // Get the file name with the extension
                String fileNameWithExtension = path.getFileName().toString();

                // Attempt to load the config
                YamlConfigurationLoader loader = ConfigurationUtility.getYamlConfigurationLoader(path);
                try {
                    // Load the config
                    TransactionConfig transactionConfig = loader.load().get(TransactionConfig.class);
                    // If non-null, store the config
                    if(transactionConfig != null) {
                        // Get the transaction style name, which is the file name without the extension
                        String fileNameWithoutExtension = getFileNameWithoutExtension(path);
                        // Store the transaction configuration
                        transactionConfigurations.put(fileNameWithoutExtension, transactionConfig);
                    } else {
                        logger.warn(AdventureUtil.serialize("Failed to load " + fileNameWithExtension + " configuration."));
                    }
                } catch (ConfigurateException e) {
                    logger.warn(AdventureUtil.serialize("Failed to load " + fileNameWithExtension + " configuration. " + e.getMessage()));
                }
            });
        } catch (IOException e) {
            logger.error(AdventureUtil.serialize("Failed to load transaction configuration files. " + e.getMessage()));
        }
    }

    /**
     * Save the default transaction style configurations if they do not exist.
     */
    private void saveDefaultConfig() {
        Path itemsStylePath = Path.of(skyShop.getDataFolder() + File.separator + "transaction_styles" + File.separator + "items.yml");
        Path singleCommandStylePath = Path.of(skyShop.getDataFolder() + File.separator + "transaction_styles" + File.separator + "single_command.yml");

        if(!itemsStylePath.toFile().exists()) skyShop.saveResource("transaction_styles/items.yml", false);
        if(!singleCommandStylePath.toFile().exists()) skyShop.saveResource("transaction_styles/single_command.yml", false);
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
