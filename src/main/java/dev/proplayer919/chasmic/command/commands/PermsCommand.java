package dev.proplayer919.chasmic.command.commands;

import dev.proplayer919.chasmic.CustomPlayer;
import dev.proplayer919.chasmic.command.PermissionCommand;
import dev.proplayer919.chasmic.command.PlayerNameArgument;
import dev.proplayer919.chasmic.data.MongoDBHandler;
import lombok.Setter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.MinecraftServer;
import net.minestom.server.command.builder.arguments.ArgumentString;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.arguments.ArgumentWord;
import net.minestom.server.entity.Player;

/**
 * /perms command for viewing and managing player permissions
 * Permission: admin.command.perms
 */
public class PermsCommand extends PermissionCommand {
    @Setter
    private static MongoDBHandler mongoDBHandler;

    public PermsCommand() {
        super("perms", "admin.command.perms");

        // Arguments
        PlayerNameArgument playerArg = PlayerNameArgument.playerName("player");

        ArgumentWord setArg = ArgumentType.Word("set").from("set");

        ArgumentString permArg = ArgumentType.String("permission");

        ArgumentWord boolArg = ArgumentType.Word("value")
                .from("true", "false");

        // /perms <player> - View player's permissions
        addSyntax((sender, context) -> {
            if (!checkPermission(sender)) return;

            String targetName = context.get(playerArg);
            Player target = MinecraftServer.getConnectionManager().getOnlinePlayerByUsername(targetName);

            if (!(target instanceof CustomPlayer customTarget)) {
                sender.sendMessage(Component.text("Player not found!", NamedTextColor.RED));
                return;
            }

            sender.sendMessage(Component.text("=== Permissions for " + target.getUsername() + " ===", NamedTextColor.GOLD));
            sender.sendMessage(Component.text("Rank: ", NamedTextColor.YELLOW)
                    .append(customTarget.getRank().getName()));

            sender.sendMessage(Component.text("Rank Permissions:", NamedTextColor.AQUA));
            for (String perm : customTarget.getRank().getInheritedDefaultPermissions()) {
                sender.sendMessage(Component.text("  • " + perm, NamedTextColor.GRAY));
            }

            if (customTarget.getPlayerData() != null &&
                !customTarget.getPlayerData().getCustomPermissions().isEmpty()) {
                sender.sendMessage(Component.text("Custom Permissions:", NamedTextColor.AQUA));
                for (String perm : customTarget.getPlayerData().getCustomPermissions()) {
                    NamedTextColor color = perm.startsWith("-") ? NamedTextColor.RED : NamedTextColor.GREEN;
                    sender.sendMessage(Component.text("  • " + perm, color));
                }
            } else {
                sender.sendMessage(Component.text("No custom permissions.", NamedTextColor.GRAY));
            }
        }, playerArg);

        // /perms <player> set <permission> <true|false> - Set permission
        addSyntax((sender, context) -> {
            if (!checkPermission(sender)) return;

            String targetName = context.get(playerArg);
            Player target = MinecraftServer.getConnectionManager().getOnlinePlayerByUsername(targetName);

            if (!(target instanceof CustomPlayer customTarget)) {
                sender.sendMessage(Component.text("Player not found!", NamedTextColor.RED));
                return;
            }

            String permission = String.join(".", context.get(permArg));
            boolean value = context.get(boolArg).equals("true");

            if (value) {
                // Grant permission
                customTarget.addPermission(permission);
                sender.sendMessage(Component.text("Granted permission '", NamedTextColor.GREEN)
                        .append(Component.text(permission, NamedTextColor.YELLOW))
                        .append(Component.text("' to " + target.getUsername(), NamedTextColor.GREEN)));
            } else {
                // Deny permission (add with - prefix)
                String denyPerm = permission.startsWith("-") ? permission : "-" + permission;
                customTarget.addPermission(denyPerm);
                sender.sendMessage(Component.text("Denied permission '", NamedTextColor.RED)
                        .append(Component.text(permission, NamedTextColor.YELLOW))
                        .append(Component.text("' for " + target.getUsername(), NamedTextColor.RED)));
            }

            // Save to database
            if (customTarget.getPlayerData() != null && mongoDBHandler != null) {
                mongoDBHandler.savePlayerData(customTarget.getPlayerData());
            }

            target.sendMessage(Component.text("Your permissions have been updated.", NamedTextColor.YELLOW));

        }, playerArg, setArg, permArg, boolArg);

        setDefaultExecutor((sender, context) -> sender.sendMessage(Component.text("Usage: /perms <player> [set <permission> <true|false>]", NamedTextColor.RED)));
    }
}
