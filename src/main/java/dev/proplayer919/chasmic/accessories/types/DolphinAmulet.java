package dev.proplayer919.chasmic.accessories.types;

import dev.proplayer919.chasmic.PlayerHeads;
import dev.proplayer919.chasmic.Rarity;
import dev.proplayer919.chasmic.accessories.Accessory;
import dev.proplayer919.chasmic.player.PlayerStat;
import dev.proplayer919.chasmic.player.PlayerStatBonus;
import net.minestom.server.item.Material;

import java.util.List;

public class DolphinAmulet extends Accessory {
    public DolphinAmulet() {
        super("dolphin_amulet", "Dolphin Amulet", "An amulet forged from the souls of dolphins, providing a passive defense boost.", Rarity.EPIC, List.of(new PlayerStatBonus(PlayerStat.DEFENSE, 25.0f)), Material.PLAYER_HEAD, PlayerHeads.DOLPHIN);
    }
}
