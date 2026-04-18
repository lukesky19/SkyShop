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
package com.github.lukesky19.skyshop.configuration.legacy.menu;

import com.github.lukesky19.skylib.common.api.adventure.AdventureUtility;
import com.github.lukesky19.skylib.common.platform.PlatformUtils;
import com.github.lukesky19.skylib.libs.configurate.CommentedConfigurationNode;
import com.github.lukesky19.skylib.libs.configurate.ConfigurateException;
import com.github.lukesky19.skylib.libs.configurate.ConfigurationNode;
import com.github.lukesky19.skylib.libs.configurate.yaml.NodeStyle;
import com.github.lukesky19.skylib.libs.configurate.yaml.YamlConfigurationLoader;
import com.github.lukesky19.skylib.paper.api.gui.GUIType;
import com.github.lukesky19.skylib.paper.api.itemstack.ItemStackConfig;
import com.github.lukesky19.skyshop.SkyShop;
import com.github.lukesky19.skyshop.configuration.category.data.CategoryConfigV4;
import com.github.lukesky19.skyshop.util.ButtonType;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import org.bukkit.Material;
import org.bukkit.inventory.ItemType;
import org.jspecify.annotations.NonNull;

import java.io.File;
import java.nio.file.Path;
import java.util.List;

/**
 * This class manages the migration of the legacy menu config file.
 */
public class MenuConfigManager {
    private final @NonNull SkyShop skyShop;

    /**
     * Constructor
     * @param skyShop A {@link SkyShop} instance.
    */
    public MenuConfigManager(@NonNull SkyShop skyShop) {
        this.skyShop = skyShop;
    }

    /**
     * A method to migrate the plugin's legacy menu config.
     */
    public void migrate() {
        ComponentLogger logger = skyShop.getComponentLogger();

        // Create the path to the file.
        Path legacyPath = Path.of(skyShop.getDataFolder() + File.separator + "menu.yml");
        Path categoryPath = Path.of(skyShop.getDataFolder() + File.separator + "category" + File.separator + "menu.yml");

        // If the legacy menu.yml doesn't exist, return as there is nothing to migrate.
        if(!legacyPath.toFile().exists()) return;
        // Don't overwrite any existing files.
        if(categoryPath.toFile().exists()) return;

        // Attempt to load and migrate the config.
        YamlConfigurationLoader legacyLoader = createLoader(legacyPath);
        YamlConfigurationLoader categoryLoader = createLoader(categoryPath);
        try {
            ConfigurationNode root = legacyLoader.load();
            ConfigurationNode versionNode = root.node("config-version");
            String version = versionNode.virtual() ? null : versionNode.getString();

            CategoryConfigV4 categoryConfig;
            if(version == null) {
                MenuConfigV1 menuConfigV1 = root.get(MenuConfigV1.class);
                if(menuConfigV1 == null) {
                    logger.warn(AdventureUtility.plain("Failed to migrate the legacy menu.yml configuration due failure to load."));
                    return;
                }

                List<MenuConfigV1.MenuPage> pages = menuConfigV1.pages().values().stream().toList();
                if(pages.isEmpty()) {
                    logger.warn(AdventureUtility.plain("Failed to migrate the legacy menu.yml configuration due no pages configured."));
                    return;
                }
                MenuConfigV1.MenuPage firstPage = pages.getFirst();
                GUIType guiType = GUIType.getType("CHEST_" + (firstPage.size() != null ? firstPage.size() : 54));

                categoryConfig = new CategoryConfigV4(
                        4,
                        null,
                        guiType,
                        firstPage.name(),
                        pages.stream().map(pageConfig ->
                                new CategoryConfigV4.PageConfig(pageConfig.entries().values().stream().map(button -> {
                                    Material material = null;
                                    if(button.item() != null) {
                                        if(button.item().material() != null) {
                                            material = Material.getMaterial(button.item().material());
                                        }
                                    }
                                    ItemType itemType = material != null ? material.asItemType() : null;

                                    return new CategoryConfigV4.ButtonConfig(
                                            ButtonType.getType(button.type()),
                                            button.slot(),
                                            button.shop(),
                                            null,
                                            new ItemStackConfig(
                                                    itemType,
                                                    null,
                                                    null,
                                                    button.item() != null ? button.item().name() : null,
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
                                            null);
                                }).toList())).toList());
            } else {
                if(!version.equals("2.0.0.0")) {
                    logger.warn(AdventureUtility.plain("Failed to migrate the legacy menu.yml configuration due to an unsupported version. Version: " + version));
                    return;
                }

                MenuConfigV2 menuConfigV2 = root.get(MenuConfigV2.class);
                if(menuConfigV2 == null) {
                    logger.warn(AdventureUtility.plain("Failed to migrate the legacy menu.yml configuration due failure to load."));
                    return;
                }

                categoryConfig = new CategoryConfigV4(
                        4,
                        null,
                        menuConfigV2.gui().guiType(),
                        menuConfigV2.gui().name(),
                        menuConfigV2.gui().pages().stream().map(pageConfig ->
                                new CategoryConfigV4.PageConfig(
                                        pageConfig.buttons().stream().map(button ->
                                                        new CategoryConfigV4.ButtonConfig(
                                                                button.buttonType(),
                                                                button.slot(),
                                                                button.shopName(),
                                                                null,
                                                                button.displayItem(),
                                                                null))
                                                .toList()
                                )).toList());
            }

            // Save migrated config
            CommentedConfigurationNode node = categoryLoader.createNode();
            node.set(CategoryConfigV4.class, categoryConfig);
            categoryLoader.save(node);

            // Delete the legacy file.
            legacyPath.toFile().delete();
        } catch (ConfigurateException e) {
            logger.warn(AdventureUtility.plain("Failed to migrate the legacy menu.yml configuration. " + e.getMessage()));
        }
    }

    /**
     * Create the {@link YamlConfigurationLoader} for the path provided.
     * @apiNote {@link PlatformUtils#getSerializers()} are included by default.
     * @param path The {@link Path}.
     * @return The {@link YamlConfigurationLoader}.
     */
    protected @NonNull YamlConfigurationLoader createLoader(@NonNull Path path) {
        return YamlConfigurationLoader.builder()
                .path(path)
                .nodeStyle(NodeStyle.BLOCK)
                .indent(4)
                .defaultOptions(configurationOptions ->
                        configurationOptions.serializers(builder ->
                                builder.registerAll(PlatformUtils.getSerializers())))
                .build();
    }
}