package com.github.lukesky19.skyshop.task.tasks;

import com.github.lukesky19.skylib.api.gui.impl.UUIDGUIManager;
import com.github.lukesky19.skyshop.player.PlayerDataManager;
import com.github.lukesky19.skyshop.player.data.PlayerModifiers;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/**
 * This class decrements cooldowns for player price modifiers and recalculates the player price modifiers.
 */
public class CooldownTask extends BukkitRunnable {
    private final @NotNull PlayerDataManager playerDataManager;
    private final @NotNull UUIDGUIManager guiManager;

    /**
     * Constructor
     * @param playerDataManager A {@link PlayerDataManager} instance.
     * @param guiManager A {@link UUIDGUIManager} instance.
     */
    public CooldownTask(@NotNull PlayerDataManager playerDataManager, @NotNull UUIDGUIManager guiManager) {
        this.playerDataManager = playerDataManager;
        this.guiManager = guiManager;
    }

    /**
     * This method is run every time the task is executed.
     */
    @Override
    public void run() {
        playerDataManager.getPlayerData().forEach((playerId, playerData) ->
                playerData.getPlayerPrices().forEach(playerPrices ->
                        decrementCooldown(playerId, playerPrices)));
    }

    /**
     * Decrement the player's cooldowns and recalculate the player prices once the time elapsed equals the update interval.
     * @param playerId The player's {@link UUID}.
     * @param playerPrices The {@link PlayerModifiers}.
     */
    private void decrementCooldown(@NotNull UUID playerId, @NotNull PlayerModifiers playerPrices) {
        if(playerPrices.getBuyCooldownSeconds() <= 0 && playerPrices.getSellCooldownSeconds() <= 0) return;
        boolean updateGUIs = false;

        if(playerPrices.getBuyCooldownSeconds() > 0
                && playerPrices.getHighestBuyCooldown() > 0
                && playerPrices.getBuyUpdateInterval() > 0) {
            playerPrices.removeBuyCooldownSeconds(1);
            playerPrices.addBuyTimeElapsed(1);

            if(playerPrices.getBuyCooldownSeconds() == 0) {
                playerPrices.setBuyMoneyModifier(0);
                playerPrices.setHighestBuyMoneyModifier(0);
                playerPrices.setBuyPointsModifier(0);
                playerPrices.setHighestBuyPointsModifier(0);
                playerPrices.setBuyCooldownSeconds(0);
                playerPrices.setHighestBuyCooldown(0);
                playerPrices.setBuyUpdateInterval(0);
                playerPrices.setBuyTimeElapsed(0);

                updateGUIs = true;
            } else if(playerPrices.getBuyTimeElapsed() % playerPrices.getBuyUpdateInterval() == 0) {
                if(playerPrices.getBuyMoneyModifier() > 0 && playerPrices.getHighestBuyMoneyModifier() > 0) {
                    // Formula: Updated Modifier = Starting Modifier × (Current Cooldown / Total Cooldown)

                    playerPrices.setBuyMoneyModifier(playerPrices.getHighestBuyMoneyModifier()
                            * ((double) playerPrices.getBuyCooldownSeconds() / playerPrices.getHighestBuyCooldown()));

                    updateGUIs = true;
                }

                if(playerPrices.getBuyPointsModifier() > 0 && playerPrices.getHighestBuyPointsModifier() > 0) {
                    // Formula: Updated Modifier = Starting Modifier × (Current Cooldown / Total Cooldown)

                    playerPrices.setBuyPointsModifier(playerPrices.getHighestBuyPointsModifier()
                            * (playerPrices.getBuyCooldownSeconds() / playerPrices.getHighestBuyCooldown()));

                    updateGUIs = true;
                }
            }
        }

        if(playerPrices.getSellCooldownSeconds() > 0
                && playerPrices.getHighestSellCooldown() > 0
                && playerPrices.getSellUpdateInterval() > 0) {
            playerPrices.removeSellCooldownSeconds(1);
            playerPrices.addSellTimeElapsed(1);

            if(playerPrices.getSellCooldownSeconds() == 0) {
                playerPrices.setSellMoneyModifier(0);
                playerPrices.setHighestSellMoneyModifier(0);
                playerPrices.setSellPointsModifier(0);
                playerPrices.setHighestSellPointsModifier(0);
                playerPrices.setSellCooldownSeconds(0);
                playerPrices.setHighestSellCooldown(0);
                playerPrices.setSellUpdateInterval(0);
                playerPrices.setSellTimeElapsed(0);

                updateGUIs = true;
            } else if(playerPrices.getSellTimeElapsed() % playerPrices.getSellUpdateInterval() == 0) {
                if(playerPrices.getSellMoneyModifier() > 0 && playerPrices.getHighestSellMoneyModifier() > 0) {
                    // Formula: Updated Modifier = Starting Modifier × (Current Cooldown / Total Cooldown)

                    playerPrices.setSellMoneyModifier(playerPrices.getHighestSellMoneyModifier()
                            * ((double) playerPrices.getSellCooldownSeconds() / playerPrices.getHighestSellCooldown()));

                    updateGUIs = true;
                }

                if(playerPrices.getSellPointsModifier() > 0 && playerPrices.getHighestSellPointsModifier() > 0) {
                    // Formula: Updated Modifier = Starting Modifier × (Current Cooldown / Total Cooldown)

                    playerPrices.setSellPointsModifier(playerPrices.getHighestSellPointsModifier()
                            * (playerPrices.getSellCooldownSeconds() / playerPrices.getHighestSellCooldown()));

                    updateGUIs = true;
                }
            }
        }

        if(updateGUIs) {
            guiManager.refreshGUIs(playerId);
        }
    }
}