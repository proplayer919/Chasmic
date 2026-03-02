package dev.proplayer919.chasmic.command.commands;

import dev.proplayer919.chasmic.CustomPlayer;
import dev.proplayer919.chasmic.command.PermissionCommand;
import dev.proplayer919.chasmic.command.PlayerNameArgument;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.MinecraftServer;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.arguments.ArgumentWord;
import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.Player;

/**
 * /gamemode command for changing player gamemode
 * Permission: admin.command.gamemode
 */
public class GamemodeCommand extends PermissionCommand {

    public GamemodeCommand() {
        super("gamemode", "admin.command.gamemode");

        // Arguments
        ArgumentWord modeArg = ArgumentType.Word("mode")
                .from("survival", "creative", "adventure", "spectator");

        PlayerNameArgument playerArg = PlayerNameArgument.playerName("player");

        // /gamemode <mode> - Set own gamemode
        addSyntax((sender, context) -> {
            if (!checkPlayerPermission(sender)) return;

            CustomPlayer player = (CustomPlayer) sender;
            String modeStr = context.get(modeArg).toLowerCase();
            GameMode gameMode = parseGameMode(modeStr);

            if (gameMode == null) {
                sender.sendMessage(Component.text("Invalid gamemode!", NamedTextColor.RED));
                return;
            }

            player.setGameMode(gameMode);
            sender.sendMessage(Component.text("Gamemode set to: ", NamedTextColor.GREEN)
                    .append(Component.text(modeStr, NamedTextColor.AQUA)));
        }, modeArg);

        // /gamemode <mode> <player> - Set another player's gamemode
        addSyntax((sender, context) -> {
            if (!checkPermission(sender)) return;

            String modeStr = context.get(modeArg).toLowerCase();
            GameMode gameMode = parseGameMode(modeStr);


            if (gameMode == null) {
                sender.sendMessage(Component.text("Invalid gamemode!", NamedTextColor.RED));
                return;
            }

            String targetName = context.get(playerArg);
            Player target = MinecraftServer.getConnectionManager().getOnlinePlayerByUsername(targetName);

            if (!(target instanceof CustomPlayer)) {
                sender.sendMessage(Component.text("Player not found!", NamedTextColor.RED));
                return;
            }

            target.setGameMode(gameMode);
            target.sendMessage(Component.text("Your gamemode has been set to: ", NamedTextColor.GREEN)
                    .append(Component.text(modeStr, NamedTextColor.AQUA)));

            sender.sendMessage(Component.text(target.getUsername() + "'s gamemode set to: ", NamedTextColor.YELLOW)
                    .append(Component.text(modeStr, NamedTextColor.AQUA)));
        }, modeArg, playerArg);
    }

    private GameMode parseGameMode(String mode) {
        return switch (mode.toLowerCase()) {
            case "survival", "s", "0" -> GameMode.SURVIVAL;
            case "creative", "c", "1" -> GameMode.CREATIVE;
            case "adventure", "a", "2" -> GameMode.ADVENTURE;
            case "spectator", "sp", "3" -> GameMode.SPECTATOR;
            default -> null;
        };
    }
}

