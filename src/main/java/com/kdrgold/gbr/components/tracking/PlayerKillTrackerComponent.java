package com.kdrgold.gbr.components.tracking;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.Nonnull;

import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.kdrgold.gbr.config.ConditionalSpawnConfig;

/**
 * Component that tracks player kills of specific entity types with timestamps.
 * Used for conditional elite spawning based on kill counts within a time window.
 * 
 * @author kdrgold
 * @version 2.0.0
 */
public class PlayerKillTrackerComponent implements Component<EntityStore> {
    
    public static ComponentType<EntityStore, PlayerKillTrackerComponent> TYPE;
    
    // Codec for serialization
    public static final BuilderCodec<PlayerKillTrackerComponent> CODEC = BuilderCodec
            .builder(PlayerKillTrackerComponent.class, PlayerKillTrackerComponent::new)
            .build();
    
    // Track kill timestamps for each entity type
    private final Map<String, List<Long>> killTimestamps;
    
    public PlayerKillTrackerComponent() {
        this.killTimestamps = new ConcurrentHashMap<>();
    }
    
    /**
     * Records a kill for a specific entity type with timestamp
     * 
     * @param entityType The type of entity killed (e.g., "wolf", "grizzly_bear")
     * @param currentTime Current game time in ticks
     */
    public void recordKill(@Nonnull String entityType, long currentTime) {
        killTimestamps.computeIfAbsent(entityType, k -> new ArrayList<>()).add(currentTime);
    }
    
    /**
     * Gets the current kill count for an entity type within the time window
     * 
     * @param entityType The type of entity
     * @param currentTime Current game time in ticks
     * @return The number of valid kills within the time window
     */
    public int getKillCount(String entityType, long currentTime) {
        List<Long> timestamps = killTimestamps.get(entityType);
        if (timestamps == null) {
            return 0;
        }
        
        // Clean up old kills and count valid ones
        long cutoffTime = currentTime - ConditionalSpawnConfig.KILL_WINDOW_TICKS;
        timestamps.removeIf(time -> time < cutoffTime);
        
        return timestamps.size();
    }
    
    /**
     * Checks if the player has enough kills to trigger elite spawn
     * 
     * @param entityType The type of entity to check
     * @param threshold The required kill count
     * @param currentTime Current game time in ticks
     * @return true if player has enough kills
     */
    public boolean hasEnoughKills(String entityType, int threshold, long currentTime) {
        return getKillCount(entityType, currentTime) >= threshold;
    }
    
    /**
     * Resets kill count for a specific entity type
     * 
     * @param entityType The type of entity
     */
    public void resetKills(String entityType) {
        killTimestamps.remove(entityType);
    }
    
    @Nonnull
    @Override
    public PlayerKillTrackerComponent clone() {
        PlayerKillTrackerComponent clone = new PlayerKillTrackerComponent();
        this.killTimestamps.forEach((key, value) -> 
            clone.killTimestamps.put(key, new ArrayList<>(value))
        );
        return clone;
    }
}
