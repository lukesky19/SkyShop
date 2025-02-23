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
package com.github.lukesky19.skyshop;

import com.github.lukesky19.skylib.format.FormatUtil;
import com.github.lukesky19.skylib.libs.bstats.bukkit.Metrics;
import com.github.lukesky19.skyshop.commands.SellCommand;
import com.github.lukesky19.skyshop.commands.SkyShopCommand;
import com.github.lukesky19.skyshop.configuration.manager.*;
import com.github.lukesky19.skyshop.gui.GUIManager;
import com.github.lukesky19.skyshop.listener.InventoryListener;
import com.github.lukesky19.skyshop.manager.StatsDatabaseManager;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.SQLException;
import java.util.List;

public final class SkyShop extends JavaPlugin {
    // Class Instances
    private GUIManager guiManager;
    private SettingsManager settingsManager;
    private LocaleManager localeManager;
    private MenuManager menuManager;
    private ShopManager shopManager;
    private TransactionManager transactionManager;
    private SellAllManager sellAllManager;
    private StatsDatabaseManager statsDatabaseManager;

    // The variable that stores the server's economy instance.
    Economy economy;

    /**
     * @return The server's economy.
     */
    public Economy getEconomy() {
        return this.economy;
    }

    /**
     * Startup logic
    */
    @Override
    public void onEnable() {
        // Check the version of SkyLib running on the server.
        boolean skyLibResult = checkSkyLibVersion();
        if(!skyLibResult) return;
        // Check for and set up Vault/Economy.
        boolean economyResult = setupEconomy();
        if(!economyResult) return;

        // Set up bstats.
        setupBStats();

        // Set up all other classes

        this.settingsManager = new SettingsManager(this);
        this.localeManager = new LocaleManager(this, this.settingsManager);
        this.menuManager = new MenuManager(this);
        this.shopManager = new ShopManager(this, this.menuManager);
        transactionManager = new TransactionManager(this, shopManager);
        sellAllManager = new SellAllManager(this);

        if (!getDataFolder().exists()) {
            //noinspection ResultOfMethodCallIgnored
            getDataFolder().mkdirs();
        }

        statsDatabaseManager = new StatsDatabaseManager(getDataFolder().getAbsolutePath() + "/database.db");

        // Register SkyShopAPI
        SkyShopAPI skyShopAPI = new SkyShopAPI(this, settingsManager, localeManager, shopManager, statsDatabaseManager);
        this.getServer().getServicesManager().register(SkyShopAPI.class, skyShopAPI, this, ServicePriority.Lowest);

        guiManager = new GUIManager(this);

        InventoryListener inventoryListener = new InventoryListener(guiManager);

        // Register listeners
        Bukkit.getPluginManager().registerEvents(inventoryListener, this);

        // Register commands
        SkyShopCommand skyShopCommand = new SkyShopCommand(this, menuManager, shopManager, settingsManager, localeManager, transactionManager, sellAllManager, statsDatabaseManager, guiManager, skyShopAPI);
        SellCommand sellCommand = new SellCommand(skyShopAPI);

        this.getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, commands -> {
            commands.registrar().register(skyShopCommand.createCommand(),
                    "Command to manage SkyShop plugin and to access the shop.", List.of("shop"));

            commands.registrar().register(sellCommand.createCommand(),
                    "Command to use the sell command.");
        });

        // Reload the plugin data
        reload();
    }

    @Override
    public void onDisable() {
        guiManager.closeOpenGUIs(true);

        closeDatabase();
    }

    /**
     * Main reload method
    */
    public void reload() {
        guiManager.closeOpenGUIs(false);

        if (!getDataFolder().exists()) {
            //noinspection ResultOfMethodCallIgnored
            getDataFolder().mkdirs();
        }

        statsDatabaseManager.updateConnection(getDataFolder().getAbsolutePath() + "/database.db");

        this.settingsManager.reload();
        this.localeManager.reload();
        this.menuManager.reload();
        this.shopManager.reload();
        this.transactionManager.reload();
        this.sellAllManager.reload();
    }

    /**
     * Checks for Vault as a dependency and sets up the Economy instance.
    */
    private boolean setupEconomy() {
        if(getServer().getPluginManager().getPlugin("Vault") != null) {
            RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
            if (rsp != null) {
                this.economy = rsp.getProvider();

                return true;
            }
        }

        getComponentLogger().error(MiniMessage.miniMessage().deserialize("<red>SkyShop has been disabled due to no Vault dependency found!</red>"));
        this.getServer().getPluginManager().disablePlugin(this);
        return false;
    }

    /**
     * Checks if the Server has the proper SkyLib version.
     * @return true if it does, false if not.
     */
    @SuppressWarnings("UnstableApiUsage")
    private boolean checkSkyLibVersion() {
        PluginManager pluginManager = this.getServer().getPluginManager();
        Plugin skyLib = pluginManager.getPlugin("SkyLib");
        if (skyLib != null) {
            String version = skyLib.getPluginMeta().getVersion();
            String[] splitVersion = version.split("\\.");
            int second = Integer.parseInt(splitVersion[1]);

            if(second >= 2) {
                return true;
            }
        }

        this.getComponentLogger().error(FormatUtil.format("SkyLib Version 1.2.0.0 or newer is required to run this plugin."));
        this.getServer().getPluginManager().disablePlugin(this);
        return false;
    }

    /**
     * Closes the open connection to the stats database.
     */
    private void closeDatabase() {
        try {
            if (statsDatabaseManager != null) {
                statsDatabaseManager.closeConnection();
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Sets up bstats
     */
    private void setupBStats() {
        int pluginId = 22277;
        new Metrics(this, pluginId);
    }
}
