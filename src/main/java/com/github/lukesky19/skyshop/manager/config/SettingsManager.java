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
package com.github.lukesky19.skyshop.manager.config;

import com.github.lukesky19.skylib.api.adventure.AdventureUtil;
import com.github.lukesky19.skylib.api.common.abstracts.config.SimpleConfigManager;
import com.github.lukesky19.skyshop.SkyShop;
import com.github.lukesky19.skyshop.config.settings.Settings;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.nio.file.Path;

/**
 * This class manages everything related to handling the plugin's settings.
*/
public class SettingsManager extends SimpleConfigManager<Settings> {
    /**
     * Constructor
     * @param skyShop A {@link SkyShop} instance.
    */
    public SettingsManager(@NotNull SkyShop skyShop) {
        super(skyShop, Path.of(skyShop.getDataFolder() + File.separator + "settings.yml"), Settings.class);
    }

    @Override
    public void loadConfiguration() {
        super.loadConfiguration();
    }

    @Override
    protected void saveBundledConfig() {
        Path path = Path.of(plugin.getDataFolder() + File.separator + "settings.yml");
        if(!path.toFile().exists()) {
            plugin.saveResource("settings.yml", false);
        }
    }

    @Override
    protected @Nullable Settings migrateConfiguration(@NotNull Settings configuration) {
        switch(configuration.configVersion()) {
            case "2.1.0.0" -> {
                // Latest version, do nothing
                return configuration;
            }

            case "2.0.0.0" -> {
                return new Settings(
                        "2.1.0.0",
                        configuration.locale(),
                        configuration.firstRun(),
                        configuration.statistics(),
                        100);
            }

            case null -> {
                logger.error(AdventureUtil.deserialize("Unable to migrate settings configuration because the config version is null."));
                return null;
            }

            default -> {
                logger.error(AdventureUtil.deserialize("Unable to migrate settings configuration because the config version is unknown."));
                return null;
            }
        }
    }

    @Override
    protected boolean validateConfiguration() {
        return true;
    }

    /**
     * This edits the settings.yml file to set `first-run` to false.
    */
    public void setFirstRunFalse() {
        if(configuration == null) return;

        configuration = new Settings(configuration.configVersion(), configuration.locale(), false, configuration.statistics(), configuration.islandSizeLimit());

        saveConfiguration();
    }
}
