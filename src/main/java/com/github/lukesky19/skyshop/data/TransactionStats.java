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
package com.github.lukesky19.skyshop.data;

import org.bukkit.inventory.ItemType;

/**
 * This class holds the stats for the amount the {@link ItemType} has been purchased or sold.
 */
public class TransactionStats {
    /**
     * The amount purchased.
     */
    private long amountPurchased;
    /**
     * The amount sold.
     */
    private long amountSold;

    /**
     * Default Constructor.
     * You should use {@link TransactionStats#TransactionStats(long, long)} instead.
     */
    @Deprecated
    public TransactionStats() {
        throw new RuntimeException("The use of this constructor is not allowed.");
    }

    /**
     * Constructor
     * @param amountPurchased The number of times the {@link ItemType} has been purchased.
     * @param amountSold The number of times the {@link ItemType} has been sold.
     */
    public TransactionStats(long amountPurchased, long amountSold) {
        this.amountPurchased = amountPurchased;
        this.amountSold = amountSold;
    }

    /**
     * Get the amount that has been purchased.
     * @return The amount that has been purchased.
     */
    public long getAmountPurchased() {
        return amountPurchased;
    }

    /**
     * Get the amount that has been sold.
     * @return The amount that has been sold.
     */
    public long getAmountSold() {
        return amountSold;
    }

    /**
     * Increment the {@link #amountPurchased} counter by provided amount.
     * @param incrementAmount The amount to increment the {@link #amountPurchased} counter by.
     */
    public void incrementAmountPurchased(long incrementAmount) {
        amountPurchased += incrementAmount;
    }

    /**
     * Increment the {@link #amountSold} counter by provided amount.
     * @param incrementAmount The amount to increment the {@link #amountSold} counter by.
     */
    public void incrementAmountSold(long incrementAmount) {
        amountSold += incrementAmount;
    }
}
