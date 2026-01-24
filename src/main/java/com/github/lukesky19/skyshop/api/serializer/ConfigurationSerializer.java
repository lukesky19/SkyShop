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

import com.github.lukesky19.skylib.libs.configurate.ConfigurateException;
import com.github.lukesky19.skylib.libs.configurate.ConfigurationNode;
import com.github.lukesky19.skylib.libs.configurate.objectmapping.ConfigSerializable;
import com.github.lukesky19.skylib.libs.configurate.serialize.SerializationException;
import com.github.lukesky19.skyshop.api.configuration.TransactionConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * This abstract class can be used to create a {@link Serializer} for {@link TransactionConfiguration} that is annotated with {@link ConfigSerializable}.
 * @param <T> The class that extends {@link TransactionConfiguration}.
 */
public abstract class ConfigurationSerializer<T extends TransactionConfiguration> implements Serializer {
    private final @NotNull Class<T> clazz;

    /**
     * Constructor
     * @param clazz The class being serialized.
     */
    protected ConfigurationSerializer(@NotNull Class<T> clazz) {
        this.clazz = clazz;
    }
    @Override
    public void serialize(@Nullable TransactionConfiguration data, @NotNull ConfigurationNode root) throws SerializationException {
        if(data == null) {
            root.raw(null);
            return;
        }

        root.set(clazz, data);
    }

    @Override
    public TransactionConfiguration deserialize(@NotNull ConfigurationNode root) {
        try {
            // Apply migration
            migrate(root);

            // Get the object T from the node
            return root.get(clazz);
        } catch (ConfigurateException e) {
            throw new RuntimeException(e);
        }
    }
}
