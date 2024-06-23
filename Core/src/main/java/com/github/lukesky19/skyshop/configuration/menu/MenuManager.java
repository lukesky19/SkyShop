/*
    SkyShop is a simple inventory based shop plugin with page support, error checking, and configuration validation.
    Copyright (C) 2024  lukeskywlker19

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
package com.github.lukesky19.skyshop.configuration.menu;

import com.github.lukesky19.skyshop.SkyShop;
import com.github.lukesky19.skyshop.configuration.ConfigurationUtility;

import java.io.File;
import java.nio.file.Path;

import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;

/**
 * This class manages everything related to handling the plugin's menu.yml file.
 */
public class MenuManager {
    final SkyShop skyShop;
    final MenuValidator menuValidator;
    final ConfigurationUtility configurationUtility;
    MenuConfiguration menuConfiguration;

    /**
     * Constructor
     * @param skyShop The plugin's instance.
     * @param menuValidator A MenuValidator instance.
     * @param configurationUtility A ConfigurationUtility instance.
    */
    public MenuManager(SkyShop skyShop, MenuValidator menuValidator, ConfigurationUtility configurationUtility) {
        this.skyShop = skyShop;
        this.menuValidator = menuValidator;
        this.configurationUtility = configurationUtility;
    }

    /**
     * A getter to get the menu configuration.
     * @return A MenuConfiguration object.
     */
    public MenuConfiguration getMenuConfiguration() {
        return menuConfiguration;
    }

    /**
     * A method to reload the plugin's menu config.
     */
    public void reload() {
        menuConfiguration = null;
        if(!skyShop.isPluginEnabled()) {
            return;
        }

        Path path = Path.of(skyShop.getDataFolder() + File.separator + "menu.yml");

        if(!path.toFile().exists()) {
            skyShop.saveResource("menu.yml", false);
        }

        YamlConfigurationLoader loader = configurationUtility.getYamlConfigurationLoader(path);

        try {
            menuConfiguration = loader.load().get(MenuConfiguration.class);
        } catch (ConfigurateException ignored) {}

        if(!menuValidator.isMenuValid(menuConfiguration)) {
            skyShop.setPluginState(false);
        }
    }
}
