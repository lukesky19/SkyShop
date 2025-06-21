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
package com.github.lukesky19.skyshop.commands;

import com.github.lukesky19.skyshop.SkyShopAPI;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * This class is used to create the sell command used to view the sell items the player's inventory.
 */
public class SellCommand {
    private final @NotNull SkyShopAPI skyShopAPI;

    /**
     * Constructor
     * @param skyShopAPI A {@link SkyShopAPI} instance.
     */
    public SellCommand(@NotNull SkyShopAPI skyShopAPI) {
        this.skyShopAPI = skyShopAPI;
    }

    /**
     * Builds a {@link LiteralCommandNode} of type {@link CommandSourceStack} for the sell command.
     * @return A {@link LiteralCommandNode} of type {@link CommandSourceStack} representing the sell command.
     */
    public @NotNull LiteralCommandNode<CommandSourceStack> createCommand() {
        LiteralArgumentBuilder<CommandSourceStack> builder = Commands.literal("sell")
            .requires(ctx -> ctx.getSender() instanceof Player && ctx.getSender().hasPermission("skyshop.commands.sell"));

        builder.then(Commands.literal("hand")
            .requires(ctx -> ctx.getSender().hasPermission("skyshop.commands.sell.hand"))
            .executes(ctx -> {
                Player player = (Player) ctx.getSource().getSender();

                ItemStack itemStack = player.getInventory().getItemInMainHand();
                int slot = player.getInventory().getHeldItemSlot();

                if(!itemStack.isEmpty() && !itemStack.getType().equals(Material.AIR)) {
                    skyShopAPI.sellItemStack(player, itemStack, slot, true);

                    return 1;
                }

                return 0;
            })
            .then(Commands.literal("all")
                .requires(ctx -> ctx.getSender().hasPermission("skyshop.commands.sell.hand.all"))
                .executes(ctx -> {
                    Player player = (Player) ctx.getSource().getSender();

                    ItemStack itemStack = player.getInventory().getItemInMainHand();

                    if(!itemStack.isEmpty() && !itemStack.getType().equals(Material.AIR)) {
                        skyShopAPI.sellAllMatchingItemStack(player, itemStack, true);

                        return 1;
                    }

                    return 0;
                })
            )
        );

        builder.then(Commands.literal("all")
            .requires(ctx -> ctx.getSender().hasPermission("skyshop.commands.sell.all"))
            .executes(ctx -> {
                Player player = (Player) ctx.getSource().getSender();

                skyShopAPI.sellPlayerInventory(player, player.getInventory(), true);

                return 1;
            })
        );

        return builder.build();
    }
}
