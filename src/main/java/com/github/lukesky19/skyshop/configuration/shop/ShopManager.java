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
package com.github.lukesky19.skyshop.configuration.shop;

import com.github.lukesky19.skyshop.SkyShop;
import com.github.lukesky19.skyshop.configuration.ConfigurationUtility;
import com.github.lukesky19.skyshop.configuration.menu.MenuConfiguration;
import com.github.lukesky19.skyshop.configuration.menu.MenuManager;
import com.github.lukesky19.skyshop.configuration.settings.SettingsManager;
import java.io.File;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import com.github.lukesky19.skyshop.util.enums.TransactionType;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;

/**
 * This class manages everything related to handling the plugin's shop files.
*/
public class ShopManager {
    final SkyShop skyShop;
    final SettingsManager settingsManager;
    final MenuManager menuManager;
    final ConfigurationUtility configurationUtility;
    final ShopValidator shopValidator;
    Map<String, ShopConfiguration> shopConfigurations;

    /**
     * Constructor
     * @param skyShop The plugin's instance.
     * @param settingsManager A SettingsManager instance.
     * @param menuManager A MenuManager instance.
     * @param configurationUtility A ConfigurationUtility instance.
     * @param shopValidator A ShopValidator instance.
    */
    public ShopManager(SkyShop skyShop, SettingsManager settingsManager, MenuManager menuManager, ConfigurationUtility configurationUtility, ShopValidator shopValidator) {
        shopConfigurations = new HashMap<>();
        this.skyShop = skyShop;
        this.settingsManager = settingsManager;
        this.menuManager = menuManager;
        this.configurationUtility = configurationUtility;
        this.shopValidator = shopValidator; }

    /**
     * A getter to get a shop configuration by shop id.
      * @param shopId The shop id to get the configuration for.
     * @return A ShopConfiguration object.
     */
    public ShopConfiguration getShopConfig(String shopId) {
        return shopConfigurations.get(shopId);
    }

    /**
     * A method to reload the plugin's shop config.
    */
    public void reload() {
        if(!skyShop.isPluginEnabled()) {
            return;
        }

        if(settingsManager.getSettingsConfig().firstRun()) {
            saveExampleShop();
        }

        shopConfigurations = new HashMap<>();
        for (Map.Entry<String, MenuConfiguration.MenuPage> pageEntry : menuManager.getMenuConfiguration().pages().entrySet()) {
            MenuConfiguration.MenuPage page = pageEntry.getValue();
            for (Map.Entry<String, MenuConfiguration.MenuEntry> itemEntry : page.entries().entrySet()) {
                MenuConfiguration.MenuEntry item = itemEntry.getValue();
                String shopId = item.shop();

                if(TransactionType.valueOf(item.type()).equals(TransactionType.OPEN_SHOP)) {
                    Path path = Path.of(skyShop.getDataFolder() + File.separator + "shops" + File.separator + shopId + ".yml");
                    YamlConfigurationLoader loader = configurationUtility.getYamlConfigurationLoader(path);
                    if(!shopConfigurations.containsKey(shopId)) {
                        ShopConfiguration configuration = null;
                        try {
                            configuration = loader.load().get(ShopConfiguration.class);
                        } catch (ConfigurateException ignored) {}

                        if(!shopValidator.isShopConfigValid(configuration, shopId)) {
                            skyShop.setPluginState(false);
                            return;
                        } else {
                            shopConfigurations.put(shopId, configuration);
                        }
                    }
                }
            }
        }
    }

    /**
     * Saves the example shop file if it does not exist.
     * Only does so if first-run in settings.yml is true.
    */
    public void saveExampleShop() {
        Path path = Path.of(skyShop.getDataFolder() + File.separator + "shops" + File.separator + "example.yml");
        if(!path.toFile().exists()) {
            skyShop.saveResource("shops" + File.separator + "example.yml", false);
            settingsManager.setFirstRunFalse();
        }
    }
}
