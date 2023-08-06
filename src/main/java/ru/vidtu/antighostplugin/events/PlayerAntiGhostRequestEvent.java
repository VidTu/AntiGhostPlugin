package ru.vidtu.antighostplugin.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import ru.vidtu.antighostplugin.AGMode;

/**
 * Event that is called when an AntiGhost player requests a block update via {@link AGMode#CUSTOM} mode.
 *
 * @author VidTu
 */
public class PlayerAntiGhostRequestEvent extends PlayerEvent implements Cancellable {
    private static final HandlerList HANDLERS = new HandlerList();
    private boolean cancelled;

    /**
     * Creates an event.
     *
     * @param who       Target player
     * @param cancelled Whether the event should be cancelled by default
     * @apiNote Internal use only.
     */
    @ApiStatus.Internal
    public PlayerAntiGhostRequestEvent(@NotNull Player who, boolean cancelled) {
        super(who);
        this.cancelled = cancelled;
    }

    /**
     * Gets whether the server is prohibited to send block updates.
     *
     * @return Whether the server block update process is cancelled
     */
    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    /**
     * Sets whether the server is prohibited to send block updates.
     *
     * @param cancel Whether the server block update process is cancelled
     */
    @Override
    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
    }

    @Contract(pure = true)
    @ApiStatus.Internal
    @Override
    @NotNull
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    @Contract(pure = true)
    @ApiStatus.Internal
    @NotNull
    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

}
