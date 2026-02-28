package dev.proplayer919.chasmic.helpers;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;

public class BlockTracer {
    public static BlockTraceResult trace(Instance instance, Pos start, double maxDistance) {
        // Get the direction from the player's look angle
        Vec direction = start.direction();

        Pos currentPos = start;
        Pos previousPos = start;
        double distanceTraveled = 0;

        // Step through the space in small increments
        double stepSize = 0.1; // Fine-grained stepping for accurate block detection

        while (distanceTraveled < maxDistance) {
            // Move to the next position
            currentPos = currentPos.add(direction.x() * stepSize, direction.y() * stepSize, direction.z() * stepSize);
            distanceTraveled += stepSize;

            // Check if current position has a solid block
            Block blockAtPosition = instance.getBlock(currentPos.blockX(), currentPos.blockY(), currentPos.blockZ(), Block.Getter.Condition.TYPE);

            // If we hit a solid block, return the previous position
            if (blockAtPosition != null && !blockAtPosition.isAir()) {
                return new BlockTraceResult(true, blockAtPosition, previousPos);
            }

            previousPos = currentPos;
        }

        // No block hit, return final position
        return new BlockTraceResult(false, null, currentPos);
    }
}
