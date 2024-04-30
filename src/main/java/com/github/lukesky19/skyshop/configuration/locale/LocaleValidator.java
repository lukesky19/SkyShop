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
package com.github.lukesky19.skyshop.configuration.locale;

import com.github.lukesky19.skyshop.SkyShop;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import net.kyori.adventure.text.minimessage.MiniMessage;

/**
 * This class manages the validation logic for locale configuration.
*/
public class LocaleValidator {
    final SkyShop skyShop;

    /**
     * Constructor
     * @param skyShop The plugin's instance.
    */
    public LocaleValidator(SkyShop skyShop) {
        this.skyShop = skyShop;
    }

    /**
     * Checks if menu.yml is valid.
     * @param localeConfiguration The LocaleConfiguration to be validated.
     * @return true if valid, false if not.
    */
    public boolean isLocaleValid(LocaleConfiguration localeConfiguration, String locale) {
        ComponentLogger logger = skyShop.getComponentLogger();
        if(localeConfiguration == null) {
            logger.error(MiniMessage.miniMessage().deserialize("<red>Setting <yellow>locale</yellow> is invalid. Is <yellow> " + locale + "</yellow> a valid file?</red>"));
            return false;
        }

        if(localeConfiguration.prefix() == null) {
            logger.error(MiniMessage.miniMessage().deserialize("<red>The prefix message in <yellow>" + locale + "</yellow> is invalid. Is it a valid String?"));
            return false;
        }

        for(String msg : localeConfiguration.help()) {
            if(msg == null) {
                logger.error(MiniMessage.miniMessage().deserialize("<red>The help message in <yellow>" + locale + "</yellow> is invalid. Is it a valid list of Strings?"));
                return false;
            }
        }

        if(localeConfiguration.noPermission() == null) {
            logger.error(MiniMessage.miniMessage().deserialize("<red>The no-permission message in <yellow>" + locale + "</yellow> is invalid. Is it a valid String?"));
            return false;
        }

        if(localeConfiguration.configReload() == null) {
            logger.error(MiniMessage.miniMessage().deserialize("<red>The config-reload message in <yellow>" + locale + "</yellow> is invalid. Is it a valid String?"));
            return false;
        }

        if(localeConfiguration.notEnoughItems() == null) {
            logger.error(MiniMessage.miniMessage().deserialize("<red>The not-enough-items message in <yellow>" + locale + "</yellow> is invalid. Is it a valid String?"));
            return false;
        }

        if(localeConfiguration.insufficientFunds() == null) {
            logger.error(MiniMessage.miniMessage().deserialize("<red>The insufficient-funds message in <yellow>" + locale + "</yellow> is invalid. Is it a valid String?"));
            return false;
        }

        if(localeConfiguration.buySuccess() == null) {
            logger.error(MiniMessage.miniMessage().deserialize("<red>The buy-success message in <yellow>" + locale + "</yellow> is invalid. Is it a valid String?"));
            return false;
        }

        if(localeConfiguration.sellSuccess() == null) {
            logger.error(MiniMessage.miniMessage().deserialize("<red>The sell-success message in <yellow>" + locale + "</yellow> is invalid. Is it a valid String?"));
            return false;
        }

        if(localeConfiguration.sellallSuccess() == null) {
            logger.error(MiniMessage.miniMessage().deserialize("<red>The sellall-success message in <yellow>" + locale + "</yellow> is invalid. Is it a valid String?"));
            return false;
        }

        if(localeConfiguration.sellallUnsellable() == null) {
            logger.error(MiniMessage.miniMessage().deserialize("<red>The sellall-unsellable message in <yellow>" + locale + "</yellow> is invalid. Is it a valid String?"));
            return false;
        }

        if(localeConfiguration.unbuyable() == null) {
            logger.error(MiniMessage.miniMessage().deserialize("<red>The unbuyable message in <yellow>" + locale + "</yellow> is invalid. Is it a valid String?"));
            return false;
        }

        if(localeConfiguration.unsellable() == null) {
            logger.error(MiniMessage.miniMessage().deserialize("<red>The unsellable message in <yellow>" + locale + "</yellow> is invalid. Is it a valid String?"));
            return false;
        }

        if(localeConfiguration.inGameOnly() == null) {
            logger.error(MiniMessage.miniMessage().deserialize("<red>The in-game-only message in <yellow>" + locale + "</yellow> is invalid. Is it a valid String?"));
            return false;
        }

        if(localeConfiguration.unknownArgument() == null) {
            logger.error(MiniMessage.miniMessage().deserialize("<red>The unknown-argument message in <yellow>" + locale + "</yellow> is invalid. Is it a valid String?"));
            return false;
        }

        return true;
    }
}
