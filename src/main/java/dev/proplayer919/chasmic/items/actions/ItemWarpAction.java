package dev.proplayer919.chasmic.items.actions;

import dev.proplayer919.chasmic.CustomPlayer;
import dev.proplayer919.chasmic.helpers.BlockTraceResult;
import dev.proplayer919.chasmic.helpers.BlockTracer;
import dev.proplayer919.chasmic.items.ItemActionHandler;
import net.minestom.server.coordinate.Pos;

public class ItemWarpAction implements ItemActionHandler {
    @Override
    public void handleAction(CustomPlayer customPlayer) {
        Pos position = customPlayer.getPosition();

        // Find the Pos 10 blocks forward in the direction the player is facing
        Pos forwardPosition = position.add(position.direction().mul(10));

        // Trace the line from the player's current position to the forward position to find the first solid block
        BlockTraceResult traceResult = BlockTracer.trace(customPlayer.getInstance(), position, 10);

        // Teleport the player
        if (traceResult.hit) {
            // If we hit a block, teleport to the position just before the block
            customPlayer.teleport(traceResult.hitPosition);
        } else {
            // If we didn't hit any block, teleport to the forward position
            customPlayer.teleport(forwardPosition);
        }
    }
}
