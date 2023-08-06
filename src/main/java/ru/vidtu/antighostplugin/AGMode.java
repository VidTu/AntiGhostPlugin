package ru.vidtu.antighostplugin;

/**
 * AntiGhost current mode.
 * Identical to {@code AGMode} in the mod.
 *
 * @author VidTu
 */
public enum AGMode {
    /**
     * The mod is enabled.
     * Default state.
     */
    ENABLED,
    /**
     * The mod is disabled.
     */
    DISABLED,
    /**
     * The mod is enabled, but will use custom packets for requesting blocks.
     */
    CUSTOM;
}
