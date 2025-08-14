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
import com.github.lukesky19.skyshop.data.gui.MenuConfig;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.nio.file.Path;
import java.util.Optional;

/**
 * This class manages everything related to handling the plugin's menu.yml file.
 */
public class MenuManager {
    private final @NotNull SkyShop skyShop;
    private @Nullable MenuConfig menuConfig;

    /**
     * Constructor
     * @param skyShop A {@link SkyShop} instance.
    */
    public MenuManager(@NotNull SkyShop skyShop) {
        this.skyShop = skyShop;
    }

    /**
     * Get the {@link MenuConfig}.
     * @return An {@link Optional} containing the {@link MenuConfig}. Will be empty if the config failed to load or is otherwise invalid.
     */
    public Optional<MenuConfig> getMenuConfig() {
        return Optional.ofNullable(menuConfig);
    }

    /**
     * A method to reload the plugin's menu config.
     */
    public void reload() {
        ComponentLogger logger = skyShop.getComponentLogger();

        // Set the current config to null
        menuConfig = null;

        // Create the path to the file.
        Path path = Path.of(skyShop.getDataFolder() + File.separator + "menu.yml");

        // Save the default config if the file doesn't exist.
        if(!path.toFile().exists()) skyShop.saveResource("menu.yml", false);

        // Attempt to load the config.
        YamlConfigurationLoader loader = ConfigurationUtility.getYamlConfigurationLoader(path);
        try {
            menuConfig = loader.load().get(MenuConfig.class);
        } catch (ConfigurateException e) {
            logger.error(AdventureUtil.serialize("Failed to load <yellow>menu.yml</yellow> configuration. " + e.getMessage()));
        }
    }
}
