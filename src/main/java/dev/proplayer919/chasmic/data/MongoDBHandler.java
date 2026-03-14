package dev.proplayer919.chasmic.data;

import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.ReplaceOptions;
import dev.proplayer919.chasmic.player.PlayerRank;
import lombok.Getter;
import org.bson.UuidRepresentation;
import org.bson.codecs.configuration.CodecRegistries;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Pattern;

/**
 * Handles MongoDB connections and player data operations
 */
@Getter
public class MongoDBHandler {
    private static final Logger logger = LoggerFactory.getLogger(MongoDBHandler.class);

    private final MongoClient mongoClient;
    private final MongoDatabase database;
    private final MongoCollection<PlayerData> playerCollection;

    /**
     * Creates a new MongoDB handler with the given connection string
     * @param connectionString MongoDB connection string (e.g., "mongodb://localhost:27017")
     * @param databaseName Name of the database to use
     */
    public MongoDBHandler(String connectionString, String databaseName) {
        // Configure codec registry for POJO support
        CodecRegistry pojoCodecRegistry = CodecRegistries.fromRegistries(
                MongoClientSettings.getDefaultCodecRegistry(),
                CodecRegistries.fromCodecs(new BigIntegerCodec()),
                CodecRegistries.fromProviders(PojoCodecProvider.builder().automatic(true).build())
        );

        // Create MongoDB client with UUID representation
        MongoClientSettings settings = MongoClientSettings.builder()
                .applyConnectionString(new com.mongodb.ConnectionString(connectionString))
                .uuidRepresentation(UuidRepresentation.STANDARD)
                .codecRegistry(pojoCodecRegistry)
                .build();

        this.mongoClient = MongoClients.create(settings);
        this.database = mongoClient.getDatabase(databaseName);
        this.playerCollection = database.getCollection("players", PlayerData.class);
    }

    /**
     * Default constructor using localhost MongoDB
     */
    public MongoDBHandler() {
        this("mongodb://localhost:27017", "chasmic");
    }

    /**
     * Loads player data from the database asynchronously
     * @param uuid The player's UUID
     * @return CompletableFuture with PlayerData, or null if not found
     */
    public CompletableFuture<PlayerData> loadPlayerData(UUID uuid) {
        return CompletableFuture.supplyAsync(() -> playerCollection.find(Filters.eq("_id", uuid)).first());
    }

    /**
     * Loads player data by username asynchronously (case-insensitive)
     * @param username The player's username
     * @return CompletableFuture with PlayerData, or null if not found
     */
    public CompletableFuture<PlayerData> loadPlayerDataByUsername(String username) {
        Pattern usernamePattern = Pattern.compile("^" + Pattern.quote(username) + "$", Pattern.CASE_INSENSITIVE);
        return CompletableFuture.supplyAsync(() -> playerCollection.find(Filters.regex("username", usernamePattern)).first());
    }

    /**
     * Loads multiple player records by UUID asynchronously
     * @param uuids UUIDs to load
     * @return CompletableFuture with matching player data records
     */
    public CompletableFuture<List<PlayerData>> loadPlayerDataByUuids(List<UUID> uuids) {
        if (uuids == null || uuids.isEmpty()) {
            return CompletableFuture.completedFuture(Collections.emptyList());
        }

        return CompletableFuture.supplyAsync(() -> playerCollection.find(Filters.in("_id", uuids)).into(new ArrayList<>()));
    }

    /**
     * Saves player data to the database asynchronously
     * @param playerData The player data to save
     * @return CompletableFuture that completes when save is done
     */
    public CompletableFuture<Void> savePlayerData(PlayerData playerData) {
        return CompletableFuture.runAsync(() -> playerCollection.replaceOne(
                Filters.eq("_id", playerData.getUuid()),
                playerData,
                new ReplaceOptions().upsert(true)
        ));
    }

    /**
     * Creates a new player entry in the database
     * @param uuid Player's UUID
     * @param username Player's username
     * @return The created PlayerData
     */
    public PlayerData createNewPlayer(UUID uuid, String username) {
        long currentTime = System.currentTimeMillis();

        PlayerData playerData = new PlayerData();
        playerData.setUuid(uuid);
        playerData.setUsername(username);
        playerData.setRankId(PlayerRank.DEFAULT.getId());
        playerData.setFirstJoinTimestamp(currentTime);
        playerData.setLastJoinTimestamp(currentTime);
        playerData.setNew(true);
        playerData.setSchemaVersion(5);
        playerData.ensureSocialIntegrity();

        PlayerProfileData initialProfile = playerData.createProfileForSlot(1);
        playerData.setActiveProfileId(initialProfile.getProfileId());

        return playerData;
    }

    /**
     * Loads or creates player data
     * @param uuid Player's UUID
     * @param username Player's username
     * @return CompletableFuture with PlayerData (existing or newly created)
     */
    public CompletableFuture<PlayerData> loadOrCreatePlayerData(UUID uuid, String username) {
        return loadPlayerData(uuid).thenCompose(data -> {
            if (data == null) {
                // Player doesn't exist, create new entry
                PlayerData newData = createNewPlayer(uuid, username);
                // Save synchronously for new players to ensure data exists before spawn
                return savePlayerData(newData).thenApply(v -> newData);
            } else {
                // Player exists, update last join and mark as not new
                data.setUsername(username);
                data.setLastJoinTimestamp(System.currentTimeMillis());
                data.setNew(false);
                boolean migrated = migrateLegacySchemaIfNeeded(data);

                // Save asynchronously and handle errors
                return savePlayerData(data)
                        .thenApply(v -> data)
                        .exceptionally(throwable -> {
                            if (migrated) {
                                logger.error("Failed to save migrated player data for {}", username, throwable);
                            } else {
                                logger.error("Failed to save player data for {}", username, throwable);
                            }
                            return data; // Return data anyway, just log the error
                        });
            }
        });
    }

    private boolean migrateLegacySchemaIfNeeded(PlayerData data) {
        boolean hasProfiles = data.getProfiles() != null && !data.getProfiles().isEmpty();
        boolean changed = false;

        if (!hasProfiles || data.getSchemaVersion() < 2) {
            // Create fresh profile for all users (reset everyone)
            PlayerProfileData newProfile = new PlayerProfileData(PlayerProfileData.buildIdForSlot(1));

            List<PlayerProfileData> migratedProfiles = new ArrayList<>();
            migratedProfiles.add(newProfile);
            data.setProfiles(migratedProfiles);
            data.setActiveProfileId(newProfile.getProfileId());
            changed = true;
        }

        String beforeActiveProfile = data.getActiveProfileId();
        data.ensureProfileIntegrity();
        if (beforeActiveProfile == null || !beforeActiveProfile.equals(data.getActiveProfileId())) {
            changed = true;
        }

        int beforeFriendCount = data.getFriends() != null ? data.getFriends().size() : -1;
        data.ensureSocialIntegrity();
        if (beforeFriendCount != data.getFriends().size()) {
            changed = true;
        }

        if (data.getSchemaVersion() < 5) {
            data.setSchemaVersion(5);
            changed = true;
        }

        return changed;
    }

    /**
     * Updates a player's rank in the database
     * @param uuid Player's UUID
     * @param rank New rank
     * @return CompletableFuture that completes when update is done
     */
    public CompletableFuture<Void> updatePlayerRank(UUID uuid, PlayerRank rank) {
        return loadPlayerData(uuid).thenCompose(data -> {
            if (data != null) {
                data.setRank(rank);
                return savePlayerData(data);
            }
            return CompletableFuture.completedFuture(null);
        });
    }

    /**
     * Adds a custom permission to a player in the database
     * @param uuid Player's UUID
     * @param permission Permission to add
     * @return CompletableFuture that completes when update is done
     */
    public CompletableFuture<Void> addPlayerPermission(UUID uuid, String permission) {
        return loadPlayerData(uuid).thenCompose(data -> {
            if (data != null) {
                if (!data.getCustomPermissions().contains(permission)) {
                    data.getCustomPermissions().add(permission);
                }
                return savePlayerData(data);
            }
            return CompletableFuture.completedFuture(null);
        });
    }

    /**
     * Removes a custom permission from a player in the database
     * @param uuid Player's UUID
     * @param permission Permission to remove
     * @return CompletableFuture that completes when update is done
     */
    public CompletableFuture<Void> removePlayerPermission(UUID uuid, String permission) {
        return loadPlayerData(uuid).thenCompose(data -> {
            if (data != null) {
                data.getCustomPermissions().remove(permission);
                return savePlayerData(data);
            }
            return CompletableFuture.completedFuture(null);
        });
    }

    /**
     * Closes the MongoDB connection
     */
    public void close() {
        if (mongoClient != null) {
            mongoClient.close();
        }
    }

}
