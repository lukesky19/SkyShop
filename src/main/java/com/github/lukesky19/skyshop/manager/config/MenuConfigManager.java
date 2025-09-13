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
import com.github.lukesky19.skylib.libs.configurate.CommentedConfigurationNode;
import com.github.lukesky19.skylib.libs.configurate.ConfigurateException;
import com.github.lukesky19.skylib.libs.configurate.yaml.YamlConfigurationLoader;
import com.github.lukesky19.skyshop.SkyShop;
import com.github.lukesky19.skyshop.config.gui.CategoryConfig;
import com.github.lukesky19.skyshop.config.gui.legacy.MenuConfig;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * This class manages the migration of the legacy menu config file.
 */
public class MenuConfigManager {
    private final @NotNull SkyShop skyShop;

    /**
     * Constructor
     * @param skyShop A {@link SkyShop} instance.
    */
    public MenuConfigManager(@NotNull SkyShop skyShop) {
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
        YamlConfigurationLoader legacyLoader = ConfigurationUtility.getYamlConfigurationLoader(legacyPath);
        YamlConfigurationLoader categoryLoader = ConfigurationUtility.getYamlConfigurationLoader(categoryPath);
        try {
            @Nullable MenuConfig menuConfig = legacyLoader.load().get(MenuConfig.class);
            if(menuConfig == null) return;

            // Create the new config from the legacy config
            CategoryConfig categoryConfig = createCategoryConfig(menuConfig);

            // Save migrated config
            CommentedConfigurationNode node = categoryLoader.createNode();
            node.set(CategoryConfig.class, categoryConfig);
            categoryLoader.save(node);

            // Delete the legacy file.
            legacyPath.toFile().delete();
        } catch (ConfigurateException e) {
            logger.error(AdventureUtil.serialize("Failed to migrate the legacy <yellow>menu.yml</yellow> configuration. " + e.getMessage()));
        }
    }

    /**
     * Create the {@link CategoryConfig} from the {@link MenuConfig}.
     * @param menuConfig The legacy {@link MenuConfig}.
     * @return The created {@link CategoryConfig}.
     */
    private @NotNull CategoryConfig createCategoryConfig(@NotNull MenuConfig menuConfig) {
        List<CategoryConfig.PageConfig> pageConfigList = new ArrayList<>();

        for(MenuConfig.PageConfig legacyPageConfig : menuConfig.gui().pages()) {
            CategoryConfig.PageConfig categoryPageConfig = getCategoryPageConfig(legacyPageConfig);

            pageConfigList.add(categoryPageConfig);
        }

        CategoryConfig.GuiData guiData = new CategoryConfig.GuiData(
                menuConfig.gui().guiType(),
                menuConfig.gui().name(),
                pageConfigList);

        return new CategoryConfig("2.1.0.0", guiData);
    }

    /**
     * Create the {@link CategoryConfig.PageConfig} from the {@link MenuConfig.PageConfig}.
     * @param legacyPageConfig The legacy {@link MenuConfig.PageConfig}.
     * @return The created {@link CategoryConfig.PageConfig}.
     */
    private @NotNull CategoryConfig.PageConfig getCategoryPageConfig(@NotNull MenuConfig.PageConfig legacyPageConfig) {
        List<CategoryConfig.ButtonConfig> buttonConfigList = new ArrayList<>();

        for(MenuConfig.Button legacyButtonConfig : legacyPageConfig.buttons()) {
            CategoryConfig.ButtonConfig categoryButtonConfig = new CategoryConfig.ButtonConfig(
                    legacyButtonConfig.buttonType(),
                    legacyButtonConfig.slot(),
                    legacyButtonConfig.shopName(),
                    legacyButtonConfig.displayItem(),
                    null);

            buttonConfigList.add(categoryButtonConfig);
        }

        return new CategoryConfig.PageConfig(buttonConfigList);
    }
}
