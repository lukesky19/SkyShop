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
import com.github.lukesky19.skyshop.configuration.record.Transaction;

import java.io.File;
import java.nio.file.Path;

/**
 * This class manages everything related to handling the plugin's transaction.yml file.
 */
public class TransactionManager {
    final SkyShop skyShop;
    final ShopManager shopManager;
    Transaction transactionGuiConfig;

    /**
     *
     * @param skyShop The plugin's instance.
     * @param shopManager A ShopManager instance.
     */
    public TransactionManager(
            SkyShop skyShop,
            ShopManager shopManager) {
        this.skyShop = skyShop;
        this.shopManager = shopManager;
    }

    /**
     * A getter to get the menu configuration.
     * @return A Transaction object representing the transaction configuration.
     */
    public Transaction getTransactionGuiConfig() {
        return transactionGuiConfig;
    }

    /**
     * A method to reload the plugin's transaction config.
     */
    public void reload() {
        transactionGuiConfig = null;

        Path path = Path.of(skyShop.getDataFolder() + File.separator + "transaction.yml");
        if(!path.toFile().exists()) {
            skyShop.saveResource("transaction.yml", false);
        }

        YamlConfigurationLoader loader = ConfigurationUtility.getYamlConfigurationLoader(path);
        try {
            transactionGuiConfig = loader.load().get(Transaction.class);
        } catch (ConfigurateException e) {
            throw new RuntimeException(e);
        }
    }
}
