package dev.proplayer919.chasmic.accessories.types;

import dev.proplayer919.chasmic.PlayerHeads;
import dev.proplayer919.chasmic.PlayerStat;
import dev.proplayer919.chasmic.PlayerStatBonus;
import dev.proplayer919.chasmic.Rarity;
import dev.proplayer919.chasmic.accessories.Accessory;
import net.minestom.server.item.Material;

import java.util.List;

public class TurtleSprite extends Accessory {
    public TurtleSprite() {
        super("turtle_sprite", "Turtle Sprite", "A speed-increasing accessory.", Rarity.RARE, List.of(new PlayerStatBonus(PlayerStat.SPEED, 25.0f)), Material.PLAYER_HEAD, PlayerHeads.BABY_TURTLE);
    }
}
