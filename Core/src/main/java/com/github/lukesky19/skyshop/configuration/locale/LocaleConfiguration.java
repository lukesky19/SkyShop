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

import org.spongepowered.configurate.objectmapping.ConfigSerializable;

import java.util.List;

@ConfigSerializable
public record LocaleConfiguration(
        String prefix,
        List<String> help,
        String noPermission,
        String configReload,
        String notEnoughItems,
        String insufficientFunds,
        String buySuccess,
        String sellSuccess,
        String sellallSuccess,
        String sellallUnsellable,
        String unbuyable,
        String unsellable,
        String inGameOnly,
        String unknownArgument) {
}
