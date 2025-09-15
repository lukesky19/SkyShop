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

import com.github.lukesky19.skylib.api.adventure.AdventureUtil;
import com.github.lukesky19.skyshop.SkyShop;
import com.github.lukesky19.skyshop.SkyShopAPI;
import com.github.lukesky19.skyshop.config.gui.CategoryConfig;
import com.github.lukesky19.skyshop.config.locale.Locale;
import com.github.lukesky19.skyshop.gui.CategoryGUI;
import com.github.lukesky19.skyshop.manager.GUIManager;
import com.github.lukesky19.skyshop.manager.HookManager;
import com.github.lukesky19.skyshop.manager.StatsManager;
import com.github.lukesky19.skyshop.manager.config.CategoryConfigManager;
import com.github.lukesky19.skyshop.manager.config.LocaleManager;
import com.github.lukesky19.skyshop.manager.config.SellAllManager;
import com.github.lukesky19.skyshop.manager.config.TransactionManager;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

/**
 * This class is used to create the open command used to open specific shop categories.
 */
public class OpenCommand {
    private final @NotNull SkyShop skyShop;
    private final @NotNull LocaleManager localeManager;
    private final @NotNull CategoryConfigManager categoryConfigManager;
    private final @NotNull TransactionManager transactionManager;
    private final @NotNull SellAllManager sellAllManager;
    private final @Nullable StatsManager statsManager;
    private final @NotNull HookManager hookManager;
    private final @NotNull GUIManager guiManager;
    private final @NotNull SkyShopAPI skyShopAPI;

    /**
     * Constructor
     * @param skyShop A {@link SkyShop} instance
     * @param guiManager A {@link GUIManager} instance.
     * @param localeManager A {@link LocaleManager} instance.
     * @param categoryConfigManager A {@link CategoryConfigManager} instance.
     * @param transactionManager A {@link TransactionManager} instance.
     * @param sellAllManager A {@link SellAllManager} instance.
     * @param statsManager A {@link StatsManager} instance.
     * @param hookManager A {@link HookManager} instance.
     * @param skyShopAPI A {@link SkyShopAPI} instance.
     */
    public OpenCommand(
            @NotNull SkyShop skyShop,
            @NotNull GUIManager guiManager,
            @NotNull LocaleManager localeManager,
            @NotNull CategoryConfigManager categoryConfigManager,
            @NotNull TransactionManager transactionManager,
            @NotNull SellAllManager sellAllManager,
            @Nullable StatsManager statsManager,
            @NotNull HookManager hookManager,
            @NotNull SkyShopAPI skyShopAPI) {
        this.skyShop = skyShop;
        this.localeManager = localeManager;
        this.categoryConfigManager = categoryConfigManager;
        this.transactionManager = transactionManager;
        this.sellAllManager = sellAllManager;
        this.statsManager = statsManager;
        this.guiManager = guiManager;
        this.hookManager = hookManager;
        this.skyShopAPI = skyShopAPI;
    }

    /**
     * Builds a {@link LiteralCommandNode} of type {@link CommandSourceStack} for the open command argument.
     * @return A {@link LiteralCommandNode} of type {@link CommandSourceStack} representing the open command argument.
     */
    public @NotNull LiteralCommandNode<CommandSourceStack> createCommand() {
        return Commands.literal("open")
                .requires(ctx -> ctx.getSender().hasPermission("skyshop.commands.skyshop.open") && ctx.getSender() instanceof Player)
                .then(Commands.argument("category", StringArgumentType.word())
                    .suggests((context, suggestionsProvider) -> {
                        categoryConfigManager.getCategoryIds().forEach(suggestionsProvider::suggest);
                        return suggestionsProvider.buildFuture();
                    })
                    .executes(ctx -> {
                        ComponentLogger logger = skyShop.getComponentLogger();
                        Locale locale = localeManager.getLocale();
                        Player player = (Player) ctx.getSource().getSender();
                        String categoryId = ctx.getArgument("category", String.class);

                        Optional<CategoryConfig> optionalCategoryConfig = categoryConfigManager.getCategoryConfig(categoryId);
                        if(optionalCategoryConfig.isEmpty()) {
                            logger.error(AdventureUtil.serialize("Unable to open the category GUI for the category id " + categoryId + " for player " + player.getName() + " due to a configuration error."));
                            player.sendMessage(AdventureUtil.serialize(locale.prefix() + locale.guiOpenError()));
                            return 0;
                        }
                        CategoryConfig categoryConfig = optionalCategoryConfig.get();

                        CategoryGUI categoryGUI = new CategoryGUI(skyShop, guiManager, player, localeManager, categoryConfigManager, transactionManager, sellAllManager, statsManager, hookManager, skyShopAPI, null, categoryConfig, categoryId);

                        boolean creationResult = categoryGUI.create();
                        if(!creationResult) {
                            logger.error(AdventureUtil.serialize("Unable to create the InventoryView for the category GUI for the category id " + categoryId + " for player " + player.getName() + " due to a configuration error."));
                            player.sendMessage(AdventureUtil.serialize(locale.prefix() + locale.guiOpenError()));
                            return 0;
                        }

                        boolean updateResult = categoryGUI.update();
                        if(!updateResult) {
                            logger.error(AdventureUtil.serialize("Unable to decorate the category GUI for the category id " + categoryId + " for player " + player.getName() + " due to a configuration error."));
                            player.sendMessage(AdventureUtil.serialize(locale.prefix() + locale.guiOpenError()));
                            return 0;
                        }

                        boolean openResult = categoryGUI.open();
                        if(!openResult) {
                            logger.error(AdventureUtil.serialize("Unable to open the category GUI for the category id " + categoryId + " for player " + player.getName() + " due to a configuration error."));
                            player.sendMessage(AdventureUtil.serialize(locale.prefix() + locale.guiOpenError()));
                            return 0;
                        }

                        return 1;
                    })).build();
    }
}
