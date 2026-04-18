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
package com.github.lukesky19.skyshop.registry;

import com.github.lukesky19.skyshop.api.processor.TransactionProcessor;
import com.github.lukesky19.skyshop.api.serializer.Serializer;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

/**
 * This class manages the serializers and processors for custom configuration files.
 */
public class RegistryManager {
    private final @NonNull Map<String, Serializer> registeredSerializers = new HashMap<>();
    private final @NonNull Map<String, TransactionProcessor> registeredProcessors = new HashMap<>();

    /**
     * Constructor
     */
    public RegistryManager() {}

    /**
     * Register the serializer and processor for the id provided.
     * @param id The id as a {@link String}.
     * @param serializer A {@link Serializer} used to load data from the configuration for the id.
     * @param processor The {@link TransactionProcessor} that processes the data.
     */
    public void register(
            @NonNull String id,
            @NonNull Serializer serializer,
            @NonNull TransactionProcessor processor) {
        id = id.toLowerCase();
        registeredSerializers.putIfAbsent(id, serializer);
        registeredProcessors.putIfAbsent(id, processor);
    }

    /**
     * Unregister the data class and processor for the id provided.
     * @param id The id as a {@link String}.
     */
    public void unregister(@NonNull String id) {
        id = id.toLowerCase();
        registeredSerializers.remove(id);
        registeredProcessors.remove(id);
    }

    /**
     * Get the {@link TransactionProcessor} for the id provided.
     * @param id The id as a {@link String}.
     * @return The {@link TransactionProcessor} or null.
     */
    public @Nullable TransactionProcessor getProcessor(@Nullable String id) {
        if (id == null) return null;
        return registeredProcessors.get(id.toLowerCase());
    }

    /**
     * Get the serializer for the id.
     * @param id The id as a {@link String}.
     * @return A {@link Serializer}.
     */
    public @Nullable Serializer getSerializer(@Nullable String id) {
        if (id == null) return null;

        return registeredSerializers.get(id.toLowerCase());
    }

    /**
     * Registers only a serializer. Used internally only and should not be used elsewhere.
     * @param id The id.
     * @param serializer The serializer.
     */
    public void registerSerializer(@NonNull String id, @NonNull Serializer serializer) {
        id = id.toLowerCase();
        registeredSerializers.put(id, serializer);
    }

    /**
     * Unregisters only a serializer. Used internally only and should not be used elsewhere.
     * @param id The id.
     */
    public void unregisterSerializer(@NonNull String id) {
        id = id.toLowerCase();
        registeredSerializers.remove(id);
    }
}
