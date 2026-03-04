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
import com.github.lukesky19.skylib.api.format.FormatUtil;
import com.github.lukesky19.skylib.api.gui.GUIType;
import com.github.lukesky19.skylib.api.itemstack.ItemStackConfig;
import com.github.lukesky19.skylib.libs.configurate.CommentedConfigurationNode;
import com.github.lukesky19.skylib.libs.configurate.ConfigurateException;
import com.github.lukesky19.skylib.libs.configurate.ConfigurationNode;
import com.github.lukesky19.skylib.libs.configurate.yaml.NodeStyle;
import com.github.lukesky19.skylib.libs.configurate.yaml.YamlConfigurationLoader;
import com.github.lukesky19.skyshop.SkyShop;
import com.github.lukesky19.skyshop.api.configuration.TransactionConfiguration;
import com.github.lukesky19.skyshop.configuration.category.data.CategoryConfigV4;
import com.github.lukesky19.skyshop.configuration.category.transaction.CommandConfiguration;
import com.github.lukesky19.skyshop.configuration.category.transaction.ItemConfiguration;
import com.github.lukesky19.skyshop.configuration.util.TransactionConfigurationSerializer;
import com.github.lukesky19.skyshop.registry.RegistryManager;
import com.github.lukesky19.skyshop.util.ButtonType;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import org.bukkit.Material;
import org.bukkit.inventory.ItemType;
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
    private final @NotNull TransactionConfigurationSerializer serializer;

    /**
     * Constructor
     * @param skyShop A {@link SkyShop} instance.
     * @param registryManager A {@link RegistryManager} instance.
    */
    public ShopConfigManager(@NotNull SkyShop skyShop, @NotNull RegistryManager registryManager) {
        this.skyShop = skyShop;
        this.serializer = new TransactionConfigurationSerializer(registryManager);
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
                if(legacyPath.toFile().isDirectory()) continue;
                // Get the file name with the extension
                String fileNameWithExtension = legacyPath.getFileName().toString();
                String identifier = getFileNameWithoutExtension(legacyPath);

                // Create the path where the migrated file will be saved
                Path categoryPath = Path.of(skyShop.getDataFolder() + File.separator + "category" + File.separator + fileNameWithExtension);

                // Don't overwrite existing files
                if(categoryPath.toFile().exists()) continue;

                YamlConfigurationLoader legacyLoader = ConfigurationUtility.getYamlConfigurationLoader(legacyPath);
                YamlConfigurationLoader categoryLoader = YamlConfigurationLoader.builder()
                        .nodeStyle(NodeStyle.BLOCK)
                        .path(categoryPath)
                        .indent(4)
                        .defaultOptions(opts ->
                                opts.serializers(build ->
                                        build.registerExact(TransactionConfiguration.class, serializer)))
                        .build();

                ConfigurationNode root = legacyLoader.load();
                ConfigurationNode versionNode = root.node("config-version");
                @Nullable String version = versionNode.virtual() ? null : versionNode.getString();

                CategoryConfigV4 categoryConfig;
                try {
                    if(version == null) {
                        @Nullable ShopConfigV1 shopConfigV1 = root.get(ShopConfigV1.class);
                        if(shopConfigV1 == null) {
                            logger.warn(AdventureUtil.deserialize("Failed to migrate " + fileNameWithExtension + " due due failure to load."));
                            continue;
                        }

                        List<ShopConfigV1.ShopPage> pages = shopConfigV1.pages().values().stream().toList();
                        if(pages.isEmpty()) {
                            logger.warn(AdventureUtil.deserialize("Failed to migrate the legacy " + fileNameWithExtension + " configuration due no pages configured."));
                            return;
                        }
                        ShopConfigV1.ShopPage firstPage = pages.getFirst();
                        GUIType guiType = GUIType.getType("CHEST_" + (firstPage.size() != null ? firstPage.size() : 54));

                        int pageNum = 0;
                        List<CategoryConfigV4.PageConfig> pageConfigList =  new ArrayList<>();
                        for(ShopConfigV1.ShopPage pageConfig : shopConfigV1.pages().values()) {
                            int buttonNum = 0;
                            List<CategoryConfigV4.ButtonConfig> buttonConfigList =  new ArrayList<>();

                            for(ShopConfigV1.ShopEntry button : pageConfig.entries().values()) {
                                Material material = null;
                                if(button.item().material() != null) {
                                    material = Material.getMaterial(button.item().material());
                                }
                                ItemType itemType = material != null ? material.asItemType() : null;

                                @Nullable ButtonType buttonType = ButtonType.getType(button.type());
                                if(buttonType != null) {
                                    if(buttonType.equals(ButtonType.COMMAND)) {
                                        buttonType = ButtonType.TRANSACTION;
                                    } else if(buttonType.equals(ButtonType.ITEM)) {
                                        buttonType = ButtonType.TRANSACTION;
                                    }
                                }

                                buttonConfigList.add(new CategoryConfigV4.ButtonConfig(
                                        buttonType,
                                        button.slot(),
                                        null,
                                        null,
                                        new ItemStackConfig(
                                                itemType,
                                                null,
                                                null,
                                                button.item().name(),
                                                button.item().lore(),
                                                null,
                                                null,
                                                List.of(),
                                                new ItemStackConfig.PotionConfig(null, List.of()),
                                                new ItemStackConfig.ColorConfig(false, null, null, null),
                                                null,
                                                List.of(),
                                                new ItemStackConfig.DecoratedPotConfig(null, null, null, null),
                                                new ItemStackConfig.ArmorTrimConfig(null, null),
                                                List.of(),
                                                new ItemStackConfig.OptionsConfig(null, null, null, null, null)),
                                        new CategoryConfigV4.TransactionData(
                                                identifier + ":" + pageNum + ":" + buttonNum,
                                                "items",
                                                itemType != null ? FormatUtil.formatItemTypeName(itemType) : "an item",
                                                new CategoryConfigV4.PriceConfig(
                                                        new CategoryConfigV4.PriceModifier(-1, -1, -1, -1, -1),
                                                        new CategoryConfigV4.PriceModifier(-1, -1, -1, -1, -1),
                                                        button.prices().buyPrice() != null ? button.prices().buyPrice() : -1.0,
                                                        -1,
                                                        button.prices().sellPrice() != null ? button.prices().sellPrice() : -1.0,
                                                        -1
                                                ),
                                                new ItemStackConfig(
                                                        itemType,
                                                        null,
                                                        null,
                                                        button.item().name(),
                                                        button.item().lore(),
                                                        null,
                                                        null,
                                                        List.of(),
                                                        new ItemStackConfig.PotionConfig(null, List.of()),
                                                        new ItemStackConfig.ColorConfig(false, null, null, null),
                                                        null,
                                                        List.of(),
                                                        new ItemStackConfig.DecoratedPotConfig(null, null, null, null),
                                                        new ItemStackConfig.ArmorTrimConfig(null, null),
                                                        List.of(),
                                                        new ItemStackConfig.OptionsConfig(null, null, null, null, null)),
                                                List.of(
                                                        new ItemConfiguration(1, "skyshop:item",
                                                                new ItemStackConfig(
                                                                        itemType,
                                                                        null,
                                                                        null,
                                                                        null,
                                                                        List.of(),
                                                                        null,
                                                                        null,
                                                                        List.of(),
                                                                        new ItemStackConfig.PotionConfig(null, List.of()),
                                                                        new ItemStackConfig.ColorConfig(false, null, null, null),
                                                                        null,
                                                                        List.of(),
                                                                        new ItemStackConfig.DecoratedPotConfig(null, null, null, null),
                                                                        new ItemStackConfig.ArmorTrimConfig(null, null),
                                                                        List.of(),
                                                                        new ItemStackConfig.OptionsConfig(null, null, null, null, null)),
                                                                true),
                                                        new CommandConfiguration(1, "skyshop:commands", button.commands().buyCommands(), button.commands().sellCommands())
                                                ))));

                                buttonNum++;
                            }

                            pageConfigList.add(new CategoryConfigV4.PageConfig(buttonConfigList));

                            pageNum++;
                        }

                        categoryConfig = new CategoryConfigV4(
                                4,
                                null,
                                guiType,
                                firstPage.name(),
                                pageConfigList);
                    } else {
                        if(!version.equals("2.0.0.0")) {
                            logger.warn(AdventureUtil.deserialize("Failed to migrate " + fileNameWithExtension + " due to an unsupported version. Version: " + version));
                            continue;
                        }

                        @Nullable ShopConfigV2 shopConfigV2 = root.get(ShopConfigV2.class);
                        // If the shop config is null, move to the next file
                        if(shopConfigV2 == null) {
                            logger.warn(AdventureUtil.deserialize("Failed to migrate " + fileNameWithExtension + " due due failure to load."));
                            continue;
                        }

                        int pageNum = 0;
                        List<CategoryConfigV4.PageConfig> pageConfigList =  new ArrayList<>();
                        for(ShopConfigV2.PageConfig pageConfig : shopConfigV2.gui().pages()) {
                            int buttonNum = 0;
                            List<CategoryConfigV4.ButtonConfig> buttonConfigList =  new ArrayList<>();

                            for(ShopConfigV2.Button button : pageConfig.buttons()) {
                                ButtonType buttonType = null;
                                if(button.buttonType() != null) {
                                    if(button.buttonType().equals(ButtonType.COMMAND)) {
                                        buttonType = ButtonType.TRANSACTION;
                                    } else if(button.buttonType().equals(ButtonType.ITEM)) {
                                        buttonType = ButtonType.TRANSACTION;
                                    } else {
                                        buttonType = button.buttonType();
                                    }
                                }

                                buttonConfigList.add(new CategoryConfigV4.ButtonConfig(
                                        buttonType,
                                        button.slot(),
                                        null,
                                        null,
                                        button.displayItem(),
                                        new CategoryConfigV4.TransactionData(
                                                identifier + ":" + pageNum + ":" + buttonNum,
                                                button.transactionData().transactionStyle(),
                                                button.transactionData().transactionName(),
                                                new CategoryConfigV4.PriceConfig(
                                                        new CategoryConfigV4.PriceModifier(-1, -1, -1, -1, -1),
                                                        new CategoryConfigV4.PriceModifier(-1, -1, -1, -1, -1),
                                                        Objects.requireNonNullElse(button.transactionData().buyPrice(), -1.0),
                                                        -1,
                                                        Objects.requireNonNullElse(button.transactionData().sellPrice(), -1.0),
                                                        -1),
                                                button.transactionData().displayItem(),
                                                List.of(
                                                        new ItemConfiguration(1, "skyshop:item", button.transactionData().transactionItem(), true),
                                                        new CommandConfiguration(1, "skyshop:commands", button.transactionData().buyCommands(), button.transactionData().sellCommands())
                                                )))
                                );

                                buttonNum++;
                            }

                            pageConfigList.add(new CategoryConfigV4.PageConfig(buttonConfigList));
                        }

                        categoryConfig = new CategoryConfigV4(
                                4,
                                null,
                                shopConfigV2.gui().guiType(),
                                shopConfigV2.gui().name(),
                                pageConfigList);
                    }

                    // Save migrated config
                    CommentedConfigurationNode node = categoryLoader.createNode();
                    node.set(CategoryConfigV4.class, categoryConfig);
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
            logger.error(AdventureUtil.deserialize("Failed to migrate legacy shop configuration files. Error: " + e.getMessage()));
        }
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