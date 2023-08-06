package ru.vidtu.antighostplugin.events;

import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import ru.vidtu.antighostplugin.AGMode;

import java.util.Objects;

/**
 * Event that is called when an AntiGhost player joins the game.
 *
 * @author VidTu
 */
public class PlayerAntiGhostRegisterEvent extends PlayerEvent {
    private static final HandlerList HANDLERS = new HandlerList();
    private AGMode mode;

    /**
     * Creates an event.
     *
     * @param who  Target player
     * @param mode Requested client mode
     * @apiNote Internal use only.
     */
    @ApiStatus.Internal
    public PlayerAntiGhostRegisterEvent(@NotNull Player who, @NotNull AGMode mode) {
        super(who);
        this.mode = mode;
    }

    /**
     * Gets the requested client mode to use.
     *
     * @return Requested client mode
     */
    @Contract(pure = true)
    @NotNull
    public AGMode getMode() {
        return mode;
    }

    /**
     * Sets the requested client mode to use.
     *
     * @param mode New requested client mode
     */
    public void setMode(@NotNull AGMode mode) {
        Objects.requireNonNull(mode, "mode is null");
        this.mode = mode;
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
