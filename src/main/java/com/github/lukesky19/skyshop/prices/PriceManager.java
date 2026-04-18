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
package com.github.lukesky19.skyshop.prices;

import com.github.lukesky19.skyshop.configuration.category.data.CategoryConfigV4;
import com.github.lukesky19.skyshop.configuration.category.transaction.ItemConfiguration;
import com.github.lukesky19.skyshop.util.ButtonType;
import org.bukkit.inventory.ItemType;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * This class keeps track of the appropriate sell prices for each {@link ItemType} as configured in {@link CategoryConfigV4}s.
 */
public class PriceManager {
    private final @NonNull Map<ItemType, PriceCache> priceCacheByItemType = new HashMap<>();

    /**
     * Constructor
     */
    public PriceManager() {}

    /**
     * Get the {@link PriceCache} for the {@link ItemType}.
     * @param itemType The {@link ItemType}.
     * @return The {@link PriceCache} or null.
     */
    public @Nullable PriceCache getPriceCache(@NonNull ItemType itemType) {
        return priceCacheByItemType.get(itemType);
    }

    /**
     * Clear the cached prices.
     */
    public void clearCache() {
        priceCacheByItemType.clear();
    }

    /**
     * Cache the sell prices for the provided {@link CategoryConfigV4}.
     * @param categoryConfig The {@link CategoryConfigV4} to parse.
     */
    public void cacheCategorySellPrices(@NonNull CategoryConfigV4 categoryConfig) {
        categoryConfig.pages().forEach(pageConfig -> pageConfig.buttons().stream()
                .filter(buttonConfig -> buttonConfig.buttonType() != null && buttonConfig.buttonType().equals(ButtonType.TRANSACTION))
                .filter(buttonConfig -> {
                    CategoryConfigV4.TransactionData transactionData = buttonConfig.transactionData();
                    return transactionData != null && transactionData.transactionId() != null;
                })
                .filter(buttonConfig -> {
                    CategoryConfigV4.PriceConfig priceConfig = buttonConfig.transactionData().prices();
                    return priceConfig.sellMoney() > 0 || priceConfig.sellPoints() > 0;
                })
                .forEach(buttonConfig -> {
                    CategoryConfigV4.TransactionData transactionData = buttonConfig.transactionData();
                    CategoryConfigV4.PriceConfig priceConfig = buttonConfig.transactionData().prices();
                    Optional<ItemType> optionalItemType = transactionData.transactionList().stream()
                            .filter(data -> data instanceof ItemConfiguration)
                            .map(data -> (ItemConfiguration) data)
                            .filter(data -> data.transactionItem().itemType() != null)
                            .filter(ItemConfiguration::cacheSellPrice)
                            .map(data -> data.transactionItem().itemType())
                            .findFirst();

                    optionalItemType.ifPresent(itemType -> {
                        assert transactionData.transactionId() != null; // Button configs with null transaction ids are filtered out.

                        PriceCache existingCache = this.priceCacheByItemType.get(itemType);
                        PriceCache newPriceCache = new PriceCache(
                                categoryConfig.permission(), buttonConfig.permission(),
                                transactionData.transactionId(), transactionData.prices());
                        if(existingCache == null) {
                            this.priceCacheByItemType.put(itemType, newPriceCache);
                        } else {
                            CategoryConfigV4.PriceConfig existingPriceConfig = existingCache.priceConfig();

                            double existingMoney = existingPriceConfig.sellMoney();
                            int existingPoints = existingPriceConfig.sellPoints();
                            double newMoney = priceConfig.sellMoney();
                            int newPoints = priceConfig.sellPoints();

                            // Store the new price config if the money is higher or the money is equal and the points are higher.
                            if(newMoney > existingMoney || (newMoney == existingMoney && newPoints > existingPoints)) {
                                this.priceCacheByItemType.put(itemType, newPriceCache);
                            }
                        }
                    });
                }));
    }
}