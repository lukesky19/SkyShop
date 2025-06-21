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
import com.github.lukesky19.skyshop.SkyShop;
import com.github.lukesky19.skyshop.SkyShopAPI;
import com.github.lukesky19.skyshop.configuration.LocaleManager;
import com.github.lukesky19.skyshop.configuration.SellAllManager;
import com.github.lukesky19.skyshop.data.Locale;
import com.github.lukesky19.skyshop.data.gui.SellAllConfig;
import com.github.lukesky19.skyshop.gui.GUIManager;
import com.github.lukesky19.skyshop.gui.SellAllGUI;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/**
 * This class is used to create the sellall command used to sell items inside the player's inventory.
 */
public class SellAllCommand {
    private final @NotNull SkyShop skyShop;
    private final @NotNull LocaleManager localeManager;
    private final @NotNull SellAllManager sellAllManager;
    private final @NotNull GUIManager guiManager;
    private final @NotNull SkyShopAPI skyShopAPI;

    /**
     * Constructor
     * @param skyShop A {@link SkyShop} instance.
     * @param localeManager A {@link LocaleManager} instance.
     * @param guiManager  A {@link GUIManager} instance.
     * @param sellAllManager A {@link SellAllManager} instance.
     * @param skyShopAPI A {@link SkyShopAPI} instance.
     */
    public SellAllCommand(@NotNull SkyShop skyShop, @NotNull LocaleManager localeManager, @NotNull GUIManager guiManager, @NotNull SellAllManager sellAllManager, @NotNull SkyShopAPI skyShopAPI) {
        this.skyShop = skyShop;
        this.localeManager = localeManager;
        this.guiManager = guiManager;
        this.sellAllManager = sellAllManager;
        this.skyShopAPI = skyShopAPI;
    }

    /**
     * Creates the {@link LiteralCommandNode} of type {@link CommandSourceStack} for the sellall command argument.
     * @return A {@link LiteralCommandNode} of type {@link CommandSourceStack}.
     */
    public LiteralCommandNode<CommandSourceStack> createCommand() {
        return Commands.literal("sellall")
                .requires(ctx -> ctx.getSender().hasPermission("skyshop.commands.skyshop.sellall") && ctx.getSender() instanceof Player)
                .executes(ctx -> {
                    Player player = (Player) ctx.getSource().getSender();
                    Locale locale = localeManager.getLocale();
                    ComponentLogger logger = skyShop.getComponentLogger();

                    @NotNull Optional<@NotNull SellAllConfig> optionalSellAllConfig = sellAllManager.getSellAllGuiConfig();
                    if(optionalSellAllConfig.isEmpty()) {
                        logger.error(AdventureUtil.serialize("Unable to open the sell all GUI for player " + player.getName() + " due to a configuration error."));
                        player.sendMessage(AdventureUtil.serialize(locale.prefix() + locale.guiOpenError()));
                        return 0;
                    }

                    SellAllConfig sellAllConfig  = optionalSellAllConfig.get();
                    SellAllGUI gui = new SellAllGUI(skyShop, guiManager, sellAllConfig, skyShopAPI, player);

                    boolean creationResult = gui.create();
                    if(!creationResult) {
                        logger.error(AdventureUtil.serialize("Unable to create the InventoryView for the sell all GUI for player " + player.getName() + " due to a configuration error."));
                        player.sendMessage(AdventureUtil.serialize(locale.prefix() + locale.guiOpenError()));
                        return 0;
                    }

                    // This method is completed sync, the api returns a CompletableFuture for supporting plugins with async requirements.
                    @NotNull CompletableFuture<Boolean> updateFuture = gui.update();
                    try {
                        boolean updateResult = updateFuture.get();

                        if(!updateResult) {
                            logger.error(AdventureUtil.serialize("Unable to decorate the sell all GUI for player " + player.getName() + " due to a configuration error."));
                            player.sendMessage(AdventureUtil.serialize(locale.prefix() + locale.guiOpenError()));
                            return 0;
                        }
                    } catch (InterruptedException | ExecutionException e) {
                        skyShop.getComponentLogger().error(AdventureUtil.serialize("Failed to update the sell all gui: " + e.getMessage()));
                        return 0;
                    }

                    boolean openResult = gui.open();
                    if(!openResult) {
                        logger.error(AdventureUtil.serialize("Unable to open the sell all GUI for player " + player.getName() + " due to a configuration error."));
                        player.sendMessage(AdventureUtil.serialize(locale.prefix() + locale.guiOpenError()));
                        return 0;
                    }

                    return 1;
                }).build();
    }
}
