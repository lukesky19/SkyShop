package com.github.lukesky19.skyshop.api.event;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jspecify.annotations.NonNull;

import java.util.List;

/**
 * This event is called before a {@link List} of commands are sold.
 */
public class CommandPreSellEvent extends Event implements Cancellable {
    private static final @NonNull HandlerList HANDLERS = new HandlerList();
    private boolean isCancelled = false;
    private final @NonNull Player player;
    private final @NonNull List<String> commands;

    /**
     * Constructor
     * @param player The {@link Player} selling the commands.
     * @param commands A {@link List} of {@link String} for the commands being sold.
     */
    public CommandPreSellEvent(@NonNull Player player, @NonNull List<String> commands) {
        this.player = player;
        this.commands = commands;
    }

    /**
     * Get the {@link Player} selling.
     * @return The {@link Player} selling.
     */
    public @NonNull Player getPlayer() {
        return player;
    }

    /**
     * Get the {@link List} of {@link String} for the commands being sold.
     * @return A {@link List} of {@link String} for the commands being sold.
     */
    public @NonNull List<String> getCommands() {
        return commands;
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
