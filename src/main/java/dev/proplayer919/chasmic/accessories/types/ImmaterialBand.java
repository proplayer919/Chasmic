package dev.proplayer919.chasmic.accessories.types;

import dev.proplayer919.chasmic.PlayerHeads;
import dev.proplayer919.chasmic.Rarity;
import dev.proplayer919.chasmic.accessories.Accessory;
import net.minestom.server.item.Material;

import java.util.Collections;

public class ImmaterialBand extends Accessory {
    public ImmaterialBand() {
        super("immaterial_band", "Immaterial Band", "A powerful accessory granting access to The Shallows.", Rarity.MYTHIC, Collections.emptyList(), Material.PLAYER_HEAD, PlayerHeads.NECKLACE);
    }
}
