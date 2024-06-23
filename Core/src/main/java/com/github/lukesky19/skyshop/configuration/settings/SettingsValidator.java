/*
    SkyShop is a simple inventory based shop plugin with page support, error checking, and configuration validation.
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
package com.github.lukesky19.skyshop.configuration.settings;

import com.github.lukesky19.skyshop.SkyShop;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import net.kyori.adventure.text.minimessage.MiniMessage;

/**
 * This class manages the validation logic for settings configuration (settings.yml).
*/
public class SettingsValidator {
    final SkyShop skyShop;

    /**
     * Constructor
     * @param skyShop The plugin's instance.
    */
    public SettingsValidator(SkyShop skyShop) {
        this.skyShop = skyShop;
    }

    /**
     * Checks if the settings configuration is valid.
     * @param settingsConfiguration The SettingsConfiguration to be validated.
     * @return true if valid, false if not.
    */
    public boolean isSettingsValid(SettingsConfiguration settingsConfiguration) {
        ComponentLogger logger = this.skyShop.getComponentLogger();
        if(settingsConfiguration == null) {
            logger.error(MiniMessage.miniMessage().deserialize("<red>Unable to load <yellow>settings.yml</yellow>. Does it exist?</red>"));
            return false;
        }

        if(settingsConfiguration.firstRun() == null) {
            logger.error(MiniMessage.miniMessage().deserialize("<red>Setting <yellow>first-run</yellow> is invalid. Is it set?</red>"));
            return false;
        }

        if(settingsConfiguration.debug() == null) {
            logger.error(MiniMessage.miniMessage().deserialize("<red>Setting <yellow>debug</yellow> is invalid. Is it set?</red>"));
            return false;
        }

        if(settingsConfiguration.locale() == null) {
            logger.error(MiniMessage.miniMessage().deserialize("<red>Setting <yellow>locale</yellow> is invalid. Is it set?</red>"));
            return false;
        }

        return true;
    }
}
