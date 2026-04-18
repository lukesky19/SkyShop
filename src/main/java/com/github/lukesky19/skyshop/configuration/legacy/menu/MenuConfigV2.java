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
import com.github.lukesky19.skylib.paper.api.gui.GUIType;
import com.github.lukesky19.skylib.paper.api.itemstack.ItemStackConfig;
import com.github.lukesky19.skyshop.configuration.category.data.CategoryConfigV4;
import com.github.lukesky19.skyshop.util.ButtonType;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.List;

/**
 * The version 2 format of the menu configuration for migration purposes only.
 * Version 2 = 2.0.0.0 in the old format.
 * @param configVersion The file's config version.
 * @param gui The {@link GuiData} configuration.
 * @deprecated The legacy configuration for the menu has been replaced by {@link CategoryConfigV4}. Only used for data migration.
 */
@Deprecated(since = "2.1.0.0")
@ConfigSerializable
public record MenuConfigV2(@Nullable String configVersion, @NonNull GuiData gui) {
    /**
     * This record contains the actual configuration for creating the initial GUI.
     * @param guiType The {@link GUIType}.
     * @param name The name to display inside the GUI.
     * @param pages The {@link List} of {@link PageConfig}s.
     */
    @ConfigSerializable
    public record GuiData(@Nullable GUIType guiType, @Nullable String name, @NonNull List<@NonNull PageConfig> pages) {}

    /**
     * This record contains the configuration for individual pages.
     * @param buttons The {@link List} of {@link Button}s.
     */
    @ConfigSerializable
    public record PageConfig(@NonNull List<@NonNull Button> buttons) {}

    /**
     * This record contains the configuration for a single button.
     * @param buttonType The {@link ButtonType}.
     * @param slot The slot to place the button at.
     * @param shopName If the {@link ButtonType} is that of OPEN_SHOP, this is the shop name to open. This name corresponds to a file in {@code SkyShop/shops}.
     * @param displayItem The {@link ItemStackConfig} used to create the {@link ItemStack} to display for the button.
     */
    @ConfigSerializable
    public record Button(
            @Nullable ButtonType buttonType,
            @Nullable Integer slot,
            @Nullable String shopName,
            @NonNull ItemStackConfig displayItem) {}
}
