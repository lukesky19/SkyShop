package com.github.lukesky19.skyshop.api.event;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.NonNull;

/**
 * This event is called before an {@link ItemStack} is sold.
 */
public class ItemPreSellEvent extends Event implements Cancellable {
    private static final @NonNull HandlerList HANDLERS = new HandlerList();
    private boolean isCancelled = false;
    private final @NonNull Player player;
    private final @NonNull ItemStack itemStack; // Item sold

    /**
     * Constructor
     * @param player The player selling the item.
     * @param itemStack The {@link ItemStack} being sold.
     */
    public ItemPreSellEvent(@NonNull Player player, @NonNull ItemStack itemStack) {
        this.player = player;
        this.itemStack = itemStack;
    }

    /**
     * Get the {@link Player} selling.
     * @return The {@link Player} selling.
     */
    public @NonNull Player getPlayer() {
        return player;
    }

    /**
     * Get the {@link ItemStack} that is being sold.
     * @return The {@link ItemStack} that is being sold.
     */
    public @NonNull ItemStack getItemStack() {
        return itemStack;
    }

    /**
     * Get the {@link HandlerList} for this event.
     * @return A {@link HandlerList}.
     */
    public static @NonNull HandlerList getHandlerList() {
        return HANDLERS;
    }

    /**
     * Get the {@link HandlerList} for this event.
     * @return A {@link HandlerList}.
     */
    @Override
    public @NonNull HandlerList getHandlers() {
        return HANDLERS;
    }

    /**
     * Checks if the event is cancelled.
     * @return true if cancelled, otherwise false.
     */
    @Override
    public boolean isCancelled() {
        return this.isCancelled;
    }

    /**
     * Set if this event should be cancelled.
     * @param isCancelled {@code true} if you wish to cancel this event
     */
    @Override
    public void setCancelled(boolean isCancelled) {
        this.isCancelled = isCancelled;
    }
}