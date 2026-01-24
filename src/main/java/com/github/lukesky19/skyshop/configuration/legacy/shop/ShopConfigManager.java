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
package com.github.lukesky19.skyshop.configuration.legacy.shop;

import com.github.lukesky19.skylib.api.adventure.AdventureUtil;
import com.github.lukesky19.skylib.api.configurate.ConfigurationUtility;
import com.github.lukesky19.skylib.libs.configurate.CommentedConfigurationNode;
import com.github.lukesky19.skylib.libs.configurate.ConfigurateException;
import com.github.lukesky19.skylib.libs.configurate.yaml.YamlConfigurationLoader;
import com.github.lukesky19.skyshop.SkyShop;
import com.github.lukesky19.skyshop.configuration.category.gui.CategoryConfig;
import com.github.lukesky19.skyshop.configuration.category.gui.CategoryConfigV2;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * This class manages the migration of legacy shop config files.
*/
public class ShopConfigManager {
    private final @NotNull SkyShop skyShop;

    /**
     * Constructor
     * @param skyShop A {@link SkyShop} instance.
    */
    public ShopConfigManager(@NotNull SkyShop skyShop) {
        this.skyShop = skyShop;
    }

    /**
     * A method to migrate the plugin's legacy shop config files.
    */
    public void migrate() {
        ComponentLogger logger = skyShop.getComponentLogger();

        // Create the path to the shops directory.
        Path shopsPath = Path.of(skyShop.getDataFolder() + File.separator + "shops");
        // If the path doesn't exist, return as there is nothing to migrate
        if(!shopsPath.toFile().exists()) return;

        // Walk through all files
        try(Stream<Path> stream = Files.walk(shopsPath)) {
            for(Path legacyPath : stream.toList()) {
                // Get the file name with the extension
                String fileNameWithExtension = legacyPath.getFileName().toString();

                // Create the path where the migrated file will be saved
                Path categoryPath = Path.of(skyShop.getDataFolder() + File.separator + "category" + File.separator + fileNameWithExtension);

                // Don't overwrite existing files
                if(categoryPath.toFile().exists()) continue;

                YamlConfigurationLoader legacyLoader = ConfigurationUtility.getYamlConfigurationLoader(legacyPath);
                YamlConfigurationLoader categoryLoader = ConfigurationUtility.getYamlConfigurationLoader(categoryPath);

                try {
                    // Load the legacy shop config
                    @Nullable ShopConfig shopConfig = legacyLoader.load().get(ShopConfig.class);
                    // If the shop config is null, move to the next file
                    if(shopConfig == null) continue;

                    // Create the new config from the legacy config
                    CategoryConfigV2 categoryConfig = createCategoryConfig(shopConfig);

                    // Save migrated config
                    CommentedConfigurationNode node = categoryLoader.createNode();
                    node.set(CategoryConfig.class, categoryConfig);
                    categoryLoader.save(node);

                    // Delete the legacy file.
                    legacyPath.toFile().delete();
                } catch (ConfigurateException e) {
                    logger.error(AdventureUtil.deserialize("Failed to migrate the legacy shop configuration for file " + fileNameWithExtension + ". " + e.getMessage()));
                }
            }

            @Nullable File[] filesList = shopsPath.toFile().listFiles();
            if(filesList == null) return;

            // Delete the directory if all files were migrated and deleted.
            if(filesList.length == 0) {
                shopsPath.toFile().delete();
            }
        } catch (IOException e) {
            logger.error(AdventureUtil.deserialize("Failed to migrate legacy shop configuration files. " + e.getMessage()));
        }
    }

    /**
     * Create the {@link CategoryConfigV2} from the {@link ShopConfig}.
     * @param shopConfig The legacy {@link ShopConfig}.
     * @return The created {@link CategoryConfigV2}.
     */
    private @NotNull CategoryConfigV2 createCategoryConfig(@NotNull ShopConfig shopConfig) {
        List<CategoryConfigV2.PageConfig> pageConfigList = new ArrayList<>();

        for(ShopConfig.PageConfig legacyPageConfig : shopConfig.gui().pages()) {
            CategoryConfigV2.PageConfig categoryPageConfig = createCategoryPageConfig(legacyPageConfig);

            pageConfigList.add(categoryPageConfig);
        }

        CategoryConfigV2.GuiData guiData = new CategoryConfigV2.GuiData(shopConfig.gui().guiType(), shopConfig.gui().name(), pageConfigList);

        return new CategoryConfigV2("2.1.0.0", null, guiData);
    }

    /**
     * Create the {@link CategoryConfigV2.PageConfig} from the {@link ShopConfig.PageConfig}.
     * @param legacyPageConfig The legacy {@link ShopConfig.PageConfig}.
     * @return The created {@link CategoryConfigV2.PageConfig}.
     */
    private @NotNull CategoryConfigV2.PageConfig createCategoryPageConfig(@NotNull ShopConfig.PageConfig legacyPageConfig) {
        List<CategoryConfigV2.ButtonConfig> buttonConfigList = new ArrayList<>();

        for(ShopConfig.Button legacyButtonConfig : legacyPageConfig.buttons()) {
            CategoryConfigV2.TransactionData transactionData = getTransactionData(legacyButtonConfig.transactionData());

            CategoryConfigV2.ButtonConfig categoryButtonConfig = new CategoryConfigV2.ButtonConfig(
                    legacyButtonConfig.buttonType(),
                    legacyButtonConfig.slot(),
                    null,
                    legacyButtonConfig.displayItem(),
                    transactionData,
                    null);

            buttonConfigList.add(categoryButtonConfig);
        }

        return new CategoryConfigV2.PageConfig(buttonConfigList);
    }

    /**
     * Create the {@link CategoryConfigV2.TransactionData} from the {@link ShopConfig.TransactionData}.
     * @param legacyTransactionData The legacy {@link ShopConfig.TransactionData}.
     * @return The created {@link CategoryConfigV2.TransactionData}.
     */
    private @NotNull CategoryConfigV2.TransactionData getTransactionData(@NotNull ShopConfig.TransactionData legacyTransactionData) {
        return new CategoryConfigV2.TransactionData(
                legacyTransactionData.transactionStyle(),
                legacyTransactionData.transactionName(),
                new CategoryConfigV2.PriceConfig(Objects.requireNonNullElse(legacyTransactionData.buyPrice(), -1.0), Objects.requireNonNullElse(legacyTransactionData.sellPrice(), -1.0), -1, -1),
                legacyTransactionData.displayItem(),
                legacyTransactionData.transactionItem(),
                legacyTransactionData.buyCommands(),
                legacyTransactionData.sellCommands(),
                new CategoryConfigV2.IslandSizeData(false, null, null),
                new CategoryConfigV2.PrestigeMultiplierData(null, true, true, true, null, null, null));
    }
}