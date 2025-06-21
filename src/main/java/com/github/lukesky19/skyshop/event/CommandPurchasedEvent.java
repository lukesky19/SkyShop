package com.github.lukesky19.skyshop.event;

import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * This event is called before a {@link List} of commands are purchased.
 */
public class CommandPurchasedEvent extends Event implements Cancellable {
    private static final @NotNull HandlerList HANDLERS = new HandlerList();
    private boolean isCancelled = false;
    private final @NotNull List<String> commands;

    /**
     * Constructor
     * @param commands A {@link List} of {@link String} for the commands being purchased.
     */
    public CommandPurchasedEvent(@NotNull List<String> commands) {
        this.commands = commands;
    }

    /**
     * Get the {@link List} of {@link String} for the commands being purchased.
     * @return A {@link List} of {@link String} for the commands being purchased.
     */
    public @NotNull List<String> getCommands() {
        return commands;
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
