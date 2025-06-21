/*
    SkyShop is a simple inventory based shop plugin with page support, sell commands, and error checking.
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
package com.github.lukesky19.skyshop.manager;

import com.github.lukesky19.skylib.api.adventure.AdventureUtil;
import com.github.lukesky19.skylib.api.registry.RegistryUtil;
import com.github.lukesky19.skyshop.SkyShop;
import com.github.lukesky19.skyshop.data.gui.ShopConfig;
import com.github.lukesky19.skyshop.util.ButtonType;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import org.bukkit.inventory.ItemType;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;

/**
 * This class keeps track of the appropriate sell price for each {@link ItemType} as configured in {@link ShopConfig}s.
 */
public class PriceManager {
    private final @NotNull SkyShop skyShop;
    private final @NotNull HashMap<@NotNull ItemType, @NotNull Double> sellPrices = new HashMap<>();

    /**
     * Default Constructor.
     * You should use {@link PriceManager#PriceManager(SkyShop)} instead.
     * @throws RuntimeException if used.
     */
    @Deprecated
    public PriceManager() {
        throw new RuntimeException("The use of the default constructor is not allowed");
    }

    /**
     * Constructor
     * @param skyShop A {@link SkyShop} instance.
     */
    public PriceManager(@NotNull SkyShop skyShop) {
        this.skyShop = skyShop;
    }

    /**
     * Get the configured sell price for the {@link ItemType}.
     * @param itemType The {@link ItemType}
     * @return {@link Optional} containing the sell price as a {@link Double} for the {@link ItemType}.
     * Will return an empty {@link Optional} if no sell price is cached, which means the {@link ItemType} is not configured to be sold.
     */
    @NotNull
    public Optional<@NotNull Double> getItemTypeSellPrice(@NotNull ItemType itemType) {
        return Optional.ofNullable(sellPrices.get(itemType));
    }

    /**
     * Clear cached sell prices.
     */
    public void clearPrices() {
        sellPrices.clear();
    }

    /**
     * Stores the sell prices for all buttons with a {@link ButtonType} of TRANSACTION from a {@link ShopConfig}.
     * If multiple {@link ShopConfig} contains prices the same {@link ItemType}, the last {@link ShopConfig} processed will take priority.
     * @param shopConfig A {@link ShopConfig}.
     */
    public void cacheSellPrices(@NotNull ShopConfig shopConfig) {
        ComponentLogger logger = skyShop.getComponentLogger();

        List<ShopConfig.PageConfig> pages = shopConfig.gui().pages();
        for(int pageNum = 0; pageNum <= (pages.size() - 1); pageNum++) {
            ShopConfig.PageConfig pageConfig = pages.get(pageNum);
            List<ShopConfig.Button> buttons = pageConfig.buttons();

            for(int buttonNum = 0; buttonNum <= (buttons.size() - 1); buttonNum++) {
                // Get the button config.
                ShopConfig.Button buttonConfig = buttons.get(buttonNum);
                // Only continue if the button type is valid and of type TRANSACTION
                if(buttonConfig.buttonType() == null || !buttonConfig.buttonType().equals(ButtonType.TRANSACTION)) continue;
                // Get the transaction data
                ShopConfig.TransactionData transactionData = buttonConfig.transactionData();

                // If no sell price is configured, skip to the next button.
                if(transactionData.sellPrice() == null || transactionData.sellPrice() <= 0.0) continue;

                // If the ItemType isn't configured, log an error and skip to the next button.
                if(transactionData.transactionItem().itemType() == null) {
                    logger.warn(AdventureUtil.serialize("Unable to cache sell price due to an invalid ItemType."));
                    continue;
                }

                // Get the ItemType, logging an error if no ItemType was found and skip to the next button.
                @NotNull Optional<ItemType> optionalItemType = RegistryUtil.getItemType(logger, transactionData.transactionItem().itemType());
                if(optionalItemType.isEmpty()) {
                    logger.warn(AdventureUtil.serialize("Unable to cache sell price due to an invalid ItemType for ." + transactionData.transactionItem().itemType()));
                    continue;
                }

                // Cache the sell price
                sellPrices.put(optionalItemType.get(), transactionData.sellPrice());
            }
        }
    }
}
