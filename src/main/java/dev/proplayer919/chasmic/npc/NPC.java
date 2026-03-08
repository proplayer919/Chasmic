package dev.proplayer919.chasmic.npc;

import dev.proplayer919.chasmic.player.CustomPlayer;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.minestom.server.MinecraftServer;
import net.minestom.server.collision.BoundingBox;
import net.minestom.server.entity.*;
import net.minestom.server.entity.attribute.Attribute;
import net.minestom.server.entity.metadata.LivingEntityMeta;
import net.minestom.server.event.Event;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.entity.EntityDeathEvent;
import net.minestom.server.instance.block.Block;
import net.minestom.server.network.packet.server.SendablePacket;
import net.minestom.server.network.packet.server.play.PlayerInfoRemovePacket;
import net.minestom.server.network.packet.server.play.PlayerInfoUpdatePacket;
import org.jspecify.annotations.NonNull;

import java.util.*;

@Getter
public class NPC extends LivingEntity {
    private final UUID uuid;
    private final String username;
    private final PlayerSkin skin;

    private final Collection<Player> botViewers = new HashSet<>();

    private final PlayerInfoUpdatePacket playerViewBotPacket;
    private final PlayerInfoRemovePacket playerRemoveBotPacket;

    private final EventNode<@NonNull Event> botEventNode;

    private final boolean canDie;

    public NPC(UUID uuid, String username, PlayerSkin skin, int order, boolean canDie) {
        super(EntityType.PLAYER, uuid);

        this.canDie = canDie;

        setHealth(20);

        getAttribute(Attribute.MOVEMENT_SPEED).setBaseValue(0.1);

        this.uuid = uuid;
        this.username = username;
        this.skin = skin;
        this.botEventNode = EventNode.all("bot-event-node-" + uuid);

        this.playerViewBotPacket = new PlayerInfoUpdatePacket(
                PlayerInfoUpdatePacket.Action.ADD_PLAYER,
                new PlayerInfoUpdatePacket.Entry(
                        uuid,
                        username,
                        List.of(
                                new PlayerInfoUpdatePacket.Property(
                                        "textures",
                                        skin.textures(),
                                        skin.signature()
                                )
                        ),
                        true,
                        0,
                        GameMode.SURVIVAL,
                        Component.text(username),
                        null,
                        order,
                        true
                )
        );

        this.playerRemoveBotPacket = new PlayerInfoRemovePacket(uuid);

        if (canDie) {
            eventNode().addListener(EntityDeathEvent.class, event -> {
                if (event.getEntity().getUuid().equals(uuid)) {
                    setFireTicks(0);
                    entityMeta.setOnFire(false);
                    setHealth(20);
                    refreshIsDead(false);
                    updatePose();
                }
            });
        }

        MinecraftServer.getGlobalEventHandler().addChild(botEventNode);
    }

    private CustomPlayer findNearestPlayer() {
        float minDistance = Float.MAX_VALUE;
        CustomPlayer nearestPlayer = null;
        for (Player player : getInstance().getPlayers()) {
            if (player.getUuid().equals(this.uuid)) {
                continue;
            }

            float distance = (float) getPosition().distance(player.getPosition());
            if (distance < minDistance) {
                minDistance = distance;
                nearestPlayer = (CustomPlayer) player;
            }
        }
        return nearestPlayer;
    }

    public void addPlayerViewer(Player viewer) {
        viewer.sendPacket(playerViewBotPacket);
        updateNewViewer(viewer);
        botViewers.add(viewer);
    }

    public void removePlayerViewer(Player viewer) {
        viewer.sendPacket(playerRemoveBotPacket);
        botViewers.remove(viewer);
    }

    public void sendPacketToViewers(@NonNull SendablePacket packet) {
        for (Player viewer : botViewers) {
            viewer.sendPacket(packet);
        }
    }

    @Override
    protected void updatePose() {
        EntityPose oldPose = getPose();
        EntityPose newPose;

        // Figure out their expected state
        var meta = getEntityMeta();
        if (meta.isFlyingWithElytra()) {
            newPose = EntityPose.FALL_FLYING;
        } else if (meta instanceof LivingEntityMeta livingMeta && livingMeta.getBedInWhichSleepingPosition() != null) {
            newPose = EntityPose.SLEEPING;
        } else if (meta.isSwimming()) {
            newPose = EntityPose.SWIMMING;
        } else if (meta instanceof LivingEntityMeta livingMeta && livingMeta.isInRiptideSpinAttack()) {
            newPose = EntityPose.SPIN_ATTACK;
        } else if (isSneaking()) {
            newPose = EntityPose.SNEAKING;
        } else {
            newPose = EntityPose.STANDING;
        }

        // Try to put them in their expected state, or the closest if they don't fit.
        if (canFitWithBoundingBox(newPose)) {
            // Use expected state
        } else if (canFitWithBoundingBox(EntityPose.SNEAKING)) {
            newPose = EntityPose.SNEAKING;
        } else if (canFitWithBoundingBox(EntityPose.SWIMMING)) {
            newPose = EntityPose.SWIMMING;
        } else {
            // If they can't fit anywhere, just use standing
            newPose = EntityPose.STANDING;
        }

        if (newPose != oldPose) setPose(newPose);
    }

    private boolean canFitWithBoundingBox(EntityPose pose) {
        BoundingBox bb = pose == EntityPose.STANDING ? boundingBox : BoundingBox.fromPose(pose);
        if (bb == null) return false;

        var position = getPosition();
        var iter = bb.getBlocks(getPosition());
        while (iter.hasNext()) {
            var pos = iter.next();
            Block block;
            try {
                block = instance.getBlock(pos.blockX(), pos.blockY(), pos.blockZ(), Block.Getter.Condition.TYPE);
            } catch (NullPointerException ignored) {
                block = null;
            }

            // Block was in unloaded chunk, no bounding box.
            if (block == null) continue;

            // For now just ignore scaffolding. It seems to have a dynamic bounding box, or is just parsed
            // incorrectly in MinestomDataGenerator.
            if (block.id() == Block.SCAFFOLDING.id()) continue;

            var hit = block.registry().collisionShape()
                    .intersectBox(position.sub(pos.blockX(), pos.blockY(), pos.blockZ()), bb);
            if (hit) return false;
        }

        return true;
    }

    @Override
    public double getEyeHeight() {
        return switch (getPose()) {
            case SLEEPING -> 0.2;
            case FALL_FLYING, SWIMMING, SPIN_ATTACK -> 0.4;
            case SNEAKING -> 1.27;
            default -> 1.62;
        };
    }

    protected void refreshAfterTeleport() {
        sendPacketToViewers(getSpawnPacket());
        sendPacketToViewers(getVelocityPacket());
        sendPacketToViewers(getMetadataPacket());
        sendPacketToViewers(getPropertiesPacket());
        sendPacketToViewers(getEquipmentsPacket());
    }
}
