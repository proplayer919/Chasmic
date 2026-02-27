package dev.proplayer919.chasmic;

import dev.proplayer919.chasmic.command.CommandRegistry;
import dev.proplayer919.chasmic.data.MongoDBHandler;
import dev.proplayer919.chasmic.module.*;
import dev.proplayer919.chasmic.npc.NPC;
import net.minestom.server.Auth;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.ChunkRange;
import net.minestom.server.coordinate.Point;
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
    private static MongoDBHandler mongoDBHandler;

    static void main(String[] args) {
        // Initialize the server
        MinecraftServer minecraftServer = MinecraftServer.init(new Auth.Online());

        MinecraftServer.setBrandName("ChasmicMC");

        // Register custom player provider
        MinecraftServer.getConnectionManager().setPlayerProvider(CustomPlayer::new);

        // Initialize MongoDB handler
        mongoDBHandler = new MongoDBHandler(); // Uses default: mongodb://localhost:27017, database "chasmic"

        // Add shutdown hook to close MongoDB connection
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Closing MongoDB connection...");
            mongoDBHandler.close();
        }));

        // Initialize modules
        ModuleManager moduleManager = new ModuleManager()
                .register(new PlayerDataModule(mongoDBHandler))  // Load player data from MongoDB
                .register(new ChatModule())
                .register(new ServerListPingModule())
                .register(new TabListModule())
                .register(new WelcomeMessageModule("Welcome to ChasmicMC!")); // Send welcome to new players

        // Register commands
        CommandRegistry.registerCommands(mongoDBHandler);

        InstanceContainer spawn = getInstanceContainer();

        GlobalEventHandler globalEventHandler = MinecraftServer.getGlobalEventHandler();

        // Attach all modules to the global event handler
        moduleManager.attachAll(globalEventHandler);

        globalEventHandler.addListener(AsyncPlayerConfigurationEvent.class, event -> {
            event.setSpawningInstance(spawn);
        });

        globalEventHandler.addListener(PlayerSpawnEvent.class, event -> {
            Pos spawnPos = new Pos(0.5, 50, 0.5);
            event.getPlayer().teleport(spawnPos);

            NPC npc = new NPC(UUID.randomUUID(), "NPC", Objects.requireNonNull(PlayerSkin.fromUsername("Notch")), 1, false);

            npc.setInstance(spawn, spawnPos);
            npc.addPlayerViewer(event.getPlayer());
        });


        // Start the server
        minecraftServer.start("0.0.0.0", 25565);
    }

    private static @NonNull InstanceContainer getInstanceContainer() {
        InstanceManager instanceManager = MinecraftServer.getInstanceManager();

        InstanceContainer spawn = instanceManager.createInstanceContainer();
        spawn.setChunkSupplier(LightingChunk::new);

        spawn.setGenerator(unit -> {
            final Point start = unit.absoluteStart();
            final Point size = unit.size();

            // Fill every 5th layer with stone
            for (int y = 0; y < size.y(); y++) {
                for (int x = 0; x < size.x(); x++) {
                    for (int z = 0; z < size.z(); z++) {
                        if (y % 5 == 0) {
                            unit.modifier().setBlock(start.add(x, y, z), Block.STONE);
                        }
                    }
                }
            }
        });

        var chunks = new ArrayList<CompletableFuture<Chunk>>();
        ChunkRange.chunksInRange(0, 0, 32, (x, z) -> chunks.add(spawn.loadChunk(x, z)));

        CompletableFuture.runAsync(() -> {
            CompletableFuture.allOf(chunks.toArray(CompletableFuture[]::new)).join();
            LightingChunk.relight(spawn, spawn.getChunks());
        });

        return spawn;
    }
}
