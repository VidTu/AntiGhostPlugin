package ru.vidtu.antighostplugin;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.configuration.Configuration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerRegisterChannelEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.messaging.PluginMessageListener;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;
import ru.vidtu.antighostplugin.events.PlayerAntiGhostRegisterEvent;
import ru.vidtu.antighostplugin.events.PlayerAntiGhostRequestEvent;

import java.time.Duration;
import java.util.*;
import java.util.logging.Level;

/**
 * Main AntiGhost plugin class.
 *
 * @author VidTu
 */
public final class AntiGhostPlugin extends JavaPlugin implements Listener, PluginMessageListener, TabExecutor {
    /**
     * Plugin channel used by the mod.
     */
    public static final String CHANNEL = "antighost:v1";
    /**
     * Filler object for Cache
     */
    private static final Object NOTHING = new Object();

    private static AntiGhostPlugin instance;
    private AGMode mode;
    private int radius;
    private int rateLimit;
    private Cache<UUID, Object> rateLimits;

    @ApiStatus.Internal
    @Override
    public void onEnable() {
        instance = this;
        Bukkit.getPluginManager().registerEvents(this, this);
        Bukkit.getMessenger().registerOutgoingPluginChannel(this, CHANNEL);
        Bukkit.getMessenger().registerIncomingPluginChannel(this, CHANNEL, this);
        loadConfigSafe();
    }

    @ApiStatus.Internal
    @Override
    public void onPluginMessageReceived(@NotNull String channel, @NotNull Player player, byte @NotNull [] message) {
        UUID uuid = player.getUniqueId();
        PlayerAntiGhostRequestEvent ev = new PlayerAntiGhostRequestEvent(player, mode != AGMode.CUSTOM ||
                radius == 0 || rateLimits != null && rateLimits.getIfPresent(uuid) != null);
        Bukkit.getPluginManager().callEvent(ev);
        if (ev.isCancelled()) return;
        if (rateLimits != null) {
            rateLimits.put(uuid, NOTHING);
        }
        World world = player.getWorld();
        int min = world.getMinHeight();
        int max = world.getMaxHeight();
        int diameter = radius * 2 + 1;
        List<BlockState> states = new ArrayList<>(diameter * diameter * diameter);
        Block block = player.getLocation().getBlock();
        for (int y = -radius; y <= radius; y++) {
            if (y < min || y >= max) continue;
            for (int x = -radius; x <= radius; x++) {
                for (int z = -radius; z <= radius; z++) {
                    states.add(block.getRelative(x, y, z).getState());
                }
            }
        }
        player.sendBlockChanges(states);
    }

    @ApiStatus.Internal
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        try {
            loadConfigSafe();
            sender.sendMessage(ChatColor.GREEN + "AntiGhost config reloaded.");
        } catch (Exception e) {
            sender.sendMessage(ChatColor.RED + "Unable to reload AntiGhost config. See console for more details.");
        }
        return true;
    }

    @ApiStatus.Internal
    @NotNull
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        return List.of();
    }

    @ApiStatus.Internal
    @EventHandler(ignoreCancelled = true)
    public void onRegisterChannel(PlayerRegisterChannelEvent event) {
        if (!CHANNEL.equals(event.getChannel())) return;
        Player player = event.getPlayer();
        PlayerAntiGhostRegisterEvent ev = new PlayerAntiGhostRegisterEvent(player, mode);
        Bukkit.getPluginManager().callEvent(ev);
        player.sendPluginMessage(this, CHANNEL, new byte[]{(byte) mode.ordinal()});
    }

    /**
     * Loads the config, suppressing and logging all exceptions.
     *
     * @return Whether the config has been loaded without exceptions
     */
    @CanIgnoreReturnValue
    public boolean loadConfigSafe() {
        try {
            loadConfig();
            return true;
        } catch (Exception e) {
            this.mode = AGMode.ENABLED;
            this.radius = 4;
            this.rateLimit = 1000;
            rateLimits = CacheBuilder.newBuilder().expireAfterWrite(Duration.ofMillis(rateLimit)).build();
            getLogger().log(Level.SEVERE, "Unable to load AntiGhost config. Using default config.", e);
            return false;
        }
    }

    /**
     * Loads the config.
     *
     * @throws NullPointerException     If config mode is absent
     * @throws IllegalArgumentException If config mode is invalid or config radius or rate-limit is negative
     */
    public void loadConfig() {
        saveDefaultConfig();
        reloadConfig();
        Configuration config = getConfig();
        String rawMode = config.getString("mode");
        Objects.requireNonNull(rawMode, "Config entry 'mode' is null");
        this.mode = Arrays.stream(AGMode.values()).filter(m -> m.name().equalsIgnoreCase(rawMode))
                .findAny().orElseThrow(() -> new IllegalArgumentException("Unknown mode: " + rawMode));
        this.radius = config.getInt("radius");
        if (radius < 0) {
            throw new IllegalArgumentException("Negative 'radius': " + radius);
        }
        this.rateLimit = config.getInt("rateLimit");
        if (rateLimit < 0) {
            throw new IllegalArgumentException("Negative 'rateLimit': " + rateLimit);
        }
        this.rateLimits = rateLimit == 0 ? null : CacheBuilder.newBuilder().expireAfterWrite(Duration.ofMillis(rateLimit)).build();
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (!player.getListeningPluginChannels().contains(CHANNEL)) continue;
            PlayerAntiGhostRegisterEvent ev = new PlayerAntiGhostRegisterEvent(player, mode);
            Bukkit.getPluginManager().callEvent(ev);
            player.sendPluginMessage(this, CHANNEL, new byte[]{(byte) mode.ordinal()});
        }
    }

    /**
     * Gets the plugin mode.
     *
     * @return Current plugin mode
     */
    @Contract(pure = true)
    @NotNull
    public AGMode getMode() {
        return mode;
    }

    /**
     * Sets the plugin mode.
     *
     * @param mode New plugin mode
     * @throws NullPointerException If the mode is null
     */
    public void setMode(@NotNull AGMode mode) {
        Objects.requireNonNull(mode, "mode is null");
        this.mode = mode;
    }

    /**
     * Gets the radius for {@link AGMode#CUSTOM}.
     *
     * @return Custom radius
     */
    @Contract(pure = true)
    @Range(from = 0L, to = Integer.MAX_VALUE)
    public int getRadius() {
        return radius;
    }

    /**
     * Sets the radius for {@link AGMode#CUSTOM}.
     *
     * @param radius Custom radius
     * @throws IllegalArgumentException If the radius is negative
     */
    public void setRadius(@Range(from = 0L, to = Integer.MAX_VALUE) int radius) {
        //noinspection ConstantValue
        if (radius < 0) {
            throw new IllegalArgumentException("Negative radius: " + radius);
        }
        this.radius = radius;
    }

    /**
     * Gets the rate-limit for {@link AGMode#CUSTOM} in millis.
     *
     * @return Custom rate-limit
     */
    @Contract(pure = true)
    @Range(from = 0L, to = Integer.MAX_VALUE)
    public int getRateLimit() {
        return rateLimit;
    }

    /**
     * Sets the rate-limit for {@link AGMode#CUSTOM} in millis.
     *
     * @param rateLimit New custom rate-limit
     * @throws IllegalArgumentException If the rateLimit is negative
     * @apiNote This method will invalidate all current active rate-limits.
     */
    public void rateLimit(@Range(from = 0L, to = Integer.MAX_VALUE) int rateLimit) {
        //noinspection ConstantValue
        if (rateLimit < 0) {
            throw new IllegalArgumentException("Negative rateLimit: " + radius);
        }
        this.rateLimit = rateLimit;
        this.rateLimits = rateLimit == 0 ? null : CacheBuilder.newBuilder().expireAfterWrite(Duration.ofMillis(rateLimit)).build();
    }

    /**
     * Gets the plugin instance.
     *
     * @return Plugin instance
     */
    @Contract(pure = true)
    public static AntiGhostPlugin instance() {
        return instance;
    }
}
