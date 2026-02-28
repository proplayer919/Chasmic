package dev.proplayer919.chasmic.helpers;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.instance.block.Block;

public class BlockTraceResult {
    public boolean hit;
    public Block block;
    public Pos hitPosition;

    public BlockTraceResult(boolean hit, Block block, Pos hitPosition) {
        this.hit = hit;
        this.block = block;
        this.hitPosition = hitPosition;
    }
}
