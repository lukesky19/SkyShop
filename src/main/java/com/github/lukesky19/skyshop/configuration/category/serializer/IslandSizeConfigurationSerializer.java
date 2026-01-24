package com.github.lukesky19.skyshop.configuration.category.serializer;

import com.github.lukesky19.skylib.libs.configurate.ConfigurationNode;
import com.github.lukesky19.skyshop.api.serializer.ConfigurationSerializer;
import com.github.lukesky19.skyshop.configuration.category.transaction.IslandSizeConfiguration;
import org.jetbrains.annotations.NotNull;

/**
 * Serializes/deserializes {@link IslandSizeConfiguration}.
 */
public class IslandSizeConfigurationSerializer extends ConfigurationSerializer<IslandSizeConfiguration> {
    /**
     * Constructor
     */
    public IslandSizeConfigurationSerializer() {
        super(IslandSizeConfiguration.class);
    }

    @Override
    public void migrate(@NotNull ConfigurationNode root) {}
}