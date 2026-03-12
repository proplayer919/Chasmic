package dev.proplayer919.chasmic;

import dev.proplayer919.chasmic.accessories.AccessoryRegistry;
import dev.proplayer919.chasmic.command.CommandRegistry;
import dev.proplayer919.chasmic.data.MongoDBHandler;
import dev.proplayer919.chasmic.items.CustomItemRegistry;
import dev.proplayer919.chasmic.items.ItemActionRegistry;
import dev.proplayer919.chasmic.player.CustomPlayer;
import dev.proplayer919.chasmic.service.module.*;
import dev.proplayer919.chasmic.service.module.*;
import dev.proplayer919.chasmic.service.ModuleManager;
import dev.proplayer919.chasmic.service.ServiceContainer;
import dev.proplayer919.chasmic.service.ServiceInitializationException;
import dev.proplayer919.chasmic.service.module.*;
import dev.proplayer919.chasmic.time.ChasmicTime;
import dev.proplayer919.chasmic.npc.NPC;
import dev.proplayer919.chasmic.player.friend.FriendManager;
import dev.proplayer919.chasmic.player.punishment.PunishmentManager;
import lombok.Getter;
import net.minestom.server.Auth;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.ChunkRange;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.PlayerSkin;
import net.minestom.server.event.GlobalEventHandler;
import net.minestom.server.instance.Chunk;
import net.minestom.server.instance.InstanceContainer;
import net.minestom.server.instance.InstanceManager;
import net.minestom.server.instance.LightingChunk;
import net.minestom.server.instance.block.Block;
import net.minestom.server.timer.TaskSchedule;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    @Getter
    private static final Pos spawnPos = new Pos(0.5, 41, 0.5);

    @Getter
    private static InstanceContainer spawnInstance;

    @Getter
    private static NPC npc;

    @Getter
    private static ServiceContainer serviceContainer;

    static void main() {
        // Initialize the server
        MinecraftServer minecraftServer = MinecraftServer.init(new Auth.Online());

        MinecraftServer.setBrandName("Chasmic");

        // Register custom player provider
        MinecraftServer.getConnectionManager().setPlayerProvider(CustomPlayer::new);

        // Initialize MongoDB handler
        MongoDBHandler mongoDBHandler = new MongoDBHandler();

        // Initialize service container with dependency-based ordering
        try {
            serviceContainer = new ServiceContainer(mongoDBHandler);
        } catch (ServiceInitializationException e) {
            logger.error("Service initialization failed. Shutting down server startup.", e);
            mongoDBHandler.close();
            System.exit(1);
            return;
        }

        // Add shutdown hook to close MongoDB connection
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            logger.info("Closing MongoDB connection...");
            mongoDBHandler.close();
        }));

        // Get service references for module initialization
        ItemActionRegistry itemActionRegistry = serviceContainer.getItemActionRegistry();
        PunishmentManager punishmentManager = serviceContainer.getPunishmentManager();
        CustomItemRegistry customItemRegistry = serviceContainer.getCustomItemRegistry();
        AccessoryRegistry accessoryRegistry = serviceContainer.getAccessoryRegistry();
        FriendManager friendManager = serviceContainer.getFriendManager();

        // Initialize modules with proper dependency injection
        ModuleManager moduleManager = new ModuleManager()
                .register(new BanCheckModule(punishmentManager)) // Check for bans on login
                .register(new PlayerDataModule(mongoDBHandler))  // Load player data from MongoDB
                .register(new FriendsModule(friendManager)) // Notify friends when players join and leave
                .register(new ChatModule()) // Handle chat formatting and commands
                .register(new ServerListPingModule()) // Custom MOTD and player count
                .register(new EntityAttackModule())  // Creatures attacking players
                .register(new PlayerAttackModule())  // Players attacking creatures
                .register(new ItemActionModule(itemActionRegistry)) // Handle custom item actions
                .register(new TabListModule()) // Update tab list on player spawn
                .register(new PlayerLocationModule()) // Track player locations and fire events on location enter/exit
                .register(new PlayerSpawnModule()) // Handle player spawning and teleporting to spawn
                .register(new PlayerEquipmentModule()) // Monitor equipment changes and invalidate stat caches
                .register(new MenuItemModule(mongoDBHandler, accessoryRegistry)) // Handle menu items for all players
                .register(new ItemFoodModule()) // Handle accessories and their effects
                .register(new WorldProtectModule()); // Prevent block breaking and placing in the spawn area


        // Register commands
        CommandRegistry.registerCommands(mongoDBHandler, punishmentManager,
            customItemRegistry, accessoryRegistry, friendManager);

        // Create spawn instance and preload chunks
        spawnInstance = createSpawnInstance();

        // Schedule a task to continuously sync instance time with Chasmic time
        // Uses smooth interpolation with millisecond precision for seamless transitions
        MinecraftServer.getSchedulerManager().submitTask(() -> {
            long interpolatedTime = ChasmicTime.getInterpolatedInstanceTime();
            spawnInstance.setTime(interpolatedTime);
            return TaskSchedule.tick(1); // Update every tick for smooth interpolation
        });

        npc = new NPC(UUID.randomUUID(), "NPC_Test_Guy", Objects.requireNonNull(PlayerSkin.fromUsername("jeb_")), 1, false);
        npc.setInstance(spawnInstance, spawnPos);

        GlobalEventHandler globalEventHandler = MinecraftServer.getGlobalEventHandler();

        // Attach all modules to the global event handler
        moduleManager.attachAll(globalEventHandler);

        // Start the server
        minecraftServer.start("0.0.0.0", 25565);
    }

    private static @NonNull InstanceContainer createSpawnInstance() {
        InstanceContainer spawn = getSpawn();

        // Load chunks synchronously before server starts
        var chunks = new ArrayList<CompletableFuture<Chunk>>();
        ChunkRange.chunksInRange(0, 0, 32, (x, z) -> chunks.add(spawn.loadChunk(x, z)));

        // Wait for all chunks to load
        CompletableFuture.allOf(chunks.toArray(CompletableFuture[]::new)).join();

        // Relight the chunks
        LightingChunk.relight(spawn, spawn.getChunks());

        return spawn;
    }

    private static @NonNull InstanceContainer getSpawn() {
        InstanceManager instanceManager = MinecraftServer.getInstanceManager();

        InstanceContainer spawn = instanceManager.createInstanceContainer();
        spawn.setChunkSupplier(LightingChunk::new);

        spawn.setGenerator(unit -> unit.modifier().fillHeight(0, 40, Block.STONE));

        // Disable automatic time progression so our Chasmic time scheduler has full control
        spawn.setTimeRate(0);

        // Set instance time to match Chasmic time
        long interpolatedTime = ChasmicTime.getInterpolatedInstanceTime();
        spawn.setTime(interpolatedTime);

        return spawn;
    }
}
