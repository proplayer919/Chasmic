package dev.proplayer919.chasmic;

import dev.proplayer919.chasmic.accessories.AccessoryRegistry;
import dev.proplayer919.chasmic.command.CommandRegistry;
import dev.proplayer919.chasmic.data.MongoDBHandler;
import dev.proplayer919.chasmic.entities.creatures.TestZombie;
import dev.proplayer919.chasmic.helpers.PlayerHeadCreator;
import dev.proplayer919.chasmic.items.CustomItemRegistry;
import dev.proplayer919.chasmic.items.ItemActionRegistry;
import dev.proplayer919.chasmic.module.*;
import dev.proplayer919.chasmic.npc.NPC;
import dev.proplayer919.chasmic.punishment.PunishmentManager;
import lombok.Getter;
import net.minestom.server.Auth;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.ChunkRange;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.PlayerSkin;
import net.minestom.server.event.GlobalEventHandler;
import net.minestom.server.event.player.AsyncPlayerConfigurationEvent;
import net.minestom.server.event.player.PlayerSpawnEvent;
import net.minestom.server.instance.Chunk;
import net.minestom.server.instance.InstanceContainer;
import net.minestom.server.instance.InstanceManager;
import net.minestom.server.instance.LightingChunk;
import net.minestom.server.instance.block.Block;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class Main {
    @Getter
    private static MongoDBHandler mongoDBHandler;

    @Getter
    private static ItemActionRegistry itemActionRegistry;

    @Getter
    private static CustomItemRegistry customItemRegistry;

    @Getter
    private static AccessoryRegistry accessoryRegistry;

    @Getter
    private static PunishmentManager punishmentManager;

    private final static Pos spawnPos = new Pos(0.5, 41, 0.5);


    static void main(String[] args) {
        // Initialize the server
        MinecraftServer minecraftServer = MinecraftServer.init(new Auth.Online());

        MinecraftServer.setBrandName("ChasmicMC");

        // Register custom player provider
        MinecraftServer.getConnectionManager().setPlayerProvider(CustomPlayer::new);

        // Initialize MongoDB handler
        mongoDBHandler = new MongoDBHandler(); // Uses default: mongodb://localhost:27017, database "chasmic"

        // Initialize punishment manager
        punishmentManager = new PunishmentManager(mongoDBHandler);

        // Add shutdown hook to close MongoDB connection
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Closing MongoDB connection...");
            mongoDBHandler.close();
        }));

        // Initialize modules
        ModuleManager moduleManager = new ModuleManager()
                .register(new BanCheckModule(punishmentManager)) // Check for bans on login
                .register(new PlayerDataModule(mongoDBHandler))  // Load player data from MongoDB
                .register(new ChatModule()) // Handle chat formatting and commands
                .register(new ServerListPingModule()) // Custom MOTD and player count
                .register(new EntityAttackModule())  // Creatures attacking players
                .register(new PlayerAttackModule())  // Players attacking creatures
                .register(new ItemActionModule()) // Handle custom item actions
                .register(new TabListModule()); // Update tab list on player spawn

        // Initialize registries
        itemActionRegistry = new ItemActionRegistry();
        customItemRegistry = new CustomItemRegistry();
        accessoryRegistry = new AccessoryRegistry();

        // Register commands
        CommandRegistry.registerCommands(mongoDBHandler, punishmentManager);

        InstanceContainer spawn = getInstanceContainer();

        NPC npc = new NPC(UUID.randomUUID(), "NPC", Objects.requireNonNull(PlayerSkin.fromUsername("Notch")), 1, false);

        npc.setInstance(spawn, spawnPos);

        GlobalEventHandler globalEventHandler = MinecraftServer.getGlobalEventHandler();

        // Attach all modules to the global event handler
        moduleManager.attachAll(globalEventHandler);

        globalEventHandler.addListener(AsyncPlayerConfigurationEvent.class, event -> {
            event.setSpawningInstance(spawn);
        });

        globalEventHandler.addListener(PlayerSpawnEvent.class, event -> {
            event.getPlayer().teleport(spawnPos);
            event.getPlayer().setRespawnPoint(spawnPos);

            TestZombie zombie = new TestZombie();
            zombie.setInstance(spawn, spawnPos);

            npc.addPlayerViewer(event.getPlayer());
        });


        // Start the server
        minecraftServer.start("0.0.0.0", 25565);
    }

    private static @NonNull InstanceContainer getInstanceContainer() {
        InstanceManager instanceManager = MinecraftServer.getInstanceManager();

        InstanceContainer spawn = instanceManager.createInstanceContainer();
        spawn.setChunkSupplier(LightingChunk::new);

        spawn.setGenerator(unit -> unit.modifier().fillHeight(0, 40, Block.STONE));

        // Load chunks synchronously before server starts
        var chunks = new ArrayList<CompletableFuture<Chunk>>();
        ChunkRange.chunksInRange(0, 0, 32, (x, z) -> chunks.add(spawn.loadChunk(x, z)));

        // Wait for all chunks to load
        CompletableFuture.allOf(chunks.toArray(CompletableFuture[]::new)).join();

        // Relight the chunks
        LightingChunk.relight(spawn, spawn.getChunks());

        return spawn;
    }
}
