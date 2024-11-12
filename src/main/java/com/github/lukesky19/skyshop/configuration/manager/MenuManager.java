/*
    SkyShop is a simple inventory based shop plugin with page support, sell commands, and error checking.
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
package com.github.lukesky19.skyshop.configuration.manager;

import com.github.lukesky19.skylib.config.ConfigurationUtility;
import com.github.lukesky19.skylib.format.FormatUtil;
import com.github.lukesky19.skylib.libs.configurate.ConfigurateException;
import com.github.lukesky19.skylib.libs.configurate.yaml.YamlConfigurationLoader;
import com.github.lukesky19.skyshop.SkyShop;
import com.github.lukesky19.skyshop.configuration.record.GUI;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;

import java.io.File;
import java.nio.file.Path;

/**
 * This class manages everything related to handling the plugin's menu.yml file.
 */
public class MenuManager {
    private final SkyShop skyShop;
    private GUI menuConfig;

    /**
     * Constructor
     * @param skyShop The plugin's instance.
    */
    public MenuManager(SkyShop skyShop) {
        this.skyShop = skyShop;
    }

    /**
     * A getter to get the menu configuration.
     * @return A Gui object representing the menu configuration.
     */
    public GUI getMenuConfig() {
        return menuConfig;
    }

    /**
     * A method to reload the plugin's menu config.
     */
    public void reload() {
        menuConfig = null;
        ComponentLogger logger = skyShop.getComponentLogger();

        Path path = Path.of(skyShop.getDataFolder() + File.separator + "menu.yml");

        if(!path.toFile().exists()) {
            skyShop.saveResource("menu.yml", false);
        }

        YamlConfigurationLoader loader = ConfigurationUtility.getYamlConfigurationLoader(path);

        try {
            menuConfig = loader.load().get(GUI.class);
        } catch (ConfigurateException e) {
            logger.error(FormatUtil.format("<red>Unable to load <yellow>menu.yml</yellow> configuration.</red>"));

            throw new RuntimeException(e);
        }
    }
}
