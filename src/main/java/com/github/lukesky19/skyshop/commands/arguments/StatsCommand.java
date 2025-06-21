/*
    SkyShop is a simple inventory based shop plugin with page support, sell commands, and error checking.
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
package com.github.lukesky19.skyshop.commands.arguments;

import com.github.lukesky19.skylib.api.adventure.AdventureUtil;
import com.github.lukesky19.skylib.api.gui.GUIType;
import com.github.lukesky19.skyshop.SkyShop;
import com.github.lukesky19.skyshop.configuration.LocaleManager;
import com.github.lukesky19.skyshop.data.Locale;
import com.github.lukesky19.skyshop.gui.GUIManager;
import com.github.lukesky19.skyshop.gui.StatsGUI;
import com.github.lukesky19.skyshop.manager.StatsManager;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/**
 * This class is used to create the stats command used to view shop stats.
 */
public class StatsCommand {
    private final @NotNull SkyShop skyShop;
    private final @NotNull LocaleManager localeManager;
    private final @NotNull GUIManager guiManager;
    private final @Nullable StatsManager statsManager;

    /**
     * Constructor
     * @param skyShop A {@link SkyShop} instance.
     * @param localeManager A {@link LocaleManager} instance.
     * @param guiManager  A {@link GUIManager} instance.
     * @param statsManager A {@link StatsManager} instance.
     */
    public StatsCommand(@NotNull SkyShop skyShop, @NotNull LocaleManager localeManager, @NotNull GUIManager guiManager, @Nullable StatsManager statsManager) {
        this.skyShop = skyShop;
        this.localeManager = localeManager;
        this.guiManager = guiManager;
        this.statsManager = statsManager;
    }

    /**
     * Creates the {@link LiteralCommandNode} of type {@link CommandSourceStack} for the stats command argument.
     * @return A {@link LiteralCommandNode} of type {@link CommandSourceStack}.
     */
    public LiteralCommandNode<CommandSourceStack> createCommand() {
        return Commands.literal("stats")
            .requires(ctx -> ctx.getSender().hasPermission("skyshop.command.skyshop.stats") && ctx.getSender() instanceof Player)
            .executes(ctx -> {
                Player player = (Player) ctx.getSource().getSender();
                Locale locale = localeManager.getLocale();
                ComponentLogger logger = skyShop.getComponentLogger();

                if(statsManager == null) {
                    player.sendMessage(AdventureUtil.serialize(locale.prefix() + locale.statsDisabledGuiError()));
                    return 0;
                }

                StatsGUI statsGUI = new StatsGUI(skyShop, guiManager, statsManager, player);

                boolean creationResult = statsGUI.create(GUIType.CHEST_54, "<yellow><bold>Transaction Stats</bold></yellow>");
                if(!creationResult) {
                    logger.error(AdventureUtil.serialize("Unable to create the InventoryView for the stats GUI for player " + player.getName() + " due to a configuration error."));
                    player.sendMessage(AdventureUtil.serialize(locale.prefix() + locale.guiOpenError()));
                    return 0;
                }

                // This method is completed sync, the api returns a CompletableFuture for supporting plugins with async requirements.
                @NotNull CompletableFuture<Boolean> updateFuture = statsGUI.update();
                try {
                    boolean updateResult = updateFuture.get();

                    if(!updateResult) {
                        logger.error(AdventureUtil.serialize("Unable to decorate the stats GUI for player " + player.getName() + " due to a configuration error."));
                        player.sendMessage(AdventureUtil.serialize(locale.prefix() + locale.guiOpenError()));
                        return 0;
                    }
                } catch (InterruptedException | ExecutionException e) {
                    logger.error(AdventureUtil.serialize("Unable to decorate the stats GUI for player " + player.getName() + " due to a configuration error. " + e.getMessage()));
                    player.sendMessage(AdventureUtil.serialize(locale.prefix() + locale.guiOpenError()));
                    return 0;
                }

                boolean openResult = statsGUI.open();
                if(!openResult) {
                    logger.error(AdventureUtil.serialize("Unable to open the stats GUI for player " + player.getName() + " due to a configuration error."));
                    player.sendMessage(AdventureUtil.serialize(locale.prefix() + locale.guiOpenError()));
                    return 0;
                }

                return 1;
            }).build();
    }
}
