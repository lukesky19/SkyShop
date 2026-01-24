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
package com.github.lukesky19.skyshop.api.serializer;

import com.github.lukesky19.skylib.libs.configurate.ConfigurationNode;
import com.github.lukesky19.skylib.libs.configurate.serialize.SerializationException;
import com.github.lukesky19.skyshop.api.configuration.TransactionConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * This interface can be used to create a serializer to serialize/deserialize {@link TransactionConfiguration}.
 * @apiNote This should be used to serialize/deserialize a concrete implementation of the {@link TransactionConfiguration} interface.
 */
public interface Serializer {
    /**
     * Serialize the {@link TransactionConfiguration}.
     * @param data The {@link TransactionConfiguration}.
     * @param root The root {@link ConfigurationNode}.
     * @throws SerializationException If serialization fails.
     */
    void serialize(@Nullable TransactionConfiguration data, @NotNull ConfigurationNode root) throws SerializationException;

    /**
     * Deserialize the {@link TransactionConfiguration}.
     * @param root The root {@link ConfigurationNode}.
     * @return The {@link TransactionConfiguration} or null.
     */
    @Nullable TransactionConfiguration deserialize(@NotNull ConfigurationNode root);

    /**
     * Migrate the configuration.
     * @param root The root {@link ConfigurationNode}.
     */
    void migrate(@NotNull ConfigurationNode root);
}
