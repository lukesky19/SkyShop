/*
    SkyShop is a graphical shop plugin.
    Copyright (C) 2024  lukeskywlker19

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
package com.github.lukesky19.skyshop.configuration;

import com.github.lukesky19.skyshop.SkyShop;
import java.nio.file.Path;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.configurate.yaml.NodeStyle;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;

/**
 * This class contains utilities related to loading configuration files using Configurate.
 */
public class ConfigurationUtility {
    final SkyShop plugin;

    /**
     * Constructor
     * @param plugin The plugin instance
     */
    public ConfigurationUtility(SkyShop plugin) {
        this.plugin = plugin;
    }

    /**
     * Creates a YamlConfigurationLoader based on the supplied path.
     * @param path The path of the config file to build a YamlConfigurationLoader for.
     * @return The newly configured YamlConfigurationLoader.
     */
    @Contract("_ -> new")
    @NotNull
    public YamlConfigurationLoader getYamlConfigurationLoader(Path path) {
        return YamlConfigurationLoader.builder()
                .nodeStyle(NodeStyle.BLOCK)
                .path(path)
                .indent(4)
                .build();
    }
}
