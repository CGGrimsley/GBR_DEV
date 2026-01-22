package com.kdrgold.gbr.config;

/**
 * Configuration constants for the conditional spawning system.
 * All thresholds, timings, and damage values in one centralized location.
 * 
 * @author kdrgold
 * @version 2.0.0
 */
public class ConditionalSpawnConfig {
    
    // ==================== MASTER TOGGLES ====================
    
    /**
     * Master switch for the entire conditional spawning system
     * Note: To modify settings, edit values in this file and rebuild the mod
     */
    public static final boolean ENABLE_CONDITIONAL_SPAWNING = true;
    
    /**
     * Enable verbose debug logging for troubleshooting
     */
    public static final boolean DEBUG_MODE = false;
    
    // ==================== ENTITY TYPE NAMES ====================
    
    /**
     * Entity type names must match exactly with GBR's NPC role definitions
     */
    public static final String ENTITY_WOLF = "Wolf_Black";
    public static final String ENTITY_GRIZZLY_BEAR = "Bear_Grizzly";
    public static final String ENTITY_BATTLE_HARDENED_BEAR = "Battle_Hardened_Bear";
    public static final String ENTITY_BATTLE_SCARRED_WOLF = "Battle_Scarred_Wolf";
    public static final String ENTITY_ADVENTURER_BEAR = "Bear_Adventurer";
    
    // ==================== KILL THRESHOLDS ====================
    
    /**
     * Number of wolf kills required to trigger Battle Scarred Wolf spawn chance
     */
    public static final int BATTLE_SCARRED_WOLF_THRESHOLD = 5;
    
    /**
     * Number of grizzly bear kills required to trigger Battle Hardened Bear spawn chance
     */
    public static final int BATTLE_HARDENED_BEAR_THRESHOLD = 5;
    
    /**
     * Number of Battle Hardened Bear kills required to trigger Adventurer Bear spawn chance
     */
    public static final int ADVENTURER_BEAR_THRESHOLD = 2;
    
    // ==================== TIME WINDOWS ====================
    
    /**
     * Time window for tracking kills (72 in-game hours = 5,184,000 ticks at 20 TPS)
     * Kills outside this window are not counted toward thresholds
     */
    public static final long KILL_WINDOW_TICKS = 5_184_000L; // 72 hours
    
    /**
     * Cooldown between elite spawns (10 seconds = 200 ticks at 20 TPS)
     * Prevents multiple elites from spawning in rapid succession
     * TODO: Change back to 6_000L (5 minutes) for production
     */
    public static final long SPAWN_COOLDOWN_TICKS = 200L; // 10 seconds (TESTING ONLY)
    
    // ==================== SPAWN CHANCES ====================
    
    /**
     * Spawn chance when exactly at threshold (5 kills for grizzly/wolf, 2 for battle hardened)
     */
    public static final double SPAWN_CHANCE_AT_THRESHOLD = 0.60; // 60%
    
    /**
     * Spawn chance when above threshold - guaranteed spawn
     */
    public static final double SPAWN_CHANCE_GUARANTEED = 1.0; // 100%
    
    // ==================== SPAWN DISTANCES ====================
    
    /**
     * Minimum distance from player to spawn elite (blocks)
     */
    public static final double SPAWN_DISTANCE_MIN = 10.0;
    
    /**
     * Maximum distance from player to spawn elite (blocks)
     */
    public static final double SPAWN_DISTANCE_MAX = 20.0;
    
    // ==================== BLEED MECHANICS ====================
    
    /**
     * Damage applied per bleed tick
     */
    public static final float BLEED_DAMAGE_PER_TICK = 0.1f;
    
    /**
     * Time interval between bleed damage applications (seconds)
     */
    public static final float BLEED_TICK_INTERVAL = 1.0f;
    
    /**
     * Total duration of bleed effect in damage applications
     * (200 ticks = 10 seconds at 20 TPS with 1-second intervals)
     */
    public static final int BLEED_DURATION_TICKS = 200;
    
    // ==================== HELPER METHODS ====================
    
    /**
     * Calculates spawn chance based on kill count above threshold.
     * At threshold: 60% chance
     * Above threshold: 100% guaranteed
     * 
     * @param killCount The current kill count
     * @param threshold The threshold for spawning
     * @return The spawn chance as a decimal (0.0 to 1.0)
     */
    public static double calculateSpawnChance(int killCount, int threshold) {
        if (killCount < threshold) {
            return 0.0; // Below threshold
        } else if (killCount == threshold) {
            return SPAWN_CHANCE_AT_THRESHOLD;
        } else {
            return SPAWN_CHANCE_GUARANTEED; // Any kills above threshold = guaranteed
        }
    }
    
    /**
     * Gets the appropriate elite entity type for a base entity type
     * 
     * @param baseEntityType The base entity type killed
     * @return The elite variant to spawn, or null if no elite exists
     */
    public static String getEliteVariant(String baseEntityType) {
        switch (baseEntityType) {
            case ENTITY_WOLF:
                return ENTITY_BATTLE_SCARRED_WOLF;
            case ENTITY_GRIZZLY_BEAR:
                return ENTITY_BATTLE_HARDENED_BEAR;
            case ENTITY_BATTLE_HARDENED_BEAR:
                return ENTITY_ADVENTURER_BEAR;
            default:
                return null;
        }
    }
    
    /**
     * Gets the kill threshold for a specific base entity type
     * 
     * @param baseEntityType The base entity type
     * @return The kill threshold, or 0 if entity type is not trackable
     */
    public static int getThreshold(String baseEntityType) {
        switch (baseEntityType) {
            case ENTITY_WOLF:
                return BATTLE_SCARRED_WOLF_THRESHOLD;
            case ENTITY_GRIZZLY_BEAR:
                return BATTLE_HARDENED_BEAR_THRESHOLD;
            case ENTITY_BATTLE_HARDENED_BEAR:
                return ADVENTURER_BEAR_THRESHOLD;
            default:
                return 0;
        }
    }
    
    /**
     * Checks if an entity type should be tracked for conditional spawning
     * 
     * @param entityType The entity type to check
     * @return True if this entity type triggers elite spawns
     */
    public static boolean isTrackableEntity(String entityType) {
        return entityType.equals(ENTITY_WOLF) 
            || entityType.equals(ENTITY_GRIZZLY_BEAR) 
            || entityType.equals(ENTITY_BATTLE_HARDENED_BEAR);
    }
}
