package com.github.lukesky19.skyshop.configuration.category.serializer;

import com.github.lukesky19.skylib.libs.configurate.ConfigurationNode;
import com.github.lukesky19.skyshop.api.serializer.ConfigurationSerializer;
import com.github.lukesky19.skyshop.configuration.category.transaction.CommandConfiguration;
import org.jetbrains.annotations.NotNull;

/**
 * Serializes/deserializes {@link CommandConfiguration}.
 */
public class CommandConfigurationSerializer extends ConfigurationSerializer<CommandConfiguration> {
    /**
     * Constructor
     */
    public CommandConfigurationSerializer() {
        super(CommandConfiguration.class);
    }

    @Override
    public void migrate(@NotNull ConfigurationNode root) {}
}