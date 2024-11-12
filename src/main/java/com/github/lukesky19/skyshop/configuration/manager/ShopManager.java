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
import com.github.lukesky19.skyshop.enums.ActionType;
import org.bukkit.Material;

import javax.annotation.CheckForNull;
import java.io.File;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

/**
 * This class manages everything related to handling the plugin's shop files.
*/
public class ShopManager {
    private final SkyShop skyShop;
    private final MenuManager menuManager;
    private final Map<String, GUI> shopConfigurations = new HashMap<>();
    private final HashMap<Material, Double> sellPrices = new HashMap<>();

    /**
     * Constructor
     * @param skyShop The plugin's instance.
     * @param menuManager A MenuManager instance.
    */
    public ShopManager(SkyShop skyShop, MenuManager menuManager) {
        this.skyShop = skyShop;
        this.menuManager = menuManager;
    }

    /**
     * A getter to get a shop configuration by shop id.
     *
     * @param shopId The shop id to get the configuration for.
     * @return A ShopConfiguration object.
     */
    public GUI getShopConfig(String shopId) {
        return shopConfigurations.get(shopId);
    }

    /**
     * Get the configured sell price of a given Material/
     * @param material A Bukkit Material
     * @return The sell price for the Material or null if it does not have a sell price (can't be sold)
     */
    @CheckForNull
    public Double getMaterialSellPrice(Material material) {
        return sellPrices.get(material);
    }

    /**
     * A method to reload the plugin's shop config files.
    */
    public void reload() {
        shopConfigurations.clear();

        for (Map.Entry<Integer, GUI.Page> pageEntry : menuManager.getMenuConfig().gui().pages().entrySet()) {
            GUI.Page page = pageEntry.getValue();
            for (Map.Entry<Integer, GUI.Entry> itemEntry : page.entries().entrySet()) {
                GUI.Entry item = itemEntry.getValue();
                String shopId = item.shop();
                ActionType actionType = ActionType.getActionType(item.type());

                if(actionType != null && actionType.equals(ActionType.OPEN_SHOP)) {
                    Path path = Path.of(skyShop.getDataFolder() + File.separator + "shops" + File.separator + shopId + ".yml");
                    YamlConfigurationLoader loader = ConfigurationUtility.getYamlConfigurationLoader(path);
                    if(!shopConfigurations.containsKey(shopId)) {
                        try {
                            GUI configuration = loader.load().get(GUI.class);
                            if(configuration != null) {
                                shopConfigurations.put(shopId, configuration);
                                cacheSellPrices(configuration);
                            }
                        } catch (ConfigurateException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
            }
        }
    }

    /**
     * Stores the sell prices of all configured materials from a shop config.
     * @param shopConfiguration A GUI object representing a shop config.
     */
    private void cacheSellPrices(GUI shopConfiguration) {
        for(Map.Entry<Integer, GUI.Page> shopPageEntry : shopConfiguration.gui().pages().entrySet()) {
            for(Map.Entry<Integer, GUI.Entry> shopItemEntry : shopPageEntry.getValue().entries().entrySet()) {
                GUI.Entry entry = shopItemEntry.getValue();
                ActionType type = ActionType.valueOf(entry.type());
                if(type.equals(ActionType.ITEM)) {
                    Material material = Material.getMaterial(shopItemEntry.getValue().item().material());
                    Double sellPrice = shopItemEntry.getValue().prices().sellPrice();
                    if(material != null) {
                        sellPrices.put(material, sellPrice);
                    }
                }
            }
        }
    }
}
