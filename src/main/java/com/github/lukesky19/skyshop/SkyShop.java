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
import com.github.lukesky19.skylib.api.common.abstracts.SkyPlugin;
import com.github.lukesky19.skylib.api.gui.impl.UUIDGUIListener;
import com.github.lukesky19.skylib.api.gui.impl.UUIDGUIManager;
import com.github.lukesky19.skylib.libs.bstats.bukkit.Metrics;
import com.github.lukesky19.skyshop.api.SkyShopAPI;
import com.github.lukesky19.skyshop.commands.SellCommand;
import com.github.lukesky19.skyshop.commands.SkyShopCommand;
import com.github.lukesky19.skyshop.configuration.category.CategoryConfigManager;
import com.github.lukesky19.skyshop.configuration.category.serializer.CommandConfigurationSerializer;
import com.github.lukesky19.skyshop.configuration.category.serializer.IslandSizeConfigurationSerializer;
import com.github.lukesky19.skyshop.configuration.category.serializer.ItemConfigurationSerializer;
import com.github.lukesky19.skyshop.configuration.legacy.menu.MenuConfigManager;
import com.github.lukesky19.skyshop.configuration.legacy.shop.ShopConfigManager;
import com.github.lukesky19.skyshop.configuration.locale.LocaleManager;
import com.github.lukesky19.skyshop.configuration.sellall.SellAllManager;
import com.github.lukesky19.skyshop.configuration.settings.SettingsManager;
import com.github.lukesky19.skyshop.configuration.settings.data.SettingsV4;
import com.github.lukesky19.skyshop.configuration.transaction.TransactionGUIConfigManager;
import com.github.lukesky19.skyshop.database.ConnectionManager;
import com.github.lukesky19.skyshop.database.DatabaseManager;
import com.github.lukesky19.skyshop.database.QueueManager;
import com.github.lukesky19.skyshop.hook.HookManager;
import com.github.lukesky19.skyshop.listener.PlayerJoinListener;
import com.github.lukesky19.skyshop.listener.PlayerQuitListener;
import com.github.lukesky19.skyshop.player.PlayerDataManager;
import com.github.lukesky19.skyshop.prices.PriceManager;
import com.github.lukesky19.skyshop.registry.RegistryManager;
import com.github.lukesky19.skyshop.stats.StatsManager;
import com.github.lukesky19.skyshop.task.TaskManager;
import com.github.lukesky19.skyshop.transaction.TransactionManager;
import com.github.lukesky19.skyshop.transaction.processor.CommandDataProcessor;
import com.github.lukesky19.skyshop.transaction.processor.IslandSizeProcessor;
import com.github.lukesky19.skyshop.transaction.processor.ItemStackProcessor;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.ServicePriority;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

/**
 * This class is the entry point to the plugin.
 */
public final class SkyShop extends SkyPlugin {
    private SettingsManager settingsManager;
    private LocaleManager localeManager;
    private MenuConfigManager menuConfigManager;
    private ShopConfigManager shopConfigManager;
    private CategoryConfigManager categoryConfigManager;
    private TransactionGUIConfigManager transactionStyleConfigManager;
    private SellAllManager sellAllManager;
    private DatabaseManager databaseManager;
    private PlayerDataManager playerDataManager;
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

        // Setup database related classes classes.
        ConnectionManager connectionManager = new ConnectionManager(this);
        QueueManager queueManager = new QueueManager(connectionManager);
        databaseManager = new DatabaseManager(this, connectionManager, queueManager);

        // Setup PlayerDataManager
        playerDataManager = new PlayerDataManager(this, databaseManager);

        // Create the RegistryManager
        RegistryManager registryManager = new RegistryManager();

        // Setup HookManager / Hooks
        HookManager hookManager = new HookManager(this);

        // Set up configuration manager classes
        settingsManager = new SettingsManager(this);
        localeManager = new LocaleManager(this, this.settingsManager);
        PriceManager priceManager = new PriceManager(this);
        menuConfigManager = new MenuConfigManager(this);
        shopConfigManager = new ShopConfigManager(this, registryManager);
        categoryConfigManager = new CategoryConfigManager(this, settingsManager, priceManager, registryManager);
        transactionStyleConfigManager = new TransactionGUIConfigManager(this);
        sellAllManager = new SellAllManager(this);

        // Register SkyShop serializers and processors
        registryManager.register("skyshop:item", new ItemConfigurationSerializer(), new ItemStackProcessor(this, localeManager, statsManager));
        registryManager.register("skyshop:commands", new CommandConfigurationSerializer(), new CommandDataProcessor(this));
        registryManager.register("skyshop:island_size", new IslandSizeConfigurationSerializer(), new IslandSizeProcessor(settingsManager, localeManager, hookManager));

        // Create the gui manager class
        guiManager = new UUIDGUIManager();

        // Create the transaction manager class
        TransactionManager transactionManager = new TransactionManager(this, localeManager, registryManager, hookManager);

        // Create and register the SkyShopAPI
        SkyShopAPI skyShopAPI = new SkyShopAPI(this, localeManager, priceManager, statsManager, hookManager, registryManager, playerDataManager, transactionManager);
        this.getServer().getServicesManager().register(SkyShopAPI.class, skyShopAPI, this, ServicePriority.Lowest);

        // Register commands
        SkyShopCommand skyShopCommand = new SkyShopCommand(this, guiManager, localeManager, categoryConfigManager, transactionStyleConfigManager, registryManager, sellAllManager, statsManager, hookManager, playerDataManager, transactionManager, skyShopAPI);
        SellCommand sellCommand = new SellCommand(skyShopAPI);
        this.getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, commands -> {
            commands.registrar().register(skyShopCommand.createCommand(),
                    "Command to manage SkyShop plugin and to access the shop.", List.of("shop"));

            commands.registrar().register(sellCommand.createCommand(),
                    "Command to use the sell command.");
        });

        // Register listeners
        PluginManager pluginManager = this.getServer().getPluginManager();
        pluginManager.registerEvents(new PlayerJoinListener(playerDataManager), this);
        pluginManager.registerEvents(new PlayerQuitListener(this, playerDataManager), this);
        pluginManager.registerEvents(new UUIDGUIListener(guiManager), this);

        // Final initialization is delayed until all plugins get the chance to register serializers and processors.
        this.getServer().getScheduler().runTaskLater(this, () -> {
            // Reload the plugin data
            reload();

            // Get the plugin's settings and whether or not statistics should be tracked.
            @Nullable SettingsV4 settings = settingsManager.getConfiguration();
            boolean statistics = Objects.requireNonNullElse(settings != null ? settings.statistics() : null, false);

            // If statistics are to be tracked, setup statistics manager and stats task.
            if(statistics) {
                // Setup the stats manager class.
                statsManager = new StatsManager(this.getComponentLogger(), databaseManager);
                // Loads stats from the database
                statsManager.loadStats();
            }

            // Setup the task manager class.
            taskManager = new TaskManager(this, playerDataManager, guiManager, statsManager);
            taskManager.startTasks();
        }, 1L);

        playerDataManager.loadPlayerData();
    }

    @Override
    public void onDisable() {
        if(guiManager != null) guiManager.closeOpenGUIs(true);

        if(taskManager != null) taskManager.stopTasks();

        ComponentLogger logger = getComponentLogger();
        if(playerDataManager != null) {
            logger.info("Saving player data.");

            CompletableFuture<Void> playerDataFuture = playerDataManager.unloadPlayerData();
            playerDataFuture.join();

            playerDataFuture.thenAccept(v -> {
                logger.info(AdventureUtil.deserialize("Player Data saved."));
            }).exceptionally(ex -> {
                logger.warn(AdventureUtil.deserialize("Failed to save player data on plugin disable. Data loss will occur."));
                return null;
            });
        }

        if(statsManager != null) {
            CompletableFuture<List<Boolean>> statsFuture = statsManager.saveStats();
            statsFuture.join();

            statsFuture.thenAccept(results -> {
                boolean finalResult = !results.contains(false);

                if(finalResult) {
                    databaseManager.handlePluginDisable();
                } else {
                    logger.warn(AdventureUtil.deserialize("Failed to save stats on plugin disable. Data loss will occur."));
                }
            }).exceptionally(ex -> {
                logger.warn(AdventureUtil.deserialize("Failed to save stats on plugin disable. Data loss will occur."));
                return null;
            });
        }

        if(databaseManager != null) databaseManager.handlePluginDisable();
    }

    /**
     * Main reload method
    */
    @Override
    public void reload() {
        guiManager.closeOpenGUIs(false);

        this.settingsManager.loadConfiguration();
        this.localeManager.loadConfiguration();
        this.menuConfigManager.migrate();
        this.shopConfigManager.migrate();
        this.categoryConfigManager.loadConfigurations();
        this.transactionStyleConfigManager.loadConfigurations();
        this.sellAllManager.loadConfiguration();
        this.playerDataManager.loadPlayerData();
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

            if(second >= 5) {
                return true;
            }
        }

        this.getComponentLogger().error(AdventureUtil.deserialize("SkyLib Version 1.5.0.0 or newer is required to run this plugin."));
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