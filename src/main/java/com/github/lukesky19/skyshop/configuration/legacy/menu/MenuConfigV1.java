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
package com.github.lukesky19.skyshop.configuration.legacy.menu;

import com.github.lukesky19.skylib.libs.configurate.objectmapping.ConfigSerializable;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

/**
 * The version 1 format of the menu configuration for migration purposes only.
 * Note: The first version did not have a proper config version defined.
 * @param pages The {@link Map} mapping {@link String}s to {@link MenuPage}.
 */
@Deprecated(since = "2.0.0.0")
@ConfigSerializable
public record MenuConfigV1(@NotNull Map<String, MenuPage> pages) {
    /**
     * This record contains the configuration for a page.
     * @param size The GUI size.
     * @param name The gui name.
     * @param entries The {@link Map} mapping {@link String}s to {@link MenuEntry}.
     */
    @ConfigSerializable
    public record MenuPage(
            @Nullable Integer size,
            @Nullable String name,
            @NotNull Map<String, MenuEntry> entries) {
    }
    /**
     * This record contains the configuration for a button.
     * @param type The button type.
     * @param slot The slot number.
     * @param shop The shop name.
     * @param item The {@link Item} configuration.
     */
    @ConfigSerializable
    public record MenuEntry(
            @Nullable String type,
            @Nullable Integer slot,
            @Nullable String shop,
            @Nullable Item item) {}
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
}