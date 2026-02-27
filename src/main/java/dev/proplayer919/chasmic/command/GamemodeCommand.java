package dev.proplayer919.chasmic.command;

import dev.proplayer919.chasmic.CustomPlayer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.arguments.ArgumentWord;
import net.minestom.server.command.builder.arguments.minecraft.ArgumentEntity;
import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.Player;
import net.minestom.server.utils.entity.EntityFinder;

/**
 * /gamemode command for changing player gamemode
 * Permission: admin.command.gamemode
 */
public class GamemodeCommand extends Command {

    public GamemodeCommand() {
        super("gamemode");

        setCondition((sender, commandString) -> {
            if (sender instanceof CustomPlayer player) {
                return player.hasPermission("admin.command.gamemode");
            }
            return true; // Console always has permission
        });

        // Arguments
        ArgumentWord modeArg = ArgumentType.Word("mode")
                .from("survival", "creative", "adventure", "spectator");

        ArgumentEntity playerArg = ArgumentType.Entity("player")
                .onlyPlayers(true)
                .singleEntity(true);

        // /gamemode <mode> - Set own gamemode
        addSyntax((sender, context) -> {
            if (!(sender instanceof CustomPlayer player)) {
                sender.sendMessage(Component.text("Only players can use this command!", NamedTextColor.RED));
                return;
            }

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
            String modeStr = context.get(modeArg).toLowerCase();
            GameMode gameMode = parseGameMode(modeStr);

            if (gameMode == null) {
                sender.sendMessage(Component.text("Invalid gamemode!", NamedTextColor.RED));
                return;
            }

            EntityFinder finder = context.get(playerArg);
            Player target = finder.findFirstPlayer(sender);

            if (target == null) {
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

