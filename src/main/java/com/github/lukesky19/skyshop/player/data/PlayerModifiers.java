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
package com.github.lukesky19.skyshop.player.data;

import com.github.lukesky19.skyshop.player.countdown.Cooldown;
import com.github.lukesky19.skyshop.player.modifier.DoubleModifier;
import com.github.lukesky19.skyshop.player.modifier.IntegerModifier;
import org.jspecify.annotations.NonNull;

import java.io.Serializable;

/**
 * This class stores the player's price modifiers.
 */
public class PlayerModifiers implements Serializable {
    /**
     * The transaction id the modifiers are for.
     */
    private final @NonNull String id;

    /**
     * The modifier for buying with money.
     */
    private final @NonNull DoubleModifier buyMoneyModifier = new DoubleModifier();
    /**
     * The modifier for buying with points.
     */
    private final @NonNull IntegerModifier buyPointsModifier = new IntegerModifier();

    /**
     * The modifier for selling with money.
     */
    private final @NonNull DoubleModifier sellMoneyModifier = new DoubleModifier();
    /**
     * The modifier for selling with points.
     */
    private final @NonNull IntegerModifier sellPointsModifier = new IntegerModifier();

    /**
     * The cooldown for buy modifiers.
     */
    private final @NonNull Cooldown buyCooldown = new Cooldown();
    /**
     * The cooldown for sell modifiers.
     */
    private final @NonNull Cooldown sellCooldown = new Cooldown();

    /**
     * The purchase count for the transaction id.
     */
    private int amountPurchased = 0;
    /**
     * The sell count for the transaction id.
     */
    private int amountSold = 0;

    /**
     * Constructor
     * @param id The transaction id this modifier is tied to.
     */
    public PlayerModifiers(@NonNull String id) {
        this.id = id.toLowerCase();
    }

    /**
     * Get the transaction id this modifier is tied to.
     * @return The transaction id this modifier is tied to.
     */
    public @NonNull String getId() {
        return id;
    }

    /**
     * Set the buy money modifier.
     * @param money The money modifier.
     */
    public void setBuyMoneyModifier(double money) {
        buyMoneyModifier.setCurrentModifier(money);
    }

    /**
     * Get the buy money modifier.
     * @return The buy money modifier.
     */
    public double getBuyMoneyModifier() {
        return buyMoneyModifier.getCurrentModifier();
    }

    /**
     * Set the highest known buy money modifier.
     * @param money The highest buy money modifier.
     */
    public void setHighestBuyMoneyModifier(double money) {
        buyMoneyModifier.setHighestModifier(money);
    }

    /**
     * Get the highest buy money modifier.
     * @return The highest buy money modifier.
     */
    public double getHighestBuyMoneyModifier() {
        return buyMoneyModifier.getHighestModifier();
    }

    /**
     * Set the sell money modifier.
     * @param money The money modifier.
     */
    public void setSellMoneyModifier(double money) {
        sellMoneyModifier.setCurrentModifier(money);
    }

    /**
     * Get the sell money modifier.
     * @return The sell money modifier.
     */
    public double getSellMoneyModifier() {
        return sellMoneyModifier.getCurrentModifier();
    }

    /**
     * Set the highest known sell money modifier.
     * @param money The highest sell money modifier.
     */
    public void setHighestSellMoneyModifier(double money) {
        sellMoneyModifier.setHighestModifier(money);
    }

    /**
     * Get the highest sell money modifier.
     * @return The highest sell money modifier.
     */
    public double getHighestSellMoneyModifier() {
        return sellMoneyModifier.getHighestModifier();
    }

    /**
     * Set the buy points modifier.
     * @param points The points modifier.
     */
    public void setBuyPointsModifier(int points) {
        buyPointsModifier.setCurrentModifier(points);
    }

    /**
     * Get the buy points modifier.
     * @return The buy points modifier.
     */
    public int getBuyPointsModifier() {
        return buyPointsModifier.getCurrentModifier();
    }

    /**
     * Set the highest buy points modifier.
     * @param points The highest buy points modifier.
     */
    public void setHighestBuyPointsModifier(int points) {
        buyPointsModifier.setHighestModifier(points);
    }

    /**
     * Get the highest buy points modifier.
     * @return The highest buy points modifier.
     */
    public int getHighestBuyPointsModifier() {
        return buyPointsModifier.getHighestModifier();
    }

    /**
     * Set the sell points modifier.
     * @param points The points modifier.
     */
    public void setSellPointsModifier(int points) {
        sellPointsModifier.setCurrentModifier(points);
    }

    /**
     * Get the sell points modifier.
     * @return The sell points modifier.
     */
    public int getSellPointsModifier() {
        return sellPointsModifier.getCurrentModifier();
    }

    /**
     * Set the highest sell points modifier.
     * @param points The highest sell points modifier.
     */
    public void setHighestSellPointsModifier(int points) {
        sellPointsModifier.setHighestModifier(points);
    }

    /**
     * Get the highest sell points modifier.
     * @return The highest sell points modifier.
     */
    public int getHighestSellPointsModifier() {
        return sellPointsModifier.getHighestModifier();
    }

    /**
     * Set the buy cooldown.
     * @param seconds The cooldown in seconds.
     */
    public void setBuyCooldownSeconds(int seconds) {
        buyCooldown.setCooldownSeconds(seconds);
    }

    /**
     * Remove the seconds from the buy cooldown.
     * @param seconds The seconds to remove.
     */
    public void removeBuyCooldownSeconds(int seconds) {
        buyCooldown.removeCooldownSeconds(seconds);
    }

    /**
     * Get the buy cooldown in seconds.
     * @return The cooldown in seconds.
     */
    public int getBuyCooldownSeconds() {
        return buyCooldown.getCooldownSeconds();
    }

    /**
     * Set the highest buy cooldown.
     * @param seconds The highest cooldown in seconds.
     */
    public void setHighestBuyCooldown(int seconds) {
        buyCooldown.setHighestCooldownSeconds(seconds);
    }

    /**
     * Get the highest buy cooldown.
     * @return The highest cooldown in seconds.
     */
    public int getHighestBuyCooldown() {
        return buyCooldown.getHighestCooldownSeconds();
    }

    /**
     * Set the buy update interval.
     * @param interval The interval in seconds.
     */
    public void setBuyUpdateInterval(int interval) {
        buyCooldown.setUpdateInterval(interval);
    }

    /**
     * Set the buy update interval.
     * @return The interval in seconds.
     */
    public int getBuyUpdateInterval() {
        return buyCooldown.getUpdateInterval();
    }

    /**
     * Set the buy time elapsed.
     * @param seconds The time in seconds.
     */
    public void setBuyTimeElapsed(int seconds) {
        buyCooldown.setSecondsElapsed(seconds);
    }

    /**
     * Add the time in seconds to the buy time elapsed.
     * @param seconds The time in seconds.
     */
    public void addBuyTimeElapsed(int seconds) {
        buyCooldown.addSecondsElapsed(seconds);
    }

    /**
     * Get the buy time elapsed.
     * @return The time in seconds.
     */
    public int getBuyTimeElapsed() {
        return buyCooldown.getSecondsElapsed();
    }

    /**
     * Set the sell cooldown.
     * @param seconds The cooldown in seconds.
     */
    public void setSellCooldownSeconds(int seconds) {
        sellCooldown.setCooldownSeconds(seconds);
    }

    /**
     * Get the sell cooldown in seconds.
     * @return The cooldown in seconds.
     */
    public int getSellCooldownSeconds() {
        return sellCooldown.getCooldownSeconds();
    }

    /**
     * Remove the seconds from the sell cooldown.
     * @param seconds The seconds to remove.
     */
    public void removeSellCooldownSeconds(int seconds) {
        sellCooldown.removeCooldownSeconds(seconds);
    }

    /**
     * Set the highest sell cooldown.
     * @param seconds The highest cooldown in seconds.
     */
    public void setHighestSellCooldown(int seconds) {
        sellCooldown.setHighestCooldownSeconds(seconds);
    }

    /**
     * Get the highest sell cooldown.
     * @return The highest sell cooldown.
     */
    public int getHighestSellCooldown() {
        return sellCooldown.getHighestCooldownSeconds();
    }

    /**
     * Set the sell update interval.
     * @param interval The interval in seconds.
     */
    public void setSellUpdateInterval(int interval) {
        sellCooldown.setUpdateInterval(interval);
    }

    /**
     * Get the sell update interval.
     * @return The interval in seconds.
     */
    public int getSellUpdateInterval() {
        return sellCooldown.getUpdateInterval();
    }

    /**
     * Set the sell time elapsed.
     * @param seconds The time in seconds.
     */
    public void setSellTimeElapsed(int seconds) {
        sellCooldown.setSecondsElapsed(seconds);
    }

    /**
     * Add the time in seconds to the sell time elapsed.
     * @param seconds The time in seconds.
     */
    public void addSellTimeElapsed(int seconds) {
        sellCooldown.addSecondsElapsed(seconds);
    }

    /**
     * Get the sell time elapsed.
     * @return The time in seconds.
     */
    public int getSellTimeElapsed() {
        return sellCooldown.getSecondsElapsed();
    }

    /**
     * Increment the buy amount.
     * @param buyAmount The buy amount to add.
     */
    public void incrementBuyAmount(int buyAmount) {
        if(buyAmount <= 0) return;

        amountPurchased += buyAmount;
    }

    /**
     * Reset the buy amount.
     */
    public void resetBuyAmount() {
        amountPurchased = 0;
    }

    /**
     * Get the buy amount.
     * @return The number of times purchased.
     */
    public int getBuyAmount() {
        return amountPurchased;
    }

    /**
     * Increment the sell amount.
     * @param sellAmount The sell amount to add.
     */
    public void incrementSellAmount(int sellAmount) {
        if(sellAmount <= 0) return;

        amountSold += sellAmount;
    }

    /**
     * Reset the sell amount.
     */
    public void resetSellAmount() {
        amountSold = 0;
    }

    /**
     * Get the sell amount.
     * @return The number of times sold.
     */
    public int getSellAmount() {
        return amountSold;
    }
}