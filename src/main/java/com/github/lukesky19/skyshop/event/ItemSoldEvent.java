package com.github.lukesky19.skyshop.event;

import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * This event is called before an {@link ItemStack} is sold.
 */
public class ItemSoldEvent extends Event implements Cancellable {
    private static final @NotNull HandlerList HANDLERS = new HandlerList();
    private boolean isCancelled = false;
    private final @NotNull ItemStack itemStack; // Item sold

    /**
     * Constructor
     * @param itemStack The {@link ItemStack} being sold.
     */
    public ItemSoldEvent(@NotNull ItemStack itemStack) {
        this.itemStack = itemStack;
    }

    /**
     * Get the {@link ItemStack} that is being sold.
     * @return The {@link ItemStack} that is being sold.
     */
    public @NotNull ItemStack getItemStack() {
        return itemStack;
    }

    /**
     * Get the {@link HandlerList} for this event.
     * @return A {@link HandlerList}.
     */
    public static @NotNull HandlerList getHandlerList() {
        return HANDLERS;
    }

    /**
     * Get the {@link HandlerList} for this event.
     * @return A {@link HandlerList}.
     */
    @Override
    public @NotNull HandlerList getHandlers() {
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
