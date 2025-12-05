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
package com.github.lukesky19.skyshop.manager;

import com.github.lukesky19.skylib.api.adventure.AdventureUtil;
import com.github.lukesky19.skylib.api.registry.RegistryUtil;
import com.github.lukesky19.skyshop.SkyShop;
import com.github.lukesky19.skyshop.config.gui.CategoryConfig;
import com.github.lukesky19.skyshop.data.PriceCache;
import com.github.lukesky19.skyshop.util.ButtonType;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import org.bukkit.inventory.ItemType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * This class keeps track of the appropriate sell prices for each {@link ItemType} as configured in {@link CategoryConfig}s.
 */
public class PriceManager {
    private final @NotNull SkyShop skyShop;
    private final @NotNull Map<@NotNull ItemType, @NotNull PriceCache> priceCache = new HashMap<>();

    /**
     * Default Constructor.
     * You should use {@link PriceManager#PriceManager(SkyShop)} instead.
     * @throws RuntimeException if used.
     * @deprecated Use {@link PriceManager#PriceManager(SkyShop)} instead.
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
     * Get the {@link PriceCache} for the {@link ItemType}.
     * @param itemType The {@link ItemType}.
     * @return The {@link PriceCache} or null if no prices are cached for the ItemType.
     */
    public @Nullable PriceCache getCachedPrice(@NotNull ItemType itemType) {
        return priceCache.get(itemType);
    }

    /**
     * Clear the cached prices.
     */
    public void clearCache() {
        priceCache.clear();
    }

    /**
     * Cache the sell prices for the provided {@link CategoryConfig}.
     * @param categoryConfig The {@link CategoryConfig} to parse.
     */
    public void cacheCategorySellPrices(@NotNull CategoryConfig categoryConfig) {
        ComponentLogger logger = skyShop.getComponentLogger();

        List<CategoryConfig.PageConfig> pages = categoryConfig.gui().pages();
        for(int pageNum = 0; pageNum <= (pages.size() - 1); pageNum++) {
            CategoryConfig.PageConfig pageConfig = pages.get(pageNum);
            List<CategoryConfig.ButtonConfig> buttons = pageConfig.buttons();

            for(int buttonNum = 0; buttonNum <= (buttons.size() - 1); buttonNum++) {
                // Get the button config.
                CategoryConfig.ButtonConfig buttonConfig = buttons.get(buttonNum);
                // Only continue if the button type is valid and of type TRANSACTION
                if(buttonConfig.buttonType() == null || !buttonConfig.buttonType().equals(ButtonType.TRANSACTION)) continue;
                // Get the transaction data
                CategoryConfig.TransactionData transactionData = buttonConfig.transactionData();

                // If the TransactionData is null, skip to the next button.
                if(transactionData == null) continue;
                // If the ItemType isn't configured, skip to the next button.
                if(transactionData.transactionItem().itemType() == null) continue;

                // Get the ItemType, logging an error if no ItemType was found and skip to the next button.
                @NotNull Optional<ItemType> optionalItemType = RegistryUtil.getItemType(logger, transactionData.transactionItem().itemType());
                if(optionalItemType.isEmpty()) {
                    logger.warn(AdventureUtil.deserialize("Unable to cache sell prices due to an invalid ItemType for ." + transactionData.transactionItem().itemType()));
                    continue;
                }
                ItemType itemType = optionalItemType.get();

                // Get the price config
                CategoryConfig.PriceConfig priceConfig = transactionData.prices();
                // If there is no sell prices configured, continue
                if(priceConfig.sellPrice() <= 0 && priceConfig.sellPoints() <= 0) continue;

                // Create a new PriceCache
                PriceCache priceCache = new PriceCache(priceConfig.sellPrice(), priceConfig.sellPoints());

                // Attempt to cache the new price cache if it is better than the existing
                addPriceCache(itemType, priceCache);
            }
        }
    }

    /**
     * Cache the {@link PriceCache} for the {@link ItemType} if there is no stored cache for that ItemType, the money is higher, or the money is equal and points is higher.
     * @param itemType The {@link ItemType}.
     * @param priceCache The {@link PriceCache}.
     */
    private void addPriceCache(@NotNull ItemType itemType, @NotNull PriceCache priceCache) {
        @Nullable PriceCache existingCache = this.priceCache.get(itemType);

        // If there is no cached price stored for the ItemType, add it to the cache or determine the better price cache to store.
        if(existingCache == null) {
            this.priceCache.put(itemType, priceCache);
        } else {
            double existingMoney = existingCache.money();
            int existingPoints = existingCache.points();
            double newMoney = priceCache.money();
            int newPoints = priceCache.points();

            // Store the new price cache if the money is higher or the money is equal and the points are higher.
            if(newMoney > existingMoney || (newMoney == existingMoney && newPoints > existingPoints)) {
                this.priceCache.put(itemType, priceCache);
            }
        }
    }
}
