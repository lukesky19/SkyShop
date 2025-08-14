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
import com.github.lukesky19.skyshop.configuration.LocaleManager;
import com.github.lukesky19.skyshop.data.Locale;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * This class is used to create the help command used to view the help message.
 */
public class HelpCommand {
    private final @NotNull SkyShop skyShop;
    private final @NotNull LocaleManager localeManager;

    /**
     * Constructor
     * @param skyShop A {@link SkyShop} instance.
     * @param localeManager A {@link LocaleManager} instance.
     */
    public HelpCommand(@NotNull SkyShop skyShop, @NotNull LocaleManager localeManager) {
        this.skyShop = skyShop;
        this.localeManager = localeManager;
    }

    /**
     * Creates the {@link LiteralCommandNode} of type {@link CommandSourceStack} for the help command argument.
     * @return A {@link LiteralCommandNode} of type {@link CommandSourceStack}.
     */
    public LiteralCommandNode<CommandSourceStack> createCommand() {
        return Commands.literal("help")
                .requires(ctx -> ctx.getSender().hasPermission("skyshop.commands.skyshop.help"))
                .executes(ctx -> {
                    Locale locale = localeManager.getLocale();

                    if(ctx.getSource().getSender() instanceof Player player) {
                        for(String msg : locale.help()) {
                            player.sendMessage(AdventureUtil.serialize(player, msg));
                        }
                    } else {
                        for(String msg : locale.help()) {
                            skyShop.getComponentLogger().info(AdventureUtil.serialize(msg));
                        }
                    }

                    return 1;
                }).build();
    }
}
