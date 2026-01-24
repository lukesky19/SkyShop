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
package com.github.lukesky19.skyshop.configuration.type_serializer;

import com.github.lukesky19.skylib.libs.configurate.ConfigurationNode;
import com.github.lukesky19.skylib.libs.configurate.serialize.SerializationException;
import com.github.lukesky19.skylib.libs.configurate.serialize.TypeSerializer;
import com.github.lukesky19.skyshop.api.configuration.TransactionConfiguration;
import com.github.lukesky19.skyshop.api.serializer.Serializer;
import com.github.lukesky19.skyshop.registry.RegistryManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Type;

/**
 * This class serializes/deserializes {@link TransactionConfiguration}. Relies on registered serializers based on ids.
 */
public class TransactionConfigurationSerializer implements TypeSerializer<TransactionConfiguration> {
    private final @NotNull RegistryManager registryManager;

    /**
     * Constructor
     * @param registryManager A {@link RegistryManager} instance.
     */
    public TransactionConfigurationSerializer(@NotNull RegistryManager registryManager) {
        this.registryManager = registryManager;
    }

    /**
     * Deserializes the configuration to {@link TransactionConfiguration} based on registered serializers.
     * @param type The {@link Type}.
     * @param node The {@link ConfigurationNode}.
     * @return The {@link TransactionConfiguration} or null.
     * @throws SerializationException If the configuration lacks an id, there is no serializer for the id, or the serialization fails.
     */
    @Override
    public @Nullable TransactionConfiguration deserialize(@NotNull Type type, @NotNull ConfigurationNode node) throws SerializationException {
        ConfigurationNode idNode = node.node("id");
        if(idNode.virtual()) {
            throw new SerializationException("The configuration node lacks an id.");
        }

        String id = idNode.getString();
        if(id == null) throw new SerializationException("Unable to deserialize when the id is null.");

        // Obtain the serializer
        Serializer serializer = registryManager.getSerializer(id);
        if(serializer == null) throw new SerializationException("No serializer for id " + id);

        // Use the serializer to deserialize safely
        return serializer.deserialize(node);
    }

    /**
     * Serializes the {@link TransactionConfiguration} based on registered serializers.
     * @param type The {@link Type}.
     * @param transactionConfiguration The {@link TransactionConfiguration}.
     * @param node The {@link ConfigurationNode} to save to.
     * @throws SerializationException If the configuration lacks an id, there is no serializer for the id, or the serialization fails.
     */
    @Override
    public void serialize(@NotNull Type type, @Nullable TransactionConfiguration transactionConfiguration, @NotNull ConfigurationNode node) throws SerializationException {
        if(transactionConfiguration == null) {
            node.raw(null);
            return;
        }

        @Nullable String id = transactionConfiguration.getId();
        if(id == null) throw new SerializationException("Unable to serialize when the id is null.");

        // Obtain the serializer
        Serializer serializer = registryManager.getSerializer(id);
        if(serializer == null) throw new SerializationException("No serializer for id " + id);

        serializer.serialize(transactionConfiguration, node);
    }
}