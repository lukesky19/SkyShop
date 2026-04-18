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
package com.github.lukesky19.skyshop.configuration.legacy.shop;

import com.github.lukesky19.skylib.libs.configurate.objectmapping.ConfigSerializable;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Map;

/**
 * The version 1 format of the shop configuration for migration purposes only.
 * Note: The first version did not have a proper config version defined.
 * @param pages The {@link Map} mapping {@link String}s to {@link ShopConfigV1.ShopPage}.
 */
@Deprecated(since = "2.0.0.0")
@ConfigSerializable
public record ShopConfigV1(@NonNull Map<String, ShopPage> pages) {
    /**
     * This record contains the configuration for a page.
     * @param size The GUI size.
     * @param name The gui name.
     * @param entries The {@link Map} mapping {@link String}s to {@link ShopConfigV1.ShopEntry}.
     */
    @ConfigSerializable
    public record ShopPage(
            @Nullable Integer size,
            @Nullable String name,
            @NonNull Map<String, ShopEntry> entries) {}
    /**
     * This record contains the configuration for a button.
     * @param type The button type.
     * @param slot The slot number.
     * @param item The {@link Item} configuration.
     * @param prices The {@link Prices} configuration.
     * @param commands The {@link Commands} configuration.
     */
    @ConfigSerializable
    public record ShopEntry(
            @Nullable String type,
            @Nullable Integer slot,
            @NonNull Item item,
            @NonNull Prices prices,
            @NonNull Commands commands) {}
    /**
     * This record contains the configuration used to create an {@link ItemStack}.
     * @param material The {@link Material} name.
     * @param name The item's name.
     * @param lore The {@link List} of {@link String}s for the item's lore.
     */
    @ConfigSerializable
    public record Item(
            String material,
            String name,
            List<String> lore) {}
    /**
     * This record contains the price config.
     * @param buyPrice The money required to buy.
     * @param sellPrice The money required to sell.
     */
    @ConfigSerializable
    public record Prices(
            Double buyPrice,
            Double sellPrice) {}
    /**
     * This record contains the command configuration.
     * @param buyCommands The commands to buy.
     * @param sellCommands The commands to sell.
     */
    @ConfigSerializable
    public record Commands(
            List<String> buyCommands,
            List<String> sellCommands) {}
}