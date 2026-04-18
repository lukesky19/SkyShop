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
package com.github.lukesky19.skyshop.hook;

import com.github.lukesky19.skylib.common.api.integration.Hook;
import com.github.lukesky19.skyshop.SkyShop;
import com.github.lukesky19.skyshop.hook.impl.BentoBoxHook;
import com.github.lukesky19.skyshop.hook.impl.EconomyHook;
import com.github.lukesky19.skyshop.hook.impl.PlayerPointsHook;
import org.jspecify.annotations.NonNull;

import java.util.HashMap;
import java.util.Map;

/**
 * This class manages hooks into different plugins.
 */
public class HookManager {
    private final @NonNull Map<Class<?>, Hook> hooks = new HashMap<>();

    /**
     * Constructor
     * @param skyShop A {@link SkyShop} instance.
     */
    public HookManager(@NonNull SkyShop skyShop) {
        registerHook(BentoBoxHook.class, new BentoBoxHook(skyShop));

        registerHook(EconomyHook.class, new EconomyHook(skyShop));

        registerHook(PlayerPointsHook.class, new PlayerPointsHook(skyShop));
    }

    /**
     * Register a hook.
     * @param hookClass The class.
     * @param hook The class instance.
     * @param <T> Parameter for any class that extends {@link Hook}.
     */
    public <T extends Hook> void registerHook(@NonNull Class<T> hookClass, @NonNull Hook hook) {
        hooks.put(hookClass, hook);
        hook.initialize();
    }

    /**
     * Get a hook.
     * @param hookClass The class.
     * @param <T> Parameter for any class that extends {@link Hook}.
     * @return The class instance.
     */
    public @NonNull <T extends Hook> T getHook(@NonNull Class<T> hookClass) {
        return hookClass.cast(hooks.get(hookClass));
    }
}
