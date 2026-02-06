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
package com.github.lukesky19.skyshop.configuration.settings.data;

import com.github.lukesky19.skylib.libs.configurate.objectmapping.ConfigSerializable;

/**
 * The version 1 format of the locale configuration for migration purposes only.
 * Note: The first version did not have a proper config version defined.
 * @param firstRun Is this the first time the plugin has run?
 * @param debug Should debug logs be sent?
 * @param locale The plugin's locale. Refers to a file in SkyShop/locale
 */
@Deprecated(since = "2.0.0.0")
@ConfigSerializable
public record SettingsV1(
        Boolean firstRun,
        Boolean debug,
        String locale) {}