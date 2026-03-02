package dev.proplayer919.chasmic.helpers;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.instance.block.Block;

public record BlockTraceResult(boolean hit, Block block, Pos hitPosition) {
}
