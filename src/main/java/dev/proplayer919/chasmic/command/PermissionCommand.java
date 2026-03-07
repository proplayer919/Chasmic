package dev.proplayer919.chasmic.command;

import dev.proplayer919.chasmic.player.CustomPlayer;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.command.CommandSender;
import net.minestom.server.command.builder.Command;
import net.minestom.server.entity.Player;

/**
 * Base class for commands that require permissions.
 * Automatically handles permission checking and common validations.
 */
public abstract class PermissionCommand extends Command {
    @Getter
    private final String permission;

    private final boolean requiresPlayer;

    /**
     * Create a command with permission checking and aliases.
     *
     * @param name The command name
     * @param permission The permission required to use this command (e.g., "admin.command.fly")
     * @param requiresPlayer Whether this command requires the sender to be a player
     * @param aliases Optional command aliases
     */
    public PermissionCommand(String name, String permission, boolean requiresPlayer, String... aliases) {
        super(name, aliases);
        this.permission = permission;
        this.requiresPlayer = requiresPlayer;

        // Set up the condition for command visibility/tab completion
        setCondition((sender, commandString) -> {
            if (sender instanceof CustomPlayer player) {
                // Allow command if player is not yet initialized (to avoid red text)
                // Actual permission check happens in executor
                if (!player.isInitialized()) {
                    return true;
                }
                return player.hasPermission(permission);
            }
            return true; // Console always has permission
        });
    }

    /**
     * Create a command with permission checking.
     *
     * @param name The command name
     * @param permission The permission required to use this command (e.g., "admin.command.fly")
     * @param requiresPlayer Whether this command requires the sender to be a player
     */
    public PermissionCommand(String name, String permission, boolean requiresPlayer) {
        this(name, permission, requiresPlayer, new String[0]);
    }

    /**
     * Create a command with permission checking that doesn't require a player.
     *
     * @param name The command name
     * @param permission The permission required to use this command
     */
    public PermissionCommand(String name, String permission) {
        this(name, permission, false, new String[0]);
    }

    /**
     * Create a command with permission checking and aliases that doesn't require a player.
     *
     * @param name The command name
     * @param permission The permission required to use this command
     * @param aliases Optional command aliases
     */
    public PermissionCommand(String name, String permission, String... aliases) {
        this(name, permission, false, aliases);
    }

    /**
     * Check if the sender has permission to use this command.
     * Automatically sends an error message if permission is denied.
     *
     * @param sender The command sender
     * @return true if the sender has permission, false otherwise
     */
    protected boolean checkPermission(CommandSender sender) {
        if (sender instanceof CustomPlayer player) {
            if (!player.hasPermission(permission)) {
                sender.sendMessage(Component.text("You don't have permission to use this command!", NamedTextColor.RED));
                return false;
            }
        }
        return true; // Console always has permission
    }

    /**
     * Check if the sender is a player.
     * Automatically sends an error message if the sender is not a player.
     *
     * @param sender The command sender
     * @return true if the sender is a player, false otherwise
     */
    protected boolean checkIsPlayer(CommandSender sender) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(Component.text("Only players can use this command!", NamedTextColor.RED));
            return false;
        }
        return true;
    }

    /**
     * Combined check for both player and permission.
     * This is the most common check needed in command executors.
     *
     * @param sender The command sender
     * @return true if sender is a player with permission, false otherwise
     */
    protected boolean checkPlayerPermission(CommandSender sender) {
        if (!checkIsPlayer(sender)) {
            return false;
        }
        return checkPermission(sender);
    }

    /**
     * Check if this command requires the sender to be a player.
     *
     * @return true if player is required, false otherwise
     */
    public boolean requiresPlayer() {
        return requiresPlayer;
    }
}

