package dev.proplayer919.chasmic.helpers;

import net.minestom.server.component.DataComponents;
import net.minestom.server.entity.PlayerSkin;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.network.player.ResolvableProfile;

public abstract class PlayerHeadCreator {
    public static ItemStack.Builder getHeadBuilder(String textureData) {
        return ItemStack.builder(Material.PLAYER_HEAD)
                .set(DataComponents.PROFILE, new ResolvableProfile(new PlayerSkin(textureData, "")));
    }

    public static ItemStack getHead(String textureData) {
        return getHeadBuilder(textureData).build();
    }
}
