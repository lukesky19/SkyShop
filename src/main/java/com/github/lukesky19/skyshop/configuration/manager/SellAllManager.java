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
import com.github.lukesky19.skylib.libs.configurate.ConfigurateException;
import com.github.lukesky19.skylib.libs.configurate.yaml.YamlConfigurationLoader;
import com.github.lukesky19.skyshop.SkyShop;
import com.github.lukesky19.skyshop.configuration.record.GUI;

import java.io.File;
import java.nio.file.Path;

/**
 * This class manages everything related to handling the plugin's sellall.yml file.
 */
public class SellAllManager {
    final SkyShop skyShop;
    GUI sellAllGuiConfig;

    /**
     * Constructor
     * @param skyShop The plugin's instance.
     */
    public SellAllManager(SkyShop skyShop) {
        this.skyShop = skyShop;
    }

    /**
     * A getter to get the menu configuration.
     * @return A Gui object representing the sellall configuration.
     */
    public GUI getSellAllGuiConfig() {
        return sellAllGuiConfig;
    }

    /**
     * A method to reload the plugin's sellall config.
     */
    public void reload() {
        sellAllGuiConfig = null;

        Path path = Path.of(skyShop.getDataFolder() + File.separator + "sellall.yml");
        if(!path.toFile().exists()) {
            skyShop.saveResource("sellall.yml", false);
        }

        YamlConfigurationLoader loader = ConfigurationUtility.getYamlConfigurationLoader(path);
        try {
            sellAllGuiConfig = loader.load().get(GUI.class);
        } catch (ConfigurateException e) {
            throw new RuntimeException(e);
        }
    }
}
