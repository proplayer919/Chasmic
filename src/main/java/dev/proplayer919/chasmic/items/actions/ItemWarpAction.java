package dev.proplayer919.chasmic.items.actions;

import dev.proplayer919.chasmic.CustomPlayer;
import dev.proplayer919.chasmic.helpers.BlockTraceResult;
import dev.proplayer919.chasmic.helpers.BlockTracer;
import dev.proplayer919.chasmic.items.ItemActionHandler;
import dev.proplayer919.chasmic.items.ItemActionResult;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.coordinate.Pos;

public class ItemWarpAction implements ItemActionHandler {
    @Override
    public ItemActionResult handleAction(CustomPlayer customPlayer) {
        Pos position = customPlayer.getPosition();

        // Find the Pos 10 blocks forward in the direction the player is facing
        Pos forwardPosition = position.add(position.direction().mul(10));

        // Trace the line from the player's current position to the forward position to find the first solid block
        BlockTraceResult traceResult = BlockTracer.trace(customPlayer.getInstance(), position, 10);

        // Teleport the player
        if (!traceResult.hit()) {
            // If we didn't hit any block, teleport to the forward position
            customPlayer.teleport(forwardPosition);

            return new ItemActionResult(true);
        } else {
            // If we hit a block, tell the player that they can't teleport there
            customPlayer.sendMessage(Component.text("You can't teleport there! There's a block in the way.").color(NamedTextColor.RED));

            return new ItemActionResult(false);
        }
    }
}
