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
package com.github.lukesky19.skyshop;

import com.github.lukesky19.skyshop.commands.SkyShopCommand;
import com.github.lukesky19.skyshop.configuration.ConfigurationUtility;
import com.github.lukesky19.skyshop.configuration.locale.LocaleManager;
import com.github.lukesky19.skyshop.configuration.locale.LocaleValidator;
import com.github.lukesky19.skyshop.configuration.menu.MenuManager;
import com.github.lukesky19.skyshop.configuration.menu.MenuValidator;
import com.github.lukesky19.skyshop.configuration.settings.SettingsManager;
import com.github.lukesky19.skyshop.configuration.settings.SettingsValidator;
import com.github.lukesky19.skyshop.configuration.shop.ShopManager;
import com.github.lukesky19.skyshop.configuration.shop.ShopValidator;
import com.github.lukesky19.skyshop.util.gui.InventoryListener;
import com.github.lukesky19.skyshop.util.gui.InventoryManager;
import me.clip.placeholderapi.metrics.bukkit.Metrics;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;

public final class SkyShop extends JavaPlugin {
    // Class Instances
    InventoryManager inventoryManager;
    SettingsManager settingsManager;
    LocaleManager localeManager;
    MenuManager menuManager;
    ShopManager shopManager;
    // The variable that stores the server's economy instance.
    Economy economy;
    // This variable relates to the status of SkyShop, i.e., whether a configuration has failed to load or not.
    Boolean pluginState = true;

    /**
     * @return The server's economy.
     */
    public Economy getEconomy() {
        return this.economy;
    }

    public void setPluginState(Boolean pluginState) {
        this.pluginState = pluginState;
    }

    public Boolean isPluginEnabled() {
        return this.pluginState;
    }

    /**
     * Startup logic
    */
    public void onEnable() {
        int pluginId = 22277;
        new Metrics(this, pluginId);

        setupEconomy();
        setupPlaceholderAPI();

        ConfigurationUtility configurationUtility = new ConfigurationUtility(this);
        this.inventoryManager = new InventoryManager(this);
        InventoryListener inventoryListener = new InventoryListener(this.inventoryManager);
        SettingsValidator settingsValidator = new SettingsValidator(this);
        this.settingsManager = new SettingsManager(this, settingsValidator, configurationUtility);
        LocaleValidator localeValidator = new LocaleValidator(this);
        this.localeManager = new LocaleManager(this, configurationUtility, this.settingsManager, localeValidator);
        MenuValidator menuValidator = new MenuValidator(this, configurationUtility);
        this.menuManager = new MenuManager(this, menuValidator, configurationUtility);
        ShopValidator shopValidator = new ShopValidator(this);
        this.shopManager = new ShopManager(this, this.settingsManager, this.menuManager, configurationUtility, shopValidator);
        SkyShopCommand skyShopCommand = new SkyShopCommand(this, this.menuManager, this.shopManager, this.localeManager, this.inventoryManager);

        Bukkit.getPluginManager().registerEvents(inventoryListener, this);

        Objects.requireNonNull(Bukkit.getPluginCommand("skyshop")).setExecutor(skyShopCommand);
        Objects.requireNonNull(Bukkit.getPluginCommand("skyshop")).setTabCompleter(skyShopCommand);
        Objects.requireNonNull(Bukkit.getPluginCommand("shop")).setExecutor(skyShopCommand);
        Objects.requireNonNull(Bukkit.getPluginCommand("shop")).setTabCompleter(skyShopCommand);

        reload();
    }

    /**
     * Main reload method
    */
    public void reload() {
        pluginState = true;
        this.inventoryManager.clearRegisteredInventories();
        this.settingsManager.reload();
        this.localeManager.reload();
        this.menuManager.reload();
        this.shopManager.reload();
    }

    /**
     * Checks for Vault as a dependency and sets up the Economy instance.
    */
    private void setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") != null) {
            RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
            if (rsp != null) {
                this.economy = rsp.getProvider();
            }
        } else {
            getComponentLogger().error("<red>SkyShop has been disabled due to no Vault dependency found!</red>");
            getServer().getPluginManager().disablePlugin(this);
        }
    }

    /**
     * Checks for PlaceholderAPI as a dependency.
    */
    private void setupPlaceholderAPI() {
        if (getServer().getPluginManager().getPlugin("PlaceholderAPI") == null) {
            getComponentLogger().error("<red>SkyShop has been disabled due to no PlaceholderAPI dependency found!</red>");
            getServer().getPluginManager().disablePlugin(this);
        }
    }
}
