package com.github.lukesky19.skyshop.gui;

import com.github.lukesky19.skylib.api.gui.AbstractGUIManager;
import com.github.lukesky19.skyshop.SkyShop;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/**
 * This class manages the mapping of open GUIs to {@link UUID}s.
 */
public class GUIManager extends AbstractGUIManager {
    /**
     * Constructor
     * @param skyShop A {@link SkyShop} instance.
     */
    public GUIManager(@NotNull SkyShop skyShop) {
        super(skyShop);
    }
}
