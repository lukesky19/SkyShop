package com.github.lukesky19.skyshop.hook.impl;

import com.github.lukesky19.skyshop.hook.Hook;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import world.bentobox.bentobox.BentoBox;
import world.bentobox.bentobox.api.events.island.IslandEvent;
import world.bentobox.bentobox.database.objects.Island;
import world.bentobox.bentobox.managers.IslandsManager;

import java.util.UUID;

/**
 * This class manages interfacing with the BentoBox plugin.
 */
public class BentoBoxHook implements Hook {
    private @NotNull IslandsManager islandsManager;

    /**
     * Constructor
     */
    public BentoBoxHook() {
        initialize();
    }

    /**
     * Get the {@link BentoBox} instance and any other classes necessary.
     */
    @Override
    public void initialize() {
        @NotNull BentoBox bentoBox = BentoBox.getInstance();
        islandsManager = bentoBox.getIslandsManager();
    }

    /**
     * Is the hook initialized?
     * @return true if hooked, otherwise false.
     */
    @Override
    public boolean isHooked() {
        return true;
    }

    /**
     * Attempt to get the island at the given location.
     * @param location The {@link Location}.
     * @return An {@link Island} or null.
     */
    public @Nullable Island getIslandAtLocation(@NotNull Location location) {
        return islandsManager.getIslandAt(location).orElse(null);
    }

    /**
     * Add the provided island size to the existing island's protection range.
     * @param playerId The {@link UUID} of the player changing the island size.
     * @param island The island to add the island size to.
     * @param islandSize The island size to add.
     * @param maxSize The maximum size an island can be.
     */
    public void addIslandSize(@NotNull UUID playerId, @NotNull Island island, int islandSize, int maxSize) {
        int oldRange = island.getProtectionRange();
        int newRange = Math.min(maxSize, island.getProtectionRange() + islandSize);

        // Set the island range
        island.setProtectionRange(newRange);

        // Call an island range change event
        IslandEvent.builder()
                .island(island).location(island.getCenter())
                .reason(IslandEvent.Reason.RANGE_CHANGE).involvedPlayer(playerId).admin(true)
                .protectionRange(newRange, oldRange)
                .build();
    }

    /**
     * Remove the provided island size to the existing island's protection range.
     * Will be clamped to 1 if the calculated size would be less than or equal to 0.
     * @param playerId The {@link UUID} of the player changing the island size.
     * @param island The island to remove the island size from.
     * @param islandSize The island size to remove.
     */
    public void removeIslandSize(@NotNull UUID playerId, @NotNull Island island, int islandSize) {
        int oldRange = island.getProtectionRange();
        int newRange = Math.min(1, island.getProtectionRange() - islandSize);

        // Set the island range
        island.setProtectionRange(newRange);

        // Call an island range change event
        IslandEvent.builder()
                .island(island).location(island.getCenter())
                .reason(IslandEvent.Reason.RANGE_CHANGE).involvedPlayer(playerId).admin(true)
                .protectionRange(newRange, oldRange)
                .build();
    }

    /**
     * Set the provided island's protection range to the provided size.
     * @param playerId The {@link UUID} of the player changing the island size.
     * @param island The island to set the island size for.
     * @param islandSize The island size to set.
     * @param maxSize The maximum size an island can be.
     */
    public void setIslandSize(@NotNull UUID playerId, @NotNull Island island, int islandSize, int maxSize) {
        int oldRange = island.getProtectionRange();
        int newRange = Math.max(1, Math.min(maxSize, islandSize));

        // Set the island range
        island.setProtectionRange(newRange);

        // Call an island range change event
        IslandEvent.builder()
                .island(island).location(island.getCenter())
                .reason(IslandEvent.Reason.RANGE_CHANGE).involvedPlayer(playerId).admin(true)
                .protectionRange(newRange, oldRange)
                .build();
    }
}
