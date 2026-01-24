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
package com.github.lukesky19.skyshop.configuration.category.serializer.migration;

import com.github.lukesky19.skylib.libs.configurate.ConfigurationNode;
import com.github.lukesky19.skyshop.api.serializer.ConfigurationSerializer;
import com.github.lukesky19.skyshop.configuration.category.transaction.migration.PrestigeMultiplierData;
import org.jetbrains.annotations.NotNull;

/**
 * Serializes/deserializes {@link PrestigeMultiplierData}.
 * Used for migration only.
 */
public class MultiplierConfigurationSerializer extends ConfigurationSerializer<PrestigeMultiplierData> {
    /**
     * Constructor
     */
    public MultiplierConfigurationSerializer() {
        super(PrestigeMultiplierData.class);
    }

    /**
     * Currently nothing to migrate.
     * @param root The root {@link ConfigurationNode}.
     */
    @Override
    public void migrate(@NotNull ConfigurationNode root) {}
}
