package dev.proplayer919.chasmic.command;

import dev.proplayer919.chasmic.CustomPlayer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.arguments.ArgumentWord;

/**
 * /media command for toggling recording and streaming status
 * Permissions: command.media.record, command.media.stream
 */
public class MediaCommand extends Command {

    public MediaCommand() {
        super("media");

        setCondition((sender, commandString) -> {
            if (sender instanceof CustomPlayer player) {
                // Allow command if player is not yet initialized (to avoid red text)
                // Actual permission check happens in executor
                if (!player.isInitialized()) {
                    return true;
                }
                return player.hasPermission("command.media.record") || player.hasPermission("command.media.stream");
            }
            return true; // Console always has permission
        });

        setDefaultExecutor((sender, context) -> {
            sender.sendMessage(Component.text("Usage: /media <record|stream>", NamedTextColor.RED));
        });

        // Create subcommand argument with suggestions
        ArgumentWord subcommand = ArgumentType.Word("action")
                .from("record", "stream");

        // /media record
        addSyntax((sender, context) -> {
            if (!(sender instanceof CustomPlayer player)) {
                sender.sendMessage(Component.text("Only players can use this command!", NamedTextColor.RED));
                return;
            }

            String action = context.get(subcommand);

            if (action.equals("record")) {
                if (!player.hasPermission("command.media.record")) {
                    sender.sendMessage(Component.text("You don't have permission to toggle recording!", NamedTextColor.RED));
                    return;
                }

                boolean newRecordingState = !player.isRecording();
                player.setRecording(newRecordingState);

                if (newRecordingState) {
                    // If enabling recording and streaming is on, disable streaming
                    if (player.isStreaming()) {
                        player.setStreaming(false);
                        sender.sendMessage(Component.text("● ", NamedTextColor.RED)
                                .append(Component.text("Recording mode enabled! ", NamedTextColor.GREEN))
                                .append(Component.text("(Streaming disabled)", NamedTextColor.GRAY)));
                    } else {
                        sender.sendMessage(Component.text("● ", NamedTextColor.RED)
                                .append(Component.text("Recording mode enabled!", NamedTextColor.GREEN)));
                    }
                } else {
                    sender.sendMessage(Component.text("Recording mode disabled.", NamedTextColor.GRAY));
                }

                // Update tab list to show the indicator
                player.updateTabList();

            } else if (action.equals("stream")) {
                if (!player.hasPermission("command.media.stream")) {
                    sender.sendMessage(Component.text("You don't have permission to toggle streaming!", NamedTextColor.RED));
                    return;
                }

                boolean newStreamingState = !player.isStreaming();
                player.setStreaming(newStreamingState);

                if (newStreamingState) {
                    // If enabling streaming and recording is on, disable recording
                    if (player.isRecording()) {
                        player.setRecording(false);
                        sender.sendMessage(Component.text("● ", NamedTextColor.LIGHT_PURPLE)
                                .append(Component.text("Streaming mode enabled! ", NamedTextColor.GREEN))
                                .append(Component.text("(Recording disabled)", NamedTextColor.GRAY)));
                    } else {
                        sender.sendMessage(Component.text("● ", NamedTextColor.LIGHT_PURPLE)
                                .append(Component.text("Streaming mode enabled!", NamedTextColor.GREEN)));
                    }
                } else {
                    sender.sendMessage(Component.text("Streaming mode disabled.", NamedTextColor.GRAY));
                }

                // Update tab list to show the indicator
                player.updateTabList();
            }
        }, subcommand);
    }
}

