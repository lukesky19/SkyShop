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
package com.github.lukesky19.skyshop.commands.arguments;

import com.github.lukesky19.skylib.common.api.adventure.AdventureUtility;
import com.github.lukesky19.skylib.paper.api.gui.impl.UUIDGUIManager;
import com.github.lukesky19.skyshop.SkyShop;
import com.github.lukesky19.skyshop.api.SkyShopAPI;
import com.github.lukesky19.skyshop.configuration.locale.LocaleManager;
import com.github.lukesky19.skyshop.configuration.locale.data.LocaleV5;
import com.github.lukesky19.skyshop.configuration.sellall.SellAllManager;
import com.github.lukesky19.skyshop.configuration.sellall.data.SellAllGUIConfigV3;
import com.github.lukesky19.skyshop.gui.SellAllGUI;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NonNull;

/**
 * This class is used to create the sellall command used to sell items inside the player's inventory.
 */
public class SellAllCommand {
    private final @NonNull SkyShop skyShop;
    private final @NonNull LocaleManager localeManager;
    private final @NonNull SellAllManager sellAllManager;
    private final @NonNull UUIDGUIManager guiManager;
    private final @NonNull SkyShopAPI skyShopAPI;

    /**
     * Constructor
     * @param skyShop A {@link SkyShop} instance.
     * @param localeManager A {@link LocaleManager} instance.
     * @param guiManager  A {@link UUIDGUIManager} instance.
     * @param sellAllManager A {@link SellAllManager} instance.
     * @param skyShopAPI A {@link SkyShopAPI} instance.
     */
    public SellAllCommand(@NonNull SkyShop skyShop, @NonNull LocaleManager localeManager, @NonNull UUIDGUIManager guiManager, @NonNull SellAllManager sellAllManager, @NonNull SkyShopAPI skyShopAPI) {
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
                    LocaleV5 locale = localeManager.getConfiguration();
                    ComponentLogger logger = skyShop.getComponentLogger();

                    SellAllGUIConfigV3 sellAllConfig = sellAllManager.getConfiguration();
                    if(sellAllConfig == null) {
                        logger.error(AdventureUtility.plain("Unable to open the sell all GUI for player " + player.getName() + " due to a configuration error."));
                        player.sendMessage(AdventureUtility.deserialize(locale.prefix() + locale.guiOpenError()));
                        return 0;
                    }
                    SellAllGUI gui = new SellAllGUI(skyShop, guiManager, sellAllConfig, skyShopAPI, player);

                    boolean creationResult = gui.create();
                    if(!creationResult) {
                        logger.error(AdventureUtility.plain("Unable to create the InventoryView for the sell all GUI for player " + player.getName() + " due to a configuration error."));
                        player.sendMessage(AdventureUtility.deserialize(locale.prefix() + locale.guiOpenError()));
                        return 0;
                    }

                    boolean updateResult = gui.update();
                    if(!updateResult) {
                        logger.error(AdventureUtility.plain("Unable to decorate the sell all GUI for player " + player.getName() + " due to a configuration error."));
                        player.sendMessage(AdventureUtility.deserialize(locale.prefix() + locale.guiOpenError()));
                        return 0;
                    }

                    boolean openResult = gui.open();
                    if(!openResult) {
                        logger.error(AdventureUtility.plain("Unable to open the sell all GUI for player " + player.getName() + " due to a configuration error."));
                        player.sendMessage(AdventureUtility.deserialize(locale.prefix() + locale.guiOpenError()));
                        return 0;
                    }

                    return 1;
                }).build();
    }
}
