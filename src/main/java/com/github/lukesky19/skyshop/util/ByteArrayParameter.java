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
package com.github.lukesky19.skyshop.util;

import com.github.lukesky19.skylib.api.database.parameter.Parameter;

/**
 * Takes a byte array and stores it as-is for use in a database.
 */
public class ByteArrayParameter implements Parameter<byte[]> {
    private final byte[] value;

    /**
     * Stores a byte array to later use to replace a parameter with.
     * @param value The byte array to store.
     */
    public ByteArrayParameter(byte[] value) {
        this.value = value;
    }

    /**
     * Returns the byte array to use replace the parameter with.
     * @return A byte array to replace a parameter with.
     */
    @Override
    public byte[] getValue() {
        return value;
    }
}