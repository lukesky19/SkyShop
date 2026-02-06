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
package com.github.lukesky19.skyshop.transaction.processor;

import com.github.lukesky19.skylib.api.adventure.AdventureUtil;
import com.github.lukesky19.skyshop.api.configuration.TransactionConfiguration;
import com.github.lukesky19.skyshop.api.processor.TransactionProcessor;
import com.github.lukesky19.skyshop.api.result.TransactionResult;
import com.github.lukesky19.skyshop.configuration.category.transaction.IslandSizeConfiguration;
import com.github.lukesky19.skyshop.configuration.locale.LocaleManager;
import com.github.lukesky19.skyshop.configuration.locale.data.LocaleV5;
import com.github.lukesky19.skyshop.configuration.settings.SettingsManager;
import com.github.lukesky19.skyshop.configuration.settings.data.SettingsV4;
import com.github.lukesky19.skyshop.hook.HookManager;
import com.github.lukesky19.skyshop.hook.impl.BentoBoxHook;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import world.bentobox.bentobox.database.objects.Island;

/**
 * This class processes {@link IslandSizeConfiguration} for buying/selling island size.
 */
public class IslandSizeProcessor implements TransactionProcessor {
    private final @NotNull SettingsManager settingsManager;
    private final @NotNull LocaleManager localeManager;
    private final @NotNull HookManager hookManager;

    /**
     * Constructor
     * @param settingsManager A {@link SettingsManager} instance.
     * @param localeManager A {@link LocaleManager} instance.
     * @param hookManager A {@link HookManager} instance.
     */
    public IslandSizeProcessor(
            @NotNull SettingsManager settingsManager,
            @NotNull LocaleManager localeManager,
            @NotNull HookManager hookManager) {
        this.settingsManager = settingsManager;
        this.localeManager = localeManager;
        this.hookManager = hookManager;
    }

    /**
     * Can the player buy the commands with no errors?
     * @apiNote Prices are already checked.
     * @param player The {@link Player} buying the commands(s).
     * @param configuration The configuration to process.
     * @param amount The amount being purchased.
     * @return A {@link TransactionResult}.
     */
    @Override
    public @NotNull TransactionResult canBuy(@NotNull Player player, @NotNull TransactionConfiguration configuration, int amount) {
        if(!(configuration instanceof IslandSizeConfiguration islandSizeConfiguration)) return new TransactionResult("Wrong Type", true, true, false);
        if(islandSizeConfiguration.buyAmount() == null || islandSizeConfiguration.buyAmount() <= 0) return new TransactionResult("Not Configured", false, false, false);

        @Nullable SettingsV4 settings = settingsManager.getConfiguration();
        if(settings == null || settings.islandSizeLimit() == null) return new TransactionResult("Invalid SkyShop plugin settings", true, true, false);

        BentoBoxHook bentoBoxHook = hookManager.getHook(BentoBoxHook.class);
        if(!bentoBoxHook.isHooked()) return new TransactionResult("BentoBox is not hooked into", true, true, false);

        LocaleV5 locale = localeManager.getConfiguration();

        // Get and validate the island
        @Nullable Island island = bentoBoxHook.getIsland(player);
        if(island == null) {
            player.sendMessage(AdventureUtil.deserialize(locale.prefix() + locale.islandSizeMessages().notOnIsland()));
            return new TransactionResult("Player not on island", true, false, false);
        }

        // Check if the island is at the max configured size
        if(island.getProtectionRange() >= settings.islandSizeLimit()) {
            player.sendMessage(AdventureUtil.deserialize(locale.prefix() + locale.islandSizeMessages().islandMaxSize()));
            return new TransactionResult("Island at max size", true, false, false);
        }

        return new TransactionResult("Success", false, false, false);
    }

    /**
     * Can the player sell the commands with no errors?
     * @param player The {@link Player} selling the commands(s).
     * @param configuration The configuration to process.
     * @param amount The amount being sold.
     * @return A {@link TransactionResult}.
     */
    @Override
    public @NotNull TransactionResult canSell(@NotNull Player player, @NotNull TransactionConfiguration configuration, int amount) {
        if(!(configuration instanceof IslandSizeConfiguration islandSizeConfiguration)) return new TransactionResult("Wrong Type", true, true, false);
        if(islandSizeConfiguration.sellAmount() == null || islandSizeConfiguration.sellAmount() <= 0) return new TransactionResult("Not Configured", false, false, false);

        @Nullable SettingsV4 settings = settingsManager.getConfiguration();
        if(settings == null || settings.islandSizeLimit() == null) return new TransactionResult("Invalid SkyShop plugin settings", true, true, false);

        BentoBoxHook bentoBoxHook = hookManager.getHook(BentoBoxHook.class);
        if(!bentoBoxHook.isHooked()) return new TransactionResult("BentoBox is not hooked into", true, true, false);

        LocaleV5 locale = localeManager.getConfiguration();

        // Get and validate the island
        @Nullable Island island = bentoBoxHook.getIsland(player);
        if(island == null) {
            player.sendMessage(AdventureUtil.deserialize(locale.prefix() + locale.islandSizeMessages().notOnIsland()));
            return new TransactionResult("Player not on island", true, false, false);
        }

        // Validate that the player's island is large enough
        if(island.getProtectionRange() <= islandSizeConfiguration.sellAmount()) {
            player.sendMessage(AdventureUtil.deserialize(locale.prefix() + locale.islandSizeMessages().islandTooSmall()));
            return new TransactionResult("Island too small", true, false, false);
        }

        return new TransactionResult("Success", false, false, false);
    }

    /**
     * Process the configuration to buy the commands(s).
     * @apiNote Prices are automatically taken from the player as necessary.<br>
     * If the buying fails, the rest of the transaction will still proceed.
     * @param player The {@link Player} to process configuration for.
     * @param configuration The configuration to process.
     * @param amount The amount being purchased.
     * @return A {@link TransactionResult}.
     */
    @Override
    public @NotNull TransactionResult buy(@NotNull Player player, @NotNull TransactionConfiguration configuration, int amount) {
        if(!(configuration instanceof IslandSizeConfiguration islandSizeConfiguration)) return new TransactionResult("Wrong Type", true, true, false);
        if(islandSizeConfiguration.buyAmount() == null || islandSizeConfiguration.buyAmount() <= 0) return new TransactionResult("Not Configured", false, false, false);

        @Nullable SettingsV4 settings = settingsManager.getConfiguration();
        if(settings == null || settings.islandSizeLimit() == null) return new TransactionResult("Invalid SkyShop plugin settings", true, true, false);

        BentoBoxHook bentoBoxHook = hookManager.getHook(BentoBoxHook.class);
        if(!bentoBoxHook.isHooked()) return new TransactionResult("BentoBox is not hooked into", true, true, false);

        LocaleV5 locale = localeManager.getConfiguration();

        // Get and validate the island
        @Nullable Island island = bentoBoxHook.getIsland(player);
        if(island == null) {
            player.sendMessage(AdventureUtil.deserialize(locale.prefix() + locale.islandSizeMessages().notOnIsland()));
            return new TransactionResult("Player not on island", true, false, false);
        }

        // Check if the island is at the max configured size
        if(island.getProtectionRange() >= settings.islandSizeLimit()) {
            player.sendMessage(AdventureUtil.deserialize(locale.prefix() + locale.islandSizeMessages().islandMaxSize()));
            return new TransactionResult("Island at max size", true, false, false);
        }

        // Modify the island size
        if(islandSizeConfiguration.setIslandSize()) {
            bentoBoxHook.setIslandSize(player.getUniqueId(), island, islandSizeConfiguration.buyAmount(), settings.islandSizeLimit());
        } else {
            bentoBoxHook.addIslandSize(player.getUniqueId(), island, islandSizeConfiguration.buyAmount(), settings.islandSizeLimit());
        }

        return new TransactionResult("Success", false, false, false);
    }

    /**
     * Process the configuration to sell the commands(s).
     * @apiNote Prices are automatically added to the player as necessary.<br>
     * If the selling fails, the rest of the transaction will still proceed.
     * @param player The {@link Player} to process configuration for.
     * @param configuration The configuration to process.
     * @param amount The amount being sold.
     * @return A {@link TransactionResult}.
     */
    @Override
    public @NotNull TransactionResult sell(@NotNull Player player, @NotNull TransactionConfiguration configuration, int amount) {
        if(!(configuration instanceof IslandSizeConfiguration islandSizeConfiguration)) return new TransactionResult("Wrong Type", true, true, false);
        if(islandSizeConfiguration.sellAmount() == null || islandSizeConfiguration.sellAmount() <= 0) return new TransactionResult("Not Configured", false, false, false);

        @Nullable SettingsV4 settings = settingsManager.getConfiguration();
        if(settings == null || settings.islandSizeLimit() == null) return new TransactionResult("Invalid SkyShop plugin settings", true, true, false);

        BentoBoxHook bentoBoxHook = hookManager.getHook(BentoBoxHook.class);
        if(!bentoBoxHook.isHooked()) return new TransactionResult("BentoBox is not hooked into", true, true, false);

        LocaleV5 locale = localeManager.getConfiguration();

        // Get and validate the island
        @Nullable Island island = bentoBoxHook.getIsland(player);
        if(island == null) {
            player.sendMessage(AdventureUtil.deserialize(locale.prefix() + locale.islandSizeMessages().notOnIsland()));
            return new TransactionResult("Player not on island", true, false, false);
        }

        // Validate that the player's island is large enough
        if(island.getProtectionRange() <= islandSizeConfiguration.sellAmount()) {
            player.sendMessage(AdventureUtil.deserialize(locale.prefix() + locale.islandSizeMessages().islandTooSmall()));
            return new TransactionResult("Island too small", true, false, false);
        }

        // Modify the island size
        if(islandSizeConfiguration.setIslandSize()) {
            bentoBoxHook.setIslandSize(player.getUniqueId(), island, islandSizeConfiguration.sellAmount(), settings.islandSizeLimit());
        } else {
            bentoBoxHook.removeIslandSize(player.getUniqueId(), island, islandSizeConfiguration.sellAmount());
        }

        return new TransactionResult("Success", false, false, false);
    }
}