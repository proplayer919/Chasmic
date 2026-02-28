package dev.proplayer919.chasmic.command;

import dev.proplayer919.chasmic.CustomPlayer;
import dev.proplayer919.chasmic.PlayerRank;
import dev.proplayer919.chasmic.data.MongoDBHandler;
import lombok.Setter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.MinecraftServer;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.arguments.ArgumentWord;
import net.minestom.server.entity.Player;

import java.util.Arrays;

/**
 * /rank command for viewing and setting player ranks
 * Permission: admin.command.rank
 */
public class RankCommand extends Command {
    @Setter
    private static MongoDBHandler mongoDBHandler;

    public RankCommand() {
        super("rank");

        setCondition((sender, commandString) -> {
            if (sender instanceof CustomPlayer player) {
                // Allow command if player is not yet initialized
                // Actual permission check happens in executor
                if (!player.isInitialized()) {
                    return true;
                }
                return player.hasPermission("admin.command.rank");
            }
            return true; // Console always has permission
        });

        // Arguments
        PlayerNameArgument playerArg = PlayerNameArgument.playerName("player");

        ArgumentWord setArg = ArgumentType.Word("set").from("set");

        ArgumentWord rankArg = ArgumentType.Word("rank")
                .from(Arrays.stream(PlayerRank.values())
                        .map(r -> r.name().toLowerCase())
                        .toArray(String[]::new));

        // /rank <player> - View player's rank
        addSyntax((sender, context) -> {
            String targetName = context.get(playerArg);
            Player target = MinecraftServer.getConnectionManager().getOnlinePlayerByUsername(targetName);

            if (!(target instanceof CustomPlayer customTarget)) {
                sender.sendMessage(Component.text("Player not found!", NamedTextColor.RED));
                return;
            }

            PlayerRank rank = customTarget.getRank();

            sender.sendMessage(Component.text(target.getUsername() + "'s rank: ", NamedTextColor.YELLOW)
                    .append(rank.getDisplayName())
                    .append(Component.text(" (Priority: " + rank.getPriority() + ")", NamedTextColor.GRAY)));
        }, playerArg);

        // /rank <player> set <rank> - Set player's rank
        addSyntax((sender, context) -> {
            String targetName = context.get(playerArg);
            Player target = MinecraftServer.getConnectionManager().getOnlinePlayerByUsername(targetName);

            if (!(target instanceof CustomPlayer customTarget)) {
                sender.sendMessage(Component.text("Player not found!", NamedTextColor.RED));
                return;
            }

            String rankName = context.get(rankArg).toUpperCase();
            PlayerRank newRank;

            try {
                newRank = PlayerRank.valueOf(rankName);
            } catch (IllegalArgumentException e) {
                sender.sendMessage(Component.text("Invalid rank!", NamedTextColor.RED));
                return;
            }

            // Additional permission check for specific rank
            if (sender instanceof CustomPlayer player) {
                if (!player.hasPermission("rank.give." + rankName.toLowerCase()) &&
                    !player.hasPermission("rank.give.*")) {
                    sender.sendMessage(Component.text("You don't have permission to give " + rankName + " rank!", NamedTextColor.RED));
                    return;
                }
            }

            customTarget.setRank(newRank);

            // Save to database
            if (customTarget.getPlayerData() != null && mongoDBHandler != null) {
                customTarget.getPlayerData().setRank(newRank);
                mongoDBHandler.savePlayerData(customTarget.getPlayerData());
            }

            sender.sendMessage(Component.text("Set " + target.getUsername() + "'s rank to ", NamedTextColor.GREEN)
                    .append(newRank.getDisplayName()));

            target.sendMessage(Component.text("Your rank has been updated to ", NamedTextColor.YELLOW)
                    .append(newRank.getDisplayName()));

        }, playerArg, setArg, rankArg);

        setDefaultExecutor((sender, context) -> {
            sender.sendMessage(Component.text("Usage: /rank <player> [set <rank>]", NamedTextColor.RED));
        });
    }
}


