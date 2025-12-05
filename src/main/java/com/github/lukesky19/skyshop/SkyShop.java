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
package com.github.lukesky19.skyshop;

import com.github.lukesky19.skylib.api.adventure.AdventureUtil;
import com.github.lukesky19.skylib.api.gui.impl.UUIDGUIListener;
import com.github.lukesky19.skylib.api.gui.impl.UUIDGUIManager;
import com.github.lukesky19.skylib.libs.bstats.bukkit.Metrics;
import com.github.lukesky19.skyshop.commands.SellCommand;
import com.github.lukesky19.skyshop.commands.SkyShopCommand;
import com.github.lukesky19.skyshop.config.settings.Settings;
import com.github.lukesky19.skyshop.database.ConnectionManager;
import com.github.lukesky19.skyshop.database.DatabaseManager;
import com.github.lukesky19.skyshop.database.QueueManager;
import com.github.lukesky19.skyshop.manager.HookManager;
import com.github.lukesky19.skyshop.manager.PriceManager;
import com.github.lukesky19.skyshop.manager.StatsManager;
import com.github.lukesky19.skyshop.manager.TaskManager;
import com.github.lukesky19.skyshop.manager.config.*;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;

/**
 * This class is the entry point to the plugin.
 */
public final class SkyShop extends JavaPlugin {
    // Class Instances
    private SettingsManager settingsManager;
    private LocaleManager localeManager;
    private MenuConfigManager menuConfigManager;
    private ShopConfigManager shopConfigManager;
    private CategoryConfigManager categoryConfigManager;
    private TransactionManager transactionManager;
    private SellAllManager sellAllManager;
    private DatabaseManager databaseManager;
    private StatsManager statsManager;
    private TaskManager taskManager;
    private UUIDGUIManager guiManager;

    /**
     * Default Constructor.
     */
    public SkyShop() {}

    /**
     * Startup logic
    */
    @Override
    public void onEnable() {
        // Check the version of SkyLib running on the server.
        if(!checkSkyLibVersion()) return;

        // Set up bstats.
        setupBStats();

        // Set up configuration manager classes
        settingsManager = new SettingsManager(this);
        localeManager = new LocaleManager(this, this.settingsManager);
        PriceManager priceManager = new PriceManager(this);
        menuConfigManager = new MenuConfigManager(this);
        shopConfigManager = new ShopConfigManager(this);
        categoryConfigManager = new CategoryConfigManager(this, settingsManager, priceManager);
        transactionManager = new TransactionManager(this);
        sellAllManager = new SellAllManager(this);

        // Setup HookManager / Hooks
        HookManager hookManager = new HookManager(this);

        // Create the gui manager class
        guiManager = new UUIDGUIManager();

        // Register listeners
        this.getServer().getPluginManager().registerEvents(new UUIDGUIListener(guiManager), this);

        // Reload the plugin data
        reload();

        // Get the plugin's settings and whether or not statistics should be tracked.
        @Nullable Settings settings = settingsManager.getSettingsConfig();
        boolean statistics = Objects.requireNonNullElse(settings != null ? settings.statistics() : null, false);

        // If statistics are to be tracked, setup the ConnectionManager, QueueManager, DatabaseManager, StatsManager, TaskManager, and start the save stats task.
        if(statistics) {
            // Setup database related classes classes.
            ConnectionManager connectionManager = new ConnectionManager(this);
            QueueManager queueManager = new QueueManager(connectionManager);
            databaseManager = new DatabaseManager(this, connectionManager, queueManager);

            // Setup the stats manager class.
            statsManager = new StatsManager(this.getComponentLogger(), databaseManager);
            // Loads stats from the database
            statsManager.loadStats();

            // Setup the task manager class.
            taskManager = new TaskManager(this, statsManager);
            // Start the save stats task.
            taskManager.startSaveStatsTask();
        }

        // Create and register the SkyShopAPI
        SkyShopAPI skyShopAPI = new SkyShopAPI(this, localeManager, priceManager, statsManager, hookManager);
        this.getServer().getServicesManager().register(SkyShopAPI.class, skyShopAPI, this, ServicePriority.Lowest);

        // Register commands
        SkyShopCommand skyShopCommand = new SkyShopCommand(this, guiManager, localeManager, categoryConfigManager, transactionManager, sellAllManager, statsManager, hookManager, skyShopAPI);
        SellCommand sellCommand = new SellCommand(skyShopAPI);

        this.getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, commands -> {
            commands.registrar().register(skyShopCommand.createCommand(),
                    "Command to manage SkyShop plugin and to access the shop.", List.of("shop"));

            commands.registrar().register(sellCommand.createCommand(),
                    "Command to use the sell command.");
        });
    }

    @Override
    public void onDisable() {
        if(guiManager != null) guiManager.closeOpenGUIs(true);

        if(taskManager != null) taskManager.stopSaveStatsTask();

        if(statsManager != null) {
            statsManager.saveStats().thenAccept(results -> {
                boolean finalResult = !results.contains(false);

                if (finalResult) {
                    databaseManager.handlePluginDisable();
                } else {
                    this.getComponentLogger().warn(AdventureUtil.deserialize("Failed to save stats on plugin disable. Data loss will occur."));
                    databaseManager.handlePluginDisable();
                }
            }).exceptionally(ex -> {
                this.getComponentLogger().warn(AdventureUtil.deserialize("Failed to save stats on plugin disable. Data loss will occur."));
                databaseManager.handlePluginDisable();
                return null;
            });
        }
    }

    /**
     * Main reload method
    */
    public void reload() {
        guiManager.closeOpenGUIs(false);

        this.settingsManager.reload();
        this.localeManager.reload();
        this.menuConfigManager.migrate();
        this.shopConfigManager.migrate();
        this.categoryConfigManager.reload();
        this.transactionManager.reload();
        this.sellAllManager.reload();
    }

    /**
     * Checks if the Server has the proper SkyLib version.
     * @return true if it does, false if not.
     */
    private boolean checkSkyLibVersion() {
        PluginManager pluginManager = this.getServer().getPluginManager();
        Plugin skyLib = pluginManager.getPlugin("SkyLib");
        if (skyLib != null) {
            String version = skyLib.getPluginMeta().getVersion();
            String[] splitVersion = version.split("\\.");
            int second = Integer.parseInt(splitVersion[1]);

            if(second >= 4) {
                return true;
            }
        }

        this.getComponentLogger().error(AdventureUtil.deserialize("SkyLib Version 1.4.0.0 or newer is required to run this plugin."));
        this.getServer().getPluginManager().disablePlugin(this);
        return false;
    }

    /**
     * Sets up bstats
     */
    private void setupBStats() {
        int pluginId = 22277;
        new Metrics(this, pluginId);
    }
}
